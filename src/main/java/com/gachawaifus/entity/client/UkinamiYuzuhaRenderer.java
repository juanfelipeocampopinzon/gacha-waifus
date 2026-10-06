package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.UkinamiYuzuhaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class UkinamiYuzuhaRenderer extends LivingEntityRenderer<UkinamiYuzuhaEntity, WaifuBustPlayerModel<UkinamiYuzuhaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ukinami_yuzuha.png");

    public UkinamiYuzuhaRenderer(EntityRendererProvider.Context context) {
        // Modelo de jugador SLIM con el tubo del pecho horneado en su propia capa.
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(UkinamiYuzuhaEntity entity) {
        return TEXTURE;
    }
}

