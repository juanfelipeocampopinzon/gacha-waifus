package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.AliceEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class AliceModel extends GeoModel<AliceEntity> {
    @Override
    public ResourceLocation getModelResource(AliceEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/alice.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AliceEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/alice.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AliceEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/alice.animation.json");
    }

    @Override
    public void setCustomAnimations(AliceEntity animatable, long instanceId,
                                    AnimationState<AliceEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
