package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.ZhaoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class ZhaoRenderer extends LivingEntityRenderer<ZhaoEntity, WaifuBustPlayerModel<ZhaoEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/zhao.png");

    public ZhaoRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true, 2.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ZhaoEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(ZhaoEntity entity) {
        return true;
    }
}
