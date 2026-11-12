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

import org.jetbrains.annotations.Nullable;

/**
 * Hatsune Miku (Vocaloid) — Rol Support (Color Verde).
 * La diva virtual número uno: emite ondas melódicas digitales, cura y potencia a su invocador al ritmo de sus conciertos.
 */
public class HatsuneMikuEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 100;
    private int ultimateCooldown = 360;

    // --- Golpes retrasados: el daño cae en el fotograma del golpe de cada clip, no al empezarlo ---
    /** Ticks entre el inicio de "attack" (0,6 s) y el barrido de la batuta (0,22 s). */
    private static final int ATTACK_HIT_DELAY = 4;
    /** Ticks entre el inicio de "special" (1,4 s) y la proyección de la voz (0,5 s). */
    private static final int SPECIAL_HIT_DELAY = 10;
    /** Ticks entre el inicio de "ultimate" (3,0 s) y el drop (1,6 s). Durante ese tiempo no se mueve. */
    private static final int ULTIMATE_HIT_DELAY = 32;
    /** Lo que dura el clip "ultimate": queda plantada en el escenario hasta el final de la pose. */
    private static final int ULTIMATE_LENGTH = 60;
    private static final double ULTIMATE_RADIUS = 10.0D;

    /** Cuenta atrás hasta el golpe (0 = nada pendiente). */
    private int attackHitTicks = 0;
    private int specialHitTicks = 0;
    /** Ticks transcurridos de la ultimate (0 = inactiva). */
    private int ultimateTicks = 0;
    @Nullable
    private LivingEntity pendingTarget = null;

    public HatsuneMikuEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§bHatsune Miku"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // ── GeoEntity ──────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.hatsune_miku.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.hatsune_miku.idle"));
        }));
        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack",  RawAnimation.begin().thenPlay("animation.hatsune_miku.attack"))
                .triggerableAnim("special",  RawAnimation.begin().thenPlay("animation.hatsune_miku.special"))
                .triggerableAnim("ultimate", RawAnimation.begin().thenPlay("animation.hatsune_miku.ultimate")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.40D, 6.0F, 2.0F));
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
        if (!this.level().isClientSide && this.tickPendingHits()) {
            return; // en mitad de una habilidad: no empieza otra ni se mueve durante la ultimate
        }
        if (this.isSinging()) {
            return; // en el concierto no ataca: se queda plantada cantando
        }
        if (this.normalAttackCooldown > 0) this.normalAttackCooldown--;
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (this.ultimateCooldown <= 0 && distSq <= 64.0D) {
                performUltimate(target);
            } else if (this.specialSkillCooldown <= 0 && distSq <= 36.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    /**
     * Avanza los golpes pendientes y los aplica en su fotograma.
     *
     * @return true si hay una habilidad en curso (el resto de la IA de combate espera)
     */
    private boolean tickPendingHits() {
        if (this.attackHitTicks > 0 && --this.attackHitTicks == 0) {
            this.hitNormalAttack();
        }
        if (this.specialHitTicks > 0 && --this.specialHitTicks == 0) {
            this.hitSpecialSkill();
        }
        if (this.ultimateTicks > 0) {
            this.ultimateTicks++;
            this.getNavigation().stop();
            if (this.pendingTarget != null && this.pendingTarget.isAlive()) {
                this.getLookControl().setLookAt(this.pendingTarget, 30.0F, 30.0F);
            }
            if (this.ultimateTicks == ULTIMATE_HIT_DELAY + 1) {
                this.hitUltimate();
            }
            if (this.ultimateTicks > ULTIMATE_LENGTH) {
                this.ultimateTicks = 0;
                this.pendingTarget = null;
            }
        }
        return this.attackHitTicks > 0 || this.specialHitTicks > 0 || this.ultimateTicks > 0;
    }

    /** El objetivo guardado si sigue vivo y cerca; si no, null (el golpe se pierde en el aire). */
    @Nullable
    private LivingEntity livePendingTarget(double range) {
        LivingEntity target = this.pendingTarget;
        if (target == null || !target.isAlive() || target.level() != this.level()
                || this.distanceToSqr(target) > range * range) {
            return null;
        }
        return target;
    }

    private void performNormalAttack(LivingEntity target) {
        this.triggerAnim("attack_controller", "attack");
        this.playVoice("attack");
        this.normalAttackCooldown = 20;
        this.pendingTarget = target;
        this.attackHitTicks = ATTACK_HIT_DELAY;
    }

    /** Barrido de la batuta: los VFX salen del clip "attack" (vfx/hatsune_miku/normal.json). */
    private void hitNormalAttack() {
        LivingEntity target = this.livePendingTarget(5.0D);
        if (target != null) {
            this.doHurtTarget(target);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    private void performSpecialSkill(LivingEntity target) {
        this.triggerAnim("attack_controller", "special");
        this.playVoice("attack");
        this.specialSkillCooldown = 220;
        this.pendingTarget = target;
        this.specialHitTicks = SPECIAL_HIT_DELAY;
    }

    /** Onda de voz: los VFX salen del clip "special" (vfx/hatsune_miku/special.json). */
    private void hitSpecialSkill() {
        LivingEntity target = this.livePendingTarget(8.0D);
        if (target != null) {
            target.hurt(this.damageSources().magic(), 12.0F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }

        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0));
        }
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
        this.level().playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.2F, 1.6F);
    }

    private void performUltimate(@Nullable LivingEntity target) {
        this.triggerAnim("attack_controller", "ultimate");
        this.playVoice("attack", true);
        this.ultimateCooldown = 650;
        this.pendingTarget = target;
        this.ultimateTicks = 1;
    }

    /** El drop: los VFX salen del clip "ultimate" (vfx/hatsune_miku/ultimate.json, stage "drop"). */
    private void hitUltimate() {
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.translatable("message.gachawaifus.hatsune_miku.1"));
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 260, 2));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.SATURATION, 60, 0));
        }
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 260, 2));
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1));

        AABB area = this.getBoundingBox().inflate(ULTIMATE_RADIUS);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 30.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2F, 1.5F);
    }
}
