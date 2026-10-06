package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.entity.PromeiaEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class PromeiaRenderer extends LivingEntityRenderer<PromeiaEntity, PlayerModel<PromeiaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/promeia.png");

    public PromeiaRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con el tubo del pecho horneado en su propia capa.
        super(context, new PlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PromeiaEntity entity) {
        return TEXTURE;
    }
}
