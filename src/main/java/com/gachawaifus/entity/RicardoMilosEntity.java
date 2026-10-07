package com.gachawaifus.entity;

import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * <b>Ricardo Milos</b> — el jefe de recompensa del mod: un enemigo neutral que suelta
 * {@value #PULL_REWARD} tiradas (Bolitas Rosas) al morir.
 *
 * <p>Tres reglas de diseño, todas pedidas a propósito:
 * <ol>
 *   <li><b>Es neutral.</b> No busca a nadie por su cuenta: solo lleva
 *       {@link HurtByTargetGoal}, así que devuelve el golpe a quien le pegue (normalmente una
 *       waifu) y nunca empieza una pelea contra el jugador.</li>
 *   <li><b>El jugador casi no le hace daño.</b> Sus golpes valen un
 *       {@value #PLAYER_DAMAGE_MULTIPLIER} del daño normal (un 20 %): la pelea es de las waifus,
 *       así que hace falta llevar equipo (3 waifus o más) para tumbarlo.</li>
 *   <li><b>Es fuerte.</b> Mucha vida, armadura y resistencia a empujones, más una
 *       <i>Sobrecarga Etérea</i> en área que telegrafía el suelo un segundo y medio antes de
 *       estallar (18 de daño y empujón a todo el que esté dentro).</li>
 * </ol>
 *
 * <p>Al morir entrega las 10 Bolitas Rosas al dueño de la waifu que le dio el golpe de gracia
 * (o al jugador que lo remató); si no hay nadie cerca, caen al suelo.
 */
public class RicardoMilosEntity extends Monster {

    /** El daño del jugador se multiplica por esto (un 20 %): sin waifus el combate no avanza. */
    public static final float PLAYER_DAMAGE_MULTIPLIER = 0.2F;
    /** Tiradas (Bolitas Rosas) que suelta al morir. */
    public static final int PULL_REWARD = 10;

    /** Radio de la Sobrecarga Etérea. */
    private static final double OVERLOAD_RADIUS = 7.0D;
    /** Daño de la Sobrecarga Etérea. */
    private static final float OVERLOAD_DAMAGE = 18.0F;
    /** Ticks de telegrafiado antes de estallar (30 = 1,5 s). */
    private static final int OVERLOAD_CHARGE_TICKS = 30;
    /** Recarga entre sobrecargas. */
    private static final int OVERLOAD_COOLDOWN_TICKS = 160;
    /** Cada cuántos ticks se le recuerda al jugador que sus golpes casi no cuentan. */
    private static final int HIT_MESSAGE_COOLDOWN = 80;
    /** Radio en el que se busca al jugador que se lleva la recompensa si no hubo golpe directo. */
    private static final double REWARD_SEARCH_RANGE = 32.0D;
    /** Vida del jefe: alta a propósito (necesita un equipo, no una waifu suelta). */
    private static final double MAX_HEALTH = 500.0D;
    /**
     * Volumen del tema de pelea: el alcance audible de un sonido posicional es {@code 16 × volumen},
     * así que con 4 se oye a unos 64 bloques (lo justo para "entrar en pelea con él").
     */
    private static final float THEME_VOLUME = 4.0F;
    /** Ticks sin objetivo tras los que la música se da por terminada y puede volver a sonar. */
    private static final int THEME_SILENCE_TICKS = 400;
    /** Alcance audible del tema en bloques ({@code 16 × THEME_VOLUME}); sin nadie dentro, se rearma. */
    private static final double THEME_RANGE = 64.0D;

    private int overloadCooldown = 80;
    private int overloadCharge = 0;
    private int hitMessageCooldown = 0;
    /** ¿Está sonando el tema de pelea? Evita reiniciarlo en cada tick. */
    private boolean themePlaying = false;
    /** Ticks seguidos sin objetivo (sirve para rearmar la música cuando la pelea se enfría). */
    private int themeSilence = 0;
    /** Jugador que se lleva las tiradas (dueño de la última waifu que le hizo daño). */
    @Nullable
    private UUID rewardPlayer;

    public RicardoMilosEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 100;
        this.setPersistenceRequired();   // un jefe no desaparece por alejarse
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // NEUTRAL: no persigue a nadie por su cuenta. Solo devuelve el golpe a quien le pegue.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /**
     * El jugador solo le hace una quinta parte del daño; las waifus, el daño completo.
     *
     * <p>Como el golpe del jugador <b>sí</b> cuenta (poco), también provoca la represalia: si el
     * jugador le pega, el jefe se defiende de él. Si no lo toca, lo ignora.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
            amount *= PLAYER_DAMAGE_MULTIPLIER;
            if (amount <= 0.0F) {
                return false;
            }
            this.complainTo(player);
        }
        this.rememberRewardOwner(attacker);
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.hitMessageCooldown > 0) {
            this.hitMessageCooldown--;
        }
        if (this.level().isClientSide) {
            return;
        }

        // 1) Sobrecarga en curso: telegrafía y estalla.
        if (this.overloadCharge > 0) {
            this.overloadCharge--;
            this.telegraphOverload();
            if (this.overloadCharge == 0) {
                this.detonateOverload();
            }
            return;
        }

        // 2) Recarga.
        if (this.overloadCooldown > 0) {
            this.overloadCooldown--;
            return;
        }

        // 3) ¿Hay alguien a quien devolverle el golpe lo bastante cerca? Entonces carga.
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.distanceToSqr(target) <= (OVERLOAD_RADIUS + 4.0D) * (OVERLOAD_RADIUS + 4.0D)) {
            this.overloadCharge = OVERLOAD_CHARGE_TICKS;
            this.overloadCooldown = OVERLOAD_COOLDOWN_TICKS;
            this.level().playSound(null, this.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE,
                    SoundSource.HOSTILE, 1.1F, 0.7F);
        }

        // 4) Música de pelea (entra al empezar y se corta al morir).
        this.tickTheme();
    }

    /**
     * Tema del jefe: suena <b>una vez</b> cuando empieza la pelea —o sea, cuando consigue un
     * objetivo, que es justo cuando alguien (una waifu) le pega— y no se reinicia mientras dure.
     * Si se queda sin objetivo un buen rato, o si no queda ningún jugador escuchando, se rearma
     * para la siguiente pelea.
     *
     * <p>El corte al morir lo hacen dos sitios: {@link #stopTheme()} aquí y, por si el paquete se
     * pierde, {@code client/audio/BossThemeWatch} (que corta el sonido en cuanto no queda ningún
     * jefe vivo y herido cerca).
     */
    private void tickTheme() {
        if (!this.hasPlayerWithin(THEME_RANGE)) {
            // Nadie dentro del alcance: el tema se rearma para cuando vuelva alguien a la pelea.
            this.themePlaying = false;
            this.themeSilence = 0;
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.themeSilence = 0;
            if (!this.themePlaying) {
                this.themePlaying = true;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.RICARDO_MILOS_THEME.get(), SoundSource.MUSIC, THEME_VOLUME, 1.0F);
            }
        } else if (this.themePlaying && ++this.themeSilence > THEME_SILENCE_TICKS) {
            this.themePlaying = false;
            this.themeSilence = 0;
        }
    }

    /** ¿Hay algún jugador dentro del alcance audible del tema? */
    private boolean hasPlayerWithin(double range) {
        double maxDistanceSq = range * range;
        for (Player player : this.level().players()) {
            if (player.isAlive() && !player.isSpectator() && player.distanceToSqr(this) <= maxDistanceSq) {
                return true;
            }
        }
        return false;
    }

    /** Corta el tema: si el jefe muere, la música no debe seguir sonando. */
    private void stopTheme() {
        this.themePlaying = false;
        this.themeSilence = 0;
        if (this.level() instanceof ServerLevel serverLevel) {
            for (ServerPlayer player : serverLevel.players()) {
                player.connection.send(new ClientboundStopSoundPacket(
                        ModSounds.RICARDO_MILOS_THEME_ID, SoundSource.MUSIC));
            }
        }
    }

    /** Dibuja el anillo de aviso en el suelo: quien esté dentro, va a cobrar. */
    private void telegraphOverload() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int points = 40;
        for (int i = 0; i < points; i++) {
            double angle = (i / (double) points) * Math.PI * 2.0D;
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    this.getX() + Math.cos(angle) * OVERLOAD_RADIUS,
                    this.getY() + 0.15D,
                    this.getZ() + Math.sin(angle) * OVERLOAD_RADIUS,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (this.overloadCharge % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0D, this.getZ(),
                    12, 0.5D, 0.7D, 0.5D, 0.02D);
        }
    }

    /** La Sobrecarga Etérea: daño en área, lentitud y empujón a las waifus de alrededor. */
    private void detonateOverload() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(OVERLOAD_RADIUS);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity.isAlive() && !(entity instanceof RicardoMilosEntity)
                        && this.overloadReaches(entity));

        for (LivingEntity victim : victims) {
            victim.hurt(this.damageSources().mobAttack(this), OVERLOAD_DAMAGE);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            victim.knockback(1.1D, -dx / length, -dz / length);
        }

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.6D, this.getZ(),
                2, 0.4D, 0.3D, 0.4D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.8D, this.getZ(),
                30, 0.6D, 0.4D, 0.6D, 0.08D);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 1.4F, 0.8F);
    }

    /**
     * Neutralidad estricta con el jugador: la sobrecarga solo le alcanza si él ha empezado
     * (o sea, si es su objetivo por haberle pegado). A las waifus les da siempre.
     */
    private boolean overloadReaches(LivingEntity entity) {
        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
            return this.getTarget() == player;
        }
        return true;
    }

    /** Aviso al jugador de que sus golpes apenas cuentan (con anti-spam por entidad). */
    private void complainTo(Player player) {
        if (this.hitMessageCooldown > 0) {
            return;
        }
        this.hitMessageCooldown = HIT_MESSAGE_COOLDOWN;
        player.displayClientMessage(
                Component.translatable("message.gachawaifus.ricardo_milos_resists"), true);
    }

    /** Apunta quién se lleva las tiradas: el dueño de la waifu que pega, o el jugador. */
    private void rememberRewardOwner(@Nullable Entity attacker) {
        if (attacker instanceof AbstractWaifuEntity waifu) {
            if (waifu.getOwner() instanceof Player owner) {
                this.rewardPlayer = owner.getUUID();
            }
        } else if (attacker instanceof Player player) {
            this.rewardPlayer = player.getUUID();
        }
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.stopTheme();
            this.handOutPulls(source);
        }
        super.die(source);
    }

    /** Segundo aviso de corte: cubre muertes que no pasan por {@link #die(DamageSource)}. */
    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            this.stopTheme();
        }
        super.remove(reason);
    }

    /** Entrega las {@value #PULL_REWARD} tiradas y monta la fanfarria de recompensa. */
    private void handOutPulls(DamageSource source) {
        Player winner = this.resolveWinner(source);
        ItemStack pulls = new ItemStack(ModItems.PINK_BALL.get(), PULL_REWARD);

        if (winner == null) {
            // Nadie cerca: las tiradas quedan en el suelo, donde cayó.
            this.spawnAtLocation(pulls);
            return;
        }
        if (!winner.getInventory().add(pulls)) {
            winner.drop(pulls, false);
        }
        winner.sendSystemMessage(Component.translatable("message.gachawaifus.ricardo_milos_defeated",
                PULL_REWARD, PULL_REWARD));
        winner.level().playSound(null, winner.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.0F, 1.4F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0D, this.getZ(),
                    3, 0.3D, 0.5D, 0.3D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(),
                    50, 0.7D, 0.9D, 0.7D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.HEART, winner.getX(), winner.getY() + 1.6D, winner.getZ(),
                    10, 0.5D, 0.4D, 0.5D, 0.02D);
        }
    }

    /** Quién se lleva la recompensa: el último jugador apuntado, el asesino o el más cercano. */
    @Nullable
    private Player resolveWinner(DamageSource source) {
        if (this.rewardPlayer != null) {
            Player player = this.level().getPlayerByUUID(this.rewardPlayer);
            if (player != null && player.isAlive()) {
                return player;
            }
        }
        if (source.getEntity() instanceof AbstractWaifuEntity waifu
                && waifu.getOwner() instanceof Player owner) {
            return owner;
        }
        if (source.getEntity() instanceof Player player) {
            return player;
        }
        AABB area = this.getBoundingBox().inflate(REWARD_SEARCH_RANGE);
        List<Player> nearby = this.level().getEntitiesOfClass(Player.class, area,
                player -> player.isAlive() && !player.isSpectator());
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Player player : nearby) {
            double distance = player.distanceToSqr(this);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = player;
            }
        }
        return closest;
    }
}
