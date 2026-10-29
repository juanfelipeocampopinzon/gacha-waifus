package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.registry.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;

/**
 * Rareza de los jefes configurable: corta las apariciones naturales según
 * {@link BossSpawnConfig#spawnChance()}, sin tocar los JSON de biome modifier (que fijan el peso
 * base del bioma y no se pueden editar en caliente).
 *
 * <p>Se hace en {@link MobSpawnEvent.SpawnPlacementCheck}, antes de crear la entidad, y solo para
 * {@link MobSpawnType#NATURAL} y {@link MobSpawnType#CHUNK_GENERATION}: así los huevos de spawn,
 * los comandos y las invocaciones de los propios jefes (Doctor House llama a Wilson) siguen
 * funcionando igual.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class BossSpawnHandler {

    @SubscribeEvent
    public static void onSpawnPlacementCheck(MobSpawnEvent.SpawnPlacementCheck event) {
        if (!isBoss(event.getEntityType())) {
            return;
        }
        MobSpawnType spawnType = event.getSpawnType();
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION) {
            return;
        }
        int chance = BossSpawnConfig.spawnChance();
        if (chance >= 100) {
            return;
        }
        if (chance <= 0 || event.getRandom().nextInt(100) >= chance) {
            event.setResult(MobSpawnEvent.SpawnPlacementCheck.Result.FAIL);
        }
    }

    /** Los cuatro jefes del mod (todas las waifus son TamableAnimal, nunca pasan por aquí). */
    private static boolean isBoss(EntityType<?> type) {
        return type == ModEntities.RICARDO_MILOS.get()
                || type == ModEntities.DOCTOR_HOUSE.get()
                || type == ModEntities.WILSON.get()
                || type == ModEntities.FERNANFLOO.get();
    }

    private BossSpawnHandler() {
    }
}
