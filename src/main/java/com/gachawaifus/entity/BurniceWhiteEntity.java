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

public class BurniceWhiteEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120;
    private int ultimateCooldown = 360;
    private int invulnerableTicks = 0;

    // --- Lanzallamas (mochila + sopletes de brazo) ---
    /** true mientras el lanzallamas está activo: Burnice queda plantada en el sitio. */
    private boolean flameChanneling = false;
    private int flameTicks = 0;

    // --- Ultimate aérea (lluvia de fuego) ---
    /** 0 = inactiva | 1 = salto | 2 = lluvia de fuego en el aire */
    private int ultimatePhase = 0;
    private int ultimateTicks = 0;
    private static final double FLAME_RANGE = 20.0D;
    /** La ultimate aérea se lanza desde más lejos. */
    private static final double ULTIMATE_RANGE = 16.0D;
    private static final double FLAME_COS = 0.82D; // cono de ~35°
    private static final int CHANNEL_LENGTH = 60;  // 3 s de soplete
    private static final int AIR_RAIN_LENGTH = 50; // 2.5 s de lluvia de fuego en el aire

    public BurniceWhiteEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§cBurnice White"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            // Durante el lanzallamas y la ultimate aérea el movimiento se congela:
            // si no, el idle pelearía contra la animación en los huesos compartidos.
            if (this.flameChanneling || this.ultimatePhase != 0) {
                return PlayState.STOP;
            }
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.burnice_white.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.burnice_white.idle"));
        }));

        // ⚠ Al terminar una habilidad hay que VOLVER a una pose neutra. Si el predicado se quedara
        // en STOP, la última pose (brazos al frente por el lanzallamas o el salto) se quedaría
        // pegada al caminar o al atacar después.
        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> {
            boolean channeling = this.flameChanneling;
            boolean airborne = this.ultimatePhase == 1;

            if (channeling || airborne) return PlayState.STOP; // manda la animación disparada

            if (this.ultimatePhase == 2) {
                // Lluvia de fuego con el chorro al frente; luego vuelve a la pose neutra.
                return this.ultimateTicks <= 48
                        ? state.setAndContinue(RawAnimation.begin().thenLoop("animation.burnice_white.flame_rain"))
                        : state.setAndContinue(RawAnimation.begin().thenLoop("animation.burnice_white.flame_rest"));
            }
            // Sin habilidad activa: volver a la pose neutra de brazos (no a la última pose de la skill).
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.burnice_white.rest"));
        })
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.burnice_white.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.burnice_white.special"))
                // Lanzallamas sostenido (EX): se queda plantada y dispara el soplete de ambos brazos.
                .triggerableAnim("flame_channel", RawAnimation.begin().thenPlay("animation.burnice_white.flame_channel"))
                // Ultimate aérea: salto -> lluvia de fuego mientras está en el aire.
                .triggerableAnim("ultimate_leap", RawAnimation.begin().thenPlay("animation.burnice_white.leap")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
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

        // Lanzallamas y ultimate aérea son estados plantados: nada de movimiento ni otros ataques.
        this.tickFlameChannel();
        this.tickUltimate();
        if (this.flameChanneling || this.ultimatePhase != 0) {
            this.getNavigation().stop();
            if (this.ultimatePhase == 0) {
                this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distanceSq = this.distanceToSqr(target);

            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate (Burnice Supernova aérea)
            if (this.ultimateCooldown <= 0 && distanceSq <= ULTIMATE_RANGE * ULTIMATE_RANGE) {
                performUltimate();
            }
            // Prioridad 2: Special Skill (Jet Flamethrower — lanzallamas sostenido)
            else if (this.specialSkillCooldown <= 0 && distanceSq <= FLAME_RANGE * FLAME_RANGE) {
                performSpecialSkill(target);
            }
            // Prioridad 3: Normal Attack (Scorching Slash)
            else if (this.normalAttackCooldown <= 0 && distanceSq <= 16.0D) {
                performNormalAttack(target);
            }
        }
    }

    /** Avanza el lanzallamas sostenido: quema en cono delante de Burnice durante 3 segundos. */
    private void tickFlameChannel() {
        if (!this.flameChanneling) return;
        this.flameTicks++;

        if (!this.level().isClientSide) {
            this.sprayConeFlames(false);
            if (this.flameTicks % 6 == 0) {
                this.burnConeTargets(3.0F);
            }
        }

        if (this.flameTicks >= CHANNEL_LENGTH) {
            this.flameChanneling = false;
            this.flameTicks = 0;
        }
    }

    /** Avanza la ultimate aérea: salto, altura de crucero y lluvia de fuego. */
    private void tickUltimate() {
        if (this.ultimatePhase == 0) return;
        this.ultimateTicks++;

        if (this.ultimatePhase == 1) {
            // Salto: sube sin gravedad hasta ~3 bloques sobre la altura inicial
            this.setDeltaMovement(0.0D, 0.34D, 0.0D);
            if (this.ultimateTicks >= 12) {
                this.ultimatePhase = 2;
                this.ultimateTicks = 0;
                this.triggerAnim("attack_controller", "flame_rain");
            }
            return;
        }

        // Fase 2: suspendida en el aire escupiendo fuego hacia abajo
        this.setDeltaMovement(0.0D, 0.02D, 0.0D);
        if (!this.level().isClientSide) {
            this.sprayConeFlames(true);
            if (this.ultimateTicks % 8 == 0) {
                this.burnConeTargets(4.0F);
            }
            if (this.ultimateTicks % 10 == 0) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 0.7F);
            }
        }

        if (this.ultimateTicks >= AIR_RAIN_LENGTH) {
            this.ultimatePhase = 0;
            this.ultimateTicks = 0;
            this.setNoGravity(false);
            this.setInvulnerable(false);
            this.invulnerableTicks = 0;
            // El predicado del controlador ya devuelve la pose neutra en la fase 0.
        }
    }

    /** Dibuja el chorro de fuego en cono desde los sopletes de ambos brazos. */
    private void sprayConeFlames(boolean downward) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        Vec3 look = this.getLookAngle();
        if (downward) {
            look = new Vec3(look.x * 0.8D, -1.0D, look.z * 0.8D).normalize();
        }
        double originY = this.getEyeY() - (downward ? 0.4D : 0.2D);

        for (int step = 1; step <= (int) FLAME_RANGE; step++) {
            double distance = step * 1.0D;
            double spread = 0.12D * step;
            double px = this.getX() + look.x * distance;
            double py = originY + look.y * distance;
            double pz = this.getZ() + look.z * distance;

            serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 2, spread, spread, spread, 0.02);
            if (step % 3 == 0) {
                serverLevel.sendParticles(ParticleTypes.LAVA, px, py, pz, 1, spread * 0.5D, spread * 0.5D, spread * 0.5D, 0.0);
            }
            if (step % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, px, py, pz, 1, spread, spread, spread, 0.01);
            }
        }

        // Chispas en las boquillas de los sopletes
        serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), originY, this.getZ(), 4, 0.3, 0.2, 0.3, 0.03);
    }

    /** Quema a los enemigos dentro del cono del lanzallamas. */
    private void burnConeTargets(float damage) {
        Vec3 look = this.getLookAngle();
        AABB area = this.getBoundingBox().inflate(FLAME_RANGE);
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity));

        for (LivingEntity enemy : candidates) {
            Vec3 toEnemy = enemy.position().subtract(this.position()).normalize();
            if (toEnemy.dot(look) < FLAME_COS) continue; // fuera del cono
            enemy.hurt(this.damageSources().inFire(), damage);
            enemy.igniteForSeconds(5);
        }
    }

    /**
     * Skill 1: Normal Attack — Scorching Slash
     */
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 20;
        this.triggerAnim("attack_controller", "attack");

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
     * Skill 2: Special Skill — Jet Flamethrower (lanzallamas sostenido)
     * Burnice se queda PLANTADA en el sitio, saca los sopletes de ambos brazos y escupe fuego
     * en cono durante 3 segundos. No hay dash: el retroceso la mantiene clavada en el suelo.
     */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 220;

        // Estado plantado: el aiStep corta movimiento y otros ataques mientras dure.
        this.flameChanneling = true;
        this.flameTicks = 0;
        this.setTarget(null);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();

        this.getLookControl().setLookAt(target, 60.0F, 60.0F);
        this.triggerAnim("attack_controller", "flame_channel");

        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.6F, 0.8F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.4F, 0.9F);

        if (this.getOwner() instanceof Player player) {
            player.displayClientMessage(Component.literal("§c§l🔥 ¡Jet Flamethrower!"), true);
        }

        // Estallido inicial de las boquillas
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    12, 0.4, 0.3, 0.4, 0.08);
        }
    }

    /**
     * Skill 3: Ultimate — Burnice Supernova (aérea)
     * Burnice SALTA por los aires y, suspendida sobre el campo, descarga una lluvia de fuego
     * en cono hacia abajo durante 2.5 segundos antes de caer.
     */
    private void performUltimate() {
        this.ultimateCooldown = 700;

        // Estado aéreo: salto controlado sin gravedad + animación de salto.
        this.ultimatePhase = 1;
        this.ultimateTicks = 0;
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.invulnerableTicks = 100;
        this.setTarget(null);
        this.getNavigation().stop();
        this.triggerAnim("attack_controller", "ultimate_leap");

        Level level = this.level();
        LivingEntity owner = this.getOwner();

        if (owner instanceof Player player) {
            player.displayClientMessage(Component.literal("§c§l🔥 ¡BURNICE SUPERNOVA!"), true);
            player.sendSystemMessage(Component.literal("§c[Burnice White] §o\"Time for a scorching performance!\""));
        }

        level.playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.3F, 1.2F);
        level.playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 2.0F, 0.8F);

        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));

        if (owner != null) {
            owner.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 1));
        }

        // Impulso del despegue
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.2, this.getZ(), 40, 0.6, 0.1, 0.6, 0.12);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    this.getX(), this.getY() + 0.2, this.getZ(), 10, 0.5, 0.1, 0.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    this.getX(), this.getY() + 0.3, this.getZ(), 2, 0.3, 0.1, 0.3, 0.0);
        }
    }
}