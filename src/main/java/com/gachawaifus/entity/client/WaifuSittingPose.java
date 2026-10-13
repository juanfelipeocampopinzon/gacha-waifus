package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AbstractWaifuEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

/**
 * Bajo de la waifu hasta el suelo cuando está sentada.
 *
 * <p>Las piernas y el torso los dobla {@code WaifuPlayerModel} (57 de las 63 waifus); aquí solo queda
 * desplazar el modelo, que un {@code PlayerModel} no tiene un hueso raíz que mover. Las 6 de GeckoLib
 * (Anby, Ellen, Miyabi, Burnice, Nicole, Ye Shunguang) animan por JSON y no pasan por ese modelo: a
 * esas se les sigue fingiendo la pose encogiendo la silueta.
 *
 * <p>El evento se dispara desde {@code EntityRenderDispatcher#render} dentro del push/pop del
 * renderizador, así que el {@code PoseStack} está alineado con el mundo: las unidades son bloques y
 * la Y crece hacia arriba.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, value = Dist.CLIENT)
public final class WaifuSittingPose {

    /** Cuánto baja la waifu para que la cadera le quede a la altura del suelo (0,75 es la cadera). */
    private static final float FLOOR_SINK = 0.62F;

    /** Respiración suave: amplitud en bloques y velocidad por tick. */
    private static final float BREATH_UNITS = 0.012F;
    private static final float BREATH_SPEED = 0.055F;

    private WaifuSittingPose() {
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        // isInSittingPose() es el flag sincronizado de TamableAnimal: lo pone el goal de sentarse,
        // así que el cliente lo ve sin haber que sincronizar el modo a mano.
        if (!(event.getEntity() instanceof AbstractWaifuEntity waifu) || !waifu.isInSittingPose()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        float age = waifu.tickCount + event.getPartialTick();
        float breath = Mth.sin(age * BREATH_SPEED) * BREATH_UNITS;
        poseStack.translate(0.0D, (double) -FLOOR_SINK + (double) breath, 0.0D);
    }
}
