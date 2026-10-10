package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sonidos propios del mod (los archivos viven en {@code assets/gachawaifus/sounds/} y se declaran
 * en {@code assets/gachawaifus/sounds.json}).
 *
 * <p>Cada {@link SoundEvent} se registra con el <b>mismo id</b> que la clave de {@code sounds.json}:
 * si los dos nombres no coinciden, el juego no encuentra el archivo y no suena nada (los avisos del
 * registro de sonidos son silenciosos, así que es el fallo típico).
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, GachaWaifusMod.MODID);

    /**
     * Tema del jefe <b>Ricardo Milos</b>: suena al empezar la pelea (cuando consigue un objetivo)
     * y se corta cuando muere.
     *
     * <p>Es de rango variable para que llegue lejos: el alcance audible es {@code 16 × volumen}, y
     * la entidad lo reproduce con volumen 4 (unos 64 bloques).
     */
    public static final ResourceLocation RICARDO_MILOS_THEME_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "boss/ricardo_milos");

    public static final DeferredHolder<SoundEvent, SoundEvent> RICARDO_MILOS_THEME =
            SOUND_EVENTS.register("boss/ricardo_milos",
                    () -> SoundEvent.createVariableRangeEvent(RICARDO_MILOS_THEME_ID));

    /**
     * Grito de ataque de <b>Fernanfloo</b>: el "¡CHORIZO!" de su escena (segundos 5-7 del video).
     * Suena cada vez que golpea a alguien.
     */
    public static final ResourceLocation FERNANFLOO_ATTACK_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/fernanfloo/attack");

    public static final DeferredHolder<SoundEvent, SoundEvent> FERNANFLOO_ATTACK =
            SOUND_EVENTS.register("entity/fernanfloo/attack",
                    () -> SoundEvent.createVariableRangeEvent(FERNANFLOO_ATTACK_ID));

    private ModSounds() {
    }
}
