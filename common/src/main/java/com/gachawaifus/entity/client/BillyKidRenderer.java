package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.BillyKidEntity;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class BillyKidRenderer extends LivingEntityRenderer<BillyKidEntity, WaifuPlayerModel<BillyKidEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/billy_kid.png");

    public BillyKidRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BillyKidEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(BillyKidEntity entity) {
        return true;
    }
}