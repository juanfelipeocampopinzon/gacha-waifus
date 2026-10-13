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
 * </ul>
 */
public enum WaifuMode {

    PASIVO("Pasivo", "§a"),
    NEUTRO("Neutro", "§e"),
    AGRESIVO("Agresivo", "§c"),
    SENTADA("Sentada", "§9");

    private final String name;
    private final String color;

    WaifuMode(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String plainName() {
        return this.name;
    }

    public String colorCode() {
        return this.color;
    }

    /** Nombre coloreado, para mensajes. */
    public Component label() {
        return Component.literal(this.color + this.name);
    }

    /** Siguiente modo del ciclo (Pasivo → Neutro → Agresivo → Sentada → Pasivo …). */
    public WaifuMode next() {
        WaifuMode[] modes = values();
        return modes[(this.ordinal() + 1) % modes.length];
    }
}
