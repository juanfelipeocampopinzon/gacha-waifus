package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.BurniceWhiteEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
        // Giro de cabeza y torso hacia el objetivo. Se SUMA sobre la animación porque GeckoLib
        // reescribe la rotación de cada hueso en cada fotograma, así que no se acumula. Se apaga
        // mientras ejecuta una habilidad y al estar sentada, para no pelear con esos clips ni con
        // WaifuGeoSitPose.
        if (animatable.isAlive() && !animatable.isInSittingPose() && !animatable.isPerformingAction()) {
            float turn = Mth.clamp(Mth.wrapDegrees(animatable.getYHeadRot() - animatable.yBodyRot),
                    -35.0F, 35.0F);
            this.getBone("head").ifPresent(bone ->
                    bone.setRotY(bone.getRotY() + turn * Mth.DEG_TO_RAD * 0.7F));
            this.getBone("body").ifPresent(bone ->
                    bone.setRotY(bone.getRotY() + turn * Mth.DEG_TO_RAD * 0.15F));
        }
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
