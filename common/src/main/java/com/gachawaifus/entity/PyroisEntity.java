package com.gachawaifus.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * <b>Pyrois</b> — la criatura de fuego que invoca Belle, ya no una waifu del roster.
 *
 * <p>No sale del gacha, no se guarda en la Cápsula y no se domada: existe mientras Belle la
 * tenga cerca. Copia el objetivo de su invocadora cada {@value #SUMMONER_SYNC_INTERVAL} ticks (por
 * eso su gracia es pegarle a lo que ella señala) y, si Belle desaparece más de
 * {@value #SUMMONER_MISSING_LIMIT} ticks, se apaga sola en vez de quedarse vagando por el mundo
 * para siempre. Sus tres skills de fuego se quedan igual que cuando era waifu.
 *
 * <p>Es <b>aliada</b>, no un monstruo: extiende {@code PathfinderMob} en vez de {@code Monster}.
 * {@code Monster} implementa {@code Enemy}, y las waifus eligen a quién morder por
 * {@code instanceof Enemy} —si Pyrois era monstruo, las waifus la atacaban y ella les devolvía el
 * golpe, peleando Belle contra su propia criatura y contra el resto del roster. Los golems de hierro
 * y demás IA que buscan {@code Monster} también lo ignoran ahora. Lo que se perdió al cambiar de
 * clase se devolvió a mano: el foley (golpe, muerte, agua y caída) y la categoría de sonido
 * {@code HOSTILE}, que la clase padre cambiaba por los genéricos y {@code NEUTRAL}. Lo que se ganó de
 * propina: en pacífico ya no se despawna y no impide dormir.
 */
public class PyroisEntity extends PathfinderMob {

    /** Cada cuántos ticks re-sincroniza su objetivo con el de Belle. */
    private static final int SUMMONER_SYNC_INTERVAL = 20;
    /** Ticks seguidos sin su invocadora (fuera de descarga de chunks) antes de apagarse. */
    private static final int SUMMONER_MISSING_LIMIT = 100;

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120;
    private int ultimateCooldown = 360;
    private int summonerSyncCooldown;
    private int summonerMissingTicks;

    /** Belle que la invocó ({@code null} si la puso el jugador con el huevo del creativo). */
    @Nullable
    private UUID summonerId;

    public PyroisEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 15;
        this.setPersistenceRequired();
        this.setCustomName(Component.literal("§fPyrois"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        // createMonsterAttributes() solo añadía ATTACK_DAMAGE, y esta cadena ya la pone a 10, así
        // que cambiar por la de Mob no pierde ningún atributo.
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 118.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // Monster traía este foley y la clase padre no: sin él Pyrois suena a jugador genérico
    // (GENERIC_HURT/DEATH/SWIM/SPLASH/FALL) y el volumen se manda por la categoría NEUTRAL.
    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.HOSTILE_SWIM;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.HOSTILE_SPLASH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HOSTILE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HOSTILE_DEATH;
    }

    @Override
    public LivingEntity.Fallsounds getFallSounds() {
        return new LivingEntity.Fallsounds(SoundEvents.HOSTILE_SMALL_FALL, SoundEvents.HOSTILE_BIG_FALL);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                entity -> entity instanceof Enemy));
    }

    public void setSummonerId(@Nullable UUID summonerId) {
        this.summonerId = summonerId;
    }

    @Nullable
    public UUID getSummonerId() {
        return this.summonerId;
    }

    @Nullable
    public LivingEntity getSummoner() {
        if (this.summonerId == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (serverLevel.getEntity(this.summonerId) instanceof LivingEntity summoner
                && summoner.isAlive()) {
            return summoner;
        }
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.summonerId != null) {
            tag.putUUID("SummonerId", this.summonerId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("SummonerId")) {
            this.summonerId = tag.getUUID("SummonerId");
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.normalAttackCooldown > 0) this.normalAttackCooldown--;
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;
        this.fireSkills();
        if (this.level().isClientSide || this.summonerId == null) {
            return;
        }

        if (--this.summonerSyncCooldown > 0) {
            return;
        }
        this.summonerSyncCooldown = SUMMONER_SYNC_INTERVAL;
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(serverLevel.getEntity(this.summonerId) instanceof Mob summoner)
                || !summoner.isAlive()) {
            // Sin Belle a la vista un buen rato: se apaga. (Con su chunk descargado no hay tick,
            // así que el contador solo avanza cuando aquí de verdad ya no está.)
            if (++this.summonerMissingTicks > SUMMONER_MISSING_LIMIT) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY() + 1.0D, this.getZ(), 20, 0.4D, 0.6D, 0.4D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY() + 0.6D, this.getZ(), 12, 0.3D, 0.5D, 0.3D, 0.0D);
                this.discard();
            }
            return;
        }
        this.summonerMissingTicks = 0;
        if (this.getTarget() != null) {
            return;
        }
        LivingEntity target = summoner.getTarget();
        if (target != null && target.isAlive() && !(target instanceof AbstractWaifuEntity)) {
            this.setTarget(target);
        }
    }

    /**
     * Aliada de las waifus: ni las persigue ni les devuelve los golpes.
     *
     * <p>Desde que Pyrois es {@code PathfinderMob} las waifus ya no lo eligen (ellas filtran por
     * {@code instanceof Enemy}), pero un golpe perdido de cualquier skill de área le deja registrado
     * al agresor en la memoria de {@code HurtByTargetGoal} y le devolvería el golpe: esta es la red
     * que lo corta.
     *
     * <p>Hay que cortar aquí y no en {@code tick()}: los goals de objetivo re-adquieren al agresor cada
     * tick mientras ese recuerdo esté vivo, así que limpiándolo al final del tick el
     * {@code MeleeAttackGoal} y las skills de fuego ya le habían dado a la waifu en ese mismo tick.
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target instanceof AbstractWaifuEntity ? null : target);
    }

    private LivingEntity ownerOfFlame() {
        LivingEntity summoner = this.getSummoner();
        if (summoner instanceof AbstractWaifuEntity waifu) {
            return waifu.getOwner();
        }
        return summoner;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.igniteForTicks(40);
        }
        return hit;
    }

    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 22;
        this.doHurtTarget(target);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(),
                    8, 0.3, 0.5, 0.3, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE,
                0.8F, 1.2F);
    }

    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 260;
        target.hurt(this.damageSources().mobAttack(this), 15.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        target.igniteForTicks(80);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(),
                    16, 0.4, 0.6, 0.4, 0.1);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE,
                1.0F, 1.1F);
    }

    private void performUltimate() {
        this.ultimateCooldown = 750;
        LivingEntity owner = this.ownerOfFlame();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.translatable("message.gachawaifus.pyrois.1"));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        }
        AABB area = this.getBoundingBox().inflate(8.0D);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity)
                        && !(e instanceof PyroisEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 40.0F);
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0, this.getZ(),
                    2, 0.5, 0.5, 0.5, 0.1);
            sl.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 0.8, this.getZ(),
                    40, 1.0, 0.8, 1.0, 0.2);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.HOSTILE, 1.2F, 1.2F);
    }

    private void fireSkills() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
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
