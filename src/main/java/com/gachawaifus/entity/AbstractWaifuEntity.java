package com.gachawaifus.entity;

import com.gachawaifus.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
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

    protected AbstractWaifuEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
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
     * Sistema de Muerte y Núcleo Durmiente: Al morir, la waifu deja caer su núcleo para ser revivida con diamantes.
     */
    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide) {
            LivingEntity owner = this.getOwner();
            Component name = this.getDisplayName();

            // Notificación global / al dueño
            if (owner instanceof Player player) {
                player.sendSystemMessage(Component.literal(
                        "§c[GachaWaifus] ¡" + name.getString() + " ha caído en batalla! Ha soltado su Núcleo Durmiente. Craftéalo con 4 Diamantes para revivirla."
                ));
            }

            // Efectos de partículas de muerte celestial
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        30, 0.5, 0.8, 0.5, 0.05);
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        this.getX(), this.getY() + 1.0, this.getZ(),
                        2, 0.1, 0.1, 0.1, 0.0);
            }
            this.level().playSound(null, this.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.5F);

            // Dejar caer el núcleo durmiente
            this.spawnAtLocation(new ItemStack(ModItems.DORMANT_WAIFU_CORE.get()));
        }

        super.die(source);
    }
}
