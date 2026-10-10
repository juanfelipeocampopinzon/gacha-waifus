package com.gachawaifus.team;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Límite del equipo en los 19 tokens sin repetir el chequeo en cada token:
 * {@link PlayerInteractEvent.RightClickBlock} se dispara ANTES de {@code Item.useOn()}, así que
 * cancelándolo ahí no se invoca ni se consume el token.
 *
 * <p>Se reconoce el token por el id del registro ({@code *_token}); todos los del mod siguen
 * esa convención (ver {@code ModItems}).
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class TeamEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        Player player = event.getEntity();
        String itemId = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).getPath();
        if (!itemId.endsWith("_token")) {
            return;
        }
        if (TeamRules.canSummon(player)) {
            return;
        }
        event.setCanceled(true);
        TeamRules.messageFull(player);
    }

    private TeamEvents() {
    }
}
