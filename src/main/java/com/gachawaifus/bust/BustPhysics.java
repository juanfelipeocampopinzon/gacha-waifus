package com.gachawaifus.bust;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;

/**
 * Fisica de la pieza del pecho: resortes sobre la ESCALA del hueso (no sobre su posicion),
 * que es lo que permite que las puntas de arriba y abajo del rombo no se despeguen del pecho.
 *
 * <p>Se integra una vez por tick en el cliente y el render interpola con el partial tick.
 * Los mandos estan todos aqui arriba.
 *
 * <p>En la v3.3.1 esto no funciono porque quien aplicaba los valores hacia
 * {@code setRotX(sway)}, borrando el giro base de 45 grados. Ahora la rotacion se SUMA al giro
 * base (ver {@code BustBones.BASE_TILT}) y los recorridos son mas amplios.
 */
public final class BustPhysics {

    /** Interruptor general: en false la pieza no se mueve nada. */
    public static final boolean ENABLED = true;

    /** Un tick, en segundos. */
    private static final double DT = 0.05D;

    // --- cuanto se puede mover cada canal -------------------------------------------
    private static final float TIP_RANGE = 0.55F;      // la punta sale/entra (escala Z)
    private static final float SWELL_RANGE = 0.30F;    // se hincha/aplasta (escala Y)
    private static final float SWAY_DEGREES = 8.0F;    // balanceo maximo

    // --- multiplicadores de cada mando (mas grande = mas exagerado) -----------------
    private static final double TIP_FROM_FALL = 0.030D;      // velocidad vertical
    private static final double TIP_FROM_LANDING = 0.055D;   // frenazo al aterrizar
    private static final double TIP_FROM_STEP = 0.150D;      // vaiven al andar
    private static final double SWELL_FROM_FALL = 0.018D;
    private static final double SWELL_FROM_LANDING = 0.065D;
    private static final double SWELL_FROM_STEP = 0.070D;
    private static final double SWAY_FROM_WALK = 3.2D;
    private static final double SWAY_FROM_ACCEL = 1.2D;
    private static final double SWAY_FROM_TURN = 0.16D;

    private static final double STIFFNESS = 55.0D;
    private static final double DAMPING = 8.0D;

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

        // Desplazamiento real de este tick en el sistema local de la waifu. No se usa
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

        // Velocidad (bloques/segundo) y aceleracion de este tick.
        double velForward = Mth.clamp(forward / DT, -8.0D, 8.0D);
        double velSide = Mth.clamp(side / DT, -8.0D, 8.0D);
        double velUp = Mth.clamp(up / DT, -14.0D, 14.0D);
        double accelForward = Mth.clamp((forward - s.lastForward) / DT, -8.0D, 8.0D);
        double accelUp = Mth.clamp((up - s.lastUp) / DT, -14.0D, 14.0D);
        float yawDelta = Mth.clamp(Mth.wrapDegrees(waifu.getYRot() - s.lastYaw), -40.0F, 40.0F);

        s.lastForward = forward;
        s.lastSide = side;
        s.lastUp = up;
        s.lastYaw = waifu.getYRot();

        float walkSpeed = Mth.clamp(waifu.walkAnimation.speed(), 0.0F, 1.0F);
        float walkPos = waifu.walkAnimation.position();
        boolean walking = walkSpeed > 0.03F;
        // El cuerpo sube y baja dos veces por ciclo y se mece una vez.
        double bob = walking ? Math.sin(walkPos * 2.0D) * walkSpeed : 0.0D;
        double lean = walking ? Math.sin(walkPos) * walkSpeed : 0.0D;
        double breath = Mth.sin((waifu.tickCount + 1) * 0.09F);

        // Objetivos (1.0 = forma modelada, 0 = sin balanceo)
        double targetTip = 1.0D
                - velUp * TIP_FROM_FALL            // al caer la punta sale (flota hacia arriba)
                - accelUp * TIP_FROM_LANDING       // al frenar en seco se mete
                + bob * TIP_FROM_STEP              // vaiven del paso
                + breath * 0.020D;                 // respiracion
        double targetSwell = 1.0D
                - Math.abs(velUp) * SWELL_FROM_FALL
                - accelUp * SWELL_FROM_LANDING     // al caer se aplasta y se ensancha
                + bob * SWELL_FROM_STEP
                + breath * 0.012D;
        double targetSwayX = -velForward * 0.55D - accelForward * SWAY_FROM_ACCEL
                + bob * SWAY_FROM_WALK + lean * 1.4D;
        double targetSwayZ = velSide * 0.9D + lean * SWAY_FROM_WALK - yawDelta * SWAY_FROM_TURN;

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

        s.tip = Mth.clamp(s.tip, 1.0F - TIP_RANGE * 1.4F, 1.0F + TIP_RANGE * 1.4F);
        s.swell = Mth.clamp(s.swell, 1.0F - SWELL_RANGE * 1.4F, 1.0F + SWELL_RANGE * 1.4F);
        s.swayX = Mth.clamp(s.swayX, -SWAY_DEGREES * 1.5F, SWAY_DEGREES * 1.5F);
        s.swayZ = Mth.clamp(s.swayZ, -SWAY_DEGREES * 1.5F, SWAY_DEGREES * 1.5F);
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
