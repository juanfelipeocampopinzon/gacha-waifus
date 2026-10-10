package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.block.GachaMachineBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(GachaWaifusMod.MODID);

    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(GachaWaifusMod.MODID);

    /**
     * Maquina Gacha — bloque decorativo con forma de gashapon (rosa, cúpula con cápsulas y
     * cartel). Se rompe como un juguete: rápido y con sonido de cristal.
     */
    public static final DeferredBlock<GachaMachineBlock> GACHA_MACHINE =
            BLOCKS.register("gacha_machine", () -> new GachaMachineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .strength(1.0F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()));

    public static final DeferredItem<BlockItem> GACHA_MACHINE_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("gacha_machine", GACHA_MACHINE);
}
