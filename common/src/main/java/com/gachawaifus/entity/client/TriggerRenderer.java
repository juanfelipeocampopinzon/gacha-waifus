package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.TriggerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class TriggerRenderer extends LivingEntityRenderer<TriggerEntity, WaifuBustPlayerModel<TriggerEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/trigger.png");

    public TriggerRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(TriggerEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(TriggerEntity entity) {
        return true;
    }
}
