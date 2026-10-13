package com.gachawaifus.client.bust;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * El modelo de jugador de las waifus: el de vanilla más la pose de sentarse en el suelo.
 *
 * <p>Existe porque en NeoForge 1.21.1 no hay ningún hook entre {@code setupAnim} y el dibujado: el
 * único sitio donde se pueden doblar huesos sin tocar los 63 renderers es el modelo, y para eso tiene
 * que ser una clase propia. Por eso los renderers que antes usaban {@code PlayerModel} de vanilla
 * usan este, y {@link WaifuBustPlayerModel} (el de la pieza del pecho) hereda de aquí.
 *
 * <p>La orden la pone {@code AbstractWaifuEntity}: el flag sincronizado {@code isInSittingPose()} es
 * el que llega del servidor, así que no hay que inventar ninguna red de más. Lo que no hace el
 * modelo es bajar el cuerpo hasta el suelo — eso lo hace el {@code PoseStack} en
 * {@code WaifuSittingPose}, porque un {@code PlayerModel} no tiene un hueso raíz que desplazar.
 */
public class WaifuPlayerModel<T extends LivingEntity> extends PlayerModel<T> {

    /** Piernas estiradas hacia delante: -85°, casi horizontales sobre el suelo. */
    private static final float LEGS_FORWARD = -1.48F;

    /** Cuánto se separan las puntas de los pies (más abiertas a petición del usuario). */
    private static final float LEGS_APART_Y = 0.28F;

    /** Suave inclinación lateral de las piernas. */
    private static final float LEGS_TILT_Z = 0.06F;

    /** Brazos descansando sobre el regazo y los muslos: inclinados hacia abajo y adelante. */
    private static final float ARMS_DOWN = -0.75F;
    private static final float ARMS_INWARD_Y = 0.12F;
    private static final float ARMS_RELAX_Z = 0.10F;

    public WaifuPlayerModel(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (!(entity instanceof AbstractWaifuEntity waifu) || !waifu.isInSittingPose()) {
            return;
        }
        waifu.markModelSitPose();

        // Torso erguido natural: sin desarticular cadera, hombros ni cuello
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;

        // Piernas estiradas hacia adelante sobre el suelo
        this.leftLeg.xRot = LEGS_FORWARD;
        this.rightLeg.xRot = LEGS_FORWARD;
        this.leftLeg.yRot = LEGS_APART_Y;
        this.rightLeg.yRot = -LEGS_APART_Y;
        this.leftLeg.zRot = -LEGS_TILT_Z;
        this.rightLeg.zRot = LEGS_TILT_Z;

        // Brazos descansando suavemente en el regazo / muslos
        this.leftArm.xRot = ARMS_DOWN;
        this.rightArm.xRot = ARMS_DOWN;
        this.leftArm.yRot = -ARMS_INWARD_Y;
        this.rightArm.yRot = ARMS_INWARD_Y;
        this.leftArm.zRot = ARMS_RELAX_Z;
        this.rightArm.zRot = -ARMS_RELAX_Z;

        // PlayerModel ya copió las capas cuando estaba de pie: hay que volver a copiarlas.
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }
}
