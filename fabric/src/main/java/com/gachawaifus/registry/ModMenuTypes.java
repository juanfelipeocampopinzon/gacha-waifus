package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.menu.GachaMenu;
import com.gachawaifus.menu.GuideMenu;
import com.gachawaifus.menu.HomeMimicMenu;
import com.gachawaifus.menu.WaifuModeMenu;
import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class ModMenuTypes {
    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String name, MenuType.MenuSupplier<T> factory) {
        MenuType<T> type = Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name),
                new MenuType<>(factory, FeatureFlags.VANILLA_SET)
        );
        return new RegistrySupplier<>(type);
    }

    public static final Supplier<MenuType<WaifuStorageMenu>> WAIFU_STORAGE =
            register("waifu_storage", WaifuStorageMenu::new);

    public static final Supplier<MenuType<GachaMenu>> GACHA_BANNER =
            register("gacha_banner", GachaMenu::new);

    public static final Supplier<MenuType<GuideMenu>> WAIFU_GUIDE =
            register("waifu_guide", GuideMenu::new);

    public static final Supplier<MenuType<WaifuModeMenu>> WAIFU_MODE =
            register("waifu_mode", (id, inv) -> new WaifuModeMenu(id, inv, (net.minecraft.network.RegistryFriendlyByteBuf) null));

    public static final Supplier<MenuType<HomeMimicMenu>> HOME_MIMIC =
            register("home_mimic", (id, inv) -> new HomeMimicMenu(id, inv, (net.minecraft.network.RegistryFriendlyByteBuf) null));

    public static void openWaifuMode(net.minecraft.server.level.ServerPlayer player, com.gachawaifus.entity.AbstractWaifuEntity waifu) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (containerId, inventory, serverPlayer) -> new WaifuModeMenu(containerId, inventory, waifu),
                net.minecraft.network.chat.Component.translatable("gachawaifus.mode_screen.title", WaifuModeMenu.plain(waifu.getDisplayName().getString()))));
    }

    public static void init() {
    }
}
