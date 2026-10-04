package com.gachawaifus.entity.client;

import com.gachawaifus.entity.NicoleDemaraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NicoleDemaraRenderer extends GeoEntityRenderer<NicoleDemaraEntity> {
    public NicoleDemaraRenderer(EntityRendererProvider.Context context) {
        super(context, new NicoleDemaraModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(NicoleDemaraEntity animatable) {
        return true;
    }
}