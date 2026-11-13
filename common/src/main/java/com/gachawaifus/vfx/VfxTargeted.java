package com.gachawaifus.vfx;

import net.minecraft.world.phys.Vec3;

/**
 * Entidad que sabe dónde cae su habilidad (el campo de Nicole se abre sobre el objetivo, no sobre
 * ella). Las capas con {@code "anchor": "target"} usan este punto; si devuelve null, caen en el
 * origen del efecto.
 *
 * <p>Interfaz plana sin clases de cliente: la implementa la entidad (que también existe en el
 * servidor) leyendo un dato sincronizado.
 */
public interface VfxTargeted {

    /** Punto del mundo (pies) del objetivo de la habilidad en curso, o null. */
    Vec3 vfxTarget();
}
