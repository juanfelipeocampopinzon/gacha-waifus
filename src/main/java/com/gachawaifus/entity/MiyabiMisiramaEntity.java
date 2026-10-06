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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hoshimi Miyabi — "La Dama de la Luz" (Zenless Zone Zero)
 *
 * S-Rank Ice Katana Master | Section 6 Chief
 *
 * Skills:
 *   - Kitsune Slash        : Cooldown 20tks, Snowflake particles, Slowness I, 10 dmg
 *   - Frostfall Stance     : Cooldown 200tks, Invulnerability dash, 20 dmg, Slowness II + Weakness
 *   - Cherry Blossom Frost : Cooldown 700tks, AoE 10 blocks, 32 dmg, Slowness IV, Regen + Speed for owner
 */
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiyabiMisiramaEntity extends AbstractWaifuEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private int kitsuneSlashCooldown    = 0;
    private int frostfallStanceCooldown = 120; // Inicia casi listo
    private int cherryBlossomCooldown   = 360; // Inicia cargando
    private int invulnerableTicks       = 0;

    // --- Secuencia de la Ultimate (Cherry Blossom Frost) ---
    /** 0 = inactiva | 1 = cargando | 2 = impacto | 3 = recuperación */
    private int ultimatePhase = 0;
    private int ultimateTicks = 0;
    private int frostRingTick = 0;
    private Vec3 ultimatePos = Vec3.ZERO;
    /** Enemigos con marca de escarcha: UUID -> ticks restantes de la marca. */
    private final Map<UUID, Integer> frostMarks = new HashMap<>();

    public MiyabiMisiramaEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setTame(true, false);
        this.setCustomName(Component.literal("§bHoshimi Miyabi"));
        this.setCustomNameVisible(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> {
            // Durante la ultimate el controlador de movimiento se congela: si no, el idle
            // pelearía contra la animación cinemática en los huesos compartidos.
            if (this.ultimatePhase != 0) {
                return PlayState.STOP;
            }
            if (state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.miyabi.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.miyabi.idle"));
        }));

        controllers.add(new AnimationController<>(this, "attack_controller", 2, state -> {
            // Sin habilidad activa se vuelve a la pose neutra (si el predicado se quedara en STOP,
            // la última pose de la ultimate —brazos abiertos tras el giro— se pegaría al caminar).
            //
            // ⚠ Pero solo valiendo para PARADA: este controlador se registra DESPUÉS del de
            // movimiento y GeckoLib aplica los controladores en orden sin mezclar, así que si
            // devolviera la pose neutra también caminando, pisaría la animación de caminar y la
            // waifu se deslizaría con las piernas rígidas (el fallo que reportó el usuario).
            if (this.ultimatePhase == 0 && !state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.miyabi.rest"));
            }
            return PlayState.STOP; // caminando manda "movement"; con habilidad, la animación disparada
        })
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.miyabi.attack"))
                .triggerableAnim("special", RawAnimation.begin().thenPlay("animation.miyabi.special"))
                // Ultimate en 3 actos: carga (iaido) -> desenvaine giratorio -> recuperación.
                // El impacto real ocurre a los 20 ticks, sincronizado con el final de la carga.
                .triggerableAnim("ultimate", RawAnimation.begin()
                        .thenPlay("animation.miyabi.ultimate_charge")
                        .thenPlay("animation.miyabi.ultimate_slash")
                        .thenPlay("animation.miyabi.ultimate_recover")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.ARMOR, 8.0D)
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

        if (this.kitsuneSlashCooldown > 0) this.kitsuneSlashCooldown--;
        if (this.frostfallStanceCooldown > 0) this.frostfallStanceCooldown--;
        if (this.cherryBlossomCooldown > 0) this.cherryBlossomCooldown--;

        if (this.invulnerableTicks > 0) {
            this.invulnerableTicks--;
            if (this.invulnerableTicks == 0) {
                this.setInvulnerable(false);
            }
        }

        // La Ultimate es una secuencia cinemática: mientras corre, Miyabi no se mueve ni ataca.
        this.tickUltimate();
        if (this.ultimatePhase != 0) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            return;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive() && !this.isOrderedToSit()) {
            double distanceSq = this.distanceToSqr(target);

            // Mirar al objetivo
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Prioridad 1: Ultimate (Cherry Blossom Frost)
            if (this.cherryBlossomCooldown <= 0 && distanceSq <= 144.0D) { // 12 bloques
                performCherryBlossomFrost();
            }
            // Prioridad 2: Special Skill (Frostfall Stance)
            else if (this.frostfallStanceCooldown <= 0 && distanceSq <= 64.0D) { // 8 bloques
                performFrostfallStance(target);
            }
            // Prioridad 3: Normal Attack (Kitsune Slash)
            else if (this.kitsuneSlashCooldown <= 0 && distanceSq <= 16.0D) { // 4 bloques
                performKitsuneSlash(target);
            }
        }
    }

    /**
     * Skill 1: Normal Attack — Kitsune Slash
     * Corte de katana rápido con partículas de cristal de hielo y copos de nieve.
     */
    private void performKitsuneSlash(LivingEntity target) {
        this.kitsuneSlashCooldown = 20;

        this.triggerAnim("attack_controller", "attack");

        this.doHurtTarget(target);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.6F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    12, 0.3, 0.5, 0.3, 0.05);
            serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    8, 0.3, 0.5, 0.3, 0.05);
        }
    }

    /**
     * Skill 2: Special Skill — Frostfall Stance
     * Dash de invulnerabilidad con katana espiritual, congelando al objetivo.
     */
    private void performFrostfallStance(LivingEntity target) {
        this.frostfallStanceCooldown = 200;

        this.triggerAnim("attack_controller", "special");

        // Invulnerabilidad temporal durante el dash
        this.setInvulnerable(true);
        this.invulnerableTicks = 25;

        // Dash hacia el objetivo
        Vec3 direction = target.position().subtract(this.position()).normalize();
        this.setDeltaMovement(direction.x * 1.4, 0.25, direction.z * 1.4);

        // Daño de corte helado
        target.hurt(this.damageSources().mobAttack(this), 20.0F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));

        this.level().playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.4F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.8F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    35, 0.8, 0.5, 0.8, 0.12);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + 1.0, target.getZ(),
                    15, 0.4, 0.6, 0.4, 0.1);
        }
    }

    /**
     * Skill 3: Ultimate — Cherry Blossom Frost
     * Vórtice de hielo en espiral tipo zorro espectral — AoE de 10 bloques, daño masivo y buffs.
     *
     * Ahora es una secuencia cinemática en 3 actos (v3.1.1):
     *   1. CARGA     (20 ticks): Miyabi se agacha en postura iaido y telegrafía el círculo de impacto.
     *   2. IMPACTO   (1 tick):   desenvaine giratorio; golpe inicial fuerte + marcas de escarcha.
     *   3. RECUPERACIÓN (36ticks): daño residual de escarcha y liberación del vórtice.
     */
    private void performCherryBlossomFrost() {
        this.cherryBlossomCooldown = 700;

        // --- ACTO 1: CARGA ---
        this.triggerAnim("attack_controller", "ultimate");
        this.ultimatePhase = 1;
        this.ultimateTicks = 0;
        this.frostRingTick = 0;
        this.ultimatePos = this.position();

        // Queda fijada en el sitio: el telégrafo debe coincidir con el golpe.
        this.setTarget(null);
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        this.setInvulnerable(true);
        this.invulnerableTicks = 70;

        Level level = this.level();
        LivingEntity owner = this.getOwner();

        // Anuncio cinemático (título en pantalla + chat, solo para el dueño)
        if (owner instanceof Player player) {
            player.displayClientMessage(
                    Component.literal("§b§l❄ CHERRY BLOSSOM FROST ❄"), true);
            player.sendSystemMessage(Component.literal(
                    "§b[Miyabi] §f\"Cherry Blossom Frost... the kitsune of winter awakens!\""));
        }

        // Sonidos de carga: viento gélido que se acumula
        level.playSound(null, this.blockPosition(), SoundEvents.POWDER_SNOW_FALL, SoundSource.PLAYERS, 2.0F, 0.5F);
        level.playSound(null, this.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.5F, 1.6F);
        level.playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.8F, 0.7F);
    }

    /** Avanza la secuencia de la Ultimate. Se llama cada tick desde aiStep(). */
    private void tickUltimate() {
        if (this.ultimatePhase == 0) {
            // Sin ultimate activa: solo mantenemos las marcas de escarcha en el tiempo.
            if (!this.frostMarks.isEmpty() && !this.level().isClientSide) {
                this.tickFrostMarks();
            }
            return;
        }

        this.ultimateTicks++;

        switch (this.ultimatePhase) {
            case 1 -> { // CARGA: telégrafo en el suelo hasta el tick 20
                if (!this.level().isClientSide) {
                    this.renderTelegraph();
                }
                if (this.ultimateTicks >= 20) {
                    this.ultimatePhase = 2;
                    this.ultimateTicks = 0;
                    if (!this.level().isClientSide) {
                        this.impactUltimate();
                    }
                }
            }
            case 2 -> { // IMPACTO: se dispara una vez y pasa a recuperación
                this.ultimatePhase = 3;
                this.ultimateTicks = 0;
            }
            case 3 -> { // RECUPERACIÓN: escarcha residual durante 36 ticks
                if (!this.level().isClientSide) {
                    this.tickFrostMarks();
                    this.renderFrostAura();
                }
                if (this.ultimateTicks >= 36) {
                    this.ultimatePhase = 0;
                    this.ultimateTicks = 0;
                }
            }
            default -> this.ultimatePhase = 0;
        }
    }

    /**
     * Telégrafo: dibuja el círculo de impacto (radio 10) para que el jugador vea dónde va a caer.
     * Se alterna el color entre copos y varas del End para que se lea incluso de noche.
     */
    private void renderTelegraph() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.ultimatePos == Vec3.ZERO) this.ultimatePos = this.position();

        double radius = 10.0D;
        double angleOffset = this.frostRingTick * 0.18D; // gira lentamente
        int points = 36;
        for (int i = 0; i < points; i++) {
            double rad = Math.toRadians((360.0D / points) * i) + angleOffset;
            double px = this.ultimatePos.x + Math.cos(rad) * radius;
            double pz = this.ultimatePos.z + Math.sin(rad) * radius;
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, px, this.ultimatePos.y + 0.15, pz, 1, 0.0, 0.0, 0.0, 0.0);
            if (i % 3 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, px, this.ultimatePos.y + 0.4, pz, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // Aura de acumulación sobre Miyabi (crece durante la carga)
        double progress = Math.min(1.0D, this.ultimateTicks / 20.0D);
        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY() + 1.0, this.getZ(),
                (int) (6 + progress * 14), 0.6 + progress, 0.8, 0.6 + progress, 0.02);
        this.frostRingTick++;
    }

    /** Acto 2: impacto — onda de choque, daño fuerte y marca de escarcha a los enemigos. */
    private void impactUltimate() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        Vec3 center = this.ultimatePos == Vec3.ZERO ? this.position() : this.ultimatePos;

        // Onda de choque: anillos expansivos desde el epicentro
        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * 3.33D;
            for (int i = 0; i < 40; i++) {
                double rad = Math.toRadians((360.0D / 40.0D) * i) + ring * 0.5D;
                double px = center.x + Math.cos(rad) * radius;
                double pz = center.z + Math.sin(rad) * radius;
                serverLevel.sendParticles(ParticleTypes.FLASH, px, center.y + 0.5, pz, 1, 0.0, 0.0, 0.0, 0.0);
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, px, center.y + 0.4, pz, 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
        serverLevel.sendParticles(ParticleTypes.FLASH, center.x, center.y + 1.2, center.z, 6, 0.0, 0.0, 0.0, 0.0);

        // Fanfarria de impacto
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.6F, 0.7F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0F, 0.5F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6F, 1.4F);

        // Buffs a Miyabi y al dueño
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 2));

        LivingEntity owner = this.getOwner();
        if (owner != null) {
            owner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
            owner.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 0));
        }

        // Golpe inicial (60% del daño) con caída por distancia + marca de escarcha
        AABB area = this.getBoundingBox().inflate(10.0D);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity != this && entity != owner && !(entity instanceof AbstractWaifuEntity));

        for (LivingEntity enemy : targets) {
            double dist = Math.sqrt(enemy.distanceToSqr(center));
            double falloff = Math.max(0.4D, 1.0D - (dist / 20.0D));
            enemy.hurt(this.damageSources().indirectMagic(this, this), (float) (19.0D * falloff));
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 3)); // Slowness IV
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 1));
            this.frostMarks.put(enemy.getUUID(), 36);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    enemy.getX(), enemy.getY() + 1.0, enemy.getZ(), 12, 0.4, 0.6, 0.4, 0.06);
        }
    }

    /** Acto 3: escarcha residual — daño en ticks sobre los enemigos marcados. */
    private void tickFrostMarks() {
        java.util.Iterator<Map.Entry<UUID, Integer>> it = this.frostMarks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;

            net.minecraft.world.entity.Entity target = null;
            if (this.level() instanceof ServerLevel serverLevel) {
                target = serverLevel.getEntity(entry.getKey());
            }

            if (remaining <= 0 || target == null || !target.isAlive()) {
                it.remove();
                continue;
            }

            entry.setValue(remaining);
            if (target instanceof LivingEntity living) {
                // 3 ticks de daño residual (13% cada uno) para que se sienta un vórtice, no un botón.
                if (remaining % 12 == 0) {
                    living.hurt(this.damageSources().indirectMagic(this, this), 4.0F);
                }
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            }
        }
    }

    /** Nieve cayendo dentro del vórtice durante la recuperación. */
    private void renderFrostAura() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        Vec3 center = this.ultimatePos == Vec3.ZERO ? this.position() : this.ultimatePos;
        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                center.x, center.y + 2.5, center.z, 14, 4.0, 0.5, 4.0, 0.02);
        serverLevel.sendParticles(ParticleTypes.ITEM_SNOWBALL,
                center.x, center.y + 1.5, center.z, 6, 3.0, 0.4, 3.0, 0.03);
    }
}