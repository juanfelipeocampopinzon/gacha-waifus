package com.gachawaifus.color;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Los 11 colores del sistema de clasificación del mod (sustituyen a los elementos de ZZZ).
 *
 * <p>Cada waifu tiene un color sacado de su paleta (traje, pelo o poder) y cada enemigo hostil
 * recibe uno al azar. El color decide el daño con una matriz <b>regular</b>: sobre la rueda de
 * los 11 colores, cada uno hace daño extra a los que están 4, 5 y 6 pasos por delante y daño
 * reducido a los 3 inmediatamente siguientes; los 4 restantes (y él mismo) son neutros.
 *
 * <p>Con 11 colores es imposible repartir 3+3 sin dejar 4 fuera, así que los 4 neutros son
 * estructurales, no un descuido. La matriz es regular: los 11 colores tienen exactamente
 * 3 fuertes, 3 débiles y 4 neutros, y cada color recibe 3 bonos y 3 penalizaciones.
 *
 * <p>El orden de las constantes ES la rueda: no reordenarlas sin volver a validar
 * ({@code research/colors/validate_colors.py}).
 */
public enum WaifuColor {
    ROJO("rojo", 0xFF0000, 'c'),
    NARANJA("naranja", 0xFFA500, '6'),
    AMARILLO("amarillo", 0xFFFF00, 'e'),
    VERDE("verde", 0x008000, 'a'),
    AZUL("azul", 0x0000FF, '9'),
    MORADO("morado", 0x800080, '5'),
    ROSA("rosa", 0xFFC0CB, 'd'),
    MARRON("marron", 0xA52A2A, '4'),
    NEGRO("negro", 0x000000, '0'),
    BLANCO("blanco", 0xFFFFFF, 'f'),
    GRIS("gris", 0x808080, '8');

    /** Pasos hacia delante que dan daño extra (el "lado opuesto" de la rueda). */
    private static final int[] STRONG_STEPS = {4, 5, 6};
    /** Pasos inmediatamente siguientes: contra estos el daño baja. */
    private static final int[] WEAK_STEPS = {1, 2, 3};
    /** Tamaños como constantes de compilación: las constantes del enum se crean antes que los
     *  arrays estáticos, así que la longitud no se puede leer de ellos en el inicializador. */
    private static final int STRONG_COUNT = 3;
    private static final int WEAK_COUNT = 3;

    private final String id;
    private final int rgb;
    private final ChatFormatting format;
    /** Se rellenan en el bloque estático, cuando ya existen todas las constantes. */
    private final WaifuColor[] strong = new WaifuColor[STRONG_COUNT];
    private final WaifuColor[] weak = new WaifuColor[WEAK_COUNT];

    WaifuColor(String id, int rgb, char code) {
        this.id = id;
        this.rgb = rgb;
        this.format = ChatFormatting.getByCode(code);
    }

    static {
        WaifuColor[] all = values();
        for (WaifuColor color : all) {
            for (int i = 0; i < STRONG_STEPS.length; i++) {
                color.strong[i] = all[(color.ordinal() + STRONG_STEPS[i]) % all.length];
                color.weak[i] = all[(color.ordinal() + WEAK_STEPS[i]) % all.length];
            }
        }
    }

    public String id() {
        return id;
    }

    public int rgb() {
        return rgb;
    }

    public ChatFormatting format() {
        return format;
    }

    /** "#FF0000" — el hex que pidió el usuario, tal cual. */
    public String hex() {
        return String.format("#%06X", rgb);
    }

    /** "§c" — para textos legacy (mensajes de chat y de acción). */
    public String chatCode() {
        return "\u00a7" + format.getChar();
    }

    /** Nombre del color, traducido (clave {@code color.gachawaifus.<id>}). */
    public MutableComponent displayName() {
        return Component.translatable("color.gachawaifus." + id);
    }

    /** El nombre ya pintado con su propio color. */
    public MutableComponent styledName() {
        return displayName().withStyle(format);
    }

    public WaifuColor[] strong() {
        return strong.clone();
    }

    public WaifuColor[] weak() {
        return weak.clone();
    }

    public boolean isStrongAgainst(WaifuColor other) {
        for (WaifuColor c : strong) {
            if (c == other) return true;
        }
        return false;
    }

    public boolean isWeakAgainst(WaifuColor other) {
        for (WaifuColor c : weak) {
            if (c == other) return true;
        }
        return false;
    }

    public static int count() {
        return values().length;
    }

    /** El color que toca a ese índice de la rueda (acepta cualquier entero, también negativos). */
    public static WaifuColor byIndex(int index) {
        WaifuColor[] all = values();
        return all[Math.floorMod(index, all.length)];
    }

    /** Color aleatorio, con el generador que le pases (los enemigos usan su UUID). */
    public static WaifuColor random(java.util.Random random) {
        return byIndex(random.nextInt(count()));
    }

    @Nullable
    public static WaifuColor byId(@Nullable String id) {
        if (id != null) {
            for (WaifuColor color : values()) {
                if (color.id.equals(id)) return color;
            }
        }
        return null;
    }

    public static WaifuColor byIdOr(String id, WaifuColor fallback) {
        WaifuColor color = byId(id);
        return color != null ? color : fallback;
    }

    /** "§9Azul§7, §5Morado§7, §dRosa" — cada nombre con su propio color. */
    public static MutableComponent names(WaifuColor[] colors) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < colors.length; i++) {
            if (i > 0) out.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            out.append(colors[i].styledName());
        }
        return out;
    }
}
