package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Voces de las 13 waifus cuyo audio está confirmado como del mismo personaje (ver
 * {@code research/sounds_db/INDEX_SHARED.csv}:Idle/ataque/daño/muerte/comida por waifu, extraídos
 * de Waifus Party). Los archivos viven en {@code assets/gachawaifus/sounds/entity/<id>/} y se
 * declaran en {@code sounds.json}; el {@code SoundEvent} de cada id lo registran los
 * {@code ModSounds} de cada loader a partir de la lista de esta clase.
 */
public final class WaifuVoices {

    /** Actions con audio: attack/hurt/death/feed/idle. */
    public static final List<String> ACTIONS = List.of("attack", "hurt", "death", "feed", "idle");

    /** ids de entidad (path del registro) con voces, idénticos a los de {@code WaifuRoster}. */
    public static final Set<String> VOICED = Set.of(
            "anby_demara", "aqua", "darkness", "ellen_joe", "hatsune_miku", "jane_doe",
            "megumin", "miyabi", "nicole_demara", "qingyi", "tokisaki_kurumi", "vivian",
            "zhu_yuan");

    /** Excepciones: waifus sin audio para alguna action (Zhu Yuan no tiene líneas de ataque). */
    private static final Map<String, Set<String>> MISSING = Map.of("zhu_yuan", Set.of("attack"));

    public static boolean has(String id, String action) {
        return VOICED.contains(id) && !MISSING.getOrDefault(id, Set.of()).contains(action);
    }

    public static ResourceLocation soundId(String id, String action) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/" + id + "/" + action);
    }

    /** El evento registrado, o null si falta (nunca debe lanzar: el audio es decorativo). */
    public static SoundEvent lookup(String id, String action) {
        return BuiltInRegistries.SOUND_EVENT.get(soundId(id, action));
    }

    private WaifuVoices() {
    }
}
