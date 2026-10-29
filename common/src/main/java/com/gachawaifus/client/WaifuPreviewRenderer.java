package com.gachawaifus.client;

import com.gachawaifus.gacha.WaifuRoster;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Pinta a la waifu de un banner dentro de la GUI.
 *
 * <p>Primero intenta el modelo 3D de verdad (el mismo renderer del juego, vía
 * {@link InventoryScreen#renderEntityInInventory}) girando despacio sobre sí misma. Si algo falla
 * —no hay mundo, la entidad no se puede crear o su renderer revienta— cae al <b>token ampliado</b>,
 * así la pantalla nunca se queda en blanco ni tumba el cliente por un modelo raro (GeckoLib).
 */
public final class WaifuPreviewRenderer {

    /** Entidad de muestra reutilizada: crear una por frame sería un desastre para el GC. */
    @Nullable
    private static LivingEntity preview;
    @Nullable
    private static String previewId;
    @Nullable
    private static Level previewLevel;
    /** Si el renderer de esa waifu ha fallado, no se vuelve a intentar hasta abrir otra pantalla. */
    private static boolean broken;

    private WaifuPreviewRenderer() {
    }

    /**
     * Dibuja a la waifu (o su token) dentro del rectángulo indicado.
     *
     * @param angleDegrees giro horizontal en grados; 180 es de frente a la cámara
     */
    public static void render(GuiGraphics graphics, @Nullable WaifuRoster.Entry entry,
                              int x1, int y1, int x2, int y2, float angleDegrees) {
        if (entry == null) {
            return;
        }
        graphics.enableScissor(x1, y1, x2, y2);
        boolean drawn = false;
        if (!broken) {
            LivingEntity entity = preview(entry);
            if (entity != null) {
                try {
                    float centerX = (x1 + x2) / 2.0F;
                    float centerY = (y1 + y2) / 2.0F;
                    float height = entity.getBbHeight();
                    // Ajuste para que quepan igual las waifus altas y las bajas.
                    float scale = (y2 - y1) * 0.42F * (1.8F / Math.max(1.2F, height));

                    entity.setYRot(angleDegrees);
                    entity.setXRot(0.0F);
                    entity.yRotO = angleDegrees;
                    entity.yBodyRot = angleDegrees;
                    entity.yBodyRotO = angleDegrees;
                    entity.yHeadRot = angleDegrees;
                    entity.yHeadRotO = angleDegrees;
                    // Sin esto GeckoLib se queda en la pose de reposo y algunos modelos no cargan.
                    entity.tickCount = (int) (Util.getMillis() / 50L) % 100000;

                    InventoryScreen.renderEntityInInventory(graphics, centerX, centerY, scale,
                            new Vector3f(0.0F, height / 2.0F + 0.0625F * entity.getScale(), 0.0F),
                            new Quaternionf().rotateZ((float) Math.PI),
                            new Quaternionf(),
                            entity);
                    drawn = true;
                } catch (Throwable t) {
                    // Un modelo problemático no puede tumbar la interfaz: token y a seguir.
                    broken = true;
                    preview = null;
                }
            }
        }
        if (!drawn) {
            renderToken(graphics, entry, (x1 + x2) / 2.0F, (y1 + y2) / 2.0F);
        }
        graphics.disableScissor();
    }

    /** Giro lento y continuo (una vuelta cada ~16 s), con una leve inclinación de entrada. */
    public static float idleAngle(long millis) {
        return 180.0F + (millis % 16000L) / 16000.0F * 360.0F;
    }

    /** Token ampliado 5×, el plan B cuando el modelo no se puede pintar. */
    private static void renderToken(GuiGraphics graphics, WaifuRoster.Entry entry, float centerX,
                                    float centerY) {
        ItemStack stack = new ItemStack(entry.token().get());
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 100.0F);
        float scale = 5.0F;
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.renderItem(stack, -8, -8);
        graphics.pose().popPose();
    }

    /** Reinicia la caché (al cerrar la pantalla, para no retener una entidad de otro mundo). */
    public static void reset() {
        preview = null;
        previewId = null;
        previewLevel = null;
        broken = false;
    }

    @Nullable
    private static LivingEntity preview(WaifuRoster.Entry entry) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null) {
            return null;
        }
        if (preview != null && entry.id().equals(previewId) && previewLevel == level) {
            return preview;
        }
        preview = null;
        previewId = null;
        previewLevel = null;
        try {
            Entity entity = entry.type().get().create(level);
            if (entity instanceof LivingEntity living) {
                if (living instanceof net.minecraft.world.entity.Mob mob) {
                    mob.setNoAi(true);
                }
                living.setInvulnerable(true);
                living.setSilent(true);
                preview = living;
                previewId = entry.id();
                previewLevel = level;
                return living;
            }
        } catch (Throwable t) {
            broken = true;
        }
        return null;
    }

    /** Ajusta un valor a un rango (azúcar para la pantalla). */
    public static float clamp01(float value) {
        return Mth.clamp(value, 0.0F, 1.0F);
    }
}
