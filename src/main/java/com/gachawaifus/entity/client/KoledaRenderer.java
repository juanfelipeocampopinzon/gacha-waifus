package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.KoledaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Koleda Belobog</b> con el <b>modelo de jugador slim + pieza del pecho</b>
 * ({@code WaifuBustPlayerModel} con la capa {@code WaifuBustLayers.TUBE}, sección 2.0F), el mismo
 * patrón que Promeia. Su skin es 64×128: la mitad inferior lleva el desplegado del rombo
 * ({@code manual-pieza-pecho.md} §7).
 *
 * <p><b>No usa GeckoLib</b>: Koleda no tiene {@code .geo.json} ni animaciones.
 */
public class KoledaRenderer extends LivingEntityRenderer<KoledaEntity, WaifuBustPlayerModel<KoledaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/koleda.png");

    public KoledaRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true, 2.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KoledaEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(KoledaEntity entity) {
        return true;
    }
}
