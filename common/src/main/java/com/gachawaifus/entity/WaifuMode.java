package com.gachawaifus.entity;

import net.minecraft.network.chat.Component;

/**
 * Modo de combate de una waifu. Se cambia con Shift + clic (mano vacía) sobre la propia waifu
 * y persiste en el NBT de la entidad.
 *
 * <ul>
 *   <li>{@link #PASIVO}: nunca ataca. Al no aceptar objetivos, los kits de las 19 waifus (que
 *       disparan cuando {@code getTarget()} no es nulo) tampoco se activan.</li>
 *   <li>{@link #NEUTRO}: solo defiende: ataca a quien le haya golpeado a él o a su dueño y ayuda
 *       en lo que el dueño tenga en la mira, pero jamás embiste a la vista.</li>
 *   <li>{@link #AGRESIVO}: comportamiento clásico del mod: ataca a los mobs hostiles de cerca.</li>
 *   <li>{@link #SENTADA}: modo descanso — no sigue al dueño, no pasea, no teletransporta y no ataca:
 *       se queda sentada en el bloque donde estaba hasta que se le cambia de modo.</li>
 *   <li>{@link #EN_CASA}: modo guardia — su centro de operaciones es el hogar (la Baliza de Hogar),
 *       no el dueño: ahí pasea, ahí pelea y ahí vuelve si se pierde. Regresa al hogar aunque el
 *       jugador esté en otra dimensión.</li>
 * </ul>
 *
 * <p>El orden importa: el NBT guarda el ordinal. Se añaden modos SIEMPRE al final.
 */
public enum WaifuMode {

    PASIVO("gachawaifus.mode.pasivo", "§a"),
    NEUTRO("gachawaifus.mode.neutro", "§e"),
    AGRESIVO("gachawaifus.mode.agresivo", "§c"),
    SENTADA("gachawaifus.mode.sentada", "§9"),
    EN_CASA("gachawaifus.mode.en_casa", "§2");

    private final String key;
    private final String color;

    WaifuMode(String key, String color) {
        this.key = key;
        this.color = color;
    }

    /** Clave de idioma del nombre del modo. */
    public String key() {
        return this.key;
    }

    public String colorCode() {
        return this.color;
    }

    /** Nombre coloreado, para mensajes. */
    public Component label() {
        return Component.literal(this.color).append(Component.translatable(this.key));
    }

    /** Siguiente modo del ciclo (Pasivo → Neutro → Agresivo → Sentada → En casa → Pasivo …). */
    public WaifuMode next() {
        WaifuMode[] modes = values();
        return modes[(this.ordinal() + 1) % modes.length];
    }
}
