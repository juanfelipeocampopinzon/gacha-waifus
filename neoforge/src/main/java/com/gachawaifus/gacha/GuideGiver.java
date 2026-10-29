package com.gachawaifus.gacha;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.menu.GuideMenu;
import com.gachawaifus.registry.ModItems;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Reparto de la Guía del Invocador.
 *
 * <p>El manual tiene que caer en las manos del jugador sin que lo pida: al entrar por primera vez
 * a un mundo se le pone en la mano (y en el inventario si ya traía algo agarrado). La marca vive
 * en los datos persistentes del jugador, igual que el regalo de {@link StarterWaifu} y la tirada
 * diaria de {@link DailyPulls}, así que no se duplica en las siguientes sesiones.
 *
 * <p>Que se entregue una vez no significa que se pierda: la guía se craftea (madera, libro y
 * papel) y {@code /guia} abre el manual aunque no lleves el ítem encima.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class GuideGiver {

    /** Marca de "ya le di la guía" en los datos persistentes del jugador. */
    private static final String KEY_GIVEN = "gachawaifus_guide_given";

    private GuideGiver() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        give(event.getEntity());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("guia")
                .executes(context -> {
                    Player player = context.getSource().getPlayerOrException();
                    GuideMenu.open((ServerPlayer) player);
                    return 1;
                }));
    }

    /** Mete la guía en la mano del jugador recién llegado, una sola vez por mundo. */
    private static void give(Entity entity) {
        if (!(entity instanceof ServerPlayer player) || given(player)) {
            return;
        }
        player.getPersistentData().putBoolean(KEY_GIVEN, true);

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
        return player.getPersistentData().getBoolean(KEY_GIVEN);
    }
}
