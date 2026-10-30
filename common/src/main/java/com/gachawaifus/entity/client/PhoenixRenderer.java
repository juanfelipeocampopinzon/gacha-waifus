package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.PhoenixEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Phoenix con modelo slim + pieza del pecho ({@code TUBE_BIG}, sección 3.0F). Skin 64×128
 * con el desplegado en la mitad inferior.
 */
public class PhoenixRenderer extends LivingEntityRenderer<PhoenixEntity, WaifuBustPlayerModel<PhoenixEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/phoenix.png");

    public PhoenixRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(PhoenixEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(PhoenixEntity entity) {
        return true;
    }
}
