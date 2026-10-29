package com.gachawaifus.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class WaifuRecallHandler {

    private static final int SCAN_TICKS = 100;
    private static final int STUCK_SCANS = 2;
    private static final double STILL_EPSILON = 1.0D;
    private static int tickCounter;

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(WaifuRecallHandler::onServerTick);
        ServerEntityEvents.ENTITY_LOAD.register(WaifuRecallHandler::onEntityJoin);
    }

    public static List<AbstractWaifuEntity> waifusOf(MinecraftServer server, UUID owner) {
        List<AbstractWaifuEntity> found = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof AbstractWaifuEntity waifu
                        && waifu.isAlive()
                        && owner.equals(waifu.getOwnerUUID())) {
                    found.add(waifu);
                }
            }
        }
        return found;
    }

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

    public static void onServerTick(MinecraftServer server) {
        if (!RecallConfig.enabled()) {
            return;
        }
        if (++tickCounter < SCAN_TICKS) {
            return;
        }
        tickCounter = 0;
        scan(server);
    }

    public static void onEntityJoin(Entity entity, ServerLevel world) {
        if (world.isClientSide || !(entity instanceof AbstractWaifuEntity waifu)) {
            return;
        }
        waifu.lastScanPos = waifu.position();
        waifu.stillScans = 0;
    }

    private static void scan(MinecraftServer server) {
        Map<UUID, List<AbstractWaifuEntity>> byOwner = new HashMap<>();
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
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
                continue;
            }
            for (AbstractWaifuEntity waifu : group.getValue()) {
                check(waifu, owner);
            }
        }
    }

    private static void check(AbstractWaifuEntity waifu, ServerPlayer owner) {
        if (waifu.waifuMode() == WaifuMode.SENTADA || waifu.isRemoved()) {
            return;
        }
        ServerLevel destination = destinationLevel(waifu, owner);
        Vec3 target = destinationPos(waifu, destination, owner);
        if (destination == null || target == null) {
            return;
        }

        boolean sameDimension = destination == waifu.level();
        if (sameDimension) {
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
                return;
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

    private static boolean guardsHome(AbstractWaifuEntity waifu) {
        return waifu.waifuMode() == WaifuMode.EN_CASA && waifu.hasHome();
    }

    @Nullable
    private static ServerLevel destinationLevel(AbstractWaifuEntity waifu, ServerPlayer owner) {
        MinecraftServer server = owner.serverLevel().getServer();
        if (guardsHome(waifu)) {
            return server.getLevel(waifu.homeDimension());
        }
        return owner.serverLevel();
    }

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

    public static void forget(AbstractWaifuEntity waifu) {
        waifu.lastScanPos = null;
        waifu.stillScans = 0;
    }

    private WaifuRecallHandler() {
    }
}
