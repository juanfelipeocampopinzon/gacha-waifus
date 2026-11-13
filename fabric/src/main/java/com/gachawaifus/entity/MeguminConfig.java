package com.gachawaifus.entity;

/**
 * Valores por defecto de la sección {@code megumin} en Fabric (allí la config COMMON todavía no
 * existe). Deben coincidir con {@code MeguminConfig} de NeoForge para que el comportamiento sea
 * el mismo en los dos loaders.
 */
public final class MeguminConfig {

    private MeguminConfig() {
    }

    public static boolean enabled() {
        return true;
    }

    public static int radius() {
        return 20;
    }

    public static int blocksPerTick() {
        return 600;
    }

    public static boolean drops() {
        return false;
    }

    public static boolean respectMobGriefing() {
        return true;
    }

    public static float damage() {
        return 64.0F;
    }

    public static int cooldown() {
        return 1800;
    }

    public static double specialCrater() {
        return 2.5D;
    }
}
