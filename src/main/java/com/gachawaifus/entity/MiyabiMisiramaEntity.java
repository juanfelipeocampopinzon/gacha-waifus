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

/**
 * Hoshimi Miyabi — "La Dama de la Luz" (Zenless Zone Zero)
 *
 * S-Rank Ice Katana Master | Section 6 Chief
 *
 * Skills:
 *   - Kitsune Slash        : Cooldown 20tks, Snowflake particles, Slowness I, 10 dmg
 *   - Frostfall Stance     : Cooldown 200tks, Invulnerability dash, 20 dmg, Slowness II + Weakness
 *   - Cherry Blossom Frost : Cooldown 700tks, AoE 10 blocks, 32 dmg, Slowness IV, Regen + Speed for owner
 */
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiyabiMisiramaEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int kitsuneSlashCooldown    = 0;
    private int frostfallStanceCooldown = 120; // Inicia casi listo
    private int cherryBlossomCooldown   = 360; // Inicia cargando
    private int invulnerableTicks       = 0;

    public MiyabiMisiramaEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§bHoshimi Miyabi"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.miyabi.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.miyabi.idle"));
        }));

        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.miyabi.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.miyabi.special")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
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

        if (this.kitsuneSlashCooldown > 0) this.kitsuneSlashCooldown--;
        if (this.frostfallStanceCooldown > 0) this.frostfallStanceCooldown--;
        if (this.cherryBlossomCooldown > 0) this.cherryBlossomCooldown--;

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

            // Prioridad 1: Ultimate (Cherry Blossom Frost)
            if (this.cherryBlossomCooldown <= 0 && distanceSq <= 144.0D) { // 12 bloques
                performCherryBlossomFrost();
            }
            // Prioridad 2: Special Skill (Frostfall Stance)
            else if (this.frostfallStanceCooldown <= 0 && distanceSq <= 64.0D) { // 8 bloques
                performFrostfallStance(target);
            }
            // Prioridad 3: Normal Attack (Kitsune Slash)
            else if (this.kitsuneSlashCooldown <= 0 && distanceSq <= 16.0D) { // 4 bloques
                performKitsuneSlash(target);
            }
        }
    }

    /**
     * Skill 1: Normal Attack — Kitsune Slash
     * Corte de katana rápido con partículas de cristal de hielo y copos de nieve.
     */
    private void performKitsuneSlash(LivingEntity target) {
        this.kitsuneSlashCooldown = 20;

        this.triggerAnim("attack_controller", "attack");

        this.doHurtTarget(target);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.6F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    12, 0.3, 0.5, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    8, 0.3, 0.5, 0.3, 0.05);
        }
    }

    /**
     * Skill 2: Special Skill — Frostfall Stance
     * Dash de invulnerabilidad con katana espiritual, congelando al objetivo.
     */
    private void performFrostfallStance(LivingEntity target) {
        this.frostfallStanceCooldown = 200;

        this.triggerAnim("attack_controller", "special");

        // Invulnerabilidad temporal durante el dash
        this.setInvulnerable(true);
        this.invulnerableTicks = 25;

        // Dash hacia el objetivo
        Vec3 direction = target.position().subtract(this.position()).normalize();
        this.setDeltaMovement(direction.x * 1.4, 0.25, direction.z * 1.4);

        // Daño de corte helado
        target.hurt(this.damageSources().mobAttack(this), 20.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.4F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.8F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    35, 0.8, 0.5, 0.8, 0.12);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    15, 0.4, 0.6, 0.4, 0.1);
        }
    }

    /**
     * Skill 3: Ultimate — Cherry Blossom Frost
     * Vórtice de hielo en espiral tipo zorro espectral — AoE de 10 bloques, daño masivo y buffs.
     */
    private void performCherryBlossomFrost() {
        this.cherryBlossomCooldown = 700;

        this.triggerAnim("attack_controller", "special");

        Level level = this.level();
        LivingEntity owner = this.getOwner();

        // Anuncio cinemático
        if (owner instanceof Player player) {
            player.sendSystemMessage(Component.literal("§b[Miyabi] §f\"Cherry Blossom Frost... the kitsune of winter awakens!\""));
        }

        // Sonidos dramáticos de katana y hielo
        level.playSound(null, this.blockPosition(), SoundEvents.POWDER_SNOW_FALL, SoundSource.PLAYERS, 2.0F, 0.5F);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 1.6F);
        level.playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5F, 0.5F);

        // Partículas en espiral tipo zorro/kitsune
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 360; i += 8) {
                double rad = Math.toRadians(i);
                double radius = 5.0;
                double px = this.getX() + Math.cos(rad) * radius;
                double pz = this.getZ() + Math.sin(rad) * radius;
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, px, this.getY() + 0.2, pz, 3, 0.1, 0.6, 0.1, 0.05);
                serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL, px, this.getY() + 1.2, pz, 2, 0.1, 0.5, 0.1, 0.05);
            }
            serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0, this.getZ(), 3, 0.0, 0.0, 0.0, 0.0);
        }

        // Buffs a Miyabi y al dueño
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2));

        if (owner != null) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
        }

        // Daño masivo AoE en 10 bloques + Slowness IV
        AABB area = this.getBoundingBox().inflate(10.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity != owner && !(entity instanceof AbstractWaifuEntity));

        for (LivingEntity enemy : targets) {
            enemy.hurt(this.damageSources().indirectMagic(this, this), 32.0F);
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3)); // Slowness IV
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1));
        }
    }
}