package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GachaWaifusMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GACHA_TAB =
            CREATIVE_MODE_TABS.register("gacha_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.gachawaifus"))
                            .icon(() -> new ItemStack(ModItems.ASTRA_YAO_TOKEN.get()))
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.ASTRA_YAO_TOKEN.get());
                            })
                            .build());
}
