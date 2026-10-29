package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.CaesarEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class CaesarModel extends GeoModel<CaesarEntity> {
    @Override
    public ResourceLocation getModelResource(CaesarEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/caesar.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CaesarEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/caesar.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CaesarEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/caesar.animation.json");
    }

    @Override
    public void setCustomAnimations(CaesarEntity animatable, long instanceId,
                                    AnimationState<CaesarEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
