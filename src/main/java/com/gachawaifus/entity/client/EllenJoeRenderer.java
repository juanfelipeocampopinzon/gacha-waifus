package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.EllenJoeEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class EllenJoeRenderer extends LivingEntityRenderer<EllenJoeEntity, PlayerModel<EllenJoeEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ellen_joe.png");

    public EllenJoeRenderer(EntityRendererProvider.Context context) {
        // Usa el modelo de jugador SLIM (Alex) que coincide perfectamente con la skin de 64x64 de Ellen Joe
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EllenJoeEntity entity) {
        return TEXTURE;
    }
}
