package com.gachawaifus.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import com.gachawaifus.vfx.VfxTargeted;
import org.joml.Vector3f;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NicoleDemaraEntity extends AbstractWaifuEntity implements GeoEntity, VfxTargeted {
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

    // --- Disparo sincronizado con la animacion y los VFX ------------------------------
    // El campo se abre donde estaba el objetivo al disparar. Ese punto viaja al cliente como dato
    // sincronizado para que los efectos "anchor: target" (proyectil, agujero negro, cupula) caigan
    // en el mismo sitio que el dano. Los ticks salen de animations/nicole_demara (blackhole_ex 1,0 s
    // y blackhole_ult 2,6 s) y de vfx/nicole_demara/*.json: cambiar uno obliga a cambiar el otro.
    private static final EntityDataAccessor<Vector3f> DATA_FIELD_TARGET =
            SynchedEntityData.defineId(NicoleDemaraEntity.class, EntityDataSerializers.VECTOR3);

    /** EX: disparo a 0,35 s; la bala tarda 0,25 s y el campo (6 s) se abre a 0,6 s. */
    private static final int EX_SHOT_TICK = 7;
    private static final int EX_FIELD_TICK = 12;
    /** Ultimate: disparo a 1,0 s, colapso y destello a 1,5 s, cupula (8 s) a 1,7 s. */
    private static final int ULT_SHOT_TICK = 20;
    private static final int ULT_BURST_TICK = 30;
    private static final int ULT_FIELD_TICK = 34;
    private static final float ULT_BURST_DAMAGE = 10.0F;

    /** 0 = nada, 1 = EX, 2 = ultimate; castTicks cuenta desde que empezo la animacion. */
    private int castType = 0;
    private int castTicks = 0;
    private Vec3 castTarget = Vec3.ZERO;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FIELD_TARGET, new Vector3f());
    }

    @Override
    public Vec3 vfxTarget() {
        Vector3f point = this.entityData.get(DATA_FIELD_TARGET);
        return point.lengthSquared() == 0.0F ? null : new Vec3(point.x, point.y, point.z);
    }

    /** Fija el punto del campo (servidor) antes de lanzar la animacion, para que el cliente lo tenga. */
    private void aimField(Vec3 center) {
        this.castTarget = center;
        this.entityData.set(DATA_FIELD_TARGET, new Vector3f((float) center.x, (float) center.y, (float) center.z));
    }

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
        }).setAnimationSpeedHandler(WaifuGeoWalk::cadence));

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
        if (!this.level().isClientSide && this.castType != 0) {
            tickCast();
            if (this.castType == 2) {
                // Durante la ultimate se queda plantada: el disparo sale de donde se ve.
                this.getNavigation().stop();
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distanceSq = this.distanceToSqr(target);

            // Mirar al objetivo
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate "Ether Grenade" (el campo mas potente)
            if (this.castType != 0) {
                // Hay un disparo en curso: no se pisan las habilidades.
            } else if (this.ultimateCooldown <= 0 && distanceSq <= ULTIMATE_RANGE * ULTIMATE_RANGE) {
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
        this.playVoice("attack");
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
        this.playVoice("attack");
        this.specialSkillCooldown = 220; // ~11 segundos

        // El campo se abre donde esta el objetivo, asi el alcance largo sirve de verdad; el
        // disparo y la apertura llegan en tickCast, al ritmo de la animacion.
        aimField(target != null ? target.position() : this.position());
        this.castType = 1;
        this.castTicks = 0;
        this.triggerAnim("attack_controller", "blackhole_ex");

        if (this.getOwner() instanceof Player player) {
            // Es soporte: el campo tambien buffea al dueño.
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
        }

    }

    /**
     * 3. ULTIMATE: "Ether Grenade" (Kit real de ZZZ) — el mismo campo pero mas potente.
     *
     * Mismo vortice que atrae y hace dano por ticks, con mas radio (12), mas dano por tick
     * (4) y mas duracion (8 s), ademas de curar y buffear al dueño. Cooldown: 650 ticks.
     */
    private void performUltimate(LivingEntity target) {
        this.playVoice("attack", true);
        this.ultimateCooldown = 650; // ~32.7 segundos

        aimField(target != null ? target.position() : this.position());
        this.castType = 2;
        this.castTicks = 0;
        this.triggerAnim("attack_controller", "blackhole_ult");

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0F, 1.1F);

        if (this.getOwner() instanceof Player player) {
            player.sendSystemMessage(Component.translatable("message.gachawaifus.nicole_demara.1"));
            // Curación y buffs al dueño
            player.heal(30.0F);
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 280, 0)); // 14s
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, 400, 1));
        }

    }

    /**
     * Avanza el disparo en curso al ritmo de la animacion: sonido del disparo, (ultimate) el
     * colapso con su golpe y, por ultimo, la apertura del campo en el punto fijado.
     */
    private void tickCast() {
        this.castTicks++;
        Vec3 c = this.castTarget;
        if (this.castType == 1) {
            if (this.castTicks == EX_SHOT_TICK) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, 1.2F, 0.7F);
            } else if (this.castTicks >= EX_FIELD_TICK) {
                this.level().playSound(null, c.x, c.y, c.z,
                        SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.6F, 1.3F);
                this.level().playSound(null, c.x, c.y, c.z,
                        SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4F, 1.2F);
                openBlackHole(c, BLACK_HOLE_EX_RADIUS, BLACK_HOLE_EX_DAMAGE, BLACK_HOLE_EX_TICKS, false);
                this.castType = 0;
            }
            return;
        }
        if (this.castTicks == ULT_SHOT_TICK) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.6F);
        } else if (this.castTicks == ULT_BURST_TICK) {
            // Colapso: el destello blanco y el golpe inicial a todo lo que haya en la cupula.
            this.level().playSound(null, c.x, c.y, c.z,
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.6F, 1.2F);
            AABB area = new AABB(c, c).inflate(BLACK_HOLE_ULT_RADIUS);
            for (LivingEntity e : this.level().getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != this && e != this.getOwner() && !(e instanceof AbstractWaifuEntity)
                            && !(e instanceof Player) && e.distanceToSqr(c) <= BLACK_HOLE_ULT_RADIUS * BLACK_HOLE_ULT_RADIUS)) {
                e.hurt(this.damageSources().magic(), ULT_BURST_DAMAGE);
            }
        } else if (this.castTicks >= ULT_FIELD_TICK) {
            this.level().playSound(null, c.x, c.y, c.z,
                    SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 2.0F, 0.8F);
            openBlackHole(c, BLACK_HOLE_ULT_RADIUS, BLACK_HOLE_ULT_DAMAGE, BLACK_HOLE_ULT_TICKS, true);
            this.castType = 0;
        }
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
        // Apertura, vortice y cierre los dibujan los stages de vfx/nicole_demara (special y ultimate).
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

        // Al cerrarse, el sonido final (el colapso lo dibuja el stage "close").
        if (this.blackHoleTicks <= 0) {
            this.level().playSound(null, this.blackHoleX, this.blackHoleY, this.blackHoleZ,
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.4F, 0.7F);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.isOwnedBy(player)) {
            // Agachado no: eso lo maneja AbstractWaifuEntity (cambio de modo de combate).
            if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND && itemstack.isEmpty()
                    && !player.isShiftKeyDown()) {
                this.toggleSitFromPet();
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