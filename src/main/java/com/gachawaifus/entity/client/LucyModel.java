package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.LucyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class LucyModel extends GeoModel<LucyEntity> {
    @Override
    public ResourceLocation getModelResource(LucyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/lucy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LucyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/lucy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LucyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/lucy.animation.json");
    }

    @Override
    public void setCustomAnimations(LucyEntity animatable, long instanceId,
                                    AnimationState<LucyEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
