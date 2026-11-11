package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.HatsuneMikuEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Hatsune Miku: geo/hatsune_miku.geo.json + textures/entity/hatsune_miku.png + animations/hatsune_miku.animation.json. */
public class HatsuneMikuModel extends GeoModel<HatsuneMikuEntity> {
    @Override
    public ResourceLocation getModelResource(HatsuneMikuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/hatsune_miku.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HatsuneMikuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/hatsune_miku.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HatsuneMikuEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/hatsune_miku.animation.json");
    }

    @Override
    public void setCustomAnimations(HatsuneMikuEntity animatable, long instanceId,
                                    AnimationState<HatsuneMikuEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
        WaifuGeoConcertPose.apply(this, animatable, animationState.getPartialTick());
    }
}
