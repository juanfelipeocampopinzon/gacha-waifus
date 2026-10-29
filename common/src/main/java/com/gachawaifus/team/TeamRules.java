package com.gachawaifus.team;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/**
 * Límite del equipo: cuántas waifus puede tener un jugador invocadas a la vez.
 *
 * <p>El recuento son las waifus vivas y cargadas en el mundo (todas las dimensiones) cuyo dueño
 * es el jugador: las guardadas en la Cápsula o caídas no cuentan. Se consulta al invocar
 * (tokens y Cápsula), no en tick, así que no cuesta nada por frame.
 */
public final class TeamRules {

    /** Límite actual; 0 = ilimitado. Seguro aunque la config todavía no se haya cargado. */
    public static int limit() {
        if (!TeamConfig.SPEC.isLoaded()) {
            return 0;
        }
        return TeamConfig.LIMIT.get();
    }

    /** Waifus vivas del jugador ahora mismo en el mundo. */
    public static int activeCount(Player player) {
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return 0;
        }
        int count = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof AbstractWaifuEntity waifu
                        && waifu.isAlive()
                        && player.getUUID().equals(waifu.getOwnerUUID())) {
                    count++;
                }
            }
        }
        return count;
    }

    /** ¿Puede el jugador invocar otra waifu ahora mismo? */
    public static boolean canSummon(Player player) {
        int limit = limit();
        return limit <= 0 || activeCount(player) < limit;
    }

    /** Mensaje estándar cuando el equipo está lleno (va a la barra de acción). */
    public static void messageFull(Player player) {
        player.displayClientMessage(Component.literal(
                "§c[GachaWaifus] Equipo completo: ya tienes §f" + activeCount(player) + "/" + limit()
                        + "§c waifus activas. Guarda una con la §dCápsula Waifu§c (Shift + clic"
                        + " sobre ella) e invócala después."), true);
    }

    private TeamRules() {
    }
}
