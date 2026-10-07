package com.gachawaifus.color;

/**
 * Interruptores del sistema de colores, en un solo sitio para poder apagarlo sin tocar código.
 *
 * <p>Todo esto son constantes de compilación: cambiar una y volver a compilar. Si el sistema
 * crece (o el usuario quiere tocar valores sin recompilar), el paso natural es un
 * {@code ModConfigSpec} de NeoForge, pero con 5 booleanos no vale la pena todavía.
 */
public final class ColorSettings {

    /** Apaga el sistema entero: sin matriz de daño, sin colores en enemigos, sin avisos. */
    public static final boolean ENABLED = true;

    /** Asigna color aleatorio a los mobs hostiles y lo muestra. */
    public static final boolean ENEMY_COLORS = true;

    /** Aura de partículas del color del mob (2 partículas cada 10 ticks, a ≤24 bloques). */
    public static final boolean ENEMY_AURA = true;

    /** Etiqueta flotante con el color encima del mob (cliente, ≤24 bloques). */
    public static final boolean FLOATING_LABEL = true;

    /** Cuadradito de color delante del nombre en la etiqueta flotante (se ve el color de un vistazo). */
    public static final boolean LABEL_SWATCH = true;

    /** Indicador bajo la mirilla: color del enemigo al que apuntas y si tu waifu le pega fuerte o flojo. */
    public static final boolean AIM_INDICATOR = true;

    /** Avisos en la barra de acción al pegar fuerte/flojo y al recibir un golpe con color. */
    public static final boolean MESSAGES = true;

    /**
     * ¿El daño que hace el PROPIO jugador con su espada también usa el color?
     * Por defecto no: el color del jugador (el de su waifu activa) solo cuenta para lo que recibe.
     */
    public static final boolean PLAYER_OUTGOING_USES_COLOR = false;

    private ColorSettings() {
    }
}
