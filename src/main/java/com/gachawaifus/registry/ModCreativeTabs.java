package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GachaWaifusMod.MODID);

    /** Tab creativo principal del mod: ordenado por raridad (★5 → ★3) */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GACHA_TAB =
            CREATIVE_MODE_TABS.register("gacha_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.gachawaifus"))
                            .icon(() -> new ItemStack(ModItems.ASTRA_YAO_TOKEN.get()))
                            .displayItems((parameters, output) -> {
                                // ★★★★★ — Hoshimi Miyabi: La Dama de la Luz (Katana / Ice DPS)
                                output.accept(ModItems.MIYABI_TOKEN.get());
                                // ★★★★★ Koleda Belobog — "Belobog Heavy Industries" (Fuego / Stun)
                                output.accept(ModItems.KOLEDA_TOKEN.get());
                                output.accept(ModItems.REMIELLE_TOKEN.get());
                                // ★★★★★ Tokisaki Kurumi — "Time and Space" (Éter / Anomaly)
                                output.accept(ModItems.TOKISAKI_KURUMI_TOKEN.get());
                                // ★★★★★ — Ye Shunguang (Yixuan): Alto Preceptor de Yunkui (Ether / Auric Ink)
                                output.accept(ModItems.YE_SHUNGUANG_TOKEN.get());
                                // ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control)
                                output.accept(ModItems.NICOLE_DEMARA_TOKEN.get());
                                // Grace Howard Token — Token de invocación para Grace Howard
                                output.accept(ModItems.GRACE_HOWARD_TOKEN.get());
                                /** Von Lycaon — "Victoria Housekeeping" (Ice / Stun) */
                                output.accept(ModItems.VON_LYCAON_TOKEN.get());
                                // ★★★★☆ — Astra Yao: Estrellas de Lyra (Ether Support)
                                output.accept(ModItems.ASTRA_YAO_TOKEN.get());
                                // ★★★☆☆ — Ellen Joe: Victoria Housekeeping (Ice DPS)
                                output.accept(ModItems.ELLEN_JOE_TOKEN.get());
                                // ★★☆☆☆ — Burnice White: La Llama del Invierno (Fire/Ice Mage)
                                output.accept(ModItems.BURNICE_WHITE_TOKEN.get());
                                output.accept(ModItems.ANBY_DEMARA_TOKEN.get());
                                output.accept(ModItems.UKINAMI_YUZUHA_TOKEN.get());
                                output.accept(ModItems.PROMEIA_TOKEN.get());
                                output.accept(ModItems.YIDHARI_TOKEN.get());
                                output.accept(ModItems.RINA_TOKEN.get());
                                // — Jefe de recompensa: neutral, suelta 10 tiradas (Bolitas Rosas)
                                output.accept(ModItems.RICARDO_MILOS_SPAWN_EGG.get());
                                // — Utilidades: sistema gacha y cápsula (guardar / invocar / revivir)
                                output.accept(ModItems.PINK_BALL.get());
                                output.accept(ModItems.BLUE_BALL.get());
                                output.accept(ModItems.GACHA_TERMINAL.get());
                                output.accept(ModItems.WAIFU_CAPSULE.get());
                            })
                            .build());
}