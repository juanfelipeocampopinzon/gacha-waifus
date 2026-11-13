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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.DamageTypeTags;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.List;

public class CaesarEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Cooldowns: los iniciales arrancan "casi listos"; los de uso son los reales.
    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120;
    private int ultimateCooldown = 360;

    // --- Golpes al ritmo de la animacion (ticks desde que empieza el clip) ---
    // Salen de animations/caesar (attack 0,5 s, special 1,2 s, ultimate 2,0 s, parry 0,3 s) y de
    // vfx/caesar/*.json: cambiar uno obliga a cambiar el otro.
    private static final int ATTACK_HIT_TICK = 3;
    /** Parry Counter: guardia hasta el tick 13 (cualquier golpe se para), empujón 14, tajo 16. */
    private static final int GUARD_END = 13;
    private static final int COUNTER_BASH = 14;
    private static final int COUNTER_SLASH = 16;
    private static final int SPECIAL_LENGTH = 24;
    /** Savage Smash: salto a 0,4 s y caída con espada y escudo a 1,15 s. */
    private static final int ULT_JUMP = 8;
    private static final int ULT_SLAM = 23;
    private static final int ULT_LENGTH = 40;
    private static final double ULT_RADIUS = 8.0D;
    /** -1 = nada en curso. */
    private int attackTicks = -1;
    private LivingEntity attackTarget;
    private int specialTicks = -1;
    private LivingEntity specialTarget;
    private boolean parried = false;
    private int ultTicks = -1;
    private LivingEntity ultTarget;

    public CaesarEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§cCaesar"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.caesar.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.caesar.idle"));
        }).setAnimationSpeedHandler(WaifuGeoWalk::cadence));
        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.caesar.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.caesar.special"))
                .triggerableAnim("ultimate", RawAnimation.begin().thenPlay("animation.caesar.ultimate")));
        // Controlador aparte para el choque del escudo: se lanza mientras sigue el clip "special".
        controllers.add(new AnimationController<>(this, "parry_controller", 0, state -> PlayState.STOP)
                .triggerableAnim("parry", RawAnimation.begin().thenPlay("animation.caesar.parry")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0F);
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
        if (!this.level().isClientSide) {
            tickSkills();
        }
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distSq = this.distanceToSqr(target);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (this.attackTicks >= 0 || this.specialTicks >= 0 || this.ultTicks >= 0) {
                // Habilidad en curso: no se pisan.
            } else if (this.ultimateCooldown <= 0 && distSq <= 64.0D) {
                this.ultTarget = target;
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
        this.normalAttackCooldown = 22;
        // El golpe llega en tickSkills, cuando baja la espada (VFX: caesar/normal).
        this.attackTarget = target;
        this.attackTicks = 0;
    }

    private void performSpecialSkill(LivingEntity target) {
        this.triggerAnim("attack_controller", "special");
        this.specialSkillCooldown = 260;
        // Parry Counter: guardia + buff de defensa. Si algo la golpea durante la guardia lo para
        // (hurt) y el contraataque pega más fuerte. El contraataque llega en tickSkills.
        this.specialTarget = target;
        this.specialTicks = 0;
        this.parried = false;
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0));
        if (this.getOwner() != null) {
            this.getOwner().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0));
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
    }

    /** Durante la guardia del Parry Counter, cualquier golpe de un ser vivo se para. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.specialTicks >= 0 && this.specialTicks <= GUARD_END
                && source.getEntity() instanceof LivingEntity attacker && attacker != this.getOwner()
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (!this.parried) {
                this.parried = true;
                this.triggerAnim("parry_controller", "parry");
                this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.4F, 0.9F);
                this.level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 1.7F);
                attacker.knockback(0.8D, this.getX() - attacker.getX(), this.getZ() - attacker.getZ());
                if (this.specialTarget == null || !this.specialTarget.isAlive()) {
                    this.specialTarget = attacker;
                }
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    /** Avanza los golpes retrasados de las tres habilidades (solo servidor). */
    private void tickSkills() {
        if (this.attackTicks >= 0 && ++this.attackTicks >= ATTACK_HIT_TICK) {
            LivingEntity t = this.attackTarget;
            if (t != null && t.isAlive() && this.distanceToSqr(t) <= 25.0D) {
                this.doHurtTarget(t);
            }
            this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0F, 1.2F);
            this.attackTicks = -1;
            this.attackTarget = null;
        }
        if (this.specialTicks >= 0) {
            this.specialTicks++;
            LivingEntity t = this.specialTarget;
            if (this.specialTicks == COUNTER_BASH) {
                // Empujón de escudo a todo lo que tenga delante.
                Vec3 front = this.position().add(this.getLookAngle().multiply(1.5D, 0.0D, 1.5D));
                for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, new AABB(front, front).inflate(2.0D),
                        e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity) && !(e instanceof Player))) {
                    e.hurt(this.damageSources().mobAttack(this), this.parried ? 14.0F : 8.0F);
                    e.knockback(0.9D, this.getX() - e.getX(), this.getZ() - e.getZ());
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.2F, 0.6F);
            } else if (this.specialTicks == COUNTER_SLASH) {
                if (t != null && t.isAlive() && this.distanceToSqr(t) <= 36.0D) {
                    t.hurt(this.damageSources().mobAttack(this), this.parried ? 24.0F : 15.0F);
                    t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, this.parried ? 3 : 2));
                }
                if (this.parried) {
                    this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 1));
                }
                this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);
            } else if (this.specialTicks >= SPECIAL_LENGTH) {
                this.specialTicks = -1;
                this.specialTarget = null;
            }
        }
        if (this.ultTicks >= 0) {
            this.ultTicks++;
            if (this.ultTicks != ULT_JUMP) {
                this.getNavigation().stop();
            }
            if (this.ultTicks == ULT_JUMP) {
                // Salto de verdad hacia el objetivo; la caída coincide con el golpe (tick 23).
                Vec3 dir = this.ultTarget != null && this.ultTarget.isAlive()
                        ? this.ultTarget.position().subtract(this.position()) : this.getLookAngle();
                Vec3 flat = new Vec3(dir.x, 0.0D, dir.z);
                flat = flat.lengthSqr() > 1.0E-4D ? flat.normalize().scale(Math.min(0.45D, flat.length() * 0.06D)) : Vec3.ZERO;
                this.setDeltaMovement(flat.x, 0.62D, flat.z);
                this.hasImpulse = true;
                this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_KNOCKBACK, SoundSource.PLAYERS, 1.0F, 0.7F);
            } else if (this.ultTicks == ULT_SLAM) {
                slamUltimate();
            } else if (this.ultTicks >= ULT_LENGTH) {
                this.ultTicks = -1;
                this.ultTarget = null;
            }
        }
    }

    private void performUltimate() {
        this.triggerAnim("attack_controller", "ultimate");
        this.ultimateCooldown = 750;
        this.ultTicks = 0;
    }

    /** Savage Smash: el golpe de espada y escudo al caer (tick ULT_SLAM). */
    private void slamUltimate() {
        LivingEntity owner = this.getOwner();
if (owner instanceof Player p) {
            p.sendSystemMessage(Component.translatable("message.gachawaifus.caesar.1"));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
        }
        AABB area = this.getBoundingBox().inflate(ULT_RADIUS);
        List<LivingEntity> enemies = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity));
        for (LivingEntity e : enemies) {
            e.hurt(this.damageSources().magic(), 34.0F);
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
        }
        // Onda, cúpula y destello: stage "slam" de vfx/caesar/ultimate.
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 1.2F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.8F, 0.6F);
    }
}
