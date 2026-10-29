package com.gachawaifus.entity.client;

import com.gachawaifus.entity.KoledaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Koleda Belobog con GeckoLib (geo/koleda.geo.json). */
public class KoledaRenderer extends GeoEntityRenderer<KoledaEntity> {
    public KoledaRenderer(EntityRendererProvider.Context context) {
        super(context, new KoledaModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(KoledaEntity entity) {
        return true;
    }
}
