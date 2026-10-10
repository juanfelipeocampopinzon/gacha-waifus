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
        put("ukinami_yuzuha", WaifuColor.VERDE);    // traje verde-teal
        put("promeia", WaifuColor.BLANCO);         // paleta pálida/glacial
        put("remielle", WaifuColor.NEGRO);         // cazadora del vacío
        put("tokisaki_kurumi", WaifuColor.ROJO);   // vestido negro con rojos, ojos rojo/ámbar
        put("koleda", WaifuColor.ROJO);                // cabello y efectos de fuego
        put("grace_howard", WaifuColor.AZUL);      // Azul / Anomaly (Electricidad)
        put("yidhari", WaifuColor.AZUL);   // color primario de su elemento de Hielo
        put("von_lycaon", WaifuColor.AZUL);   // color primario de su elemento de Hielo
        put("rina", WaifuColor.ROSA);
        put("soldier_11", WaifuColor.NARANJA);
        put("tobichi_origami", WaifuColor.BLANCO);
        put("nekomata", WaifuColor.BLANCO);      // pelo blanco, cola y orejas de gata
        put("billy_kid", WaifuColor.GRIS);
        put("yixuan", WaifuColor.VERDE);           // tinta áurica jade de Yunkui
        put("zhu_yuan", WaifuColor.AZUL);          // uniforme del NEPS (Seguridad Pública)
        put("qingyi", WaifuColor.AZUL);
        put("pyrois", WaifuColor.BLANCO);
        put("norma", WaifuColor.NARANJA);
        put("velina", WaifuColor.VERDE);
        put("sigrid", WaifuColor.AZUL);
        put("claret", WaifuColor.ROJO);
        put("roxy", WaifuColor.GRIS);
        put("orphie", WaifuColor.ROJO);
        put("seed", WaifuColor.NEGRO);
        put("manato", WaifuColor.NARANJA);
        put("lucia", WaifuColor.MORADO);
        put("banyue", WaifuColor.NARANJA);
        put("zhao", WaifuColor.BLANCO);
        put("aria", WaifuColor.ROSA);
        put("sunna", WaifuColor.BLANCO);
        put("cissia", WaifuColor.VERDE);
        put("nangong_yu", WaifuColor.AZUL);
        put("tsukishiro_yanagi", WaifuColor.MORADO);
        put("harumasa", WaifuColor.BLANCO);
        put("evelyn_chevalier", WaifuColor.ROJO);
        put("pulchra", WaifuColor.MARRON);
        put("trigger", WaifuColor.GRIS);
        put("hugo_vlad", WaifuColor.NEGRO);
        put("vivian", WaifuColor.MORADO);
        put("ju_fufu", WaifuColor.ROJO);
        put("pan_yinhu", WaifuColor.MARRON);
        put("alice", WaifuColor.ROSA);
        put("anton", WaifuColor.AZUL);
        put("ben_bigger", WaifuColor.NARANJA);
        put("corin_wickes", WaifuColor.GRIS);
        put("lucy", WaifuColor.AMARILLO);
        put("piper_wheel", WaifuColor.MARRON);
        put("soukaku", WaifuColor.BLANCO);
        put("seth_lowell", WaifuColor.AZUL);
        put("jane_doe", WaifuColor.NEGRO);
        put("caesar", WaifuColor.ROJO);
        put("lighter", WaifuColor.NARANJA);
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