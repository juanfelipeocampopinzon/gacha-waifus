package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.NangongYuEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class NangongYuModel extends GeoModel<NangongYuEntity> {
    @Override
    public ResourceLocation getModelResource(NangongYuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/nangong_yu.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NangongYuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/nangong_yu.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NangongYuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/nangong_yu.animation.json");
    }

    @Override
    public void setCustomAnimations(NangongYuEntity animatable, long instanceId,
                                    AnimationState<NangongYuEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
