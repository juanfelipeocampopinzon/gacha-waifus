package com.gachawaifus.entity.client;

import com.gachawaifus.entity.MiyabiMisiramaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MiyabiMisiramaRenderer extends GeoEntityRenderer<MiyabiMisiramaEntity> {
    public MiyabiMisiramaRenderer(EntityRendererProvider.Context context) {
        super(context, new MiyabiMisiramaModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(MiyabiMisiramaEntity animatable) {
        return true;
    }
}