package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.KasaneTetoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Kasane Teto con modelo slim + pieza del pecho ({@code TUBE}, sección 2.0F). Skin 64×128
 * con el desplegado en la mitad inferior.
 */
public class KasaneTetoRenderer extends LivingEntityRenderer<KasaneTetoEntity, WaifuBustPlayerModel<KasaneTetoEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/kasane_teto.png");

    public KasaneTetoRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true, 2.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KasaneTetoEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(KasaneTetoEntity entity) {
        return true;
    }
}
