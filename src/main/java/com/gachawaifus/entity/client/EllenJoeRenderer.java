package com.gachawaifus.entity.client;

import com.gachawaifus.entity.EllenJoeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EllenJoeRenderer extends GeoEntityRenderer<EllenJoeEntity> {
    public EllenJoeRenderer(EntityRendererProvider.Context context) {
        super(context, new EllenJoeModel());
        this.shadowRadius = 0.5F;
    }
}
