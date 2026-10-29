package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.QingyiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Qingyi: geo/qingyi.geo.json + textures/entity/qingyi.png + animations/qingyi.animation.json. */
public class QingyiModel extends GeoModel<QingyiEntity> {
    @Override
    public ResourceLocation getModelResource(QingyiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/qingyi.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(QingyiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/qingyi.png");
    }

    @Override
    public ResourceLocation getAnimationResource(QingyiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/qingyi.animation.json");
    }

    @Override
    public void setCustomAnimations(QingyiEntity animatable, long instanceId,
                                    AnimationState<QingyiEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
