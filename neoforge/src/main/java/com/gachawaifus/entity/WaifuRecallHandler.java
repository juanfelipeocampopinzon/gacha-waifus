package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Auto-recall: recoge a las waifus que se han quedado atrás.
 *
 * <p>El seguimiento de vanilla se cae en tres casos, y los tres son lo que se veía como «mi waifu se
 * perdió»:
 * <ul>
 *   <li><b>El dueño está en otra dimensión.</b> {@code getOwner()} busca al jugador <i>en el nivel de la
 *       mascota</i>, así que devuelve {@code null} y el {@code FollowOwnerGoal} ni arranca: la waifu se
 *       queda plantada en el Overworld para siempre.</li>
 *   <li><b>No hay camino.</b> El goal teletransporta al dueño, pero solo mientras él mismo corre; si la
 *       waifu se atasca contra una pared de su propia base, deja de intentarlo.</li>
 *   <li><b>El chunk de la waifu no estaba cargado</b> mientras el jugador se alejaba: no tickó, no se
 *       teletransportó y al volver sigue ahí.</li>
 * </ul>
 *
 * <p>Política del barrido: una waifu domada cuyo dueño está conectado se considera perdida si está más
 * lejos de {@link RecallConfig#distance()} de su destino (su dueño, o su hogar si está en modo
 * {@link WaifuMode#EN_CASA}) <b>y</b> lleva dos barridos seguidos sin moverse un bloque. La segunda
 * condición es la que evita jalar a la que todavía va caminando hacia ti. Al cargar el chunk se comprueba
 * solo el cruce de dimensiones, que es el único caso en el que nadie la iba a mover.
 *
 * <p>Corre solo en el servidor: {@link WaifuTravel#moveToNear} usa {@code changeDimension}, que conserva
 * el UUID y todo el NBT (hogar, modo y dueño viajan con ella).
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class WaifuRecallHandler {

    /** Barrido cada 100 ticks (5 segundos): recorre todas las dimensiones, no se hace más seguido. */
    private static final int SCAN_TICKS = 100;

    /** Barridos seguidos quieta antes de darla por perdida (la que camina mueve más de un bloque). */
    private static final int STUCK_SCANS = 2;

    /** Desplazamiento máximo entre barridos para contar como «no se ha movido». */
    private static final double STILL_EPSILON = 1.0D;

    private static int tickCounter;

    // ------------------------------------------------------------------ API pública

    /** Todas las waifus invocadas (vivas y con el chunk cargado) de un jugador, en cualquier dimensión. */
    public static List<AbstractWaifuEntity> waifusOf(MinecraftServer server, UUID owner) {
        List<AbstractWaifuEntity> found = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof AbstractWaifuEntity waifu
                        && waifu.isAlive()
                        && owner.equals(waifu.getOwnerUUID())) {
                    found.add(waifu);
                }
            }
        }
        return found;
    }

    /**
     * Trae a todas las waifus invocadas del jugador a su lado, aunque estén en otra dimensión. Es lo que
     * hace la Baliza de Hogar con un clic normal.
     *
     * @return cuántas se han movido.
     */
    public static int recallAll(ServerPlayer player) {
        int moved = 0;
        for (AbstractWaifuEntity waifu : waifusOf(player.serverLevel().getServer(), player.getUUID())) {
            if (waifu.recallTo(player)) {
                forget(waifu);
                moved++;
            }
        }
        return moved;
    }

    /**
     * Fija el hogar de las waifus invocadas del jugador en un bloque de su dimensión.
     *
     * @param only una waifu concreta, o {@code null} para todas las suyas.
     * @return cuántas quedaron con el hogar puesto.
     */
    public static int setHome(ServerPlayer player, BlockPos pos, @Nullable AbstractWaifuEntity only) {
        int set = 0;
        for (AbstractWaifuEntity waifu : waifusOf(player.serverLevel().getServer(), player.getUUID())) {
            if (only == null || waifu == only) {
                waifu.setHome(pos, player.level().dimension());
                set++;
            }
        }
        return set;
    }

    // ----------------------------------------------------------------------- eventos

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!RecallConfig.enabled()) {
            return; // recall.enabled = false: la Baliza de Hogar sigue funcionando, solo se apaga el barrido
        }
        if (++tickCounter < SCAN_TICKS) {
            return;
        }
        tickCounter = 0;
        scan(event.getServer());
    }

    /**
     * Al entrar una waifu al mundo (chunk cargado o cambio de dimensión) se ancla el rastro de posición
     * en su sitio real: sin esto el primer barrido la vería «movida desde la nada» y le regalaría un
     * ciclo. No se actúa dentro del evento porque {@code changeDimension} destruiría a la entidad mientras
     * el nivel la está añadiendo; el barrido la recoge en los siguientes 10 segundos como mucho.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof AbstractWaifuEntity waifu)) {
            return;
        }
        waifu.lastScanPos = waifu.position();
        waifu.stillScans = 0;
    }

    // ---------------------------------------------------------------------- interno

    /** Un solo paso por todas las dimensiones, agrupando por dueño para no repetir barridos. */
    private static void scan(MinecraftServer server) {
        Map<UUID, List<AbstractWaifuEntity>> byOwner = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof AbstractWaifuEntity waifu
                        && waifu.isAlive()
                        && waifu.getOwnerUUID() != null) {
                    byOwner.computeIfAbsent(waifu.getOwnerUUID(), k -> new ArrayList<>()).add(waifu);
                }
            }
        }
        for (Map.Entry<UUID, List<AbstractWaifuEntity>> group : byOwner.entrySet()) {
            ServerPlayer owner = server.getPlayerList().getPlayer(group.getKey());
            if (owner == null) {
                continue; // dueño desconectado: no hay a quién traerla
            }
            for (AbstractWaifuEntity waifu : group.getValue()) {
                check(waifu, owner);
            }
        }
    }

    /** Decide si esta waifu está perdida y, si lo está, la mueve. */
    private static void check(AbstractWaifuEntity waifu, ServerPlayer owner) {
        if (waifu.waifuMode() == WaifuMode.SENTADA || waifu.isRemoved()) {
            return; // la dejaste sentada a propósito
        }
        ServerLevel destination = destinationLevel(waifu, owner);
        Vec3 target = destinationPos(waifu, destination, owner);
        if (destination == null || target == null) {
            return;
        }

        boolean sameDimension = destination == waifu.level();
        if (sameDimension) {
            // Por debajo de este margen la waifu todavía está dentro de lo que ella mismo puede resolver.
            int limit = Math.max(RecallConfig.distance(), WanderConfig.range() + 32);
            if (target.distanceToSqr(waifu.position()) <= (double) limit * limit) {
                waifu.lastScanPos = waifu.position();
                waifu.stillScans = 0;
                return;
            }
            boolean still = waifu.lastScanPos != null
                    && waifu.lastScanPos.distanceToSqr(waifu.position()) < STILL_EPSILON * STILL_EPSILON;
            waifu.stillScans = still ? waifu.stillScans + 1 : 0;
            waifu.lastScanPos = waifu.position();
            if (waifu.stillScans < STUCK_SCANS) {
                return; // va caminando: se la deja ir
            }
        } else if (!RecallConfig.crossDimension()) {
            return;
        }
        sendToDestination(waifu, owner, sameDimension);
    }

    private static void sendToDestination(AbstractWaifuEntity waifu, ServerPlayer owner, boolean sameDimension) {
        boolean home = guardsHome(waifu);
        boolean moved = home ? waifu.sendToHome() : waifu.recallTo(owner);
        forget(waifu);
        if (!moved) {
            return;
        }
        if (home) {
            owner.displayClientMessage(Component.literal("§2[GachaWaifus] §f" + waifu.getDisplayName().getString()
                    + " §7volvió a su hogar " + waifu.homePos().toShortString() + " §7("
                    + waifu.homeDimension().location().getPath() + ")"), true);
        } else {
            owner.displayClientMessage(Component.literal("§d[GachaWaifus] §f" + waifu.getDisplayName().getString()
                    + " §7" + (sameDimension ? "se teletransportó a ti" : "cruzó dimensiones para encontrarte")
                    + " §7— la tenías demasiado lejos. Ponla en modo §2En casa §7con su hogar si quieres"
                    + " que se quede de guardia."), true);
        }
    }

    /** ¿Su destino es el hogar en vez del dueño? */
    private static boolean guardsHome(AbstractWaifuEntity waifu) {
        return waifu.waifuMode() == WaifuMode.EN_CASA && waifu.hasHome();
    }

    /** Dimensión de destino, o {@code null} si el hogar apunta a una dimensión que no existe. */
    @Nullable
    private static ServerLevel destinationLevel(AbstractWaifuEntity waifu, ServerPlayer owner) {
        MinecraftServer server = owner.serverLevel().getServer();
        if (guardsHome(waifu)) {
            return server.getLevel(waifu.homeDimension());
        }
        return owner.serverLevel();
    }

    /** Bloque de destino dentro de {@code destination}. */
    @Nullable
    private static Vec3 destinationPos(AbstractWaifuEntity waifu, @Nullable ServerLevel destination, ServerPlayer owner) {
        if (destination == null) {
            return null;
        }
        if (guardsHome(waifu)) {
            return Vec3.atCenterOf(waifu.homePos());
        }
        return destination == owner.serverLevel() ? owner.position() : null;
    }

    /** Reinicia el rastro de «está quieta»: tras moverla (o al entrar al mundo) se vuelve a empezar. */
    public static void forget(AbstractWaifuEntity waifu) {
        waifu.lastScanPos = null;
        waifu.stillScans = 0;
    }

    private WaifuRecallHandler() {
    }
}
