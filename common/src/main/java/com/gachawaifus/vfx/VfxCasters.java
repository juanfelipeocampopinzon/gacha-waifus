package com.gachawaifus.vfx;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arranca los efectos de las entidades que implementan {@link VfxCasting} (waifus sin GeckoLib,
 * como Megumin): cuando su contador de casteo cambia, lanza TODOS los stages del efecto con el
 * {@code trigger.at} de cada uno como retardo.
 *
 * <p>Es el hermano de {@link VfxTriggers}: aquel escucha clips de GeckoLib, este escucha el
 * contador sincronizado. Ninguno de los dos necesita paquetes.
 */
public final class VfxCasters {

    /** id de entidad -> último casteo visto. */
    private static final Map<Integer, Integer> LAST_SEQ = new HashMap<>();
    /** Mismo alcance que los triggers de clip: más lejos, el jugador ya no lo ve. */
    private static final double RANGE = 96.0D;

    private VfxCasters() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        double limit = RANGE * RANGE;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof VfxCasting caster) || entity.distanceToSqr(minecraft.player) > limit) {
                continue;
            }
            int seq = caster.vfxCastSeq();
            if (seq == 0) {
                continue;
            }
            Integer previous = LAST_SEQ.get(entity.getId());
            if (previous != null && previous == seq) {
                continue;
            }
            LAST_SEQ.put(entity.getId(), seq);
            playAll(caster.vfxEffectId(), entity);
        }
        if (LAST_SEQ.size() > 4096) {
            LAST_SEQ.clear();
        }
    }

    private static void playAll(String effectId, Entity entity) {
        List<VfxStage> stages = VfxLoader.stages(effectId);
        if (stages.isEmpty()) {
            return;
        }
        for (VfxStage stage : stages) {
            VfxManager.playOn(effectId, stage.stage, entity, stage.at);
        }
    }

    public static void clearCache() {
        LAST_SEQ.clear();
    }
}
