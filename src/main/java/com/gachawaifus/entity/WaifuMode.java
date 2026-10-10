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
 * </ul>
 */
public enum WaifuMode {

    PASIVO("Pasivo", "§a"),
    NEUTRO("Neutro", "§e"),
    AGRESIVO("Agresivo", "§c");

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

    /** Siguiente modo del ciclo (Pasivo → Neutro → Agresivo → Pasivo …). */
    public WaifuMode next() {
        WaifuMode[] modes = values();
        return modes[(this.ordinal() + 1) % modes.length];
    }
}
