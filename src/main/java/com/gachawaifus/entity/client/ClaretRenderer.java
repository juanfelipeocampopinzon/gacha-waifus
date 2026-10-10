package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.ClaretEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Claret Flint con el modelo de jugador slim (Alex, 64×64), sin pieza del pecho por ahora.
 */
public class ClaretRenderer extends LivingEntityRenderer<ClaretEntity, PlayerModel<ClaretEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/claret.png");

    public ClaretRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ClaretEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(ClaretEntity entity) {
        return true;
    }
}
