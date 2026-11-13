package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.MeguminEntity;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Megumin (KonoSuba) con el modelo de jugador slim y lienzo 64×128 (sin pieza, con sitio para accesorios). */
public class MeguminRenderer extends LivingEntityRenderer<MeguminEntity, WaifuPlayerModel<MeguminEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/megumin.png");

    public MeguminRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(WaifuBustLayers.MEGUMIN), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(MeguminEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(MeguminEntity entity) {
        return true;
    }
}
