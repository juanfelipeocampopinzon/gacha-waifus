package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.Soldier11Entity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Soldier 11 con el modelo de jugador SLIM (brazos de 3px) y la pieza del pecho normal: se hornea
 * la capa {@code WaifuBustLayers.TUBE}. Estaba en la malla ancha de Steve, que la hacía leer como
 * un hombre; la skin se refluyó a 3px con {@code research/bust/wide_to_slim_arms.py}. Skin
 * 64×128 con el desplegado en (0,121).
 */
public class Soldier11Renderer extends LivingEntityRenderer<Soldier11Entity, WaifuBustPlayerModel<Soldier11Entity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/soldier_11.png");

    public Soldier11Renderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE), true, 2.0F), 0.5F);
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
