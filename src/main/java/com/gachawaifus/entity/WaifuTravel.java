package com.gachawaifus.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * El salto de una waifu a un destino, dentro de la misma dimensión o cruzando de dimensión.
 *
 * <p>Es la pieza que le falta a vanilla para que una mascota no se pierda: {@code FollowOwnerGoal}
 * solo teletransporta mientras el goal corre, y el goal no corre si {@code getOwner()} devuelve
 * {@code null} — que es exactamente lo que pasa cuando el dueño está en OTRA dimensión o desconectado.
 * La waifu se quedaba plantada en el Overworld para siempre. Aquí el destino lo elige el llamador
 * (el dueño o el hogar) y el cruce de dimensión se hace con {@link Entity#changeDimension}, que
 * conserva el UUID y todo el NBT: el hogar, el modo y el dueño viajan con ella.
 */
final class WaifuTravel {

    /** Cuántos huecos se prueban alrededor del destino antes de rendirse. */
    private static final int ATTEMPTS = 24;

    /**
     * Mueve a la waifu a un hueco libre junto a {@code center} en la dimensión {@code target}.
     *
     * @return {@code true} si se movió. {@code false} si no hubo hueco libre (el llamador reintenta
     *         más tarde) o si el cambio de dimensión fue cancelado.
     */
    static boolean moveToNear(AbstractWaifuEntity waifu, ServerLevel target, Vec3 center) {
        if (!(waifu.level() instanceof ServerLevel here) || waifu.isRemoved()) {
            return false;
        }
        Vec3 spot = findStandSpot(target, waifu, center);
        if (spot == null) {
            return false;
        }
        if (here != target) {
            Entity moved = waifu.changeDimension(new DimensionTransition(
                    target, spot, Vec3.ZERO, waifu.getYRot(), 0.0F, DimensionTransition.PLAY_PORTAL_SOUND));
            if (moved == null) {
                return false;
            }
        } else {
            waifu.moveTo(spot.x, spot.y, spot.z, waifu.getYRot(), 0.0F);
            waifu.setDeltaMovement(Vec3.ZERO);
            waifu.getNavigation().stop();
        }
        flash(target, spot);
        return true;
    }

    /** Destello y chispazo de invocación en el destino (el mismo lenguaje visual que los tokens). */
    private static void flash(ServerLevel level, Vec3 spot) {
        level.sendParticles(ParticleTypes.FLASH, spot.x, spot.y + 1.0D, spot.z, 2, 0.1D, 0.1D, 0.1D, 0.0D);
        level.sendParticles(ParticleTypes.PORTAL, spot.x, spot.y + 1.0D, spot.z, 20, 0.4D, 0.8D, 0.4D, 0.0D);
        level.playSound(null, BlockPos.containing(spot),
                SoundEvents.SHULKER_TELEPORT, SoundSource.PLAYERS, 0.5F, 1.4F);
    }

    /**
     * Hueco libre alrededor del destino. Se prueba primero el anillo de 2-3 bloques (caer encima del
     * jugador lo empuja, y en el mismo bloque dos waifus se quedan trabadas) y, si todo está lleno
     * —una base con las seis invocadas—, se acepta el propio bloque.
     */
    @Nullable
    private static Vec3 findStandSpot(ServerLevel target, AbstractWaifuEntity waifu, Vec3 center) {
        BlockPos origin = BlockPos.containing(center);
        AABB box = waifu.getBoundingBox();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            int dx = waifu.getRandom().nextIntBetweenInclusive(-3, 3);
            int dz = waifu.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue;
            }
            int dy = waifu.getRandom().nextIntBetweenInclusive(-1, 1);
            BlockPos pos = origin.offset(dx, dy, dz);
            if (canStand(target, waifu, box, pos)) {
                return Vec3.atBottomCenterOf(pos);
            }
        }
        return canStand(target, waifu, box, origin) ? Vec3.atBottomCenterOf(origin) : null;
    }

    /**
     * Suelo firme, hueco sin colisiones y dentro de la frontera. Es {@code TamableAnimal#canTeleportTo}
     * de vanilla simplificado a propósito: se llama con la waifu en una dimensión y el destino en otra,
     * y el atajo de vanilla ({@code WalkNodeEvaluator.getPathTypeStatic}) construye su
     * {@code PathfindingContext} con el {@code getLevel()} de la entidad, que leería y contaminaría la
     * caché de path types del mundo de ORIGEN con datos del de destino.
     */
    private static boolean canStand(ServerLevel target, AbstractWaifuEntity waifu, AABB box, BlockPos pos) {
        if (!target.getWorldBorder().isWithinBounds(pos)) {
            return false;
        }
        BlockState below = target.getBlockState(pos.below());
        BlockState here = target.getBlockState(pos);
        if (below.getBlock() instanceof LeavesBlock || below.getBlock() instanceof BubbleColumnBlock) {
            return false;
        }
        if (here.is(Blocks.LAVA) || below.is(Blocks.LAVA) || here.is(Blocks.WATER)) {
            return false;
        }
        if (!below.isFaceSturdy(target, pos.below(), Direction.UP)) {
            return false; // Sin suelo bajo los pies la waifu caería al vacío
        }
        Vec3 shift = Vec3.atBottomCenterOf(pos).subtract(waifu.position());
        return target.noCollision(waifu, box.move(shift));
    }

    private WaifuTravel() {
    }
}
