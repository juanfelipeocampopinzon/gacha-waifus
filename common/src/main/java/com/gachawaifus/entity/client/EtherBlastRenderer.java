package com.gachawaifus.entity.client;

import com.gachawaifus.entity.EtherBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class EtherBlastRenderer extends EntityRenderer<EtherBlastEntity> {

    public EtherBlastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EtherBlastEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // El proyectil se visualiza mediante partículas constantes de éter y notas musicales emitidas en tick()
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EtherBlastEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
