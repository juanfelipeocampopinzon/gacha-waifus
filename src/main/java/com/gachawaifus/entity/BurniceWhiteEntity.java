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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BurniceWhiteEntity extends AbstractWaifuEntity {
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120;
    private int ultimateCooldown = 360;
    private int invulnerableTicks = 0;

    public BurniceWhiteEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 8.0D)
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

        if (this.invulnerableTicks > 0) {
            this.invulnerableTicks--;
            if (this.invulnerableTicks == 0) {
                this.setInvulnerable(false);
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distanceSq = this.distanceToSqr(target);

            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate (Burnice Supernova)
            if (this.ultimateCooldown <= 0 && distanceSq <= 144.0D) {
                performUltimate();
            }
            // Prioridad 2: Special Skill (Jet Flamethrower)
            else if (this.specialSkillCooldown <= 0 && distanceSq <= 64.0D) {
                performSpecialSkill(target);
            }
            // Prioridad 3: Normal Attack (Scorching Slash)
            else if (this.normalAttackCooldown <= 0 && distanceSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    /**
     * Skill 1: Normal Attack — Scorching Slash
     */
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 20;

        this.doHurtTarget(target);
        target.igniteForSeconds(3);

        this.level().playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    12, 0.3, 0.5, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    4, 0.2, 0.4, 0.2, 0.05);
        }
    }

    /**
     * Skill 2: Special Skill — Jet Flamethrower
     */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 200;

        this.setInvulnerable(true);
        this.invulnerableTicks = 25;

        Vec3 direction = target.position().subtract(this.position()).normalize();
        this.setDeltaMovement(direction.x * 1.3, 0.2, direction.z * 1.3);

        target.hurt(this.damageSources().inFire(), 16.0F);
        target.igniteForSeconds(6);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.9F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    35, 0.8, 0.5, 0.8, 0.1);
            serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    10, 0.3, 0.5, 0.3, 0.05);
        }
    }

    /**
     * Skill 3: Ultimate — Burnice Supernova
     */
    private void performUltimate() {
        this.ultimateCooldown = 700;

        Level level = this.level();
        LivingEntity owner = this.getOwner();

        if (owner instanceof Player player) {
            player.sendSystemMessage(Component.literal("§c[Burnice White] §i\"Time for a scorching performance!\""));
        }

        level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.0F);
        level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0F, 0.8F);

        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 360; i += 12) {
                double rad = Math.toRadians(i);
                double radius = 4.5;
                double px = this.getX() + Math.cos(rad) * radius;
                double pz = this.getZ() + Math.sin(rad) * radius;
                serverLevel.sendParticles(ParticleTypes.FLAME, px, this.getY() + 0.5, pz, 4, 0.1, 0.5, 0.1, 0.08);
                serverLevel.sendParticles(ParticleTypes.LAVA, px, this.getY() + 1.0, pz, 2, 0.1, 0.5, 0.1, 0.05);
            }
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.2, 0.2, 0.2, 0.0);
        }

        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));

        if (owner != null) {
            owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 0));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        }

        AABB area = this.getBoundingBox().inflate(10.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity != owner && !(entity instanceof AbstractWaifuEntity));

        for (LivingEntity enemy : targets) {
            enemy.hurt(this.damageSources().inFire(), 25.0F);
            enemy.igniteForSeconds(8);
        }
    }
}