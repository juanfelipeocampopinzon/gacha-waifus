package com.gachawaifus.entity;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Sección {@code wild} de la configuración COMMON ({@code config/gachawaifus-common.toml}): las
 * waifus salvajes que deambulan por el mundo sin dueño.
 *
 * <p>Sigue el patrón de {@link WanderConfig}: no crea un spec propio (NeoForge rechaza un segundo
 * spec COMMON del mismo mod) y {@link TeamConfig} es quien arma el builder. Es COMMON porque quien
 * decide los spawns y cobra el precio de reclamarlas es el servidor.
 */
public final class WildWaifuConfig {

    /** Interruptor general: sin esto no nace ninguna waifu salvaje. */
    public static ModConfigSpec.BooleanValue ENABLED;

    /** Probabilidad (en %) de intentar un spawn cuando toca el chequeo. */
    public static ModConfigSpec.IntValue CHANCE;

    /** Cada cuántos ticks se tira el dado, por jugador. */
    public static ModConfigSpec.IntValue INTERVAL;

    /** Cuántas waifus salvajes como máximo hay alrededor de un jugador. */
    public static ModConfigSpec.IntValue MAX_NEARBY;

    /** Bolitas Rosas que cuesta CADA intento de doma (no la doma entera). */
    public static ModConfigSpec.IntValue CLAIM_COST;

    /** La doma sale a la primera de cada N intentos (uno entre N). */
    public static ModConfigSpec.IntValue TAME_ODDS;

    /** Intentos que garantizan la doma aunque el dado no acompañe. */
    public static ModConfigSpec.IntValue TAME_PITY;

    private static final boolean DEFAULT_ENABLED = true;
    private static final int DEFAULT_CHANCE = 80;
    private static final int DEFAULT_INTERVAL = 100;
    private static final int DEFAULT_MAX_NEARBY = 10;
    private static final int DEFAULT_CLAIM_COST = 1;
    private static final int DEFAULT_TAME_ODDS = 110;
    private static final int DEFAULT_TAME_PITY = 128;

    /** Añade la sección {@code wild} al builder COMMON del mod. Lo llama {@link TeamConfig}. */
    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("wild");
        ENABLED = builder
                .comment("Waifus salvajes sin duenio que deambulan por el mundo segun su clima.",
                        "Se pueden reclamar con Bolitas Rosas (ver claim_cost).")
                .define("enabled", DEFAULT_ENABLED);
        CHANCE = builder
                .comment("Probabilidad en % de que nazca una waifu salvaje cuando toca el chequeo.",
                        "El chequeo tira por jugador, no por mundo.")
                .defineInRange("spawn_chance", DEFAULT_CHANCE, 0, 100);
        INTERVAL = builder
                .comment("Cada cuantos ticks se tira el dado (20 ticks = 1 segundo).",
                        "100 = un intento cada 5 segundos por jugador. Cada intento que pasa suelta",
                        "una manada de 2 o 3 waifus, asi que el mundo se puebla como el ganado.")
                .defineInRange("check_interval", DEFAULT_INTERVAL, 20, 6000);
        MAX_NEARBY = builder
                .comment("Maximo de waifus salvajes que puede haber alrededor de un jugador.",
                        "Se cuenta en un radio de 96 bloques.")
                .defineInRange("max_nearby", DEFAULT_MAX_NEARBY, 1, 32);
        CLAIM_COST = builder
                .comment("Cuantas Bolitas Rosas cuesta CADA intento de doma (un clic con las bolitas",
                        "en la mano). Con 1 cada clic tira el dado: la suerte se paga por intento, no",
                        "en bloque, asi puedes domar aunque lleves pocas.")
                .defineInRange("claim_cost", DEFAULT_CLAIM_COST, 1, 64);
        TAME_ODDS = builder
                .comment("La doma sale a la primera de cada N intentos. 110 = 0,9 % por intento.",
                        "Comparado con el Terminal Gacha (1,6 % por tirada y 64 de pity): domar es casi",
                        "el doble de caro, pero aqui eliges TU a quien te llevas y la salvaje te espera.")
                .defineInRange("tame_odds", DEFAULT_TAME_ODDS, 2, 1000);
        TAME_PITY = builder
                .comment("Intentos fallidos que garantizan la doma. El contador sube de 1 en 1 por",
                        "intento, es por jugador y por waifu (cada una tiene su propia escala), y se",
                        "reinicia en cuanto esa waifu es tuya.",
                        "El pity del Terminal es 64; aqui el doble, para que la doma sea la via cara.")
                .defineInRange("tame_pity", DEFAULT_TAME_PITY, 2, 1000);
        builder.pop();
    }

    private WildWaifuConfig() {
    }

    public static boolean active() {
        if (!TeamConfig.SPEC.isLoaded() || ENABLED == null) {
            return DEFAULT_ENABLED;
        }
        return Boolean.TRUE.equals(ENABLED.get());
    }

    public static int chance() {
        return value(CHANCE, DEFAULT_CHANCE);
    }

    public static int interval() {
        return value(INTERVAL, DEFAULT_INTERVAL);
    }

    public static int maxNearby() {
        return value(MAX_NEARBY, DEFAULT_MAX_NEARBY);
    }

    /** Precio en Bolitas Rosas; nunca 0 (si se apaga la misión de tala no tiene sentido regalarla). */
    public static int claimCost() {
        return Math.max(1, value(CLAIM_COST, DEFAULT_CLAIM_COST));
    }

    /** Uno entre N: los intentos que hacen falta de media para domar. Nunca menos de 2. */
    public static int tameOdds() {
        return Math.max(2, value(TAME_ODDS, DEFAULT_TAME_ODDS));
    }

    /** Intentos que dan la doma por hecha. Nunca menos de 2 (con 1 no hay suerte que tirar). */
    public static int tamePity() {
        return Math.max(2, value(TAME_PITY, DEFAULT_TAME_PITY));
    }

    private static int value(ModConfigSpec.IntValue configured, int fallback) {
        if (!TeamConfig.SPEC.isLoaded() || configured == null) {
            return fallback;
        }
        Integer current = configured.get();
        return current == null ? fallback : current;
    }
}
