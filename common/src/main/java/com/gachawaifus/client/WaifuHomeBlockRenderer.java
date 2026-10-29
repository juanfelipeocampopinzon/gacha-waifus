package com.gachawaifus.client;

import com.gachawaifus.block.WaifuHomeBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Pinta el Bloque de Hogar como el bloque que esté imitando.
 *
 * <p>El bloque en sí tiene {@code RenderShape.INVISIBLE} (se lo da {@code BaseEntityBlock}), así que
 * el cubo lo dibuja exclusivamente esta clase con {@code renderSingleBlock}: la misma ruta que usan
 * el {@code BlockDisplay} o las flores del gólem, y por tanto el tinte de la grama, el modelo
 * conexo de una cama o la losa media funcionan sin escribir ningún renderer propio.
 *
 * <p>El {@code PoseStack} que llega a un renderer de bloque ya viene desplazado a la esquinita del
 * bloque (menos la cámara), así que no hay que traducir nada: el cubo cae exactamente encima del
 * hueco que ocupa, y las caras comparten el orden que llevaría el bloque verdadero. La luz tampoco se
 * calcula aquí — viene en {@code light}, medida en nuestra posición, que es la posición correcta.
 */
public class WaifuHomeBlockRenderer implements BlockEntityRenderer<WaifuHomeBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public WaifuHomeBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(WaifuHomeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        this.blockRenderer.renderSingleBlock(blockEntity.mimicState(), poseStack, bufferSource,
                packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
