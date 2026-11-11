package com.gachawaifus.entity.client;

import com.gachawaifus.entity.KasaneTetoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Kasane Teto con GeckoLib (geo/kasane_teto.geo.json). */
public class KasaneTetoRenderer extends GeoEntityRenderer<KasaneTetoEntity> {
    public KasaneTetoRenderer(EntityRendererProvider.Context context) {
        super(context, new KasaneTetoModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(KasaneTetoEntity entity) {
        return true;
    }
}
