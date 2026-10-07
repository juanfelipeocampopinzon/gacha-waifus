package com.gachawaifus.client.color;

import com.gachawaifus.color.ColorCombat;
import com.gachawaifus.color.ColorSettings;
import com.gachawaifus.color.WaifuColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

/**
 * Etiqueta flotante con el color del enemigo, encima de su cabeza.
 *
 * <p>Es la forma "buena" de mostrar el color: un nombre propio en el mob sería más fácil, pero
 * {@code Mob.checkDespawn} <b>no borra</b> los mobs con nombre (se volverían permanentes y
 * romperían las granjas), así que el color se pinta como capa de render y el mob sigue siendo
 * anónimo para el juego.
 *
 * <p>Se dibuja solo para mobs hostiles ({@link Enemy}), a ≤24 bloques y con oclusión normal
 * ({@code DisplayMode.NORMAL}, no se ve a través de las paredes).
 */
public class ColorTagLayer<T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>>
        extends RenderLayer<T, M> {

    private static final double MAX_DISTANCE = 24.0D;
    /** Cuánto por encima de la cabeza se dibuja, en bloques. */
    private static final double HEIGHT_OFFSET = 0.55D;
    /** Fondo semitransparente para que el texto se lea sobre cualquier cosa. */
    private static final int BACKGROUND = 0x55000000;

    public ColorTagLayer(LivingEntityRenderer<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ColorSettings.ENABLED || !ColorSettings.FLOATING_LABEL) return;
        if (!(entity instanceof Enemy)) return;
        if (entity.isInvisible() || entity.isDeadOrDying()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player viewer = minecraft.player;
        if (viewer == null || entity == viewer) return;
        if (viewer.distanceToSqr(entity) > MAX_DISTANCE * MAX_DISTANCE) return;

        WaifuColor color = ColorCombat.colorOf(entity);
        if (color == null) return;

        int argb = argb(color);
        // Cuadradito de color delante del nombre: el color se ve de un vistazo aunque no se lea.
        MutableComponent label = Component.empty();
        if (ColorSettings.LABEL_SWATCH) {
            label.append(Component.literal("\u2588\u2588")
                    .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(argb & 0xFFFFFF))));
            label.append(Component.literal(" "));
        }
        label.append(color.displayName());

        Font font = minecraft.font;

        pose.pushPose();
        pose.translate(0.0D, entity.getBbHeight() + HEIGHT_OFFSET, 0.0D);
        pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        pose.scale(-0.025F, -0.025F, 0.025F);
        Matrix4f matrix = pose.last().pose();
        font.drawInBatch(label, -font.width(label) / 2.0F, 0.0F, argb, true, matrix, buffer,
                Font.DisplayMode.NORMAL, BACKGROUND, packedLight);
        pose.popPose();
    }

    /** El color del mob como ARGB opaco, ya aclarado si es tan oscuro que no se vería. */
    private static int argb(WaifuColor color) {
        float[] rgb = ColorCombat.visibleRgb(color);
        return 0xFF000000
                | (Math.round(rgb[0] * 255.0F) << 16)
                | (Math.round(rgb[1] * 255.0F) << 8)
                | Math.round(rgb[2] * 255.0F);
    }
}
