package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.menu.GachaMenu;
import com.gachawaifus.menu.GuideMenu;
import com.gachawaifus.menu.HomeMimicMenu;
import com.gachawaifus.menu.WaifuModeMenu;
import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, GachaWaifusMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<WaifuStorageMenu>> WAIFU_STORAGE =
            MENU_TYPES.register("waifu_storage", () -> new MenuType<>(WaifuStorageMenu::new, FeatureFlags.VANILLA_SET));

    /** Pantalla del gacha: banner promocional + permanente en una sola interfaz. */
    public static final DeferredHolder<MenuType<?>, MenuType<GachaMenu>> GACHA_BANNER =
            MENU_TYPES.register("gacha_banner", () -> new MenuType<>(GachaMenu::new, FeatureFlags.VANILLA_SET));

    /** Guía del Invocador: manual del mod en pantalla de libro (sin huecos, solo texto). */
    public static final DeferredHolder<MenuType<?>, MenuType<GuideMenu>> WAIFU_GUIDE =
            MENU_TYPES.register("waifu_guide", () -> new MenuType<>(GuideMenu::new, FeatureFlags.VANILLA_SET));

    /**
     * Panel de modos de una waifu. Se registra con la fábrica de NeoForge (tres argumentos) porque el
     * lado cliente necesita el id de la entidad y el modo inicial, que viajan en los datos extra del
     * {@code openMenu}.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<WaifuModeMenu>> WAIFU_MODE =
            MENU_TYPES.register("waifu_mode", () -> new MenuType<>(
                    (IContainerFactory<WaifuModeMenu>) WaifuModeMenu::new, FeatureFlags.VANILLA_SET));

    /**
     * Selector de textura del Bloque de Hogar: una rejilla de bloques, sin huecos. También va por
     * fábrica porque el lado cliente necesita la posición del bloque y su imitación actual.
     */
    public static final DeferredHolder<MenuType<?>, MenuType<HomeMimicMenu>> HOME_MIMIC =
            MENU_TYPES.register("home_mimic", () -> new MenuType<>(
                    (IContainerFactory<HomeMimicMenu>) HomeMimicMenu::new, FeatureFlags.VANILLA_SET));

    public static void openWaifuMode(net.minecraft.server.level.ServerPlayer player, com.gachawaifus.entity.AbstractWaifuEntity waifu) {
        int waifuId = waifu.getId();
        int mode = waifu.waifuMode().ordinal();
        String home = waifu.hasHome()
                ? waifu.homePos().toShortString() + " §7("
                        + waifu.level().dimension().location().getPath() + ")"
                : "";
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (containerId, inventory, serverPlayer) -> new WaifuModeMenu(containerId, inventory, waifu),
                        net.minecraft.network.chat.Component.translatable("gachawaifus.mode_screen.title", WaifuModeMenu.plain(waifu.getDisplayName().getString()))),
                buf -> {
                    buf.writeVarInt(waifuId);
                    buf.writeVarInt(mode);
                    buf.writeBoolean(!home.isEmpty());
                    buf.writeUtf(home, 64);
                });
    }
}
