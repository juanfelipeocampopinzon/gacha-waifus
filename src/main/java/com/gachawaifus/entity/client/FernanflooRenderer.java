package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.FernanflooEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Fernanfloo</b>: modelo de jugador ancho escalado x2 — mismo tamaño que
 * Ricardo Milos (hitbox 1.6x4.2 en {@code ModEntities} + {@code scale()} 2.0F aquí) — con la
 * skin de {@code bosses/fernanfloo/} convertida a HD y la cara real en la cabeza.
 *
 * <p>Se añade {@link ItemInHandLayer} a mano (los renderers custom del mod no tienen capas
 * por defecto) para que el chorizo GeckoLib de su mano derecha se dibuje.
 */
public class FernanflooRenderer extends BossHumanoidRenderer<FernanflooEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/fernanfloo.png");

    public FernanflooRenderer(EntityRendererProvider.Context context) {
        super(context, 1.6F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    /** Escala visual x2 para que el cuerpo llene la hitbox gigante (mismo tamaño que Ricardo). */
    @Override
    protected void scale(FernanflooEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(FernanflooEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(FernanflooEntity entity) {
        return true;
    }
}
