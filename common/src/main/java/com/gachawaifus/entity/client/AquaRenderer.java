package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AquaEntity;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Aqua (KonoSuba) con el modelo de jugador slim (Alex) y la pieza del pecho normal. */
public class AquaRenderer extends LivingEntityRenderer<AquaEntity, WaifuBustPlayerModel<AquaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/aqua.png");

    public AquaRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true, 2.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AquaEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(AquaEntity entity) {
        return true;
    }
}
