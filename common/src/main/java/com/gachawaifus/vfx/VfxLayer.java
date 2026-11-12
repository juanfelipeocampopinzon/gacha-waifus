package com.gachawaifus.vfx;

import com.google.gson.JsonObject;
import net.minecraft.world.phys.Vec3;

/**
 * Una capa del JSON de efecto (emisor, anillo, brillo...). Solo se leen las claves que este
 * prototipo entiende; el resto del estándar se conserva en el archivo pero se ignora.
 */
public final class VfxLayer {

    public String type = "";
    public float at = 0.0F;
    public String anchor = "entity";
    public Vec3 offset = Vec3.ZERO;

    // emitter
    public String shape = "point";
    public float radius = 0.0F;
    public String texture = "spark";
    public boolean additive = false;
    public int count = 0;
    public Curve rate = null;
    public float duration = 0.2F;
    public float[] lifetime = {0.3F, 0.6F};
    public float[] size = {0.3F, 0.05F};
    public Gradient color = null;
    public float[] speed = {0.0F, 1.0F};
    public String direction = "random";
    public float gravity = 0.0F;
    /** Estirado del quad en la dirección de la velocidad (0 = cuadrado, 3 = chorro). */
    public float stretch = 0.0F;
    /** Tamaño relativo del núcleo caliente que se dibuja dentro (0 = sin núcleo). */
    public float core = 0.0F;
    /** Ondulación lateral en bloques por tick. */
    public float swirl = 0.0F;
    /**
     * Eje local del hueso que apunta hacia la boquilla; por defecto [0,-1,0] (-Y).
     */
    public float[] boneAxis = {0.0F, -1.0F, 0.0F};

    // ring_wave
    public float[] ringRadius = {0.5F, 3.0F};
    public float[] thickness = {0.3F, 0.1F};
    public String ease = "linear";
    public float y = 0.0F;

    public static VfxLayer parse(JsonObject json, float stageDuration) {
        VfxLayer layer = new VfxLayer();
        layer.type = string(json, "type", "");
        layer.at = number(json, "at", 0.0F);
        layer.anchor = string(json, "anchor", "entity");
        layer.offset = vec3(json, "offset");

        layer.shape = string(json, "shape", "point");
        layer.radius = number(json, "radius", 0.0F);
        if ("ring_wave".equals(layer.type)) {
            // En ring_wave "radius" es un par [inicio, fin], no un escalar.
            layer.ringRadius = pair(json, "radius", layer.ringRadius);
            layer.radius = layer.ringRadius[1];
        }
        layer.texture = string(json, "texture", "spark");
        layer.additive = "additive".equals(string(json, "blend", "alpha"));
        layer.count = json.has("count") ? json.get("count").getAsInt() : 0;
        layer.rate = Curve.parse(json.get("rate"));
        // Un emisor sin "duration" emite durante todo el stage.
        layer.duration = "emitter".equals(layer.type)
                ? number(json, "duration", stageDuration)
                : number(json, "duration", 0.2F);
        layer.lifetime = pair(json, "lifetime", layer.lifetime);
        layer.size = pair(json, "size", layer.size);
        layer.color = Gradient.parse(json.get("color"));
        layer.speed = pair(json, "speed", layer.speed);
        layer.direction = string(json, "direction", "random");
        layer.gravity = number(json, "gravity", 0.0F);
        layer.stretch = number(json, "stretch", 0.0F);
        layer.core = number(json, "core", 0.0F);
        layer.swirl = number(json, "swirl", 0.0F);
        layer.boneAxis = triple(json, "bone_axis", layer.boneAxis);

        layer.thickness = pair(json, "thickness", layer.thickness);
        layer.ease = string(json, "ease", "linear");
        layer.y = number(json, "y", 0.0F);

        // ribbon
        layer.bone = string(json, "bone", "");
        layer.width = pair(json, "width", new float[]{0.2F, 0.0F});
        if ("ribbon".equals(layer.type)) {
            layer.duration = number(json, "duration", stageDuration);
            layer.ribbonLifetime = number(json, "lifetime", 0.3F);
        }

        // telegraph: sin "duration" dura todo el stage, como un emisor.
        layer.spin = number(json, "spin", 0.0F);
        layer.fillAlpha = number(json, "fill_alpha", 0.0F);
        layer.fadeIn = number(json, "fade_in", 0.0F);
        if ("telegraph".equals(layer.type)) {
            float width = number(json, "thickness", 0.25F);
            layer.thickness = new float[]{width, width};
            layer.duration = Math.max(0.05F, number(json, "duration", stageDuration - layer.at));
        }

        // slash_arc: "width" es un escalar (en ribbon es un par).
        layer.arcDegrees = number(json, "arc_degrees", 120.0F);
        layer.tilt = number(json, "tilt", 0.0F);
        layer.arcWidth = number(json, "width", 0.6F);

        // flash
        layer.intensity = number(json, "intensity", 0.5F);
        layer.scope = string(json, "scope", "world");
        layer.flashColor = hex(string(json, "color", "#ffffff"));

        // shake
        layer.amplitude = number(json, "amplitude", 0.1F);
        layer.falloff = string(json, "falloff", "linear");
        if ("shake".equals(layer.type) && !json.has("radius")) {
            layer.radius = 16.0F;
        }
        return layer;
    }

    // telegraph
    /** Giro del anillo en radianes por segundo (el ejemplo de Miyabi usa 3,6). */
    public float spin = 0.0F;
    public float fillAlpha = 0.0F;
    public float fadeIn = 0.0F;

    // slash_arc
    public float arcDegrees = 120.0F;
    public float tilt = 0.0F;
    public float arcWidth = 0.6F;

    // flash
    public float intensity = 0.5F;
    public String scope = "world";
    public float[] flashColor = {1.0F, 1.0F, 1.0F};

    // shake
    public float amplitude = 0.1F;
    public String falloff = "linear";

    private static float[] hex(String value) {
        try {
            int rgb = Integer.parseInt(value.startsWith("#") ? value.substring(1) : value, 16);
            return new float[]{((rgb >> 16) & 255) / 255.0F, ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F};
        } catch (NumberFormatException exception) {
            return new float[]{1.0F, 1.0F, 1.0F};
        }
    }

    // ribbon
    public String bone = "";
    public float[] width = {0.2F, 0.0F};
    public float ribbonLifetime = 0.3F;

    private static String string(JsonObject json, String key, String fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : fallback;
    }

    private static float number(JsonObject json, String key, float fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsFloat() : fallback;
    }

    private static float[] pair(JsonObject json, String key, float[] fallback) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return fallback;
        }
        var array = json.get(key).getAsJsonArray();
        if (array.size() != 2) {
            return fallback;
        }
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat()};
    }

    private static float[] triple(JsonObject json, String key, float[] fallback) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return fallback;
        }
        var array = json.get(key).getAsJsonArray();
        if (array.size() != 3) {
            return fallback;
        }
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    private static Vec3 vec3(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return Vec3.ZERO;
        }
        var array = json.get(key).getAsJsonArray();
        if (array.size() != 3) {
            return Vec3.ZERO;
        }
        return new Vec3(array.get(0).getAsDouble(), array.get(1).getAsDouble(), array.get(2).getAsDouble());
    }
}
