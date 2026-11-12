package com.gachawaifus.client.vfx;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.vfx.VfxCommand;
import com.gachawaifus.vfx.VfxManager;
import com.gachawaifus.vfx.VfxRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

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
        VfxRenderer.render(event.getModelViewMatrix(), event.getCamera(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(VfxCommand.build());
    }
}
