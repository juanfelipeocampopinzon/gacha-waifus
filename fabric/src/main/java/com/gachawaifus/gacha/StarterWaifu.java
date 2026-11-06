package com.gachawaifus.gacha;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class StarterWaifu {

    private static final String KEY_CLAIMED = "gachawaifus_starter_claimed";

    private StarterWaifu() {
    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            if (!claimed(player)) {
                player.sendSystemMessage(Component.translatable("gui.gachawaifus.starter_waifu.1"));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(Commands.literal("inicio")
                    .executes(context -> listar(context.getSource().getPlayerOrException()))
                    .then(Commands.argument("numero",
                                    IntegerArgumentType.integer(1, WaifuRoster.STANDARD_POOL.size()))
                            .executes(context -> entregar(context.getSource().getPlayerOrException(),
                                    IntegerArgumentType.getInteger(context, "numero")))));
            dispatcher.register(Commands.literal("start")
                    .executes(context -> listar(context.getSource().getPlayerOrException()))
                    .then(Commands.argument("numero",
                                    IntegerArgumentType.integer(1, WaifuRoster.STANDARD_POOL.size()))
                            .executes(context -> entregar(context.getSource().getPlayerOrException(),
                                    IntegerArgumentType.getInteger(context, "numero")))));
        });
    }

    private static int listar(ServerPlayer player) {
        List<WaifuRoster.Entry> opciones = WaifuRoster.STANDARD_POOL;
        if (claimed(player)) {
            player.sendSystemMessage(Component.translatable("gui.gachawaifus.starter_waifu.2"));
            return 0;
        }
        player.sendSystemMessage(Component.translatable("gui.gachawaifus.starter_waifu.3"));
        for (int i = 0; i < opciones.size(); i++) {
            player.sendSystemMessage(Component.literal(
                    "§7 " + (i + 1) + " §f· §d" + opciones.get(i).name()));
        }
        player.sendSystemMessage(Component.translatable("gui.gachawaifus.starter_waifu.4"));
        return opciones.size();
    }

    private static int entregar(ServerPlayer player, int numero) {
        if (claimed(player)) {
            player.sendSystemMessage(Component.translatable("gui.gachawaifus.starter_waifu.5"));
            return 0;
        }
        WaifuRoster.Entry entry = WaifuRoster.STANDARD_POOL.get(numero - 1);
        player.addTag(KEY_CLAIMED);

        ItemStack token = new ItemStack(entry.token().get());
        if (!player.getInventory().add(token)) {
            player.drop(token, false);
        }

        player.sendSystemMessage(Component.translatable("gachawaifus.starter.given", entry.name()));
        player.sendSystemMessage(Component.translatable("gui.gachawaifus.starter_waifu.6"));
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 1.0F, 1.3F);
        player.serverLevel().sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.6D,
                player.getZ(), 10, 0.4D, 0.5D, 0.4D, 0.1D);
        return 1;
    }

    private static boolean claimed(ServerPlayer player) {
        return player.getTags().contains(KEY_CLAIMED);
    }
}
