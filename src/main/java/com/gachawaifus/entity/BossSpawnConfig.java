package com.gachawaifus.entity;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Sección {@code bosses} de la configuración COMMON del mod: con qué frecuencia aparecen los jefes
 * de forma natural (Ricardo Milos, Doctor House, Wilson y Fernanfloo).
 *
 * <p>Como {@link WanderConfig}, no tiene spec propio: NeoForge solo admite un {@code ModConfigSpec}
 * por tipo y mod, así que {@link TeamConfig} la mete en {@code gachawaifus-common.toml}.
 */
public final class BossSpawnConfig {

    /** % de apariciones naturales que se dejan pasar. 15 = jefes raros (por defecto). */
    public static ModConfigSpec.IntValue SPAWN_CHANCE;

    private static final int DEFAULT_CHANCE = 15;

    /** Añade la sección {@code bosses} al builder COMMON del mod. Lo llama {@link TeamConfig}. */
    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("bosses");
        SPAWN_CHANCE = builder
                .comment("Porcentaje de intentos de aparicion natural de los jefes que se dejan pasar.",
                        "Afecta solo a la aparicion espontanea por el mundo: los huevos de spawn, los",
                        "tokens y los teletransportes de los propios jefes nunca se cortan.",
                        "0 = no aparecen solos. 15 = raros (por defecto). 100 = como el peso del bioma.")
                .defineInRange("spawn_chance", DEFAULT_CHANCE, 0, 100);
        builder.pop();
    }

    private BossSpawnConfig() {
    }

    /** % actual (15 si la config aún no está cargada). */
    public static int spawnChance() {
        if (!TeamConfig.SPEC.isLoaded() || SPAWN_CHANCE == null) {
            return DEFAULT_CHANCE;
        }
        Integer value = SPAWN_CHANCE.get();
        return value == null ? DEFAULT_CHANCE : value;
    }
}
