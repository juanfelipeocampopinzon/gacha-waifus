package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.BurniceWhiteEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BurniceWhiteModel extends GeoModel<BurniceWhiteEntity> {
    @Override
    public ResourceLocation getModelResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/burnice_white.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/burnice_white.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/burnice_white.animation.json");
    }
}
