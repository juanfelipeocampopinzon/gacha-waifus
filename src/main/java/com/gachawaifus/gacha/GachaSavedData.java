package com.gachawaifus.gacha;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Pity y marca de 5★ garantizada por jugador. Persiste por mundo.
 */
public class GachaSavedData extends SavedData {

    private static final String NAME = "gachawaifus_gacha";

    public static class PlayerState {
        public int pity;
        public boolean guaranteed;
    }

    private final Map<UUID, PlayerState> players = new HashMap<>();

    public static GachaSavedData get(Level level) {
        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(GachaSavedData::new, GachaSavedData::load, null), NAME);
    }

    public static GachaSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        GachaSavedData data = new GachaSavedData();
        CompoundTag playersTag = tag.getCompound("players");
        for (String key : playersTag.getAllKeys()) {
            CompoundTag st = playersTag.getCompound(key);
            PlayerState state = new PlayerState();
            state.pity = st.getInt("pity");
            state.guaranteed = st.getBoolean("guaranteed");
            data.players.put(UUID.fromString(key), state);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerState> e : players.entrySet()) {
            CompoundTag st = new CompoundTag();
            st.putInt("pity", e.getValue().pity);
            st.putBoolean("guaranteed", e.getValue().guaranteed);
            playersTag.put(e.getKey().toString(), st);
        }
        tag.put("players", playersTag);
        return tag;
    }

    public PlayerState state(UUID player) {
        return players.computeIfAbsent(player, k -> new PlayerState());
    }
}
