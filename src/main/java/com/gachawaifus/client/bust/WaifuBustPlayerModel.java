package com.gachawaifus.client.bust;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Modelo de jugador slim con la pieza del pecho para las waifus sin GeckoLib
 * (Astra, Yuzuha, Promeia, Remielle y Kurumi).
 *
 * <p>La fisica se aplica igual que en los modelos geo: la peticion viene en CUADROS y aqui se
 * convierte a escala con la mitad de la diagonal del rombo (el lado se recibe en el constructor,
 * porque `ModelPart.cubes` es privado en 1.21.1). Su pivote esta puesto en el hueso (cara delantera del torso, z = -2), asi que al escalar
 * hacia delante y en vertical las puntas de arriba y abajo siguen apoyadas en el pecho. La
 * posicion del hueso NO se toca: la trae la capa ya horneada, con el pivote de cada tamano.
 *
 * <p><b>IMPORTANTE:</b> la capa hornea la pieza con un giro de 45 grados. El balanceo se SUMA a
 * ese angulo; asignarlo solo borraria el giro (fallo de la v3.3.1).
 *
 * <p>En el modelo de jugador la Y crece hacia los pies, pero la escala es simetrica respecto al
 * pivote, asi que el signo da igual.
 */
public class WaifuBustPlayerModel<T extends LivingEntity> extends PlayerModel<T> {

    private static final float HALF_DIAGONAL_FACTOR = 0.70710678F;

    private final ModelPart bustRoot;
    /** Lado del rombo: 2.0 en la pieza normal y 3.0 en la grande. */
    private final float section;

    public WaifuBustPlayerModel(ModelPart root, boolean slim, float section) {
        super(root, slim);
        this.bustRoot = root.getChild("body").getChild("breasts");
        this.section = section;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (!(entity instanceof AbstractWaifuEntity waifu)) {
            return;
        }
        float partialTick = ageInTicks - entity.tickCount;
        BustPhysics.Sample sample = BustPhysics.sample(waifu, partialTick);

        float halfDiagonal = this.section * HALF_DIAGONAL_FACTOR;

        this.bustRoot.xRot = BustBones.BASE_TILT + sample.swayX() * Mth.DEG_TO_RAD;
        this.bustRoot.zRot = sample.swayZ() * Mth.DEG_TO_RAD;
        this.bustRoot.xScale = 1.0F;
        this.bustRoot.yScale = 1.0F + sample.swellUnits() / halfDiagonal;
        this.bustRoot.zScale = 1.0F + sample.tipUnits() / halfDiagonal;
    }
}
