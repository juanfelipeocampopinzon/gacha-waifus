package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.block.GachaMachineBlock;
import com.gachawaifus.block.WaifuHomeBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;

public class ModBlocks {
    private static <T extends Block> Supplier<T> registerBlock(String name, Supplier<T> factory) {
        T block = factory.get();
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name), block);
        return new RegistrySupplier<>(block);
    }

    private static Supplier<BlockItem> registerBlockItem(String name, Supplier<? extends Block> blockSupplier) {
        BlockItem item = new BlockItem(blockSupplier.get(), new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name), item);
        return new RegistrySupplier<>(item);
    }

    public static final Supplier<GachaMachineBlock> GACHA_MACHINE =
            registerBlock("gacha_machine", () -> new GachaMachineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .strength(1.0F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()));

    public static final Supplier<BlockItem> GACHA_MACHINE_ITEM =
            registerBlockItem("gacha_machine", GACHA_MACHINE);

    public static final Supplier<WaifuHomeBlock> WAIFU_HOME_BLOCK =
            registerBlock("waifu_home_block", () -> new WaifuHomeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PINK)
                            .strength(1.2F)
                            .sound(SoundType.GLASS)
                            .noOcclusion()
                            .lightLevel(state -> 7)));

    public static final Supplier<BlockItem> WAIFU_HOME_BLOCK_ITEM =
            registerBlockItem("waifu_home_block", WAIFU_HOME_BLOCK);

    public static void init() {
    }
}
