package com.gachawaifus.gacha;

import com.gachawaifus.menu.GuideMenu;
import com.gachawaifus.registry.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class GuideGiver {

    private static final String KEY_GIVEN = "gachawaifus_guide_given";

    private GuideGiver() {
    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> give(handler.player));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(Commands.literal("guia")
                    .executes(context -> {
                        Player player = context.getSource().getPlayerOrException();
                        GuideMenu.open((ServerPlayer) player);
                        return 1;
                    }));
        });
    }

    public static void give(Entity entity) {
        if (!(entity instanceof ServerPlayer player) || given(player)) {
            return;
        }
        player.addTag(KEY_GIVEN);

        ItemStack guide = new ItemStack(ModItems.WAIFU_GUIDE.get());
        if (player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, guide);
        } else if (!player.getInventory().add(guide)) {
            player.drop(guide, false);
        }

        player.sendSystemMessage(Component.literal(
                "§d[GachaWaifus] §fTienes una §dGuía del Invocador §fen la mano: clic derecho para"
                        + " abrirla. §7Explica las bolitas, las tiradas, los modos y el hogar."));
        player.sendSystemMessage(Component.literal(
                "§7Después se craftea y §f/guia §fla abre sin llevarla encima."));
    }

    private static boolean given(ServerPlayer player) {
        return player.getTags().contains(KEY_GIVEN);
    }
}
