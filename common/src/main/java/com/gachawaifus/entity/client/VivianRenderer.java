package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.VivianEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class VivianRenderer extends LivingEntityRenderer<VivianEntity, WaifuBustPlayerModel<VivianEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/vivian.png");

    public VivianRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(VivianEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(VivianEntity entity) {
        return true;
    }
}
