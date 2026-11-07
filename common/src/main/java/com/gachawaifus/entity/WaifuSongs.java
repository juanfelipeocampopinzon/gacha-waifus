package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.Map;

/**
 * Conciertos de las waifus que tienen una canción propia (voz sola, sin instrumentos).
 *
 * <p>Igual que {@link WaifuVoices}, el {@code SoundEvent} se registra con el mismo id que la clave
 * de {@code sounds.json} ({@code entity/<id>/song}) y el archivo vive en
 * {@code assets/gachawaifus/sounds/entity/<id>/song.ogg}. La diferencia es que aquí solo hay una
 * pista por waifu y suena entera: el mapa guarda cuánto dura para saber cuándo termina el concierto.
 *
 * <p>Para añadir a Teto o Neru basta con dejar su {@code song.ogg} en su carpeta, declararlo en
 * {@code sounds.json} y añadir aquí su entrada con la duración en ticks (20 por segundo). El
 * concierto en sí (quedarse quieta, pose de cante, partículas, aura y parada) es común, pero cada
 * waifu que ataque desde su propio {@code aiStep} debe devolver temprano si {@code isSinging()}
 * para no seguir peleando en el escenario (Miku ya lo hace).
 */
public final class WaifuSongs {

    /** Duración en ticks (20 por segundo) de cada concierto, por id de entidad. */
    private static final Map<String, Integer> LENGTH_TICKS = Map.of(
            "hatsune_miku", 600 // 30 s: prueba_voz_sola.wav
            // "kasane_teto", 600,
            // "akita_neru", 600
    );

    /** ids con concierto. */
    public static final java.util.Set<String> SINGERS = LENGTH_TICKS.keySet();

    public static boolean has(String id) {
        return id != null && LENGTH_TICKS.containsKey(id);
    }

    /** Duración del concierto en ticks, o 0 si esa waifu no canta. */
    public static int lengthTicks(String id) {
        Integer ticks = LENGTH_TICKS.get(id);
        return ticks == null ? 0 : ticks;
    }

    public static ResourceLocation soundId(String id) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/" + id + "/song");
    }

    /** El evento registrado, o null si falta (el audio es decorativo: nunca debe lanzar). */
    public static SoundEvent lookup(String id) {
        return has(id) ? BuiltInRegistries.SOUND_EVENT.get(soundId(id)) : null;
    }

    private WaifuSongs() {
    }
}
