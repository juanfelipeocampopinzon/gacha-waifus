package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.MiyabiMisiramaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MiyabiMisiramaModel extends GeoModel<MiyabiMisiramaEntity> {
    @Override
    public ResourceLocation getModelResource(MiyabiMisiramaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/miyabi.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MiyabiMisiramaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/miyabi.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MiyabiMisiramaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/miyabi.animation.json");
    }
}
