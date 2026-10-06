package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.EllenJoeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class EllenJoeModel extends GeoModel<EllenJoeEntity> {
    @Override
    public ResourceLocation getModelResource(EllenJoeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/ellen_joe.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EllenJoeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ellen_joe.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EllenJoeEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/ellen_joe.animation.json");
    }
    /**
     * Fisica de la pieza del pecho: GeckoLib llama aqui en cada fotograma, despues de aplicar
     * las animaciones. Solo se toca la escala del hueso y un balanceo minimo, asi que las
     * puntas del rombo siguen apoyadas en el pecho.
     */
    @Override
    public void setCustomAnimations(EllenJoeEntity animatable, long instanceId,
                                    AnimationState<EllenJoeEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
    }
}
