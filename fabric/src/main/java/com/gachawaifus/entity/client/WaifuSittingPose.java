package com.gachawaifus.entity.client;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;

public final class WaifuSittingPose {

    private static final float FLOOR_SINK = 0.62F;
    private static final float BREATH_UNITS = 0.012F;
    private static final float BREATH_SPEED = 0.055F;

    private WaifuSittingPose() {
    }

    public static void apply(AbstractWaifuEntity waifu, PoseStack poseStack, float partialTick) {
        if (!waifu.isInSittingPose()) {
            return;
        }
        float age = waifu.tickCount + partialTick;
        float breath = Mth.sin(age * BREATH_SPEED) * BREATH_UNITS;
        poseStack.translate(0.0D, (double) -FLOOR_SINK + (double) breath, 0.0D);
    }
}
