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
 * Pity, marca de 5★ garantizada y reclamaciones de doma por jugador. Persiste por mundo.
 */
public class GachaSavedData extends SavedData {

    private static final String NAME = "gachawaifus_gacha";

    public static class PlayerState {
        public int pity;
        public boolean guaranteed;
        /** Pity del banner PERMANENTE (pestaña permanente del Terminal Gacha): independiente de la destacada. */
        public int standardPity;
        /**
         * Pity de la vía <b>4★</b> del Terminal Rosa. Corre aparte de la 5★: una 5★ que cae también
         * la reinicia (es un premio mayor), pero una 4★ no toca la pity de la destacada.
         */
        public int pity4;
        /**
         * Waifus domadas de salvajes, por id. Es un registro <b>permanente</b>: una doma no deja
         * token ni entrada en la Cápsula, así que sin esta lista {@code WaifuRoster.owns()} solo veía
         * a la waifu si estaba cerca del jugador y el Terminal podía volver a darla.
         */
        public final List<String> wildClaims = new ArrayList<>();
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
            UUID player;
            try {
                player = UUID.fromString(key);
            } catch (IllegalArgumentException malformed) {
                continue; // clave escrita a mano o corrompida: se ignora en vez de tumbar el mundo
            }
            CompoundTag st = playersTag.getCompound(key);
            PlayerState state = new PlayerState();
            state.pity = st.getInt("pity");
            state.guaranteed = st.getBoolean("guaranteed");
            state.standardPity = st.getInt("standard_pity");
            state.pity4 = st.getInt("pity4");
            ListTag claims = st.getList("wild_claims", Tag.TAG_STRING);
            for (int i = 0; i < claims.size(); i++) {
                state.wildClaims.add(claims.getString(i));
            }
            data.players.put(player, state);
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
            st.putInt("pity4", e.getValue().pity4);
            ListTag claims = new ListTag();
            for (String id : e.getValue().wildClaims) {
                claims.add(StringTag.valueOf(id));
            }
            st.put("wild_claims", claims);
            playersTag.put(e.getKey().toString(), st);
        }
        tag.put("players", playersTag);
        return tag;
    }

    public PlayerState state(UUID player) {
        return players.computeIfAbsent(player, k -> new PlayerState());
    }

    /**
     * Sella una doma: a partir de ahora esa waifu cuenta como poseída aunque su entidad esté lejos,
     * dentro de la Cápsula o ya ni exista. Va al registro del Overworld, como el pity.
     */
    public void claimWild(UUID player, String waifuId) {
        PlayerState state = state(player);
        if (!state.wildClaims.contains(waifuId)) {
            state.wildClaims.add(waifuId);
            setDirty();
        }
    }

    /** ¿Ya fue domada por este jugador? Solo lectura: no ensucia el guardado ni crea estado vacío. */
    public boolean hasWildClaim(UUID player, String waifuId) {
        PlayerState state = players.get(player);
        return state != null && state.wildClaims.contains(waifuId);
    }
}
