package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.NicoleDemaraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class NicoleDemaraModel extends GeoModel<NicoleDemaraEntity> {
    @Override
    public ResourceLocation getModelResource(NicoleDemaraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/nicole_demara.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NicoleDemaraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/nicole_demara.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NicoleDemaraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/nicole_demara.animation.json");
    }
    /**
     * Fisica de la pieza del pecho: GeckoLib llama aqui en cada fotograma, despues de aplicar
     * las animaciones. Solo se toca la escala del hueso y un balanceo minimo, asi que las
     * puntas del rombo siguen apoyadas en el pecho.
     */
    @Override
    public void setCustomAnimations(NicoleDemaraEntity animatable, long instanceId,
                                    AnimationState<NicoleDemaraEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
    }
}
