package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.AnbyDemaraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class AnbyDemaraModel extends GeoModel<AnbyDemaraEntity> {
    @Override
    public ResourceLocation getModelResource(AnbyDemaraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/anby_demara.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnbyDemaraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/anby_demara.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AnbyDemaraEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/anby_demara.animation.json");
    }
    /**
     * Fisica de la pieza del pecho: GeckoLib llama aqui en cada fotograma, despues de aplicar
     * las animaciones. Solo se toca la escala del hueso y un balanceo minimo, asi que las
     * puntas del rombo siguen apoyadas en el pecho.
     */
    @Override
    public void setCustomAnimations(AnbyDemaraEntity animatable, long instanceId,
                                    AnimationState<AnbyDemaraEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
    }
}
