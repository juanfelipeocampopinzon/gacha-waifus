package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.MiyabiMisiramaEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class MiyabiMisiramaModel extends GeoModel<MiyabiMisiramaEntity> {
    @Override
    public ResourceLocation getModelResource(MiyabiMisiramaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/miyabi.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MiyabiMisiramaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/miyabi.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MiyabiMisiramaEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/miyabi.animation.json");
    }
    /**
     * Fisica de la pieza del pecho: GeckoLib llama aqui en cada fotograma, despues de aplicar
     * las animaciones. Solo se toca la escala del hueso y un balanceo minimo, asi que las
     * puntas del rombo siguen apoyadas en el pecho.
     */
    @Override
    public void setCustomAnimations(MiyabiMisiramaEntity animatable, long instanceId,
                                    AnimationState<MiyabiMisiramaEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
    }
}
