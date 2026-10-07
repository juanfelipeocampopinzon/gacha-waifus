package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.RinaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Rina</b> (Alexandrina) con el modelo de jugador SLIM y la pieza del pecho
 * <b>grande</b> (sección 3×3, capa {@link WaifuBustLayers#TUBE_BIG}), como Astra, Remielle, Kurumi
 * y Yidhari. El {@code 3.0F} del constructor es el lado del rombo y lo usa la física para convertir
 * "cuadros" en escala: con el 2.0F de la pieza normal, el tamaño y el bamboleo bailarían.
 */
public class RinaRenderer extends LivingEntityRenderer<RinaEntity, WaifuBustPlayerModel<RinaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/rina.png");

    public RinaRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(RinaEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(RinaEntity entity) {
        return true;
    }
}
