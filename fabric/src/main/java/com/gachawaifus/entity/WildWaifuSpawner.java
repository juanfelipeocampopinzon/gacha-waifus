package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.gacha.WaifuRoster;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Waifus salvajes por bioma: de vez en cuando nace una sin dueño, deambulando por un clima que
 * encaja con su color (la clasificación del mod es el color, así que el color decide su terreno).
 *
 * <p>Se genera desde Java y no con {@code neoforge:add_spawns} (el arreglo de los jefes) por tres
 * razones: el tope por zona, la probabilidad y el intervalo se pueden mover en la config sin
 * recompilar; los JSON repartirían las 59 waifus por igual en todos los biomas listados; y las
 * reglas de spawn vanilla de un {@code CREATURE} exigirían hierba y luz, lo que dejaría fuera
 * desiertos de arena y nieves.
 *
 * <p>El ciclo es: cada {@link WildWaifuConfig#interval()} ticks se tira el dado
 * ({@link WildWaifuConfig#chance()} %) por jugador conectado; si pasa, se busca un sitio libre en el
 * anillo de 28-64 bloques y se invoca una waifu del clima de ese punto. Nunca se pone a los pies del
 * jugador: aparece de lejos, como algo que estás encontrando.
 */
public final class WildWaifuSpawner {

    /** Radio máximo de búsqueda: la waifu nace lejos, no encima del jugador. */
    private static final double MAX_DISTANCE = 64.0D;
    /** Radio mínimo: fuera de esta distancia el spawn no interrumpe la construcción ni el combate. */
    private static final double MIN_DISTANCE = 28.0D;
    /** Radio en el que se cuentan las waifus salvajes que ya hay para respetar el tope. */
    private static final double COUNT_DISTANCE = 96.0D;
    /** Puntos probados por tirada antes de rendirse (suelen sobrar 2 o 3). */
    private static final int SPOT_ATTEMPTS = 16;
    /**
     * Cuánto se baja por columna buscando un hueco pisable antes de descartarla. 128 es para el
     * Nether: allí el heightmap responde con el techo de bedrock, y entre el techo (127) y el suelo
     * de lava (31) hay 96 bloques de aire que hay que atravesar.
     */
    private static final int SCAN_DEPTH = 128;

    /**
     * Terreno donde deambula cada color. Público porque {@code /gwcolor biomas} lo lee: la tabla
     * esta única vez que se escribe y el comando la enseña tal cual la usa el spawn.
     */
    public enum Climate {
        FRIO("clima frío", "nieve, tundras y montañas"),
        TEMPLADO("clima templado", "bosques, llanuras, ríos y junglas"),
        CALIDO("clima cálido", "desiertos, sabanas y mesetas"),
        NETHER("el Nether", "cualquier bioma del Nether"),
        END("el End", "las islas de End y sus costas");

        private final String name;
        private final String where;

        Climate(String name, String where) {
            this.name = name;
            this.where = where;
        }

        /** Cómo se llama el clima en los mensajes. */
        public String label() {
            return this.name;
        }

        /** Dónde se encuentra, en palabras. */
        public String where() {
            return this.where;
        }
    }

    /**
     * Qué colores pasean por cada clima. Los fríos se llevan los pálidos y glaciales, los cálidos los
     * de fuego y tierra, y el Vacío se queda con lo más oscuro y raro.
     */
    private static final Map<Climate, WaifuColor[]> FAUNA = new EnumMap<>(Climate.class);

    static {
        FAUNA.put(Climate.FRIO, new WaifuColor[]{WaifuColor.BLANCO, WaifuColor.AZUL, WaifuColor.GRIS});
        FAUNA.put(Climate.TEMPLADO, new WaifuColor[]{WaifuColor.VERDE, WaifuColor.ROSA,
                WaifuColor.MORADO, WaifuColor.BLANCO});
        FAUNA.put(Climate.CALIDO, new WaifuColor[]{WaifuColor.ROJO, WaifuColor.NARANJA,
                WaifuColor.AMARILLO, WaifuColor.MARRON});
        FAUNA.put(Climate.NETHER, new WaifuColor[]{WaifuColor.ROJO, WaifuColor.NARANJA,
                WaifuColor.MARRON, WaifuColor.NEGRO});
        FAUNA.put(Climate.END, new WaifuColor[]{WaifuColor.MORADO, WaifuColor.NEGRO,
                WaifuColor.GRIS, WaifuColor.AZUL});
    }

    /** Pool por clima, resuelto la primera vez que se necesita (los EntityType aún no existen al cargar la clase). */
    private static Map<Climate, List<WaifuRoster.Entry>> pool;

    private static long tickCounter;

    private WildWaifuSpawner() {
    }

    /** Registra el callback de tick del servidor (llamado desde {@link com.gachawaifus.GachaWaifusMod#onInitialize()}). */
    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(WildWaifuSpawner::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        if (!WildWaifuConfig.active()) {
            return;
        }
        tickCounter++;
        if (tickCounter % Math.max(20, WildWaifuConfig.interval()) != 0) {
            return;
        }
        int chance = WildWaifuConfig.chance();
        if (chance <= 0) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getRandom().nextInt(100) < chance) {
                trySpawnNear(player);
            }
        }
    }

    private static void trySpawnNear(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }
        AABB area = player.getBoundingBox().inflate(COUNT_DISTANCE);
        List<AbstractWaifuEntity> waifusCerca = new ArrayList<>(
                level.getEntitiesOfClass(AbstractWaifuEntity.class, area));
        List<AbstractWaifuEntity> salvajes = new ArrayList<>();
        for (AbstractWaifuEntity cerca : waifusCerca) {
            if (WildWaifus.isWild(cerca)) {
                salvajes.add(cerca);
            }
        }
        Set<String> poseidas = poseidas(player, waifusCerca);
        int tope = WildWaifuConfig.maxNearby();
        RandomSource random = level.getRandom();

        // Nacen en manada, como los animales: de una en una el mundo siempre parece vacío.
        for (int i = 0, manada = 2 + random.nextInt(2); i < manada && salvajes.size() < tope; i++) {
            for (int attempt = 0; attempt < SPOT_ATTEMPTS; attempt++) {
                BlockPos spot = randomSurfaceSpot(level, player.blockPosition(), random);
                if (spot == null) {
                    continue;
                }
                WaifuRoster.Entry entry = pickEntry(level, spot, random, salvajes, poseidas);
                if (entry == null) {
                    continue;
                }
                AbstractWaifuEntity waifu = entry.type().get().create(level);
                if (waifu == null) {
                    continue;
                }
                waifu.moveTo(spot.getX() + 0.5D, spot.getY(), spot.getZ() + 0.5D,
                        random.nextFloat() * 360.0F, 0.0F);
                WildWaifus.markWild(waifu);
                level.addFreshEntity(waifu);
                salvajes.add(waifu);
                break; // esta ya está en el mundo: toca la siguiente de la manada
            }
        }
    }

    /**
     * Punto pisable en el anillo [28, 64] alrededor del jugador, o {@code null} si la columna no
     * tiene ninguno.
     *
     * <p>Se baja desde la capa de altura hasta encontrar un hueco de dos bloques con suelo firme,
     * porque la altura de vanilla miente en tres sitios: en el Nether devuelve por encima del techo
     * de bedrock (las waifus nacerían fuera del mapa), bajo un dosel de jungla cae encima de los
     * troncos, y en un océano se queda en la superficie del agua.
     */
    private static BlockPos randomSurfaceSpot(ServerLevel level, BlockPos center, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = MIN_DISTANCE + random.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
        int x = center.getX() + Mth.floor(Math.cos(angle) * distance);
        int z = center.getZ() + Mth.floor(Math.sin(angle) * distance);
        // MOTION_BLOCKING_NO_LEAVES: la primera capa libre cuenta desde el suelo, sin que las hojas
        // de los árboles la suban (si no, las waifus nacerían arriba de la jungla).
        int techo = level.getMaxBuildHeight() - 4;
        int top = Math.min(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), techo);
        int fondo = Math.max(level.getMinBuildHeight(), top - SCAN_DEPTH);
        for (int y = top; y > fondo; y--) {
            BlockPos spot = new BlockPos(x, y, z);
            if (isStandable(level, spot)) {
                return spot;
            }
        }
        return null;
    }

    private static boolean isStandable(LevelReader level, BlockPos pos) {
        BlockState floor = level.getBlockState(pos.below());
        if (floor.isAir() || !floor.getFluidState().isEmpty()) {
            return false;
        }
        // El agua se rechaza: el hueco está pisable pero la waifu se ahogaría ahí mismo.
        return level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                && !level.isWaterAt(pos) && !level.isWaterAt(pos.above());
    }

    /** Elige una waifu del clima del punto, sin repetir una salvaje de la zona ni algo que ya sea tuyo. */
    private static WaifuRoster.Entry pickEntry(ServerLevel level, BlockPos pos, RandomSource random,
                                               List<AbstractWaifuEntity> salvajes, Set<String> poseidas) {
        WaifuRoster.Entry elegida = alAzar(
                libres(pool().get(climateOf(level.getBiome(pos), pos)), salvajes, poseidas), random);
        if (elegida == null) {
            // Ninguna del clima está libre (o el clima no tiene fauna apuntada): se tira del
            // listado completo, que así ningún bioma se queda sin waifus salvajes por casualidad.
            elegida = alAzar(libres(WaifuRoster.ROTATION, salvajes, poseidas), random);
        }
        return elegida;
    }

    /**
     * Ids que el jugador ya tiene y que no deben volver a nacer de salvaje: {@link WildWaifus} no
     * deja domar una waifu repetida, así que solo quitarían un hueco a la manada.
     *
     * <p>Es {@link WaifuRoster#ownedIds}, que resuelve todo con lo que ya está en memoria (inventario,
     * Cápsula, registro de doma y las waifus visibles de la zona) de una sola pasada. A propósito no
     * usa {@link WaifuRoster#owns}, que escanea 128 bloques por entrada: multiplicado por las 62 del
     * roster salía un barrido caro cada pocos segundos.
     */
    private static Set<String> poseidas(ServerPlayer player, List<AbstractWaifuEntity> waifusCerca) {
        return WaifuRoster.ownedIds(player, waifusCerca);
    }

    /** Candidatas que aún no están paseando por la zona y que el jugador todavía no tiene. */
    private static List<WaifuRoster.Entry> libres(@Nullable List<WaifuRoster.Entry> candidates,
                                                  List<AbstractWaifuEntity> salvajes, Set<String> poseidas) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<WaifuRoster.Entry> libres = new ArrayList<>(candidates.size());
        for (WaifuRoster.Entry entry : candidates) {
            if (poseidas.contains(entry.id())) {
                continue;
            }
            boolean repetida = false;
            for (AbstractWaifuEntity existing : salvajes) {
                if (existing.getType() == entry.type().get()) {
                    repetida = true;
                    break;
                }
            }
            if (!repetida) {
                libres.add(entry);
            }
        }
        return libres;
    }

    @Nullable
    private static WaifuRoster.Entry alAzar(List<WaifuRoster.Entry> entries, RandomSource random) {
        return entries.isEmpty() ? null : entries.get(random.nextInt(entries.size()));
    }

    /**
     * Clima del bioma: primero la dimensión (nether/end), luego nieve y por último temperatura.
     *
     * <p>Recibe el {@code Holder} y la posición por separado (en vez de leerlos del nivel) para que
     * el comando pueda clasificar biomas del registro sin estar parado encima de ellos. En ese caso
     * vale cualquier {@code BlockPos}: la nieve y la temperatura base no dependen del punto.
     */
    public static Climate climateOf(Holder<Biome> biome, BlockPos pos) {
        if (biome.is(BiomeTags.IS_NETHER)) {
            return Climate.NETHER;
        }
        if (biome.is(BiomeTags.IS_END)) {
            return Climate.END;
        }
        if (biome.value().getPrecipitationAt(pos) == Biome.Precipitation.SNOW) {
            return Climate.FRIO;
        }
        // 1.0 es la frontera que usa vanilla: por encima van desierto, sabana y meseta.
        return biome.value().getBaseTemperature() >= 1.0F ? Climate.CALIDO : Climate.TEMPLADO;
    }

    /** Colores que pasean por ese clima: la misma tabla con la que nacen las salvajes. */
    public static WaifuColor[] faunaOf(Climate climate) {
        return FAUNA.get(climate);
    }

    /**
     * Waifus que pueden nacer en ese clima, resuelta por el color registrado de cada una. Se lee del
     * pool real del spawn, así que el comando no cuenta nada distinto de lo que luego pasa en el mundo.
     */
    public static List<WaifuRoster.Entry> waifusOf(Climate climate) {
        return pool().getOrDefault(climate, List.of());
    }

    /** Waifus por clima, sacadas de la rotación completa según su color registrado. */
    private static Map<Climate, List<WaifuRoster.Entry>> pool() {
        if (pool != null) {
            return pool;
        }
        Map<Climate, List<WaifuRoster.Entry>> built = new EnumMap<>(Climate.class);
        for (Climate climate : Climate.values()) {
            built.put(climate, new ArrayList<>());
        }
        for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
            // Sin AttributeSupplier registrado la entidad nace con los atributos vacíos y revienta
            // al primer tick (fue el caso de Burnice). Mejor no invocar a esa waifu que tumbar el
            // servidor entero desde el bucle de spawn.
            if (!DefaultAttributes.hasSupplier(entry.type().get())) {
                GachaWaifusMod.LOGGER.warn("Waifu salvaje descartada, faltan sus atributos: {}",
                        entry.id());
                continue;
            }
            WaifuColor color = WaifuColors.of(entry.type().get());
            for (Climate climate : Climate.values()) {
                // Sin color registrado (una waifu nueva aún sin tabla) se queda en terreno templado,
                // que es el bioma más común del overworld.
                boolean viveAqui = color == null ? climate == Climate.TEMPLADO : contains(FAUNA.get(climate), color);
                if (viveAqui) {
                    built.get(climate).add(entry);
                }
            }
        }
        pool = built;
        return pool;
    }

    private static boolean contains(WaifuColor[] colors, WaifuColor color) {
        for (WaifuColor candidate : colors) {
            if (candidate == color) {
                return true;
            }
        }
        return false;
    }
}
