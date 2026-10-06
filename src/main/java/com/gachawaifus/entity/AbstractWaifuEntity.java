package com.gachawaifus.entity;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.bust.BustState;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            BustPhysics.tick(this);
        }
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
