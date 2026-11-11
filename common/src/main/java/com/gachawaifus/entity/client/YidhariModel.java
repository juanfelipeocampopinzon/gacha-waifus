package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.YidhariEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Yidhari: geo/yidhari.geo.json + textures/entity/yidhari.png + animations/yidhari.animation.json. */
public class YidhariModel extends GeoModel<YidhariEntity> {
    @Override
    public ResourceLocation getModelResource(YidhariEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/yidhari.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(YidhariEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/yidhari.png");
    }

    @Override
    public ResourceLocation getAnimationResource(YidhariEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/yidhari.animation.json");
    }

    @Override
    public void setCustomAnimations(YidhariEntity animatable, long instanceId,
                                    AnimationState<YidhariEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
