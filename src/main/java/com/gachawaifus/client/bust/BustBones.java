package com.gachawaifus.client.bust;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Aplica la fisica al hueso {@code breasts} de los modelos GeckoLib.
 *
 * <p>Se toca la ESCALA y un balanceo minimo, nunca la posicion: como el pivote del hueso esta
 * en la cara del pecho, escalar hacia delante y en vertical deja las puntas de arriba y abajo
 * del rombo apoyadas en esa cara. GeckoLib guarda las rotaciones en radianes.
 *
 * <p><b>IMPORTANTE:</b> el hueso trae un giro base de 45 grados en el `.geo.json`
 * ({@code "rotation": [45, 0, 0]}). El balanceo se SUMA a ese angulo; asignarlo solo borraria
 * el giro y la pieza saldria recta y mal colocada (fallo de la v3.3.1).
 */
public final class BustBones {

    /** Nombre del hueso que llevan los seis modelos geo. */
    public static final String BONE_NAME = "breasts";

    /** Giro base de la pieza, en radianes: los 45 grados del desplegado. */
    public static final float BASE_TILT = 45.0F * Mth.DEG_TO_RAD;

    private BustBones() {
    }

    public static void drive(GeoBone bone, AbstractWaifuEntity waifu, float partialTick) {
        if (bone == null) {
            return;
        }
        BustPhysics.Sample sample = BustPhysics.sample(waifu, partialTick);
        bone.setScaleX(1.0F);                       // el ancho del torso no se toca
        bone.setScaleY(sample.swell());
        bone.setScaleZ(sample.tip());
        // SUMA al giro base: nunca lo reemplaces.
        bone.setRotX(BASE_TILT + sample.swayX() * Mth.DEG_TO_RAD);
        bone.setRotZ(sample.swayZ() * Mth.DEG_TO_RAD);
    }
}
