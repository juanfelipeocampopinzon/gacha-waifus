package com.gachawaifus.bust;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;

/**
 * Fisica de la pieza del pecho: resortes sobre la ESCALA del hueso (no sobre su posicion),
 * que es lo que permite que las puntas de arriba y abajo del rombo no se despeguen del pecho.
 *
 * <p>Se integra una vez por tick en el cliente y el render interpola con el partial tick.
 * Los numeros son deliberadamente discretos: se nota el bamboleo, no una pieza de goma.
 *
 * <p>Mandos (aqui se ajusta todo):
 * <ul>
 *   <li>{@link #TIP_RANGE} cuanto puede salir/entrar la punta.</li>
 *   <li>{@link #SWELL_RANGE} cuanto puede hincharse/aplastarse en vertical.</li>
 *   <li>{@link #SWAY_DEGREES} el balanceo maximo, en grados.</li>
 *   <li>{@link #STIFFNESS} y {@link #DAMPING} la rigidez y el freno del resorte.</li>
 *   <li>Poner {@link #ENABLED} en false deja la pieza completamente quieta.</li>
 * </ul>
 */
public final class BustPhysics {

    /** Interruptor general: en false la pieza no se mueve nada. */
    public static final boolean ENABLED = true;

    /** Un tick, en segundos. */
    private static final double DT = 0.05D;

    private static final float TIP_RANGE = 0.42F;      // cuanto sobresale la punta
    private static final float SWELL_RANGE = 0.26F;    // cuanto se hincha en vertical
    private static final float SWAY_DEGREES = 5.0F;    // balanceo maximo

    private static final double STIFFNESS = 52.0D;
    private static final double DAMPING = 8.4D;

    private BustPhysics() {
    }

    /** Avanza la simulacion un tick. Solo debe llamarse en el cliente. */
    public static void tick(AbstractWaifuEntity waifu) {
        BustState s = waifu.bustState();

        s.prevTip = s.tip;
        s.prevSwell = s.swell;
        s.prevSwayX = s.swayX;
        s.prevSwayZ = s.swayZ;

        if (!ENABLED) {
            s.tip = 1.0F;
            s.swell = 1.0F;
            s.swayX = 0.0F;
            s.swayZ = 0.0F;
            return;
        }

        // Movimiento real de este tick en el sistema local de la waifu. No se usa
        // getDeltaMovement() porque en el cliente las waifus se mueven por interpolacion:
        // el desplazamiento por tick (lo mismo que alimenta la animacion de andar) si vale.
        double dx = waifu.getX() - waifu.xo;
        double dy = waifu.getY() - waifu.yo;
        double dz = waifu.getZ() - waifu.zo;
        float yaw = waifu.getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        double forward = -dx * sin + dz * cos;
        double side = dx * cos + dz * sin;
        double up = dy;

        if (!s.primed) {
            s.lastForward = forward;
            s.lastSide = side;
            s.lastUp = up;
            s.lastYaw = waifu.getYRot();
            s.primed = true;
            return;
        }

        double accelForward = Mth.clamp((forward - s.lastForward) / DT, -4.0D, 4.0D);
        double accelSide = Mth.clamp((side - s.lastSide) / DT, -4.0D, 4.0D);
        double accelUp = Mth.clamp((up - s.lastUp) / DT, -6.0D, 6.0D);
        float yawDelta = Mth.clamp(Mth.wrapDegrees(waifu.getYRot() - s.lastYaw), -30.0F, 30.0F);

        s.lastForward = forward;
        s.lastSide = side;
        s.lastUp = up;
        s.lastYaw = waifu.getYRot();

        float walkSpeed = Mth.clamp(waifu.walkAnimation.speed(), 0.0F, 1.0F);
        float walkPos = waifu.walkAnimation.position();
        boolean walking = walkSpeed > 0.03F;
        double step = walking ? Math.sin(walkPos * 2.0D * Math.PI) * walkSpeed : 0.0D;
        double sway = walking ? Math.sin(walkPos * Math.PI) * walkSpeed : 0.0D;

        // Objetivos (1.0 = forma modelada, 0 = sin balanceo)
        double targetTip = 1.0D
                - accelUp * 0.055D              // al caer la punta se mete, al saltar sale
                - accelForward * 0.030D         // se queda atras al arrancar
                + step * 0.045D                 // el paso la hace vibrar
                + Mth.sin((waifu.tickCount + 1) * 0.09F) * 0.012D;   // respiracion
        double targetSwell = 1.0D
                - accelUp * 0.060D              // al caer se aplasta y se ensancha
                + step * 0.030D
                + Mth.sin((waifu.tickCount + 1) * 0.09F) * 0.008D;
        double targetSwayX = -accelForward * 1.1D + step * 1.6D;
        double targetSwayZ = accelSide * 1.0D + sway * 2.2D - yawDelta * 0.10D;

        // Topes
        targetTip = Mth.clamp(targetTip, 1.0D - TIP_RANGE, 1.0D + TIP_RANGE);
        targetSwell = Mth.clamp(targetSwell, 1.0D - SWELL_RANGE, 1.0D + SWELL_RANGE);
        targetSwayX = Mth.clamp(targetSwayX, -SWAY_DEGREES, SWAY_DEGREES);
        targetSwayZ = Mth.clamp(targetSwayZ, -SWAY_DEGREES, SWAY_DEGREES);

        if (waifu.isOrderedToSit()) {
            targetTip = 1.0D;
            targetSwell = 1.0D;
            targetSwayX = 0.0D;
            targetSwayZ = 0.0D;
        }

        Spring tip = spring(s.tip, s.velTip, targetTip, STIFFNESS, DAMPING);
        s.tip = tip.value();
        s.velTip = tip.velocity();

        Spring swell = spring(s.swell, s.velSwell, targetSwell, STIFFNESS * 0.9D, DAMPING);
        s.swell = swell.value();
        s.velSwell = swell.velocity();

        Spring swayX = spring(s.swayX, s.velSwayX, targetSwayX, STIFFNESS * 0.7D, DAMPING);
        s.swayX = swayX.value();
        s.velSwayX = swayX.velocity();

        Spring swayZ = spring(s.swayZ, s.velSwayZ, targetSwayZ, STIFFNESS * 0.7D, DAMPING);
        s.swayZ = swayZ.value();
        s.velSwayZ = swayZ.velocity();

        s.tip = Mth.clamp(s.tip, 1.0F - TIP_RANGE * 1.5F, 1.0F + TIP_RANGE * 1.5F);
        s.swell = Mth.clamp(s.swell, 1.0F - SWELL_RANGE * 1.5F, 1.0F + SWELL_RANGE * 1.5F);
        s.swayX = Mth.clamp(s.swayX, -SWAY_DEGREES * 1.6F, SWAY_DEGREES * 1.6F);
        s.swayZ = Mth.clamp(s.swayZ, -SWAY_DEGREES * 1.6F, SWAY_DEGREES * 1.6F);
    }

    /** Resultado de integrar un resorte durante un tick. */
    private record Spring(float value, float velocity) {
    }

    /** Un paso de integracion semi-implicito. */
    private static Spring spring(float value, float velocity, double target,
                                 double stiffness, double damping) {
        double v = velocity + (stiffness * (target - value) - damping * velocity) * DT;
        return new Spring((float) (value + v * DT), (float) v);
    }

    /** Muestra ya interpolada para dibujar. */
    public record Sample(float tip, float swell, float swayX, float swayZ) {
    }

    /** Valores suavizados con el partial tick, listos para el render. */
    public static Sample sample(AbstractWaifuEntity waifu, float partialTick) {
        BustState s = waifu.bustState();
        float t = Mth.clamp(partialTick, 0.0F, 1.0F);
        return new Sample(
                Mth.lerp(t, s.prevTip, s.tip),
                Mth.lerp(t, s.prevSwell, s.swell),
                Mth.lerp(t, s.prevSwayX, s.swayX),
                Mth.lerp(t, s.prevSwayZ, s.swayZ));
    }
}
