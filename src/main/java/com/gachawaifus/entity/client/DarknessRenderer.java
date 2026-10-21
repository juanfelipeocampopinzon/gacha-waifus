package com.gachawaifus.entity.client;

import com.gachawaifus.entity.DarknessEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DarknessRenderer extends GeoEntityRenderer<DarknessEntity> {
    public DarknessRenderer(EntityRendererProvider.Context context) {
        super(context, new DarknessModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(DarknessEntity animatable) {
        return true;
    }
}
