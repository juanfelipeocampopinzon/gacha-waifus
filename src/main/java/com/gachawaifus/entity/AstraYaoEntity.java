package com.gachawaifus.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AstraYaoEntity extends AbstractWaifuEntity {
    /** Alcance de cada habilidad, en bloques. Astra es tiradora: el normal llega muy lejos. */
    private static final double NORMAL_RANGE = 30.0D;
    private static final double SPECIAL_RANGE = 10.0D;
    private static final double ULTIMATE_RANGE = 14.0D;

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 100; // Inicia casi listo
    private int ultimateCooldown = 300;     // Inicia cargando

    public AstraYaoEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.25D, 6.0F, 2.0F));
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

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distanceSq = this.distanceToSqr(target);

            // Mirar al objetivo
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate (Grand Finale Concert)
            if (this.ultimateCooldown <= 0 && distanceSq <= ULTIMATE_RANGE * ULTIMATE_RANGE) {
                performUltimate();
            }
            // Prioridad 2: Special Skill (Vocal Solo)
            else if (this.specialSkillCooldown <= 0 && distanceSq <= SPECIAL_RANGE * SPECIAL_RANGE) {
                performSpecialSkill();
            }
            // Prioridad 3: Normal Attack (Ether Blast) — alcance largo, es su ataque a distancia
            else if (this.normalAttackCooldown <= 0 && distanceSq <= NORMAL_RANGE * NORMAL_RANGE) {
                performNormalAttack(target);
            }
        }
    }

    // 1. ATAQUE NORMAL: Ether Blast
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 25; // Cada 1.25 segundos

        Vec3 eyePos = this.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 direction = targetPos.subtract(eyePos).normalize();

        EtherBlastEntity blast = new EtherBlastEntity(this.level(), this, 9.0F);
        blast.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
        blast.shoot(direction.x, direction.y, direction.z, 1.6F, 1.0F);

        this.level().addFreshEntity(blast);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ALLAY_THROW, SoundSource.PLAYERS, 1.0F, 1.6F);
    }

    // 2. HABILIDAD ESPECIAL: Vocal Solo (Onda de sonido, buffs a aliados y slow a enemigos)
    private void performSpecialSkill() {
        this.specialSkillCooldown = 280; // ~14 segundos

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.8F, 1.2F);

        if (this.getOwner() instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.gachawaifus.astra_yao_special"), true);
            // Buffs al jugador
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        }

        // Efectos en área
        AABB aabb = this.getBoundingBox().inflate(SPECIAL_RANGE);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
        for (LivingEntity e : nearby) {
            if (e != this && e != this.getOwner() && (e instanceof Enemy || e == this.getTarget())) {
                DamageSource source = this.damageSources().mobAttack(this);
                e.hurt(source, 16.0F);
                e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));

                // Empuje sónico
                Vec3 push = e.position().subtract(this.position()).normalize().scale(0.8D);
                e.push(push.x, 0.3D, push.z);
            }
        }

        // Partículas sónicas
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.NOTE,
                    this.getX(), this.getY() + 1.2, this.getZ(),
                    12, 1.5, 0.5, 1.5, 0.2);
        }
    }

    // 3. ULTIMATE: Grand Finale Concert (Detonación masiva de Éter)
    private void performUltimate() {
        this.ultimateCooldown = 700; // ~35 segundos

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0F, 1.1F);

        if (this.getOwner() instanceof Player player) {
            player.displayClientMessage(Component.translatable("message.gachawaifus.astra_yao_ultimate"), true);
            // Curación masiva y escudos
            player.heal(20.0F);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 2));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        }

        // Daño masivo a todos los hostiles en el radio de la ultimate
        AABB aabb = this.getBoundingBox().inflate(ULTIMATE_RANGE);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, aabb);
        for (LivingEntity e : nearby) {
            if (e != this && e != this.getOwner() && (e instanceof Enemy || e == this.getTarget())) {
                DamageSource source = this.damageSources().magic();
                e.hurt(source, 38.0F);
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 160, 0));
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
            }
        }

        // Espectáculo visual de partículas
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    this.getX(), this.getY() + 1.5, this.getZ(),
                    40, 2.0, 1.0, 2.0, 0.4);
            serverLevel.sendParticles(ParticleTypes.FLASH,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    3, 0.5, 0.5, 0.5, 0.0);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.isOwnedBy(player)) {
            if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND && itemstack.isEmpty()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }
}
