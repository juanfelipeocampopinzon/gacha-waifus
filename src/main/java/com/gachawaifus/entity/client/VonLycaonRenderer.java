package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.VonLycaonEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Von Lycaon</b> con el modelo de jugador <b>ancho</b> ({@code ModelLayers.PLAYER}, el
 * de Steve): es un personaje masculino y corpulento, y el modelo slim le quedaría estrecho.
 *
 * <p><b>No lleva pieza del pecho</b>: por eso usa un {@code PlayerModel} normal y su skin es de
 * 64×64 (no hace falta la mitad de abajo, que es donde vive el desplegado del rombo). Si algún día
 * se le quisiera añadir, habría que pasar a {@code WaifuBustPlayerModel} y meterlo en la tabla
 * {@code SECTION} del pipeline del pecho.
 */
public class VonLycaonRenderer extends LivingEntityRenderer<VonLycaonEntity, PlayerModel<VonLycaonEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/von_lycaon.png");

    public VonLycaonRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(VonLycaonEntity entity) {
        return TEXTURE;
    }

    /**
     * Un poco más grande que una waifu (es el mayordomo lobo más alto del reparto). Solo afecta al
     * dibujo: la caja de colisión de la entidad no cambia, así que no rompe nada del combate.
     */
    @Override
    protected void scale(VonLycaonEntity entity, com.mojang.blaze3d.vertex.PoseStack poseStack, float partialTick) {
        poseStack.scale(1.15F, 1.15F, 1.15F);
    }

    @Override
    public boolean shouldShowName(VonLycaonEntity entity) {
        return true;
    }
}
