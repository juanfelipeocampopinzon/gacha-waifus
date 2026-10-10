package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.YixuanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Yi Xuan con el modelo de jugador slim + pieza del pecho grande
 * ({@code WaifuBustLayers.TUBE_BIG}, sección 3.0F). Alta Preceptora de la Cima Yunkui: tinta
 * áurica y talismanes. Su skin es 64×128: la mitad inferior lleva el desplegado de la pieza
 * anclado en (0, 121).
 */
public class YixuanRenderer extends LivingEntityRenderer<YixuanEntity, WaifuBustPlayerModel<YixuanEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/yixuan.png");

    public YixuanRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(YixuanEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(YixuanEntity entity) {
        return true;
    }
}
