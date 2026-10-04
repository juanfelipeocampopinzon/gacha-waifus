package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.EllenJoeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EllenJoeModel extends GeoModel<EllenJoeEntity> {
    @Override
    public ResourceLocation getModelResource(EllenJoeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/ellen_joe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EllenJoeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ellen_joe.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EllenJoeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/ellen_joe.animation.json");
    }
}
