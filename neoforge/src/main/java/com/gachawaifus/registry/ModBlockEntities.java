package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.block.WaifuHomeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Los BlockEntity del mod. Hasta la 6.6.0 no había ninguno: el Bloque de Hogar guardaba el punto en
 * el NBT de cada waifu y no necesitaba estado propio. Ahora el bloque también recuerda qué textura
 * está imitando, y eso sí vive aquí.
 */
public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GachaWaifusMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaifuHomeBlockEntity>> WAIFU_HOME =
            BLOCK_ENTITY_TYPES.register("waifu_home", () -> BlockEntityType.Builder.of(
                    WaifuHomeBlockEntity::new, ModBlocks.WAIFU_HOME_BLOCK.get()).build(null));
}
