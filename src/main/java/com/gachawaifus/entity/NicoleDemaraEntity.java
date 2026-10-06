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
    private int specialSkillCooldown = 120; // Inicia casi listo (EX: campo de energia)
    private int ultimateCooldown = 400;     // Inicia cargando (Ultimate: Ether Grenade)

    // --- Agujero negro / campo de energia activo (Kit real de ZZZ) -------------------
    // El EX Special "Stuffed Sugarcoated Bullet" y la Ultimate "Ether Grenade" abren un
    // campo de energia que ATRAE a los enemigos al centro y les hace dano Eter por TICKS
    // mientras dura; la ultimate es el mismo campo pero mas grande y mas potente.
    private int blackHoleTicks = 0;          // ticks que le quedan al campo
    private int blackHoleDamageTimer = 0;    // cuenta atras para el siguiente tick de dano
    private double blackHoleX;
    private double blackHoleY;
    private double blackHoleZ;
    private double blackHoleRadius = 8.0D;
    private float blackHoleDamage = 2.5F;    // dano de CADA tick
    private boolean blackHoleUltimate = false;

    /** Cada cuantos ticks pega el campo. */
    private static final int BLACK_HOLE_DAMAGE_INTERVAL = 10;
    /** Cuanto dura el campo del EX y el de la ultimate, en ticks. */
    private static final int BLACK_HOLE_EX_TICKS = 120;
    private static final int BLACK_HOLE_ULT_TICKS = 160;
    /** Radios y dano por tick de cada uno. */
    private static final double BLACK_HOLE_EX_RADIUS = 8.0D;
    private static final float BLACK_HOLE_EX_DAMAGE = 2.5F;
    private static final double BLACK_HOLE_ULT_RADIUS = 12.0D;
    private static final float BLACK_HOLE_ULT_DAMAGE = 4.0F;

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
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.nicole_demara.special"))
                // Animaciones nuevas del agujero negro: el EX lanza el campo con el maletin y
                // las dos manos, y la ultimate abre los brazos y remata manteniendo mas tiempo
                // el vortice.
                .triggerableAnim("blackhole_ex",
                        RawAnimation.begin().thenPlay("animation.nicole_demara.blackhole_ex"))
                .triggerableAnim("blackhole_ult",
                        RawAnimation.begin().thenPlay("animation.nicole_demara.blackhole_ult")));
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

        // El campo de energia sigue actuando aunque ella se mueva o pierda el objetivo.
        if (!this.level().isClientSide && this.blackHoleTicks > 0) {
            tickBlackHole();
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distanceSq = this.distanceToSqr(target);

            // Mirar al objetivo
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate "Ether Grenade" (el campo mas potente)
            if (this.ultimateCooldown <= 0 && distanceSq <= ULTIMATE_RANGE * ULTIMATE_RANGE) {
                performUltimate(target);
            }
            // Prioridad 2: EX Special "Stuffed Sugarcoated Bullet" (campo de energia)
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
     * 2. EX SPECIAL: "Stuffed Sugarcoated Bullet" (Kit real de ZZZ) — campo de energia.
     *
     * Abre un vortice sobre el objetivo que ATRAE a los enemigos hacia el centro y les hace
     * dano Eter por TICKS mientras dura (2,5 de dano cada 0,5 s durante 6 s). Aplica
     * debilidad, que aqui hace de la rotura de DEF de su pasiva. Cooldown: 220 ticks.
     */
    private void performSpecialSkill(LivingEntity target) {
        this.specialSkillCooldown = 220; // ~11 segundos

        this.triggerAnim("attack_controller", "blackhole_ex");

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.6F, 1.3F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4F, 1.2F);

        if (this.getOwner() instanceof Player player) {
            // Es soporte: el campo tambien buffea al dueño.
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        }

        // El campo se abre donde esta el objetivo, asi el alcance largo sirve de verdad.
        Vec3 center = (target != null ? target.position() : this.position());
        openBlackHole(center, BLACK_HOLE_EX_RADIUS, BLACK_HOLE_EX_DAMAGE,
                BLACK_HOLE_EX_TICKS, false);
    }

    /**
     * 3. ULTIMATE: "Ether Grenade" (Kit real de ZZZ) — el mismo campo pero mas potente.
     *
     * Mismo vortice que atrae y hace dano por ticks, con mas radio (12), mas dano por tick
     * (4) y mas duracion (8 s), ademas de curar y buffear al dueño. Cooldown: 650 ticks.
     */
    private void performUltimate(LivingEntity target) {
        this.ultimateCooldown = 650; // ~32.7 segundos

        this.triggerAnim("attack_controller", "blackhole_ult");

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0F, 1.1F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 2.0F, 0.8F);

        if (this.getOwner() instanceof Player player) {
            player.sendSystemMessage(Component.literal("§d[Nicole Demara] §o\"¡Todo tiene un precio... y ustedes acaban de pagar la cuenta!\""));
            // Curación y buffs al dueño
            player.heal(30.0F);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 280, 0)); // 14s
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 1));
        }

        // El agujero negro se abre sobre el objetivo: alcance largo de verdad.
        Vec3 center = (target != null ? target.position() : this.position());
        openBlackHole(center, BLACK_HOLE_ULT_RADIUS, BLACK_HOLE_ULT_DAMAGE,
                BLACK_HOLE_ULT_TICKS, true);
    }

    /** Abre el campo de energia en un punto. */
    private void openBlackHole(Vec3 center, double radius, float damage, int ticks, boolean ultimate) {
        this.blackHoleX = center.x;
        this.blackHoleY = center.y;
        this.blackHoleZ = center.z;
        this.blackHoleRadius = radius;
        this.blackHoleDamage = damage;
        this.blackHoleTicks = ticks;
        this.blackHoleDamageTimer = BLACK_HOLE_DAMAGE_INTERVAL;
        this.blackHoleUltimate = ultimate;

        if (this.level() instanceof ServerLevel serverLevel) {
            // Estallido de apertura
            serverLevel.sendParticles(ParticleTypes.FLASH,
                    center.x, center.y + 1.2, center.z, 3, 0.3, 0.3, 0.3, 0.0);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    center.x, center.y + 1.2, center.z, 40, 1.0, 1.0, 1.0, 0.25);
        }
    }

    /**
     * Avanza el campo de energia: atrae a los enemigos al centro, les hace dano cada
     * {@link #BLACK_HOLE_DAMAGE_INTERVAL} ticks y dibuja el vortice.
     */
    private void tickBlackHole() {
        this.blackHoleTicks--;
        Vec3 center = new Vec3(this.blackHoleX, this.blackHoleY, this.blackHoleZ);

        // Atraccion gravitatoria: cada tick empuja a los enemigos hacia el centro.
        AABB area = new AABB(center, center).inflate(this.blackHoleRadius);
        List<LivingEntity> atraidos = this.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity));

        boolean tocaDano = --this.blackHoleDamageTimer <= 0;
        if (tocaDano) {
            this.blackHoleDamageTimer = BLACK_HOLE_DAMAGE_INTERVAL;
        }

        for (LivingEntity e : atraidos) {
            Vec3 haciaCentro = center.subtract(e.position());
            double distancia = haciaCentro.length();
            if (distancia > 0.2D) {
                // Cuanto mas lejos, mas fuerte tira; cerca del centro solo lo mantiene.
                double fuerza = this.blackHoleUltimate ? 0.30D : 0.22D;
                Vec3 pull = haciaCentro.normalize().scale(Math.min(fuerza, distancia * 0.25D));
                e.push(pull.x, 0.02D, pull.z);
                e.hurtMarked = true;   // que el cliente reciba el empujon
            }

            if (tocaDano) {
                e.hurt(this.blackHoleUltimate ? this.damageSources().magic()
                                : this.damageSources().indirectMagic(this, this),
                        this.blackHoleDamage);
                e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                if (this.blackHoleUltimate) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                }
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            // Vortice: portales girando hacia dentro, mas densos en la ultimate.
            int brazos = this.blackHoleUltimate ? 18 : 12;
            double giro = (this.blackHoleTicks * 0.35D);
            double cierre = 1.0D - (this.blackHoleTicks % 40) / 40.0D;   // va cerrándose
            for (int i = 0; i < brazos; i++) {
                double angulo = giro + i * (Math.PI * 2.0D / brazos);
                double radio = this.blackHoleRadius * (0.35D + 0.65D * (1.0D - cierre));
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                        center.x + Math.cos(angulo) * radio,
                        center.y + 1.0D + Math.sin(giro + i) * 0.6D,
                        center.z + Math.sin(angulo) * radio,
                        2, 0.15, 0.15, 0.15, 0.02);
            }
            if (this.blackHoleTicks % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        center.x, center.y + 1.2D, center.z, 10, 0.6, 0.6, 0.6, 0.1);
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        center.x, center.y + 1.0D, center.z, 6, 0.8, 0.5, 0.8, 0.15);
            }

            // Al cerrarse, un fogonazo final.
            if (this.blackHoleTicks <= 0) {
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        center.x, center.y + 1.2D, center.z, 2, 0.2, 0.2, 0.2, 0.0);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                        center.x, center.y + 1.0D, center.z, 1, 0, 0, 0, 0);
                this.level().playSound(null, this.blackHoleX, this.blackHoleY, this.blackHoleZ,
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4F, 0.7F);
            }
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