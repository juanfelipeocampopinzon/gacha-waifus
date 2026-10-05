package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.AstraYaoTokenItem;
import com.gachawaifus.item.BurniceWhiteTokenItem;
import com.gachawaifus.item.DormantWaifuCoreItem;
import com.gachawaifus.item.EllenJoeTokenItem;
import com.gachawaifus.item.GachaTerminalItem;
import com.gachawaifus.item.MiyabiMisiramaTokenItem;
import com.gachawaifus.item.NicoleDemaraTokenItem;
import com.gachawaifus.item.YeShunguangTokenItem;
import com.gachawaifus.item.AnbyDemaraTokenItem;
import com.gachawaifus.item.UkinamiYuzuhaTokenItem;
import com.gachawaifus.item.PromeiaTokenItem;
import com.gachawaifus.item.WaifuCapsuleItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, GachaWaifusMod.MODID);

    public static final DeferredHolder<Item, Item> ASTRA_YAO_TOKEN =
            ITEMS.register("astra_yao_token", () -> new AstraYaoTokenItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> ELLEN_JOE_TOKEN =
            ITEMS.register("ellen_joe_token", () -> new EllenJoeTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Hoshimi Miyabi — Token de invocación (sumona a la Dama de la Luz) */
    public static final DeferredHolder<Item, Item> MIYABI_TOKEN =
            ITEMS.register("miyabi_token", () -> new MiyabiMisiramaTokenItem(new Item.Properties().stacksTo(1)));

    /** ★★★★★ Burnice White — Token de invocación */
    public static final DeferredHolder<Item, Item> BURNICE_WHITE_TOKEN =
            ITEMS.register("burnice_white_token", () -> new BurniceWhiteTokenItem(new Item.Properties().stacksTo(16)));

    /** ★★★★★ Dormant Waifu Core — Núcleo durmiente para resurrección de waifus */
    public static final DeferredHolder<Item, Item> DORMANT_WAIFU_CORE =
            ITEMS.register("dormant_waifu_core", () -> new DormantWaifuCoreItem(new Item.Properties().stacksTo(64)));

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

    /** Terminal Gacha — máquina de tiradas con diamantes (1 diamante = 1 tirada, Shift = x10) */
    public static final DeferredHolder<Item, Item> GACHA_TERMINAL =
            ITEMS.register("gacha_terminal", () -> new GachaTerminalItem(new Item.Properties().stacksTo(1)));

    /** Cápsula Waifu — cofre portátil: guarda (Shift+clic) e invoca a tus waifus. La colección vive en el mundo. */
    public static final DeferredHolder<Item, Item> WAIFU_CAPSULE =
            ITEMS.register("waifu_capsule", () -> new WaifuCapsuleItem(new Item.Properties().stacksTo(1)));
}