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
        /** Pity del banner PERMANENTE (StandardTerminalItem): independiente de la destacada. */
        public int standardPity;
    }

    private final Map<UUID, PlayerState> players = new HashMap<>();

    /**
     * Estado del Gacha, SIEMPRE guardado en el Overworld.
     *
     * <p>Antes se usaba el nivel del jugador, y como cada dimensión tiene su propio almacén
     * ({@code world/DIM-1/data/...}), el pity y la garantía se reiniciaban al tirar en el Nether
     * o en el End. Con el Overworld fijo hay una sola pity por jugador y mundo.
     */
    public static GachaSavedData get(Level level) {
        ServerLevel overworld = ((ServerLevel) level).getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
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
            state.standardPity = st.getInt("standard_pity");
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
            st.putInt("standard_pity", e.getValue().standardPity);
            playersTag.put(e.getKey().toString(), st);
        }
        tag.put("players", playersTag);
        return tag;
    }

    public PlayerState state(UUID player) {
        return players.computeIfAbsent(player, k -> new PlayerState());
    }
}
