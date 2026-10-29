package com.gachawaifus.bust;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;

/**
 * Fisica de la pieza del pecho: resortes sobre la ESCALA del hueso, con los canales medidos en
 * <b>cuadros de modelo</b>, para que las puntas de arriba y abajo del rombo no se despeguen del
 * pecho mientras solo se mueve la punta.
 *
 * <p>Se integra una vez por tick en el cliente y el render interpola con el partial tick.
 * Los mandos estan todos aqui:
 *
 * <ul>
 *   <li>{@link #TIP_OUT_UNITS} / {@link #TIP_IN_UNITS}: intervalo de la punta, en cuadros.</li>
 *   <li>{@link #FALL_TO_UNITS} y compañía: cuanto mueve cada tipo de inercia.</li>
 *   <li>{@link #STIFFNESS} y {@link #DAMPING}: rigidez y freno. Mas bajos = bamboleo mas largo
 *       (mas inercia); mas altos = vuelve antes a su sitio.</li>
 *   <li>{@link #ENABLED} en false deja la pieza completamente quieta.</li>
 * </ul>
 */
public final class BustPhysics {

    /** Interruptor general: en false la pieza no se mueve nada. */
    public static final boolean ENABLED = true;

    /** Un tick, en segundos. */
    private static final double DT = 0.05D;

    // --- intervalo de movimiento, en CUADROS (1 cuadro = 1 pixel de skin) ---------------
    /** Cuanto puede salir la punta hacia delante. */
    private static final float TIP_OUT_UNITS = 1.00F;
    /** Cuanto puede meterse hacia dentro. */
    private static final float TIP_IN_UNITS = 0.35F;
    /** Cuanto se hincha (+) o se aplasta (-) en vertical. */
    private static final float SWELL_UNITS = 0.30F;
    /** Balanceo maximo, en grados. */
    private static final float SWAY_DEGREES = 8.0F;

    // --- cuanto mueve cada inercia (los desplazamientos van en bloques por tick) --------
    /** Caida: la punta flota hacia fuera. 1 bloque/tick de caida = 1 cuadro. */
    private static final double FALL_TO_UNITS = 1.00D;
    /** Frenazo del aterrizaje (aceleracion vertical): la punta se mete y la pieza se aplasta. */
    private static final double LAND_TO_UNITS = 0.055D;
    /** Avance: la punta se queda atras. */
    private static final double WALK_LAG_UNITS = 1.60D;
    /** Vaiven del paso (sube y baja dos veces por ciclo). */
    private static final double STEP_UNITS = 0.30D;
    /** Respiracion, muy suave. */
    private static final double BREATH_UNITS = 0.060D;

    // --- resortes: blandos, para que la inercia se NOTE --------------------------------
    private static final double STIFFNESS = 30.0D;
    private static final double DAMPING = 4.2D;

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
            s.tip = 0.0F;
            s.swell = 0.0F;
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

        // Velocidad (bloques por tick) y aceleracion vertical (bloques por tick y segundo).
        double velForward = Mth.clamp(forward, -1.5D, 1.5D);
        double velSide = Mth.clamp(side, -1.5D, 1.5D);
        double velUp = Mth.clamp(up, -4.0D, 4.0D);
        double accelForward = Mth.clamp((forward - s.lastForward) / DT, -8.0D, 8.0D);
        double accelUp = Mth.clamp((up - s.lastUp) / DT, -20.0D, 20.0D);
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

        // Objetivos, en CUADROS.
        double targetTip = -velUp * FALL_TO_UNITS          // al caer, la punta flota hacia fuera
                - accelUp * LAND_TO_UNITS                  // al frenar en seco, se mete
                - velForward * WALK_LAG_UNITS              // al avanzar, se queda atras
                + bob * STEP_UNITS                         // vaiven del paso
                + breath * BREATH_UNITS;                   // respiracion
        double targetSwell = -Math.abs(velUp) * 0.35D      // al caer se estira en vertical
                - accelUp * LAND_TO_UNITS * 0.9D           // y al aterrizar se aplasta
                + bob * STEP_UNITS * 0.45D
                + breath * BREATH_UNITS * 0.4D;
        double targetSwayX = -velForward * 6.0D - accelForward * 1.2D + bob * 4.0D + lean * 1.5D;
        double targetSwayZ = velSide * 4.0D + lean * 3.5D - yawDelta * 0.16D;

        // Topes: la punta puede salir TIP_OUT_UNITS cuadros y meterse TIP_IN_UNITS.
        targetTip = Mth.clamp(targetTip, -(double) TIP_IN_UNITS, (double) TIP_OUT_UNITS);
        targetSwell = Mth.clamp(targetSwell, -(double) SWELL_UNITS, (double) SWELL_UNITS);
        targetSwayX = Mth.clamp(targetSwayX, -SWAY_DEGREES, SWAY_DEGREES);
        targetSwayZ = Mth.clamp(targetSwayZ, -SWAY_DEGREES, SWAY_DEGREES);

        if (waifu.isOrderedToSit()) {
            targetTip = 0.0D;
            targetSwell = 0.0D;
            targetSwayX = 0.0D;
            targetSwayZ = 0.0D;
        }

        Spring tip = spring(s.tip, s.velTip, targetTip, STIFFNESS, DAMPING);
        s.tip = tip.value();
        s.velTip = tip.velocity();

        Spring swell = spring(s.swell, s.velSwell, targetSwell, STIFFNESS * 0.9D, DAMPING * 1.05D);
        s.swell = swell.value();
        s.velSwell = swell.velocity();

        Spring swayX = spring(s.swayX, s.velSwayX, targetSwayX, STIFFNESS * 0.6D, DAMPING * 1.1D);
        s.swayX = swayX.value();
        s.velSwayX = swayX.velocity();

        Spring swayZ = spring(s.swayZ, s.velSwayZ, targetSwayZ, STIFFNESS * 0.6D, DAMPING * 1.1D);
        s.swayZ = swayZ.value();
        s.velSwayZ = swayZ.velocity();

        // El resorte rebota un poco por la inercia: se deja pasar un 10 % y no mas.
        s.tip = Mth.clamp(s.tip, -TIP_IN_UNITS * 1.4F, TIP_OUT_UNITS * 1.10F);
        s.swell = Mth.clamp(s.swell, -SWELL_UNITS * 1.4F, SWELL_UNITS * 1.4F);
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

    /** Muestra ya interpolada, EN CUADROS de modelo, lista para el render. */
    public record Sample(float tipUnits, float swellUnits, float swayX, float swayZ) {
    }

    /** Valores suavizados con el partial tick. */
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
