package com.gachawaifus.vfx;

/**
 * Entidad que lanza sus efectos por casteo y no por clip de GeckoLib (Megumin es un modelo de
 * jugador). El servidor sube {@link #vfxCastSeq()} una vez por habilidad y guarda en
 * {@link #vfxEffectId()} qué efecto toca; el cliente ve el cambio y arranca los stages del JSON.
 *
 * <p>Así las waifus sin GeckoLib tienen VFX completos sin paquetes nuevos ni tocar los loaders.
 */
public interface VfxCasting {

    /** Efecto a lanzar, con la misma forma que la ruta del asset: {@code "megumin/ultimate"}. */
    String vfxEffectId();

    /** Contador que sube en cada casteo (0 = nunca ha lanzado nada). */
    int vfxCastSeq();
}
