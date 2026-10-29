package com.gachawaifus.entity.client;

import com.gachawaifus.entity.QingyiEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Qingyi con GeckoLib (geo/qingyi.geo.json): reemplaza al modelo de jugador con pieza del pecho. */
public class QingyiRenderer extends GeoEntityRenderer<QingyiEntity> {
    public QingyiRenderer(EntityRendererProvider.Context context) {
        super(context, new QingyiModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public boolean shouldShowName(QingyiEntity entity) {
        return true;
    }
}
