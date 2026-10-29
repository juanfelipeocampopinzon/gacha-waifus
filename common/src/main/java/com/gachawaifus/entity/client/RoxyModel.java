package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.RoxyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Roxy Ifrita Pryce: geo/roxy.geo.json + textures/entity/roxy.png + animations/roxy.animation.json. */
public class RoxyModel extends GeoModel<RoxyEntity> {
    @Override
    public ResourceLocation getModelResource(RoxyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/roxy.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RoxyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/roxy.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RoxyEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/roxy.animation.json");
    }

    @Override
    public void setCustomAnimations(RoxyEntity animatable, long instanceId,
                                    AnimationState<RoxyEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
