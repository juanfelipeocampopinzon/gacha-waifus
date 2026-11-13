package com.gachawaifus.entity.client;

import com.gachawaifus.entity.MiyabiMisiramaEntity;
import com.gachawaifus.vfx.VfxClones;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

public class MiyabiMisiramaRenderer extends GeoEntityRenderer<MiyabiMisiramaEntity> {
    public MiyabiMisiramaRenderer(EntityRendererProvider.Context context) {
        super(context, new MiyabiMisiramaModel());
        this.shadowRadius = 0.5F;
    }

    /** Estela del dash (capa {@code clone} de sus VFX): translúcida, teñida y sin nombre. */
    @Override
    public Color getRenderColor(MiyabiMisiramaEntity animatable, float partialTick, int packedLight) {
        if (VfxClones.active()) {
            int argb = VfxClones.argb();
            return Color.ofARGB((argb >>> 24) & 255, (argb >> 16) & 255, (argb >> 8) & 255, argb & 255);
        }
        return super.getRenderColor(animatable, partialTick, packedLight);
    }

    @Override
    public RenderType getRenderType(MiyabiMisiramaEntity animatable, ResourceLocation texture,
                                    @Nullable MultiBufferSource bufferSource, float partialTick) {
        if (VfxClones.active()) {
            return RenderType.entityTranslucent(texture);
        }
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }

    @Override
    public boolean shouldShowName(MiyabiMisiramaEntity animatable) {
        return !VfxClones.active();
    }
}