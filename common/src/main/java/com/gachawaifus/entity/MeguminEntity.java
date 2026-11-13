package com.gachawaifus.entity;

import com.gachawaifus.vfx.VfxCasting;
import com.gachawaifus.vfx.VfxTargeted;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Megumin (KonoSuba) — archimaga de la Explosión.
 *
 * <p>Su ultimate es la habilidad insignia del mod: 3 s de carga (aura cian + orbe magenta + torre
 * de anillos en el cielo), luego una Explosión de radio 20 que <b>destruye el terreno</b> de
 * verdad y deja el cráter abriéndose como una onda expansiva durante ~3 s. Su ataque normal es una
 * explosión mediana con mini-cráter. El daño lo aplica el servidor; el VFX lo dibuja el cliente
 * desde {@code vfx/megumin/*.json} (ver {@link VfxCasting}).
 */
public class MeguminEntity extends AbstractWaifuEntity implements VfxCasting, VfxTargeted {

    /** Datos sincronizados para que el cliente sepa qué efecto dibujar, cuándo y dónde. */
    private static final EntityDataAccessor<Integer> DATA_CAST_SEQ =
            SynchedEntityData.defineId(MeguminEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_CAST_KIND =
            SynchedEntityData.defineId(MeguminEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_CAST_X =
            SynchedEntityData.defineId(MeguminEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CAST_Y =
            SynchedEntityData.defineId(MeguminEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_CAST_Z =
            SynchedEntityData.defineId(MeguminEntity.class, EntityDataSerializers.FLOAT);

    /** Ticks de carga de la ultimate: coincide con el stage "charge" de vfx/megumin/ultimate.json. */
    private static final int ULTIMATE_CHARGE = 60;
    /**
     * Alcance de la Explosión, en bloques al cuadrado. Es una habilidad A DISTANCIA: la lanza desde
     * lejos y el cráter cae en el objetivo, así que ella se queda fuera del radio de la explosión.
     */
    private static final double ULTIMATE_RANGE = 32.0D;
    /** Tipo de casteo guardado en DATA_CAST_KIND: elige qué JSON de vfx/ se lanza. */
    private static final byte KIND_SPECIAL = 1;
    private static final byte KIND_ULTIMATE = 2;

    private int specialSkillCooldown = 140;
    private int ultimateCooldown = 500;

    /** Servidor: ticks desde que empezó la carga; -1 = no está casteando. */
    private int castTicks = -1;
    /** Punto congelado donde cae la habilidad (el objetivo puede moverse). */
    private Vec3 castPoint = Vec3.ZERO;
    /** Contador que sube en cada casteo y dispara el VFX en el cliente. */
    private int castSeq = 0;
    /** Cráteres que se están abriendo (solo el de la ultimate). */
    private final List<ExplosionMagic.Crater> craters = new ArrayList<>();
    /** Ticks desde la detonación: mantiene el sonido de fuego del cráter. */
    private int afterglow = 0;

    public MeguminEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§5Megumin"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 70.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CAST_SEQ, 0);
        builder.define(DATA_CAST_KIND, KIND_ULTIMATE);
        builder.define(DATA_CAST_X, 0.0F);
        builder.define(DATA_CAST_Y, 0.0F);
        builder.define(DATA_CAST_Z, 0.0F);
    }

    // ------------------------------------------------------------------ VfxCasting / VfxTargeted

    @Override
    public String vfxEffectId() {
        return this.entityData.get(DATA_CAST_KIND) == KIND_SPECIAL
                ? "megumin/special" : "megumin/ultimate";
    }

    @Override
    public int vfxCastSeq() {
        return this.entityData.get(DATA_CAST_SEQ);
    }

    @Override
    public Vec3 vfxTarget() {
        return new Vec3(this.entityData.get(DATA_CAST_X), this.entityData.get(DATA_CAST_Y),
                this.entityData.get(DATA_CAST_Z));
    }

    // ------------------------------------------------------------------ IA

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
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;

        if (!this.level().isClientSide) {
            this.tickCraters();
        }

        // Cargando: clavada en el sitio, mirando al punto de impacto (el objetivo puede huir).
        if (this.castTicks >= 0) {
            this.getNavigation().stop();
            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(0.0D, motion.y, 0.0D);
            this.getLookControl().setLookAt(this.castPoint.x, this.castPoint.y, this.castPoint.z);
            if (!this.level().isClientSide) {
                this.tickCast();
            }
            return;
        }

        if (this.afterglow > 0) {
            this.afterglow--;
            if (this.afterglow % 30 == 0) {
                this.playSound(SoundEvents.FIRE_AMBIENT, 1.1F, 0.65F);
            }
            if (this.afterglow == 20) {
                this.playSound(SoundEvents.FIRE_EXTINGUISH, 1.4F, 0.7F);
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            // Solo dos habilidades: la Explosión (a distancia) y la magia especial. No tiene ataque
            // normal: si las dos están en recarga, se queda mirando (es el nerfeo que pidió el humano).
            if (this.ultimateCooldown <= 0 && distSq <= ULTIMATE_RANGE * ULTIMATE_RANGE) {
                this.beginUltimate(target);
            } else if (this.specialSkillCooldown <= 0 && distSq <= 256.0D) {
                this.performSpecialSkill(target);
            }
        }
    }

    // ------------------------------------------------------------------ habilidades

    /**
     * Magia especial: la explosión mediana de siempre (22 de daño + debilidad) con su mini-cráter de
     * {@code megumin.special_crater}. Es la habilidad que más usa, así que lleva el VFX completo en
     * {@code vfx/megumin/special.json} en vez de partículas de vanilla.
     */
    private void performSpecialSkill(LivingEntity target) {
        this.playVoice("attack");
        this.specialSkillCooldown = 300;
        this.swing(InteractionHand.MAIN_HAND, true);

        Vec3 center = target.position().add(0.0D, 0.6D, 0.0D);
        this.setCastPoint(center);
        this.markCast(KIND_SPECIAL);

        target.hurt(this.damageSources().mobAttack(this), 22.0F);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
        if (this.level() instanceof ServerLevel server) {
            double craterRadius = MeguminConfig.specialCrater();
            if (craterRadius > 0.0D) {
                this.craters.add(ExplosionMagic.crater(server, center, craterRadius));
            }
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.9F, 1.3F);
    }

    /** Empieza la carga de la Explosión. El golpe real cae en {@link #ULTIMATE_CHARGE} ticks. */
    private void beginUltimate(LivingEntity target) {
        this.ultimateCooldown = MeguminConfig.cooldown();
        this.castTicks = 0;
        this.setCastPoint(target.position().add(0.0D, 0.6D, 0.0D));
        this.markCast(KIND_ULTIMATE);
        this.playVoice("attack", true);
        this.playSound(SoundEvents.BEACON_ACTIVATE, 1.2F, 0.5F);
        this.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 1.6F, 0.6F);
        if (this.getOwner() instanceof Player player) {
            player.sendSystemMessage(Component.translatable("message.gachawaifus.megumin.1"));
        }
    }

    private void tickCast() {
        this.castTicks++;
        if (this.castTicks == 30) {
            this.playSound(SoundEvents.WARDEN_SONIC_CHARGE, 1.1F, 0.55F);
        }
        if (this.castTicks % 20 == 0) {
            this.swing(InteractionHand.MAIN_HAND, true);
        }
        if (this.castTicks >= ULTIMATE_CHARGE) {
            this.detonate();
        }
    }

    /** El golpe: daño, cráter y sonidos. Los VFX ya están lanzados por el cliente desde el casteo. */
    private void detonate() {
        this.castTicks = -1;
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        int radius = MeguminConfig.radius();
        Vec3 center = this.castPoint;
        ExplosionMagic.damage(server, center.add(0.0D, 1.0D, 0.0D), radius + 2.0D,
                MeguminConfig.damage(), this, this.getOwner());
        this.craters.add(ExplosionMagic.crater(server, center.add(0.0D, 0.8D, 0.0D), radius));

        // Agotada, como en el anime: no puede moverse ni volver a castear en un buen rato.
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 4));
        this.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 300, 2));
        this.afterglow = 100;

        this.playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 4.0F, 1.0F);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 4.0F, 0.6F);
        this.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 3.0F, 0.5F);
    }

    /** Avanza los cráteres pendientes; el presupuesto por tick lo pone la config. */
    private void tickCraters() {
        if (this.craters.isEmpty()) {
            return;
        }
        int budget = MeguminConfig.blocksPerTick();
        for (int i = this.craters.size() - 1; i >= 0; i--) {
            ExplosionMagic.Crater crater = this.craters.get(i);
            crater.tick(budget);
            if (crater.done()) {
                this.craters.remove(i);
            }
        }
    }

    // ------------------------------------------------------------------ sincronización

    private void setCastPoint(Vec3 point) {
        this.castPoint = point;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_CAST_X, (float) point.x);
            this.entityData.set(DATA_CAST_Y, (float) point.y);
            this.entityData.set(DATA_CAST_Z, (float) point.z);
        }
    }

    /** Sube el contador de casteo: el cliente ve el cambio y arranca los stages del efecto. */
    private void markCast(byte kind) {
        if (this.level().isClientSide) {
            return;
        }
        this.castSeq++;
        this.entityData.set(DATA_CAST_KIND, kind);
        this.entityData.set(DATA_CAST_SEQ, this.castSeq);
    }
}
