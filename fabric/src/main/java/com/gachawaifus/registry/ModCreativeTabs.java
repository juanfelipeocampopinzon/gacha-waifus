package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.gacha.WaifuRoster;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final Supplier<CreativeModeTab> GACHA_TAB = register("gacha_tab");

    private static Supplier<CreativeModeTab> register(String name) {
        CreativeModeTab tab = FabricItemGroup.builder()
                .title(Component.translatable("itemGroup.gachawaifus"))
                .icon(() -> new ItemStack(ModItems.ASTRA_YAO_TOKEN.get()))
                .displayItems((parameters, output) -> {
                    for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
                        output.accept(new ItemStack(entry.token().get()));
                    }
                    output.accept(ModItems.RICARDO_MILOS_SPAWN_EGG.get());
                    output.accept(ModItems.DOCTOR_HOUSE_SPAWN_EGG.get());
                    output.accept(ModItems.WILSON_SPAWN_EGG.get());
                    output.accept(ModItems.FERNANFLOO_SPAWN_EGG.get());
                    output.accept(ModItems.CURLY_SPAWN_EGG.get());
                    output.accept(ModItems.TUNG_TUNG_SAHUR_SPAWN_EGG.get());
                    output.accept(ModItems.BATE_BEISBOL.get());
                    output.accept(ModItems.PINK_BALL.get());
                    output.accept(ModItems.BLUE_BALL.get());
                    output.accept(ModItems.GACHA_TERMINAL.get());
                    output.accept(ModBlocks.GACHA_MACHINE_ITEM.get());
                    output.accept(ModItems.WAIFU_CAPSULE.get());
                    output.accept(ModItems.WAIFU_BEACON.get());
                    output.accept(ModBlocks.WAIFU_HOME_BLOCK_ITEM.get());
                    output.accept(ModItems.WAIFU_GUIDE.get());
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name), tab);
        return new RegistrySupplier<>(tab);
    }

    public static void init() {
    }
}
