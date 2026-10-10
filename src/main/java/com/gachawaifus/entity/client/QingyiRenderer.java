package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.QingyiEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Qingyi con el modelo de jugador slim (Alex, 64×64), el patrón de waifu femenina
 * sin pieza del pecho. Tea Master del Equipo de Investigación: bastón eléctrico
 * y un look de oficinista tranquila.
 */
public class QingyiRenderer extends LivingEntityRenderer<QingyiEntity, PlayerModel<QingyiEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/qingyi.png");

    public QingyiRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(QingyiEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(QingyiEntity entity) {
        return true;
    }
}