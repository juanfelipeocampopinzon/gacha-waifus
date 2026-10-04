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

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class EllenJoeEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120; // Inicia casi listo
    private int ultimateCooldown = 360;     // Inicia cargando
    private int invulnerableTicks = 0;

    public EllenJoeEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§bEllen Joe"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ellen_joe.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ellen_joe.idle"));
        }));

        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.ellen_joe.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.ellen_joe.special")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 10.0D)
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

            // Mirar al objetivo
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate (Endless Winter)
            if (this.ultimateCooldown <= 0 && distanceSq <= 144.0D) { // 12 bloques
                performUltimate();
            }
            // Prioridad 2: Special Skill (Sharknami)
            else if (this.specialSkillCooldown <= 0 && distanceSq <= 64.0D) { // 8 bloques
                performSpecialSkill(target);
            }
            // Prioridad 3: Normal Attack (Flash Freeze Trimming)
            else if (this.normalAttackCooldown <= 0 && distanceSq <= 16.0D) { // 4 bloques
                performNormalAttack(target);
            }
        }
    }

    /**
     * Skill 1: Normal Attack — Flash Freeze Trimming
     */
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 20; // Cooldown de 1 segundo

        this.triggerAnim("attack_controller", "attack");
        this.doHurtTarget(target);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.4F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    10, 0.3, 0.5, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    8, 0.3, 0.5, 0.3, 0.05);
        }
    }

    /**
     * Skill 2: Special Skill — Sharknami
     */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 200; // Cooldown de 10 segundos

        this.triggerAnim("attack_controller", "special");

        // Invulnerabilidad temporal durante el dash de la embestida tiburón
        this.setInvulnerable(true);
        this.invulnerableTicks = 25;

        // Dash rápido hacia el objetivo
        Vec3 direction = target.position().subtract(this.position()).normalize();
        this.setDeltaMovement(direction.x * 1.4, 0.25, direction.z * 1.4);

        // Daño de congelación profunda
        target.hurt(this.damageSources().mobAttack(this), 18.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.2F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.9F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    30, 0.8, 0.5, 0.8, 0.1);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    15, 0.4, 0.6, 0.4, 0.1);
        }
    }

    /**
     * Skill 3: Ultimate — Endless Winter (Vórtice Helado AoE)
     */
    private void performUltimate() {
        this.ultimateCooldown = 700; // Cooldown de 35 segundos

        Level level = this.level();
        LivingEntity owner = this.getOwner();

        // Anuncio en el chat
        if (owner instanceof Player player) {
            player.sendSystemMessage(Component.literal("§b[Ellen Joe] §i\"Endless Winter... chill out!\""));
        }

        // Efectos de sonido dramáticos
        level.playSound(null, this.blockPosition(), SoundEvents.POWDER_SNOW_FALL, SoundSource.PLAYERS, 2.0F, 0.5F);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 1.8F);
        level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.6F);

        // Partículas masivas de vórtice helado
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 360; i += 10) {
                double rad = Math.toRadians(i);
                double radius = 4.5;
                double px = this.getX() + Math.cos(rad) * radius;
                double pz = this.getZ() + Math.sin(rad) * radius;
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, px, this.getY() + 0.2, pz, 3, 0.1, 0.5, 0.1, 0.05);
                serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL, px, this.getY() + 1.0, pz, 2, 0.1, 0.5, 0.1, 0.05);
            }
            serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.0, 0.0, 0.0, 0.0);
        }

        // Buffs al dueño y a Ellen
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));

        if (owner != null) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
        }

        // Daño masivo de hielo y ralentización IV a enemigos en un radio de 10 bloques
        AABB area = this.getBoundingBox().inflate(10.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity != owner && !(entity instanceof AbstractWaifuEntity));

        for (LivingEntity enemy : targets) {
            enemy.hurt(this.damageSources().indirectMagic(this, this), 30.0F);
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3)); // Slowness IV
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        }
    }
}
