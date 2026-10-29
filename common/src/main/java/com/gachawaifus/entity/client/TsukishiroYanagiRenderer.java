package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.TsukishiroYanagiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class TsukishiroYanagiRenderer extends LivingEntityRenderer<TsukishiroYanagiEntity, WaifuBustPlayerModel<TsukishiroYanagiEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/tsukishiro_yanagi.png");

    public TsukishiroYanagiRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TsukishiroYanagiEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(TsukishiroYanagiEntity entity) {
        return true;
    }
}
