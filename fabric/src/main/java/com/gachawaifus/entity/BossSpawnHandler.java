package com.gachawaifus.entity;

import com.gachawaifus.registry.ModEntities;
import net.minecraft.world.entity.EntityType;

public final class BossSpawnHandler {
    public static void init() {
    }

    public static boolean isBoss(EntityType<?> type) {
        return type == ModEntities.RICARDO_MILOS.get()
                || type == ModEntities.DOCTOR_HOUSE.get()
                || type == ModEntities.WILSON.get()
                || type == ModEntities.FERNANFLOO.get();
    }

    private BossSpawnHandler() {
    }
}
