package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.TokisakiKurumiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class TokisakiKurumiRenderer extends LivingEntityRenderer<TokisakiKurumiEntity, WaifuBustPlayerModel<TokisakiKurumiEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/tokisaki_kurumi.png");

    public TokisakiKurumiRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con la pieza del pecho (la grande, seccion 3x3) en su propia capa.
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TokisakiKurumiEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(TokisakiKurumiEntity entity) {
        return true;
    }
}
