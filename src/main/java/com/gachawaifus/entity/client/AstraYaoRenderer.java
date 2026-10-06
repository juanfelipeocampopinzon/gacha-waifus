package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.entity.AstraYaoEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class AstraYaoRenderer extends LivingEntityRenderer<AstraYaoEntity, PlayerModel<AstraYaoEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/astra_yao.png");

    public AstraYaoRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con el tubo del pecho horneado en su propia capa.
        super(context, new PlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AstraYaoEntity entity) {
        return TEXTURE;
    }
}
