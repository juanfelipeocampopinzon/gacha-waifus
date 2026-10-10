package com.gachawaifus.entity;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.bust.BustState;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class AbstractWaifuEntity extends TamableAnimal {

    /** Fisica de la pieza del pecho (solo cliente; en el servidor no se usa). */
    private final BustState bustState = new BustState();

    protected AbstractWaifuEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
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
            this.updateWanderRestriction();
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
            }
        }
    }

    /** El área de paseo es un círculo de {@link WanderConfig#range()} bloques alrededor del dueño. */
    private void updateWanderRestriction() {
        LivingEntity owner = this.getOwner();
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
        LivingEntity owner = this.getOwner();
        if (owner == null) {
            return false;
        }
        double limit = (double) WanderConfig.range() + 16.0D;
        return this.distanceToSqr(owner) >= limit * limit;
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

    /** Modo actual de la waifu. */
    public WaifuMode waifuMode() {
        return this.waifuMode;
    }

    /** Shift + clic con mano vacía (solo el dueño): pasa al siguiente modo y avisa. */
    private void cycleWaifuMode(Player player) {
        this.waifuMode = this.waifuMode.next();
        this.setTarget(null); // al cambiar de modo se suelta el objetivo actual
        player.displayClientMessage(Component.literal(
                "§d[GachaWaifus] §f" + this.getDisplayName().getString() + " §7→ modo "
                        + this.waifuMode.colorCode() + this.waifuMode.plainName()
                        + "§7 · Shift + clic para cambiarlo."), true);
        this.level().playSound(null, this.blockPosition(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.4F);
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
        return switch (this.waifuMode) {
            case AGRESIVO -> true;
            case PASIVO -> false;
            case NEUTRO -> target == this.getTarget()
                    || target == this.getLastHurtByMob()
                    || (this.getOwner() != null
                            && (target == this.getOwner().getLastHurtByMob()
                                    || target == this.getOwner().getLastHurtMob()));
        };
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND
                && player.isShiftKeyDown()
                && player.getMainHandItem().isEmpty()
                && this.isOwnedBy(player)) {
            if (!this.level().isClientSide) {
                this.cycleWaifuMode(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("WaifuMode", (byte) this.waifuMode.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WaifuMode")) {
            int ordinal = tag.getByte("WaifuMode");
            WaifuMode[] modes = WaifuMode.values();
            if (ordinal >= 0 && ordinal < modes.length) {
                this.waifuMode = modes[ordinal];
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
        return super.hurt(source, amount);
    }

    /**
     * Sistema de Caída y Revivificación: al morir, el alma de la waifu se resguarda en la Cápsula
     * Waifu del dueño. Desde la cápsula se la puede revivir con 4 diamantes — sin recetas ambiguas
     * y sin depender de un núcleo genérico que no sabía a quién pertenecía.
     */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            LivingEntity owner = this.getOwner();
            Component name = this.getDisplayName();

            // Identidad real de la waifu caída: se guarda server-side, no en un item anónimo.
            WaifuRoster.Entry entry = WaifuRoster.byId(
                    BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath());

            if (owner instanceof Player player && entry != null) {
                WaifuStorageSavedData.get(this.level()).addFallen(player.getUUID(), entry.id());
                player.sendSystemMessage(Component.literal(
                        "§c[GachaWaifus] ¡" + name.getString() + " ha caído en combate!"));
                player.sendSystemMessage(Component.literal(
                        "§7Su alma quedó resguardada en tu §dCápsula Waifu§7. "
                                + "Ábrela (clic derecho) y selecciónala para revivirla con §b4 diamantes§7."));
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
