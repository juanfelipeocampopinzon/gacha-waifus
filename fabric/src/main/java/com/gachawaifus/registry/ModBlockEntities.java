package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.block.WaifuHomeBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class ModBlockEntities {
    private static <T extends BlockEntityType<?>> Supplier<T> register(String name, T type) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name), type);
        return new RegistrySupplier<>(type);
    }

    public static final Supplier<BlockEntityType<WaifuHomeBlockEntity>> WAIFU_HOME =
            register("waifu_home", BlockEntityType.Builder.of(
                    WaifuHomeBlockEntity::new, ModBlocks.WAIFU_HOME_BLOCK.get()).build(null));

    public static void init() {
    }
}
