package com.gachawaifus;

import com.gachawaifus.color.ColorConfig;
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
import com.gachawaifus.entity.DoctorHouseEntity;
import com.gachawaifus.entity.WilsonEntity;
import com.gachawaifus.entity.FernanflooEntity;
import com.gachawaifus.entity.client.RicardoMilosRenderer;
import com.gachawaifus.entity.client.DoctorHouseRenderer;
import com.gachawaifus.entity.client.WilsonRenderer;
import com.gachawaifus.entity.client.FernanflooRenderer;
import com.gachawaifus.entity.client.AstraYaoRenderer;
import com.gachawaifus.entity.client.BurniceWhiteRenderer;
import com.gachawaifus.entity.client.MiyabiMisiramaRenderer;
import com.gachawaifus.entity.client.EllenJoeRenderer;
import com.gachawaifus.entity.client.YeShunguangRenderer;
import com.gachawaifus.entity.client.NicoleDemaraRenderer;
import com.gachawaifus.entity.client.AnbyDemaraRenderer;
import com.gachawaifus.entity.Soldier11Entity;
import com.gachawaifus.entity.client.Soldier11Renderer;
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
import com.gachawaifus.entity.NekomataEntity;
import com.gachawaifus.entity.BillyKidEntity;
import com.gachawaifus.entity.client.BillyKidRenderer;
import com.gachawaifus.entity.client.NekomataRenderer;
import com.gachawaifus.entity.YixuanEntity;
import com.gachawaifus.entity.ZhuYuanEntity;
import com.gachawaifus.entity.client.ZhuYuanRenderer;
import com.gachawaifus.entity.client.QingyiRenderer;
import com.gachawaifus.entity.client.YixuanRenderer;
import com.gachawaifus.client.WaifuStorageScreen;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.registry.ModBlocks;
import com.gachawaifus.registry.ModCreativeTabs;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModMenuTypes;
import com.gachawaifus.registry.ModSounds;
import com.gachawaifus.team.TeamConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GachaWaifusMod.MODID)
public class GachaWaifusMod {
    public static final String MODID = "gachawaifus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public GachaWaifusMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Gacha Waifus Mod for NeoForge 1.21.1!");

        // Configs: CLIENT (etiqueta de color) y COMMON (límite del equipo + radio de paseo de las
        // waifus). Solo puede haber UN spec por tipo y mod: dos contra gachawaifus-common.toml
        // revientan el arranque con "Detected config file conflict". Las secciones COMMON se juntan
        // en TeamConfig.SPEC. Todo sale en el menú Mods → Gacha Waifus → Config.
        modContainer.registerConfig(ModConfig.Type.CLIENT, ColorConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, TeamConfig.SPEC);

        // Pantalla de configuración para el botón "Config" del menú Mods: sin este
        // registro el botón sale desactivado (IConfigScreenFactory.getForMod vacío).
        // ConfigurationScreen es de cliente: solo se registra en Dist.CLIENT.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
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
        event.registerLayerDefinition(WaifuBustLayers.TUBE_WIDE, WaifuBustLayers::createWide);
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

        // Jefe — Doctor House (invoca Wilsons, suelta Bolitas Azules)
        event.put(ModEntities.DOCTOR_HOUSE.get(), DoctorHouseEntity.createAttributes().build());
        event.put(ModEntities.WILSON.get(), WilsonEntity.createAttributes().build());

        // Jefe — Fernanfloo (chorizo en mano, suelta Bolitas Rosas)
        event.put(ModEntities.FERNANFLOO.get(), FernanflooEntity.createAttributes().build());

        // Rina — "Victoria Housekeeping Co."
        event.put(ModEntities.RINA_ENTITY.get(), RinaEntity.createAttributes().build());

        // ★★★★★ Koleda Belobog — "Belobog Heavy Industries" (Fuego / Stun)
        event.put(ModEntities.KOLEDA.get(), KoledaEntity.createAttributes().build());

        /** Soldier 11 — Attack */
        event.put(ModEntities.SOLDIER_11.get(), Soldier11Entity.createAttributes().build());

        // ★★★★★ Tobichi Origami — "Defense" (White / Defense)
        event.put(ModEntities.TOBICHI_ORIGAMI.get(), com.gachawaifus.entity.TobichiOrigamiEntity.createAttributes().build());

        // ★★★★★ Nekomiya Mana (Nekomata) — "Cunning Hares" (Physical / Attack)
        event.put(ModEntities.NEKOMATA.get(), NekomataEntity.createAttributes().build());
        event.put(ModEntities.BILLY_KID.get(), BillyKidEntity.createAttributes().build());
        event.put(ModEntities.YIXUAN.get(), YixuanEntity.createAttributes().build());
        event.put(ModEntities.ZHU_YUAN.get(), ZhuYuanEntity.createAttributes().build());

        // ★★★★★ Qingyi — "Tea Master" (Blue / Stun)
        event.put(ModEntities.QINGYI.get(), com.gachawaifus.entity.QingyiEntity.createAttributes().build());
        event.put(ModEntities.PYROIS.get(), com.gachawaifus.entity.PyroisEntity.createAttributes().build());
        event.put(ModEntities.NORMA.get(), com.gachawaifus.entity.NormaEntity.createAttributes().build());
        event.put(ModEntities.VELINA.get(), com.gachawaifus.entity.VelinaEntity.createAttributes().build());
        event.put(ModEntities.SIGRID.get(), com.gachawaifus.entity.SigridEntity.createAttributes().build());
        event.put(ModEntities.CLARET.get(), com.gachawaifus.entity.ClaretEntity.createAttributes().build());
        event.put(ModEntities.ROXY.get(), com.gachawaifus.entity.RoxyEntity.createAttributes().build());
        event.put(ModEntities.ORPHIE.get(), com.gachawaifus.entity.OrphieEntity.createAttributes().build());
        event.put(ModEntities.SEED.get(), com.gachawaifus.entity.SeedEntity.createAttributes().build());
        event.put(ModEntities.MANATO.get(), com.gachawaifus.entity.ManatoEntity.createAttributes().build());
        event.put(ModEntities.LUCIA.get(), com.gachawaifus.entity.LuciaEntity.createAttributes().build());
        event.put(ModEntities.BANYUE.get(), com.gachawaifus.entity.BanyueEntity.createAttributes().build());
        event.put(ModEntities.ZHAO.get(), com.gachawaifus.entity.ZhaoEntity.createAttributes().build());
        event.put(ModEntities.ARIA.get(), com.gachawaifus.entity.AriaEntity.createAttributes().build());
        event.put(ModEntities.SUNNA.get(), com.gachawaifus.entity.SunnaEntity.createAttributes().build());
        event.put(ModEntities.CISSIA.get(), com.gachawaifus.entity.CissiaEntity.createAttributes().build());
        event.put(ModEntities.NANGONG_YU.get(), com.gachawaifus.entity.NangongYuEntity.createAttributes().build());
        event.put(ModEntities.TSUKISHIRO_YANAGI.get(), com.gachawaifus.entity.TsukishiroYanagiEntity.createAttributes().build());
        event.put(ModEntities.HARUMASA.get(), com.gachawaifus.entity.HarumasaEntity.createAttributes().build());
        event.put(ModEntities.EVELYN_CHEVALIER.get(), com.gachawaifus.entity.EvelynChevalierEntity.createAttributes().build());
        event.put(ModEntities.PULCHRA.get(), com.gachawaifus.entity.PulchraEntity.createAttributes().build());
        event.put(ModEntities.TRIGGER.get(), com.gachawaifus.entity.TriggerEntity.createAttributes().build());
        event.put(ModEntities.HUGO_VLAD.get(), com.gachawaifus.entity.HugoVladEntity.createAttributes().build());
        event.put(ModEntities.VIVIAN.get(), com.gachawaifus.entity.VivianEntity.createAttributes().build());
        event.put(ModEntities.JU_FUFU.get(), com.gachawaifus.entity.JuFufuEntity.createAttributes().build());
        event.put(ModEntities.PAN_YINHU.get(), com.gachawaifus.entity.PanYinhuEntity.createAttributes().build());
        event.put(ModEntities.ALICE.get(), com.gachawaifus.entity.AliceEntity.createAttributes().build());
        event.put(ModEntities.ANTON.get(), com.gachawaifus.entity.AntonEntity.createAttributes().build());
        event.put(ModEntities.BEN_BIGGER.get(), com.gachawaifus.entity.BenBiggerEntity.createAttributes().build());
        event.put(ModEntities.CORIN_WICKES.get(), com.gachawaifus.entity.CorinWickesEntity.createAttributes().build());
        event.put(ModEntities.LUCY.get(), com.gachawaifus.entity.LucyEntity.createAttributes().build());
        event.put(ModEntities.PIPER_WHEEL.get(), com.gachawaifus.entity.PiperWheelEntity.createAttributes().build());
        event.put(ModEntities.SOUKAKU.get(), com.gachawaifus.entity.SoukakuEntity.createAttributes().build());
        event.put(ModEntities.SETH_LOWELL.get(), com.gachawaifus.entity.SethLowellEntity.createAttributes().build());
        event.put(ModEntities.JANE_DOE.get(), com.gachawaifus.entity.JaneDoeEntity.createAttributes().build());
        event.put(ModEntities.CAESAR.get(), com.gachawaifus.entity.CaesarEntity.createAttributes().build());
        event.put(ModEntities.LIGHTER.get(), com.gachawaifus.entity.LighterEntity.createAttributes().build());
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

        // Jefe — Doctor House (y su minion Wilson)
        event.registerEntityRenderer(ModEntities.DOCTOR_HOUSE.get(), DoctorHouseRenderer::new);
        event.registerEntityRenderer(ModEntities.WILSON.get(), WilsonRenderer::new);

        // Jefe — Fernanfloo (con su chorizo GeckoLib en la mano)
        event.registerEntityRenderer(ModEntities.FERNANFLOO.get(), FernanflooRenderer::new);

        // Rina
        event.registerEntityRenderer(ModEntities.RINA_ENTITY.get(), RinaRenderer::new);

        // ★★★★★ Koleda Belobog — modelo de jugador slim (sin GeckoLib: no tiene geo ni animaciones)
        event.registerEntityRenderer(ModEntities.KOLEDA.get(), KoledaRenderer::new);

        /** Grace Howard — "Anomaly" (Electric / Support-Anomaly) */
        event.registerEntityRenderer(ModEntities.GRACE_HOWARD.get(), com.gachawaifus.entity.client.GraceHowardRenderer::new);

        /** Soldier 11 — Attack */
        event.registerEntityRenderer(ModEntities.SOLDIER_11.get(), Soldier11Renderer::new);

        // ★★★★★ Tobichi Origami — "Defense" (White / Defense)
        event.registerEntityRenderer(ModEntities.TOBICHI_ORIGAMI.get(), com.gachawaifus.entity.client.TobichiOrigamiRenderer::new);

        // ★★★★★ Nekomiya Mana (Nekomata) — "Cunning Hares" (Physical / Attack)
        event.registerEntityRenderer(ModEntities.NEKOMATA.get(), NekomataRenderer::new);
        event.registerEntityRenderer(ModEntities.BILLY_KID.get(), BillyKidRenderer::new);
        event.registerEntityRenderer(ModEntities.YIXUAN.get(), YixuanRenderer::new);
        event.registerEntityRenderer(ModEntities.QINGYI.get(), QingyiRenderer::new);
        event.registerEntityRenderer(ModEntities.PYROIS.get(), com.gachawaifus.entity.client.PyroisRenderer::new);
        event.registerEntityRenderer(ModEntities.NORMA.get(), com.gachawaifus.entity.client.NormaRenderer::new);
        event.registerEntityRenderer(ModEntities.VELINA.get(), com.gachawaifus.entity.client.VelinaRenderer::new);
        event.registerEntityRenderer(ModEntities.SIGRID.get(), com.gachawaifus.entity.client.SigridRenderer::new);
        event.registerEntityRenderer(ModEntities.CLARET.get(), com.gachawaifus.entity.client.ClaretRenderer::new);
        event.registerEntityRenderer(ModEntities.ROXY.get(), com.gachawaifus.entity.client.RoxyRenderer::new);
        event.registerEntityRenderer(ModEntities.ORPHIE.get(), com.gachawaifus.entity.client.OrphieRenderer::new);
        event.registerEntityRenderer(ModEntities.SEED.get(), com.gachawaifus.entity.client.SeedRenderer::new);
        event.registerEntityRenderer(ModEntities.MANATO.get(), com.gachawaifus.entity.client.ManatoRenderer::new);
        event.registerEntityRenderer(ModEntities.LUCIA.get(), com.gachawaifus.entity.client.LuciaRenderer::new);
        event.registerEntityRenderer(ModEntities.BANYUE.get(), com.gachawaifus.entity.client.BanyueRenderer::new);
        event.registerEntityRenderer(ModEntities.ZHAO.get(), com.gachawaifus.entity.client.ZhaoRenderer::new);
        event.registerEntityRenderer(ModEntities.ARIA.get(), com.gachawaifus.entity.client.AriaRenderer::new);
        event.registerEntityRenderer(ModEntities.SUNNA.get(), com.gachawaifus.entity.client.SunnaRenderer::new);
        event.registerEntityRenderer(ModEntities.CISSIA.get(), com.gachawaifus.entity.client.CissiaRenderer::new);
        event.registerEntityRenderer(ModEntities.NANGONG_YU.get(), com.gachawaifus.entity.client.NangongYuRenderer::new);
        event.registerEntityRenderer(ModEntities.TSUKISHIRO_YANAGI.get(), com.gachawaifus.entity.client.TsukishiroYanagiRenderer::new);
        event.registerEntityRenderer(ModEntities.HARUMASA.get(), com.gachawaifus.entity.client.HarumasaRenderer::new);
        event.registerEntityRenderer(ModEntities.EVELYN_CHEVALIER.get(), com.gachawaifus.entity.client.EvelynChevalierRenderer::new);
        event.registerEntityRenderer(ModEntities.PULCHRA.get(), com.gachawaifus.entity.client.PulchraRenderer::new);
        event.registerEntityRenderer(ModEntities.TRIGGER.get(), com.gachawaifus.entity.client.TriggerRenderer::new);
        event.registerEntityRenderer(ModEntities.HUGO_VLAD.get(), com.gachawaifus.entity.client.HugoVladRenderer::new);
        event.registerEntityRenderer(ModEntities.VIVIAN.get(), com.gachawaifus.entity.client.VivianRenderer::new);
        event.registerEntityRenderer(ModEntities.JU_FUFU.get(), com.gachawaifus.entity.client.JuFufuRenderer::new);
        event.registerEntityRenderer(ModEntities.PAN_YINHU.get(), com.gachawaifus.entity.client.PanYinhuRenderer::new);
        event.registerEntityRenderer(ModEntities.ALICE.get(), com.gachawaifus.entity.client.AliceRenderer::new);
        event.registerEntityRenderer(ModEntities.ANTON.get(), com.gachawaifus.entity.client.AntonRenderer::new);
        event.registerEntityRenderer(ModEntities.BEN_BIGGER.get(), com.gachawaifus.entity.client.BenBiggerRenderer::new);
        event.registerEntityRenderer(ModEntities.CORIN_WICKES.get(), com.gachawaifus.entity.client.CorinWickesRenderer::new);
        event.registerEntityRenderer(ModEntities.LUCY.get(), com.gachawaifus.entity.client.LucyRenderer::new);
        event.registerEntityRenderer(ModEntities.PIPER_WHEEL.get(), com.gachawaifus.entity.client.PiperWheelRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUKAKU.get(), com.gachawaifus.entity.client.SoukakuRenderer::new);
        event.registerEntityRenderer(ModEntities.SETH_LOWELL.get(), com.gachawaifus.entity.client.SethLowellRenderer::new);
        event.registerEntityRenderer(ModEntities.JANE_DOE.get(), com.gachawaifus.entity.client.JaneDoeRenderer::new);
        event.registerEntityRenderer(ModEntities.CAESAR.get(), com.gachawaifus.entity.client.CaesarRenderer::new);
        event.registerEntityRenderer(ModEntities.LIGHTER.get(), com.gachawaifus.entity.client.LighterRenderer::new);
        event.registerEntityRenderer(ModEntities.ZHU_YUAN.get(), ZhuYuanRenderer::new);
    }
}