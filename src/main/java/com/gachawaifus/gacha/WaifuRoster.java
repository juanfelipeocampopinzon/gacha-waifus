package com.gachawaifus.gacha;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Roster fijo de las 9 waifus. El orden de la lista define la rotación diaria del banner.
 */
public final class WaifuRoster {

    public record Entry(String id, Supplier<EntityType<? extends AbstractWaifuEntity>> type,
                        Supplier<Item> token, String name) {
    }

    public static final Entry MIYABI = new Entry("miyabi", () -> ModEntities.MIYABI.get(), () -> ModItems.MIYABI_TOKEN.get(), "Hoshimi Miyabi");
    public static final Entry YE_SHUNGUANG = new Entry("ye_shunguang", () -> ModEntities.YE_SHUNGUANG.get(), () -> ModItems.YE_SHUNGUANG_TOKEN.get(), "Ye Shunguang");
    public static final Entry KOLEDA = new Entry("koleda", () -> ModEntities.KOLEDA.get(), () -> ModItems.KOLEDA_TOKEN.get(), "Koleda");
    public static final Entry NICOLE = new Entry("nicole_demara", () -> ModEntities.NICOLE_DEMARA.get(), () -> ModItems.NICOLE_DEMARA_TOKEN.get(), "Nicole Demara");
    public static final Entry ASTRA_YAO = new Entry("astra_yao", () -> ModEntities.ASTRA_YAO.get(), () -> ModItems.ASTRA_YAO_TOKEN.get(), "Astra Yao");
    public static final Entry ELLEN_JOE = new Entry("ellen_joe", () -> ModEntities.ELLEN_JOE.get(), () -> ModItems.ELLEN_JOE_TOKEN.get(), "Ellen Joe");
    public static final Entry BURNICE = new Entry("burnice_white", () -> ModEntities.BURNICE_WHITE.get(), () -> ModItems.BURNICE_WHITE_TOKEN.get(), "Burnice White");
    public static final Entry ANBY = new Entry("anby_demara", () -> ModEntities.ANBY_DEMARA.get(), () -> ModItems.ANBY_DEMARA_TOKEN.get(), "Anby Demara");
    public static final Entry YUZUHA = new Entry("ukinami_yuzuha", () -> ModEntities.UKINAMI_YUZUHA.get(), () -> ModItems.UKINAMI_YUZUHA_TOKEN.get(), "Ukinami Yuzuha");
    public static final Entry PROMEIA = new Entry("promeia", () -> ModEntities.PROMEIA.get(), () -> ModItems.PROMEIA_TOKEN.get(), "Promeia");
    public static final Entry REMIELLE = new Entry("remielle", () -> ModEntities.REMIELLE.get(), () -> ModItems.REMIELLE_TOKEN.get(), "Remielle");
    public static final Entry TOKISAKI_KURUMI = new Entry("tokisaki_kurumi", () -> ModEntities.TOKISAKI_KURUMI.get(), () -> ModItems.TOKISAKI_KURUMI_TOKEN.get(), "Tokisaki Kurumi");
    public static final Entry YIDHARI = new Entry("yidhari", () -> ModEntities.YIDHARI.get(), () -> ModItems.YIDHARI_TOKEN.get(), "Yidhari Murphy");
    public static final Entry RINA = new Entry("rina", () -> ModEntities.RINA_ENTITY.get(), () -> ModItems.RINA_TOKEN.get(), "Rina");
    public static final Entry VON_LYCAON = new Entry("von_lycaon", () -> ModEntities.VON_LYCAON.get(), () -> ModItems.VON_LYCAON_TOKEN.get(), "Von Lycaon");

    /** Grace Howard — "Anomaly" (Electric / Support-Anomaly) */
    public static final Entry GRACE_HOWARD = new Entry("grace_howard", () -> ModEntities.GRACE_HOWARD.get(), () -> ModItems.GRACE_HOWARD_TOKEN.get(), "Grace Howard");

    /** Soldier 11 — Attack (Fire) */
    public static final Entry SOLDIER_11 = new Entry("soldier_11", () -> ModEntities.SOLDIER_11.get(), () -> ModItems.SOLDIER_11_TOKEN.get(), "Soldier 11");

    /** Tobichi Origami — Defense (White / Defense) */
    public static final Entry TOBICHI_ORIGAMI = new Entry("tobichi_origami", () -> ModEntities.TOBICHI_ORIGAMI.get(), () -> ModItems.TOBICHI_ORIGAMI_TOKEN.get(), "Tobichi Origami");

    /** Qingyi — Tea Master (Blue / Stun) */
    public static final Entry QINGYI = new Entry("qingyi", () -> ModEntities.QINGYI.get(), () -> ModItems.QINGYI_TOKEN.get(), "Qingyi");

    /** Nekomiya Mana (Nekomata) — Attack (Physical / Cunning Hares) */
    public static final Entry NEKOMATA = new Entry("nekomata", () -> ModEntities.NEKOMATA.get(), () -> ModItems.NEKOMATA_TOKEN.get(), "Nekomiya Mana");

    /** Billy Kid — Attack */
    public static final Entry BILLY_KID = new Entry("billy_kid", () -> ModEntities.BILLY_KID.get(), () -> ModItems.BILLY_KID_TOKEN.get(), "Billy Kid");

    public static final Entry YIXUAN = new Entry("yixuan", () -> ModEntities.YIXUAN.get(), () -> ModItems.YIXUAN_TOKEN.get(), "Yixuan");

    public static final Entry ZHU_YUAN = new Entry("zhu_yuan", () -> ModEntities.ZHU_YUAN.get(), () -> ModItems.ZHU_YUAN_TOKEN.get(), "Zhu Yuan");

    public static final Entry PYROIS = new Entry("pyrois", () -> ModEntities.PYROIS.get(), () -> ModItems.PYROIS_TOKEN.get(), "Pyrois");
    public static final Entry NORMA = new Entry("norma", () -> ModEntities.NORMA.get(), () -> ModItems.NORMA_TOKEN.get(), "Norma Hollowell");
    public static final Entry VELINA = new Entry("velina", () -> ModEntities.VELINA.get(), () -> ModItems.VELINA_TOKEN.get(), "Velina Airgid");
    public static final Entry SIGRID = new Entry("sigrid", () -> ModEntities.SIGRID.get(), () -> ModItems.SIGRID_TOKEN.get(), "Sigrid de L'Azur");
    public static final Entry CLARET = new Entry("claret", () -> ModEntities.CLARET.get(), () -> ModItems.CLARET_TOKEN.get(), "Claret Flint");
    public static final Entry ROXY = new Entry("roxy", () -> ModEntities.ROXY.get(), () -> ModItems.ROXY_TOKEN.get(), "Roxy Ifrita Pryce");
    public static final Entry ORPHIE = new Entry("orphie", () -> ModEntities.ORPHIE.get(), () -> ModItems.ORPHIE_TOKEN.get(), "Orphie Magnusson");
    public static final Entry SEED = new Entry("seed", () -> ModEntities.SEED.get(), () -> ModItems.SEED_TOKEN.get(), "Seed");
    public static final Entry MANATO = new Entry("manato", () -> ModEntities.MANATO.get(), () -> ModItems.MANATO_TOKEN.get(), "Komano Manato");
    public static final Entry LUCIA = new Entry("lucia", () -> ModEntities.LUCIA.get(), () -> ModItems.LUCIA_TOKEN.get(), "Lucia Elowen");
    public static final Entry BANYUE = new Entry("banyue", () -> ModEntities.BANYUE.get(), () -> ModItems.BANYUE_TOKEN.get(), "Banyue");
    public static final Entry ZHAO = new Entry("zhao", () -> ModEntities.ZHAO.get(), () -> ModItems.ZHAO_TOKEN.get(), "Zhao");
    public static final Entry ARIA = new Entry("aria", () -> ModEntities.ARIA.get(), () -> ModItems.ARIA_TOKEN.get(), "Aria");
    public static final Entry SUNNA = new Entry("sunna", () -> ModEntities.SUNNA.get(), () -> ModItems.SUNNA_TOKEN.get(), "Sunna");
    public static final Entry CISSIA = new Entry("cissia", () -> ModEntities.CISSIA.get(), () -> ModItems.CISSIA_TOKEN.get(), "Cissia");
    public static final Entry NANGONG_YU = new Entry("nangong_yu", () -> ModEntities.NANGONG_YU.get(), () -> ModItems.NANGONG_YU_TOKEN.get(), "Nangong Yu");
    public static final Entry TSUKISHIRO_YANAGI = new Entry("tsukishiro_yanagi", () -> ModEntities.TSUKISHIRO_YANAGI.get(), () -> ModItems.TSUKISHIRO_YANAGI_TOKEN.get(), "Tsukishiro Yanagi");
    public static final Entry HARUMASA = new Entry("harumasa", () -> ModEntities.HARUMASA.get(), () -> ModItems.HARUMASA_TOKEN.get(), "Asaba Harumasa");
    public static final Entry EVELYN_CHEVALIER = new Entry("evelyn_chevalier", () -> ModEntities.EVELYN_CHEVALIER.get(), () -> ModItems.EVELYN_CHEVALIER_TOKEN.get(), "Evelyn Chevalier");
    public static final Entry PULCHRA = new Entry("pulchra", () -> ModEntities.PULCHRA.get(), () -> ModItems.PULCHRA_TOKEN.get(), "Pulchra Fellini");
    public static final Entry TRIGGER = new Entry("trigger", () -> ModEntities.TRIGGER.get(), () -> ModItems.TRIGGER_TOKEN.get(), "Trigger");
    public static final Entry HUGO_VLAD = new Entry("hugo_vlad", () -> ModEntities.HUGO_VLAD.get(), () -> ModItems.HUGO_VLAD_TOKEN.get(), "Hugo Vlad");
    public static final Entry VIVIAN = new Entry("vivian", () -> ModEntities.VIVIAN.get(), () -> ModItems.VIVIAN_TOKEN.get(), "Vivian Banshee");
    public static final Entry JU_FUFU = new Entry("ju_fufu", () -> ModEntities.JU_FUFU.get(), () -> ModItems.JU_FUFU_TOKEN.get(), "Ju Fufu");
    public static final Entry PAN_YINHU = new Entry("pan_yinhu", () -> ModEntities.PAN_YINHU.get(), () -> ModItems.PAN_YINHU_TOKEN.get(), "Pan Yinhu");
    public static final Entry ALICE = new Entry("alice", () -> ModEntities.ALICE.get(), () -> ModItems.ALICE_TOKEN.get(), "Alice Thymefield");
    public static final Entry ANTON = new Entry("anton", () -> ModEntities.ANTON.get(), () -> ModItems.ANTON_TOKEN.get(), "Anton Ivanov");
    public static final Entry BEN_BIGGER = new Entry("ben_bigger", () -> ModEntities.BEN_BIGGER.get(), () -> ModItems.BEN_BIGGER_TOKEN.get(), "Ben Bigger");
    public static final Entry CORIN_WICKES = new Entry("corin_wickes", () -> ModEntities.CORIN_WICKES.get(), () -> ModItems.CORIN_WICKES_TOKEN.get(), "Corin Wickes");
    public static final Entry LUCY = new Entry("lucy", () -> ModEntities.LUCY.get(), () -> ModItems.LUCY_TOKEN.get(), "Luciana Lucy de Montefio");
    public static final Entry PIPER_WHEEL = new Entry("piper_wheel", () -> ModEntities.PIPER_WHEEL.get(), () -> ModItems.PIPER_WHEEL_TOKEN.get(), "Piper Wheel");
    public static final Entry SOUKAKU = new Entry("soukaku", () -> ModEntities.SOUKAKU.get(), () -> ModItems.SOUKAKU_TOKEN.get(), "Soukaku");
    public static final Entry SETH_LOWELL = new Entry("seth_lowell", () -> ModEntities.SETH_LOWELL.get(), () -> ModItems.SETH_LOWELL_TOKEN.get(), "Seth Lowell");
    public static final Entry JANE_DOE = new Entry("jane_doe", () -> ModEntities.JANE_DOE.get(), () -> ModItems.JANE_DOE_TOKEN.get(), "Jane Doe");
    public static final Entry CAESAR = new Entry("caesar", () -> ModEntities.CAESAR.get(), () -> ModItems.CAESAR_TOKEN.get(), "Caesar King");
    public static final Entry LIGHTER = new Entry("lighter", () -> ModEntities.LIGHTER.get(), () -> ModItems.LIGHTER_TOKEN.get(), "Lighter");

    /** Aqua (KonoSuba) — Support (Agua / curación) */
    public static final Entry AQUA = new Entry("aqua", () -> ModEntities.AQUA.get(), () -> ModItems.AQUA_TOKEN.get(), "Aqua");

    /** Megumin (KonoSuba) — Anomaly (Explosión) */
    public static final Entry MEGUMIN = new Entry("megumin", () -> ModEntities.MEGUMIN.get(), () -> ModItems.MEGUMIN_TOKEN.get(), "Megumin");

    /** Darkness (KonoSuba) — Defense (Cruzada) */
    public static final Entry DARKNESS = new Entry("darkness", () -> ModEntities.DARKNESS.get(), () -> ModItems.DARKNESS_TOKEN.get(), "Darkness");
    /** Orden = rotación del banner */
    public static final List<Entry> ROTATION = List.of(MIYABI, YE_SHUNGUANG, KOLEDA, NICOLE, ASTRA_YAO, ELLEN_JOE, BURNICE, ANBY, YUZUHA, PROMEIA, REMIELLE, TOKISAKI_KURUMI, YIDHARI, RINA, VON_LYCAON, GRACE_HOWARD, SOLDIER_11, TOBICHI_ORIGAMI, NEKOMATA, BILLY_KID, YIXUAN, ZHU_YUAN, QINGYI, ANTON, BEN_BIGGER, CORIN_WICKES, LUCY, PIPER_WHEEL, SOUKAKU, SETH_LOWELL, JANE_DOE, CAESAR, LIGHTER, TSUKISHIRO_YANAGI, HARUMASA, EVELYN_CHEVALIER, PULCHRA, TRIGGER, HUGO_VLAD, VIVIAN, JU_FUFU, PAN_YINHU, ALICE, ORPHIE, SEED, MANATO, LUCIA, BANYUE, ZHAO, ARIA, SUNNA, CISSIA, NANGONG_YU, PYROIS, NORMA, VELINA, SIGRID, CLARET, ROXY, AQUA, MEGUMIN, DARKNESS);

    /**
     * Pool fijo del 50/50 perdido (el "banner permanente" de ZZZ): el consuelo solo cae de
     * entre estas 7. Están también en {@link #ROTATION} (para poder sacarlas con el gacha), pero
     * NUNCA son la destacada: para eso está {@link #PROMO_ROTATION}.
     */
    public static final List<Entry> STANDARD_POOL = List.of(RINA, VON_LYCAON, NICOLE, KOLEDA, GRACE_HOWARD, SOLDIER_11, NEKOMATA);

    /**
     * Rotación del banner <b>promocional</b>: {@link #ROTATION} sin las 7 del {@link #STANDARD_POOL}.
     *
     * <p>Las del pool permanente pueden salir en el banner rosa al perder el 50/50 (consuelo), pero
     * no pueden ser la waifu destacada del día: si la destacada fuera Koleda, el banner estaría
     * anunciando como promocional a una que ya se regala en el permanente.
     */
    public static final List<Entry> PROMO_ROTATION = buildPromoRotation();

    private static List<Entry> buildPromoRotation() {
        List<Entry> out = new java.util.ArrayList<>();
        for (Entry e : ROTATION) {
            if (!STANDARD_POOL.contains(e)) out.add(e);
        }
        return List.copyOf(out);
    }

    /**
     * Las <b>4★</b> del mod: los personajes que en su juego de origen son de rango bajo. Salen del
     * Terminal Rosa con su propia probabilidad y su propio pity (bastante más fácil que una 5★), así
     * que esta lista es la vía rápida para completar la colección.
     *
     * <p>Vive aquí, en un solo sitio, para que subir o bajar de rango a una waifu sea mover su id de
     * esta línea. Koleda y Alice subieron a 5★ (en su juego de origen lo son) y Pan Yinhu bajó a
     * 4★. A propósito NO están {@code anby_demara} ni {@code billy_kid}: en este mod ya existen como
     * 5★ (Soldier 0 y la versión destacada), y mientras se decide qué hacer con ellos no pueden
     * aparecer dos veces en el mismo tirón.
     */
    public static final Set<String> FOUR_STAR = Set.of(
            "nicole_demara", "corin_wickes", "lucy", "piper_wheel", "manato", "soukaku",
            "anton", "ben_bigger", "harumasa", "nekomata", "pan_yinhu");

    private WaifuRoster() {
    }

    /** ¿Es de rango 4★ (la vía fácil del Terminal)? */
    public static boolean isFourStar(Entry entry) {
        return FOUR_STAR.contains(entry.id());
    }

    /**
     * Una 4★ que al jugador todavía le falte, al azar. {@code null} si ya las tiene todas: el
     * Terminal lo trata como tirada de comida sin castigar la pity.
     */
    @Nullable
    public static Entry randomFourStarUnowned(Player player) {
        List<Entry> libres = new java.util.ArrayList<>();
        for (Entry e : ROTATION) {
            if (isFourStar(e) && !owns(player, e)) {
                libres.add(e);
            }
        }
        return libres.isEmpty() ? null : libres.get(player.getRandom().nextInt(libres.size()));
    }

    /** La destacada del día: solo de la rotación promocional (nunca una del pool permanente). */
    public static Entry featured(ServerLevel level) {
        long day = level.getServer().overworld().getDayTime() / 24000L;
        return PROMO_ROTATION.get((int) (Math.floorMod(day, PROMO_ROTATION.size())));
    }

    public static Entry byId(String id) {
        for (Entry e : ROTATION) {
            if (e.id().equals(id)) return e;
        }
        return null;
    }

    /**
     * La waifu SIGUIENTE en la rotación (dando la vuelta al final). Se usa para la tirada doble:
     * cuando un 5★ sale, hay una probabilidad de que caiga también la siguiente del roster.
     *
     * <p>Si la que salió es promocional, la siguiente también lo es; si fue un consuelo del pool
     * permanente, la siguiente sale de ese mismo pool (nunca se cruzan).
     */
    @Nullable
    public static Entry next(Entry entry) {
        List<Entry> list = PROMO_ROTATION.contains(entry) ? PROMO_ROTATION : STANDARD_POOL;
        int i = list.indexOf(entry);
        if (i < 0) return null;
        return list.get((i + 1) % list.size());
    }

    public static Entry byToken(Item item) {
        for (Entry e : ROTATION) {
            if (e.token().get() == item) return e;
        }
        return null;
    }

    /**
     * "Consuelo" del 50/50: cualquier waifu de la rotación que el jugador aún no posea,
     * elegida AL AZAR. Antes recorría la lista en orden fijo y devolvía siempre la primera que
     * faltara (siempre Miyabi, luego siempre Ye Shunguang...), lo que hacía que la colección
     * tendiera a ser un prefijo de la rotación.
     */
    @Nullable
    public static Entry anyUnowned(Player player) {
        return randomUnowned(player, null);
    }

    /**
     * Una waifu no poseída al azar, excluyendo la indicada (normalmente la destacada, que ya se
     * resuelve en el 50/50). Devuelve {@code null} solo si no queda ninguna por conseguir.
     */
    @Nullable
    public static Entry randomUnowned(Player player, @Nullable Entry exclude) {
        List<Entry> libres = new java.util.ArrayList<>();
        for (Entry e : ROTATION) {
            if (exclude != null && e.id().equals(exclude.id())) continue;
            if (!owns(player, e)) libres.add(e);
        }
        if (libres.isEmpty()) return null;
        return libres.get(player.getRandom().nextInt(libres.size()));
    }

    /**
     * Consuelo del 50/50: waifu NO poseída del {@link #STANDARD_POOL}, elegida AL AZAR. Si las 7
     * del pool ya están poseídas, se recurre a cualquier no poseída de la rotación completa
     * (nunca null mientras quede alguna por conseguir).
     */
    @Nullable
    public static Entry randomStandardUnowned(Player player, @Nullable Entry exclude) {
        List<Entry> libres = new java.util.ArrayList<>();
        for (Entry e : STANDARD_POOL) {
            if (exclude != null && e.id().equals(exclude.id())) continue;
            if (!owns(player, e)) libres.add(e);
        }
        if (libres.isEmpty()) return randomUnowned(player, exclude);
        return libres.get(player.getRandom().nextInt(libres.size()));
    }

    /**
     * Ids de las waifus que el jugador ya tiene, en una sola pasada y solo con lo que está en
     * memoria: los tokens del inventario, la Cápsula (vivas y caídas), el registro de doma salvaje y
     * las waifus que se le enseñan. Es lo mismo que {@link #owns} sin repetir el barrido de 128
     * bloques por cada entrada del roster, que es lo que necesitaban el spawn de salvajes y ahora
     * {@code /coleccion}.
     */
    public static Set<String> ownedIds(Player player, Iterable<AbstractWaifuEntity> visibles) {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            Entry token = byToken(player.getInventory().getItem(i).getItem());
            if (token != null) {
                ids.add(token.id());
            }
        }
        if (!player.level().isClientSide()) {
            ids.addAll(GachaSavedData.get(player.level()).state(player.getUUID()).wildClaims);
            WaifuStorageSavedData capsula = WaifuStorageSavedData.get(player.level());
            ids.addAll(capsula.get(player.getUUID()));
            ids.addAll(capsula.getFallen(player.getUUID()));
            for (AbstractWaifuEntity cerca : visibles) {
                if (player.getUUID().equals(cerca.getOwnerUUID())) {
                    ids.add(BuiltInRegistries.ENTITY_TYPE.getKey(cerca.getType()).getPath());
                }
            }
        }
        return ids;
    }

    /** {@link #ownedIds(Player, Iterable)} con el barrido de 128 bloques alrededor del jugador. */
    public static Set<String> ownedIds(Player player) {
        return ownedIds(player, player.level().getEntitiesOfClass(AbstractWaifuEntity.class,
                player.getBoundingBox().inflate(128.0D)));
    }

    /**
     * "Tenerla" = token en el inventario, entidad viva propia cerca, guardada/caída en la Cápsula
     * Waifu, o domada una vez de salvaje. Antes la cápsula no contaba, así que guardar una waifu hacía
     * que el gacha la volviera a dar (duplicado) y una waifu caída parecía no poseída.
     *
     * <p>El registro de doma ({@link GachaSavedData.PlayerState#wildClaims}) es el que cierra el
     * agujero de la waifu salvaje domada: no deja token ni entrada en la cápsula, y su entidad puede
     * estar paseando fuera de los 128 bloques que mide el chequeo de arriba.
     */
    public static boolean owns(Player player, Entry entry) {
        Item token = entry.token().get();
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(token)) return true;
        }
        if (!player.level().isClientSide()
                && GachaSavedData.get(player.level()).hasWildClaim(player.getUUID(), entry.id())) {
            return true;
        }
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(player.level());
        if (storage.contains(player.getUUID(), entry.id()) || storage.isFallen(player.getUUID(), entry.id())) {
            return true;
        }
        AABB box = player.getBoundingBox().inflate(128.0D);
        List<AbstractWaifuEntity> owned = player.level().getEntitiesOfClass(AbstractWaifuEntity.class, box,
                e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));
        for (AbstractWaifuEntity e : owned) {
            if (e.getType() == entry.type().get()) return true;
        }
        return false;
    }

    /** Solo la parte de entidad viva de owns(), para re-chequeos al invocar desde la cápsula. */
    public static boolean hasActive(Player player, Entry entry) {
        AABB box = player.getBoundingBox().inflate(128.0D);
        List<AbstractWaifuEntity> owned = player.level().getEntitiesOfClass(AbstractWaifuEntity.class, box,
                e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));
        for (AbstractWaifuEntity e : owned) {
            if (e.getType() == entry.type().get()) return true;
        }
        return false;
    }
}
