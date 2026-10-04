package com.gachawaifus.entity.client;

import com.gachawaifus.entity.AnbyDemaraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AnbyDemaraRenderer extends GeoEntityRenderer<AnbyDemaraEntity> {
    public AnbyDemaraRenderer(EntityRendererProvider.Context context) {
        super(context, new AnbyDemaraModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(AnbyDemaraEntity animatable) {
        return true;
    }
}
