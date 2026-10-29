package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.BenBiggerEntity;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class BenBiggerRenderer extends LivingEntityRenderer<BenBiggerEntity, WaifuPlayerModel<BenBiggerEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ben_bigger.png");

    public BenBiggerRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BenBiggerEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(BenBiggerEntity entity) {
        return true;
    }
}
