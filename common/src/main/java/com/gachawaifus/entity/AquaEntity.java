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

/** Aqua (KonoSuba) — diosa del Agua: soporte, cura y bendice a su dueño. */
public class AquaEntity extends AbstractWaifuEntity {
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 100;
    private int ultimateCooldown = 300;
    private int blessingCooldown = 60;

    public AquaEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§3Aqua"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 90.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0F);
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
        if (this.blessingCooldown > 0) this.blessingCooldown--;

        // Bendición pasiva: de vez en cuando cura al dueño si lo tiene tocado.
        if (!this.level().isClientSide() && this.blessingCooldown <= 0) {
            this.blessingCooldown = 200;
            LivingEntity owner = this.getOwner();
            if (owner instanceof Player p && p.getHealth() < p.getMaxHealth() * 0.75F) {
                p.heal(6.0F);
                p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.WITCH, p.getX(), p.getY() + 1.4, p.getZ(), 10, 0.4, 0.6, 0.4, 0.1);
                }
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.ultimateCooldown <= 0 && distSq <= 64.0D) {
                performUltimate();
            } else if (this.specialSkillCooldown <= 0 && distSq <= 36.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 24;
        this.doHurtTarget(target);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.3, 0.5, 0.3, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.FISH_SWIM, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 220;
        target.hurt(this.damageSources().mobAttack(this), 11.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.BUBBLE, target.getX(), target.getY() + 1.0, target.getZ(), 16, 0.4, 0.7, 0.4, 0.05);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GUARDIAN_ATTACK, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    private void performUltimate() {
        this.ultimateCooldown = 700;
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.literal("§3Aqua §o\"¡Bendición de la Diosa del Agua! Sanar, sanar y sanar.\""));
            p.heal(14.0F);
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        }
        AABB area = this.getBoundingBox().inflate(7.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 24.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
        }
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 40, 0.9, 0.9, 0.9, 0.2);
            sl.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY() + 0.6, this.getZ(), 30, 0.9, 0.7, 0.9, 0.05);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.1F, 1.5F);
    }
}
