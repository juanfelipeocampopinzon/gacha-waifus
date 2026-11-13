package com.gachawaifus.vfx;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Una reproducción de un stage: lleva el reloj, emite partículas y expone el tiempo a las capas
 * que el renderer dibuja por su cuenta (anillos y brillo).
 *
 * <p>Es simulación pura: aquí no se dibuja nada.
 */
public final class VfxInstance {

    private static final Logger LOG = LoggerFactory.getLogger("gachawaifus-vfx");
    private static final Set<String> IGNORED = new HashSet<>();
    private static final Set<String> BONE_WARNED = new HashSet<>();
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static final Set<String> SUPPORTED = Set.of("emitter", "ring_wave", "glow", "telegraph",
            "slash_arc", "flash", "shake", "decal", "clone", "sphere", "projectile");

    public final VfxStage stage;
    public Vec3 origin;
    public Vec3 look;
    public final List<Particle> particles = new ArrayList<>();
    public final List<RibbonState> ribbons = new ArrayList<>();

    public static final class RibbonState {
        public final VfxLayer layer;
        public final List<RibbonNode> nodes = new ArrayList<>();

        public RibbonState(VfxLayer layer) {
            this.layer = layer;
        }
    }

    public record RibbonNode(Vec3 position, float time) {
    }

    /** Si se reproduce sobre una waifu, el origen y la mirada se refrescan cada tick. */
    net.minecraft.world.entity.Entity anchorEntity;

    private final float[] accumulators;
    private final boolean[] fired;
    private final int cap;
    private int ticks;

    public VfxInstance(VfxStage stage, Vec3 origin, Vec3 look) {
        this(stage, origin, look, 0.0F);
    }

    /**
     * @param delaySeconds retardo antes de que empiece el stage ({@code trigger.at}): el reloj
     *                     arranca en negativo, así que nada se emite ni se dibuja hasta llegar a 0.
     */
    public VfxInstance(VfxStage stage, Vec3 origin, Vec3 look, float delaySeconds) {
        this.stage = stage;
        this.ticks = -Math.round(Math.max(0.0F, delaySeconds) * 20.0F);
        this.origin = origin;
        this.look = look.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
        this.accumulators = new float[stage.layers.size()];
        this.fired = new boolean[stage.layers.size()];
        this.cap = stage.particleCap();
        for (VfxLayer layer : stage.layers) {
            if ("ribbon".equals(layer.type)) {
                ribbons.add(new RibbonState(layer));
            } else if (!SUPPORTED.contains(layer.type)) {
                if (IGNORED.add(layer.type)) {
                    LOG.info("[vfx] capa '{}' ignorada por el prototipo (el motor definitivo la hará)", layer.type);
                }
            }
        }
    }

    public void tick() {
        ticks++;
        if (anchorEntity != null && anchorEntity.isAlive() && !anchorEntity.isRemoved()) {
            // El efecto sigue a la waifu: origen en sus pies, mirada en su cabeza.
            origin = anchorEntity.position();
            Vec3 facing = anchorEntity.getLookAngle();
            if (facing.lengthSqr() > 1.0E-6D) {
                look = facing.normalize();
            }
        }
        float seconds = ticks / 20.0F;

        for (int i = 0; i < stage.layers.size(); i++) {
            VfxLayer layer = stage.layers.get(i);
            if (!"emitter".equals(layer.type)) {
                continue;
            }
            float local = seconds - layer.at;
            if (local < 0.0F) {
                continue;
            }
            // count = ráfaga única justo al llegar a "at".
            if (layer.count > 0 && !fired[i]) {
                fired[i] = true;
                spawn(layer, layer.count);
            }
            if (layer.rate == null || local > layer.duration) {
                continue;
            }
            float progress = Mth.clamp(local / Math.max(0.01F, layer.duration), 0.0F, 1.0F);
            accumulators[i] += layer.rate.sample(progress) * 0.05F;
            int amount = (int) accumulators[i];
            if (amount > 0) {
                accumulators[i] -= amount;
                spawn(layer, amount);
            }
        }

        for (Iterator<Particle> iterator = particles.iterator(); iterator.hasNext(); ) {
            Particle particle = iterator.next();
            particle.tick();
            if (!particle.alive()) {
                iterator.remove();
            }
        }

        for (RibbonState ribbon : ribbons) {
            VfxLayer layer = ribbon.layer;
            float local = seconds - layer.at;
            if (local >= 0.0F && local <= layer.duration && anchorEntity != null) {
                VfxBones.Pose pose = VfxBones.of(anchorEntity, layer.bone,
                        new Vector3f((float) layer.offset.x, (float) layer.offset.y, (float) layer.offset.z), 0.0F);
                Vec3 pos = pose != null ? pose.origin() : origin.add(layer.offset);
                ribbon.nodes.add(new RibbonNode(pos, seconds));
            }
            ribbon.nodes.removeIf(node -> (seconds - node.time()) > layer.ribbonLifetime);
        }
    }

    public boolean done() {
        boolean ribbonsDone = ribbons.isEmpty() || ribbons.stream().allMatch(r -> r.nodes.isEmpty());
        return ticks > stage.duration * 20.0F + 200.0F || (ticks > stage.duration * 20.0F + 20.0F && particles.isEmpty() && ribbonsDone);
    }

    /** Segundos transcurridos, con el partialTick aplicado para que no vaya a saltos. */
    public float timeSeconds(float partialTick) {
        return (ticks + partialTick) / 20.0F;
    }

    /** Engancha el origen a una entidad (la waifu) para verlo sobre ella. */
    public void attach(net.minecraft.world.entity.Entity entity) {
        this.anchorEntity = entity;
    }

    /** Progreso 0..1 de una capa con duración; las de duración 0 se quedan en 1. */
    public float progress(VfxLayer layer, float partialTick) {
        return Mth.clamp((timeSeconds(partialTick) - layer.at) / Math.max(0.01F, layer.duration), 0.0F, 1.0F);
    }

    public boolean visible(VfxLayer layer, float partialTick) {
        float now = timeSeconds(partialTick);
        return now >= layer.at && now <= layer.at + layer.duration;
    }

    // ------------------------------------------------------------------ emisión

    /**
     * Bloques por segundo (unidad del JSON) -> bloques por tick.
     *
     * <p>Antes se usaba el número del JSON tal cual, así que un `speed: 7` eran 7 bloques por
     * tick (140 bloques/s): la partícula se salía del encuadre en medio segundo y el chorro se
     * veía como cuentas sueltas. Todo el estándar está escrito en segundos.
     */
    private static final float TICKS_PER_SECOND = 20.0F;

    private void spawn(VfxLayer layer, int amount) {
        Anchor anchor = anchor(layer);
        Random random = ThreadLocalRandom.current();
        for (int i = 0; i < amount; i++) {
            if (particles.size() >= cap) {
                return;
            }
            Vec3 position = shape(layer, anchor.position(), random);
            Vec3 direction = direction(layer, position, anchor, random);
            if ("cone".equals(layer.shape)) {
                direction = perturb(direction, Math.max(0.12F, layer.radius), random);
            }
            float speed = Mth.lerp(random.nextFloat(), layer.speed[0], layer.speed[1]) / TICKS_PER_SECOND;
            float lifeSeconds = Mth.lerp(random.nextFloat(), layer.lifetime[0], layer.lifetime[1]);
            Vec3 velocity = direction.scale(speed);

            // Sub-tick: las del mismo tick se reparten por el recorrido de esos 50 ms y nacen con
            // esa fracción de edad. Sin esto salen todas del mismo punto y se ven tandas a 20 Hz.
            float offset = (i + random.nextFloat()) / Math.max(1, amount);
            position = position.add(velocity.scale(offset));

            Vec3 wobble = velocity.cross(unit(random));
            wobble = wobble.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 1.0D, 0.0D) : wobble.normalize();

            Particle particle = new Particle(position, velocity,
                    Math.max(1.0F, lifeSeconds * TICKS_PER_SECOND),
                    layer.size[0], layer.size[1], layer.color == null ? Gradient.WHITE : layer.color,
                    layer.texture, layer.additive, random.nextFloat(), layer.gravity,
                    layer.stretch, layer.core, layer.swirl, wobble);
            particle.age = offset;
            particles.add(particle);
        }
    }

    /**
     * Anclas: entity/ground = origen del efecto; target = {@link #target()}; {@code bone:<nombre>} = el hueso real de
     * GeckoLib (los sopletes), con el {@code offset} aplicado dentro del hueso y su eje disponible
     * para {@code direction: "bone"}. Si no hay hueso (entidad que no es GeckoLib, o sin renderizar
     * todavía) se cae a la aproximación de siempre: altura del pecho.
     */
    Anchor anchor(VfxLayer layer) {
        if (layer.anchor.startsWith("bone:") && anchorEntity != null) {
            String bone = layer.anchor.substring("bone:".length());
            Vector3f localAxis = new Vector3f(layer.boneAxis[0], layer.boneAxis[1], layer.boneAxis[2]);
            VfxBones.Pose pose = VfxBones.of(anchorEntity, bone,
                    new Vector3f((float) layer.offset.x, (float) layer.offset.y, (float) layer.offset.z), 0.0F, localAxis);
            if (pose != null) {
                return new Anchor(pose.origin(), pose.axis());
            }
        }
        Vec3 base = "target".equals(layer.anchor) ? target() : origin;
        if (layer.anchor.startsWith("bone:")) {
            if (BONE_WARNED.add(layer.anchor)) {
                LOG.info("[vfx] hueso '{}' no disponible (entidad sin GeckoLib, sin renderizar o nombre mal); uso la aproximacion del pecho", layer.anchor.substring("bone:".length()));
            }
            Vec3 right = look.cross(UP);
            right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
            boolean left = layer.anchor.startsWith("bone:left");
            base = origin.add(0.0D, 1.35D, 0.0D)
                    .add(right.scale(left ? -0.35D : 0.35D))
                    .add(look.scale(0.2D));
        }
        return new Anchor(base.add(local(layer.offset)), null);
    }

    /**
     * Offset del JSON (x = derecha, y = arriba, z = delante) pasado al mundo con el giro horizontal
     * de la mirada. Así {@code [0,1,1.4]} queda delante de la waifu mire donde mire; antes iba
     * siempre hacia +Z del mundo.
     */
    public Vec3 local(Vec3 offset) {
        if (offset.x == 0.0D && offset.z == 0.0D) {
            return offset;
        }
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        forward = forward.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        return right.scale(offset.x).add(0.0D, offset.y, 0.0D).add(forward.scale(offset.z));
    }

    /**
     * Punto donde cae la habilidad: el de la entidad si implementa {@link VfxTargeted}; si no lo
     * sabe, 6 bloques delante del origen (así el comando de prueba también lo enseña).
     */
    public Vec3 target() {
        if (anchorEntity instanceof VfxTargeted targeted) {
            Vec3 point = targeted.vfxTarget();
            if (point != null) {
                return point;
            }
        }
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        forward = forward.lengthSqr() < 1.0E-6D ? new Vec3(0.0D, 0.0D, 1.0D) : forward.normalize();
        return origin.add(forward.scale(6.0D));
    }

    /** Punto del mundo de una capa sin hueso (ancla + offset local). */
    public Vec3 point(VfxLayer layer) {
        return anchor(layer).position();
    }

    /** Segundos desde que empezó el stage (negativo mientras dura el retardo). */
    public float localSeconds(VfxLayer layer, float partialTick) {
        return timeSeconds(partialTick) - layer.at;
    }

    /** Punto de emisión y, si se ancló a un hueso, su eje (pivote → punta). */
    record Anchor(Vec3 position, @Nullable Vec3 axis) {
    }

    private Vec3 shape(VfxLayer layer, Vec3 base, Random random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        switch (layer.shape) {
            case "disc" -> {
                double distance = layer.radius * Math.sqrt(random.nextDouble());
                return base.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
            }
            case "ring" -> {
                return base.add(Math.cos(angle) * layer.radius, 0.0D, Math.sin(angle) * layer.radius);
            }
            case "sphere" -> {
                double distance = layer.radius * Math.cbrt(random.nextDouble());
                return base.add(unit(random).scale(distance));
            }
            case "line" -> {
                Vec3 right = look.cross(UP);
                right = right.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : right.normalize();
                return base.add(right.scale((random.nextDouble() * 2.0D - 1.0D) * layer.radius));
            }
            case "cone" -> {
                return base.add(unit(random).scale(layer.radius * 0.25D * random.nextDouble()));
            }
            default -> {
                return base;
            }
        }
    }

    private Vec3 direction(VfxLayer layer, Vec3 position, Anchor anchor, Random random) {
        Vec3 base = anchor.position();
        return switch (layer.direction) {
            case "up" -> UP;
            case "down" -> new Vec3(0.0D, -1.0D, 0.0D);
            case "forward", "look" -> look;
            // "bone" = por el hueso anclado, del pivote hacia la punta (el soplete apunta).
            case "bone" -> anchor.axis() != null ? anchor.axis() : look;
            case "inward" -> {
                Vec3 towards = base.subtract(position);
                yield towards.lengthSqr() < 1.0E-6D ? unit(random) : towards.normalize();
            }
            case "outward" -> {
                Vec3 away = new Vec3(position.x - base.x, 0.0D, position.z - base.z);
                yield away.lengthSqr() < 1.0E-6D ? horizontal(random) : away.normalize();
            }
            default -> unit(random);
        };
    }

    private static Vec3 perturb(Vec3 direction, float spread, Random random) {
        Vec3 mixed = direction.add(unit(random).scale(spread));
        return mixed.lengthSqr() < 1.0E-6D ? direction : mixed.normalize();
    }

    private static Vec3 unit(Random random) {
        double z = random.nextDouble() * 2.0D - 1.0D;
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = Math.sqrt(Math.max(0.0D, 1.0D - z * z));
        return new Vec3(Math.cos(angle) * radius, z, Math.sin(angle) * radius);
    }

    private static Vec3 horizontal(Random random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        return new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
    }
}
