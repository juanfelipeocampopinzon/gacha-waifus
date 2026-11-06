package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.YeShunguangEntity;
import com.gachawaifus.skin.WaifuSkinState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Brillo de la transformación de Ye Shunguang.
 *
 * <p>Dos cosas que a propósito NO se hacen como en otros sitios:
 * <ul>
 * <li>Nada de {@code AutoGlowingGeoLayer} de GeckoLib: esa capa *borra* del texture base los píxeles
 * del glowmask y los repinta con un RenderType propio ({@code geo_glowing_layer}) que los shader
 * packs no reconocen, así que con shaders el pelo quedaba como agujero transparente.</li>
 * <li>Nada de shaders custom ni GL_ALWAYS: el overlay se pinta con el RenderType EMISIVO vanilla
 * ({@code entityTranslucentEmissive}), el que Bliss/Complementary/Rethinking Voxels tratan como
 * emisivo real (bloom y luz propia con packs que lo soporten) y con profundidad normal LESS, así que
 * la luz no atraviesa bloques ni el propio cuerpo.</li>
 * </ul>
 *
 * <p>El modelo se re-pinta con el glowmask blanco ligeramente inflado (1.25 %): al ser coplanar con
 * la melena, sin inflar quedaría descartado por el test de profundidad o entraría en Z-fighting.
 * La base se queda intacta: si un pack se saltara el overlay, lo peor que pasa es que el pelo no
 * brille, nunca que desaparezca.
 */
public class YeShunguangGlowLayer extends GeoRenderLayer<YeShunguangEntity> {

    private static final ResourceLocation GLOW_MASK =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/ye_shunguang_ultimate_glowmask.png");

    private static final RenderType GLOW_RENDER_TYPE = RenderType.entityTranslucentEmissive(GLOW_MASK);

    private static final float INFLATE = 1.0125F;

    public YeShunguangGlowLayer(GeoRenderer<YeShunguangEntity> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, YeShunguangEntity entity, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                       float partialTick, int packedLight, int packedOverlay) {
        if (entity.skinState() != WaifuSkinState.ULTIMATE) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(INFLATE, INFLATE, INFLATE);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, entity, GLOW_RENDER_TYPE,
                bufferSource.getBuffer(GLOW_RENDER_TYPE), partialTick, 15728640, packedOverlay, 0xFFFFFFFF);
        poseStack.popPose();
    }
}
