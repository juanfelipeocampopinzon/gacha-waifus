package com.gachawaifus.entity.client;

import com.gachawaifus.entity.BurniceWhiteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BurniceWhiteRenderer extends GeoEntityRenderer<BurniceWhiteEntity> {
    public BurniceWhiteRenderer(EntityRendererProvider.Context context) {
        super(context, new BurniceWhiteModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(BurniceWhiteEntity animatable) {
        return true;
    }
}
