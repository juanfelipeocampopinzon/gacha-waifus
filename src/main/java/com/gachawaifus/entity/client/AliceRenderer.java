package com.gachawaifus.entity.client;

import com.gachawaifus.entity.AliceEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AliceRenderer extends GeoEntityRenderer<AliceEntity> {
    public AliceRenderer(EntityRendererProvider.Context context) {
        super(context, new AliceModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(AliceEntity animatable) {
        return true;
    }
}
