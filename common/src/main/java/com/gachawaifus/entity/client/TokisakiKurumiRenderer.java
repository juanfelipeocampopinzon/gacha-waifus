package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.TokisakiKurumiEntity;
import com.gachawaifus.vfx.VfxClones;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

/**
 * Kurumi con GeckoLib. Además sabe dibujarse como clon de sombra: mientras {@link VfxClones#active()}
 * se pinta translúcida con el color del efecto y sin nombre (capa {@code clone} de sus VFX).
 */
public class TokisakiKurumiRenderer extends GeoEntityRenderer<TokisakiKurumiEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/tokisaki_kurumi.png");

    public TokisakiKurumiRenderer(EntityRendererProvider.Context context) {
        super(context, new TokisakiKurumiModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public Color getRenderColor(TokisakiKurumiEntity animatable, float partialTick, int packedLight) {
        if (VfxClones.active()) {
            int argb = VfxClones.argb();
            return Color.ofARGB((argb >>> 24) & 255, (argb >> 16) & 255, (argb >> 8) & 255, argb & 255);
        }
        return super.getRenderColor(animatable, partialTick, packedLight);
    }

    @Override
    public RenderType getRenderType(TokisakiKurumiEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (VfxClones.active()) {
            return RenderType.entityTranslucent(texture);
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(TokisakiKurumiEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(TokisakiKurumiEntity entity) {
        return !VfxClones.active();
    }
}
