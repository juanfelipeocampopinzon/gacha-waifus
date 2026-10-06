package com.gachawaifus.gacha;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.time.LocalDate;

/**
 * Tirada diaria: una <b>Bolita Rosa</b> gratis por día real de Minecraft.
 *
 * <p>Se entrega al entrar al mundo (si ese jugador no la ha reclamado hoy) y también con el
 * comando {@code /tirada}, por si se queda conectado y cambia el día sin reconectar. El día se
 * guarda en los datos persistentes del jugador (que sobreviven a la muerte) como día de época,
 * así que no hace falta ningún archivo aparte.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class DailyPulls {

    /** Clave donde se apunta el último día reclamado (día de época). */
    private static final String KEY_LAST_DAY = "gachawaifus_last_daily";
    /** Bolitas rosas que se regalan cada día. */
    private static final int PINK_PER_DAY = 1;

    private DailyPulls() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Al entrar no se avisa si todavía no toca: solo se da cuando es un día nuevo.
            grantDaily(player, false);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("tirada")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    return grantDaily(player, true) ? 1 : 0;
                }));
        // Consultar la destacada de hoy y el estado de la pity (antes no había forma de saberlo).
        dispatcher.register(Commands.literal("banner")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    WaifuRoster.Entry featured = WaifuRoster.featured(player.serverLevel());
                    GachaSavedData.PlayerState state = GachaSavedData.get(player.level())
                            .state(player.getUUID());
                    player.sendSystemMessage(Component.literal(
                            "§d[GachaWaifus] §fDestacada de hoy: §d" + featured.name()
                                    + "§f. §7(rota cada día de Minecraft)"));
                    player.sendSystemMessage(Component.literal(
                            "§7Pity: §b" + state.pity + "§7/64"
                                    + (state.guaranteed ? " §7· §e¡la próxima 5★ es la destacada!" : "")));
                    boolean hoy = player.getPersistentData().getLong(KEY_LAST_DAY) >= LocalDate.now().toEpochDay();
                    player.sendSystemMessage(Component.literal(
                            "§7Tirada diaria: " + (hoy ? "§aya recogida hoy" : "§e¡disponible! usa §f/tirada")));
                    return 1;
                }));
    }

    /**
     * Entrega la bolita del día si corresponde.
     *
     * @param announceAnyway con {@code true} avisa también cuando ya se ha reclamado hoy
     *                       (para el comando), con {@code false} se calla en ese caso.
     * @return {@code true} si se ha entregado algo.
     */
    public static boolean grantDaily(ServerPlayer player, boolean announceAnyway) {
        long today = LocalDate.now().toEpochDay();
        CompoundTag data = player.getPersistentData();
        long last = data.getLong(KEY_LAST_DAY);

        if (last >= today) {
            if (announceAnyway) {
                player.sendSystemMessage(Component.literal(
                        "§e[GachaWaifus] Ya recogiste tu Bolita Rosa de hoy. Vuelve mañana. §7(Entra al mundo o usa /tirada)"));
            }
            return false;
        }

        data.putLong(KEY_LAST_DAY, today);

        ItemStack ball = new ItemStack(ModItems.PINK_BALL.get(), PINK_PER_DAY);
        if (!player.getInventory().add(ball)) {
            player.drop(ball, false);
        }

        player.sendSystemMessage(Component.literal(
                "§d[GachaWaifus] §f¡Tirada diaria! §dHas recibido §f" + PINK_PER_DAY
                        + " Bolita Rosa§d. Úsala en el §fTerminal Gacha§d."));
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.7F, 1.6F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }
}
