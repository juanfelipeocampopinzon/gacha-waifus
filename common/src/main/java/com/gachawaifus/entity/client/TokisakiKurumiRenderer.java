package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.TokisakiKurumiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class TokisakiKurumiRenderer extends LivingEntityRenderer<TokisakiKurumiEntity, TokisakiKurumiModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/tokisaki_kurumi.png");

    public TokisakiKurumiRenderer(EntityRendererProvider.Context context) {
        // Modelo personalizado con falda gótica, coletas asimétricas, mosquete y busto 3.0F
        super(context, new TokisakiKurumiModel(context.bakeLayer(TokisakiKurumiModel.LAYER_LOCATION)), 0.5F);
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
