package com.gachawaifus.entity;

import com.gachawaifus.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
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
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Tokisaki Kurumi (Date A Live), GeckoLib desde la 7.8.0.
 *
 * <ul>
 *   <li>Normal: mosquete a distancia (clip {@code attack}) o culatazo cuerpo a cuerpo ({@code strike}).</li>
 *   <li>Especial "Sombras": tres clones salen de su sombra y disparan con ella tres veces (clip
 *       {@code special}, 2 s; balas en 0,8 / 1,1 / 1,4 s). Los clones son VFX (capa {@code clone});
 *       las balas son reales y salen de la posición de cada clon.</li>
 *   <li>Definitiva "Zafkiel: Zayin": invoca el reloj, dispara la séptima bala al cielo (1,5 s) y
 *       detiene el tiempo 4 s a todo lo que esté en el reloj (radio 8). Al reanudarse llega el golpe.</li>
 * </ul>
 * Los efectos viven en {@code vfx/tokisaki_kurumi/*.json} y se disparan solos con los clips.
 */
public class TokisakiKurumiEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // --- Especial: clones de sombra ---
    private static final int SPECIAL_LENGTH = 40;
    /** Ticks del clip "special" en que disparan ella y los clones. */
    private static final int[] SPECIAL_SHOTS = {16, 22, 28};
    /**
     * Dónde están los clones respecto a ella: {derecha, delante} en bloques. TIENE que coincidir
     * con el "offset" de las capas clone de vfx/tokisaki_kurumi/special.json.
     */
    private static final double[][] CLONE_OFFSETS = {{-1.6D, -0.7D}, {1.6D, -0.7D}, {0.0D, -1.6D}};
    private int specialTicks = 0;

    // --- Definitiva: Zafkiel, séptima bala (Zayin) ---
    private static final int ULTIMATE_SHOT_TICK = 30;   // 1,5 s: dispara al cielo
    private static final int FREEZE_TICKS = 80;         // 4 s de tiempo detenido
    private static final int ULTIMATE_CAST_END = 64;    // 3,2 s: se queda quieta hasta acabar el clip
    private static final double ULTIMATE_RADIUS = 8.0D; // = radio del reloj del suelo en el JSON
    private int ultimateTicks = 0;
    /** Enemigos detenidos: posición y giro en que se quedaron. */
    private final List<Frozen> frozen = new ArrayList<>();
    private int freezeTicks = 0;

    private record Frozen(LivingEntity entity, Vec3 pos, float yRot, float xRot) {
    }

    // --- Culatazo ---
    private static final int STRIKE_HIT_DELAY = 5;
    private int strikeTicks = 0;
    @Nullable
    private LivingEntity pendingTarget = null;
    // getTarget() es solo del servidor: el cliente siempre ve null. El modelo necesita saber si está
    // apuntando, así que el estado viaja en un dato sincronizado.
    private static final EntityDataAccessor<Boolean> DATA_MUSKET_AIM =
            SynchedEntityData.defineId(TokisakiKurumiEntity.class, EntityDataSerializers.BOOLEAN);

    // Cooldowns: los iniciales arrancan "casi listos"; los de uso son los reales.
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120;
    private int ultimateCooldown = 360;

    public TokisakiKurumiEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§9Tokisaki Kurumi"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 110.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 4.0D)
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MUSKET_AIM, false);
    }

    /** Si lleva el mosquete enalto apuntando; lo lee el modelo para estirar el brazo. */
    public boolean isMusketAiming() {
        return this.entityData.get(DATA_MUSKET_AIM);
    }

    // ── GeoEntity ──────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.tokisaki_kurumi.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.tokisaki_kurumi.idle"));
        }).setAnimationSpeedHandler(WaifuGeoWalk::cadence));
        // Brazo del mosquete en alto mientras apunta: va encima de andar y debajo de los ataques.
        controllers.add(new AnimationController<>(this, "aim_controller", 4, state -> {
            if (this.isMusketAiming()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.tokisaki_kurumi.aim"));
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.tokisaki_kurumi.attack"))
                .triggerableAnim("strike", RawAnimation.begin().thenPlay("animation.tokisaki_kurumi.strike"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.tokisaki_kurumi.special"))
                .triggerableAnim("ultimate", RawAnimation.begin().thenPlay("animation.tokisaki_kurumi.ultimate")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.tickFrozen();
            if (this.tickCasts()) {
                return; // en mitad de una habilidad no empieza otra
            }
        }
        if (this.normalAttackCooldown > 0) this.normalAttackCooldown--;
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;
        LivingEntity target = this.getTarget();
        // el brazo se levanta en el rango del mosquete (mas alla de los 3 bloques de la culata)
        if (!this.level().isClientSide) {
            double dist = target == null ? -1.0D : this.distanceToSqr(target);
            boolean aiming = target != null && target.isAlive() && !this.isOrderedToSit()
                    && dist > 9.0D && dist <= 196.0D;
            if (this.entityData.get(DATA_MUSKET_AIM) != aiming) this.entityData.set(DATA_MUSKET_AIM, aiming);
        }
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            // 64 = 8 bloques | 36 = 6 bloques | 196 = 14 bloques con el mosquete (256.0D = 16 si es RANGO)
            if (this.ultimateCooldown <= 0 && distSq <= 64.0D) {
                performUltimate(target);
            } else if (this.specialSkillCooldown <= 0 && distSq <= 36.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 196.0D) {
                performNormalAttack(target);
            }
        }
    }

    /**
     * Avanza las habilidades en curso (especial y definitiva) y el culatazo pendiente.
     *
     * @return true si hay una habilidad en curso
     */
    private boolean tickCasts() {
        if (this.strikeTicks > 0 && --this.strikeTicks == 0) {
            LivingEntity target = this.pendingTarget;
            if (target != null && target.isAlive() && this.distanceToSqr(target) <= 16.0D) {
                this.doHurtTarget(target);
            }
            this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        if (this.specialTicks > 0) {
            this.specialTicks++;
            this.getNavigation().stop();
            this.faceCastTarget();
            for (int shot : SPECIAL_SHOTS) {
                if (this.specialTicks == shot + 1) {
                    this.cloneVolley(shot == SPECIAL_SHOTS[0]);
                }
            }
            if (this.specialTicks > SPECIAL_LENGTH) {
                this.specialTicks = 0;
            }
        }
        if (this.ultimateTicks > 0) {
            this.ultimateTicks++;
            this.getNavigation().stop();
            this.faceCastTarget();
            if (this.ultimateTicks == ULTIMATE_SHOT_TICK + 1) {
                this.stopTime();
            }
            if (this.ultimateTicks > ULTIMATE_CAST_END) {
                this.ultimateTicks = 0;
            }
        }
        return this.specialTicks > 0 || this.ultimateTicks > 0 || this.strikeTicks > 0;
    }

    private void faceCastTarget() {
        LivingEntity target = this.pendingTarget;
        if (target != null && target.isAlive()) {
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    private void performNormalAttack(LivingEntity target) {
        this.playVoice("attack");
        // Dentro de 3 bloques usa la culata; desde mas lejos dispara el mosquete
        if (this.distanceToSqr(target) > 9.0D) {
            performMusketShot(target);
            return;
        }
        this.normalAttackCooldown = 22;
        this.triggerAnim("attack_controller", "strike");
        this.pendingTarget = target;
        this.strikeTicks = STRIKE_HIT_DELAY;
    }

    // La pepita del mosquete: sale de la boca del cañon, a 0.9 de bloque por delante de la cara
    private void performMusketShot(LivingEntity target) {
        this.playVoice("attack");
        this.normalAttackCooldown = 30;
        this.triggerAnim("attack_controller", "attack");

        Vec3 look = this.getLookAngle();
        // La punta del cañon de su skin: en la malla es el cubo largo del brazo (local -1..0, 12..18,
        // -2..-1). Con el brazo en cruz (xRot -90) y apartado del costado (zRot 0.18) la matriz de
        // ModelPart (Rx*Ry*Rz sobre el local, mas el pivote -5,2.5,0) deja la boca en
        // (-8.72, 1.0, -17.62) absoluto = 1,10 bloque delante, 0,545 al costado derecho y 1,4375 de
        // alto sobre los pies. El pitch lo absorbe 'look', que ya lleva la inclinacion de la mirada.
        float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
        Vec3 side = new Vec3(-Mth.cos(yawRad), 0.0D, -Mth.sin(yawRad));
        Vec3 muzzle = this.position()
                .add(0.0D, 1.4375D, 0.0D)
                .add(look.scale(1.10D))
                .add(side.scale(0.545D));
        // El fogonazo y el humo son VFX del clip "attack" (vfx/tokisaki_kurumi/normal.json, en la
        // boca del hueso musket_muzzle); aquí solo la bala y el sonido.
        this.fireShot(muzzle, target, 7.0F);
    }

    /** Una bala de mosquete desde {@code muzzle} hacia los ojos del objetivo, con su disparo. */
    private void fireShot(Vec3 muzzle, LivingEntity target, float damage) {
        Vec3 aim = target.getEyePosition().subtract(muzzle).normalize();
        MusketShotEntity shot = new MusketShotEntity(this.level(), this, damage);
        shot.setPos(muzzle.x, muzzle.y, muzzle.z);
        // 2.0F es la mitad de bloque por tick: los 200 ticks de EtherBlastEntity eran 100 bloques
        // de vuelo; aqui 60 ticks x 1 bloque dan ~18 bloques, que es el alcance real del mosquete.
        shot.shoot(aim.x, aim.y, aim.z, 2.0F, 0.6F);
        this.level().addFreshEntity(shot);
        // su disparo propio (crack sintetizado con ffmpeg), con una variacion minima de tono
        this.level().playSound(null, this.blockPosition(), ModSounds.MUSKET_SHOT.get(),
                SoundSource.PLAYERS, 1.2F, 0.95F + this.random.nextFloat() * 0.12F);
    }

    /**
     * Una andanada de la especial: ella y sus tres clones disparan a la vez. Los clones están donde
     * los dibujan los VFX (CLONE_OFFSETS, relativos a su mirada horizontal).
     */
    private void cloneVolley(boolean first) {
        LivingEntity target = this.pendingTarget;
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > 400.0D) {
            return;
        }
        Vec3 look = this.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        forward = forward.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 chest = this.position().add(0.0D, 1.44D, 0.0D);
        this.fireShot(chest.add(forward.scale(0.9D)), target, 6.0F);
        for (double[] offset : CLONE_OFFSETS) {
            Vec3 clone = chest.add(right.scale(offset[0])).add(forward.scale(offset[1] + 0.9D));
            this.fireShot(clone, target, 5.0F);
        }
        if (first) {
            // la primera andanada lleva la bala Bet (ralentiza), como antes la especial
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
        }
    }

    private void performSpecialSkill(LivingEntity target) {
        this.playVoice("attack");
        this.specialSkillCooldown = 250;
        this.triggerAnim("attack_controller", "special");
        this.pendingTarget = target;
        this.specialTicks = 1;
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.6F);
    }

    private void performUltimate(LivingEntity target) {
        this.playVoice("attack", true);
        this.ultimateCooldown = 800;
        this.triggerAnim("attack_controller", "ultimate");
        this.pendingTarget = target;
        this.ultimateTicks = 1;
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.literal("§9Tokisaki Kurumi §o\"I'll take your time.\""));
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
        }
    }

    /** ¿Lo congela la séptima bala? Enemigos y mobs, nunca el dueño, jugadores ni otras waifus. */
    private boolean freezable(LivingEntity e) {
        LivingEntity owner = this.getOwner();
        if (e == this || e == owner || e instanceof Player || e instanceof AbstractWaifuEntity || !e.isAlive()) {
            return false;
        }
        return !(e instanceof TamableAnimal pet && owner != null && pet.isOwnedBy(owner));
    }

    /** Zayin: el reloj se para y todo lo que hay dentro se queda quieto FREEZE_TICKS ticks. */
    private void stopTime() {
        AABB area = this.getBoundingBox().inflate(ULTIMATE_RADIUS, 4.0D, ULTIMATE_RADIUS);
        List<LivingEntity> caught = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> this.freezable(e) && e.distanceToSqr(this) <= ULTIMATE_RADIUS * ULTIMATE_RADIUS);
        this.frozen.clear();
        for (LivingEntity e : caught) {
            this.frozen.add(new Frozen(e, e.position(), e.getYRot(), e.getXRot()));
            // Sin daño durante la parada (debilidad X) y sin andar (lentitud X); la posición se clava
            // además cada tick en tickFrozen(). No se usa NoAI: si el mundo se guardara en medio, el
            // mob se quedaría congelado para siempre.
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, FREEZE_TICKS, 9, false, false));
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, FREEZE_TICKS, 9, false, false));
            e.hurt(this.damageSources().magic(), 15.0F);
        }
        this.freezeTicks = this.frozen.isEmpty() ? 0 : FREEZE_TICKS;
        this.level().playSound(null, this.blockPosition(), ModSounds.MUSKET_SHOT.get(), SoundSource.PLAYERS, 1.6F, 0.6F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4F, 0.5F);
    }

    /** Mantiene clavados a los detenidos y, al acabar, el tiempo se reanuda de golpe. */
    private void tickFrozen() {
        if (this.freezeTicks <= 0) {
            return;
        }
        this.freezeTicks--;
        for (Frozen f : this.frozen) {
            LivingEntity e = f.entity();
            if (!e.isAlive() || e.level() != this.level()) {
                continue;
            }
            e.setDeltaMovement(Vec3.ZERO);
            e.moveTo(f.pos().x, f.pos().y, f.pos().z, f.yRot(), f.xRot());
            e.setYHeadRot(f.yRot());
            if (e instanceof Mob mob) {
                mob.getNavigation().stop();
            }
        }
        if (this.freezeTicks == 0) {
            // El tiempo vuelve: todo el daño de la parada llega a la vez (vfx stage "resume").
            for (Frozen f : this.frozen) {
                if (f.entity().isAlive()) {
                    f.entity().hurt(this.damageSources().magic(), 30.0F);
                }
            }
            this.frozen.clear();
            this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.4F);
        }
    }
}
