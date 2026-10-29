package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.JuFufuEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class JuFufuModel extends GeoModel<JuFufuEntity> {
    @Override
    public ResourceLocation getModelResource(JuFufuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/ju_fufu.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JuFufuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ju_fufu.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JuFufuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/ju_fufu.animation.json");
    }

    @Override
    public void setCustomAnimations(JuFufuEntity animatable, long instanceId,
                                    AnimationState<JuFufuEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
