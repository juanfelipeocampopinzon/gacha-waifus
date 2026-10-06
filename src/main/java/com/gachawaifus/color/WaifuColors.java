package com.gachawaifus.color;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Qué color le toca a cada waifu, sacado de su paleta (traje, pelo o poder) y no del elemento.
 *
 * <p>Con 11 waifus y 11 colores los colores quedan todos usados: a partir de la siguiente waifu
 * lo normal es <b>repetir</b> el color que mejor le pegue al personaje. Lo que sí conviene
 * repartir son los <b>roles</b> (Attack, Support, Anomaly, Stun, Defense).
 */
public final class WaifuColors {

    private static final Map<String, WaifuColor> BY_ID = new LinkedHashMap<>();

    static {
        // El orden es el del roster, para que sea fácil compararlo con WaifuRoster.
        put("miyabi", WaifuColor.AZUL);            // pelo y filo glaciales
        put("astra_yao", WaifuColor.AMARILLO);     // vestido blanco-dorado, aura de estrella
        put("ellen_joe", WaifuColor.GRIS);         // pelo blanco-gris, uniforme oscuro
        put("burnice_white", WaifuColor.NARANJA);  // llamas y pelo rosa-naranja
        put("ye_shunguang", WaifuColor.MARRON);    // tinta ámbar sobre blanco
        put("nicole_demara", WaifuColor.ROSA);     // pelo rosa-magenta
        put("anby_demara", WaifuColor.MORADO);     // pelo violeta
        put("ukinami_yuzuha", WaifuColor.VERDE);   // traje verde-teal
        put("promeia", WaifuColor.BLANCO);         // paleta pálida/glacial
        put("remielle", WaifuColor.NEGRO);         // cazadora del vacío
        put("tokisaki_kurumi", WaifuColor.ROJO);   // vestido negro con rojos, ojos rojo/ámbar
    }

    private WaifuColors() {
    }

    private static void put(String waifuId, WaifuColor color) {
        BY_ID.put(waifuId, color);
    }

    /** Color de una waifu por su id snake_case ({@code WaifuRoster.Entry#id()}). */
    @Nullable
    public static WaifuColor of(String waifuId) {
        return BY_ID.get(waifuId);
    }

    /** Color de una waifu a partir del tipo de entidad ya registrado. */
    @Nullable
    public static WaifuColor of(EntityType<?> type) {
        var key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key == null ? null : BY_ID.get(key.getPath());
    }

    public static Map<String, WaifuColor> all() {
        return Map.copyOf(BY_ID);
    }
}
