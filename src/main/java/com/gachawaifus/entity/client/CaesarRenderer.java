package com.gachawaifus.entity.client;

import com.gachawaifus.entity.CaesarEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class CaesarRenderer extends GeoEntityRenderer<CaesarEntity> {
    public CaesarRenderer(EntityRendererProvider.Context context) {
        super(context, new CaesarModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(CaesarEntity animatable) {
        return true;
    }
}
