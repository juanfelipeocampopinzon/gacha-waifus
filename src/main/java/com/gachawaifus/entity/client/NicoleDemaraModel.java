package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.NicoleDemaraEntity;
import net.minecraft.resources.ResourceLocation;
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
}
