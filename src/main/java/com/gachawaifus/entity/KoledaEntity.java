package com.gachawaifus.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.List;

/**
 * Koleda Belobog — "Belobog Heavy Industries" (Fuego / Stun) de Zenless Zone Zero.
 *
 * <p>Usa el <b>modelo de jugador slim</b> (igual que Von Lycaon y Rina), NO GeckoLib: no tiene
 * {@code .geo.json} ni animaciones propias, así que no se puede apoyar en {@code GeoEntityRenderer}.
 *
 * <p>Kit de tres habilidades, con los cooldowns y atributos del manual del proyecto:
 * <ul>
 *   <li><b>Demolition Blast</b> (normal): impacto cuerpo a cuerpo a 4 bloques, cooldown 24 ticks.</li>
 *   <li><b>Explosive Hammer</b> (especial): martillazo de fuego a 6 bloques, daño en área,
 *       {@code MOVEMENT_SLOWDOWN} y empuje; buff de {@code DAMAGE_BOOST} para el dueño y para ella.
 *       Cooldown 220 ticks.</li>
 *   <li><b>Furnace Firepower</b> (ultimate): estallido de 8 bloques, daño masivo, {@code FLAME} y
 *       {@code EXPLOSION}, buffs al dueño y a sí misma, fanfarria y línea de chat. Cooldown 750
 *       ticks.</li>
 * </ul>
 *
 * <p>Reglas de seguridad del proyecto: en los bucles de daño se excluyen el dueño, ella misma y
 * cualquier {@link AbstractWaifuEntity}; los buffs en área solo caen sobre el dueño y sobre ella.
 */
public class KoledaEntity extends AbstractWaifuEntity {
    // Cooldowns: los iniciales arrancan "casi listos"; los de uso son los reales.
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120;
    private int ultimateCooldown = 360;

    public KoledaEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§cKoleda"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.35D, 6.0F, 2.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                entity -> entity instanceof Enemy));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.normalAttackCooldown > 0) this.normalAttackCooldown--;
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            // 64 = 8 bloques (ultimate) | 36 = 6 bloques (especial) | 16 = 4 bloques (normal)
            if (this.ultimateCooldown <= 0 && distSq <= 64.0D) {
                performUltimate();
            } else if (this.specialSkillCooldown <= 0 && distSq <= 36.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    /** Demolition Blast: martillazo corto con chispas. Cooldown 24 ticks. */
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 24;
        this.doHurtTarget(target);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(),
                    8, 0.3, 0.5, 0.3, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.1F);
    }

    /** Explosive Hammer: daño en área, ralentiza y empuja. Cooldown 220 ticks. */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 220;
        LivingEntity owner = this.getOwner();
        AABB area = this.getBoundingBox().inflate(5.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));

        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().mobAttack(this), 14.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            // Empuje: aleja del punto de impacto (misma idea que el resto del mod, sin métodos inventados).
            double dx = e.getX() - this.getX();
            double dz = e.getZ() - this.getZ();
            double len = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            e.push(dx / len * 0.6D, 0.35D, dz / len * 0.6D);
        }

        // Buffs del especial: SOLO el dueño y ella misma.
        if (owner instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        }
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(),
                    12, 0.4, 0.5, 0.4, 0.02);
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + 0.5, target.getZ(),
                    6, 0.4, 0.4, 0.4, 0.01);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.9F);
    }

    /** Furnace Firepower: estallido de 8 bloques. Cooldown 750 ticks. */
    private void performUltimate() {
        this.ultimateCooldown = 750;
        LivingEntity owner = this.getOwner();

        // Buffs en área: SOLO el dueño y ella misma.
        if (owner instanceof Player player) {
            player.sendSystemMessage(Component.literal("§cKoleda §o\"¡Sientan el calor del impacto!\""));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0));
        }
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));

        AABB area = this.getBoundingBox().inflate(8.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 40.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(),
                    2, 0.5, 0.5, 0.5, 0.1);
            sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(),
                    50, 1.0, 0.3, 1.0, 0.5);
            sl.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 0.5, this.getZ(),
                    10, 0.8, 0.2, 0.8, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
    }
}
