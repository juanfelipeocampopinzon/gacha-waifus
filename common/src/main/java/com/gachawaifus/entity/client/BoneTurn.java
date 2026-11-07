package com.gachawaifus.entity.client;

import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Giro de un hueso hacia el objetivo sin que se acumule fotograma a fotograma y sin chasquidos.
 *
 * <p>GeckoLib solo reescribe un hueso si el clip que suena en ese fotograma lo keya; en los clips
 * que no lo keyan, la rotación queda donde la dejó el fotograma anterior. Por eso un
 * {@code setRotY(getRotY() + giro)} sumaba otra vez sobre su propio resultado cada frame y la
 * cabeza acababa dando molinetes. Antes de volver a aplicar el giro se quita el que ya estaba
 * embebido.
 *
 * <p>El giro pedido no se aplica de golpe: se persigue con aproximación exponencial, porque si no
 * la cabeza salta a la posición nueva justo cuando empieza o termina una habilidad (el objetivo pasa
 * de 0 a 35° en un frame).
 */
public final class BoneTurn {
    /** Fracción del resto que se recorre cada fotograma (~12 frames para llegar al 95 %). */
    private static final float EASE_PER_FRAME = 0.22F;
    /** Por debajo de esto, con el objetivo ya en 0, se da el giro por terminado. */
    private static final float SETTLED = 1.0E-4F;

    private float applied;
    private float written = Float.NaN;
    private float eased;

    /** Olvida el giro: hay que llamarla al cambiar de entidad, no cada frame. */
    public void reset() {
        this.applied = 0.0F;
        this.written = Float.NaN;
        this.eased = 0.0F;
    }

    /**
     * Suma el giro (suavizado) a la rotación en Y del hueso. Si {@code bone} es {@code null}
     * —modelo sin ese hueso— no hace nada.
     */
    public void apply(@Nullable GeoBone bone, float objective) {
        if (bone == null) {
            return;
        }
        this.eased += (objective - this.eased) * EASE_PER_FRAME;
        if (objective == 0.0F && Math.abs(this.eased) < SETTLED) {
            this.eased = 0.0F;
        }
        float raw = bone.getRotY();
        // Crudo igual a lo que escribimos nosotros => ningún clip tocó el hueso: la pose del clip
        // es raw menos el giro que embebimos el fotograma anterior. Si no coincide, un clip acaba
        // de reescribirlo y esa pose cruda es la que hay que respetar como base.
        float pose = raw == this.written ? raw - this.applied : raw;
        float result = pose + this.eased;
        bone.setRotY(result);
        this.applied = this.eased;
        this.written = result;
    }
}
