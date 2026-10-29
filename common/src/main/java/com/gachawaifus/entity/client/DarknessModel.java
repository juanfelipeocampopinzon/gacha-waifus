package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.DarknessEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class DarknessModel extends GeoModel<DarknessEntity> {
    @Override
    public ResourceLocation getModelResource(DarknessEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/darkness.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DarknessEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/darkness.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DarknessEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/darkness.animation.json");
    }

    @Override
    public void setCustomAnimations(DarknessEntity animatable, long instanceId,
                                    AnimationState<DarknessEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
