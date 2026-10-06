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
    public static final Entry NICOLE = new Entry("nicole_demara", () -> ModEntities.NICOLE_DEMARA.get(), () -> ModItems.NICOLE_DEMARA_TOKEN.get(), "Nicole Demara");
    public static final Entry ASTRA_YAO = new Entry("astra_yao", () -> ModEntities.ASTRA_YAO.get(), () -> ModItems.ASTRA_YAO_TOKEN.get(), "Astra Yao");
    public static final Entry ELLEN_JOE = new Entry("ellen_joe", () -> ModEntities.ELLEN_JOE.get(), () -> ModItems.ELLEN_JOE_TOKEN.get(), "Ellen Joe");
    public static final Entry BURNICE = new Entry("burnice_white", () -> ModEntities.BURNICE_WHITE.get(), () -> ModItems.BURNICE_WHITE_TOKEN.get(), "Burnice White");
    public static final Entry ANBY = new Entry("anby_demara", () -> ModEntities.ANBY_DEMARA.get(), () -> ModItems.ANBY_DEMARA_TOKEN.get(), "Anby Demara");
    public static final Entry YUZUHA = new Entry("ukinami_yuzuha", () -> ModEntities.UKINAMI_YUZUHA.get(), () -> ModItems.UKINAMI_YUZUHA_TOKEN.get(), "Ukinami Yuzuha");
    public static final Entry PROMEIA = new Entry("promeia", () -> ModEntities.PROMEIA.get(), () -> ModItems.PROMEIA_TOKEN.get(), "Promeia");
    public static final Entry REMIELLE = new Entry("remielle", () -> ModEntities.REMIELLE.get(), () -> ModItems.REMIELLE_TOKEN.get(), "Remielle");

    /** Orden = rotación del banner */
    public static final List<Entry> ROTATION = List.of(MIYABI, YE_SHUNGUANG, NICOLE, ASTRA_YAO, ELLEN_JOE, BURNICE, ANBY, YUZUHA, PROMEIA, REMIELLE);

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

    public static Entry byToken(Item item) {
        for (Entry e : ROTATION) {
            if (e.token().get() == item) return e;
        }
        return null;
    }

    /**
     * "Consuelo" del 50/50: cualquier waifu de la rotación que el jugador aún no posea.
     * Antes estaba fijado a Nicole Demara como placeholder, lo que hacía que el gacha
     * dejara de aportar waifus nuevas al crecer el roster.
     */
    @Nullable
    public static Entry anyUnowned(Player player) {
        for (Entry e : ROTATION) {
            if (!owns(player, e)) return e;
        }
        return null;
    }

    /** "Tenerla" = token en el inventario o entidad viva propia en un radio de 128 bloques. */
    public static boolean owns(Player player, Entry entry) {
        Item token = entry.token().get();
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(token)) return true;
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
