package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.block.GachaMachineBlock;
import com.gachawaifus.block.WaifuHomeBlock;
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

    /**
     * Bloque de Hogar — el punto donde las waifus se quedan de guardia. Colocarlo fija el hogar de
     * todas las waifus invocadas del que lo pone, y romperlo lo borra. Alumbra poco (luz 7) para
     * encontrarlo de noche.
     *
     * <p>Es un {@code BaseEntityBlock}: su {@code RenderShape} es {@code INVISIBLE} y quien lo pinta es
     * su renderer, imitando el bloque que elija el jugador. Por eso lleva {@code noOcclusion} — si se
     * declarara opaco, los vecinos le ocultarían sus caras aunque estuviera fingiendo cristal.
     */
    public static final DeferredBlock<WaifuHomeBlock> WAIFU_HOME_BLOCK =
            BLOCKS.register("waifu_home_block", () -> new WaifuHomeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .strength(1.2F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .lightLevel(state -> 7)));

    public static final DeferredItem<BlockItem> WAIFU_HOME_BLOCK_ITEM =
            BLOCK_ITEMS.registerSimpleBlockItem("waifu_home_block", WAIFU_HOME_BLOCK);
}
