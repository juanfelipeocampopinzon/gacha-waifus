package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.ZhuYuanEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Zhu Yuan con el modelo de jugador slim + pieza del pecho grande
 * ({@code WaifuBustLayers.TUBE_BIG}, sección 3.0F). Oficial del NEPS, se mueve por las calles
 * de Sixth Street. Su skin es 64×128: la mitad inferior lleva el desplegado de la pieza
 * anclado en (0, 121).
 */
public class ZhuYuanRenderer extends LivingEntityRenderer<ZhuYuanEntity, WaifuBustPlayerModel<ZhuYuanEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/zhu_yuan.png");

    public ZhuYuanRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ZhuYuanEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(ZhuYuanEntity entity) {
        return true;
    }
}
