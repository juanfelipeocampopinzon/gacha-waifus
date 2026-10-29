package com.gachawaifus.entity;

import net.minecraft.world.entity.LivingEntity;

/**
 * Cadencia del caminar de las waifus GeckoLib, clon exacto de la de vanilla.
 *
 * <p>Vanilla no avanza el ciclo de zancada en tiempo real: {@code walkAnimation} acumula
 * {@code position += speed} con {@code speed = min(distanciaRecorridaEsteTick * 4, 1)}
 * ({@code LivingEntity#updateWalkAnimation}) y {@code HumanoidModel#setupAnim} mete ese
 * {@code position} en un coseno. O sea: caminando despacio la zancada sale lenta y corta;
 * corriendo, rápida y completa.
 *
 * <p>En GeckoLib el {@code position} de vanilla es el tiempo de la animación, así que se imita
 * con las dos piezas ya repartidas: la amplitud vive en los keyframes de
 * {@code animations/<id>.walk} como {@code math.clamp(query.ground_speed * 4, 0, 1) * <grados>}
 * y la cadencia aquí, devolviendo ese mismo {@code speed} al {@code AnimationController}
 * ({@code adjustTick = animationSpeedModifier * ticksTranscurridos}). Con
 * {@code animation_length = 0.48}s (9.6 ticks de animación, frente a los 9.4248 = 2π/0.6662 de
 * vanilla) el ciclo queda {@code 9.6 / speed} ticks, igual que el modelo de jugador.
 *
 * <p>Parado se devuelve 1.0 en vez del 0 de {@code speed}: el mismo controlador es el que pone
 * {@code idle}, y con velocidad 0 esa animación se congelaría sin respirar.
 */
public final class WaifuGeoWalk {

    /** Suelo de cadencia mientras se mueve: evita que la animación se pegue al arrancar. */
    private static final double MIN_CADENCE = 0.25D;

    private WaifuGeoWalk() {
    }

    /** Velocidad de reproducción para el controlador {@code movement}. */
    public static double cadence(LivingEntity waifu) {
        float stride = waifu.walkAnimation.speed();
        return stride > 1.0E-5F ? Math.max(stride, MIN_CADENCE) : 1.0D;
    }
}
