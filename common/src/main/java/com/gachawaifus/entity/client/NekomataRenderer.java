package com.gachawaifus.entity.client;

import com.gachawaifus.entity.NekomataEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Nekomiya Mana con GeckoLib (geo/nekomata.geo.json). */
public class NekomataRenderer extends GeoEntityRenderer<NekomataEntity> {
    public NekomataRenderer(EntityRendererProvider.Context context) {
        super(context, new NekomataModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(NekomataEntity entity) {
        return true;
    }
}
