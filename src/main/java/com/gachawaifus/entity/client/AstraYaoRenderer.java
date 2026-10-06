package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.AstraYaoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class AstraYaoRenderer extends LivingEntityRenderer<AstraYaoEntity, WaifuBustPlayerModel<AstraYaoEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/astra_yao.png");

    public AstraYaoRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con la pieza del pecho (la grande) en su propia capa.
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AstraYaoEntity entity) {
        return TEXTURE;
    }
}

