package com.gachawaifus.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Capa {@code clone}: la waifu se vuelve a dibujar en otro sitio, teñida y translúcida (los clones de
 * sombra de Kurumi). Es la MISMA entidad con su pose actual, así que el clon dispara cuando ella
 * dispara; no existe en el servidor ni tiene colisión.
 *
 * <p>El renderer de la waifu tiene que colaborar: mientras {@link #active()} es true devuelve
 * {@link #argb()} como color, un render type translúcido y no pinta el nombre (ver
 * {@code TokisakiKurumiRenderer}). Una waifu cuyo renderer no lo haga sale como copia opaca normal.
 */
public final class VfxClones {

    private static boolean active = false;
    private static int argb = 0xFFFFFFFF;

    private VfxClones() {
    }

    /** true solo mientras se dibuja un clon: los renderers lo consultan para teñirse. */
    public static boolean active() {
        return active;
    }

    /** Color ARGB del clon que se está dibujando. */
    public static int argb() {
        return argb;
    }

    /**
     * Dibuja los clones de todas las capas {@code clone} visibles. Se llama desde
     * {@link VfxRenderer#render} con la matriz de vista del mundo ya aplicada por vanilla.
     *
     * <p>Claves: {@code offset} (posición del clon respecto a la waifu, relativa a la mirada),
     * {@code color} (degradado: el alfa es la opacidad del clon), {@code duration} y {@code rise}
     * (bloques que el clon emerge del suelo al aparecer y se hunde al irse; 0 = aparece sin más).
     */
    static void render(Vec3 cameraPos, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        boolean drew = false;
        try {
            for (VfxInstance instance : VfxManager.instances()) {
                Entity entity = instance.anchorEntity;
                if (entity == null || entity.isRemoved()) {
                    continue;
                }
                for (VfxLayer layer : instance.stage.layers) {
                    if (!"clone".equals(layer.type) || !instance.visible(layer, partialTick)) {
                        continue;
                    }
                    float progress = instance.progress(layer, partialTick);
                    float[] color = (layer.color == null ? Gradient.WHITE : layer.color).sample(progress);
                    if (color[3] <= 0.02F) {
                        continue;
                    }
                    // Emerge en el primer 20 % y se hunde en el último 20 %.
                    float sink = layer.rise * (1.0F - Mth.clamp(Math.min(progress, 1.0F - progress) / 0.2F, 0.0F, 1.0F));
                    Vec3 base = new Vec3(Mth.lerp(partialTick, entity.xo, entity.getX()),
                            Mth.lerp(partialTick, entity.yo, entity.getY()),
                            Mth.lerp(partialTick, entity.zo, entity.getZ()));
                    Vec3 at = base.add(instance.local(layer.offset)).add(0.0D, -sink, 0.0D).subtract(cameraPos);
                    active = true;
                    argb = ((int) (Mth.clamp(color[3], 0.0F, 1.0F) * 255.0F) << 24)
                            | ((int) (color[0] * 255.0F) << 16) | ((int) (color[1] * 255.0F) << 8)
                            | (int) (color[2] * 255.0F);
                    float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
                    dispatcher.render(entity, at.x, at.y, at.z, yaw, partialTick, new PoseStack(), buffers,
                            0xF000F0);
                    drew = true;
                }
            }
        } finally {
            active = false;
            argb = 0xFFFFFFFF;
            if (drew) {
                buffers.endBatch();
            }
        }
    }
}
