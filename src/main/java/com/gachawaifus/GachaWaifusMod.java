package com.gachawaifus;

import com.gachawaifus.entity.AstraYaoEntity;
import com.gachawaifus.entity.BurniceWhiteEntity;
import com.gachawaifus.entity.MiyabiMisiramaEntity;
import com.gachawaifus.entity.EllenJoeEntity;
import com.gachawaifus.entity.YeShunguangEntity;
import com.gachawaifus.entity.AnbyDemaraEntity;
import com.gachawaifus.entity.NicoleDemaraEntity;
import com.gachawaifus.entity.UkinamiYuzuhaEntity;
import com.gachawaifus.entity.PromeiaEntity;
import com.gachawaifus.entity.client.AstraYaoRenderer;
import com.gachawaifus.entity.client.BurniceWhiteRenderer;
import com.gachawaifus.entity.client.MiyabiMisiramaRenderer;
import com.gachawaifus.entity.client.EllenJoeRenderer;
import com.gachawaifus.entity.client.YeShunguangRenderer;
import com.gachawaifus.entity.client.NicoleDemaraRenderer;
import com.gachawaifus.entity.client.AnbyDemaraRenderer;
import com.gachawaifus.entity.client.EtherBlastRenderer;
import com.gachawaifus.entity.client.UkinamiYuzuhaRenderer;
import com.gachawaifus.entity.client.PromeiaRenderer;
import com.gachawaifus.entity.RemielleEntity;
import com.gachawaifus.entity.client.RemielleRenderer;
import com.gachawaifus.client.WaifuStorageScreen;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.registry.ModCreativeTabs;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModMenuTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
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
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerLayers);
        modEventBus.addListener(this::registerScreens);
    }

    /**
     * Mallas con la pieza del pecho para las waifus que usan el modelo de jugador vanilla.
     * Son capas propias: no se toca la capa compartida de jugador.
     */
    private void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WaifuBustLayers.TUBE, WaifuBustLayers::create);
        event.registerLayerDefinition(WaifuBustLayers.TUBE_BIG, WaifuBustLayers::createBig);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.WAIFU_STORAGE.get(), WaifuStorageScreen::new);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        // ★★★★★ — Hoshimi Miyabi: La Dama de la Luz
        event.put(ModEntities.MIYABI.get(), MiyabiMisiramaEntity.createAttributes().build());
        // ★★★★☆ — Astra Yao
        event.put(ModEntities.ASTRA_YAO.get(), AstraYaoEntity.createAttributes().build());
        // ★★★☆☆ — Ellen Joe
        event.put(ModEntities.ELLEN_JOE.get(), EllenJoeEntity.createAttributes().build());
        // ★★☆☆☆ — Burnice White
        event.put(ModEntities.BURNICE_WHITE.get(), BurniceWhiteEntity.createAttributes().build());
        // ★★★★★ — Ye Shunguang (Yixuan): Alto Preceptor de Yunkui
        event.put(ModEntities.YE_SHUNGUANG.get(), YeShunguangEntity.createAttributes().build());

        // ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control)
        event.put(ModEntities.NICOLE_DEMARA.get(), NicoleDemaraEntity.createAttributes().build());

        // ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant)
        event.put(ModEntities.ANBY_DEMARA.get(), AnbyDemaraEntity.createAttributes().build());

        // ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte" (Físico / Support)
        event.put(ModEntities.UKINAMI_YUZUHA.get(), UkinamiYuzuhaEntity.createAttributes().build());

        // ★★★★★ Promeia — "La Juez del Krampus Compliance Authority" (Hielo / Anomalía)
        event.put(ModEntities.PROMEIA.get(), PromeiaEntity.createAttributes().build());
        // ★★★★★ Remielle — "Void Hunter" (Éter / Anomaly DPS)
        event.put(ModEntities.REMIELLE.get(), RemielleEntity.createAttributes().build());
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // ★★★★★ — Hoshimi Miyabi
        event.registerEntityRenderer(ModEntities.MIYABI.get(), MiyabiMisiramaRenderer::new);
        // ★★★★☆ — Astra Yao
        event.registerEntityRenderer(ModEntities.ASTRA_YAO.get(), AstraYaoRenderer::new);
        // ★★★☆☆ — Ellen Joe
        event.registerEntityRenderer(ModEntities.ELLEN_JOE.get(), EllenJoeRenderer::new);
        // ★★★★★ — Ye Shunguang: Alto Preceptor de Yunkui (Ether / Auric Ink)
        event.registerEntityRenderer(ModEntities.YE_SHUNGUANG.get(), YeShunguangRenderer::new);

        // ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares"
        event.registerEntityRenderer(ModEntities.NICOLE_DEMARA.get(), NicoleDemaraRenderer::new);
        // ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant)
        event.registerEntityRenderer(ModEntities.ANBY_DEMARA.get(), AnbyDemaraRenderer::new);
        // ★★★★☆ — Burnice White
        event.registerEntityRenderer(ModEntities.BURNICE_WHITE.get(), BurniceWhiteRenderer::new);
        event.registerEntityRenderer(ModEntities.ETHER_BLAST.get(), EtherBlastRenderer::new);
        // ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte"
        event.registerEntityRenderer(ModEntities.UKINAMI_YUZUHA.get(), UkinamiYuzuhaRenderer::new);

        // ★★★★★ Promeia — "La Juez del Krampus Compliance Authority" (Hielo / Anomalía)
        event.registerEntityRenderer(ModEntities.PROMEIA.get(), com.gachawaifus.entity.client.PromeiaRenderer::new);
        // ★★★★★ Remielle — "Void Hunter" (Éter / Anomaly DPS)
        event.registerEntityRenderer(ModEntities.REMIELLE.get(), RemielleRenderer::new);
    }
}