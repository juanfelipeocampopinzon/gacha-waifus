package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.NekomataEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class NekomataModel extends GeoModel<NekomataEntity> {
    @Override
    public ResourceLocation getModelResource(NekomataEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/nekomata.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NekomataEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/nekomata.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NekomataEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/nekomata.animation.json");
    }

    @Override
    public void setCustomAnimations(NekomataEntity animatable, long instanceId,
                                    AnimationState<NekomataEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
