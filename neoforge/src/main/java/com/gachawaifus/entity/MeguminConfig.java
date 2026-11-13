package com.gachawaifus.entity;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Sección {@code megumin} de la configuración COMMON del mod: la Explosión de Megumin destruye
 * terreno, así que es lo primero que un servidor quiere poder apagar o racionar.
 *
 * <p>Como {@link RecallConfig}, no tiene spec propio: el ÚNICO {@code ModConfigSpec} COMMON lo
 * construye {@link TeamConfig}. Los valores los lee el servidor al detonar, nunca el cliente.
 */
public final class MeguminConfig {

    /** Interruptor general: con false, la ultimate sigue pegando pero no rompe bloques. */
    public static ModConfigSpec.BooleanValue ENABLED;
    /** Radio de la esfera de destrucción de la ultimate, en bloques. */
    public static ModConfigSpec.IntValue RADIUS;
    /** Bloques que se rompen como máximo cada tick (reparte el trabajo y evita tirones). */
    public static ModConfigSpec.IntValue BLOCKS_PER_TICK;
    /** Si los bloques destruidos sueltan su botín (por defecto se pulverizan). */
    public static ModConfigSpec.BooleanValue DROPS;
    /** Si con mobGriefing desactivado tampoco se rompe nada (comportamiento de las explosiones). */
    public static ModConfigSpec.BooleanValue RESPECT_MOB_GRIEFING;
    /** Daño en el centro de la ultimate; cae con la distancia hasta 6 en el borde del radio. */
    public static ModConfigSpec.DoubleValue DAMAGE;
    /** Cooldown de la ultimate en ticks. */
    public static ModConfigSpec.IntValue COOLDOWN;
    /** Radio del mini-cráter de la magia especial (0 = no rompe nada). */
    public static ModConfigSpec.DoubleValue SPECIAL_CRATER;

    private static final int DEFAULT_RADIUS = 20;
    private static final int DEFAULT_BLOCKS_PER_TICK = 600;
    private static final double DEFAULT_DAMAGE = 64.0D;
    private static final int DEFAULT_COOLDOWN = 1800;
    private static final double DEFAULT_SPECIAL_CRATER = 2.5D;

    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("megumin");
        ENABLED = builder
                .comment("La Explosión de Megumin rompe bloques de verdad (cráter de radio 'radius').",
                        "Con false sigue haciendo daño y su VFX, pero el mundo queda intacto.")
                .define("enabled", true);
        RADIUS = builder
                .comment("Radio de la esfera que destruye la ultimate, en bloques (el cráter mide el doble).",
                        "20 = cráter de 40 bloques de diámetro. 32 es el máximo: son ~137.000 bloques.")
                .defineInRange("radius", DEFAULT_RADIUS, 4, 32);
        BLOCKS_PER_TICK = builder
                .comment("Bloques que se rompen por tick. El cráter se abre como una onda expansiva;",
                        "subirlo demasiado provoca tirones en el servidor.")
                .defineInRange("blocks_per_tick", DEFAULT_BLOCKS_PER_TICK, 40, 4000);
        DROPS = builder
                .comment("Si los bloques destruidos sueltan su botín. Por defecto se pulverizan:",
                        "33.000 bloques de drops son miles de ítems y un problema de rendimiento.")
                .define("drops", false);
        RESPECT_MOB_GRIEFING = builder
                .comment("Con la gamerule mobGriefing en false, la Explosión no rompe bloques.",
                        "Es lo que hace una explosión de vanilla; apágalo si quieres que Megumin",
                        "rompa igual en mundos con mobGriefing desactivado.")
                .define("respect_mob_griefing", true);
        DAMAGE = builder
                .comment("Daño en el centro de la ultimate; baja hasta 6 en el borde del radio.")
                .defineInRange("damage", DEFAULT_DAMAGE, 0.0D, 500.0D);
        COOLDOWN = builder
                .comment("Ticks entre ultimates (1800 = 90 s).")
                .defineInRange("cooldown", DEFAULT_COOLDOWN, 100, 72000);
        SPECIAL_CRATER = builder
                .comment("Radio del mini-cráter de su magia especial (cada 15 s; 0 = no rompe nada).",
                        "2,5 abre un agujero de 5 bloques de diámetro. Súbelo y taladra el mundo.")
                .defineInRange("special_crater", DEFAULT_SPECIAL_CRATER, 0.0D, 8.0D);
        builder.pop();
    }

    private MeguminConfig() {
    }

    private static boolean loaded() {
        return TeamConfig.SPEC.isLoaded() && RADIUS != null;
    }

    public static boolean enabled() {
        return !loaded() || Boolean.TRUE.equals(ENABLED.get());
    }

    public static int radius() {
        return loaded() && RADIUS.get() != null ? RADIUS.get() : DEFAULT_RADIUS;
    }

    public static int blocksPerTick() {
        return loaded() && BLOCKS_PER_TICK.get() != null ? BLOCKS_PER_TICK.get() : DEFAULT_BLOCKS_PER_TICK;
    }

    public static boolean drops() {
        return loaded() && DROPS.get() != null && DROPS.get();
    }

    public static boolean respectMobGriefing() {
        return !loaded() || Boolean.TRUE.equals(RESPECT_MOB_GRIEFING.get());
    }

    public static float damage() {
        return loaded() && DAMAGE.get() != null ? DAMAGE.get().floatValue() : (float) DEFAULT_DAMAGE;
    }

    public static int cooldown() {
        return loaded() && COOLDOWN.get() != null ? COOLDOWN.get() : DEFAULT_COOLDOWN;
    }

    public static double specialCrater() {
        return loaded() && SPECIAL_CRATER.get() != null ? SPECIAL_CRATER.get() : DEFAULT_SPECIAL_CRATER;
    }

}
