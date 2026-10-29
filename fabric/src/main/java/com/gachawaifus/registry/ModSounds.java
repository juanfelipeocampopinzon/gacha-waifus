package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
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

    public static void init() {
    }

    private ModSounds() {
    }
}
