package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.YeShunguangEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class YeShunguangModel extends GeoModel<YeShunguangEntity> {
    @Override
    public ResourceLocation getModelResource(YeShunguangEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/ye_shunguang.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(YeShunguangEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ye_shunguang.png");
    }

    @Override
    public ResourceLocation getAnimationResource(YeShunguangEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/ye_shunguang.animation.json");
    }
}
