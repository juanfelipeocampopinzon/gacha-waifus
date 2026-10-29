package com.gachawaifus.entity.client;

import com.gachawaifus.entity.RoxyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Roxy Ifrita Pryce con GeckoLib (geo/roxy.geo.json): reemplaza al modelo de jugador con pieza del pecho. */
public class RoxyRenderer extends GeoEntityRenderer<RoxyEntity> {
    public RoxyRenderer(EntityRendererProvider.Context context) {
        super(context, new RoxyModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(RoxyEntity entity) {
        return true;
    }
}
