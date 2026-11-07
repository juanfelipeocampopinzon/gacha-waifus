package com.gachawaifus.entity;

import com.gachawaifus.registry.ModEntities;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Belle (Descendants) — Rol Support (Color Azul).
 * La invocadora: ella sola no es ni la más rápida ni la más fuerte, pero su ultimate trae a
 * <b>Pyrois</b>, la criatura de fuego que peleaba en el roster. Pyrois no es una waifu — no se
 * saca del gacha, no se guarda en la Cápsula y desaparece si Belle muere o lo pierde de vista.
 */
public class BelleEntity extends AbstractWaifuEntity {

    /** Pyrois vivos que admite a la vez (uno: es una criatura, no una horda). */
    private static final int MAX_MINIONS_ALIVE = 1;
    /** Radio (en bloques) alrededor de Belle donde aparece Pyrois. */
    private static final double SUMMON_RADIUS = 2.5D;

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 100;
    private int ultimateCooldown = 360;
    /** La última Pyrois invocada; se rearma solo cuando la anterior dejó de existir. */
    private final List<UUID> minions = new ArrayList<>();

    public BelleEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§9Belle"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 104.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ARMOR, 7.0D)
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

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
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

    private void performNormalAttack(LivingEntity target) {
        this.playVoice("attack");
        this.normalAttackCooldown = 20;
        this.doHurtTarget(target);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(),
                    7, 0.3, 0.4, 0.3, 0.15);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.0F, 1.5F);
    }

    private void performSpecialSkill(LivingEntity target) {
        this.playVoice("attack");
        this.specialSkillCooldown = 210;
        target.hurt(this.damageSources().magic(), 11.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));

        LivingEntity owner = this.getOwner();
        if (owner != null && owner.isAlive()) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 140, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0));
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 1.2, this.getZ(),
                    14, 0.5, 0.6, 0.5, 0.0);
            sl.sendParticles(ParticleTypes.ENCHANT, target.getX(), target.getY() + 1.0, target.getZ(),
                    10, 0.4, 0.5, 0.4, 0.2);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    /** La ultimate de Belle no pega: trae a Pyrois, que ya hace de las suyas por su cuenta. */
    private void performUltimate() {
        this.playVoice("attack", true);
        this.ultimateCooldown = 650;
        LivingEntity owner = this.getOwner();
        if (owner instanceof Player p) {
            p.sendSystemMessage(Component.translatable("message.gachawaifus.belle.1"));
        }

        if (this.aliveMinions() >= MAX_MINIONS_ALIVE) {
            // Pyrois ya está aquí: la ultimate gasta la recarga y refuerza a la criatura.
            this.buffMinion();
        } else if (!this.summonPyrois()) {
            // Sin sitio donde aparecer: recarga corta para no reintentar cada tick.
            this.ultimateCooldown = 40;
        }

        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 1.2, this.getZ(),
                    30, 0.7, 0.8, 0.7, 0.3);
            sl.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 1.0, this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 1.2F, 0.9F);
    }

    /** ¿Queda alguna Pyrois suya con vida? Los muertos caen solos de la lista. */
    private int aliveMinions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return 0;
        }
        int alive = 0;
        for (UUID id : this.minions) {
            if (serverLevel.getEntity(id) instanceof PyroisEntity pyrois && pyrois.isAlive()) {
                alive++;
            }
        }
        return alive;
    }

    private void buffMinion() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (UUID id : this.minions) {
            if (serverLevel.getEntity(id) instanceof PyroisEntity pyrois && pyrois.isAlive()) {
                pyrois.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                pyrois.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            }
        }
    }

    private boolean summonPyrois() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        PyroisEntity pyrois = this.findSpawnSpot();
        if (pyrois == null) {
            return false;
        }
        pyrois.setSummonerId(this.getUUID());
        serverLevel.addFreshEntity(pyrois);
        this.minions.add(pyrois.getUUID());

        serverLevel.sendParticles(ParticleTypes.POOF, pyrois.getX(), pyrois.getY() + 1.0D, pyrois.getZ(),
                20, 0.4D, 0.7D, 0.4D, 0.05D);
        serverLevel.sendParticles(ParticleTypes.FLAME, pyrois.getX(), pyrois.getY() + 0.6D, pyrois.getZ(),
                25, 0.4D, 0.6D, 0.4D, 0.02D);
        // Que empiece peleando con lo que Belle ya tenga entre ceja y ceja.
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            pyrois.setTarget(target);
        }
        return true;
    }

    /** Busca una posición libre alrededor de Belle para la nueva Pyrois. */
    @Nullable
    private PyroisEntity findSpawnSpot() {
        for (int attempt = 0; attempt < 4; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double ox = Math.cos(angle) * SUMMON_RADIUS;
            double oz = Math.sin(angle) * SUMMON_RADIUS;
            PyroisEntity pyrois = ModEntities.PYROIS.get().create(this.level());
            if (pyrois == null) {
                return null;
            }
            pyrois.moveTo(this.getX() + ox, this.getY(), this.getZ() + oz, this.getYRot(), 0.0F);
            if (this.level().noCollision(pyrois, pyrois.getBoundingBox())) {
                return pyrois;
            }
        }
        return null;
    }
}
