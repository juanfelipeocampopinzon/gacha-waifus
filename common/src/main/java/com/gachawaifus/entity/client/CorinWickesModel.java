package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.CorinWickesEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class CorinWickesModel extends GeoModel<CorinWickesEntity> {
    @Override
    public ResourceLocation getModelResource(CorinWickesEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/corin_wickes.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CorinWickesEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/corin_wickes.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CorinWickesEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID,
                "animations/corin_wickes.animation.json");
    }

    @Override
    public void setCustomAnimations(CorinWickesEntity animatable, long instanceId,
                                    AnimationState<CorinWickesEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
