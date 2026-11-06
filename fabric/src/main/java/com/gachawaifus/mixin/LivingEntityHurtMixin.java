package com.gachawaifus.mixin;

import com.gachawaifus.color.ColorEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * El puente de Fabric hacia {@link ColorEvents#onIncomingDamage}.
 *
 * <p>NeoForge parchea {@code LivingEntity#hurt} para lanzar {@code LivingIncomingDamageEvent};
 * Fabric API no tiene ningún evento de daño modificable, así que aquí se engancha al mismo punto
 * con un mixin: se reescribe el argumento {@code float amount} en HEAD de
 * {@code hurt(DamageSource, float)} ANTES de que vanilla haga cualquier comprobación. Es
 * exactamente el momento en que NeoForge lanza su evento, así que ambos loaders escalan el mismo
 * número por la misma ruta (los proyectiles ya llegan con su dueño en {@code getEntity()}).
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHurtMixin {

    @ModifyVariable(
            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private float gachawaifus$scaleIncomingDamage(float amount, DamageSource source) {
        return ColorEvents.onIncomingDamage((LivingEntity) (Object) this, source.getEntity(), amount);
    }
}
