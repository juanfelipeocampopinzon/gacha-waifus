package com.gachawaifus.entity;

import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>Fernanfloo</b> — jefe neutral: ya no anda con chorizos, ahora pelea con sus <b>perritos
 * Curly</b>. No busca pelea, pero cuando alguien la busca invoca hasta dos Curlys a la vez (y
 * repuestos conforme caen) para que le hagan de guardaespaldas. Al morir suelta
 * {@value #BALL_REWARD} Bolitas Rosas (tiradas del banner de eventos).
 *
 * <p>Mismo contrato que {@link DoctorHouseEntity}: neutral ({@link HurtByTargetGoal}), el jugador
 * apenas le hace daño, y la recompensa va al dueño de la waifu que peleó (o al jugador que más
 * cerca esté). Los Curly se van con él cuando muere, igual que los Wilson de House.
 */
public class FernanflooEntity extends Monster {

    /** El daño del jugador se multiplica por esto (un 20 %): la pelea es de las waifus. */
    public static final float PLAYER_DAMAGE_MULTIPLIER = 0.2F;
    /** Bolitas Rosas (tiradas del banner de eventos) que suelta al morir. */
    public static final int BALL_REWARD = 3;

    /** Curlys vivos que admite a la vez. */
    private static final int MAX_MINIONS_ALIVE = 2;
    /** Curlys totales por pelea: pasado este número deja de invocar (anti-granja). */
    private static final int MAX_MINIONS_SUMMONED = 6;
    /** Recarga entre invocaciones (200 ticks = 10 s). */
    private static final int SUMMON_COOLDOWN_TICKS = 200;
    /** Radio (en bloques) alrededor de Fernanfloo donde aparecen los Curlys. */
    private static final double SUMMON_RADIUS = 2.5D;
    /** Cada cuántos ticks se le recuerda al jugador que sus golpes casi no cuentan. */
    private static final int HIT_MESSAGE_COOLDOWN = 80;
    /** Vida del jefe: más frágil que House. */
    private static final double MAX_HEALTH = 280.0D;
    /** Radio en el que se busca al jugador que se lleva la recompensa si no hubo golpe directo. */
    private static final double REWARD_SEARCH_RANGE = 32.0D;

    private int hitMessageCooldown = 0;
    private int summonCooldown = 100;
    private int minionsSummoned = 0;
    /** Curlys que ha invocado (para despedirlos al morir). */
    private final List<UUID> minions = new ArrayList<>();
    /** Jugador que se lleva las Bolitas Rosas (dueño de la última waifu que le hizo daño). */
    @Nullable
    private UUID rewardPlayer;

    public FernanflooEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 60;
        this.setPersistenceRequired();
        // §2 = verde oscuro: el color de su gorra.
        this.setCustomName(Component.literal("§2Fernanfloo"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // NEUTRAL: no persigue a nadie por su cuenta. Solo devuelve el golpe a quien le pegue.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /** El jugador solo le hace una quinta parte del daño; las waifus, el daño completo. */
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
        if (this.summonCooldown > 0) {
            this.summonCooldown--;
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && this.canSummon()) {
            this.summonMinions();
        }
    }

    /** ¿Toca invocar? Solo con objetivo vivo, hueco libre, recarga hecha y cupo sin agotar. */
    private boolean canSummon() {
        if (this.minionsSummoned >= MAX_MINIONS_SUMMONED || this.aliveMinions() >= MAX_MINIONS_ALIVE) {
            return false;
        }
        return this.summonCooldown <= 0;
    }

    /** Curlys de la lista que siguen vivos en el mundo. */
    private int aliveMinions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return 0;
        }
        int alive = 0;
        for (UUID id : this.minions) {
            if (serverLevel.getEntity(id) instanceof CurlyEntity curly && curly.isAlive()) {
                alive++;
            }
        }
        return alive;
    }

    /** Trae a Curly: hasta llenar el cupo de vivos, con puff de partículas y su ladrido. */
    private void summonMinions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int summoned = 0;
        while (this.aliveMinions() + summoned < MAX_MINIONS_ALIVE) {
            CurlyEntity curly = this.findSpawnSpot();
            if (curly == null) {
                break;
            }
            curly.setFernanflooId(this.getUUID());
            serverLevel.addFreshEntity(curly);
            this.minions.add(curly.getUUID());
            summoned++;

            serverLevel.sendParticles(ParticleTypes.POOF,
                    curly.getX(), curly.getY() + 0.6D, curly.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.02D);
        }
        if (summoned > 0) {
            this.minionsSummoned += summoned;
            this.summonCooldown = SUMMON_COOLDOWN_TICKS;
            this.level().playSound(null, this.blockPosition(), SoundEvents.WOLF_GROWL,
                    SoundSource.HOSTILE, 1.0F, 1.3F);
        } else {
            // Sin sitio donde aparecer: se rearma la recarga para no reintentar cada tick.
            this.summonCooldown = 40;
        }
    }

    /** Busca una posición libre alrededor de Fernanfloo para el nuevo Curly. */
    @Nullable
    private CurlyEntity findSpawnSpot() {
        for (int attempt = 0; attempt < 4; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double ox = Math.cos(angle) * SUMMON_RADIUS;
            double oz = Math.sin(angle) * SUMMON_RADIUS;
            CurlyEntity curly = ModEntities.CURLY.get().create(this.level());
            if (curly == null) {
                return null;
            }
            curly.moveTo(this.getX() + ox, this.getY(), this.getZ() + oz, this.getYRot(), 0.0F);
            if (this.level().noCollision(curly, curly.getBoundingBox())) {
                return curly;
            }
        }
        return null;
    }

    /** Aviso al jugador de que sus golpes apenas cuentan (con anti-spam por entidad). */
    private void complainTo(Player player) {
        if (this.hitMessageCooldown > 0) {
            return;
        }
        this.hitMessageCooldown = HIT_MESSAGE_COOLDOWN;
        player.displayClientMessage(
                Component.translatable("message.gachawaifus.fernanfloo_resists"), true);
    }

    /** Apunta quién se lleva las Bolitas Rosas: el dueño de la waifu que pega, o el jugador. */
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
            this.dismissMinions();
            this.handOutBalls(source);
        }
        super.die(source);
    }

    /** Cuando Fernanfloo muere, sus Curlys se van con él: se descartan sin drops. */
    private void dismissMinions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (UUID id : this.minions) {
            if (serverLevel.getEntity(id) instanceof CurlyEntity curly && curly.isAlive()) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        curly.getX(), curly.getY() + 0.6D, curly.getZ(), 12, 0.3D, 0.4D, 0.3D, 0.02D);
                curly.discard();
            }
        }
        this.minions.clear();
    }

    /** Entrega las {@value #BALL_REWARD} Bolitas Rosas y monta la fanfarria de recompensa. */
    private void handOutBalls(DamageSource source) {
        Player winner = this.resolveWinner(source);
        ItemStack balls = new ItemStack(ModItems.PINK_BALL.get(), BALL_REWARD);

        if (winner == null) {
            // Nadie cerca: las bolitas quedan en el suelo, donde cayó.
            this.spawnAtLocation(balls);
            return;
        }
        if (!winner.getInventory().add(balls)) {
            winner.drop(balls, false);
        }
        winner.sendSystemMessage(Component.translatable("message.gachawaifus.fernanfloo_defeated",
                BALL_REWARD, BALL_REWARD));
        winner.level().playSound(null, winner.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.0F, 1.4F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(),
                    40, 0.7D, 0.9D, 0.7D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, winner.getX(), winner.getY() + 1.6D,
                    winner.getZ(), 10, 0.5D, 0.4D, 0.5D, 0.02D);
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
