package com.gachawaifus.vfx;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arranca los efectos cuando empieza su clip de animación, sin comandos ni paquetes.
 *
 * <p>El mapa sale de los propios JSON: cada stage que declare {@code trigger.clip} se asocia a ese
 * clip. Cada tick se mira qué animación reproduce cada {@link GeoEntity} cercano; cuando el nombre
 * cambia y el nuevo está en la tabla, se lanza el stage sobre esa entidad (que a su vez ancla sus
 * capas {@code bone:} a los huesos de verdad).
 */
public final class VfxTriggers {

    private static final Logger LOG = LoggerFactory.getLogger("gachawaifus-vfx");
    /** clip -> stages que arrancan con él (un clip largo puede llevar varios, cada uno en su "at"). */
    private static final Map<String, List<Target>> BY_CLIP = new HashMap<>();

    private record Target(String effectId, String stage, float at) {
    }
    /** id de entidad -> último clip visto, para no repetir el stage en cada tick. */
    private static final Map<Integer, String> LAST_CLIP = new HashMap<>();
    private static final double RANGE = 64.0D;
    private static boolean scanned = false;

    private VfxTriggers() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (!scanned) {
            scanned = true;
            scan(minecraft);
        }
        if (BY_CLIP.isEmpty()) {
            return;
        }
        double limit = RANGE * RANGE;
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof GeoEntity geo) || entity.distanceToSqr(minecraft.player) > limit) {
                continue;
            }
            String clip = currentClip(geo, entity);
            String previous = clip == null ? LAST_CLIP.remove(entity.getId()) : LAST_CLIP.put(entity.getId(), clip);
            if (clip == null || clip.equals(previous)) {
                continue;
            }
            List<Target> targets = BY_CLIP.get(clip);
            if (targets != null) {
                for (Target target : targets) {
                    VfxManager.playOn(target.effectId(), target.stage(), entity, target.at());
                }
            }
        }
    }

    /**
     * Nombre del clip que está reproduciendo la entidad, o null.
     *
     * <p>Hay dos controladores a la vez (movimiento e habilidades): si alguno reproduce un clip que
     * tiene efecto, gana ese; si no, devuelve el primero (para detectar el cambio de todos modos).
     */
    private static String currentClip(GeoEntity geo, Entity entity) {
        AnimatableManager<?> manager = geo.getAnimatableInstanceCache().getManagerForId(entity.getId());
        if (manager == null) {
            return null;
        }
        String first = null;
        for (AnimationController<?> controller : manager.getAnimationControllers().values()) {
            if (controller.getAnimationState() != AnimationController.State.RUNNING) {
                continue;
            }
            AnimationProcessor.QueuedAnimation queued = controller.getCurrentAnimation();
            if (queued == null || queued.animation() == null) {
                continue;
            }
            String name = queued.animation().name();
            if (first == null) {
                first = name;
            }
            if (BY_CLIP.containsKey(name)) {
                return name;
            }
        }
        return first;
    }

    /** Lee todos los {@code vfx/**.json} y se queda con clip -> stage. */
    private static void scan(Minecraft minecraft) {
        BY_CLIP.clear();
        Map<ResourceLocation, Resource> files;
        try {
            files = minecraft.getResourceManager().listResources("vfx", path -> path.getPath().endsWith(".json"));
        } catch (RuntimeException exception) {
            LOG.warn("[vfx] no se pudo listar vfx/: {}", exception.toString());
            return;
        }
        for (Map.Entry<ResourceLocation, Resource> entry : files.entrySet()) {
            try (var reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (!root.has("id") || !root.has("stages")) {
                    continue;
                }
                String effectId = root.get("id").getAsString();
                JsonObject stages = root.getAsJsonObject("stages");
                for (String stage : stages.keySet()) {
                    JsonObject json = stages.getAsJsonObject(stage);
                    if (!json.has("trigger")) {
                        continue;
                    }
                    JsonObject trigger = json.getAsJsonObject("trigger");
                    if (!trigger.has("clip")) {
                        continue;
                    }
                    float at = trigger.has("at") ? trigger.get("at").getAsFloat() : 0.0F;
                    BY_CLIP.computeIfAbsent(trigger.get("clip").getAsString(), key -> new ArrayList<>())
                            .add(new Target(effectId, stage, at));
                }
            } catch (Exception exception) {
                LOG.warn("[vfx] trigger ilegible en {}: {}", entry.getKey(), exception.toString());
            }
        }
        LOG.info("[vfx] triggers de animacion: {}", BY_CLIP.size());
    }

    /**
     * Lanza sobre la entidad todos los stages de un efecto que declaren trigger, cada uno con su
     * {@code at}, en el orden de sus clips. Es para el comando de prueba: los clips de verdad no
     * se reproducen, así que los stages de clips distintos se encadenan con {@code at} relativo al
     * primero solo si comparten clip; los de otro clip arrancan a la vez que el primero.
     *
     * @return cuántos stages se lanzaron
     */
    public static int playAll(String effectId, Entity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!scanned) {
            scanned = true;
            scan(minecraft);
        }
        int played = 0;
        for (List<Target> targets : BY_CLIP.values()) {
            for (Target target : targets) {
                if (target.effectId().equals(effectId)) {
                    VfxManager.playOn(target.effectId(), target.stage(), entity, target.at());
                    played++;
                }
            }
        }
        return played;
    }

    public static void clearCache() {
        BY_CLIP.clear();
        LAST_CLIP.clear();
        scanned = false;
    }
}
