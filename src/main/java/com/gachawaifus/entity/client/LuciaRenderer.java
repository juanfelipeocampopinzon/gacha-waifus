package com.gachawaifus.entity.client;

import com.gachawaifus.entity.LuciaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Lucia Elowen con GeckoLib (geo/lucia.geo.json). */
public class LuciaRenderer extends GeoEntityRenderer<LuciaEntity> {
    public LuciaRenderer(EntityRendererProvider.Context context) {
        super(context, new LuciaModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(LuciaEntity entity) {
        return true;
    }
}
