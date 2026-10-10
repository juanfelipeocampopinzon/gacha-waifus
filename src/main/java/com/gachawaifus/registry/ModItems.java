package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.AstraYaoTokenItem;
import com.gachawaifus.item.ZhuYuanTokenItem;
import com.gachawaifus.item.BurniceWhiteTokenItem;
import com.gachawaifus.item.EllenJoeTokenItem;
import com.gachawaifus.item.GachaTerminalItem;
import com.gachawaifus.item.ChorizoItem;
import com.gachawaifus.item.MiyabiMisiramaTokenItem;
import com.gachawaifus.item.NicoleDemaraTokenItem;
import com.gachawaifus.item.YeShunguangTokenItem;
import com.gachawaifus.item.AnbyDemaraTokenItem;
import com.gachawaifus.item.UkinamiYuzuhaTokenItem;
import com.gachawaifus.item.PromeiaTokenItem;
import com.gachawaifus.item.WaifuCapsuleItem;
import com.gachawaifus.item.RemielleTokenItem;
import com.gachawaifus.item.TokisakiKurumiTokenItem;
import com.gachawaifus.item.VonLycaonTokenItem;
import com.gachawaifus.item.YidhariTokenItem;
import com.gachawaifus.item.KoledaTokenItem;
import com.gachawaifus.item.Soldier11TokenItem;
import com.gachawaifus.item.StandardTerminalItem;
import com.gachawaifus.item.RinaTokenItem;
import com.gachawaifus.item.BillyKidTokenItem;
import com.gachawaifus.item.NekomataTokenItem;
import com.gachawaifus.item.QingyiTokenItem;
import com.gachawaifus.item.YixuanTokenItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, GachaWaifusMod.MODID);

    public static final DeferredHolder<Item, Item> ASTRA_YAO_TOKEN =
            ITEMS.register("astra_yao_token", () -> new AstraYaoTokenItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> ELLEN_JOE_TOKEN =
            ITEMS.register("ellen_joe_token", () -> new EllenJoeTokenItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> KOLEDA_TOKEN =
            ITEMS.register("koleda_token", () -> new KoledaTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Hoshimi Miyabi — Token de invocación (sumona a la Dama de la Luz) */
    public static final DeferredHolder<Item, Item> MIYABI_TOKEN =
            ITEMS.register("miyabi_token", () -> new MiyabiMisiramaTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Burnice White — Token de invocación */
    public static final DeferredHolder<Item, Item> BURNICE_WHITE_TOKEN =
            ITEMS.register("burnice_white_token", () -> new BurniceWhiteTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Ye Shunguang (Yixuan) — Token de invocación */
    public static final DeferredHolder<Item, Item> YE_SHUNGUANG_TOKEN =
            ITEMS.register("ye_shunguang_token", () -> new YeShunguangTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Soldier 11 — Token de invocación (Attack / Fire) */
    public static final DeferredHolder<Item, Item> SOLDIER_11_TOKEN =
            ITEMS.register("soldier_11_token", () -> new Soldier11TokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control) */
    public static final DeferredHolder<Item, Item> NICOLE_DEMARA_TOKEN =
            ITEMS.register("nicole_demara_token", () -> new NicoleDemaraTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant) */
    public static final DeferredHolder<Item, Item> ANBY_DEMARA_TOKEN =
            ITEMS.register("anby_demara_token", () -> new AnbyDemaraTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte" */
    public static final DeferredHolder<Item, Item> UKINAMI_YUZUHA_TOKEN =
            ITEMS.register("ukinami_yuzuha_token", () -> new UkinamiYuzuhaTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Promeia — Token de invocación */
    public static final DeferredHolder<Item, Item> PROMEIA_TOKEN =
            ITEMS.register("promeia_token", () -> new PromeiaTokenItem(new Item.Properties().stacksTo(1)));

    /** Terminal Gacha — máquina de tiradas con bolitas rojas (1 bolita = 1 tirada, Shift = x10) */
    public static final DeferredHolder<Item, Item> GACHA_TERMINAL =
            ITEMS.register("gacha_terminal", () -> new GachaTerminalItem(new Item.Properties().stacksTo(1)));

    /**
     * Bolita Rosa — una tirada del Gacha.
     *
     * <p>Se craftea en cruz: un diamante en el centro y cobre, lapislázuli, hierro y carbón en las
     * cuatro casillas de al lado. Además el sistema de diarias regala una por día.
     */
    public static final DeferredHolder<Item, Item> PINK_BALL =
            ITEMS.register("pink_ball", () -> new Item(new Item.Properties().stacksTo(25)));

    /** Bolita Azul — una tirada del banner PERMANENTE (Terminal Estándar). */
    public static final DeferredHolder<Item, Item> BLUE_BALL =
            ITEMS.register("blue_ball", () -> new Item(new Item.Properties()));

    /**
     * Terminal Estándar — banner permanente: gasta bolitas azules y solo entrega las 7 waifus
     * estándar ({@link com.gachawaifus.gacha.WaifuRoster#STANDARD_POOL}) que no tengas.
     */
    public static final DeferredHolder<Item, Item> STANDARD_TERMINAL =
            ITEMS.register("standard_terminal", () -> new StandardTerminalItem(new Item.Properties().stacksTo(1)));

    /** Cápsula Waifu — cofre portátil: guarda (Shift+clic) e invoca a tus waifus. La colección vive en el mundo. */
    public static final DeferredHolder<Item, Item> WAIFU_CAPSULE =
            ITEMS.register("waifu_capsule", () -> new WaifuCapsuleItem(new Item.Properties().stacksTo(1)));

    /** Remielle — token de invocación */
    public static final DeferredHolder<Item, Item> REMIELLE_TOKEN =
            ITEMS.register("remielle_token", () -> new RemielleTokenItem(new Item.Properties().stacksTo(1)));

    /** Tokisaki Kurumi — token de invocación */
    public static final DeferredHolder<Item, Item> TOKISAKI_KURUMI_TOKEN =
            ITEMS.register("tokisaki_kurumi_token", () -> new TokisakiKurumiTokenItem(new Item.Properties().stacksTo(1)));

    /** Yidhari Murphy — token de invocación */
    public static final DeferredHolder<Item, Item> YIDHARI_TOKEN =
            ITEMS.register("yidhari_token", () -> new YidhariTokenItem(new Item.Properties().stacksTo(1)));

    /**
     * Huevo de invocación de Ricardo Milos (el jefe neutral que suelta 10 tiradas).
     *
     * <p>Es la forma cómoda de probarlo: se puede colocar desde el menú creativo. Los colores del
     * huevo los toma el propio {@link net.minecraft.world.item.SpawnEggItem} (violeta oscuro con
     * brillo magenta), sin registrar nada a mano.
     */
    public static final DeferredHolder<Item, Item> RICARDO_MILOS_SPAWN_EGG =
            ITEMS.register("ricardo_milos_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.RICARDO_MILOS.get(), 0x2B1B45, 0xFF4BD8, new Item.Properties()));

    /**
     * Huevo de invocación de Doctor House (el jefe neutral que invoca Wilsons y suelta 5
     * Bolitas Azules). Colores: gabardina azul grisácea con carne.
     */
    public static final DeferredHolder<Item, Item> DOCTOR_HOUSE_SPAWN_EGG =
            ITEMS.register("doctor_house_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.DOCTOR_HOUSE.get(), 0x3B4A5A, 0x8C6E4A, new Item.Properties()));

    /** Huevo de invocación de Wilson (el médico amigo de House). */
    public static final DeferredHolder<Item, Item> WILSON_SPAWN_EGG =
            ITEMS.register("wilson_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.WILSON.get(), 0x6E4F35, 0xC8B89A, new Item.Properties()));

    /**
     * Chorizo — el embutido que Fernanfloo lleva en la mano (modelo GeckoLib). Item normal:
     * se puede sostener o poner en marcos; él lo lleva siempre (drop chance 0).
     */
    public static final DeferredHolder<Item, Item> CHORIZO =
            ITEMS.register("chorizo", () -> new ChorizoItem(new Item.Properties().stacksTo(1)));

    /**
     * Huevo de invocación de Fernanfloo (el jefe neutral del chorizo). Colores: gorra verde
     * con ropa oscura.
     */
    public static final DeferredHolder<Item, Item> FERNANFLOO_SPAWN_EGG =
            ITEMS.register("fernanfloo_spawn_egg", () -> new SpawnEggItem(
                    ModEntities.FERNANFLOO.get(), 0x3AA33A, 0x2B2B2B, new Item.Properties()));

    /** Von Lycaon — token de invocación */
    public static final DeferredHolder<Item, Item> VON_LYCAON_TOKEN =
            ITEMS.register("von_lycaon_token", () -> new VonLycaonTokenItem(new Item.Properties().stacksTo(1)));

    /** Rina — token de invocación */
    public static final DeferredHolder<Item, Item> RINA_TOKEN =
            ITEMS.register("rina_token", () -> new RinaTokenItem(new Item.Properties().stacksTo(1)));

    /** Grace Howard Token — Token de invocación para Grace Howard */
    public static final DeferredHolder<Item, Item> GRACE_HOWARD_TOKEN =
            ITEMS.register("grace_howard_token", () -> new com.gachawaifus.item.GraceHowardTokenItem(new Item.Properties().stacksTo(16)));

    /** Tobichi Origami Token — Token de invocación para Tobichi Origami */
    public static final DeferredHolder<Item, Item> TOBICHI_ORIGAMI_TOKEN =
            ITEMS.register("tobichi_origami_token", () -> new com.gachawaifus.item.TobichiOrigamiTokenItem(new Item.Properties().stacksTo(16)));

    /** Nekomiya Mana (Nekomata) — Token de invocación (Physical / Attack) */
    public static final DeferredHolder<Item, Item> NEKOMATA_TOKEN =
            ITEMS.register("nekomata_token", () -> new NekomataTokenItem(new Item.Properties().stacksTo(16)));

    /** Billy Kid — token de invocación */
    public static final DeferredHolder<Item, Item> BILLY_KID_TOKEN =
            ITEMS.register("billy_kid_token", () -> new BillyKidTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Yixuan — Token de invocación */
    public static final DeferredHolder<Item, Item> ZHU_YUAN_TOKEN =
            ITEMS.register("zhu_yuan_token", () -> new ZhuYuanTokenItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> QINGYI_TOKEN =
            ITEMS.register("qingyi_token", () -> new QingyiTokenItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> YIXUAN_TOKEN =
            ITEMS.register("yixuan_token", () -> new YixuanTokenItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> ANTON_TOKEN =
            ITEMS.register("anton_token", () -> new com.gachawaifus.item.AntonTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> BEN_BIGGER_TOKEN =
            ITEMS.register("ben_bigger_token", () -> new com.gachawaifus.item.BenBiggerTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> CORIN_WICKES_TOKEN =
            ITEMS.register("corin_wickes_token", () -> new com.gachawaifus.item.CorinWickesTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> LUCY_TOKEN =
            ITEMS.register("lucy_token", () -> new com.gachawaifus.item.LucyTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PIPER_WHEEL_TOKEN =
            ITEMS.register("piper_wheel_token", () -> new com.gachawaifus.item.PiperWheelTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SOUKAKU_TOKEN =
            ITEMS.register("soukaku_token", () -> new com.gachawaifus.item.SoukakuTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SETH_LOWELL_TOKEN =
            ITEMS.register("seth_lowell_token", () -> new com.gachawaifus.item.SethLowellTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> JANE_DOE_TOKEN =
            ITEMS.register("jane_doe_token", () -> new com.gachawaifus.item.JaneDoeTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> CAESAR_TOKEN =
            ITEMS.register("caesar_token", () -> new com.gachawaifus.item.CaesarTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> LIGHTER_TOKEN =
            ITEMS.register("lighter_token", () -> new com.gachawaifus.item.LighterTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> TSUKISHIRO_YANAGI_TOKEN =
            ITEMS.register("tsukishiro_yanagi_token", () -> new com.gachawaifus.item.TsukishiroYanagiTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> HARUMASA_TOKEN =
            ITEMS.register("harumasa_token", () -> new com.gachawaifus.item.HarumasaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> EVELYN_CHEVALIER_TOKEN =
            ITEMS.register("evelyn_chevalier_token", () -> new com.gachawaifus.item.EvelynChevalierTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PULCHRA_TOKEN =
            ITEMS.register("pulchra_token", () -> new com.gachawaifus.item.PulchraTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> TRIGGER_TOKEN =
            ITEMS.register("trigger_token", () -> new com.gachawaifus.item.TriggerTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> HUGO_VLAD_TOKEN =
            ITEMS.register("hugo_vlad_token", () -> new com.gachawaifus.item.HugoVladTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> VIVIAN_TOKEN =
            ITEMS.register("vivian_token", () -> new com.gachawaifus.item.VivianTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> JU_FUFU_TOKEN =
            ITEMS.register("ju_fufu_token", () -> new com.gachawaifus.item.JuFufuTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PAN_YINHU_TOKEN =
            ITEMS.register("pan_yinhu_token", () -> new com.gachawaifus.item.PanYinhuTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ALICE_TOKEN =
            ITEMS.register("alice_token", () -> new com.gachawaifus.item.AliceTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ORPHIE_TOKEN =
            ITEMS.register("orphie_token", () -> new com.gachawaifus.item.OrphieTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SEED_TOKEN =
            ITEMS.register("seed_token", () -> new com.gachawaifus.item.SeedTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> MANATO_TOKEN =
            ITEMS.register("manato_token", () -> new com.gachawaifus.item.ManatoTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> LUCIA_TOKEN =
            ITEMS.register("lucia_token", () -> new com.gachawaifus.item.LuciaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> BANYUE_TOKEN =
            ITEMS.register("banyue_token", () -> new com.gachawaifus.item.BanyueTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ZHAO_TOKEN =
            ITEMS.register("zhao_token", () -> new com.gachawaifus.item.ZhaoTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ARIA_TOKEN =
            ITEMS.register("aria_token", () -> new com.gachawaifus.item.AriaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SUNNA_TOKEN =
            ITEMS.register("sunna_token", () -> new com.gachawaifus.item.SunnaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> CISSIA_TOKEN =
            ITEMS.register("cissia_token", () -> new com.gachawaifus.item.CissiaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> NANGONG_YU_TOKEN =
            ITEMS.register("nangong_yu_token", () -> new com.gachawaifus.item.NangongYuTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> PYROIS_TOKEN =
            ITEMS.register("pyrois_token", () -> new com.gachawaifus.item.PyroisTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> NORMA_TOKEN =
            ITEMS.register("norma_token", () -> new com.gachawaifus.item.NormaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> VELINA_TOKEN =
            ITEMS.register("velina_token", () -> new com.gachawaifus.item.VelinaTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SIGRID_TOKEN =
            ITEMS.register("sigrid_token", () -> new com.gachawaifus.item.SigridTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> CLARET_TOKEN =
            ITEMS.register("claret_token", () -> new com.gachawaifus.item.ClaretTokenItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> ROXY_TOKEN =
            ITEMS.register("roxy_token", () -> new com.gachawaifus.item.RoxyTokenItem(new Item.Properties().stacksTo(1)));}