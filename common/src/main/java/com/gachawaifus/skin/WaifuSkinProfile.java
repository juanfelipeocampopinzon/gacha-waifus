package com.gachawaifus.skin;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Las skins de una waifu y en qué estados se usan.
 *
 * <p>Solo {@link #base()} es obligatoria. Una waifu puede definir una, dos o las tres variantes:
 * <ul>
 *   <li>{@link #combat()}: se pone al entrar en combate (tener objetivo o recibir un golpe) y se
 *       queda unos segundos al salir. Es la que usa Promeia.</li>
 *   <li>{@link #special()}: se enciende al usar la habilidad especial y dura unos segundos.</li>
 *   <li>{@link #ultimate()}: se enciende al lanzar la ultimate y dura unos segundos.</li>
 * </ul>
 *
 * <p>Si una variante no está definida, {@link #textureFor(WaifuSkinState)} devuelve la base; así
 * una waifu con solo la skin de ultimate no parpadea a una textura inexistente cuando entra en
 * combate. La prioridad cuando coinciden dos estados es ULTIMATE &gt; SPECIAL &gt; COMBAT.
 */
public record WaifuSkinProfile(
        ResourceLocation base,
        @Nullable ResourceLocation combat,
        @Nullable ResourceLocation special,
        @Nullable ResourceLocation ultimate) {

    /** Textura que toca pintar para el estado dado. */
    public ResourceLocation textureFor(WaifuSkinState state) {
        return switch (state) {
            case COMBAT -> this.combat != null ? this.combat : this.base;
            case SPECIAL -> this.special != null ? this.special : this.base;
            case ULTIMATE -> this.ultimate != null ? this.ultimate : this.base;
            case BASE -> this.base;
        };
    }
}
