package com.gachawaifus.mixin;

import com.gachawaifus.vfx.VfxScreen;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * El temblor de cámara de la capa {@code shake} en Fabric.
 *
 * <p>NeoForge lanza {@code ViewportEvent.ComputeCameraAngles} dentro de {@code Camera#setup} y el
 * gancho de {@code VfxLoaderHooks} suma ahí el desvío. Fabric API no tiene ese evento, así que se
 * suma al final de {@code setup}: con un desvío de pocos grados la diferencia (la cámara en
 * tercera persona ya se ha alejado con el ángulo sin temblor) no se nota.
 */
@Mixin(Camera.class)
public abstract class CameraShakeMixin {

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract float getXRot();

    @Inject(method = "setup", at = @At("TAIL"))
    private void gachawaifus$shake(BlockGetter level, Entity entity, boolean detached, boolean mirrored,
                                   float partialTick, CallbackInfo info) {
        float[] shake = VfxScreen.shakeAngles(partialTick);
        if (shake[0] != 0.0F || shake[1] != 0.0F) {
            this.setRotation(this.getYRot() + shake[0], this.getXRot() + shake[1]);
        }
    }
}
