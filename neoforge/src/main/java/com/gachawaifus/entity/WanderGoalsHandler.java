package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Aplica el radio de deambulación configurable a todas las waifus en cuanto entran al mundo.
 *
 * <p>Se hace por evento y no en {@code AbstractWaifuEntity#registerGoals()} porque cada waifu
 * registra sus propios goals ahí (63 ficheros); reemplazarlos después de la construcción evita
 * tocarlos uno por uno.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class WanderGoalsHandler {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return; // la IA corre en el servidor
        }
        if (event.getEntity() instanceof AbstractWaifuEntity waifu) {
            waifu.applyWanderGoals();
        }
    }

    private WanderGoalsHandler() {
    }
}
