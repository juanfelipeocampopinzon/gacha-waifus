package com.gachawaifus.entity.client;

import com.gachawaifus.entity.ClaretEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ClaretRenderer extends GeoEntityRenderer<ClaretEntity> {
    public ClaretRenderer(EntityRendererProvider.Context context) {
        super(context, new ClaretModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(ClaretEntity animatable) {
        return true;
    }
}
