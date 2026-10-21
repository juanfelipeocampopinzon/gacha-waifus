package com.gachawaifus.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * <b>Curly</b> — el perrito de Fernanfloo: aparece cuando él tiene pelea y copia su objetivo.
 *
 * <p>Es un lobo amansado de fábrica (no huye ni muerde de más), solo existe porque Fernanfloo lo
 * invoca (o alguien con su huevo). Si su dueño desaparece un rato (fuera de carga de chunks), se
 * esfuma con un ¡puf! igual que los Wilson de House.
 */
public class CurlyEntity extends Wolf {

    /** Cada cuántos ticks re-sincroniza su objetivo con el de Fernanfloo. */
    private static final int OWNER_SYNC_INTERVAL = 20;
    /** Ticks seguidos sin su Fernanfloo antes de esfumarse. */
    private static final int MASTER_MISSING_LIMIT = 100;

    @Nullable
    private UUID fernanflooId;
    private int syncCooldown;
    private int masterMissingTicks;

    public CurlyEntity(EntityType<? extends Wolf> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 10;
        this.setPersistenceRequired();
        // Amansado de nacimiento: sin dueño, pero sin IA de lobo salvaje (no huye, no merodea).
        this.setTame(true, false);
        // §6 = dorado: el color del pelo de Curly.
        this.setCustomName(Component.literal("§6Curly"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Wolf.createAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    public void setFernanflooId(@Nullable UUID fernanflooId) {
        this.fernanflooId = fernanflooId;
    }

    @Nullable
    public UUID getFernanflooId() {
        return this.fernanflooId;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide || this.fernanflooId == null) {
            return;
        }
        if (--this.syncCooldown > 0) {
            return;
        }
        this.syncCooldown = OWNER_SYNC_INTERVAL;

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(serverLevel.getEntity(this.fernanflooId) instanceof FernanflooEntity fernanfloo)) {
            if (++this.masterMissingTicks > MASTER_MISSING_LIMIT) {
                serverLevel.sendParticles(ParticleTypes.POOF,
                        this.getX(), this.getY() + 0.6D, this.getZ(), 10, 0.3D, 0.4D, 0.3D, 0.02D);
                this.discard();
            }
            return;
        }
        this.masterMissingTicks = 0;
        if (this.getTarget() != null) {
            return;
        }
        LivingEntity target = fernanfloo.getTarget();
        if (target != null && target.isAlive()) {
            this.setTarget(target);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player
                && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        return super.hurt(source, amount);
    }
}
