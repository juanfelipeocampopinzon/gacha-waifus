package com.gachawaifus.bust;

/**
 * Estado por entidad de la fisica de la pieza del pecho.
 *
 * <p>No se mueven vertices (una caja tiene los suyos fijos): lo que se mueve es el HUESO, y
 * solo en los grados de libertad que dejan la pieza pegada al pecho. Como el pivote esta
 * justo en la cara delantera del torso (z = -2) y las puntas de arriba y abajo del rombo
 * caen a z = 0 respecto a ese pivote, al escalar:
 *
 * <ul>
 *   <li><b>hacia delante ({@link #tip})</b>: la punta del pecho sale y entra, y las puntas
 *       de arriba y abajo se quedan EXACTAMENTE sobre la cara del pecho, sin despegarse.</li>
 *   <li><b>en vertical ({@link #swell})</b>: la pieza se hincha o se aplasta, y las puntas
 *       resbalan por la cara del pecho pero siguen apoyadas en ella.</li>
 *   <li><b>balanceo ({@link #swayX}, {@link #swayZ})</b>: un giro muy leve, de pocos grados,
 *       para que acompanne al cuerpo sin que las puntas se separen.</li>
 * </ul>
 *
 * <p>Todo en unidades de escala (1.0 = como esta modelado) salvo los balanceos, en grados.
 */
public final class BustState {

    /** Escala hacia delante: >1 saca la punta, <1 la mete. */
    public float tip = 1.0F;
    /** Escala vertical: >1 hincha, <1 aplasta. */
    public float swell = 1.0F;
    /** Balanceo adelante/atras, en grados. */
    public float swayX;
    /** Balanceo lateral, en grados. */
    public float swayZ;

    /** Valores del tick anterior, para interpolar en el render. */
    public float prevTip = 1.0F;
    public float prevSwell = 1.0F;
    public float prevSwayX;
    public float prevSwayZ;

    /** Velocidades de los resortes. */
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
