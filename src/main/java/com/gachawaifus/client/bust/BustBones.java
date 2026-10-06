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
 */
public final class BustBones {

    /** Nombre del hueso que llevan los seis modelos geo. */
    public static final String BONE_NAME = "breasts";

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
        bone.setRotX(sample.swayX() * Mth.DEG_TO_RAD);
        bone.setRotZ(sample.swayZ() * Mth.DEG_TO_RAD);
    }
}
