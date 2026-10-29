package com.gachawaifus.color;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Las cuentas del sistema de colores: quién tiene qué color y cuánto daño se hace entre ellos.
 *
 * <p>Multiplicadores: <b>+33 %</b> contra los 3 colores "opuestos" y <b>−33 %</b> contra los 3
 * siguientes. Como 1.33 y 0.67 promedian exactamente 1.0, la media de daño del roster no cambia.
 */
public final class ColorCombat {

    public static final float STRONG_MULTIPLIER = 1.33F;
    public static final float WEAK_MULTIPLIER = 0.67F;

    /** Radio en el que se busca la waifu activa que le presta su color al jugador. */
    public static final double OWNER_COLOR_RANGE = 32.0D;

    /** Cómo cae un ataque concreto, para el feedback. */
    public enum Matchup {
        STRONG, NEUTRAL, WEAK
    }

    private ColorCombat() {
    }

    public static Matchup matchup(@Nullable WaifuColor attacker, @Nullable WaifuColor defender) {
        if (attacker == null || defender == null) return Matchup.NEUTRAL;
        if (attacker.isStrongAgainst(defender)) return Matchup.STRONG;
        if (attacker.isWeakAgainst(defender)) return Matchup.WEAK;
        return Matchup.NEUTRAL;
    }

    public static float multiplier(@Nullable WaifuColor attacker, @Nullable WaifuColor defender) {
        return switch (matchup(attacker, defender)) {
            case STRONG -> STRONG_MULTIPLIER;
            case WEAK -> WEAK_MULTIPLIER;
            case NEUTRAL -> 1.0F;
        };
    }

    /**
     * El color de cualquier entidad:
     * <ul>
     *   <li>waifu -> el que le toca en {@link WaifuColors};</li>
     *   <li>jugador -> el de su waifu propia más cercana (así tu color es el de tu equipo);</li>
     *   <li>mob hostil -> aleatorio pero <b>estable</b>, derivado de su UUID (sobrevive al guardado
     *       y no gasta NBT ni necesita sincronización: cliente y servidor calculan lo mismo);</li>
     *   <li>cualquier otra cosa (animales, mobs pasivos) -> sin color, daño neutro.</li>
     * </ul>
     */
    @Nullable
    public static WaifuColor colorOf(@Nullable Entity entity) {
        if (entity instanceof AbstractWaifuEntity waifu) return waifu.waifuColor();
        if (entity instanceof Player player) return playerColor(player);
        if (entity instanceof Enemy) return WaifuColor.byIndex(entity.getUUID().hashCode());
        return null;
    }

    /** Igual que {@link #colorOf}, pero si el daño lo causó un proyectil usa a su dueño. */
    @Nullable
    public static WaifuColor attackerColor(@Nullable Entity sourceEntity) {
        if (sourceEntity instanceof Projectile projectile && projectile.getOwner() != null) {
            return colorOf(projectile.getOwner());
        }
        return colorOf(sourceEntity);
    }

    /** Color del jugador = el de su waifu propia viva más cercana dentro del radio. */
    @Nullable
    public static WaifuColor playerColor(Player player) {
        AABB box = player.getBoundingBox().inflate(OWNER_COLOR_RANGE);
        List<AbstractWaifuEntity> near = player.level().getEntitiesOfClass(AbstractWaifuEntity.class, box,
                waifu -> waifu.isAlive() && player.getUUID().equals(waifu.getOwnerUUID()));
        AbstractWaifuEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (AbstractWaifuEntity waifu : near) {
            double dist = waifu.distanceToSqr(player);
            if (dist < closestDist) {
                closestDist = dist;
                closest = waifu;
            }
        }
        return closest != null ? closest.waifuColor() : null;
    }

    /** Devuelve el mismo color aclarado si es tan oscuro que no se vería en partículas. */
    public static float[] visibleRgb(WaifuColor color) {
        float r = ((color.rgb() >> 16) & 0xFF) / 255.0F;
        float g = ((color.rgb() >> 8) & 0xFF) / 255.0F;
        float b = (color.rgb() & 0xFF) / 255.0F;
        if (r + g + b < 0.15F) {          // el negro puro sería invisible: se levanta a gris oscuro
            r = g = b = 0.30F;
        }
        return new float[]{r, g, b};
    }
}
