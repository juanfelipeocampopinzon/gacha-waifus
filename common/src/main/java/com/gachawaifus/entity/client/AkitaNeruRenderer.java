package com.gachawaifus.entity.client;

import com.gachawaifus.entity.AkitaNeruEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Akita Neru con GeckoLib (geo/akita_neru.geo.json): melena lateral animada en el hueso {@code coleta}. */
public class AkitaNeruRenderer extends GeoEntityRenderer<AkitaNeruEntity> {
    public AkitaNeruRenderer(EntityRendererProvider.Context context) {
        super(context, new AkitaNeruModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(AkitaNeruEntity entity) {
        return true;
    }
}
