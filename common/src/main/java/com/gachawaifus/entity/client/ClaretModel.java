package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.ClaretEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class ClaretModel extends GeoModel<ClaretEntity> {
    @Override
    public ResourceLocation getModelResource(ClaretEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/claret.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ClaretEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/claret.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ClaretEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/claret.animation.json");
    }

    @Override
    public void setCustomAnimations(ClaretEntity animatable, long instanceId,
                                    AnimationState<ClaretEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
