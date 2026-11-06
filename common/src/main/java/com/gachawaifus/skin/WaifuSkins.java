package com.gachawaifus.skin;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registro central de las skins alternativas del mod.
 *
 * <p>Cada waifu se identifica por el id de su entidad (sin el namespace), el mismo que usa
 * {@code WaifuRoster}. Añadir una transformación es una línea aquí y los PNG en
 * {@code textures/entity/}; el renderizador solo tiene que llamar a
 * {@link AbstractWaifuEntity#skinTexture(ResourceLocation)}.
 *
 * <pre>{@code
 * register("mi_waifu", new WaifuSkinProfile(
 *         texture("mi_waifu"),
 *         null,                         // sin skin de combate
 *         texture("mi_waifu_skill"),    // se enciende al usar la especial
 *         null));                       // sin skin de ultimate
 * }</pre>
 *
 * <p>Las waifus que no aparecen aquí siguen con su textura fija de siempre: el registro no cambia
 * nada para ellas.
 */
public final class WaifuSkins {

    private static final Map<ResourceLocation, WaifuSkinProfile> PROFILES = new HashMap<>();

    static {
        // Promeia: skin base nueva en pasivo y forma de combate al tener objetivo.
        register("promeia", new WaifuSkinProfile(
                texture("promeia"),
                texture("promeia_combat"),
                null,
                null));

        // Ye Shunguang: la forma blanca (pelo claro) se enciende con la ultimate. Su glowmask
        // solo cubre las partes blancas del pelo y las colas.
        register("ye_shunguang", new WaifuSkinProfile(
                texture("ye_shunguang"),
                null,
                null,
                texture("ye_shunguang_ultimate")));

        // Aria: la forma robot es su transformación de ultimate. La skin original venía en 64x64
        // y se convirtió a 64x128 con la pieza del pecho en la fila UV (0, 121).
        register("aria", new WaifuSkinProfile(
                texture("aria"),
                null,
                null,
                texture("aria_robot")));
    }

    /** Da de alta (o reemplaza) el perfil de una waifu por su id de entidad. */
    public static void register(String waifuId, WaifuSkinProfile profile) {
        PROFILES.put(ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, waifuId), profile);
    }

    /** Perfil de una waifu, o {@code null} si no tiene skins alternativas. */
    @Nullable
    public static WaifuSkinProfile profile(@Nullable ResourceLocation entityId) {
        return entityId == null ? null : PROFILES.get(entityId);
    }

    /** Perfil de esta entidad, resuelto por su tipo registrado. */
    @Nullable
    public static WaifuSkinProfile profile(AbstractWaifuEntity entity) {
        return profile(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    /**
     * Textura que debe pintar el renderizador. Si la waifu no tiene perfil (o el estado actual no
     * define variante) devuelve {@code fallback}, que es la textura fija del renderizador.
     */
    public static ResourceLocation textureFor(AbstractWaifuEntity entity, ResourceLocation fallback) {
        WaifuSkinProfile profile = profile(entity);
        return profile == null ? fallback : profile.textureFor(entity.skinState());
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID,
                "textures/entity/" + name + ".png");
    }

    private WaifuSkins() {
    }
}
