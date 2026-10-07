package com.gachawaifus.gacha;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

    /** Orden = rotación del banner */
    public static final List<Entry> ROTATION = List.of(MIYABI, YE_SHUNGUANG, KOLEDA, NICOLE, ASTRA_YAO, ELLEN_JOE, BURNICE, ANBY, YUZUHA, PROMEIA, REMIELLE, TOKISAKI_KURUMI, YIDHARI, RINA, VON_LYCAON);

    private WaifuRoster() {
    }

    public static Entry featured(ServerLevel level) {
        long day = level.getServer().overworld().getDayTime() / 24000L;
        return ROTATION.get((int) (Math.floorMod(day, ROTATION.size())));
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
     */
    @Nullable
    public static Entry next(Entry entry) {
        int i = ROTATION.indexOf(entry);
        if (i < 0) return null;
        return ROTATION.get((i + 1) % ROTATION.size());
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
     * "Tenerla" = token en el inventario, entidad viva propia cerca, o guardada/caída en la
     * Cápsula Waifu. Antes la cápsula no contaba, así que guardar una waifu hacía que el gacha la
     * volviera a dar (duplicado) y una waifu caída parecía no poseída.
     */
    public static boolean owns(Player player, Entry entry) {
        Item token = entry.token().get();
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(token)) return true;
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
