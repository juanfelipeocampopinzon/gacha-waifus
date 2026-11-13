package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.TokisakiKurumiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/**
 * Tokisaki Kurumi con GeckoLib: geo/tokisaki_kurumi.geo.json (el proyecto de Blockbench
 * {@code blockbench/tokisaki_kurumi}, con el mosquete separado en el hueso {@code musket} de la mano
 * derecha y la boca del cañón en {@code musket_muzzle} para anclar efectos) +
 * textures/entity/tokisaki_kurumi.png + animations/tokisaki_kurumi.animation.json.
 *
 * <p>Hasta la 7.6.4 era una malla de jugador escrita en Java ({@code createBodyLayer}); el geo es el
 * mismo cubo a cubo. Los mechones van dentro de {@code head} (siguen a la cabeza).
 */
public class TokisakiKurumiModel extends GeoModel<TokisakiKurumiEntity> {
    @Override
    public ResourceLocation getModelResource(TokisakiKurumiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/tokisaki_kurumi.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TokisakiKurumiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/tokisaki_kurumi.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TokisakiKurumiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/tokisaki_kurumi.animation.json");
    }

    @Override
    public void setCustomAnimations(TokisakiKurumiEntity animatable, long instanceId,
                                    AnimationState<TokisakiKurumiEntity> animationState) {
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }
}
