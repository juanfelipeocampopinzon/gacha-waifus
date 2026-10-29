package com.gachawaifus.color;

/**
 * Interruptores del sistema de colores, en un solo sitio para poder apagarlo sin tocar código.
 *
 * <p>Son constantes de compilación: cambiar una y volver a compilar. La etiqueta flotante es la
 * excepción — se configura en tiempo de ejecución desde el menú Mods → Config gracias a
 * {@link ColorConfig}.
 */
public final class ColorSettings {

    /** Apaga el sistema entero: sin matriz de daño, sin colores en enemigos, sin avisos. */
    public static final boolean ENABLED = true;

    /** Asigna color aleatorio a los mobs hostiles y lo muestra. */
    public static final boolean ENEMY_COLORS = true;

    /** Aura de partículas del color del mob (2 partículas cada 10 ticks, a ≤24 bloques). */
    public static final boolean ENEMY_AURA = true;

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
