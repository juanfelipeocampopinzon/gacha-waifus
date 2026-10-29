package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.CissiaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class CissiaModel extends GeoModel<CissiaEntity> {
    @Override
    public ResourceLocation getModelResource(CissiaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/cissia.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CissiaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/cissia.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CissiaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/cissia.animation.json");
    }

    @Override
    public void setCustomAnimations(CissiaEntity animatable, long instanceId,
                                    AnimationState<CissiaEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
