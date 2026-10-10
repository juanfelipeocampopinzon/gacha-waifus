package com.gachawaifus.entity;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Sección {@code wander} de la configuración COMMON del mod (menú Mods → Gacha Waifus → Config, o
 * editando {@code config/gachawaifus-common.toml}).
 *
 * <p>No tiene {@code ModConfigSpec} propio: NeoForge solo admite un spec por tipo y mod, y dos
 * registrados contra el mismo {@code gachawaifus-common.toml} hacen arrancar el juego con
 * "Detected config file conflict". El spec lo construye {@link TeamConfig} y aquí solo se añade la
 * sección.
 *
 * <p>Es COMMON (no CLIENT) porque el radio lo evalúa la IA, que vive en el servidor; un valor
 * CLIENT no sería legible desde un servidor dedicado.
 */
public final class WanderConfig {

    /** Radio (en bloques) alrededor del dueño donde la waifu pasea y hasta donde la sigue. */
    public static ModConfigSpec.IntValue RANGE;

    /** Valor usado si la config todavía no está cargada (arranque del servidor). */
    private static final int DEFAULT_RANGE = 16;

    /** Añade la sección {@code wander} al builder COMMON del mod. Lo llama {@link TeamConfig}. */
    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("wander");
        RANGE = builder
                .comment("Radio en bloques alrededor del duenio dentro del cual la waifu pasea sola",
                        "y hasta donde camina para alcanzarlo. Al superarlo, se teletransporta.",
                        "4 = muy pegada al jugador. 16 = comportamiento clasico del mod.",
                        "64 = casi libre, ideal para caravanas y guardias.")
                .defineInRange("range", DEFAULT_RANGE, 4, 64);
        builder.pop();
    }

    private WanderConfig() {
    }

    /** Radio actual, leído en vivo para que mover el deslizador se note sin reinvocar. */
    public static int range() {
        if (!TeamConfig.SPEC.isLoaded() || RANGE == null) {
            return DEFAULT_RANGE;
        }
        Integer value = RANGE.get();
        return value == null ? DEFAULT_RANGE : value;
    }
}
