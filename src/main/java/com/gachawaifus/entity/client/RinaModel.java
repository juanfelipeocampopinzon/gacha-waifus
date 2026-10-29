package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.RinaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Rina: geo/rina.geo.json + textures/entity/rina.png + animations/rina.animation.json. */
public class RinaModel extends GeoModel<RinaEntity> {
    @Override
    public ResourceLocation getModelResource(RinaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/rina.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RinaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/rina.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RinaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/rina.animation.json");
    }

    @Override
    public void setCustomAnimations(RinaEntity animatable, long instanceId,
                                    AnimationState<RinaEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
