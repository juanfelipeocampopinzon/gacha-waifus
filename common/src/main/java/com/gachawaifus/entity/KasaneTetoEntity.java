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
 * Kasane Teto (UTAU/Vocaloid) — Rol Attack (Color Rojo).
 * La diva de taladros: su voz no es limpia como la de Miku, es rock, y eso es lo que suelta —
 * ondas sonoras que golpean a todo lo que la rodea en vez de curar a quien la invoca.
 */
public class KasaneTetoEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** Radio (en bloques) de la onda sonora de la habilidad y la ultimate. */
    private static final double SPECIAL_RADIUS = 5.0D;
    private static final double ULTIMATE_RADIUS = 9.0D;

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 100;
    private int ultimateCooldown = 360;

    public KasaneTetoEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§cKasane Teto"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 96.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // ── GeoEntity ──────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kasane_teto.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.kasane_teto.idle"));
        }));
        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack",  RawAnimation.begin().thenPlay("animation.kasane_teto.attack"))
                .triggerableAnim("special",  RawAnimation.begin().thenPlay("animation.kasane_teto.special"))
                .triggerableAnim("ultimate", RawAnimation.begin().thenPlay("animation.kasane_teto.ultimate")));
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

            if (this.ultimateCooldown <= 0 && distSq <= 81.0D) {
                performUltimate();
            } else if (this.specialSkillCooldown <= 0 && distSq <= 36.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    private void performNormalAttack(LivingEntity target) {
        this.triggerAnim("attack_controller", "attack");
        this.playVoice("attack");
        this.normalAttackCooldown = 18;
        this.doHurtTarget(target);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.4, 0.3, 0.3);
            sl.sendParticles(ParticleTypes.NOTE, target.getX(), target.getY() + 0.9, target.getZ(), 4, 0.3, 0.3, 0.3, 0.2);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1.1F, 0.8F);
    }

    /** Onda sonora: golpea al objetivo y a todo lo que esté a cinco bloques de Teto. */
    private void performSpecialSkill(LivingEntity target) {
        this.triggerAnim("attack_controller", "special");
        this.playVoice("attack");
        this.specialSkillCooldown = 200;
        List<LivingEntity> hit = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(SPECIAL_RADIUS),
                e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : hit) {
            e.hurt(this.damageSources().magic(), 10.0F);
        }
        target.hurt(this.damageSources().magic(), 16.0F);
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 140, 1));

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + 1.1, this.getZ(), 28, 1.0, 0.6, 1.0, 0.6);
            sl.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 12, 0.4, 0.5, 0.4, 0.4);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.NOTE_BLOCK_GUITAR.value(),
                SoundSource.PLAYERS, 1.3F, 0.7F);
    }

    private void performUltimate() {
        this.triggerAnim("attack_controller", "ultimate");
        this.playVoice("attack", true);
        this.ultimateCooldown = 680;
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.translatable("message.gachawaifus.kasane_teto.1"));
        }
        if (owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 2));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0));
        }

        AABB area = this.getBoundingBox().inflate(ULTIMATE_RADIUS);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 26.0F);
            e.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
            e.knockback(0.9D, this.getX() - e.getX(), this.getZ() - e.getZ());
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + 1.2, this.getZ(), 60, 1.4, 1.0, 1.4, 1.0);
            sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            sl.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 1.0, this.getZ(), 30, 1.2, 0.8, 1.2, 0.6);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.8F, 1.4F);
    }
}
