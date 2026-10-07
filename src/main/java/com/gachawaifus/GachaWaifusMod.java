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
import com.gachawaifus.entity.RicardoMilosEntity;
import com.gachawaifus.entity.client.RicardoMilosRenderer;
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
import com.gachawaifus.entity.TokisakiKurumiEntity;
import com.gachawaifus.entity.client.TokisakiKurumiRenderer;
import com.gachawaifus.entity.YidhariEntity;
import com.gachawaifus.entity.client.YidhariRenderer;
import com.gachawaifus.entity.RinaEntity;
import com.gachawaifus.entity.client.RinaRenderer;
import com.gachawaifus.entity.GraceHowardEntity;
import com.gachawaifus.entity.client.GraceHowardRenderer;
import com.gachawaifus.entity.VonLycaonEntity;
import com.gachawaifus.entity.client.VonLycaonRenderer;
import com.gachawaifus.entity.KoledaEntity;
import com.gachawaifus.entity.client.KoledaRenderer;
import com.gachawaifus.client.WaifuStorageScreen;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.registry.ModCreativeTabs;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModMenuTypes;
import com.gachawaifus.registry.ModSounds;
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
        ModSounds.SOUND_EVENTS.register(modEventBus);
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
        // ★★★★★ — Hoshimi Miyabi: La Dama de la Luz (Katana / Ice DPS)
        event.put(ModEntities.MIYABI.get(), MiyabiMisiramaEntity.createAttributes().build());
        // ★★★★☆ — Astra Yao
        event.put(ModEntities.ASTRA_YAO.get(), AstraYaoEntity.createAttributes().build());
        // ★★★☆☆ — Ellen Joe
        event.put(ModEntities.ELLEN_JOE.get(), EllenJoeEntity.createAttributes().build());
        // ★★★★★ — Ye Shunguang (Yixuan): Alto Preceptor de Yunkui (Ether / Auric Ink)
        event.put(ModEntities.YE_SHUNGUANG.get(), YeShunguangEntity.createAttributes().build());

        // ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control)
        event.put(ModEntities.NICOLE_DEMARA.get(), NicoleDemaraEntity.createAttributes().build());

        // ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant)
        event.put(ModEntities.ANBY_DEMARA.get(), AnbyDemaraEntity.createAttributes().build());

        /** Grace Howard — "Anomaly" (Electric / Support-Anomaly) */
        event.put(ModEntities.GRACE_HOWARD.get(), GraceHowardEntity.createAttributes().build());

        // ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte" (Físico / Support)
        event.put(ModEntities.UKINAMI_YUZUHA.get(), UkinamiYuzuhaEntity.createAttributes().build());

        /** ★★★★★ Promeia — "La Juez del Krampus Compliance Authority" (Hielo / Anomalía) */
        event.put(ModEntities.PROMEIA.get(), PromeiaEntity.createAttributes().build());
        // ★★★★★ Remielle — "Void Hunter" (Éter / Anomaly DPS)
        event.put(ModEntities.REMIELLE.get(), RemielleEntity.createAttributes().build());

        // ★★★★★ Tokisaki Kurumi — "Time and Space" (Éter / Anomaly)
        event.put(ModEntities.TOKISAKI_KURUMI.get(), TokisakiKurumiEntity.createAttributes().build());

        // ★★★★★ Yidhari Murphy — "Spook Shack" (Hielo / Attack)
        event.put(ModEntities.YIDHARI.get(), YidhariEntity.createAttributes().build());

        /** Von Lycaon — "Victoria Housekeeping" (Ice / Stun) */
        event.put(ModEntities.VON_LYCAON.get(), VonLycaonEntity.createAttributes().build());

        // Jefe de recompensa — Ricardo Milos (skin placeholder; el arte se decide más adelante)
        event.put(ModEntities.RICARDO_MILOS.get(), RicardoMilosEntity.createAttributes().build());

        // Rina — "Victoria Housekeeping Co."
        event.put(ModEntities.RINA_ENTITY.get(), RinaEntity.createAttributes().build());

        // ★★★★★ Koleda Belobog — "Belobog Heavy Industries" (Fuego / Stun)
        event.put(ModEntities.KOLEDA.get(), KoledaEntity.createAttributes().build());
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

        // ★★★★★ Promeia — "La Juez del Krampus Compliance Authority"
        event.registerEntityRenderer(ModEntities.PROMEIA.get(), com.gachawaifus.entity.client.PromeiaRenderer::new);
        // ★★★★★ Remielle — "Void Hunter" (Éter / Anomaly DPS)
        event.registerEntityRenderer(ModEntities.REMIELLE.get(), RemielleRenderer::new);

        // ★★★★★ Tokisaki Kurumi — "Time and Space" (Éter / Anomaly)
        event.registerEntityRenderer(ModEntities.TOKISAKI_KURUMI.get(), TokisakiKurumiRenderer::new);

        // ★★★★★ Yidhari Murphy — "Spook Shack" (Hielo / Attack)
        event.registerEntityRenderer(ModEntities.YIDHARI.get(), YidhariRenderer::new);

        /** Von Lycaon — "Victoria Housekeeping" (Ice / Stun) */
        event.registerEntityRenderer(ModEntities.VON_LYCAON.get(), VonLycaonRenderer::new);

        // Jefe de recompensa — Ricardo Milos (skin placeholder; el arte se decide más adelante)
        event.registerEntityRenderer(ModEntities.RICARDO_MILOS.get(), RicardoMilosRenderer::new);

        // Rina
        event.registerEntityRenderer(ModEntities.RINA_ENTITY.get(), RinaRenderer::new);

        // ★★★★★ Koleda Belobog — modelo de jugador slim (sin GeckoLib: no tiene geo ni animaciones)
        event.registerEntityRenderer(ModEntities.KOLEDA.get(), KoledaRenderer::new);

        /** Grace Howard — "Anomaly" (Electric / Support-Anomaly) */
        event.registerEntityRenderer(ModEntities.GRACE_HOWARD.get(), com.gachawaifus.entity.client.GraceHowardRenderer::new);
    }
}