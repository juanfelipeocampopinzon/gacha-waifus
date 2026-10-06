package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.BurniceWhiteEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class BurniceWhiteModel extends GeoModel<BurniceWhiteEntity> {
    @Override
    public ResourceLocation getModelResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/burnice_white.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/burnice_white.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/burnice_white.animation.json");
    }
    /**
     * Fisica de la pieza del pecho: GeckoLib llama aqui en cada fotograma, despues de aplicar
     * las animaciones. Solo se toca la escala del hueso y un balanceo minimo, asi que las
     * puntas del rombo siguen apoyadas en el pecho.
     */
    @Override
    public void setCustomAnimations(BurniceWhiteEntity animatable, long instanceId,
                                    AnimationState<BurniceWhiteEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
    }
}
