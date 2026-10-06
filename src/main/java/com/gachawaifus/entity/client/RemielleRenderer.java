package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.RemielleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class RemielleRenderer extends LivingEntityRenderer<RemielleEntity, WaifuBustPlayerModel<RemielleEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/remielle.png");

    public RemielleRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con la pieza del pecho (la grande) en su propia capa.
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true), 0.5F);
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

