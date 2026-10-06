package com.gachawaifus.client.bust;

import com.gachawaifus.bust.BustPhysics;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * Modelo de jugador slim con la pieza del pecho para las waifus sin GeckoLib
 * (Astra, Yuzuha, Promeia y Remielle).
 *
 * <p>La fisica se aplica igual que en los modelos geo: escalando el hueso de la pieza. Su
 * pivote esta puesto en el propio hueso (cara delantera del torso, z = -2), asi que al escalar
 * hacia delante y en vertical las puntas de arriba y abajo del rombo siguen apoyadas en el
 * pecho. La posicion del hueso NO se toca: la trae la capa ya horneada, con el pivote que le
 * toca a cada tamano (normal o grande).
 *
 * <p><b>IMPORTANTE:</b> la capa hornea la pieza con un giro de 45 grados
 * ({@code PartPose.offsetAndRotation(..., 45 grados, 0, 0)}). El balanceo se SUMA a ese angulo;
 * asignarlo solo borraria el giro (fallo de la v3.3.1).
 *
 * <p>En el modelo de jugador la Y crece hacia los pies, pero la escala es simetrica respecto al
 * pivote, asi que el signo da igual.
 */
public class WaifuBustPlayerModel<T extends LivingEntity> extends PlayerModel<T> {

    private final ModelPart bustRoot;

    public WaifuBustPlayerModel(ModelPart root, boolean slim) {
        super(root, slim);
        this.bustRoot = root.getChild("body").getChild("breasts");
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

        this.bustRoot.xRot = BustBones.BASE_TILT + sample.swayX() * Mth.DEG_TO_RAD;
        this.bustRoot.zRot = sample.swayZ() * Mth.DEG_TO_RAD;
        this.bustRoot.xScale = 1.0F;
        this.bustRoot.yScale = sample.swell();
        this.bustRoot.zScale = sample.tip();
    }
}
