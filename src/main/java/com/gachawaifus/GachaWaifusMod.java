package com.gachawaifus;

import com.gachawaifus.entity.AstraYaoEntity;
import com.gachawaifus.entity.client.AstraYaoRenderer;
import com.gachawaifus.entity.client.EtherBlastRenderer;
import com.gachawaifus.registry.ModCreativeTabs;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GachaWaifusMod.MODID)
public class GachaWaifusMod {
    public static final String MODID = "gachawaifus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public GachaWaifusMod(IEventBus modEventBus) {
        LOGGER.info("Initializing Gacha Waifus Mod for NeoForge 1.21.1!");

        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerRenderers);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ASTRA_YAO.get(), AstraYaoEntity.createAttributes().build());
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ASTRA_YAO.get(), AstraYaoRenderer::new);
        event.registerEntityRenderer(ModEntities.ETHER_BLAST.get(), EtherBlastRenderer::new);
    }
}
