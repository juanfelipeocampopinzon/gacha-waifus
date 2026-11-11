package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.AkitaNeruEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Akita Neru: geo/akita_neru.geo.json + textures/entity/akita_neru.png + animations/akita_neru.animation.json. */
public class AkitaNeruModel extends GeoModel<AkitaNeruEntity> {
    @Override
    public ResourceLocation getModelResource(AkitaNeruEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/akita_neru.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AkitaNeruEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/akita_neru.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AkitaNeruEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/akita_neru.animation.json");
    }

    @Override
    public void setCustomAnimations(AkitaNeruEntity animatable, long instanceId,
                                    AnimationState<AkitaNeruEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
        WaifuGeoConcertPose.apply(this, animatable, animationState.getPartialTick());
    }
}
