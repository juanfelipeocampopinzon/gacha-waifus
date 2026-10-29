package com.gachawaifus.entity.client;

import com.gachawaifus.entity.CurlyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.WolfModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Curly</b>: el modelo de lobo vanilla con la textura de lobo salvaje. Se dibuja un
 * poco más grande de lo normal para que se note que es un perro de jefe.
 */
public class CurlyRenderer extends MobRenderer<CurlyEntity, WolfModel<CurlyEntity>> {

    private static final ResourceLocation WOLF_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/wolf/wolf.png");

    public CurlyRenderer(EntityRendererProvider.Context context) {
        super(context, new WolfModel<>(context.bakeLayer(ModelLayers.WOLF)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(CurlyEntity entity) {
        return WOLF_TEXTURE;
    }

    @Override
    protected void scale(CurlyEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.25F, 1.25F, 1.25F);
    }

    @Override
    public boolean shouldShowName(CurlyEntity entity) {
        return true;
    }
}
