package com.gachawaifus.client.vfx;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.vfx.VfxCommand;
import com.gachawaifus.vfx.VfxManager;
import com.gachawaifus.vfx.VfxRenderer;
import com.gachawaifus.vfx.VfxScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Gancho de NeoForge del prototipo de VFX: avanza la simulación cada tick, dibuja tras las
 * partículas del mundo y registra {@code /gwvfx} en el dispatcher de cliente.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, value = Dist.CLIENT)
public final class VfxLoaderHooks {

    private VfxLoaderHooks() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        VfxManager.tick();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        VfxRenderer.render(event.getModelViewMatrix(), event.getProjectionMatrix(), event.getCamera(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    /** Capa {@code flash}: velo de color encima de todo el HUD. */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        VfxScreen.renderFlash(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    /** Capa {@code shake}: se suma a los ángulos que la cámara ya iba a usar. */
    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float[] shake = VfxScreen.shakeAngles((float) event.getPartialTick());
        if (shake[0] != 0.0F || shake[1] != 0.0F) {
            event.setYaw(event.getYaw() + shake[0]);
            event.setPitch(event.getPitch() + shake[1]);
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(VfxCommand.build());
    }
}
