package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AstraYaoEntity;
import com.gachawaifus.entity.EtherBlastEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, GachaWaifusMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AstraYaoEntity>> ASTRA_YAO =
            ENTITY_TYPES.register("astra_yao", () ->
                    EntityType.Builder.of(AstraYaoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("astra_yao"));

    public static final DeferredHolder<EntityType<?>, EntityType<EtherBlastEntity>> ETHER_BLAST =
            ENTITY_TYPES.register("ether_blast", () ->
                    EntityType.Builder.<EtherBlastEntity>of(EtherBlastEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("ether_blast"));
}
