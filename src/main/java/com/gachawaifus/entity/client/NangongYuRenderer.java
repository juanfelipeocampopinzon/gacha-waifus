package com.gachawaifus.entity.client;

import com.gachawaifus.entity.NangongYuEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Nangong Yu con GeckoLib (geo/nangong_yu.geo.json). */
public class NangongYuRenderer extends GeoEntityRenderer<NangongYuEntity> {
    public NangongYuRenderer(EntityRendererProvider.Context context) {
        super(context, new NangongYuModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(NangongYuEntity entity) {
        return true;
    }
}
