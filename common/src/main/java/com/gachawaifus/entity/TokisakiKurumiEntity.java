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
import java.util.List;

public class TokisakiKurumiEntity extends AbstractWaifuEntity {
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

    @Override
    public void aiStep() {
        super.aiStep();
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
                performUltimate();
            } else if (this.specialSkillCooldown <= 0 && distSq <= 36.0D) {
                performSpecialSkill(target);
            } else if (this.normalAttackCooldown <= 0 && distSq <= 196.0D) {
                performNormalAttack(target);
            }
        }
    }

    private void performNormalAttack(LivingEntity target) {
        // Dentro de 3 bloques usa la culata; desde mas lejos dispara el mosquete
        if (this.distanceToSqr(target) > 9.0D) {
            performMusketShot(target);
            return;
        }
        this.normalAttackCooldown = 22;
        this.doHurtTarget(target);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.3, 0.5, 0.3, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    // La pepita del mosquete: sale de la boca del cañon, a 0.9 de bloque por delante de la cara
    private void performMusketShot(LivingEntity target) {
        this.normalAttackCooldown = 30;
        this.swing(InteractionHand.MAIN_HAND);

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
        Vec3 aim = target.getEyePosition().subtract(muzzle).normalize();

        MusketShotEntity shot = new MusketShotEntity(this.level(), this, 7.0F);
        shot.setPos(muzzle.x, muzzle.y, muzzle.z);
        // 2.0F es la mitad de bloque por tick: los 200 ticks de EtherBlastEntity eran 100 bloques
        // de vuelo; aqui 60 ticks x 1 bloque dan ~18 bloques, que es el alcance real del mosquete.
        shot.shoot(aim.x, aim.y, aim.z, 2.0F, 0.6F);
        this.level().addFreshEntity(shot);

        if (this.level() instanceof ServerLevel sl) {
            // solo humo saliendo a presiion del canon: si se sueltan llamas aqui se quedan amontonadas
            // en la boca y parece que la bala va pegada al arma
            Vec3 puff = muzzle.add(look.x * 0.25D, look.y * 0.25D, look.z * 0.25D);
            sl.sendParticles(ParticleTypes.CLOUD, puff.x, puff.y, puff.z, 4, 0.06, 0.06, 0.06, 0.10);
            sl.sendParticles(ParticleTypes.SMOKE, puff.x, puff.y, puff.z, 3, 0.05, 0.05, 0.05, 0.08);
            sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, muzzle.x, muzzle.y, muzzle.z, 4, 0.05, 0.05, 0.05, 0.02);
        }
        // su disparo propio (crack sintetizado con ffmpeg), con una variacion minima de tono
        this.level().playSound(null, this.blockPosition(), ModSounds.MUSKET_SHOT.get(),
                SoundSource.PLAYERS, 1.2F, 0.95F + this.random.nextFloat() * 0.12F);
    }

    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 250;
        target.hurt(this.damageSources().mobAttack(this), 10.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.3, 0.5, 0.3, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private void performUltimate() {
        this.ultimateCooldown = 800;
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.literal("§9Tokisaki Kurumi §o\"I'll take your time.\""));
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 300, 1));
        }
        AABB area = this.getBoundingBox().inflate(8.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 40.0F);
        }
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.5, 0.5, 0.5, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
    }
}
