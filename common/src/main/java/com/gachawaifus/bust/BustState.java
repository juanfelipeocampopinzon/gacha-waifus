package com.gachawaifus.bust;

/**
 * Estado por entidad de la fisica de la pieza del pecho.
 *
 * <p>Todos los canales van en <b>cuadros de modelo</b> (1 cuadro = 1 unidad = 1 pixel de skin),
 * no en escalas: así "un cuadro" significa lo mismo en la pieza normal (2x2) y en la grande (3x3).
 *
 * <ul>
 *   <li>{@link #tip}: cuadros que sobresale la punta (>0 sale, &lt;0 se mete).</li>
 *   <li>{@link #swell}: cuadros que se hincha (+) o se aplasta (-) en vertical.</li>
 *   <li>{@link #swayX}, {@link #swayZ}: balanceo en grados.</li>
 * </ul>
 *
 * <p>El pivote está en la cara delantera del torso, así que escalar hacia delante y en vertical
 * deja las puntas de arriba y abajo del rombo apoyadas en esa cara: nunca se despegan.
 */
public final class BustState {

    /** Cuadros que sobresale la punta. */
    public float tip;
    /** Cuadros que se hincha en vertical. */
    public float swell;
    /** Balanceo adelante/atras, en grados. */
    public float swayX;
    /** Balanceo lateral, en grados. */
    public float swayZ;

    /** Valores del tick anterior, para interpolar en el render. */
    public float prevTip;
    public float prevSwell;
    public float prevSwayX;
    public float prevSwayZ;

    /** Velocidades de los resortes (cuadros/tick y grados/tick). */
    public float velTip;
    public float velSwell;
    public float velSwayX;
    public float velSwayZ;

    /** Desplazamiento local (delante/lado/arriba) del tick anterior. */
    public double lastForward;
    public double lastSide;
    public double lastUp;
    /** Yaw del tick anterior, para el latigazo al girar. */
    public float lastYaw;
    /** Falso hasta tener una muestra previa. */
    public boolean primed;
}
