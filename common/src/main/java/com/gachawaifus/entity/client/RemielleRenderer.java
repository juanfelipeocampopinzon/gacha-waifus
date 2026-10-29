package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.RemielleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class RemielleRenderer extends LivingEntityRenderer<RemielleEntity, RemielleModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/remielle.png");

    public RemielleRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con la pieza del pecho (la grande) y sus alas en una capa propia.
        super(context, new RemielleModel(context.bakeLayer(RemielleModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RemielleEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(RemielleEntity entity) {
        return true;
    }
}

