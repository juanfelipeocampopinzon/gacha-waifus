package com.gachawaifus.client.vfx;

import com.gachawaifus.vfx.VfxCommand;
import com.gachawaifus.vfx.VfxManager;
import com.gachawaifus.vfx.VfxRenderer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Gancho de Fabric del prototipo de VFX. {@link #init()} se llama desde
 * {@code GachaWaifusClient.onInitializeClient()}.
 *
 * <p>La matriz de vista se construye aquí a mano (la misma que usa vanilla en 1.21.1:
 * {@code new Matrix4f().rotation(camera.rotation().conjugate())}) para no depender de que el
 * PoseStack del contexto venga ya con la cámara aplicada.
 */
public final class VfxLoaderHooks {

    private VfxLoaderHooks() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> VfxManager.tick());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            Camera camera = context.camera();
            Matrix4f modelView = new Matrix4f().rotation(camera.rotation().conjugate(new Quaternionf()));
            VfxRenderer.render(modelView, camera, context.tickCounter().getGameTimeDeltaPartialTick(false));
        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(VfxCommand.build()));
    }
}
