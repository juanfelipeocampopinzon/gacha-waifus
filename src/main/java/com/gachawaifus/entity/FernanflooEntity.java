package com.gachawaifus.entity;

import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModSounds;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * <b>Fernanfloo</b> — jefe neutral que deambula por el mundo con su chorizo en la mano: no busca
 * pelea, pero cada golpe suyo suena con el "¡CHORIZO!" de su escena. Al morir suelta
 * {@value #BALL_REWARD} Bolitas Rosas (tiradas del banner de eventos).
 *
 * <p>Mismo contrato que {@link DoctorHouseEntity}: neutral ({@link HurtByTargetGoal}), el jugador
 * apenas le hace daño, y la recompensa va al dueño de la waifu que peleó (o al jugador que más
 * cerca esté). El chorizo es un item GeckoLib ({@code ChorizoItem}) que se ve en su mano gracias
 * a {@code ItemInHandLayer} en {@code FernanflooRenderer}.
 */
public class FernanflooEntity extends Monster {

    /** El daño del jugador se multiplica por esto (un 20 %): la pelea es de las waifus. */
    public static final float PLAYER_DAMAGE_MULTIPLIER = 0.2F;
    /** Bolitas Rosas (tiradas del banner de eventos) que suelta al morir. */
    public static final int BALL_REWARD = 3;

    /** Cada cuántos ticks se le recuerda al jugador que sus golpes casi no cuentan. */
    private static final int HIT_MESSAGE_COOLDOWN = 80;
    /** Vida del jefe: más frágil que House, pero pega más fuerte. */
    private static final double MAX_HEALTH = 280.0D;
    /** Radio en el que se busca al jugador que se lleva la recompensa si no hubo golpe directo. */
    private static final double REWARD_SEARCH_RANGE = 32.0D;

    private int hitMessageCooldown = 0;
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
        this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CHORIZO.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
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

    /** Cada golpe suyo suena con el "¡CHORIZO!" de su escena (segundos 5-7 del video). */
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.level() instanceof ServerLevel) {
            this.level().playSound(null, this.blockPosition(), ModSounds.FERNANFLOO_ATTACK.get(),
                    SoundSource.HOSTILE, 1.0F, 1.0F);
        }
        return hit;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.hitMessageCooldown > 0) {
            this.hitMessageCooldown--;
        }
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
            this.handOutBalls(source);
        }
        super.die(source);
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
