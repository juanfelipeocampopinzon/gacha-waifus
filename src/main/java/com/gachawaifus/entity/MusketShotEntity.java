package com.gachawaifus.entity;

import com.gachawaifus.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/**
 * La pepita del mosquete de Kurumi: el mismo esqueleto que {@link EtherBlastEntity} (proyectil sin
 * gravedad) pero visible: vuela como el sprite de un item (ver {@link ItemSupplier} y
 * {@code MusketShotRenderer}) con una estela corta de humo, que es lo que hace que se vea el
 * trayectoria desde la boca del cañón hasta el objetivo.
 */
public class MusketShotEntity extends ThrowableProjectile implements ItemSupplier {
    private float damage = 7.0F;

    public MusketShotEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public MusketShotEntity(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.MUSKET_SHOT.get(), shooter, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    /** La bala se dibuja con el sprite de una carga de fuego: bola incandescente, sin gravedad. */
    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    public void tick() {
        super.tick();

        // estela: humo a los lados de la bala y una chispa cada pocos ticks. Antes salia FLAME en
        // cada tick, que se acumulaba en la boca del canon y parecia una bala pegada a Kurumi.
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.04, 0.04, 0.04, 0.01);
            if (this.tickCount % 3 == 0) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.02, 0.02, 0.02, 0.0);
            }
        }

        // Es un perdigon: a los 3 segundos de vuelo se apaga aunque no pegue en nada.
        if (this.tickCount > 60) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        Entity owner = this.getOwner();

        if (target != owner && target instanceof LivingEntity livingTarget) {
            DamageSource source = this.damageSources().indirectMagic(this, owner);
            livingTarget.hurt(source, this.damage);

            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.8F, 0.6F);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        target.getX(), target.getY() + 1.0, target.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 0.6F, 1.4F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY(), this.getZ(),
                    3, 0.1, 0.1, 0.1, 0.02);
        }
        this.discard();
    }
}
