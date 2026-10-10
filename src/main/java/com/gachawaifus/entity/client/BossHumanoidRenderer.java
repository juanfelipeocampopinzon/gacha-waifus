package com.gachawaifus.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Base de los jefes humanoides (modelo de jugador ANCHO, el de Steve) que además dibuja su
 * nametag <b>más allá de los 64 bloques</b> del corte vanilla.
 *
 * <p>El nametag normal se corta a 64 bloques ({@code ClientHooks.isNameplateInRenderDistance},
 * limitado por el atributo {@code nametag_distance} cuyo máximo permitido es justo 64): no hay
 * forma de subirlo por atributo, así que aquí se replica el dibujo de
 * {@code EntityRenderer#renderNameTag} sin ese corte. La escala del texto crece con la distancia
 * para que el nombre siga siendo legible de lejos (a menos de 64 lo dibuja el propio juego).
 */
public abstract class BossHumanoidRenderer<T extends Monster>
        extends LivingEntityRenderer<T, PlayerModel<T>> {

    /** Cuadrado del alcance vanilla (64 bloques): por debajo de aquí dibuja el juego. */
    private static final double VANILLA_NAME_RANGE_SQ = 64.0D * 64.0D;

    protected BossHumanoidRenderer(EntityRendererProvider.Context context, float shadowRadius) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), shadowRadius);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        double distanceSq = this.entityRenderDispatcher.distanceToSqr(entity);
        if (distanceSq > VANILLA_NAME_RANGE_SQ && entity.hasCustomName()) {
            this.renderFarNameTag(entity, entity.getDisplayName(), poseStack, buffer, packedLight,
                    partialTick, distanceSq);
        }
    }

    /** Cuerpo de {@code EntityRenderer#renderNameTag} sin el corte de 64 bloques. */
    private void renderFarNameTag(T entity, Component displayName, PoseStack poseStack,
                                  MultiBufferSource buffer, int packedLight, float partialTick,
                                  double distanceSq) {
        Vec3 anchor = entity.getAttachments().getNullable(
                EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        if (anchor == null) {
            return;
        }
        // Tamaño normal hasta 64 bloques; hasta 6x a ~384, para que se lea de lejos.
        float scale = 0.025F * (float) Mth.clamp(Math.sqrt(distanceSq) / 64.0D, 1.0D, 6.0D);
        poseStack.pushPose();
        poseStack.translate(anchor.x, anchor.y + 0.5D, anchor.z);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(scale, -scale, scale);
        Matrix4f matrix = poseStack.last().pose();
        Font font = this.getFont();
        float x = (float) (-font.width(displayName) / 2);
        int background = (int) (0.25F * 255.0F) << 24;
        font.drawInBatch(displayName, x, 0.0F, 553648127, false, matrix, buffer,
                Font.DisplayMode.SEE_THROUGH, background, packedLight);
        font.drawInBatch(displayName, x, 0.0F, -1, false, matrix, buffer,
                Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }
}
