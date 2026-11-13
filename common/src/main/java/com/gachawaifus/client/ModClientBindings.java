package com.gachawaifus.client;

import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.entity.client.AliceRenderer;
import com.gachawaifus.entity.client.AnbyDemaraRenderer;
import com.gachawaifus.entity.client.AntonRenderer;
import com.gachawaifus.entity.client.AquaRenderer;
import com.gachawaifus.entity.client.AriaRenderer;
import com.gachawaifus.entity.client.AstraYaoRenderer;
import com.gachawaifus.entity.client.BanyueRenderer;
import com.gachawaifus.entity.client.BenBiggerRenderer;
import com.gachawaifus.entity.client.BillyKidRenderer;
import com.gachawaifus.entity.client.BurniceWhiteRenderer;
import com.gachawaifus.entity.client.CaesarRenderer;
import com.gachawaifus.entity.client.CissiaRenderer;
import com.gachawaifus.entity.client.ClaretRenderer;
import com.gachawaifus.entity.client.CorinWickesRenderer;
import com.gachawaifus.entity.client.CurlyRenderer;
import com.gachawaifus.entity.client.DarknessRenderer;
import com.gachawaifus.entity.client.DoctorHouseRenderer;
import com.gachawaifus.entity.client.EllenJoeRenderer;
import com.gachawaifus.entity.client.EtherBlastRenderer;
import com.gachawaifus.entity.client.EvelynChevalierRenderer;
import com.gachawaifus.entity.client.FernanflooRenderer;
import com.gachawaifus.entity.client.GraceHowardRenderer;
import com.gachawaifus.entity.client.HarumasaRenderer;
import com.gachawaifus.entity.client.HatsuneMikuRenderer;
import com.gachawaifus.entity.client.KasaneTetoRenderer;
import com.gachawaifus.entity.client.AkitaNeruRenderer;
import com.gachawaifus.entity.client.BelleRenderer;
import com.gachawaifus.entity.client.HugoVladRenderer;
import com.gachawaifus.entity.client.JaneDoeModel;
import com.gachawaifus.entity.client.JaneDoeRenderer;
import com.gachawaifus.entity.client.JuFufuRenderer;
import com.gachawaifus.entity.client.KoledaRenderer;
import com.gachawaifus.entity.client.LighterRenderer;
import com.gachawaifus.entity.client.LuciaRenderer;
import com.gachawaifus.entity.client.LucyRenderer;
import com.gachawaifus.entity.client.ManatoRenderer;
import com.gachawaifus.entity.client.MeguminRenderer;
import com.gachawaifus.entity.client.MiyabiMisiramaRenderer;
import com.gachawaifus.entity.client.MusketShotRenderer;
import com.gachawaifus.entity.client.NangongYuRenderer;
import com.gachawaifus.entity.client.NekomataRenderer;
import com.gachawaifus.entity.client.NicoleDemaraRenderer;
import com.gachawaifus.entity.client.NormaRenderer;
import com.gachawaifus.entity.client.OrphieRenderer;
import com.gachawaifus.entity.client.PanYinhuRenderer;
import com.gachawaifus.entity.client.PhoenixRenderer;
import com.gachawaifus.entity.client.PiperWheelRenderer;
import com.gachawaifus.entity.client.PromeiaRenderer;
import com.gachawaifus.entity.client.PulchraRenderer;
import com.gachawaifus.entity.client.PyroisRenderer;
import com.gachawaifus.entity.client.QingyiRenderer;
import com.gachawaifus.entity.client.RemielleModel;
import com.gachawaifus.entity.client.RemielleRenderer;
import com.gachawaifus.entity.client.RicardoMilosRenderer;
import com.gachawaifus.entity.client.RinaRenderer;
import com.gachawaifus.entity.client.RoxyRenderer;
import com.gachawaifus.entity.client.SeedRenderer;
import com.gachawaifus.entity.client.SethLowellRenderer;
import com.gachawaifus.entity.client.SigridRenderer;
import com.gachawaifus.entity.client.Soldier11Renderer;
import com.gachawaifus.entity.client.SoukakuRenderer;
import com.gachawaifus.entity.client.SunnaRenderer;
import com.gachawaifus.entity.client.TobichiOrigamiRenderer;
import com.gachawaifus.entity.client.TokisakiKurumiModel;
import com.gachawaifus.entity.client.TokisakiKurumiRenderer;
import com.gachawaifus.entity.client.TriggerRenderer;
import com.gachawaifus.entity.client.TsukishiroYanagiRenderer;
import com.gachawaifus.entity.client.TungTungSahurRenderer;
import com.gachawaifus.entity.client.UkinamiYuzuhaRenderer;
import com.gachawaifus.entity.client.VelinaRenderer;
import com.gachawaifus.entity.client.VivianRenderer;
import com.gachawaifus.entity.client.VonLycaonRenderer;
import com.gachawaifus.entity.client.WilsonRenderer;
import com.gachawaifus.entity.client.YatogamiTohkaRenderer;
import com.gachawaifus.entity.client.YeShunguangRenderer;
import com.gachawaifus.entity.client.YidhariRenderer;
import com.gachawaifus.entity.client.YixuanRenderer;
import com.gachawaifus.entity.client.ZhaoRenderer;
import com.gachawaifus.entity.client.ZhuYuanRenderer;
import com.gachawaifus.registry.ModBlockEntities;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModMenuTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Registro de cliente igual en los dos cargadores. NeoForge recorre estas listas dentro de sus
 * eventos (EntityRenderersEvent, RegisterMenuScreensEvent) y Fabric dentro de sus registries
 * (EntityModelLayerRegistry, EntityRendererRegistry, BlockEntityRendererRegistry, MenuScreens).
 *
 * <p>Vive en common/ para que añadir una waifu sea una línea por tabla en vez de cuatro bloques
 * duplicados que se desincronizan: hasta la 7.3.0 Fabric no registraba ni un renderer y todas las
 * entidades se veían como cajas vacías. El orden es el del registro histórico de NeoForge.
 */
public final class ModClientBindings {

    /** Capa de modelado y su malla. Son capas propias: no se toca la capa compartida de jugador. */
    public record Layer(ModelLayerLocation location, Supplier<LayerDefinition> definition) {}

    /** Tipo de entidad y cómo se pinta. */
    public record Renderer(EntityType<? extends Entity> type,
                           EntityRendererProvider<? extends Entity> provider) {}

    /** Tipo de menú y su pantalla. El segundo tipo va con comodín: la cota real de vanilla es una
     *  intersección (Screen &amp; MenuAccess) y repetirla en cada registro no aportaría nada. */
    public record Screen(MenuType<? extends AbstractContainerMenu> type,
                         MenuScreens.ScreenConstructor<?, ?> screen) {}

    /** Tipo de block entity y cómo se pinta. */
    public record BlockRenderer(BlockEntityType<? extends BlockEntity> type,
                                BlockEntityRendererProvider<? extends BlockEntity> provider) {}

    public static final List<Layer> LAYERS = new ArrayList<>();
    public static final List<Renderer> RENDERERS = new ArrayList<>();
    public static final List<Screen> SCREENS = new ArrayList<>();
    public static final List<BlockRenderer> BLOCK_RENDERERS = new ArrayList<>();

    private ModClientBindings() {}

    private static void add(List<Layer> list, ModelLayerLocation location, Supplier<LayerDefinition> definition) {
        list.add(new Layer(location, definition));
    }

    private static <T extends Entity> void add(List<Renderer> list, EntityType<? extends T> type,
                                               EntityRendererProvider<T> provider) {
        list.add(new Renderer(type, provider));
    }

    // La cota del segundo parámetro de ScreenConstructor es la intersección de vanilla: sin
    // MenuAccess el compilador no puede deducir el descriptor desde la referencia de constructor.
    private static <T extends AbstractContainerMenu,
            S extends net.minecraft.client.gui.screens.Screen & net.minecraft.client.gui.screens.inventory.MenuAccess<T>>
    void add(List<Screen> list, MenuType<T> type, MenuScreens.ScreenConstructor<T, S> screen) {
        list.add(new Screen(type, screen));
    }

    private static <T extends BlockEntity> void add(List<BlockRenderer> list, BlockEntityType<T> type,
                                                    BlockEntityRendererProvider<? super T> provider) {
        list.add(new BlockRenderer(type, provider));
    }

    static {
        // El modelo de jugador con la pieza del pecho: tres variantes de rombo, más las tres
        // waifus que no usan GeckoLib y tienen capa de vanilla propia. La cuarta entrada es la malla
        // slim sin pieza pero en lienzo de 128 (las planas necesitan sitio para sus accesorios).
        add(LAYERS, WaifuBustLayers.TUBE, WaifuBustLayers::create);
        add(LAYERS, WaifuBustLayers.TUBE_BIG, WaifuBustLayers::createBig);
        add(LAYERS, WaifuBustLayers.SLIM_128, WaifuBustLayers::createSlim128);
        add(LAYERS, WaifuBustLayers.PROMEIA, WaifuBustLayers::createPromeia);
        add(LAYERS, WaifuBustLayers.MEGUMIN, WaifuBustLayers::createMegumin);
        add(LAYERS, RemielleModel.LAYER_LOCATION, RemielleModel::createBodyLayer);
        add(LAYERS, JaneDoeModel.LAYER_LOCATION, JaneDoeModel::createBodyLayer);

        // El único renderer de bloque: el Bloque de Hogar se pinta imitando otro bloque.
        add(BLOCK_RENDERERS, ModBlockEntities.WAIFU_HOME.get(), WaifuHomeBlockRenderer::new);

        add(SCREENS, ModMenuTypes.WAIFU_STORAGE.get(), WaifuStorageScreen::new);
        add(SCREENS, ModMenuTypes.GACHA_BANNER.get(), GachaBannerScreen::new);
        add(SCREENS, ModMenuTypes.WAIFU_GUIDE.get(), GuideScreen::new);
        add(SCREENS, ModMenuTypes.WAIFU_MODE.get(), WaifuModeScreen::new);

        add(RENDERERS, ModEntities.MIYABI.get(), MiyabiMisiramaRenderer::new);
        add(RENDERERS, ModEntities.ASTRA_YAO.get(), AstraYaoRenderer::new);
        add(RENDERERS, ModEntities.ELLEN_JOE.get(), EllenJoeRenderer::new);
        add(RENDERERS, ModEntities.YE_SHUNGUANG.get(), YeShunguangRenderer::new);
        add(RENDERERS, ModEntities.NICOLE_DEMARA.get(), NicoleDemaraRenderer::new);
        add(RENDERERS, ModEntities.ANBY_DEMARA.get(), AnbyDemaraRenderer::new);
        add(RENDERERS, ModEntities.BURNICE_WHITE.get(), BurniceWhiteRenderer::new);
        add(RENDERERS, ModEntities.ETHER_BLAST.get(), EtherBlastRenderer::new);
        add(RENDERERS, ModEntities.MUSKET_SHOT.get(), MusketShotRenderer::new);
        add(RENDERERS, ModEntities.UKINAMI_YUZUHA.get(), UkinamiYuzuhaRenderer::new);
        add(RENDERERS, ModEntities.PROMEIA.get(), PromeiaRenderer::new);
        add(RENDERERS, ModEntities.REMIELLE.get(), RemielleRenderer::new);
        add(RENDERERS, ModEntities.TOKISAKI_KURUMI.get(), TokisakiKurumiRenderer::new);
        add(RENDERERS, ModEntities.YIDHARI.get(), YidhariRenderer::new);
        add(RENDERERS, ModEntities.VON_LYCAON.get(), VonLycaonRenderer::new);
        add(RENDERERS, ModEntities.RICARDO_MILOS.get(), RicardoMilosRenderer::new);
        add(RENDERERS, ModEntities.DOCTOR_HOUSE.get(), DoctorHouseRenderer::new);
        add(RENDERERS, ModEntities.WILSON.get(), WilsonRenderer::new);
        add(RENDERERS, ModEntities.FERNANFLOO.get(), FernanflooRenderer::new);
        add(RENDERERS, ModEntities.CURLY.get(), CurlyRenderer::new);
        add(RENDERERS, ModEntities.TUNG_TUNG_SAHUR.get(), TungTungSahurRenderer::new);
        add(RENDERERS, ModEntities.RINA_ENTITY.get(), RinaRenderer::new);
        add(RENDERERS, ModEntities.KOLEDA.get(), KoledaRenderer::new);
        add(RENDERERS, ModEntities.GRACE_HOWARD.get(), GraceHowardRenderer::new);
        add(RENDERERS, ModEntities.SOLDIER_11.get(), Soldier11Renderer::new);
        add(RENDERERS, ModEntities.TOBICHI_ORIGAMI.get(), TobichiOrigamiRenderer::new);
        add(RENDERERS, ModEntities.NEKOMATA.get(), NekomataRenderer::new);
        add(RENDERERS, ModEntities.BILLY_KID.get(), BillyKidRenderer::new);
        add(RENDERERS, ModEntities.YIXUAN.get(), YixuanRenderer::new);
        add(RENDERERS, ModEntities.QINGYI.get(), QingyiRenderer::new);
        add(RENDERERS, ModEntities.PYROIS.get(), PyroisRenderer::new);
        add(RENDERERS, ModEntities.NORMA.get(), NormaRenderer::new);
        add(RENDERERS, ModEntities.VELINA.get(), VelinaRenderer::new);
        add(RENDERERS, ModEntities.SIGRID.get(), SigridRenderer::new);
        add(RENDERERS, ModEntities.CLARET.get(), ClaretRenderer::new);
        add(RENDERERS, ModEntities.ROXY.get(), RoxyRenderer::new);
        add(RENDERERS, ModEntities.ORPHIE.get(), OrphieRenderer::new);
        add(RENDERERS, ModEntities.SEED.get(), SeedRenderer::new);
        add(RENDERERS, ModEntities.MANATO.get(), ManatoRenderer::new);
        add(RENDERERS, ModEntities.LUCIA.get(), LuciaRenderer::new);
        add(RENDERERS, ModEntities.BANYUE.get(), BanyueRenderer::new);
        add(RENDERERS, ModEntities.ZHAO.get(), ZhaoRenderer::new);
        add(RENDERERS, ModEntities.ARIA.get(), AriaRenderer::new);
        add(RENDERERS, ModEntities.SUNNA.get(), SunnaRenderer::new);
        add(RENDERERS, ModEntities.CISSIA.get(), CissiaRenderer::new);
        add(RENDERERS, ModEntities.NANGONG_YU.get(), NangongYuRenderer::new);
        add(RENDERERS, ModEntities.TSUKISHIRO_YANAGI.get(), TsukishiroYanagiRenderer::new);
        add(RENDERERS, ModEntities.HARUMASA.get(), HarumasaRenderer::new);
        add(RENDERERS, ModEntities.EVELYN_CHEVALIER.get(), EvelynChevalierRenderer::new);
        add(RENDERERS, ModEntities.PULCHRA.get(), PulchraRenderer::new);
        add(RENDERERS, ModEntities.TRIGGER.get(), TriggerRenderer::new);
        add(RENDERERS, ModEntities.HUGO_VLAD.get(), HugoVladRenderer::new);
        add(RENDERERS, ModEntities.VIVIAN.get(), VivianRenderer::new);
        add(RENDERERS, ModEntities.JU_FUFU.get(), JuFufuRenderer::new);
        add(RENDERERS, ModEntities.PAN_YINHU.get(), PanYinhuRenderer::new);
        add(RENDERERS, ModEntities.ALICE.get(), AliceRenderer::new);
        add(RENDERERS, ModEntities.ANTON.get(), AntonRenderer::new);
        add(RENDERERS, ModEntities.BEN_BIGGER.get(), BenBiggerRenderer::new);
        add(RENDERERS, ModEntities.CORIN_WICKES.get(), CorinWickesRenderer::new);
        add(RENDERERS, ModEntities.LUCY.get(), LucyRenderer::new);
        add(RENDERERS, ModEntities.PIPER_WHEEL.get(), PiperWheelRenderer::new);
        add(RENDERERS, ModEntities.SOUKAKU.get(), SoukakuRenderer::new);
        add(RENDERERS, ModEntities.SETH_LOWELL.get(), SethLowellRenderer::new);
        add(RENDERERS, ModEntities.JANE_DOE.get(), JaneDoeRenderer::new);
        add(RENDERERS, ModEntities.CAESAR.get(), CaesarRenderer::new);
        add(RENDERERS, ModEntities.LIGHTER.get(), LighterRenderer::new);
        add(RENDERERS, ModEntities.ZHU_YUAN.get(), ZhuYuanRenderer::new);
        add(RENDERERS, ModEntities.AQUA.get(), AquaRenderer::new);
        add(RENDERERS, ModEntities.MEGUMIN.get(), MeguminRenderer::new);
        add(RENDERERS, ModEntities.DARKNESS.get(), DarknessRenderer::new);
        add(RENDERERS, ModEntities.YATOGAMI_TOHKA.get(), YatogamiTohkaRenderer::new);
        add(RENDERERS, ModEntities.PHOENIX.get(), PhoenixRenderer::new);
        add(RENDERERS, ModEntities.HATSUNE_MIKU.get(), HatsuneMikuRenderer::new);
        add(RENDERERS, ModEntities.KASANE_TETO.get(), KasaneTetoRenderer::new);
        add(RENDERERS, ModEntities.AKITA_NERU.get(), AkitaNeruRenderer::new);
        add(RENDERERS, ModEntities.BELLE.get(), BelleRenderer::new);
    }
}
