package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.Soldier11Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Soldier 11 con el modelo de jugador ANCHO (brazos de 4px, como su {@code PlayerModel} vanilla)
 * más la pieza del pecho normal: se hornea la capa {@code WaifuBustLayers.TUBE_WIDE}, que es la
 * malla de Steve con el tubo. Skin 64×128 con el desplegado en la mitad inferior.
 */
public class Soldier11Renderer extends LivingEntityRenderer<Soldier11Entity, WaifuBustPlayerModel<Soldier11Entity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/soldier_11.png");

    public Soldier11Renderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_WIDE), false, 2.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Soldier11Entity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(Soldier11Entity entity) {
        return true;
    }
}
