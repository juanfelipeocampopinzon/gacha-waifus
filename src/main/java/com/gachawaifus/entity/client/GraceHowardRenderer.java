package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.GraceHowardEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Grace Howard con modelo slim + pieza del pecho GRANDE ({@code TUBE_BIG}, sección 3.0F,
 * talla grande como Rina). Skin 64×128 con el desplegado en la mitad inferior.
 */
public class GraceHowardRenderer extends LivingEntityRenderer<GraceHowardEntity, WaifuBustPlayerModel<GraceHowardEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/grace_howard.png");

    public GraceHowardRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GraceHowardEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(GraceHowardEntity entity) {
        return true;
    }
}
