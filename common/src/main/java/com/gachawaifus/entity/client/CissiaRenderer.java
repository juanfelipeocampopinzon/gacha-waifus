package com.gachawaifus.entity.client;

import com.gachawaifus.entity.CissiaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CissiaRenderer extends GeoEntityRenderer<CissiaEntity> {
    public CissiaRenderer(EntityRendererProvider.Context context) {
        super(context, new CissiaModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(CissiaEntity animatable) {
        return true;
    }
}
