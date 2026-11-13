package com.gachawaifus.vfx;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lee {@code assets/gachawaifus/vfx/<efecto>.json} y devuelve el stage pedido.
 * Nunca lanza: si algo falla devuelve null y lo apunta en el log.
 */
public final class VfxLoader {

    private static final Logger LOG = LoggerFactory.getLogger("gachawaifus-vfx");
    private static final Map<String, VfxStage> CACHE = new HashMap<>();

    private VfxLoader() {
    }

    public static VfxStage load(String effectId, String stage) {
        String key = effectId + "#" + stage;
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        VfxStage loaded = read(effectId, stage);
        CACHE.put(key, loaded);
        return loaded;
    }

    /**
     * Todos los stages del efecto, en el orden del JSON. Lo usa {@link VfxCasters} para las
     * waifus sin GeckoLib: cada stage lleva su {@code trigger.at} como retardo.
     */
    public static List<VfxStage> stages(String effectId) {
        List<VfxStage> result = new ArrayList<>();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return result;
        }
        ResourceLocation location = ResourceLocation.tryParse("gachawaifus:vfx/" + effectId + ".json");
        if (location == null) {
            return result;
        }
        try {
            Optional<Resource> resource = minecraft.getResourceManager().getResource(location);
            if (resource.isEmpty()) {
                LOG.warn("[vfx] no existe {}", location);
                return result;
            }
            JsonObject root;
            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonObject stages = root.getAsJsonObject("stages");
            if (stages == null) {
                return result;
            }
            for (String stage : stages.keySet()) {
                VfxStage loaded = load(effectId, stage);
                if (loaded != null) {
                    result.add(loaded);
                }
            }
        } catch (Exception exception) {
            LOG.warn("[vfx] error listando {}: {}", effectId, exception.toString());
        }
        return result;
    }

    private static VfxStage read(String effectId, String stage) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        // Ojo: getResource NO añade la extensión; hay que pasar la ruta completa del asset.
        ResourceLocation location = ResourceLocation.tryParse("gachawaifus:vfx/" + effectId + ".json");
        if (location == null) {
            LOG.warn("[vfx] id invalido: {}", effectId);
            return null;
        }
        try {
            Optional<Resource> resource = minecraft.getResourceManager().getResource(location);
            if (resource.isEmpty()) {
                LOG.warn("[vfx] no existe {}", location);
                return null;
            }
            JsonObject root;
            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }
            JsonObject stages = root.getAsJsonObject("stages");
            if (stages == null || !stages.has(stage)) {
                LOG.warn("[vfx] {} no tiene el stage '{}'", effectId, stage);
                return null;
            }
            JsonObject json = stages.getAsJsonObject(stage);
            float duration = json.has("duration") ? json.get("duration").getAsFloat() : 1.0F;
            String tier = json.has("tier") ? json.get("tier").getAsString() : "L";
            float at = 0.0F;
            if (json.has("trigger")) {
                JsonObject trigger = json.getAsJsonObject("trigger");
                if (trigger != null && trigger.has("at")) {
                    at = trigger.get("at").getAsFloat();
                }
            }
            List<VfxLayer> layers = new ArrayList<>();
            if (json.has("layers")) {
                for (var element : json.getAsJsonArray("layers")) {
                    layers.add(VfxLayer.parse(element.getAsJsonObject(), duration));
                }
            }
            LOG.info("[vfx] cargado {}#{} ({} capas, {})", effectId, stage, layers.size(), tier);
            return new VfxStage(effectId, stage, tier, duration, at, layers);
        } catch (Exception exception) {
            LOG.warn("[vfx] error leyendo {}#{}: {}", effectId, stage, exception.toString());
            return null;
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
