package com.gachawaifus.entity.client;

import com.gachawaifus.entity.JuFufuEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class JuFufuRenderer extends GeoEntityRenderer<JuFufuEntity> {
    public JuFufuRenderer(EntityRendererProvider.Context context) {
        super(context, new JuFufuModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(JuFufuEntity animatable) {
        return true;
    }
}
