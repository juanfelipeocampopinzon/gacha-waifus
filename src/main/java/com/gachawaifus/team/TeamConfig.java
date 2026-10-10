package com.gachawaifus.team;

import com.gachawaifus.entity.WanderConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuración COMMON del mod (la única que escribe {@code config/gachawaifus-common.toml}): menú
 * Mods → Gacha Waifus → Config, o editando ese toml. Como es COMMON, la leen también los servidores
 * dedicados (no se puede leer una config CLIENT desde el servidor).
 *
 * <p>Tiene dos secciones: {@code team} (límite de waifus invocadas) y {@code wander} (radio de
 * deambulación, definida en {@link WanderConfig}). NeoForge rechaza un segundo spec COMMON del mismo
 * mod con "Detected config file conflict", así que todo lo COMMON se construye aquí.
 */
public final class TeamConfig {

    public static final ModConfigSpec SPEC;

    /** 0 = ilimitado; 1–64 = cuántas waifus puede tener invocadas un jugador a la vez. */
    public static final ModConfigSpec.IntValue LIMIT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("team");
        LIMIT = builder
                .comment("Cuántas waifus puede tener un jugador invocadas en el mundo a la vez.",
                        "0 = ilimitado (comportamiento clasico: una de cada tipo).",
                         "Con limite, los tokens y la Capsula no invocan mas: guarda una waifu con",
                         "Shift + clic en la Capsula Waifu y vuelve a invocarla despues.")
                .defineInRange("limit", 0, 0, 64);
        builder.pop();
        WanderConfig.appendTo(builder);
        SPEC = builder.build();
    }

    private TeamConfig() {
    }
}
