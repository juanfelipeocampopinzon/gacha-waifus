package com.gachawaifus.entity.client;

import com.gachawaifus.entity.NormaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Norma Hollowell con GeckoLib (geo/norma.geo.json). */
public class NormaRenderer extends GeoEntityRenderer<NormaEntity> {
    public NormaRenderer(EntityRendererProvider.Context context) {
        super(context, new NormaModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(NormaEntity entity) {
        return true;
    }
}
