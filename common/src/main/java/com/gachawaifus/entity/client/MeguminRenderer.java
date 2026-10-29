package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.MeguminEntity;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Megumin (KonoSuba) con el modelo de jugador slim (Alex, 64×64). */
public class MeguminRenderer extends LivingEntityRenderer<MeguminEntity, WaifuPlayerModel<MeguminEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/megumin.png");

    public MeguminRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
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
