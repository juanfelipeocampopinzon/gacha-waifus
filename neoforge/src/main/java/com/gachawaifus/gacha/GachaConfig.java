package com.gachawaifus.gacha;

import com.gachawaifus.team.TeamConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;

/**
 * Sección {@code husbands} de la configuración COMMON ({@code config/gachawaifus-common.toml}).
 *
 * <p>La opción viene <b>apagada</b>: mientras no se encienda, los maridos salen del gacha como siempre.
 * Al encenderla quedan fuera de <b>todo</b> el sorteo (destacada del día, 4★, consuelo del 50/50 y
 * tirada doble), pero no desaparecen del mod: siguen en la rotación a efectos de colección, se invocan
 * con su token y se doman salvajes. Lo que se apaga es la ruleta, no el personaje.
 *
 * <p>La lista de ids es editable ahí mismo, en el toml: quien quiera dejar fuera a alguna más (o
 * devolverle una a la ruleta) solo tiene que escribir o quitar el id y recargar el servidor.
 */
public final class GachaConfig {

    /** Activa la exclusión. Apagada por defecto: por defecto salen todos. */
    public static ModConfigSpec.BooleanValue EXCLUDE_FROM_GACHA;

    /** Los ids que se excluyen cuando {@link #EXCLUDE_FROM_GACHA} está encendida. */
    public static ModConfigSpec.ConfigValue<List<? extends String>> IDS;

    /**
     * Por defecto van los masculinos del roster, los mismos que el mod construye con el modelo
     * ANCHO sin pieza del pecho: Anton, Banyue, Ben Bigger, Billy Kid, Harumasa, Hugo Vlad, Lighter,
     * Manato, Pan Yinhu, Pyrois, Seth Lowell y Von Lycaon. Las waifus femeninas (aunque sean
     * corpulentas) no entran: es más fácil añadir una línea al toml que devolver a la ruleta una
     * waifu que se sacó de ella sin querer.
     */
    private static final List<String> DEFAULT_IDS = List.of(
            "von_lycaon", "billy_kid", "manato", "harumasa", "hugo_vlad", "pan_yinhu", "anton",
            "ben_bigger", "seth_lowell", "lighter", "banyue", "pyrois");

    /** La última lista leída y su conjunto ya resuelto, para no reconstruirlo en cada tirada. */
    private static List<? extends String> cachedRaw;
    private static Set<String> cachedIds = Set.copyOf(DEFAULT_IDS);

    /** Añade la sección {@code husbands} al builder COMMON del mod. Lo llama {@link TeamConfig}. */
    public static void appendTo(ModConfigSpec.Builder builder) {
        builder.push("husbands");
        EXCLUDE_FROM_GACHA = builder
                .comment("Apaga a los maridos del gacha: nunca salen en el banner rosa ni en el"
                        + " permanente, ni como destacada del dia, ni de consuelo del 50/50, ni en la"
                        + " tirada doble. Desactivado por defecto: con la opcion apagada salen todos.")
                .comment("Los excluidos siguen invocandose con su token, siguen en la Capsula y en el"
                        + " glosario y siguen apareciendo salvajes: solo se va la ruleta.")
                .define("exclude_from_gacha", false);
        IDS = builder
                .comment("Ids de las waifus excluidas (secciones del roster, p. ej. \"von_lycaon\")."
                        + " Editala para anadir o quitar maridos; solo se usa si arriba esta activado.")
                // El tercer argumento es lo que NeoForge propone al pulsar "anadir" en la pantalla
                // de config: el primer id de la lista, para no inventarse uno que no exista. El
                // validador rechaza en cualquier caso las entradas vacias.
                .defineList("ids", () -> DEFAULT_IDS, () -> "von_lycaon",
                        obj -> obj instanceof String s && !s.isBlank());
        builder.pop();
    }

    private GachaConfig() {
    }

    private static boolean loaded() {
        return TeamConfig.SPEC.isLoaded() && EXCLUDE_FROM_GACHA != null && IDS != null;
    }

    /** ¿Está la exclusión activada? Sin config cargada (arranque), no. */
    public static boolean excluded() {
        return loaded() && Boolean.TRUE.equals(EXCLUDE_FROM_GACHA.get());
    }

    /** Los ids excluidos, ya como conjunto. */
    public static Set<String> ids() {
        List<? extends String> raw = loaded() ? IDS.get() : DEFAULT_IDS;
        if (!raw.equals(cachedRaw)) {
            cachedRaw = List.copyOf(raw);
            cachedIds = Set.copyOf(raw);
        }
        return cachedIds;
    }
}
