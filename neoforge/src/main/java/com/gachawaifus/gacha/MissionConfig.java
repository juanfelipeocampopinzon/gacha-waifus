package com.gachawaifus.gacha;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Sección {@code missions} de la configuración COMMON del mod ({@code config/gachawaifus-common.toml}).
 *
 * <p>Igual que {@link com.gachawaifus.entity.WanderConfig}, no registra un spec propio: NeoForge solo
 * admite uno por tipo y mod, y lo construye {@link TeamConfig}.
 *
 * <p>Los objetivos son COMMON porque los cuentan el servidor (muertes, inventario) y porque la
 * recompensa se entrega ahí; un valor CLIENT no sería legible en un servidor dedicado.
 */
public final class MissionConfig {

    /** Interruptor general del sistema de misiones diarias. */
    public static ModConfigSpec.BooleanValue ENABLED;

    /** Mobs hostiles que hay que matar para la misión de combate. */
    public static ModConfigSpec.IntValue KILLS;

    /** Unidades de comida que hay que conseguir para la misión de cocina. */
    public static ModConfigSpec.IntValue FOOD;

    /** Troncos que hay que conseguir para la misión de tala. */
    public static ModConfigSpec.IntValue LOGS;

    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_GOAL = 10;
    private static final int MAX_GOAL = 64;

    /** Añade la sección {@code missions} al builder COMMON del mod. Lo llama {@link TeamConfig}. */
    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("missions");
        ENABLED = builder
                .comment("Misiones diarias: completarlas regala una Bolita Rosa a cada una.",
                        "Se reinician solas con el dia real (el mismo criterio que /tirada).")
                .define("enabled", DEFAULT_ENABLED);
        KILLS = builder
                .comment("Cuantos mobs hostiles hay que matar para la mision de combate.",
                        "0 = esa mision no entra en el listado de hoy.")
                .defineInRange("hostile_mobs", DEFAULT_GOAL, 0, MAX_GOAL);
        FOOD = builder
                .comment("Cuantas unidades de comida hay que conseguir (cocinar, cosechar, pescar, saquear).",
                        "0 = esa mision no entra en el listado de hoy.")
                .defineInRange("food", DEFAULT_GOAL, 0, MAX_GOAL);
        LOGS = builder
                .comment("Cuantos troncos hay que conseguir talando.",
                        "0 = esa mision no entra en el listado de hoy.")
                .defineInRange("logs", DEFAULT_GOAL, 0, MAX_GOAL);
        builder.pop();
    }

    private MissionConfig() {
    }

    /** ¿Están activas las misiones hoy? (config sin cargar = activas con los valores por defecto). */
    public static boolean active() {
        if (!TeamConfig.SPEC.isLoaded() || ENABLED == null) {
            return DEFAULT_ENABLED;
        }
        return Boolean.TRUE.equals(ENABLED.get());
    }

    static int kills() {
        return goal(KILLS, DEFAULT_GOAL);
    }

    static int food() {
        return goal(FOOD, DEFAULT_GOAL);
    }

    static int logs() {
        return goal(LOGS, DEFAULT_GOAL);
    }

    private static int goal(ModConfigSpec.IntValue value, int fallback) {
        if (!TeamConfig.SPEC.isLoaded() || value == null) {
            return fallback;
        }
        Integer current = value.get();
        return current == null ? fallback : Math.max(0, current);
    }
}
