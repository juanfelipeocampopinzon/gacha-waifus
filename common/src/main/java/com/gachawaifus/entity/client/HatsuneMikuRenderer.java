package com.gachawaifus.entity.client;

import com.gachawaifus.entity.HatsuneMikuEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Hatsune Miku con GeckoLib (geo/hatsune_miku.geo.json). */
public class HatsuneMikuRenderer extends GeoEntityRenderer<HatsuneMikuEntity> {
    public HatsuneMikuRenderer(EntityRendererProvider.Context context) {
        super(context, new HatsuneMikuModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(HatsuneMikuEntity entity) {
        return true;
    }
}
