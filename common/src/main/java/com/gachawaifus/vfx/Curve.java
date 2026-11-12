package com.gachawaifus.vfx;

import com.google.gson.JsonElement;
import net.minecraft.util.Mth;

/**
 * Curva de emisión: un número fijo (partículas por segundo) o {@code [[t,valor], ...]}.
 * El t es local a la capa (0 = cuando empieza, 1 = cuando termina).
 */
public final class Curve {

    private final float[] times;   // null => constante
    private final float[] values;

    private Curve(float[] times, float[] values) {
        this.times = times;
        this.values = values;
    }

    public static Curve of(float constant) {
        return new Curve(null, new float[]{constant});
    }

    public static Curve parse(JsonElement element) {
        if (element == null) {
            return null;
        }
        if (element.isJsonPrimitive()) {
            return of(element.getAsFloat());
        }
        if (!element.isJsonArray()) {
            return null;
        }
        var array = element.getAsJsonArray();
        if (array.size() < 2) {
            return null;
        }
        float[] times = new float[array.size()];
        float[] values = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            var point = array.get(i).getAsJsonArray();
            times[i] = point.get(0).getAsFloat();
            values[i] = point.get(1).getAsFloat();
        }
        return new Curve(times, values);
    }

    public float sample(float t) {
        if (times == null) {
            return values[0];
        }
        if (t <= times[0]) {
            return values[0];
        }
        for (int i = 1; i < times.length; i++) {
            if (t <= times[i]) {
                float span = times[i] - times[i - 1];
                float k = span <= 1.0E-5F ? 0.0F : (t - times[i - 1]) / span;
                return Mth.lerp(k, values[i - 1], values[i]);
            }
        }
        return values[values.length - 1];
    }
}
