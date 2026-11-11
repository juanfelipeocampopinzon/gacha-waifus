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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Akita Neru (Vocaloid) — Rol Attack (Color Naranja).
 * La diva perezosa: no le interesa el concierto, pero pega dos veces seguidas y más rápido que
 * nadie, y su reflejo se lo pasa entero a quien la invoca.
 *
 * <p>Modelo de Blockbench con GeckoLib (geo/akita_neru.geo.json): la melena lateral del hueso
 * {@code coleta} solo existe ahí, y con ella el cuerpo deja de ser la malla de jugador.
 */
public class AkitaNeruEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 90;
    private int ultimateCooldown = 320;
    /** Golpe pendiente de la segunda pasada del doble ataque. */
    private LivingEntity pendingFollowUp;
    private int followUpTicks;

    public AkitaNeruEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§6Akita Neru"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.akita_neru.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.akita_neru.idle"));
        }).setAnimationSpeedHandler(WaifuGeoWalk::cadence));
        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.akita_neru.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.akita_neru.special"))
                .triggerableAnim("ultimate", RawAnimation.begin().thenPlay("animation.akita_neru.ultimate")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 88.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.50D, 6.0F, 2.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.05D));
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
        if (this.isSinging()) {
            return; // en el concierto no ataca: se queda plantada cantando
        }
        if (this.normalAttackCooldown > 0) this.normalAttackCooldown--;
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;

        if (this.pendingFollowUp != null) {
            if (--this.followUpTicks <= 0 || !this.pendingFollowUp.isAlive()
                    || this.distanceToSqr(this.pendingFollowUp) > 16.0D) {
                this.pendingFollowUp = null;
            } else {
                this.triggerAnim("attack_controller", "attack"); // la segunda pasada se ve como tal
                this.doHurtTarget(this.pendingFollowUp);
                if (this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.CRIT, this.pendingFollowUp.getX(),
                            this.pendingFollowUp.getY() + 1.0, this.pendingFollowUp.getZ(),
                            6, 0.25, 0.35, 0.25, 0.4);
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.PLAYERS, 0.7F, 1.7F);
                this.pendingFollowUp = null;
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.ultimateCooldown <= 0 && distSq <= 49.0D) {
                performUltimate(target);
            } else if (this.specialSkillCooldown <= 0 && distSq <= 25.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    /** Doble golpe: el segundo llega seis ticks después, si Neru sigue lo bastante cerca. */
    private void performNormalAttack(LivingEntity target) {
        this.triggerAnim("attack_controller", "attack");
        this.playVoice("attack");
        this.normalAttackCooldown = 26;
        this.doHurtTarget(target);
        this.pendingFollowUp = target;
        this.followUpTicks = 6;
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(),
                    8, 0.3, 0.4, 0.3, 0.3);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK,
                SoundSource.PLAYERS, 0.8F, 1.6F);
    }

    private void performSpecialSkill(LivingEntity target) {
        this.triggerAnim("attack_controller", "special");
        this.playVoice("attack");
        this.specialSkillCooldown = 180;
        target.hurt(this.damageSources().mobAttack(this), 14.0F);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0));

        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
        }
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY() + 1.0, target.getZ(),
                    14, 0.4, 0.5, 0.4, 0.1);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(),
                    6, 0.4, 0.3, 0.4, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                1.0F, 1.5F);
    }

    private void performUltimate(LivingEntity target) {
        this.triggerAnim("attack_controller", "ultimate");
        this.playVoice("attack", true);
        this.ultimateCooldown = 620;
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.translatable("message.gachawaifus.akita_neru.1"));
        }
        if (owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 2));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 320, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        }
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 320, 2));

        AABB area = this.getBoundingBox().inflate(7.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().mobAttack(this), 18.0F);
            e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, 1));
        }
        this.pendingFollowUp = target;
        this.followUpTicks = 6;

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 1.1, this.getZ(),
                    40, 0.9, 0.8, 0.9, 0.5);
            sl.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 1.0, this.getZ(),
                    20, 0.8, 0.4, 0.8, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS,
                1.2F, 1.6F);
    }
}
