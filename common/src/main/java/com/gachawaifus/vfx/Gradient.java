package com.gachawaifus.vfx;

import com.google.gson.JsonElement;

/**
 * Degradado del JSON: {@code [[0,"#rrggbb",alfa], ...]} con t ascendente de 0 a 1.
 * Muestrea color y alfa en un punto t.
 */
public final class Gradient {

    /** Fallback cuando la capa no trae color: blanco que se apaga. */
    public static final Gradient WHITE = new Gradient(new float[][]{
            {0.0F, 1.0F, 1.0F, 1.0F, 1.0F},
            {1.0F, 1.0F, 1.0F, 1.0F, 0.0F}
    });

    /** Cada parada: {t, r, g, b, a}; el color y el alfa en 0..1. */
    private final float[][] stops;

    private Gradient(float[][] stops) {
        this.stops = stops;
    }

    /** Devuelve null si el JSON no trae un degradado válido (el llamante decide el fallback). */
    public static Gradient parse(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return null;
        }
        var array = element.getAsJsonArray();
        if (array.size() == 0) {
            return null;
        }
        float[][] stops = new float[array.size()][];
        for (int i = 0; i < array.size(); i++) {
            var stop = array.get(i).getAsJsonArray();
            String hex = stop.get(1).getAsString();
            int rgb = Integer.parseInt(hex.substring(1), 16);
            stops[i] = new float[]{
                    stop.get(0).getAsFloat(),
                    ((rgb >> 16) & 0xFF) / 255.0F,
                    ((rgb >> 8) & 0xFF) / 255.0F,
                    (rgb & 0xFF) / 255.0F,
                    stop.get(2).getAsFloat()
            };
        }
        return new Gradient(stops);
    }

    /** {r, g, b, a} en 0..1 para el tiempo normalizado t (se recorta a 0..1). */
    public float[] sample(float t) {
        if (t <= stops[0][0]) {
            return copy(stops[0]);
        }
        for (int i = 1; i < stops.length; i++) {
            if (t <= stops[i][0]) {
                float[] a = stops[i - 1];
                float[] b = stops[i];
                float span = b[0] - a[0];
                float k = span <= 1.0E-5F ? 0.0F : (t - a[0]) / span;
                return new float[]{
                        a[1] + (b[1] - a[1]) * k,
                        a[2] + (b[2] - a[2]) * k,
                        a[3] + (b[3] - a[3]) * k,
                        a[4] + (b[4] - a[4]) * k
                };
            }
        }
        return copy(stops[stops.length - 1]);
    }

    private static float[] copy(float[] stop) {
        return new float[]{stop[1], stop[2], stop[3], stop[4]};
    }
}
