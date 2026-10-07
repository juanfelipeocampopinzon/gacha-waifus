package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.AstraYaoTokenItem;
import com.gachawaifus.item.BurniceWhiteTokenItem;
import com.gachawaifus.item.EllenJoeTokenItem;
import com.gachawaifus.item.GachaTerminalItem;
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
import com.gachawaifus.item.RinaTokenItem;
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

    /** Bolita Azul — tirada reservada para más adelante (todavía no se gasta en nada). */
    public static final DeferredHolder<Item, Item> BLUE_BALL =
            ITEMS.register("blue_ball", () -> new Item(new Item.Properties()));

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

    /** Von Lycaon — token de invocación */
    public static final DeferredHolder<Item, Item> VON_LYCAON_TOKEN =
            ITEMS.register("von_lycaon_token", () -> new VonLycaonTokenItem(new Item.Properties().stacksTo(1)));

    /** Rina — token de invocación */
    public static final DeferredHolder<Item, Item> RINA_TOKEN =
            ITEMS.register("rina_token", () -> new RinaTokenItem(new Item.Properties().stacksTo(1)));

    /** Grace Howard Token — Token de invocación para Grace Howard */
    public static final DeferredHolder<Item, Item> GRACE_HOWARD_TOKEN =
            ITEMS.register("grace_howard_token", () -> new com.gachawaifus.item.GraceHowardTokenItem(new Item.Properties().stacksTo(16)));
}