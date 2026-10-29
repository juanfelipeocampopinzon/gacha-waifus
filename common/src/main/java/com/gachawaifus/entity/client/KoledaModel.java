package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.KoledaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class KoledaModel extends GeoModel<KoledaEntity> {
    @Override
    public ResourceLocation getModelResource(KoledaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/koleda.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KoledaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/koleda.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KoledaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/koleda.animation.json");
    }

    @Override
    public void setCustomAnimations(KoledaEntity animatable, long instanceId,
                                    AnimationState<KoledaEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
