package com.gachawaifus.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * La Explosión de Megumin: daño en esfera + destrucción del terreno repartida en varios ticks.
 *
 * <p>El cráter no se cava de golpe: {@link Crater} guarda las posiciones ordenadas del centro
 * hacia fuera y cada tick rompe {@code blocks_per_tick} bloques. Así se ve abrirse como una onda
 * expansiva y el servidor no se congela con 33.000 {@code setBlock} en un solo tick.
 *
 * <p>Todo es servidor: el cliente solo dibuja el VFX. Las reglas (radio, presupuesto, drops,
 * mobGriefing) salen de {@link MeguminConfig}.
 */
public final class ExplosionMagic {

    /** Bloques que nunca se rompen aunque el radio los alcance. */
    private static final Block[] PROTECTED = {
            Blocks.BEDROCK, Blocks.BARRIER, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.STRUCTURE_VOID,
            Blocks.JIGSAW, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.LIGHT,
            Blocks.MOVING_PISTON, Blocks.NETHER_PORTAL
    };

    /** radio -> posiciones empaquetadas, ordenadas de cerca a lejos (caché por radio). */
    private static final Map<Double, int[]> SHELLS = new HashMap<>();
    /** Daño mínimo en el borde del radio de la Explosión. */
    private static final float MIN_DAMAGE = 6.0F;

    private ExplosionMagic() {
    }

    private static boolean protectedBlock(Block block) {
        for (Block candidate : PROTECTED) {
            if (block == candidate) {
                return true;
            }
        }
        return false;
    }

    /**
     * Posiciones de una esfera de {@code radius}, de dentro hacia fuera. Se empaqueta cada
     * desplazamiento en un int para no guardar 33.000 {@link BlockPos}.
     *
     * <p>El radio es {@code double} para que un cráter de 2,5 sea de 2,5 y no se redondee a 2 o 3
     * (la diferencia entre 33 y 123 bloques).
     */
    private static synchronized int[] shell(double radius) {
        int[] cached = SHELLS.get(radius);
        if (cached != null) {
            return cached;
        }
        List<long[]> points = new ArrayList<>();
        int extent = (int) Math.ceil(radius);
        double r2 = radius * radius;
        for (int x = -extent; x <= extent; x++) {
            for (int y = -extent; y <= extent; y++) {
                for (int z = -extent; z <= extent; z++) {
                    int d2 = x * x + y * y + z * z;
                    if (d2 > r2) {
                        continue;
                    }
                    int packed = ((x + 64) << 16) | ((y + 64) << 8) | (z + 64);
                    points.add(new long[]{d2, packed});
                }
            }
        }
        points.sort(Comparator.comparingLong(point -> point[0]));
        int[] result = new int[points.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) points.get(i)[1];
        }
        SHELLS.put(radius, result);
        return result;
    }

    public static boolean griefing(ServerLevel level) {
        if (!MeguminConfig.enabled()) {
            return false;
        }
        return !MeguminConfig.respectMobGriefing()
                || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    /** Daño en esfera con caída radial; no toca al lanzador, al dueño ni a otras waifus. */
    public static int damage(ServerLevel level, Vec3 center, double radius, float maxDamage,
                             LivingEntity caster, @Nullable Entity owner) {
        AABB box = new AABB(center, center).inflate(radius);
        float minimum = Math.min(MIN_DAMAGE, maxDamage * 0.1F);
        int hit = 0;
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (victim == caster || victim == owner || victim instanceof AbstractWaifuEntity) {
                continue;
            }
            if (victim instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            double distance = victim.position().distanceTo(center);
            if (distance > radius) {
                continue;
            }
            double falloff = Math.pow(Math.max(0.0D, 1.0D - distance / radius), 0.75D);
            float applied = minimum + (maxDamage - minimum) * (float) falloff;
            if (victim.hurt(level.damageSources().explosion(caster, owner), applied)) {
                hit++;
            }
            Vec3 away = victim.position().subtract(center);
            away = away.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 0.0D) : away.normalize();
            double strength = 0.6D + 1.4D * falloff;
            victim.push(away.x * strength, 0.55D + 0.9D * falloff, away.z * strength);
            victim.setRemainingFireTicks(Math.max(victim.getRemainingFireTicks(), (int) (120 * falloff)));
        }
        return hit;
    }

    /** Prepara un cráter; se cava llamando a {@link Crater#tick(int)} desde la entidad. */
    public static Crater crater(ServerLevel level, Vec3 center, double radius) {
        return new Crater(level, BlockPos.containing(center), radius);
    }

    /** Cráter repartido en ticks. Inmutable en lo esencial: solo avanza {@code index}. */
    public static final class Crater {
        private final ServerLevel level;
        private final BlockPos origin;
        private final double radius;
        private final int[] offsets;
        private final boolean drops;
        private int index;

        private Crater(ServerLevel level, BlockPos origin, double radius) {
            this.level = level;
            this.origin = origin;
            this.radius = radius;
            this.offsets = shell(radius);
            this.drops = MeguminConfig.drops();
        }

        /** Rompe hasta {@code budget} bloques. Devuelve cuántos rompió. */
        public int tick(int budget) {
            if (!griefing(level)) {
                index = offsets.length;
                return 0;
            }
            int broken = 0;
            while (index < offsets.length && broken < budget) {
                int packed = offsets[index++];
                int x = ((packed >> 16) & 255) - 64;
                int y = ((packed >> 8) & 255) - 64;
                int z = (packed & 255) - 64;
                BlockPos pos = origin.offset(x, y, z);
                if (!level.isLoaded(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.hasBlockEntity() || protectedBlock(state.getBlock())) {
                    continue;
                }
                // Unbreakable (bedrock, barrier...) devuelve -1: se queda.
                if (state.getDestroySpeed(level, pos) < 0.0F) {
                    continue;
                }
                if (drops) {
                    level.destroyBlock(pos, true);
                } else {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
                broken++;
            }
            return broken;
        }

        public boolean done() {
            return index >= offsets.length;
        }

        public double radius() {
            return radius;
        }
    }
}
