package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.WilsonEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Wilson</b>: modelo de jugador ancho a tamaño normal, con la skin real de
 * {@code bosses/doctor house/wilson/} convertida a HD y la cara del actor en la cabeza.
 *
 * <p>Es un minion, no un jefe: su nombre (§8) solo se ve a la distancia normal del nametag.
 */
public class WilsonRenderer extends LivingEntityRenderer<WilsonEntity, PlayerModel<WilsonEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/wilson.png");

    public WilsonRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(WilsonEntity entity) {
        return TEXTURE;
    }
}
