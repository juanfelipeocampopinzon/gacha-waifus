package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AquaEntity;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/** Aqua (KonoSuba) con el modelo de jugador slim (Alex, 64×64): waifu femenina sin pieza del pecho. */
public class AquaRenderer extends LivingEntityRenderer<AquaEntity, WaifuPlayerModel<AquaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/aqua.png");

    public AquaRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(AquaEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(AquaEntity entity) {
        return true;
    }
}
