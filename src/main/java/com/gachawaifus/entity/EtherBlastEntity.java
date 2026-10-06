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
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class EtherBlastEntity extends ThrowableProjectile {
    private float damage = 9.0F;

    public EtherBlastEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public EtherBlastEntity(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.ETHER_BLAST.get(), shooter, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        // Partículas etéreas y notas musicales en la trayectoria
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    2, 0.1, 0.1, 0.1, 0.05);
            serverLevel.sendParticles(ParticleTypes.NOTE,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    1, 0.05, 0.05, 0.05, 0.1);
        }

        // Si vuela más de 8 segundos (160 ticks) se disipa. Holgado para el alcance
        // ampliado de Astra y Nicole (hasta 30 bloques).
        if (this.tickCount > 160) {
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
                    SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.PLAYERS, 1.2F, 1.4F);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);
        this.discard();
    }
}
