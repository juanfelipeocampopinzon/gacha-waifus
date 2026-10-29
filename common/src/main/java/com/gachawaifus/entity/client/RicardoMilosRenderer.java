package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.RicardoMilosEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Ricardo Milos</b>: modelo de jugador ancho (es un jefe, no una waifu slim)
 * escalado x2 — hitbox 1.6x4.2 en {@code ModEntities} + {@code scale()} 2.0F aquí.
 *
 * <p>Textura HD 512x512 con la retícula 64x64 multiplicada x8: la cara es la foto del original
 * (bandana roja, guardada en {@code bosses/ricardo milos/}) y el resto del cuerpo sigue siendo
 * el placeholder de {@code research/mobs/make_mob_skins.py}.
 */
public class RicardoMilosRenderer extends BossHumanoidRenderer<RicardoMilosEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ricardo_milos.png");

    public RicardoMilosRenderer(EntityRendererProvider.Context context) {
        super(context, 1.6F);
    }

    /** Escala visual x2 para que el cuerpo llene la hitbox gigante (jefe al doble de tamano). */
    @Override
    protected void scale(RicardoMilosEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
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
