package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.ClaretEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Claret Flint con el modelo de jugador SLIM y la pieza del pecho <b>grande</b> (sección 3×3, capa
 * {@link WaifuBustLayers#TUBE_BIG}), como Rina, Astra, Zhu Yuan o Yi Xuan. El {@code 3.0F} del
 * constructor es el lado del rombo y lo usa la física para convertir cuadros en escala: con el
 * 2.0F de la pieza normal, el tamaño y el bamboleo bailarían.
 *
 * <p>Con la pieza su skin pasó de 64×64 a 64×128: arriba el arte original y abajo, en (0, 121),
 * el desplegado que pinta {@code research/bust/tube_v5_sizes.py}.
 */
public class ClaretRenderer extends LivingEntityRenderer<ClaretEntity, WaifuBustPlayerModel<ClaretEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/claret.png");

    public ClaretRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
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
