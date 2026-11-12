package com.gachawaifus.vfx;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dibuja el prototipo: partículas como quads que miran a la cámara, anillos planos en el suelo y
 * brillos. Todo va en coordenadas relativas a la cámara (como el terreno de 1.21.1) y la matriz de
 * vista llega desde el gancho del loader.
 */
public final class VfxRenderer {

    private static final float TAU = (float) (Math.PI * 2.0D);
    private static boolean failed = false;

    private VfxRenderer() {
    }

    public static void render(Matrix4f modelView, Camera camera, float partialTick) {
        if (failed) {
            return;
        }
        // El prototipo no debe tumbar el juego: si el dibujo revienta, se apaga una vez y queda en el log.
        try {
            renderNow(modelView, camera, partialTick);
        } catch (RuntimeException exception) {
            failed = true;
            org.slf4j.LoggerFactory.getLogger("gachawaifus-vfx").error("[vfx] el dibujo fallo y se desactiva", exception);
        }
    }

    private static void renderNow(Matrix4f modelView, Camera camera, float partialTick) {
        List<VfxInstance> instances = VfxManager.instances();
        if (instances.isEmpty()) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        Vector3f right = new Vector3f(camera.getLeftVector()).negate();
        Vector3f up = new Vector3f(camera.getUpVector());

        Map<Group, List<Quad>> groups = new LinkedHashMap<>();
        for (VfxInstance instance : instances) {
            for (Particle particle : instance.particles) {
                addParticle(groups, particle, cameraPos, right, up, partialTick);
            }
            for (VfxInstance.RibbonState ribbon : instance.ribbons) {
                addRibbon(groups, ribbon, cameraPos, instance.timeSeconds(partialTick));
            }
            for (VfxLayer layer : instance.stage.layers) {
                if (!instance.visible(layer, partialTick)) {
                    continue;
                }
                if ("ring_wave".equals(layer.type)) {
                    addRing(groups, instance, layer, cameraPos, partialTick);
                } else if ("glow".equals(layer.type)) {
                    addGlow(groups, instance, layer, cameraPos, right, up, partialTick);
                }
            }
        }
        if (groups.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        for (Map.Entry<Group, List<Quad>> entry : groups.entrySet()) {
            boolean isRing = entry.getKey().texture().startsWith("ring_");
            if (isRing) {
                RenderSystem.enablePolygonOffset();
                RenderSystem.polygonOffset(-1.0F, -10.0F);
            }
            if (entry.getKey().additive()) {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.defaultBlendFunc();
            }
            draw(entry.getKey().texture(), entry.getValue(), modelView, minecraft);
            if (isRing) {
                RenderSystem.disablePolygonOffset();
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // ------------------------------------------------------------------ quads

    private static void addParticle(Map<Group, List<Quad>> groups, Particle particle, Vec3 cameraPos,
                                    Vector3f right, Vector3f up, float partialTick) {
        float progress = particle.normalizedAge(partialTick);
        float size = Mth.lerp(progress, particle.sizeStart, particle.sizeEnd);
        if (size <= 0.002F) {
            return;
        }
        float half = size * 0.5F;
        float halfAlong = half;
        float halfAcross = half;

        Vector3f axis;
        Vector3f across;
        if (particle.texture.endsWith("_8f")) {
            // Los fotogramas ya traen la silueta: girarlos al azar los convierte en hojas sueltas.
            axis = new Vector3f(right);
            across = new Vector3f(up);
        } else {
            float angle = particle.phase * TAU;
            float cos = Mth.cos(angle);
            float sin = Mth.sin(angle);
            axis = new Vector3f(right).mul(cos).fma(sin, up);
            across = new Vector3f(up).mul(cos).fma(-sin, right);
        }

        if (particle.stretch > 0.0F) {
            // Estirado en la dirección de la velocidad, proyectada en el plano de la cámara:
            // un chorro deja de ser una fila de bolitas y pasa a ser una veta continua.
            Vector3f velocity = new Vector3f((float) particle.vel.x,
                    (float) particle.vel.y, (float) particle.vel.z);
            float alongRight = velocity.dot(right);
            float alongUp = velocity.dot(up);
            float length = Mth.sqrt(alongRight * alongRight + alongUp * alongUp);
            if (length > 1.0E-4F) {
                axis = new Vector3f(right).mul(alongRight / length).fma(alongUp / length, up);
                Vector3f normal = new Vector3f(right).cross(up, new Vector3f());
                across = new Vector3f(normal).cross(axis, new Vector3f()).normalize();
                // |v| va en bloques/tick (7 bloques/s ≈ 0,35). El ancho se afina con la edad.
                halfAlong = half * (1.0F + particle.stretch * velocity.length() * 1.6F);
                halfAcross = half * Mth.clamp(0.78F - progress * 0.22F, 0.45F, 1.0F);
            }
        }

        Vec3 centre = particle.pos.add(particle.vel.scale(partialTick)).subtract(cameraPos);

        float u0 = 0.0F;
        float u1 = 1.0F;
        if (particle.texture.endsWith("_8f")) {
            // Fotograma por edad: la tira va de bola caliente a jirón, así que la partícula se
            // ENFRÍA a lo largo de su vida en vez de recorrer los 8 cuadros a su aire.
            int frame = Mth.clamp((int) (progress * 8.0F), 0, 7);
            u0 = frame / 8.0F;
            u1 = (frame + 1) / 8.0F;
        }

        float[] color = particle.color.sample(progress);
        float red = color[0];
        float green = color[1];
        float blue = color[2];
        if (particle.additive) {
            red = Math.min(1.0F, red * 1.25F);
            green = Math.min(1.0F, green * 1.25F);
            blue = Math.min(1.0F, blue * 1.25F);
        }

        if ("cube".equals(particle.texture) || particle.texture.startsWith("cube_")) {
            addCube(groups, particle.texture, particle.additive, centre, particle.vel,
                    particle.stretch, half, red, green, blue, color[3], particle.phase, progress);
            return;
        }

        addQuad(groups, particle.texture, particle.additive, centre, axis, across,
                halfAlong, halfAcross, u0, u1, red, green, blue, color[3]);


        if (particle.core > 0.0F) {
            // Núcleo caliente: la misma partícula dibujada otra vez, más pequeña y más blanca.
            // Da volumen (blanco dentro, naranja fuera) sin gastar más partículas en el tope.
            float coreSize = particle.core * (0.80F - 0.28F * progress);
            float coreAlpha = Math.min(1.0F, color[3] * (1.0F - progress) * 0.70F);
            if (coreAlpha > 0.01F && coreSize > 0.01F) {
                float white = Mth.clamp(0.40F + 0.55F * (1.0F - progress), 0.0F, 1.0F);
                addQuad(groups, "flame_core", true, centre, axis, across,
                        halfAlong * coreSize, halfAcross * coreSize, 0.0F, 1.0F,
                        Mth.lerp(white, red, 1.0F), Mth.lerp(white, green, 1.0F),
                        Mth.lerp(white, blue, 1.0F), coreAlpha);
            }
        }
    }

    private static void addQuad(Map<Group, List<Quad>> groups, String texture, boolean additive,
                                Vec3 centre, Vector3f axis, Vector3f across, float halfAlong,
                                float halfAcross, float u0, float u1,
                                float red, float green, float blue, float alpha) {
        groups.computeIfAbsent(new Group(texture, additive), key -> new ArrayList<>())
                .add(new Quad(
                        pole(centre, axis, across, -halfAlong, -halfAcross),
                        pole(centre, axis, across, halfAlong, -halfAcross),
                        pole(centre, axis, across, halfAlong, halfAcross),
                        pole(centre, axis, across, -halfAlong, halfAcross),
                        u0, 0.0F, u1, 1.0F, red, green, blue, alpha));
    }

    private static void addCube(Map<Group, List<Quad>> groups, String texture, boolean additive,
                                Vec3 centre, Vec3 vel, float stretch, float half,
                                float r, float g, float b, float a, float phase, float progress) {
        float yaw = phase * 6.2831853F + progress * 4.0F;
        float pitch = phase * 3.14159F + progress * 3.0F;
        Quaternionf rot = new Quaternionf().rotationYXZ(yaw, pitch, phase * 2.0F);

        float halfX = half;
        float halfY = half;
        float halfZ = half;

        if (stretch > 0.0F && vel.lengthSqr() > 1.0E-4D) {
            halfZ = half * (1.0F + stretch * (float) vel.length() * 1.5F);
            halfX = half * 0.85F;
            halfY = half * 0.85F;
            Vector3f vDir = new Vector3f((float) vel.x, (float) vel.y, (float) vel.z).normalize();
            rot = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), vDir);
            rot.rotateZ(yaw);
        }

        Vector3f p000 = new Vector3f(-halfX, -halfY, -halfZ).rotate(rot);
        Vector3f p100 = new Vector3f( halfX, -halfY, -halfZ).rotate(rot);
        Vector3f p110 = new Vector3f( halfX,  halfY, -halfZ).rotate(rot);
        Vector3f p010 = new Vector3f(-halfX,  halfY, -halfZ).rotate(rot);
        Vector3f p001 = new Vector3f(-halfX, -halfY,  halfZ).rotate(rot);
        Vector3f p101 = new Vector3f( halfX, -halfY,  halfZ).rotate(rot);
        Vector3f p111 = new Vector3f( halfX,  halfY,  halfZ).rotate(rot);
        Vector3f p011 = new Vector3f(-halfX,  halfY,  halfZ).rotate(rot);

        Vec3 v000 = centre.add(p000.x, p000.y, p000.z);
        Vec3 v100 = centre.add(p100.x, p100.y, p100.z);
        Vec3 v110 = centre.add(p110.x, p110.y, p110.z);
        Vec3 v010 = centre.add(p010.x, p010.y, p010.z);
        Vec3 v001 = centre.add(p001.x, p001.y, p001.z);
        Vec3 v101 = centre.add(p101.x, p101.y, p101.z);
        Vec3 v111 = centre.add(p111.x, p111.y, p111.z);
        Vec3 v011 = centre.add(p011.x, p011.y, p011.z);

        List<Quad> list = groups.computeIfAbsent(new Group(texture, additive), key -> new ArrayList<>());

        float topShade = 1.0F;
        float sideShade = 0.85F;
        float bottomShade = 0.70F;

        // Frente (+Z)
        list.add(new Quad(v001, v101, v111, v011, 0.0F, 0.0F, 1.0F, 1.0F, r * sideShade, g * sideShade, b * sideShade, a));
        // Atrás (-Z)
        list.add(new Quad(v100, v000, v010, v110, 0.0F, 0.0F, 1.0F, 1.0F, r * sideShade, g * sideShade, b * sideShade, a));
        // Arriba (+Y)
        list.add(new Quad(v011, v111, v110, v010, 0.0F, 0.0F, 1.0F, 1.0F, r * topShade, g * topShade, b * topShade, a));
        // Abajo (-Y)
        list.add(new Quad(v000, v100, v101, v001, 0.0F, 0.0F, 1.0F, 1.0F, r * bottomShade, g * bottomShade, b * bottomShade, a));
        // Derecha (+X)
        list.add(new Quad(v101, v100, v110, v111, 0.0F, 0.0F, 1.0F, 1.0F, r * sideShade, g * sideShade, b * sideShade, a));
        // Izquierda (-X)
        list.add(new Quad(v000, v001, v011, v010, 0.0F, 0.0F, 1.0F, 1.0F, r * sideShade, g * sideShade, b * sideShade, a));
    }


    private static void addRing(Map<Group, List<Quad>> groups, VfxInstance instance, VfxLayer layer,
                                Vec3 cameraPos, float partialTick) {
        float progress = instance.progress(layer, partialTick);
        float eased = ease(progress, layer.ease);
        float radius = Mth.lerp(eased, layer.ringRadius[0], layer.ringRadius[1]);
        float half = radius + Math.max(layer.thickness[0], layer.thickness[1]) * 0.5F;

        // Soporte de anclaje a hueso: si se declara "anchor":"bone:xxx", el anillo sale
        // en la punta de ese hueso (ej. muzzle flash en la boquilla del lanzallamas).
        Vec3 rawCentre;
        Vec3 boneAxis = null;
        if (layer.anchor.startsWith("bone:") && instance.anchorEntity != null) {
            VfxInstance.Anchor a = instance.anchor(layer);
            rawCentre = a.position();
            boneAxis = a.axis();
        } else {
            rawCentre = new Vec3(instance.origin.x, instance.origin.y + layer.y, instance.origin.z);
        }
        Vec3 centre = rawCentre.subtract(cameraPos);

        float[] color = (layer.color == null ? Gradient.WHITE : layer.color).sample(progress);

        // Plano del anillo: por defecto horizontal (XZ). Si el hueso tiene eje conocido,
        // construimos dos vectores perpendiculares a ese eje para orientar el anillo
        // en la dirección de disparo (muzzle flash perpendicular al chorro).
        Vec3 bL, bR, tR, tL;
        if (boneAxis != null && boneAxis.lengthSqr() > 1.0E-6D) {
            Vec3 fwd = boneAxis.normalize();
            // Elegimos un "up" auxiliar que no sea paralelo a fwd
            Vec3 aux = Math.abs(fwd.y) < 0.9D ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 right = fwd.cross(aux).normalize();
            Vec3 up2   = fwd.cross(right).normalize();
            bL = centre.add(right.scale(-half)).add(up2.scale(-half));
            bR = centre.add(right.scale( half)).add(up2.scale(-half));
            tR = centre.add(right.scale( half)).add(up2.scale( half));
            tL = centre.add(right.scale(-half)).add(up2.scale( half));
        } else {
            // Plano horizontal clásico
            bL = centre.add(-half, 0.0D, -half);
            bR = centre.add( half, 0.0D, -half);
            tR = centre.add( half, 0.0D,  half);
            tL = centre.add(-half, 0.0D,  half);
        }

        groups.computeIfAbsent(new Group(layer.texture, layer.additive), key -> new ArrayList<>())
                .add(new Quad(bL, bR, tR, tL, 0.0F, 0.0F, 1.0F, 1.0F, color[0], color[1], color[2], color[3]));
    }

    private static void addGlow(Map<Group, List<Quad>> groups, VfxInstance instance, VfxLayer layer,
                                Vec3 cameraPos, Vector3f right, Vector3f up, float partialTick) {
        float progress = instance.progress(layer, partialTick);
        float half = Mth.lerp(progress, layer.size[0], layer.size[1]) * 0.5F;
        if (half <= 0.002F) {
            return;
        }
        Vec3 centre = instance.origin.add(layer.offset).subtract(cameraPos);
        float[] color = (layer.color == null ? Gradient.WHITE : layer.color).sample(progress);
        groups.computeIfAbsent(new Group("soft_glow", true), key -> new ArrayList<>())
                .add(new Quad(
                        pole(centre, right, up, -half, -half), pole(centre, right, up, half, -half),
                        pole(centre, right, up, half, half), pole(centre, right, up, -half, half),
                        0.0F, 0.0F, 1.0F, 1.0F, color[0], color[1], color[2], color[3]));
    }

    private static void addRibbon(Map<Group, List<Quad>> groups, VfxInstance.RibbonState ribbon,
                                  Vec3 cameraPos, float nowSeconds) {
        List<VfxInstance.RibbonNode> nodes = ribbon.nodes;
        if (nodes.size() < 2) {
            return;
        }
        VfxLayer layer = ribbon.layer;
        Gradient colorGrad = layer.color == null ? Gradient.WHITE : layer.color;
        float lifetime = Math.max(0.01F, layer.ribbonLifetime);

        int count = nodes.size();
        for (int i = 0; i < count - 1; i++) {
            VfxInstance.RibbonNode n0 = nodes.get(i);
            VfxInstance.RibbonNode n1 = nodes.get(i + 1);

            float age0 = nowSeconds - n0.time();
            float age1 = nowSeconds - n1.time();
            float uAge0 = Mth.clamp(age0 / lifetime, 0.0F, 1.0F);
            float uAge1 = Mth.clamp(age1 / lifetime, 0.0F, 1.0F);

            float w0 = Mth.lerp(uAge0, layer.width[0], layer.width[1]) * 0.5F;
            float w1 = Mth.lerp(uAge1, layer.width[0], layer.width[1]) * 0.5F;

            Vec3 pos0 = n0.position();
            Vec3 pos1 = n1.position();
            Vec3 dir = pos1.subtract(pos0);
            if (dir.lengthSqr() < 1.0E-8D) {
                continue;
            }

            // Normal orientada a la camara
            Vec3 toCam = cameraPos.subtract(pos0);
            Vec3 side = dir.cross(toCam);
            if (side.lengthSqr() < 1.0E-8D) {
                side = new Vec3(0.0D, 1.0D, 0.0D);
            } else {
                side = side.normalize();
            }

            Vec3 p0L = pos0.add(side.scale(w0)).subtract(cameraPos);
            Vec3 p0R = pos0.subtract(side.scale(w0)).subtract(cameraPos);
            Vec3 p1L = pos1.add(side.scale(w1)).subtract(cameraPos);
            Vec3 p1R = pos1.subtract(side.scale(w1)).subtract(cameraPos);

            float texU0 = (float) i / (float) (count - 1);
            float texU1 = (float) (i + 1) / (float) (count - 1);

            float[] c0 = colorGrad.sample(uAge0);
            float[] c1 = colorGrad.sample(uAge1);
            float r = (c0[0] + c1[0]) * 0.5F;
            float g = (c0[1] + c1[1]) * 0.5F;
            float b = (c0[2] + c1[2]) * 0.5F;
            float a = (c0[3] + c1[3]) * 0.5F;

            if (a <= 0.005F) {
                continue;
            }

            groups.computeIfAbsent(new Group(layer.texture, layer.additive), key -> new ArrayList<>())
                    .add(new Quad(p0L, p0R, p1R, p1L,
                            texU0, 0.0F, texU1, 1.0F,
                            r, g, b, a));
        }
    }

    private static Vec3 pole(Vec3 centre, Vector3f right, Vector3f up, float alongRight, float alongUp) {
        return centre.add(right.x * alongRight + up.x * alongUp,
                right.y * alongRight + up.y * alongUp,
                right.z * alongRight + up.z * alongUp);
    }

    private static float ease(float t, String ease) {
        return switch (ease) {
            case "in" -> t * t;
            case "out" -> 1.0F - (1.0F - t) * (1.0F - t);
            default -> t;
        };
    }

    // ------------------------------------------------------------------ dibujo

    private static void draw(String texture, List<Quad> quads, Matrix4f modelView, Minecraft minecraft) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        for (Quad quad : quads) {
            vertex(buffer, quad, quad.bottomLeft(), quad.u0(), quad.v1());
            vertex(buffer, quad, quad.bottomRight(), quad.u1(), quad.v1());
            vertex(buffer, quad, quad.topRight(), quad.u1(), quad.v0());
            vertex(buffer, quad, quad.topLeft(), quad.u0(), quad.v0());
        }
        try (MeshData mesh = buffer.build()) {
            if (mesh == null) {
                return;
            }
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0,
                    ResourceLocation.fromNamespaceAndPath("gachawaifus", "textures/vfx/" + texture + ".png"));
            ShaderInstance shader = RenderSystem.getShader();
            if (shader == null) {
                return;
            }
            shader.setDefaultUniforms(VertexFormat.Mode.QUADS, modelView, RenderSystem.getProjectionMatrix(),
                    minecraft.getWindow());
            shader.apply();
            BufferUploader.draw(mesh);
            shader.clear();
        }
    }

    private static void vertex(BufferBuilder buffer, Quad quad, Vec3 position, float u, float v) {
        buffer.addVertex((float) position.x, (float) position.y, (float) position.z)
                .setUv(u, v)
                .setColor(quad.red(), quad.green(), quad.blue(), quad.alpha());
    }

    private record Group(String texture, boolean additive) {
    }

    private record Quad(Vec3 bottomLeft, Vec3 bottomRight, Vec3 topRight, Vec3 topLeft,
                        float u0, float v0, float u1, float v1,
                        float red, float green, float blue, float alpha) {
    }
}
