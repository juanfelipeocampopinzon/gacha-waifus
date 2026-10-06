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

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NicoleDemaraEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /** Alcance de cada habilidad, en bloques. Nicole dispara desde muy lejos. */
    private static final double NORMAL_RANGE = 30.0D;
    private static final double SPECIAL_RANGE = 20.0D;
    private static final double SPECIAL_RADIUS = 10.0D;
    private static final double ULTIMATE_RANGE = 24.0D;
    private static final double ULTIMATE_RADIUS = 14.0D;

    private int normalAttackCooldown = 0;
    private int specialSkillCooldown = 120; // Inicia casi listo (Ether Grenade)
    private int ultimateCooldown = 400;     // Inicia cargando (Black Hole)

    public NicoleDemaraEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§dNicole Demara"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.nicole_demara.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.nicole_demara.idle"));
        }));

        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.nicole_demara.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.nicole_demara.special")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /**
     * Atributos: MAX_HEALTH = 95.0D, MOVEMENT_SPEED = 0.32D, ATTACK_DAMAGE = 8.0D, ARMOR = 8.0D
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 95.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                // Tiradora: necesita ver lejos para aprovechar su alcance ampliado.
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

            // Prioridad 1: Ultimate (Special Delivery - Black Hole)
            if (this.ultimateCooldown <= 0 && distanceSq <= ULTIMATE_RANGE * ULTIMATE_RANGE) {
                performUltimate(target);
            }
            // Prioridad 2: Special Skill (Ether Grenade)
            else if (this.specialSkillCooldown <= 0 && distanceSq <= SPECIAL_RANGE * SPECIAL_RANGE) {
                performSpecialSkill(target);
            }
            // Prioridad 3: Normal Attack (Sugarcoated Bullet)
            else if (this.normalAttackCooldown <= 0 && distanceSq <= NORMAL_RANGE * NORMAL_RANGE) {
                performNormalAttack(target);
            }
        }
    }

    /**
     * 1. ATAQUE NORMAL: Sugarcoated Bullet - Disparo con maletín, 8 daño, partículas ENCHANTED_HIT y WITCH
     * Cooldown: 25 ticks (1.25 segundos)
     */
    private void performNormalAttack(LivingEntity target) {
        this.normalAttackCooldown = 25; // Cada 1.25 segundos

        this.triggerAnim("attack_controller", "attack");

        Vec3 eyePos = this.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 direction = targetPos.subtract(eyePos).normalize();

        EtherBlastEntity blast = new EtherBlastEntity(this.level(), this, 8.0F);
        blast.setPos(eyePos.x, eyePos.y - 0.1, eyePos.z);
        blast.shoot(direction.x, direction.y, direction.z, 1.6F, 1.0F);

        this.level().addFreshEntity(blast);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ALLAY_THROW, SoundSource.PLAYERS, 1.0F, 1.6F);
    }

    /**
     * 2. HABILIDAD ESPECIAL: Ether Grenade - Granada gravitatoria que atrae y daña enemigos
     * Cooldown: 220 ticks (~11 segundos)
     * Efectos: Daño de 14, atrae enemigos cercanos, aplica WEAKNESS
     * La granada aterriza donde está el objetivo, así que el alcance ampliado sirve de verdad.
     */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 220; // ~11 segundos

        this.triggerAnim("attack_controller", "special");

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.8F, 1.2F);

        if (this.getOwner() instanceof Player player) {
            // Buffs al jugador
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        }

        // Centro de la explosión: el objetivo (o Nicole si no hay objetivo válido).
        Vec3 center = (target != null ? target.position() : this.position());

        // Efectos en área - atrae y daña enemigos
        AABB aabb = new AABB(center, center).inflate(SPECIAL_RADIUS);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, aabb,
                e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity));

        for (LivingEntity e : nearby) {
            DamageSource source = this.damageSources().mobAttack(this);
            e.hurt(source, 14.0F);

            // Efecto de debilidad (Weakness)
            e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));

            // Empuje gravitatorio hacia el centro (efecto atraente)
            Vec3 push = center.subtract(e.position()).normalize().scale(0.5D);
            e.push(push.x, 0.1F, push.z);
        }

        // Partículas gravitatorias
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    center.x, center.y + 1.0, center.z,
                    8, 1.0, 0.5, 1.0, 0.3);
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    center.x, center.y + 1.2, center.z,
                    6, 1.2, 0.8, 1.2, 0.4);
        }
    }

    /**
     * 3. ULTIMATE: Special Delivery - Black Hole (Vórtice Gravitatorio)
     * Cooldown: 650 ticks (~32.7 segundos)
     * Efectos:
     * - Vórtice que atrae y daña enemigos (26 daño mágico) alrededor del objetivo
     * - Aplica SLOWDOWN II a todos los enemigos afectados
     * - Buffea al jugador (owner): REGENERATION I (15s), LUCK I
     */
    private void performUltimate(LivingEntity target) {
        this.ultimateCooldown = 650; // ~32.7 segundos

        this.triggerAnim("attack_controller", "special");

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0F, 1.1F);

        if (this.getOwner() instanceof Player player) {
            player.sendSystemMessage(Component.literal("§d[Nicole Demara] §o\"¡Todo tiene un precio... y ustedes acaban de pagar la cuenta!\""));
            // Curación y buffs al dueño
            player.heal(30.0F);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 280, 0)); // 14s
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 1));
        }

        // El agujero negro se abre sobre el objetivo: alcance largo de verdad.
        Vec3 center = (target != null ? target.position() : this.position());

        // Daño masivo a todos los hostiles dentro del radio
        AABB aabb = new AABB(center, center).inflate(ULTIMATE_RADIUS);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, aabb,
                e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity));

        for (LivingEntity e : nearby) {
            DamageSource source = this.damageSources().magic();
            e.hurt(source, 26.0F); // 26 daño mágico
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 280, 1));

            // Atracción gravitatoria al centro
            Vec3 pull = center.subtract(e.position()).normalize().scale(0.8D);
            e.setDeltaMovement(pull.x, 0.2D, pull.z);

            this.level().playSound(null, e.getX(), e.getY(), e.getZ(),
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.0F, 1.4F);
        }

        // Espectáculo visual cinemático - partículas de vórtice y flash
        if (this.level() instanceof ServerLevel serverLevel) {
            // Vórtice gravitatorio en el centro
            for (int i = 0; i < 20; i++) {
                double angle = (i * Math.PI / 10.0);
                double px = center.x + 6.0 * Math.cos(angle);
                double pz = center.z + 6.0 * Math.sin(angle);
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        px, center.y + 1.0, pz,
                        4, 0.2, 0.5, 0.2, 0.1);
            }

            // Flash de luz en el centro
            serverLevel.sendParticles(ParticleTypes.FLASH,
                    center.x, center.y + 1.5, center.z,
                    3, 0.5, 0.5, 0.5, 0.0);

            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                    center.x, center.y + 1.0, center.z,
                    40, 2.0F, 1.0F, 2.0F, 0.4F);
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