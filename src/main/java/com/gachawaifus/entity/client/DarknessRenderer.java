package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.DarknessEntity;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Darkness (KonoSuba) con el modelo de jugador slim (Alex, 64×64). */
public class DarknessRenderer extends LivingEntityRenderer<DarknessEntity, WaifuPlayerModel<DarknessEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/darkness.png");

    public DarknessRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(DarknessEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(DarknessEntity entity) {
        return true;
    }
}
