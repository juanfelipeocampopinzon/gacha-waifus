package com.gachawaifus.gacha;

import com.gachawaifus.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DailyPulls {

    private static final Map<UUID, Long> LAST_DAILY = new HashMap<>();
    private static final int PINK_PER_DAY = 1;

    private DailyPulls() {
    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            grantDaily(handler.player, false);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(Commands.literal("tirada")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        return grantDaily(player, true) ? 1 : 0;
                    }));
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
                                "§7Pity 5★: §b" + state.pity + "§7/64"
                                        + (state.guaranteed ? " §7· §e¡la próxima 5★ es la destacada!" : "")));
                        player.sendSystemMessage(Component.literal(
                                "§7Pity 4★: §e" + state.pity4 + "§7/32 §7(a la 32 llega una 4★ segura)"));
                        boolean hoy = LAST_DAILY.getOrDefault(player.getUUID(), 0L) >= LocalDate.now().toEpochDay();
                        player.sendSystemMessage(Component.literal(
                                "§7Tirada diaria: " + (hoy ? "§aya recogida hoy" : "§e¡disponible! usa §f/tirada")));
                        return 1;
                    }));
        });
    }

    public static boolean grantDaily(ServerPlayer player, boolean announceAnyway) {
        long today = LocalDate.now().toEpochDay();
        long last = LAST_DAILY.getOrDefault(player.getUUID(), 0L);

        if (last >= today) {
            if (announceAnyway) {
                player.sendSystemMessage(Component.literal(
                        "§e[GachaWaifus] Ya recogiste tu Bolita Rosa de hoy. Vuelve mañana. §7(Entra al mundo o usa /tirada)"));
            }
            return false;
        }

        LAST_DAILY.put(player.getUUID(), today);

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
