package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.AstraYaoTokenItem;
import com.gachawaifus.item.ZhuYuanTokenItem;
import com.gachawaifus.item.BurniceWhiteTokenItem;
import com.gachawaifus.item.EllenJoeTokenItem;
import com.gachawaifus.item.GachaBallItem;
import com.gachawaifus.item.GachaTerminalItem;
import com.gachawaifus.item.BateBeisbolItem;
import com.gachawaifus.item.MiyabiMisiramaTokenItem;
import com.gachawaifus.item.NicoleDemaraTokenItem;
import com.gachawaifus.item.YeShunguangTokenItem;
import com.gachawaifus.item.AnbyDemaraTokenItem;
import com.gachawaifus.item.UkinamiYuzuhaTokenItem;
import com.gachawaifus.item.PromeiaTokenItem;
import com.gachawaifus.item.WaifuCapsuleItem;
import com.gachawaifus.item.WaifuBeaconItem;
import com.gachawaifus.item.WaifuGuideItem;
import com.gachawaifus.item.RemielleTokenItem;
import com.gachawaifus.item.TokisakiKurumiTokenItem;
import com.gachawaifus.item.VonLycaonTokenItem;
import com.gachawaifus.item.YidhariTokenItem;
import com.gachawaifus.item.KoledaTokenItem;
import com.gachawaifus.item.Soldier11TokenItem;
import com.gachawaifus.item.RinaTokenItem;
import com.gachawaifus.item.BillyKidTokenItem;
import com.gachawaifus.item.NekomataTokenItem;
import com.gachawaifus.item.QingyiTokenItem;
import com.gachawaifus.item.YixuanTokenItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {
        private static <T extends Item> Supplier<T> register(String name, Supplier<T> factory) {
        T item = factory.get();
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name), item);
        return new RegistrySupplier<>(item);
    }

    public static void init() {}

    public static final Supplier<Item> ASTRA_YAO_TOKEN =
            register("astra_yao_token", () -> new AstraYaoTokenItem(new Item.Properties().stacksTo(16)));

    public static final Supplier<Item> ELLEN_JOE_TOKEN =
            register("ellen_joe_token", () -> new EllenJoeTokenItem(new Item.Properties().stacksTo(16)));

    public static final Supplier<Item> KOLEDA_TOKEN =
            register("koleda_token", () -> new KoledaTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Hoshimi Miyabi — Token de invocación (sumona a la Dama de la Luz) */
    public static final Supplier<Item> MIYABI_TOKEN =
            register("miyabi_token", () -> new MiyabiMisiramaTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Burnice White — Token de invocación */
    public static final Supplier<Item> BURNICE_WHITE_TOKEN =
            register("burnice_white_token", () -> new BurniceWhiteTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Ye Shunguang (Yixuan) — Token de invocación */
    public static final Supplier<Item> YE_SHUNGUANG_TOKEN =
            register("ye_shunguang_token", () -> new YeShunguangTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Soldier 11 — Token de invocación (Attack / Fire) */
    public static final Supplier<Item> SOLDIER_11_TOKEN =
            register("soldier_11_token", () -> new Soldier11TokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control) */
    public static final Supplier<Item> NICOLE_DEMARA_TOKEN =
            register("nicole_demara_token", () -> new NicoleDemaraTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant) */
    public static final Supplier<Item> ANBY_DEMARA_TOKEN =
            register("anby_demara_token", () -> new AnbyDemaraTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte" */
    public static final Supplier<Item> UKINAMI_YUZUHA_TOKEN =
            register("ukinami_yuzuha_token", () -> new UkinamiYuzuhaTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Promeia — Token de invocación */
    public static final Supplier<Item> PROMEIA_TOKEN =
            register("promeia_token", () -> new PromeiaTokenItem(new Item.Properties().stacksTo(1)));

    /** Terminal Gacha — abre la pantalla del gacha (pestañas promocional y permanente) */
    public static final Supplier<Item> GACHA_TERMINAL =
            register("gacha_terminal", () -> new GachaTerminalItem(new Item.Properties().stacksTo(1)));

    /**
     * Bolita Rosa — una tirada del Gacha.
     *
     * <p>Se craftea en cruz: un diamante en el centro y cobre, lapislázuli, hierro y carbón en las
     * cuatro casillas de al lado. Además el sistema de diarias regala una por día.
     */
    public static final Supplier<GachaBallItem> PINK_BALL =
            register("pink_ball", () -> new GachaBallItem(new Item.Properties().stacksTo(25)));

    /** Bolita Azul — una tirada del banner PERMANENTE (pestaña permanente del Terminal Gacha). */
    public static final Supplier<GachaBallItem> BLUE_BALL =
            register("blue_ball", () -> new GachaBallItem(new Item.Properties()));

    /** Cápsula Waifu — cofre portátil: guarda (Shift+clic) e invoca a tus waifus. La colección vive en el mundo. */
    public static final Supplier<Item> WAIFU_CAPSULE =
            register("waifu_capsule", () -> new WaifuCapsuleItem(new Item.Properties().stacksTo(1)));

    /** Remielle — token de invocación */
    public static final Supplier<Item> REMIELLE_TOKEN =
            register("remielle_token", () -> new RemielleTokenItem(new Item.Properties().stacksTo(1)));

    /** Tokisaki Kurumi — token de invocación */
    public static final Supplier<Item> TOKISAKI_KURUMI_TOKEN =
            register("tokisaki_kurumi_token", () -> new TokisakiKurumiTokenItem(new Item.Properties().stacksTo(1)));

    /** Yidhari Murphy — token de invocación */
    public static final Supplier<Item> YIDHARI_TOKEN =
            register("yidhari_token", () -> new YidhariTokenItem(new Item.Properties().stacksTo(1)));

    /**
     * Huevo de invocación de Ricardo Milos (el jefe neutral que suelta 10 tiradas).
     *
     * <p>Es la forma cómoda de probarlo: se puede colocar desde el menú creativo. Los colores del
     * huevo los toma el propio {@link net.minecraft.world.item.SpawnEggItem} (violeta oscuro con
     * brillo magenta), sin registrar nada a mano.
     */
    public static final Supplier<Item> RICARDO_MILOS_SPAWN_EGG =
            register("ricardo_milos_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.RICARDO_MILOS.get(), 0x2B1B45, 0xFF4BD8, new Item.Properties()));

    /**
     * Huevo de invocación de Doctor House (el jefe neutral que invoca Wilsons y suelta 5
     * Bolitas Azules). Colores: gabardina azul grisácea con carne.
     */
    public static final Supplier<Item> DOCTOR_HOUSE_SPAWN_EGG =
            register("doctor_house_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.DOCTOR_HOUSE.get(), 0x3B4A5A, 0x8C6E4A, new Item.Properties()));

    /** Huevo de invocación de Wilson (el médico amigo de House). */
    public static final Supplier<Item> WILSON_SPAWN_EGG =
            register("wilson_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.WILSON.get(), 0x6E4F35, 0xC8B89A, new Item.Properties()));

    /**
     * Huevo de invocación de Fernanfloo (el jefe neutral de los Curlys). Colores: gorra verde
     * con ropa oscura.
     */
    public static final Supplier<Item> FERNANFLOO_SPAWN_EGG =
            register("fernanfloo_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.FERNANFLOO.get(), 0x3AA33A, 0x2B2B2B, new Item.Properties()));

    /**
     * Bate de béisbol — el arma de Tung Tung Sahur (modelo GeckoLib).
     */
    public static final Supplier<Item> BATE_BEISBOL =
            register("beisbol_bate", () -> new BateBeisbolItem(new Item.Properties().stacksTo(1)));

    /** Huevo de invocación de Tung Tung Sahur (el jefe del bate). Colores: madera con corteza. */
    public static final Supplier<Item> TUNG_TUNG_SAHUR_SPAWN_EGG =
            register("tung_tung_sahur_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.TUNG_TUNG_SAHUR.get(), 0xCC7014, 0x3B2408, new Item.Properties()));

    /** Huevo de invocación de Curly (el perrito de Fernanfloo). */
    public static final Supplier<Item> CURLY_SPAWN_EGG =
            register("curly_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.CURLY.get(), 0xD8A030, 0xF5F0E6, new Item.Properties()));

    /** Von Lycaon — token de invocación */
    public static final Supplier<Item> VON_LYCAON_TOKEN =
            register("von_lycaon_token", () -> new VonLycaonTokenItem(new Item.Properties().stacksTo(1)));

    /** Rina — token de invocación */
    public static final Supplier<Item> RINA_TOKEN =
            register("rina_token", () -> new RinaTokenItem(new Item.Properties().stacksTo(1)));

    /** Grace Howard Token — Token de invocación para Grace Howard */
    public static final Supplier<Item> GRACE_HOWARD_TOKEN =
            register("grace_howard_token", () -> new com.gachawaifus.item.GraceHowardTokenItem(new Item.Properties().stacksTo(16)));

    /** Tobichi Origami Token — Token de invocación para Tobichi Origami */
    public static final Supplier<Item> TOBICHI_ORIGAMI_TOKEN =
            register("tobichi_origami_token", () -> new com.gachawaifus.item.TobichiOrigamiTokenItem(new Item.Properties().stacksTo(16)));

    /** Nekomiya Mana (Nekomata) — Token de invocación (Physical / Attack) */
    public static final Supplier<Item> NEKOMATA_TOKEN =
            register("nekomata_token", () -> new NekomataTokenItem(new Item.Properties().stacksTo(16)));

    /** Billy Kid — token de invocación */
    public static final Supplier<Item> BILLY_KID_TOKEN =
            register("billy_kid_token", () -> new BillyKidTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Yixuan — Token de invocación */
    public static final Supplier<Item> ZHU_YUAN_TOKEN =
            register("zhu_yuan_token", () -> new ZhuYuanTokenItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> QINGYI_TOKEN =
            register("qingyi_token", () -> new QingyiTokenItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> YIXUAN_TOKEN =
            register("yixuan_token", () -> new YixuanTokenItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> ANTON_TOKEN =
            register("anton_token", () -> new com.gachawaifus.item.AntonTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> BEN_BIGGER_TOKEN =
            register("ben_bigger_token", () -> new com.gachawaifus.item.BenBiggerTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> CORIN_WICKES_TOKEN =
            register("corin_wickes_token", () -> new com.gachawaifus.item.CorinWickesTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> LUCY_TOKEN =
            register("lucy_token", () -> new com.gachawaifus.item.LucyTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> PIPER_WHEEL_TOKEN =
            register("piper_wheel_token", () -> new com.gachawaifus.item.PiperWheelTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> SOUKAKU_TOKEN =
            register("soukaku_token", () -> new com.gachawaifus.item.SoukakuTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> SETH_LOWELL_TOKEN =
            register("seth_lowell_token", () -> new com.gachawaifus.item.SethLowellTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> JANE_DOE_TOKEN =
            register("jane_doe_token", () -> new com.gachawaifus.item.JaneDoeTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> CAESAR_TOKEN =
            register("caesar_token", () -> new com.gachawaifus.item.CaesarTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> LIGHTER_TOKEN =
            register("lighter_token", () -> new com.gachawaifus.item.LighterTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> TSUKISHIRO_YANAGI_TOKEN =
            register("tsukishiro_yanagi_token", () -> new com.gachawaifus.item.TsukishiroYanagiTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> HARUMASA_TOKEN =
            register("harumasa_token", () -> new com.gachawaifus.item.HarumasaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> EVELYN_CHEVALIER_TOKEN =
            register("evelyn_chevalier_token", () -> new com.gachawaifus.item.EvelynChevalierTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> PULCHRA_TOKEN =
            register("pulchra_token", () -> new com.gachawaifus.item.PulchraTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> TRIGGER_TOKEN =
            register("trigger_token", () -> new com.gachawaifus.item.TriggerTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> HUGO_VLAD_TOKEN =
            register("hugo_vlad_token", () -> new com.gachawaifus.item.HugoVladTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> VIVIAN_TOKEN =
            register("vivian_token", () -> new com.gachawaifus.item.VivianTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> JU_FUFU_TOKEN =
            register("ju_fufu_token", () -> new com.gachawaifus.item.JuFufuTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> PAN_YINHU_TOKEN =
            register("pan_yinhu_token", () -> new com.gachawaifus.item.PanYinhuTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ALICE_TOKEN =
            register("alice_token", () -> new com.gachawaifus.item.AliceTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ORPHIE_TOKEN =
            register("orphie_token", () -> new com.gachawaifus.item.OrphieTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> SEED_TOKEN =
            register("seed_token", () -> new com.gachawaifus.item.SeedTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MANATO_TOKEN =
            register("manato_token", () -> new com.gachawaifus.item.ManatoTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> LUCIA_TOKEN =
            register("lucia_token", () -> new com.gachawaifus.item.LuciaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> BANYUE_TOKEN =
            register("banyue_token", () -> new com.gachawaifus.item.BanyueTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ZHAO_TOKEN =
            register("zhao_token", () -> new com.gachawaifus.item.ZhaoTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ARIA_TOKEN =
            register("aria_token", () -> new com.gachawaifus.item.AriaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> SUNNA_TOKEN =
            register("sunna_token", () -> new com.gachawaifus.item.SunnaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> CISSIA_TOKEN =
            register("cissia_token", () -> new com.gachawaifus.item.CissiaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> NANGONG_YU_TOKEN =
            register("nangong_yu_token", () -> new com.gachawaifus.item.NangongYuTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> PYROIS_TOKEN =
            register("pyrois_token", () -> new com.gachawaifus.item.PyroisTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> NORMA_TOKEN =
            register("norma_token", () -> new com.gachawaifus.item.NormaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> VELINA_TOKEN =
            register("velina_token", () -> new com.gachawaifus.item.VelinaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> SIGRID_TOKEN =
            register("sigrid_token", () -> new com.gachawaifus.item.SigridTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> CLARET_TOKEN =
            register("claret_token", () -> new com.gachawaifus.item.ClaretTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> ROXY_TOKEN =
            register("roxy_token", () -> new com.gachawaifus.item.RoxyTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> AQUA_TOKEN =
            register("aqua_token", () -> new com.gachawaifus.item.AquaTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> MEGUMIN_TOKEN =
            register("megumin_token", () -> new com.gachawaifus.item.MeguminTokenItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<Item> DARKNESS_TOKEN =
            register("darkness_token", () -> new com.gachawaifus.item.DarknessTokenItem(new Item.Properties().stacksTo(1)));

    /** Baliza de Hogar: mando de recall y asignación de hogar de las waifus. */
    public static final Supplier<WaifuBeaconItem> WAIFU_BEACON =
            register("waifu_beacon", () -> new WaifuBeaconItem(new Item.Properties().stacksTo(1)));

    /** Guía del Invocador: manual del mod en pantalla de libro. */
    public static final Supplier<WaifuGuideItem> WAIFU_GUIDE =
            register("waifu_guide", () -> new WaifuGuideItem(new Item.Properties().stacksTo(1)));
}