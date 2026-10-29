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
                player.sendSystemMessage(Component.literal(
                        "§d[GachaWaifus] §fTienes una §dwaifu de regalo§f sin reclamar. Míralas con §f/inicio"
                                + " §7y elige con §f/inicio <número>§f."));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(Commands.literal("inicio")
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
            player.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] §fTu waifu inicial ya está elegida. §7Las demás salen del"
                            + " §fTerminal Gacha§7, de las §fmisiones diarias§7 y domando waifus salvajes."));
            return 0;
        }
        player.sendSystemMessage(Component.literal(
                "§d[GachaWaifus] §fElige tu waifu inicial §7(una sola vez, gratis)§f:"));
        for (int i = 0; i < opciones.size(); i++) {
            player.sendSystemMessage(Component.literal(
                    "§7 " + (i + 1) + " §f· §d" + opciones.get(i).name()));
        }
        player.sendSystemMessage(Component.literal(
                "§7Reclama con §f/inicio <número>§7. §f/inicio §7vuelve a listarlas."));
        return opciones.size();
    }

    private static int entregar(ServerPlayer player, int numero) {
        if (claimed(player)) {
            player.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] §fLa waifu inicial ya se reclamó en este mundo. §7Escribe §f/inicio"
                            + " §7para ver cuál elegiste en tu Cápsula."));
            return 0;
        }
        WaifuRoster.Entry entry = WaifuRoster.STANDARD_POOL.get(numero - 1);
        player.addTag(KEY_CLAIMED);

        ItemStack token = new ItemStack(entry.token().get());
        if (!player.getInventory().add(token)) {
            player.drop(token, false);
        }

        player.sendSystemMessage(Component.literal(
                "§d[GachaWaifus] §f¡" + entry.name() + " §fes tu waifu inicial! §7Su token está en tu"
                        + " inventario: clic derecho al suelo para invocarla."));
        player.sendSystemMessage(Component.literal(
                "§7Shift + clic con la mano vacía sobre ella cambia su modo de combate."));
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
