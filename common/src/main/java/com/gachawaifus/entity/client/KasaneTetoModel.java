package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.KasaneTetoEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/** Kasane Teto: geo/kasane_teto.geo.json + textures/entity/kasane_teto.png + animations/kasane_teto.animation.json. */
public class KasaneTetoModel extends GeoModel<KasaneTetoEntity> {
    @Override
    public ResourceLocation getModelResource(KasaneTetoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/kasane_teto.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KasaneTetoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/kasane_teto.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KasaneTetoEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/kasane_teto.animation.json");
    }

    @Override
    public void setCustomAnimations(KasaneTetoEntity animatable, long instanceId,
                                    AnimationState<KasaneTetoEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
        WaifuGeoConcertPose.apply(this, animatable, animationState.getPartialTick());
    }
}
