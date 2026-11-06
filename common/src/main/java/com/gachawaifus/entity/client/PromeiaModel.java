package com.gachawaifus.entity.client;

import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.PromeiaEntity;
import com.gachawaifus.skin.WaifuSkinState;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Modelo de jugador de Promeia: la pieza del pecho normal más la cola de caballo.
 *
 * <p>La cola viene horneada en la capa {@code WaifuBustLayers.PROMEIA} como hija de la cabeza, así
 * que sigue sus giros sin código extra. Lo que sí se toca aquí es la pose de los brazos: sin
 * transformar (skin base) se quedan rígidos y pegados al cuerpo aunque camine; al entrar en
 * combate vuelve la animación normal de vanilla para caminar y atacar.
 */
public class PromeiaModel extends WaifuBustPlayerModel<PromeiaEntity> {

    public PromeiaModel(ModelPart root) {
        super(root, true, 2.0F);
    }

    @Override
    public void setupAnim(PromeiaEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Sentada ya tiene su propia pose (brazos en el regazo): no se pisa.
        if (entity.skinState() != WaifuSkinState.BASE || entity.isInSittingPose()) {
            return;
        }

        stiffen(this.leftArm);
        stiffen(this.rightArm);

        // Vanilla copió las mangas antes de este cambio; hay que volver a copiarlas.
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
    }

    /** Brazos rectos hacia abajo, sin balanceo de caminar ni vaivén de reposo. */
    private static void stiffen(ModelPart arm) {
        arm.xRot = 0.0F;
        arm.yRot = 0.0F;
        arm.zRot = 0.0F;
    }
}
