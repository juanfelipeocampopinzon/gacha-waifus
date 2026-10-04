package com.gachawaifus.entity.client;

import com.gachawaifus.entity.YeShunguangEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class YeShunguangRenderer extends GeoEntityRenderer<YeShunguangEntity> {
    public YeShunguangRenderer(EntityRendererProvider.Context context) {
        super(context, new YeShunguangModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(YeShunguangEntity entity) {
        return true;
    }
}
