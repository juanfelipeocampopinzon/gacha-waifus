package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.RicardoMilosEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Ricardo Milos</b> con el modelo de jugador (brazos anchos: es un jefe, no una
 * waifu slim) sobre una skin 64x64 de momento <b>placeholder</b> generada con
 * {@code research/mobs/make_mob_skins.py} — el arte definitivo se decide más adelante, y solo hay
 * que sustituir {@code textures/entity/ricardo_milos.png}.
 */
public class RicardoMilosRenderer
        extends LivingEntityRenderer<RicardoMilosEntity, PlayerModel<RicardoMilosEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ricardo_milos.png");

    public RicardoMilosRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(RicardoMilosEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(RicardoMilosEntity entity) {
        return true;
    }
}
