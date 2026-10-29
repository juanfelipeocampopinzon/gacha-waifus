package com.gachawaifus.entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

public final class WanderGoalsHandler {
    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (world.isClientSide) {
                return;
            }
            if (entity instanceof AbstractWaifuEntity waifu) {
                waifu.applyWanderGoals();
            }
        });
    }

    private WanderGoalsHandler() {
    }
}
