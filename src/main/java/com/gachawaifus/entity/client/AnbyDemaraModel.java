package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AnbyDemaraEntity;
import net.minecraft.resources.ResourceLocation;
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
}
