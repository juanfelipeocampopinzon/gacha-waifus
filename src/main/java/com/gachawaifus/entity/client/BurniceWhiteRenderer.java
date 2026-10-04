package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.BurniceWhiteEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class BurniceWhiteRenderer extends LivingEntityRenderer<BurniceWhiteEntity, PlayerModel<BurniceWhiteEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/burnice_white.png");

    public BurniceWhiteRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BurniceWhiteEntity entity) {
        return TEXTURE;
    }
}