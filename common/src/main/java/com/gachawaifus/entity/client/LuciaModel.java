package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.LuciaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class LuciaModel extends GeoModel<LuciaEntity> {
    @Override
    public ResourceLocation getModelResource(LuciaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/lucia.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LuciaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/lucia.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LuciaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/lucia.animation.json");
    }

    @Override
    public void setCustomAnimations(LuciaEntity animatable, long instanceId,
                                    AnimationState<LuciaEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
