package com.gachawaifus.client.bust;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Aplica la fisica al hueso {@code breasts} de los modelos GeckoLib.
 *
 * <p>La fisica pide CUADROS de movimiento y aqui se convierten a escala. El pivote del hueso esta
 * en la cara del pecho y la punta del rombo a "medio lado" de distancia, asi que
 * {@code escalaZ = 1 + cuadros / medioLado} mueve la punta exactamente esos cuadros y deja las
 * puntas de arriba y abajo apoyadas en la cara del pecho.
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

    /** Mitad de la diagonal del rombo: (seccion/2) * raiz(2). */
    private static final float HALF_DIAGONAL_FACTOR = 0.70710678F;

    /** Por si el hueso no trae cubos legibles: seccion normal. */
    private static final float FALLBACK_SECTION = 2.0F;

    private BustBones() {
    }

    public static void drive(GeoBone bone, AbstractWaifuEntity waifu, float partialTick) {
        if (bone == null) {
            return;
        }
        BustPhysics.Sample sample = BustPhysics.sample(waifu, partialTick);

        // La seccion se lee del propio cubo del hueso (8 x s x s), asi sirve para la pieza
        // normal y para la grande sin tener que pasar numeros a mano por modelo.
        float section = FALLBACK_SECTION;
        if (!bone.getCubes().isEmpty()) {
            section = (float) bone.getCubes().get(0).size().y;
        }
        if (section <= 0.0F) {
            section = FALLBACK_SECTION;
        }
        float halfDiagonal = section * HALF_DIAGONAL_FACTOR;

        bone.setScaleX(1.0F);                       // el ancho del torso no se toca
        bone.setScaleY(1.0F + sample.swellUnits() / halfDiagonal);
        bone.setScaleZ(1.0F + sample.tipUnits() / halfDiagonal);
        // SUMA al giro base: nunca lo reemplaces.
        bone.setRotX(BASE_TILT + sample.swayX() * Mth.DEG_TO_RAD);
        bone.setRotZ(sample.swayZ() * Mth.DEG_TO_RAD);
    }
}
