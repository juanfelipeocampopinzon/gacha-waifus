package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.OrphieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class OrphieRenderer extends LivingEntityRenderer<OrphieEntity, WaifuBustPlayerModel<OrphieEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/orphie.png");

    public OrphieRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true, 2.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(OrphieEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(OrphieEntity entity) {
        return true;
    }
}
