package com.gachawaifus.vfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Las dos capas que no se dibujan en el mundo sino en la pantalla: {@code flash} (un velo de color
 * sobre el HUD) y {@code shake} (un temblor de la cámara). Aquí solo se calcula; cada loader llama
 * a {@link #renderFlash} desde su evento de HUD y suma {@link #shakeAngles} a la cámara.
 *
 * <p>Accesibilidad: el flash respeta la opción de vanilla "Ocultar destellos" y el interruptor
 * {@code /gwvfx flash off}; el temblor se multiplica por "Efectos de distorsión" de vanilla.
 */
public final class VfxScreen {

    /** Interruptor del comando {@code /gwvfx flash on|off}. */
    public static boolean flashEnabled = true;

    /** Amplitud 1 del JSON = este número de grados de temblor. */
    private static final float SHAKE_DEGREES = 8.0F;
    /** Un flash "world" se apaga del todo a esta distancia de la cámara. */
    private static final double FLASH_RANGE = 48.0D;

    private VfxScreen() {
    }

    /** Velo de color a pantalla completa. Se llama desde el evento de HUD de cada loader. */
    public static void renderFlash(GuiGraphics graphics, float partialTick) {
        if (!flashEnabled) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideLightningFlash().get() || minecraft.gameRenderer == null) {
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float best = 0.0F;
        float[] color = null;
        for (VfxInstance instance : VfxManager.instances()) {
            for (VfxLayer layer : instance.stage.layers) {
                if (!"flash".equals(layer.type) || !instance.visible(layer, partialTick)) {
                    continue;
                }
                float progress = instance.progress(layer, partialTick);
                // Sube en el primer 15 % y cae en curva: un golpe de luz, no una lámpara.
                float envelope = progress < 0.15F ? progress / 0.15F : (1.0F - progress) * (1.0F - progress) / 0.7225F;
                float alpha = layer.intensity * Mth.clamp(envelope, 0.0F, 1.0F);
                if ("world".equals(layer.scope)) {
                    double distance = instance.point(layer).distanceTo(camera);
                    alpha *= (float) Mth.clamp(1.0D - distance / FLASH_RANGE, 0.0D, 1.0D);
                }
                if (alpha > best) {
                    best = alpha;
                    color = layer.flashColor;
                }
            }
        }
        if (color == null || best < 0.01F) {
            return;
        }
        int argb = ((int) (Mth.clamp(best, 0.0F, 0.85F) * 255.0F) << 24)
                | ((int) (color[0] * 255.0F) << 16) | ((int) (color[1] * 255.0F) << 8) | (int) (color[2] * 255.0F);
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), argb);
    }

    /**
     * Desvío de la cámara por los {@code shake} activos.
     *
     * @return {yaw, pitch} en grados que hay que SUMAR a los de la cámara ({0,0} si no hay temblor)
     */
    public static float[] shakeAngles(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.gameRenderer == null || VfxManager.instances().isEmpty()) {
            return new float[]{0.0F, 0.0F};
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float amplitude = 0.0F;
        for (VfxInstance instance : VfxManager.instances()) {
            for (VfxLayer layer : instance.stage.layers) {
                if (!"shake".equals(layer.type) || !instance.visible(layer, partialTick)) {
                    continue;
                }
                float local = instance.localSeconds(layer, partialTick);
                float progress = instance.progress(layer, partialTick);
                // Entra en 50 ms y se apaga en el último 30 %: un temblor largo no corta en seco.
                float envelope = Math.min(1.0F, local / 0.05F) * Math.min(1.0F, (1.0F - progress) / 0.3F);
                float falloff = 1.0F;
                if ("linear".equals(layer.falloff)) {
                    double distance = instance.point(layer).distanceTo(camera);
                    falloff = (float) Mth.clamp(1.0D - distance / Math.max(1.0F, layer.radius), 0.0D, 1.0D);
                }
                amplitude += layer.amplitude * Mth.clamp(envelope, 0.0F, 1.0F) * falloff;
            }
        }
        if (amplitude <= 0.0005F) {
            return new float[]{0.0F, 0.0F};
        }
        float scale = minecraft.options.screenEffectScale().get().floatValue();
        float degrees = Math.min(1.0F, amplitude) * SHAKE_DEGREES * scale;
        float time = (minecraft.level.getGameTime() + partialTick) / 20.0F;
        // Dos senos de frecuencias que no casan entre sí: vibra sin parecer un péndulo.
        float yaw = (Mth.sin(time * 47.0F) * 0.6F + Mth.sin(time * 83.0F + 1.3F) * 0.4F) * degrees;
        float pitch = (Mth.sin(time * 53.0F + 0.7F) * 0.6F + Mth.sin(time * 71.0F + 2.1F) * 0.4F) * degrees;
        return new float[]{yaw, pitch};
    }
}
