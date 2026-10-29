package com.gachawaifus.entity.client;

import com.gachawaifus.entity.LucyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Luciana Lucy de Montefio con GeckoLib (geo/lucy.geo.json). */
public class LucyRenderer extends GeoEntityRenderer<LucyEntity> {
    public LucyRenderer(EntityRendererProvider.Context context) {
        super(context, new LucyModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(LucyEntity entity) {
        return true;
    }
}
