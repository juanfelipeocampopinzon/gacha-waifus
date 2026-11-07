package com.gachawaifus.entity;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.bust.BustState;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import com.gachawaifus.menu.WaifuModeMenu;
import com.gachawaifus.skin.WaifuSkinProfile;
import com.gachawaifus.skin.WaifuSkinState;
import com.gachawaifus.skin.WaifuSkins;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class AbstractWaifuEntity extends TamableAnimal {

    /** Fisica de la pieza del pecho (solo cliente; en el servidor no se usa). */
    private final BustState bustState = new BustState();

    protected AbstractWaifuEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.goalsRegistrados = true;
    }

    // ---------------------------------------------------------------------
    // Skins alternativas (ver com.gachawaifus.skin)
    // ---------------------------------------------------------------------

    /** Estado visual de la skin; se sincroniza porque el renderizador vive en el cliente. */
    private static final EntityDataAccessor<Byte> DATA_SKIN_STATE =
            SynchedEntityData.defineId(AbstractWaifuEntity.class, EntityDataSerializers.BYTE);

    /** Flag del concierto: el cliente lo usa para la pose de cante (ver {@link WaifuSongs}). */
    private static final EntityDataAccessor<Byte> DATA_SINGING =
            SynchedEntityData.defineId(AbstractWaifuEntity.class, EntityDataSerializers.BYTE);

    /** Ticks que se queda la skin de combate después del último golpe u objetivo. */
    private static final int COMBAT_SKIN_LINGER = 100;

    /** Duración de la skin de habilidad especial y de la ultimate. */
    private static final int SPECIAL_SKIN_TICKS = 100;
    private static final int ULTIMATE_SKIN_TICKS = 140;

    private int combatSkinTicks;
    private int specialSkinTicks;
    private int ultimateSkinTicks;

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SKIN_STATE, (byte) WaifuSkinState.BASE.ordinal());
        builder.define(DATA_SINGING, (byte) 0);
        builder.define(DATA_AFFINITY, (byte) 0);
    }

    // ---------------------------------------------------------------------
    // Sistema de amor: afinidad 0..100 (comer cura y sube el vínculo)
    // ---------------------------------------------------------------------

    /** Afinidad con su dueño; sincronizada porque el panel de modos la pinta en el cliente. */
    private static final EntityDataAccessor<Byte> DATA_AFFINITY =
            SynchedEntityData.defineId(AbstractWaifuEntity.class, EntityDataSerializers.BYTE);

    /** Ticks (20 s) entre comida y comida: sin esto, curar sería un spam de clics infinitos. */
    private static final int FEED_COOLDOWN = 400;

    /** Hijos de la afinidad: se anuncian al cruzarlos hacia arriba y nunca se pierden. */
    public static final int[] LOVE_MILESTONES = {25, 50, 75, 100};

    /** Con la afinidad al máximo la waifu pega un 15 % más (vía ATTACK_DAMAGE, igual en los dos loaders). */
    private static final float LOVE_DAMAGE_MULTIPLIER = 1.15F;

    /** Cuenta atrás de {@link #FEED_COOLDOWN}; solo corre en el servidor. */
    private int feedCooldown;

    /** Dos voces seguidas se enciman: cada waifu habla como mucho cada 2 segundos. */
    private static final int VOICE_COOLDOWN = 40;

    /** Cuenta atrás de {@link #VOICE_COOLDOWN}; solo corre en el servidor. */
    private int voiceCooldown;

    /** Afinidad actual (0..100). */
    public int affinity() {
        return Math.max(0, this.entityData.get(DATA_AFFINITY));
    }

    /** Fija la afinidad (solo en el servidor: el cliente la recibe sincronizada). */
    public void setAffinity(int value) {
        this.entityData.set(DATA_AFFINITY, (byte) Mth.clamp(value, 0, 100));
    }

    /** Cuántos hitos (25/50/75/100) ha alcanzado: 0..4, el índice del rótulo de amor. */
    public int loveTier() {
        int affinity = this.affinity();
        int tier = 0;
        for (int milestone : LOVE_MILESTONES) {
            if (affinity >= milestone) {
                tier++;
            }
        }
        return tier;
    }

    /**
     * Voz de esta waifu: el path registrado de su tipo, si está entre las 13 con audio confirmado
     * ({@link WaifuVoices}). Null para el resto: simplemente no hablan.
     */
    protected String voiceId() {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath();
        return WaifuVoices.VOICED.contains(path) ? path : null;
    }

    /** Reproduce una voz de acción (attack/hurt/death/feed/idle) respetando el enfriamiento. */
    public void playVoice(String action) {
        this.playVoice(action, false);
    }

    /**
     * Con {@code force} la voz pasa aunque el enfriamiento esté activo: la muerte y la ultimate
     * no pueden callarse por haber atacado hace un instante. El tono varía ligeramente para que
     * las repeticiones no suenen a disco rayado.
     */
    public void playVoice(String action, boolean force) {
        if (this.level().isClientSide) {
            return;
        }
        String id = this.voiceId();
        if (id == null || !WaifuVoices.has(id, action)) {
            return;
        }
        if (!force && this.voiceCooldown > 0) {
            return;
        }
        SoundEvent sound = WaifuVoices.lookup(id, action);
        if (sound == null) {
            return;
        }
        this.voiceCooldown = VOICE_COOLDOWN;
        this.level().playSound(null, this.blockPosition(), sound, this.getSoundSource(),
                1.0F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    // ---------------------------------------------------------------------
    // Concierto (ver WaifuSongs): se queda quieta, canta, buffa y suelta notas
    // ---------------------------------------------------------------------

    /** Radio del aura de concierto (jugadores). */
    private static final double CONCERT_RADIUS = 12.0D;

    /** Ticks de canción restantes; solo corre en el servidor. */
    private int songTicks;

    public boolean isSinging() {
        return (this.entityData.get(DATA_SINGING) & 1) != 0;
    }

    private void setSinging(boolean singing) {
        this.entityData.set(DATA_SINGING, (byte) (singing ? 1 : 0));
    }

    /** id del registro si esta waifu tiene canción; null si no canta. */
    @Nullable
    private String songId() {
        String path = BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath();
        return WaifuSongs.has(path) ? path : null;
    }

    /**
     * Empieza el concierto. Devuelve false si esta waifu no tiene pista. La canción se oye a unos
     * 64 bloques (volumen 4 sobre un evento de rango variable) y se reproduce en {@code RECORDS}
     * para poder cortarla con el paquete de parada.
     */
    public boolean startSinging() {
        if (this.level().isClientSide) {
            return false;
        }
        String id = this.songId();
        SoundEvent song = id == null ? null : WaifuSongs.lookup(id);
        if (id == null || song == null) {
            return false;
        }
        if (this.isSinging()) {
            this.stopSinging(); // reinicio limpio: nunca dos pistas superpuestas
        }
        if (this.isOrderedToSit()) {
            // El concierto se hace de pie; levantarla también deja el modo coherente al recargar.
            this.setWaifuMode(this.modeBeforeSit == null ? WaifuMode.AGRESIVO : this.modeBeforeSit);
        }
        this.setTarget(null);
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.songTicks = WaifuSongs.lengthTicks(id);
        this.setSinging(true);
        this.level().playSound(null, this.blockPosition(), song, SoundSource.RECORDS, 4.0F, 1.0F);
        this.applyConcertAura();
        return true;
    }

    /** Corta el concierto (si estaba cantando) y manda parar el sonido a los clientes. */
    public void stopSinging() {
        if (!this.isSinging()) {
            this.songTicks = 0;
            return;
        }
        this.setSinging(false);
        this.songTicks = 0;
        if (this.level() instanceof ServerLevel level) {
            String id = this.songId();
            if (id != null) {
                ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(
                        WaifuSongs.soundId(id), SoundSource.RECORDS);
                for (ServerPlayer player : level.players()) {
                    player.connection.send(packet);
                }
            }
        }
    }

    /** Un tick de canción: aura, partículas y parada al acabar. Solo servidor. */
    private void tickSinging() {
        if (this.songTicks <= 0) {
            this.stopSinging();
            return;
        }
        // Aura cada segundo: dura 3 s, así nunca se cae mientras suena aunque el servidor pierda
        // algún tick; al terminar se apaga sola en un par de segundos.
        if (this.songTicks % 20 == 0) {
            this.applyConcertAura();
        }
        if (this.songTicks % 4 == 0 && this.level() instanceof ServerLevel level) {
            this.spawnConcertParticles(level);
        }
        // Plantada: la IA ya está bloqueada por isImmobile(), pero la velocidad residual de un
        // empujón podría arrastrarla; se cancela solo la componente horizontal.
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.songTicks--;
        if (this.songTicks <= 0) {
            this.stopSinging();
        }
    }

    /** Regeneración, Velocidad y Resistencia para los jugadores alrededor mientras canta. */
    private void applyConcertAura() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(CONCERT_RADIUS);
        for (Player player : level.getEntitiesOfClass(Player.class, area, Player::isAlive)) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false, true));
        }
    }

    /** Notas de colores sobre la cabeza y algún END_ROD a los pies. */
    private void spawnConcertParticles(ServerLevel level) {
        double x = this.getX() + (this.random.nextDouble() - 0.5D) * 2.4D;
        double y = this.getY() + 1.2D + this.random.nextDouble() * 1.2D;
        double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 2.4D;
        // En la partícula NOTE el "xDist" elige el color del arcoíris.
        level.sendParticles(ParticleTypes.NOTE, x, y, z, 1,
                0.55D + this.random.nextDouble() * 0.45D, 0.1D, 0.0D, 0.0D);
        if (this.songTicks % 20 == 0) {
            level.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.2D, this.getZ(),
                    2, 0.5D, 0.05D, 0.5D, 0.01D);
        }
    }

    /**
     * Mientras canta no anda ni la empujan: {@code isImmobile()} apaga el {@code serverAiStep} y
     * los goals de movimiento. El estado llega al cliente, así que la pose también es la correcta.
     */
    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.isSinging();
    }

    /**
     * Comida en mano principal de su dueño: cura según nutrición y afinidad, y sube la afinidad.
     * El enfriamiento vive en la waifu, así que alimentar a una no deja comer a las demás.
     */
    private InteractionResult feed(Player player, ItemStack stack, FoodProperties food) {
        if (this.feedCooldown > 0) {
            return InteractionResult.PASS;
        }
        this.feedCooldown = FEED_COOLDOWN;

        int nutrition = food.nutrition();
        // La curación escala con la afinidad: al máximo cura 1.5 veces la base.
        this.heal(nutrition * 2.0F * (1.0F + this.affinity() / 200.0F));

        int before = this.affinity();
        int gained = Mth.clamp(2 + nutrition / 2, 2, 8);
        this.setAffinity(before + gained);

        if (this.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HEART, this.getX(),
                    this.getY() + this.getBbHeight() + 0.2D, this.getZ(),
                    3 + gained, 0.3D, 0.3D, 0.3D, 0.0D);
        }
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS, 1.0F, 1.1F);
        this.playVoice("feed", true);

        // En la barra de acciones: la colección de mensajes de comida no debe llenar el chat.
        player.displayClientMessage(Component.translatable("gachawaifus.love.fed",
                this.getDisplayName(), stack.getHoverName()), true);

        // Aviso de hijo alcanzado (el 100 añade además el rótulo definitivo).
        for (int i = 0; i < LOVE_MILESTONES.length; i++) {
            if (LOVE_MILESTONES[i] > before && LOVE_MILESTONES[i] <= this.affinity()) {
                player.sendSystemMessage(Component.translatable("gachawaifus.love.milestone",
                        this.getDisplayName(),
                        Component.translatable("gachawaifus.love.tier." + (i + 1)).withStyle(
                                net.minecraft.ChatFormatting.LIGHT_PURPLE)));
                this.level().playSound(null, this.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                        SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    /** Estado visual que debe pintar el renderizador ahora mismo. */
    public WaifuSkinState skinState() {
        byte id = this.entityData.get(DATA_SKIN_STATE);
        WaifuSkinState[] states = WaifuSkinState.values();
        return id >= 0 && id < states.length ? states[id] : WaifuSkinState.BASE;
    }

    private void setSkinState(WaifuSkinState state) {
        byte id = (byte) state.ordinal();
        if (this.entityData.get(DATA_SKIN_STATE) != id) {
            this.entityData.set(DATA_SKIN_STATE, id);
        }
    }

    /**
     * Textura que hay que enseñar para esta waifu. Las waifus sin perfil devuelven {@code fallback}
     * (la textura fija de su renderizador), así que esto se puede usar en todos los renderizadores
     * sin tocar a las que no se transforman.
     */
    public ResourceLocation skinTexture(ResourceLocation fallback) {
        return WaifuSkins.textureFor(this, fallback);
    }

    /**
     * Avisa de que esta waifu acaba de usar su habilidad especial. Solo hace algo si su
     * {@link WaifuSkinProfile} define una skin para {@link WaifuSkinState#SPECIAL}.
     */
    public void notifySpecialUsed() {
        if (this.level().isClientSide) {
            return;
        }
        WaifuSkinProfile profile = WaifuSkins.profile(this);
        if (profile != null && profile.special() != null) {
            this.specialSkinTicks = SPECIAL_SKIN_TICKS;
        }
    }

    /**
     * Avisa de que esta waifu acaba de lanzar su ultimate. Solo hace algo si su perfil define una
     * skin para {@link WaifuSkinState#ULTIMATE}.
     */
    public void notifyUltimateUsed() {
        if (this.level().isClientSide) {
            return;
        }
        WaifuSkinProfile profile = WaifuSkins.profile(this);
        if (profile != null && profile.ultimate() != null) {
            this.ultimateSkinTicks = ULTIMATE_SKIN_TICKS;
        }
    }

    /**
     * Decide la skin en el servidor. El combate se detecta por objetivo vivo o por un golpe reciente
     * y se mantiene unos segundos al salir; las habilidades se encienden con los {@code notify*}.
     */
    private void tickSkin() {
        WaifuSkinProfile profile = WaifuSkins.profile(this);
        if (profile == null) {
            return;
        }
        if (this.combatSkinTicks > 0) this.combatSkinTicks--;
        if (this.specialSkinTicks > 0) this.specialSkinTicks--;
        if (this.ultimateSkinTicks > 0) this.ultimateSkinTicks--;

        if (profile.combat() != null) {
            LivingEntity target = this.getTarget();
            boolean canFight = this.waifuMode != WaifuMode.PASIVO
                    && this.waifuMode != WaifuMode.SENTADA;
            boolean inCombat = canFight && target != null && target.isAlive();
            if (!inCombat && canFight && this.getLastHurtByMob() != null) {
                inCombat = this.tickCount - this.getLastHurtByMobTimestamp() < COMBAT_SKIN_LINGER;
            }
            if (inCombat) {
                this.combatSkinTicks = COMBAT_SKIN_LINGER;
            } else if (!canFight) {
                this.combatSkinTicks = 0;
            }
        }

        WaifuSkinState state = WaifuSkinState.BASE;
        if (profile.ultimate() != null && this.ultimateSkinTicks > 0) {
            state = WaifuSkinState.ULTIMATE;
        } else if (profile.special() != null && this.specialSkinTicks > 0) {
            state = WaifuSkinState.SPECIAL;
        } else if (profile.combat() != null && this.combatSkinTicks > 0) {
            state = WaifuSkinState.COMBAT;
        }
        this.setSkinState(state);
    }

    /** {@code false} mientras corre el constructor de {@code Mob}, que es cuando los goals capturan el rango. */
    private boolean goalsRegistrados;

    /**
     * El rango con que la waifu busca enemigos es su radio de deambulación. {@code FOLLOW_RANGE}
     * manda en el área de búsqueda de {@code NearestAttackableTargetGoal} y en hasta dónde puede
     * estar el objetivo antes de soltarlo, así que la waifu pelea solo con lo que alcanza a cuidar;
     * el 32/40/48 que cada una pone en {@code createAttributes()} queda anulado a propósito.
     *
     * <p>Durante la construcción se devuelve el máximo y no el valor actual: el goal de vanilla se
     * guarda el rango <b>una sola vez</b> en su {@code TargetingConditions}, así que si lo capturas
     * de 16, subir el radio después no lo cambiaría nunca. Capturando el máximo, el único límite
     * que manda es el que se lee aquí, que sí es vivo.
     */
    @Override
    public double getAttributeValue(Holder<Attribute> attribute) {
        if (attribute.is(Attributes.FOLLOW_RANGE)) {
            return this.goalsRegistrados ? (double) WanderConfig.range() : (double) WanderConfig.MAX_RANGE;
        }
        if (attribute.is(Attributes.ATTACK_DAMAGE) && this.affinity() >= 100) {
            // Recompensa del amor: +15 % de daño. Se escala aquí y no en un evento porque el
            // evento de daño modificado solo existe en NeoForge (Fabric no tiene homólogo); con
            // la atribución los dos loaders se comportan igual.
            return super.getAttributeValue(attribute) * LOVE_DAMAGE_MULTIPLIER;
        }
        return super.getAttributeValue(attribute);
    }

    /** Estado de los resortes de la pieza del pecho. */
    public BustState bustState() {
        return this.bustState;
    }

    /**
     * Color de clasificación de esta waifu (el del sistema de colores). Se resuelve una sola vez
     * por entidad y se guarda: lo llaman tanto el combate como el renderizado.
     */
    @Nullable
    public WaifuColor waifuColor() {
        if (this.cachedColor == null) {
            this.cachedColor = WaifuColors.of(this.getType());
        }
        return this.cachedColor;
    }

    /** Color ya resuelto (o {@code null} si la waifu no está en la tabla de colores). */
    @Nullable
    private WaifuColor cachedColor;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            BustPhysics.tick(this);
        } else {
            if (this.feedCooldown > 0) this.feedCooldown--;
            if (this.voiceCooldown > 0) this.voiceCooldown--;
            if (this.isSinging()) {
                this.tickSinging();
            } else if (this.random.nextInt(400) == 0 && !this.isOrderedToSit()) {
                this.playVoice("idle");
            }
            this.updateWanderRestriction();
            this.tickSkin();
        }
    }

    // ---------------------------------------------------------------------
    // Radio de deambulación alrededor del dueño (WanderConfig → config/gachawaifus-common.toml)
    // ---------------------------------------------------------------------

    /**
     * Cambia el {@code FollowOwnerGoal} vanilla de la waifu por {@link WaifuFollowOwnerGoal}, que
     * lee el radio en vivo. Lo llama {@link WanderGoalsHandler} cuando la entidad entra al mundo
     * (cada waifu registra sus goals en su propio {@code registerGoals()}, así que se reemplazan
     * después de la construcción en lugar de editar los 63 ficheros).
     *
     * <p>La deambulación no se sustituye: se usa {@link #restrictTo}. Los goals de paseo de vanilla
     * descartan los puntos que caen fuera de la restricción del mob, así que basta con centrar esa
     * restricción en el dueño para que paseen dentro del disco configurado — y cada waifu conserva
     * su velocidad de paseo (algunas van a 0.6/0.7/0.8 a propósito).
     */
    public void applyWanderGoals() {
        for (WrappedGoal wrapped : new ArrayList<>(this.goalSelector.getAvailableGoals())) {
            Goal goal = wrapped.getGoal();
            if (goal instanceof FollowOwnerGoal) {
                this.goalSelector.removeGoal(goal);
                this.goalSelector.addGoal(wrapped.getPriority(), new WaifuFollowOwnerGoal(this, 1.35D));
            } else if (goal instanceof SitWhenOrderedToGoal) {
                // El de vanilla siente a la mascota en cuanto el dueño no está cargado; este solo
                // obedece la orden del modo Sentada (o de las caricias de Astra y Nicole).
                this.goalSelector.removeGoal(goal);
                this.goalSelector.addGoal(wrapped.getPriority(), new WaifuSitGoal(this));
            }
        }
    }

    /** El área de paseo es un círculo de {@link WanderConfig#range()} bloques alrededor del dueño. */
    private void updateWanderRestriction() {
        LivingEntity owner = this.getOwner();
        if (this.waifuMode == WaifuMode.SENTADA) {
            // Radio 0 alrededor del bloque en el que se sentó: ningún goal de paseo encuentra punto
            // válido, así que se queda ahí aunque el goal no declare los flags del sentarse vanilla.
            this.restrictTo(this.seatPos != null ? this.seatPos : this.blockPosition(), 0);
            return;
        }
        if (this.waifuMode == WaifuMode.EN_CASA) {
            // Guardia: su centro es el hogar, no el dueño. Sin hogar registrado todavía, se comporta
            // como en Agresivo (sigue al dueño) para no dejarla clavada en un hueco por no haber usado
            // nunca la Baliza.
            if (this.hasHome()) {
                this.restrictTo(this.homePos, WanderConfig.range());
                return;
            }
        } else if (this.homePos == null && owner != null) {
            // Primer día de la waifu: su hogar provisional es el bloque donde está (donde la invocaste
            // o donde la domesticaste). La Baliza de Hogar lo cambia cuando el jugador quiera otro.
            this.setHome(this.blockPosition(), this.level().dimension());
        }
        if (owner == null || this.isOrderedToSit()) {
            this.clearRestriction();
            return;
        }
        this.restrictTo(owner.blockPosition(), WanderConfig.range());
    }

    /**
     * Vanilla salta al dueño a los 12 bloques (144 de distancia al cuadrado). Con un radio grande
     * eso la teletransportaría antes de pasear, así que el umbral sigue al configuración.
     */
    @Override
    public boolean shouldTryTeleportToOwner() {
        if (this.waifuMode == WaifuMode.EN_CASA && this.hasHome()) {
            return false; // En casa no se abandona el puesto: quien la mueve es el recall, hacia el hogar
        }
        LivingEntity owner = this.getOwner();
        if (owner == null) {
            return false;
        }
        double limit = (double) WanderConfig.range() + 16.0D;
        return this.distanceToSqr(owner) >= limit * limit;
    }

    // ---------------------------------------------------------------------
    // Hogar: posición + dimensión guardadas en el NBT de la entidad
    // ---------------------------------------------------------------------

    /**
     * Bloque del hogar (modo {@link WaifuMode#EN_CASA} y destino del auto-recall). Se pone solo al
     * invocar la waifu y lo reescribe la Baliza de Hogar.
     */
    @Nullable
    private BlockPos homePos;

    /** Dimensión a la que pertenece {@link #homePos}: sin ella el hogar no es alcanzable. */
    @Nullable
    private ResourceKey<Level> homeDimension;

    /** Bloque del hogar, o {@code null} si todavía no se registró. */
    @Nullable
    public BlockPos homePos() {
        return this.homePos;
    }

    /** Dimensión del hogar. */
    @Nullable
    public ResourceKey<Level> homeDimension() {
        return this.homeDimension;
    }

    /** ¿Tiene un hogar completo (bloque + dimensión)? */
    public boolean hasHome() {
        return this.homePos != null && this.homeDimension != null;
    }

    /**
     * Posición que vio el auto-recall en su último barrido. {@code null} = todavía no la ha visto.
     * No se guarda en el NBT: al recargar el mundo se vuelve a tomar como punto de partida.
     */
    @Nullable
    Vec3 lastScanPos;

    /** Barridos seguidos sin moverse un bloque: es lo que distingue a la que va caminando de la perdida. */
    int stillScans;

    /** Fija el hogar (solo en el servidor: el item que lo llama ya se encarga de ese chequeo). */
    public void setHome(@Nullable BlockPos pos, @Nullable ResourceKey<Level> dimension) {
        this.homePos = pos == null ? null : pos.immutable();
        this.homeDimension = pos == null ? null : dimension;
    }

    /**
     * Se teletransporta al lado del jugador, cambiando de dimensión si hace falta.
     *
     * @return {@code true} si efectivamente se movió.
     */
    public boolean recallTo(ServerPlayer player) {
        return WaifuTravel.moveToNear(this, player.serverLevel(), player.position());
    }

    /**
     * Vuelve a su hogar, cambiando de dimensión si hace falta. Si la dimensión del hogar no está
     * cargada en el servidor no se fuerza: devuelve {@code false} y se reintenta en el siguiente scan.
     */
    public boolean sendToHome() {
        if (!this.hasHome()) {
            return false;
        }
        if (!(this.level() instanceof ServerLevel here)) {
            return false;
        }
        ServerLevel target = here.getServer().getLevel(this.homeDimension);
        if (target == null) {
            return false;
        }
        return WaifuTravel.moveToNear(this, target, Vec3.atCenterOf(this.homePos));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false; // No se alimentan con comida de animal normal
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null; // No crían
    }

    // ---------------------------------------------------------------------
    // Modo de combate: Pasivo / Neutro / Agresivo (Shift + clic, mano vacía)
    // ---------------------------------------------------------------------

    /** Modo de combate; por defecto Agresivo (el comportamiento clásico del mod). */
    private WaifuMode waifuMode = WaifuMode.AGRESIVO;

    /**
     * Bloque en el que se sentó (solo con el modo Sentada). No se guarda: al recargar el mundo vale
     * su posición actual, que es el mismo sitio porque no se ha movido.
     */
    @Nullable
    private BlockPos seatPos;

    /** Modo que tenía la waifu antes de que una caricia la sentara, para devolvérselo al levantarla. */
    @Nullable
    private WaifuMode modeBeforeSit;

    /** Modo actual de la waifu. */
    public WaifuMode waifuMode() {
        return this.waifuMode;
    }

    /**
     * Traduce el modo a lo que entiende la IA. {@code SENTADA} usa el sentarse vanilla
     * ({@link #setOrderedToSit}): con esa orden activa, {@code unableToMoveToOwner()} le cierra el
     * goal de seguir al dueño, el {@code SitWhenOrderedToGoal} (prioridad 2) bloquea por flags los
     * goals de paseo, y el flag sincronizado {@code isInSittingPose()} llega al cliente para la pose.
     */
    private void applyWaifuMode() {
        boolean sentada = this.waifuMode == WaifuMode.SENTADA;
        this.setOrderedToSit(sentada);
        this.seatPos = sentada ? this.blockPosition().immutable() : null;
        if (sentada) {
            this.setTarget(null);
            this.getNavigation().stop();
        }
        if (this.waifuMode == WaifuMode.EN_CASA && !this.hasHome()) {
            // Primera vez de guardia sin haber usado nunca la Baliza: el puesto es el bloque donde
            // está plantada, que para eso la acabas de invocar ahí.
            this.setHome(this.blockPosition(), this.level().dimension());
        }
    }

    /**
     * La pose de sentada nunca va por delante de la orden. Vanilla tiene dos caminos que sientan a la
     * mascota sin que nadie se lo pida: {@code SitWhenOrderedToGoal#canUse()} devuelve true en cuanto
     * el dueño no está cargado (desconectado o en otra dimensión), y al cargar el NBT se relanza el
     * "Sitting" guardado. Antes de este modo eso no se veía; ahora dejaría a las waifus salvajes y a
     * las que esperan a su dueño sentadas por defecto.
     */
    @Override
    public void setInSittingPose(boolean sitting) {
        super.setInSittingPose(sitting && this.isOrderedToSit());
    }

    /**
     * Cambia el modo sin pasar por el ciclo de Shift + clic. Lo necesitan las caricias de Astra y
     * Nicole: sentar a una waifu a mano tiene que hacerlo <b>a través</b> de {@link WaifuMode#SENTADA},
     * porque si no el NBT sigue diciendo Agresivo y al recargar el chunk {@link #applyWaifuMode()} la
     * levanta sin que nadie se lo haya pedido.
     */
    public void setWaifuMode(WaifuMode mode) {
        if (mode == this.waifuMode) {
            return;
        }
        if (this.isSinging()) {
            // Cambiar de modo (sentarse, guardia, pasivo…) interrumpe el concierto.
            this.stopSinging();
        }
        this.modeBeforeSit = mode == WaifuMode.SENTADA ? this.waifuMode : null;
        this.waifuMode = mode;
        this.applyWaifuMode();
    }

    /**
     * Sentar o levantar a la waifu con una caricia. Al levantarla devuelve el modo que tenía antes de
     * que la sentaran, no el del ciclo: si estaba en Neutro, vuelve a Neutro.
     */
    public void toggleSitFromPet() {
        if (this.waifuMode == WaifuMode.SENTADA) {
            this.setWaifuMode(this.modeBeforeSit == null ? WaifuMode.AGRESIVO : this.modeBeforeSit);
        } else {
            this.setWaifuMode(WaifuMode.SENTADA);
        }
    }

    /**
     * Si el modelo de esta waifu ya dobló piernas y torso esta frame. Lo escribe
     * {@code WaifuPlayerModel} (solo en el cliente) y lo lee {@code WaifuSittingPose} para no
     * encogerla además: las 6 waifus de GeckoLib animan por JSON, no pasan por ese modelo, y esas sí
     * necesitan el truco del PoseStack.
     */
    private boolean modelSitPose;

    /** Marca que la pose sentada real ya está aplicada en los huesos. */
    public void markModelSitPose() {
        this.modelSitPose = true;
    }

    /** ¿El modelo aplica la pose sentada real, o hay que fingirla desde el PoseStack? */
    public boolean hasModelSitPose() {
        return this.modelSitPose;
    }

    /**
     * Filtro central de objetivos según el modo. Al interceptar {@code setTarget} se controla de
     * golpe a las 19 waifus: sus kits (en {@code aiStep}) solo se disparan con objetivo, y los
     * target goals (agresivos, represalia, defender al dueño) pasan por aquí.
     *
     * <ul>
     *   <li>PASIVO: no acepta ningún objetivo → nunca ataca.</li>
     *   <li>NEUTRO: solo a quien le haya golpeado a él/a su dueño y lo que el dueño tenga en la
     *       mira; jamás a la vista (rechaza el NearestAttackableTargetGoal).</li>
     *   <li>AGRESIVO: todo, como hasta ahora.</li>
     *   <li>SENTADA: nada igual que Pasivo, y encima el sentarse vanilla le apaga el paseo y el
     *       seguimiento: se queda en el bloque donde la dejaste.</li>
     * </ul>
     */
    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && !this.acceptsTarget(target)) {
            target = null;
        }
        super.setTarget(target);
    }

    private boolean acceptsTarget(LivingEntity target) {
        if (this.isSinging()) {
            return false; // en el escenario no se pelea
        }
        return switch (this.waifuMode) {
            case AGRESIVO -> true;
            // PASIVO no ataca y SENTADA tampoco: descansando no se pelea con nadie.
            case PASIVO, SENTADA -> false;
            case NEUTRO -> target == this.getTarget()
                    || target == this.getLastHurtByMob()
                    || (this.getOwner() != null
                            && (target == this.getOwner().getLastHurtByMob()
                                    || target == this.getOwner().getLastHurtMob()));
            // De guardia: solo pelea dentro de su terreno. Sin esto se pondría a atacar al mob que
            // ve el goal de rango mientras ella sigue plantada junto a la casa.
            case EN_CASA -> !this.hasHome()
                    || this.homePos.distSqr(target.blockPosition())
                            <= (double) (WanderConfig.range() * WanderConfig.range());
        };
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND
                && player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && this.isOwnedBy(player)) {
            if (!this.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                WaifuModeMenu.open(serverPlayer, this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // Waifu salvaje (sin dueño, nacida deambulando): el clic derecho intenta reclamarla. El
        // servidor es quien cobra y decide; el cliente solo consume el clic para no usar el item.
        if (hand == InteractionHand.MAIN_HAND && WildWaifus.isWild(this)) {
            if (!this.level().isClientSide) {
                return WildWaifus.onInteract(this, player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        // Concierto: clic derecho con la mano vacía (o con un disco de música) sobre una waifu que
        // tenga canción. Shift + mano vacía sigue abriendo el menú de modos.
        if (hand == InteractionHand.MAIN_HAND && this.isOwnedBy(player) && this.songId() != null) {
            ItemStack held = player.getItemInHand(hand);
            boolean disc = held.has(net.minecraft.core.component.DataComponents.JUKEBOX_PLAYABLE);
            if (held.isEmpty() || disc) {
                if (!this.level().isClientSide) {
                    if (this.isSinging()) {
                        this.stopSinging();
                        player.displayClientMessage(Component.translatable(
                                "gachawaifus.concert.stop", this.getDisplayName()), true);
                    } else if (this.startSinging()) {
                        player.displayClientMessage(Component.translatable(
                                "gachawaifus.concert.start", this.getDisplayName()), true);
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        // Comida en mano del dueño: sistema de amor (cura + afinidad). Las waifus que tienen su
        // propio mobInteract (caricias de Astra y Nicole) caen aquí por super cuando llevan item.
        if (hand == InteractionHand.MAIN_HAND && this.isOwnedBy(player)) {
            ItemStack held = player.getItemInHand(hand);
            // DataComponents.FOOD es el camino vanilla común a los dos loaders: nulo si no es comida.
            FoodProperties food = held.get(net.minecraft.core.component.DataComponents.FOOD);
            if (food != null) {
                if (this.level().isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                return this.feed(player, held, food);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("WaifuMode", (byte) this.waifuMode.ordinal());
        tag.putByte("WaifuAffinity", (byte) this.affinity());
        if (this.homePos != null && this.homeDimension != null) {
            tag.put("WaifuHome", NbtUtils.writeBlockPos(this.homePos));
            tag.putString("WaifuHomeDim", this.homeDimension.location().toString());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WaifuAffinity")) {
            this.setAffinity(tag.getByte("WaifuAffinity"));
        }
        if (tag.contains("WaifuMode")) {
            int ordinal = tag.getByte("WaifuMode");
            WaifuMode[] modes = WaifuMode.values();
            if (ordinal >= 0 && ordinal < modes.length) {
                this.waifuMode = modes[ordinal];
                this.applyWaifuMode(); // el modo manda sobre el "Sitting" vanilla: si no coincide, se relanza
            }
        }
        if (tag.contains("WaifuHome")) {
            BlockPos pos = NbtUtils.readBlockPos(tag, "WaifuHome").orElse(null);
            String dim = tag.getString("WaifuHomeDim");
            if (pos != null && !dim.isEmpty()) {
                try {
                    this.setHome(pos, ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dim)));
                } catch (RuntimeException ignored) {
                    // Id de dimensión inválida (un mundo con un mod de dimensiones ya retirado): se
                    // queda sin hogar antes que romper la carga de la waifu.
                }
            }
        }
    }

    /**
     * Prevención de Fuego Amigo: Las Waifus no se atacan entre sí ni atacan al dueño ni a otros jugadores.
     */
    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof AbstractWaifuEntity) {
            return false;
        }
        if (target instanceof Player) {
            return false;
        }
        return super.wantsToAttack(target, owner);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof AbstractWaifuEntity || source.getEntity() instanceof Player) {
            // Cancelar daño proveniente de otra waifu o del jugador dueño
            if (source.getEntity() == this.getOwner() || source.getEntity() instanceof AbstractWaifuEntity) {
                return false;
            }
        }
        boolean damaged = super.hurt(source, amount);
        if (damaged) {
            if (this.isSinging()) {
                this.stopSinging();
            }
            this.playVoice("hurt");
        }
        return damaged;
    }

    /**
     * Sistema de Caída y Revivificación: al morir, el alma de la waifu se resguarda en la Cápsula
     * Waifu del dueño. Desde la cápsula se la puede revivir con 4 diamantes — sin recetas ambiguas
     * y sin depender de un núcleo genérico que no sabía a quién pertenecía.
     */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            this.stopSinging();
            this.playVoice("death", true);
            LivingEntity owner = this.getOwner();
            Component name = this.getDisplayName();

            // Identidad real de la waifu caída: se guarda server-side, no en un item anónimo.
            WaifuRoster.Entry entry = WaifuRoster.byId(
                    BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath());

            if (owner instanceof Player player && entry != null) {
                WaifuStorageSavedData.get(this.level())
                        .addFallen(player.getUUID(), entry.id(), this.affinity());
                player.sendSystemMessage(Component.translatable(
                        "gachawaifus.waifu.fallen", name.getString()));
                player.sendSystemMessage(Component.translatable("message.gachawaifus.abstract_waifu.1"));
            }

            // Efectos de partículas de muerte celestial
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        30, 0.5, 0.8, 0.5, 0.05);
                serverLevel.sendParticles(ParticleTypes.SOUL,
                        this.getX(), this.getY() + 0.8, this.getZ(),
                        12, 0.4, 0.6, 0.4, 0.02);
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        2, 0.1, 0.1, 0.1, 0.0);
            }
            this.level().playSound(null, this.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.5F);
        }

        super.die(source);
    }
}
