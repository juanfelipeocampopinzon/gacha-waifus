package com.gachawaifus.entity.client;

import com.gachawaifus.entity.YidhariEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Yidhari con GeckoLib (geo/yidhari.geo.json). */
public class YidhariRenderer extends GeoEntityRenderer<YidhariEntity> {
    public YidhariRenderer(EntityRendererProvider.Context context) {
        super(context, new YidhariModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(YidhariEntity entity) {
        return true;
    }
}
