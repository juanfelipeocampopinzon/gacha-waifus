package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.TungTungSahurEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Tung Tung Sahur</b>: modelo de jugador ancho escalado x2 (hitbox 1.6x4.2, como
 * Fernanfloo) con la textura de {@code bosses/tung tung sahur/} adaptada a skin de jugador, y el
 * bate de béisbol GeckoLib en la mano gracias al {@link ItemInHandLayer}.
 */
public class TungTungSahurRenderer extends BossHumanoidRenderer<TungTungSahurEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/tung_tung_sahur.png");

    public TungTungSahurRenderer(EntityRendererProvider.Context context) {
        super(context, 1.6F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    /** Escala visual x2 para que el tronco llene la hitbox gigante. */
    @Override
    protected void scale(TungTungSahurEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(TungTungSahurEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(TungTungSahurEntity entity) {
        return true;
    }
}
