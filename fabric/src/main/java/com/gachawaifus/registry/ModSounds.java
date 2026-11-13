package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.WaifuSongs;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class ModSounds {

    private static Supplier<SoundEvent> register(String path, ResourceLocation id) {
        SoundEvent event = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        return new RegistrySupplier<>(event);
    }

    public static final ResourceLocation RICARDO_MILOS_THEME_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "boss/ricardo_milos");

    public static final Supplier<SoundEvent> RICARDO_MILOS_THEME =
            register("boss/ricardo_milos", RICARDO_MILOS_THEME_ID);

    public static final ResourceLocation FERNANFLOO_ATTACK_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/fernanfloo/attack");

    public static final Supplier<SoundEvent> FERNANFLOO_ATTACK =
            register("entity/fernanfloo/attack", FERNANFLOO_ATTACK_ID);

    public static final ResourceLocation MUSKET_SHOT_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/tokisaki_kurumi/musket_shot");

    public static final Supplier<SoundEvent> MUSKET_SHOT =
            register("entity/tokisaki_kurumi/musket_shot", MUSKET_SHOT_ID);

    public static final ResourceLocation MEGUMIN_EXPLOSION_CHARGE_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/megumin/explosion_charge");

    public static final Supplier<SoundEvent> MEGUMIN_EXPLOSION_CHARGE =
            register("entity/megumin/explosion_charge", MEGUMIN_EXPLOSION_CHARGE_ID);

    public static final ResourceLocation MEGUMIN_EXPLOSION_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/megumin/explosion");

    public static final Supplier<SoundEvent> MEGUMIN_EXPLOSION =
            register("entity/megumin/explosion", MEGUMIN_EXPLOSION_ID);

    public static final ResourceLocation MEGUMIN_EXPLOSION_RAY_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "entity/megumin/explosion_ray");

    public static final Supplier<SoundEvent> MEGUMIN_EXPLOSION_RAY =
            register("entity/megumin/explosion_ray", MEGUMIN_EXPLOSION_RAY_ID);

    static {
        // Voces de las 13 waifus con audio confirmado: se registran desde la lista común, siempre
        // con el mismo id que declare {@code sounds.json} (si no coinciden, no suena nada).
        for (String id : com.gachawaifus.entity.WaifuVoices.VOICED) {
            for (String action : com.gachawaifus.entity.WaifuVoices.ACTIONS) {
                if (!com.gachawaifus.entity.WaifuVoices.has(id, action)) {
                    continue;
                }
                ResourceLocation voiceId = com.gachawaifus.entity.WaifuVoices.soundId(id, action);
                Registry.register(BuiltInRegistries.SOUND_EVENT, voiceId,
                        SoundEvent.createVariableRangeEvent(voiceId));
            }
        }

        // Canciones de concierto (una pista por waifu, se reproduce entera).
        for (String id : WaifuSongs.SINGERS) {
            ResourceLocation songId = WaifuSongs.soundId(id);
            Registry.register(BuiltInRegistries.SOUND_EVENT, songId,
                    SoundEvent.createVariableRangeEvent(songId));
        }
    }

    public static void init() {
    }

    private ModSounds() {
    }
}
