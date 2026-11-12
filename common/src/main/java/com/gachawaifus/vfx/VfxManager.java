package com.gachawaifus.vfx;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Lista de efectos vivos del cliente. Singleton de facto: todo estático y sin estado por jugador. */
public final class VfxManager {

    private static final List<VfxInstance> INSTANCES = new ArrayList<>();

    private VfxManager() {
    }

    public static void play(String effectId, String stage, Vec3 origin, Vec3 look) {
        VfxStage loaded = VfxLoader.load(effectId, stage);
        if (loaded == null) {
            return;
        }
        INSTANCES.add(new VfxInstance(loaded, origin, look));
    }

    /** Igual que play, pero el efecto sigue a la entidad (para verlo sobre una waifu). */
    public static void playOn(String effectId, String stage, net.minecraft.world.entity.Entity entity) {
        VfxStage loaded = VfxLoader.load(effectId, stage);
        if (loaded == null) {
            return;
        }
        VfxInstance instance = new VfxInstance(loaded, entity.position(), entity.getLookAngle());
        instance.attach(entity);
        INSTANCES.add(instance);
    }

    public static void tick() {
        VfxTriggers.tick();
        for (Iterator<VfxInstance> iterator = INSTANCES.iterator(); iterator.hasNext(); ) {
            VfxInstance instance = iterator.next();
            instance.tick();
            if (instance.done()) {
                iterator.remove();
            }
        }
    }

    public static void clear() {
        INSTANCES.clear();
    }

    public static List<VfxInstance> instances() {
        return Collections.unmodifiableList(INSTANCES);
    }

    /** Todas las partículas vivas, para el renderer. */
    public static List<Particle> particles() {
        List<Particle> all = new ArrayList<>();
        for (VfxInstance instance : INSTANCES) {
            all.addAll(instance.particles);
        }
        return all;
    }
}
