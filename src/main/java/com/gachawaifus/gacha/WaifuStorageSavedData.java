package com.gachawaifus.gacha;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Almacen server-side de waifus capturadas por jugador. La colección sobrevive a la
 * destrucción del item: vive en el mundo, no en el inventario.
 *
 * Cada jugador tiene DOS listas:
 *  - "captured": waifus listas para ser invocadas o guardadas.
 *  - "fallen":   waifus caídas en combate, cuya alma quedó dentro de la Cápsula.
 *                Solo pueden volver con el ritual de revivificación (4 diamantes).
 */
public class WaifuStorageSavedData extends SavedData {

    private static final String NAME = "gachawaifus_waifu_storage";

    private final Map<UUID, List<String>> captured = new HashMap<>();
    private final Map<UUID, List<String>> fallen = new HashMap<>();

    public static WaifuStorageSavedData get(Level level) {
        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WaifuStorageSavedData::new, WaifuStorageSavedData::load, null), NAME);
    }

    private static Map<UUID, List<String>> readList(CompoundTag parent, String key) {
        Map<UUID, List<String>> map = new HashMap<>();
        CompoundTag players = parent.getCompound(key);
        for (String uuidKey : players.getAllKeys()) {
            List<String> ids = new ArrayList<>();
            ListTag list = players.getList(uuidKey, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ids.add(list.getString(i));
            }
            try {
                map.put(UUID.fromString(uuidKey), ids);
            } catch (IllegalArgumentException ignored) {
                // UUID corrupto en el guardado: se descarta esa entrada en vez de romper la carga.
            }
        }
        return map;
    }

    private static CompoundTag writeList(Map<UUID, List<String>> map) {
        CompoundTag players = new CompoundTag();
        for (Map.Entry<UUID, List<String>> e : map.entrySet()) {
            ListTag list = new ListTag();
            for (String id : e.getValue()) {
                list.add(StringTag.valueOf(id));
            }
            players.put(e.getKey().toString(), list);
        }
        return players;
    }

    public static WaifuStorageSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        WaifuStorageSavedData data = new WaifuStorageSavedData();
        data.captured.putAll(readList(tag, "players"));
        data.fallen.putAll(readList(tag, "fallen"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("players", writeList(captured));
        tag.put("fallen", writeList(fallen));
        return tag;
    }

    // ---------------------------------------------------------------- capturadas

    /** Waifus guardadas y listas (vivas dentro de la cápsula). */
    public List<String> get(UUID player) {
        return captured.computeIfAbsent(player, k -> new ArrayList<>());
    }

    public boolean contains(UUID player, String waifuId) {
        return get(player).contains(waifuId);
    }

    public void add(UUID player, String waifuId) {
        List<String> list = get(player);
        if (!list.contains(waifuId)) {
            list.add(waifuId);
        }
        setDirty();
    }

    public void remove(UUID player, String waifuId) {
        get(player).remove(waifuId);
        setDirty();
    }

    // -------------------------------------------------------------------- caídas

    /** Waifus caídas en combate: su alma vive en la cápsula hasta ser revivida. */
    public List<String> getFallen(UUID player) {
        return fallen.computeIfAbsent(player, k -> new ArrayList<>());
    }

    public boolean isFallen(UUID player, String waifuId) {
        return getFallen(player).contains(waifuId);
    }

    /** Registra la caída y la saca de las guardadas vivas (no puede estar en ambas). */
    public void addFallen(UUID player, String waifuId) {
        List<String> down = getFallen(player);
        if (!down.contains(waifuId)) {
            down.add(waifuId);
        }
        get(player).remove(waifuId);
        setDirty();
    }

    /** La waifu vuelve a estar viva y disponible en la cápsula. */
    public void revive(UUID player, String waifuId) {
        getFallen(player).remove(waifuId);
        add(player, waifuId);
        setDirty();
    }
}
