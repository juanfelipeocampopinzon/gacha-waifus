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

import java.util.List;

public class YeShunguangEntity extends AbstractWaifuEntity {
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120; // Inicia casi listo
    private int ultimateCooldown = 360;     // Inicia cargando
    private int invulnerableTicks = 0;

    public YeShunguangEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 115.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.33D)
                .add(Attributes.ATTACK_DAMAGE, 11.0D)
                .add(Attributes.ARMOR, 12.0D)
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

        if (this.normalAttackCooldown > 0) this.normalAttackCooldown--;
        if (this.specialSkillCooldown > 0) this.specialSkillCooldown--;
        if (this.ultimateCooldown > 0) this.ultimateCooldown--;

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

            // Prioridad 1: Ultimate (Endless Talisman)
            if (this.ultimateCooldown <= 0 && distanceSq <= 144.0D) { // 12 bloques
                performUltimate(target);
            }
            // Prioridad 2: Special Skill (Ink Eruption)
            else if (this.specialSkillCooldown <= 0 && distanceSq <= 64.0D) { // 8 bloques
                performSpecialSkill(target);
            }
            // Prioridad 3: Normal Attack (Auric Slash)
            else if (this.normalAttackCooldown <= 0 && distanceSq <= 16.0D) { // 4 bloques
                performNormalAttack(target);
            }
        }
    }

    /**
     * Skill 1: Normal Attack — Auric Slash (Auric Array / Qingming Eruption)
     */
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 25; // Cooldown de ~1.25 segundos

        // doHurtTarget aplica el daño base del atributo ATTACK_DAMAGE (11.0 para Ye Shunguang)
        this.doHurtTarget(target);

        // Efectos adicionales: WEAKNESS + MOVEMENT_SLOWDOWN
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        // Partículas de Éter: ENCHANTED_HIT + WITCH
        if (this.level() instanceof ServerLevel serverLevel) {
            double tx = target.getX();
            double ty = target.getY();
            double tz = target.getZ();
            for (int i = 0; i < 8; i++) {
                float angle = ((float) Math.PI * 2.0F / 8.0F) * i;
                float radius = this.random.nextFloat() * 1.5F + 0.5F;
                double px = tx + Math.cos(angle) * radius;
                double py = ty + 1.0;
                double pz = tz + Math.sin(angle) * radius;
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py, pz, 3, 0.2F, 0.3F, 0.2F, 0.04F);
                serverLevel.sendParticles(ParticleTypes.WITCH, px, py, pz, 3, 0.2F, 0.3F, 0.2F, 0.04F);
            }
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    /**
     * Skill 2: Special Skill — Ink Eruption (Cloud-Shaper / Ashen Ink Becomes Shadows)
     */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 250; // Cooldown de ~12.5 segundos

        // Invulnerabilidad temporal durante el canalizado
        this.setInvulnerable(true);
        this.invulnerableTicks = 20;

        // Dash rápido hacia el objetivo
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 1.0) {
            this.setDeltaMovement(dx / dist * 1.4, 0.25, dz / dist * 1.4);
        }

        // AoE: 5 bloques (radio) — aplica daño y efectos a todos los enemigos en el área
        AABB aoeBox = this.getBoundingBox().inflate(5.0D);
        LivingEntity owner = this.getOwner();
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, aoeBox,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));

        for (LivingEntity entity : enemies) {
            entity.hurt(this.damageSources().indirectMagic(this, this), 18.0F);
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 180, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        }

        // Partículas de Éter: WITCH + ENCHANTED_HIT en espiral
        if (this.level() instanceof ServerLevel serverLevel) {
            double tx = target.getX();
            double ty = target.getY();
            double tz = target.getZ();
            for (int i = 0; i < 15; i++) {
                float angle = ((float) Math.PI * 2.0F / 15.0F) * i + (this.random.nextFloat() - 0.5F);
                double px = tx + Math.cos(angle) * 2.0;
                double py = ty + 0.5;
                double pz = tz + Math.sin(angle) * 2.0;
                serverLevel.sendParticles(ParticleTypes.WITCH, px, py, pz, 2, 0.3F, 0.6F, 0.3F, 0.08F);
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py + 0.2, pz, 2, 0.2F, 0.4F, 0.2F, 0.06F);
            }
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.2F, 1.3F);
    }

    /**
     * Ultimate: Endless Talisman (Meditation State)
     */
    private void performUltimate(LivingEntity target) {
        this.ultimateCooldown = 750; // Cooldown de ~37.5 segundos

        // Fase de invulnerabilidad "Meditación"
        this.setInvulnerable(true);
        this.invulnerableTicks = 30;

        Level level = this.level();
        LivingEntity owner = this.getOwner();

        if (owner instanceof Player player) {
            player.sendSystemMessage(Component.literal("§d[Ye Shunguang] §o\"Endless Talisman Suppression... your fate is sealed.\""));
        }

        // AoE: 12 bloques (radio) — aplica daño masivo a enemigos
        AABB aoeBox = this.getBoundingBox().inflate(12.0D);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aoeBox,
                entity -> entity != this && entity != owner && !(entity instanceof AbstractWaifuEntity));

        for (LivingEntity enemy : targets) {
            enemy.hurt(this.damageSources().indirectMagic(this, this), 35.0F);
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        }

        // Buffs al owner (el dueño de la mascota)
        if (owner != null) {
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2));
        }

        // Buffs para Ye Shunguang misma
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 2));

        // Partículas de Éter: FLASH + ENCHANTED_HIT + WITCH (anillo masivo)
        if (level instanceof ServerLevel serverLevel) {
            double tx = this.getX();
            double ty = this.getY();
            double tz = this.getZ();
            for (int i = 0; i < 16; i++) {
                float angle = ((float) Math.PI * 2.0F / 16.0F) * i;
                float radius = 4.0F + (this.random.nextFloat() * 3.0F);
                double px = tx + Math.cos(angle) * radius;
                double py = ty + 1.0;
                double pz = tz + Math.sin(angle) * radius;
                serverLevel.sendParticles(ParticleTypes.FLASH, px, py, pz, 1, 0.0F, 0.0F, 0.0F, 0.0F);
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, px, py + 0.2, pz, 3, 0.2F, 0.5F, 0.2F, 0.05F);
                serverLevel.sendParticles(ParticleTypes.WITCH, px, py - 0.2, pz, 3, 0.2F, 0.5F, 0.2F, 0.05F);
            }
        }

        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 1.2F);
        level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 2.0F, 0.9F);
    }
}