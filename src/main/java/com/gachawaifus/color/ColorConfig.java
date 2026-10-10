package com.gachawaifus.color;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuración de cliente del sistema de colores: se cambia desde el menú Mods → Config
 * (o editando {@code config/gachawaifus-client.toml}), sin recompilar.
 *
 * <p>Solo gobierna la etiqueta flotante del color. El resto de interruptores (matriz, aura,
 * avisos…) siguen siendo constantes de compilación en {@link ColorSettings}.
 */
public final class ColorConfig {

    public static final ModConfigSpec SPEC;

    /** ¿Se muestra el cuadradito de color sobre los mobs hostiles? */
    public static final ModConfigSpec.BooleanValue FLOATING_LABEL;

    /** A qué distancia (bloques) se muestra la etiqueta; más lejos, no se dibuja. */
    public static final ModConfigSpec.IntValue LABEL_RANGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("floating_label");
        FLOATING_LABEL = builder
                .comment("Etiqueta flotante con el color del mob hostil (el cuadradito sobre su cabeza).")
                .define("enabled", true);
        LABEL_RANGE = builder
                .comment("Rango en bloques hasta el que se muestra la etiqueta.",
                        "Por debajo se ve a tamano de nametag; mas alla no se dibuja.")
                .defineInRange("range", 64, 16, 256);
        builder.pop();
        SPEC = builder.build();
    }

    private ColorConfig() {
    }
}
