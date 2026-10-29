package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.NormaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class NormaModel extends GeoModel<NormaEntity> {
    @Override
    public ResourceLocation getModelResource(NormaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/norma.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NormaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/norma.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NormaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/norma.animation.json");
    }

    @Override
    public void setCustomAnimations(NormaEntity animatable, long instanceId,
                                    AnimationState<NormaEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
