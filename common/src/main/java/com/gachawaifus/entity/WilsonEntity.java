package com.gachawaifus.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * <b>Wilson</b> — el médico amigo del Doctor House, y su único aliado en la pelea: aparece de
 * la nada cuando alguien se enfrenta a House y entra en la pelea que este tenga.
 *
 * <p>No existe en la naturaleza (solo lo invoca House, o alguien con su huevo del creativo),
 * no suelta nada y no busca pelea por su cuenta: devuelve los golpes que recibe y, cada poco,
 * copia el objetivo de House para que los dos lien al mismo. Si su House desaparece (y no es
 * por carga de chunks), se esfuma con un ¡puf! en vez de quedarse vagando para siempre.
 */
public class WilsonEntity extends Monster {

    /** Cada cuántos ticks re-sincroniza su objetivo con el de House. */
    private static final int HOUSE_SYNC_INTERVAL = 20;
    /** Ticks seguidos sin su House (fuera de descarga de chunks) antes de esfumarse. */
    private static final int HOUSE_MISSING_LIMIT = 100;
    /** Radio en el que busca a su House para copiarle el objetivo. */
    private static final double HOUSE_SEARCH_RANGE = 32.0D;

    /** House que lo invocó (@code null si lo puso el jugador con el huevo). */
    @Nullable
    private UUID houseId;
    private int houseSyncCooldown;
    private int houseMissingTicks;

    public WilsonEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 20;
        this.setPersistenceRequired();
        // §8 = gris oscuro (libre): el socio discreto del §7 de House.
        this.setCustomName(Component.literal("§8Wilson"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        // NEUTRAL como House: devuelve el golpe a quien le pegue, nada más.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    public void setHouseId(@Nullable UUID houseId) {
        this.houseId = houseId;
    }

    @Nullable
    public UUID getHouseId() {
        return this.houseId;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || this.houseId == null) {
            return;
        }
        if (--this.houseSyncCooldown > 0) {
            return;
        }
        this.houseSyncCooldown = HOUSE_SYNC_INTERVAL;

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(serverLevel.getEntity(this.houseId) instanceof DoctorHouseEntity house)) {
            // Sin House a la vista un buen rato: se esfuma. (Con el chunk descargado no hay
            // tick, así que el contador solo avanza cuando aquí de verdad ya no está.)
            if (++this.houseMissingTicks > HOUSE_MISSING_LIMIT) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 15, 0.3D, 0.6D, 0.3D, 0.02D);
                this.discard();
            }
            return;
        }
        this.houseMissingTicks = 0;
        if (this.getTarget() != null) {
            return;
        }
        // Si House está peleando, Wilson entra en la misma pelea.
        LivingEntity target = house.getTarget();
        if (target != null && target.isAlive()) {
            this.setTarget(target);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Sus drops son cero y su vida corta: en creativo no hace falta torturarlo.
        if (source.getEntity() instanceof Player player
                && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return super.hurt(source, amount);
    }
}
