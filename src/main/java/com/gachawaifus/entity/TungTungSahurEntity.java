package com.gachawaifus.entity;

import com.gachawaifus.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * <b>Tung Tung Sahur</b> — el tronco durmiente de los Brainrot: jefe neutral con un bate de
 * béisbol que, cada vez que conecta, manda al objetivo volando lejos (el golpe del bate no es
 * solo daño: es un lanzamiento de home run). Al morir suelta {@value #BALL_REWARD} Bolitas Azules.
 *
 * <p>Mismo contrato que {@link FernanflooEntity}: neutral ({@code HurtByTargetGoal} nada más),
 * el jugador apenas le hace daño y la recompensa va al dueño de la waifu que peleó. El bate es un
 * item GeckoLib ({@code BateBeisbolItem}) visible gracias al {@code ItemInHandLayer}.
 */
public class TungTungSahurEntity extends Monster {

    /** El daño del jugador se multiplica por esto (un 20 %): la pelea es de las waifus. */
    public static final float PLAYER_DAMAGE_MULTIPLIER = 0.2F;
    /** Bolitas Azules que suelta al morir. */
    public static final int BALL_REWARD = 4;

    /** Componente horizontal del lanzamiento: en bloques por tick. */
    private static final double LAUNCH_HORIZONTAL = 2.6D;
    /** Componente vertical del lanzamiento: lo suficiente para un vuelo de ~9 bloques. */
    private static final double LAUNCH_VERTICAL = 1.35D;
    /** Cada cuántos ticks se le recuerda al jugador que sus golpes casi no cuentan. */
    private static final int HIT_MESSAGE_COOLDOWN = 80;
    /** Radio en el que se busca al jugador que se lleva la recompensa si no hubo golpe directo. */
    private static final double REWARD_SEARCH_RANGE = 32.0D;

    private int hitMessageCooldown = 0;
    @Nullable
    private UUID rewardPlayer;

    public TungTungSahurEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 70;
        this.setPersistenceRequired();
        // §6 = dorado: la madera de su cuerpo.
        this.setCustomName(Component.literal("§6Tung Tung Sahur"));
        this.setCustomNameVisible(true);
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.BATE_BEISBOL.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 320.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BatAttackGoal());
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // NEUTRAL: solo devuelve el golpe a quien le pegue.
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
    }

    /** Lanza al objetivo por los aires lejos de él (el "home run" del bate). */
    private void launchAway(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
        target.setDeltaMovement(dx / len * LAUNCH_HORIZONTAL, LAUNCH_VERTICAL, dz / len * LAUNCH_HORIZONTAL);
        target.hurtMarked = true;
        target.hasImpulse = true;
    }

    /** Aviso al jugador de que sus golpes apenas cuentan (con anti-spam por entidad). */
    private void complainTo(Player player) {
        if (this.hitMessageCooldown > 0) {
            return;
        }
        this.hitMessageCooldown = HIT_MESSAGE_COOLDOWN;
        player.displayClientMessage(
                Component.translatable("message.gachawaifus.tung_tung_sahur_resists"), true);
    }

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
            this.handOutBalls(source);
        }
        super.die(source);
    }

    /** Entrega las {@value #BALL_REWARD} Bolitas Azules y monta la fanfarria de recompensa. */
    private void handOutBalls(DamageSource source) {
        Player winner = this.resolveWinner(source);
        ItemStack balls = new ItemStack(ModItems.BLUE_BALL.get(), BALL_REWARD);

        if (winner == null) {
            this.spawnAtLocation(balls);
            return;
        }
        if (!winner.getInventory().add(balls)) {
            winner.drop(balls, false);
        }
        winner.sendSystemMessage(Component.translatable("message.gachawaifus.tung_tung_sahur_defeated",
                BALL_REWARD, BALL_REWARD));
        winner.level().playSound(null, winner.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.0F, 1.4F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 1.0D, this.getZ(),
                    40, 0.7D, 0.9D, 0.7D, 0.12D);
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

    /**
     * El bate: deja que el {@code MeleeAttackGoal} padre pegue con su cadencia normal y, si el
     * golpe conecta (el objetivo entra en invulnerabilidad), lo lanza lejos. El retroceso vanilla
     * se queda corto para un home run, así que aquí se pisa el {@code DeltaMovement} del golpeado.
     */
    private class BatAttackGoal extends MeleeAttackGoal {

        BatAttackGoal() {
            super(TungTungSahurEntity.this, 1.15D, true);
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            int hurtBefore = target.hurtTime;
            super.checkAndPerformAttack(target);
            if (target.hurtTime > hurtBefore) {
                launchAway(target);
                TungTungSahurEntity.this.level().playSound(null, TungTungSahurEntity.this.blockPosition(),
                        SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.HOSTILE, 1.4F, 0.7F);
                if (TungTungSahurEntity.this.level() instanceof ServerLevel serverLevel) {
                    Vec3 burst = target.position().add(0.0D, 1.0D, 0.0D);
                    serverLevel.sendParticles(ParticleTypes.CRIT, burst.x, burst.y, burst.z,
                            16, 0.4D, 0.6D, 0.4D, 0.25D);
                }
            }
        }
    }
}
