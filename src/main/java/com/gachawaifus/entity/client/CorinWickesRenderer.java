package com.gachawaifus.entity.client;

import com.gachawaifus.entity.CorinWickesEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CorinWickesRenderer extends GeoEntityRenderer<CorinWickesEntity> {
    public CorinWickesRenderer(EntityRendererProvider.Context context) {
        super(context, new CorinWickesModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(CorinWickesEntity animatable) {
        return true;
    }
}
