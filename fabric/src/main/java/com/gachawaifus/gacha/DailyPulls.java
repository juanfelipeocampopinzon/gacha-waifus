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
            dispatcher.register(Commands.literal("pull")
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
                        player.sendSystemMessage(Component.translatable(
                                "gachawaifus.daily.featured", featured.name()));
                        player.sendSystemMessage(Component.translatable("gachawaifus.daily.pity5", state.pity)
                                .append(state.guaranteed
                                        ? Component.translatable("gachawaifus.daily.pity5_guaranteed")
                                        : Component.empty()));
                        player.sendSystemMessage(Component.translatable(
                                "gachawaifus.daily.pity4", state.pity4));
                        boolean hoy = LAST_DAILY.getOrDefault(player.getUUID(), 0L) >= LocalDate.now().toEpochDay();
                        player.sendSystemMessage(Component.translatable(
                                hoy ? "gachawaifus.daily.claimed" : "gachawaifus.daily.available"));
                        return 1;
                    }));
        });
    }

    public static boolean grantDaily(ServerPlayer player, boolean announceAnyway) {
        long today = LocalDate.now().toEpochDay();
        long last = LAST_DAILY.getOrDefault(player.getUUID(), 0L);

        if (last >= today) {
            if (announceAnyway) {
                player.sendSystemMessage(Component.translatable("gui.gachawaifus.daily_pulls.1"));
            }
            return false;
        }

        LAST_DAILY.put(player.getUUID(), today);

        ItemStack ball = new ItemStack(ModItems.PINK_BALL.get(), PINK_PER_DAY);
        if (!player.getInventory().add(ball)) {
            player.drop(ball, false);
        }

        player.sendSystemMessage(Component.translatable("gachawaifus.daily.granted", PINK_PER_DAY));
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.7F, 1.6F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.8F, 1.4F);
        return true;
    }
}
