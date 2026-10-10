package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.gacha.WaifuRoster;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GachaWaifusMod.MODID);

    /**
     * Tab creativo principal. Orden en tres bloques: tokens de waifu en el orden de
     * {@link WaifuRoster#ROTATION} (el mismo del banner), después jefes y por último utilidades.
     */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GACHA_TAB =
            CREATIVE_MODE_TABS.register("gacha_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.gachawaifus"))
                            .icon(() -> new ItemStack(ModItems.ASTRA_YAO_TOKEN.get()))
                            .displayItems((parameters, output) -> {
                                for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
                                    output.accept(new ItemStack(entry.token().get()));
                                }
                                // — Jefes de recompensa y su suelta
                                output.accept(ModItems.RICARDO_MILOS_SPAWN_EGG.get());
                                output.accept(ModItems.DOCTOR_HOUSE_SPAWN_EGG.get());
                                output.accept(ModItems.WILSON_SPAWN_EGG.get());
                                output.accept(ModItems.FERNANFLOO_SPAWN_EGG.get());
                                output.accept(ModItems.CHORIZO.get());
                                // — Utilidades: sistema gacha y cápsula (guardar / invocar / revivir)
                                output.accept(ModItems.PINK_BALL.get());
                                output.accept(ModItems.BLUE_BALL.get());
                                output.accept(ModItems.GACHA_TERMINAL.get());
                                output.accept(ModItems.STANDARD_TERMINAL.get());
                                // — Maquina Gacha (bloque decorativo; la logica de tiradas llega despues)
                                output.accept(ModBlocks.GACHA_MACHINE_ITEM.get());
                                output.accept(ModItems.WAIFU_CAPSULE.get());
                            })
                            .build());
}
