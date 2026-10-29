package com.gachawaifus.entity;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Sección {@code recall} de la configuración COMMON del mod ({@code config/gachawaifus-common.toml}).
 *
 * <p>Como {@link WanderConfig}, no tiene {@code ModConfigSpec} propio: NeoForge solo admite un spec
 * por tipo y mod, y el spec lo construye {@link TeamConfig}. Es COMMON porque el auto-recall lo evalúa
 * el servidor.
 */
public final class RecallConfig {

    /** Activa o apaga el auto-teletransporte de waifus perdidas. */
    public static ModConfigSpec.BooleanValue ENABLED;

    /** Si una waifu puede cruzar de dimensión para volver con su dueño. */
    public static ModConfigSpec.BooleanValue CROSS_DIMENSION;

    /** Distancia a partir de la cual se la considera perdida. */
    public static ModConfigSpec.IntValue DISTANCE;

    private static final int DEFAULT_DISTANCE = 96;

    /** Añade la sección {@code recall} al builder COMMON del mod. Lo llama {@link TeamConfig}. */
    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("recall");
        ENABLED = builder
                .comment("Busca cada pocos segundos las waifus de un jugador conectado que se hayan"
                        + " quedado atras (sin camino, al otro lado del mapa o en otra dimension) y las"
                        + " teletransporta a su lado, o a su hogar si estan en modo En casa.")
                .define("enabled", true);
        CROSS_DIMENSION = builder
                .comment("Deja que el auto-recall cruce dimensiones: una waifu olvidada en el Overworld"
                        + " vuelve con su dueño al Nether. Las que estan de guardia (modo En casa con"
                        + " hogar fijado en ESA dimension) nunca se mueven de su puesto.")
                .define("cross_dimension", true);
        DISTANCE = builder
                .comment("A cuantos bloques del destino (dueno u hogar) se la considera perdida."
                        + " Debe superar el radio de paseo para que no interfiera con el seguimiento"
                        + " normal; si esta de camino no se la mueve hasta que se note que no avanza.")
                .defineInRange("distance", DEFAULT_DISTANCE, 32, 512);
        builder.pop();
    }

    private RecallConfig() {
    }

    private static boolean loaded() {
        return TeamConfig.SPEC.isLoaded() && ENABLED != null && DISTANCE != null;
    }

    /** Valor usado si la config todavía no está cargada (arranque del servidor). */
    public static boolean enabled() {
        return !loaded() || Boolean.TRUE.equals(ENABLED.get());
    }

    public static boolean crossDimension() {
        return !loaded() || Boolean.TRUE.equals(CROSS_DIMENSION.get());
    }

    public static int distance() {
        if (!loaded()) {
            return DEFAULT_DISTANCE;
        }
        Integer value = DISTANCE.get();
        return value == null ? DEFAULT_DISTANCE : value;
    }
}
