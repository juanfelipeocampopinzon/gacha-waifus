package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GachaWaifusMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<WaifuStorageMenu>> WAIFU_STORAGE =
            MENU_TYPES.register("waifu_storage", () -> new MenuType<>(WaifuStorageMenu::new, FeatureFlags.VANILLA_SET));
}
