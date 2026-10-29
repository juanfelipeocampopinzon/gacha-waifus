package com.gachawaifus.entity.client;

import com.gachawaifus.entity.RinaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Rina con GeckoLib (geo/rina.geo.json): reemplaza al modelo de jugador con pieza del pecho. */
public class RinaRenderer extends GeoEntityRenderer<RinaEntity> {
    public RinaRenderer(EntityRendererProvider.Context context) {
        super(context, new RinaModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(RinaEntity entity) {
        return true;
    }
}
