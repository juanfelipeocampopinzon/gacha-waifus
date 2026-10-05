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
 */
public class WaifuStorageSavedData extends SavedData {

    private static final String NAME = "gachawaifus_waifu_storage";

    private final Map<UUID, List<String>> storage = new HashMap<>();

    public static WaifuStorageSavedData get(Level level) {
        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WaifuStorageSavedData::new, WaifuStorageSavedData::load, null), NAME);
    }

    public static WaifuStorageSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        WaifuStorageSavedData data = new WaifuStorageSavedData();
        CompoundTag players = tag.getCompound("players");
        for (String key : players.getAllKeys()) {
            List<String> ids = new ArrayList<>();
            ListTag list = players.getList(key, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ids.add(list.getString(i));
            }
            data.storage.put(UUID.fromString(key), ids);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag players = new CompoundTag();
        for (Map.Entry<UUID, List<String>> e : storage.entrySet()) {
            ListTag list = new ListTag();
            for (String id : e.getValue()) {
                list.add(StringTag.valueOf(id));
            }
            players.put(e.getKey().toString(), list);
        }
        tag.put("players", players);
        return tag;
    }

    public List<String> get(UUID player) {
        return storage.computeIfAbsent(player, k -> new ArrayList<>());
    }

    public boolean contains(UUID player, String waifuId) {
        return get(player).contains(waifuId);
    }

    public void add(UUID player, String waifuId) {
        get(player).add(waifuId);
        setDirty();
    }

    public void remove(UUID player, String waifuId) {
        get(player).remove(waifuId);
        setDirty();
    }
}
