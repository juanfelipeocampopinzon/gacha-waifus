package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.TokisakiKurumiEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Modelo personalizado para Tokisaki Kurumi:
 * <ul>
 *   <li>Base: cuerpo de jugadora slim con la pieza del busto grande (3.0F).</li>
 *   <li>El pelo es el que se dibujo en Blockbench ({@code blockbench/tokisaki_kurumi}): cuatro
 *       mechones con giro, dos sobre la frente y dos laterales, cada uno como hueso propio de la
 *       cabeza porque en la malla de Java un cubo no puede girar suelto.</li>
 *   <li>Mosquete de chispa: los cinco cubos que el geo pinta en el brazo derecho (UV 36,69 / 42,65 /
 *       47,66). Van fundidos en el brazo, como en Blockbench, así que el giro de apuntar se los lleva.
 *       Antes existía un hueso {@code rifle} propio con un mosquete modelado a mano, pero sus redes
 *       (32,64 / 32,70 / 32,74) caían encima de la pintura de este: salía un cubo suelto flotando.</li>
 * </ul>
 */
public class TokisakiKurumiModel extends WaifuBustPlayerModel<TokisakiKurumiEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "tokisaki_kurumi"), "main");

    /** Giro base de los mechones, el mismo del .geo.json (rad). */
    private static final float SIDE_TILT_LEFT = -25.0F * Mth.DEG_TO_RAD;
    private static final float SIDE_TILT_RIGHT = 25.0F * Mth.DEG_TO_RAD;
    private static final float TOP_TILT_LEFT = 15.0F * Mth.DEG_TO_RAD;
    private static final float TOP_TILT_RIGHT = -15.0F * Mth.DEG_TO_RAD;

    /** Vaiven de reposo: lento y corto, para que el pelo nunca esté completamente quieto. */
    private static final float IDLE_AMP = 0.035F;
    private static final float IDLE_SPEED = 0.05F;
    /** Cuanto se atrasan los mechones con la zancada (xRot en rad por unidad de swing). */
    private static final float STRIDE_AMP = 0.22F;
    /** Cuanto se queda el pelo detras de la cabeza al girarla (fraction del giro del tick). */
    private static final float HEAD_TRAIL = 0.45F;
    /** En el aire (salto o caida) el pelo se levanta hacia atras. */
    private static final float AIR_LIFT = 0.16F;

    /** Brazo en cruz: el cañón queda horizontal apuntando al frente. */
    private static final float HALF_PI = 1.5707963F;

    private final ModelPart strandTopLeft;
    private final ModelPart strandTopRight;
    private final ModelPart strandSideLeft;
    private final ModelPart strandSideRight;

    public TokisakiKurumiModel(ModelPart root) {
        super(root, true, 3.0F);
        ModelPart head = root.getChild("head");
        this.strandTopLeft = head.getChild("strand_top_left");
        this.strandTopRight = head.getChild("strand_top_right");
        this.strandSideLeft = head.getChild("strand_side_left");
        this.strandSideRight = head.getChild("strand_side_right");
    }

    @Override
    public void setupAnim(TokisakiKurumiEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // primero el andar de vanilla y la fisica del pecho; aqui solo se mueve el pelo despues
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // mecedor de reposo + arrastre de la zancada + elevacion en el aire, todos en xRot
        // (+ = la punta hacia +Z, o sea hacia atras; - = hacia delante)
        float idle = Mth.sin(ageInTicks * IDLE_SPEED) * IDLE_AMP;
        float stride = Mth.sin(limbSwing * 0.5F) * STRIDE_AMP * limbSwingAmount;
        float lift = entity.onGround() ? 0.0F : AIR_LIFT;
        float back = idle + stride + lift;

        // la cabeza ya se lleva los mechones por delante (son hijos de head); con yRot en contra se
        // quedan un instante detras, que es lo que hace que el pelo "persiga" a la cabeza
        float trail = Mth.clamp(Mth.wrapDegrees(entity.yHeadRot - entity.yHeadRotO) * HEAD_TRAIL,
                -14.0F, 14.0F) * Mth.DEG_TO_RAD;
        // respiracion lateral: abre y cierra medio grado, espejada en los dos lados
        float open = idle * 0.6F;

        this.strandSideLeft.xRot = back;
        this.strandSideRight.xRot = back * 0.85F;
        this.strandSideLeft.yRot = -trail;
        this.strandSideRight.yRot = -trail;
        this.strandSideLeft.zRot = SIDE_TILT_LEFT - open;
        this.strandSideRight.zRot = SIDE_TILT_RIGHT + open;

        // los dos mechones de la frente son cortos: medio recorrido para que no bailen de mas
        this.strandTopLeft.xRot = back * 0.5F;
        this.strandTopRight.xRot = back * 0.5F;
        this.strandTopLeft.yRot = -trail * 0.5F;
        this.strandTopRight.yRot = -trail * 0.5F;
        this.strandTopLeft.zRot = TOP_TILT_LEFT;
        this.strandTopRight.zRot = TOP_TILT_RIGHT;

        // ---- el mosquete: levanta el brazo al apuntar y da el saltito del retroceso ----
        // Sentido comprobado en la matriz de ModelPart (y' = y·cos − z·sin, z' = y·sin + z·cos):
        // xRot NEGATIVO lleva la mano (+Y, colgando) hacia -Z, o sea hacia DELANTE. A -90 grados el
        // brazo queda horizontal y el cañón, que va en +Y fundido en el brazo, apunta al frente.
        // isMusketAiming() viene de un dato sincronizado; getTarget() SIEMPRE es null en el cliente.
        float partialTick = ageInTicks - entity.tickCount;
        float attackAnim = entity.getAttackAnim(partialTick);
        float raise = entity.isMusketAiming() ? 1.0F : Mth.clamp(attackAnim * 5.0F, 0.0F, 1.0F);
        if (raise > 0.0F) {
            // horizontal + la inclinación de la mirada (headPitch + es mirar abajo = cañón hacia abajo)
            float aimX = -HALF_PI + headPitch * Mth.DEG_TO_RAD;
            this.rightArm.xRot = Mth.lerp(raise, this.rightArm.xRot, aimX);
            this.rightArm.zRot = Mth.lerp(raise, this.rightArm.zRot, 0.18F);   // apartado del costado
        }
        // getAttackAnim va de 1 (justo al disparar) a 0 en ~6 ticks: el retroceso levanta el conjunto
        // brazo+arma, que es lo unico que se mueve (el arma va fundida en el brazo).
        this.rightArm.xRot -= attackAnim * 0.35F;
        // PlayerModel copio las capas dentro de super: hay que volver a copiar la manga.
        this.rightSleeve.copyFrom(this.rightArm);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // ---- lo editado en Blockbench (blockbench/tokisaki_kurumi), volcado a Java ----
        // Un cubo de la malla del jugador NO puede girar por si solo: los mechones con rotacion
        // cuelgan de la cabeza como huesos propios, que es como lo hace vanilla.
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)   // la cabeza de siempre
                        .texOffs(14, 106).addBox(8.0F, 0.0F, 2.0F, 1.0F, 4.0F, 3.0F)   // mechon recto, izquierda
                        .texOffs(7, 83).addBox(-7.0F, -3.0F, 2.0F, 1.0F, 2.0F, 3.0F),  // mechon recto, derecha
                PartPose.ZERO);

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F) // el brazo slim de siempre
                        .texOffs(36, 69).addBox(-1.0F, 10.0F, 0.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(36, 69).addBox(-1.0F, 12.0F, -2.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(42, 65).addBox(-1.0F, 12.0F, -2.0F, 1.0F, 6.0F, 1.0F)
                        .texOffs(36, 69).addBox(-1.0F, 11.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(47, 66).addBox(-1.0F, 13.0F, -1.0F, 1.0F, 1.0F, 1.0F),
                PartPose.offset(-5.0F, 2.5F, 0.0F));

        PartDefinition head = root.getChild("head");

        // El giro se hereda TAL CUAL del .geo.json: con X e Z iguales en los dos espacios y la Y
        // reflejada, el signo de Z no cambia (probarlo con el mechon lateral, que sin ese giro
        // quedaria a 6 px de la cabeza y con él cae sobre la oreja).
        head.addOrReplaceChild("strand_top_right",
                CubeListBuilder.create().texOffs(2, 84).addBox(-2.0F, -4.0F, -1.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(-3.0F, -5.0F, 3.0F, 0.0F, 0.0F, TOP_TILT_RIGHT));
        head.addOrReplaceChild("strand_top_left",
                CubeListBuilder.create().texOffs(2, 84).addBox(-0.96F, -0.14F, -1.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(4.96F, -8.36F, 3.0F, 0.0F, 0.0F, TOP_TILT_LEFT));
        // Los dos mechones largos llegan con el pivote en la PUNTA (en Blockbench se giraron desde
        // abajo). Para animarlos hace falta que cuelguen de su raiz, asi que el pivote se mueve al
        // extremo superior y el cubo se desplaza lo mismo: P' = P + R*c, cubo' = cubo - c. La malla
        // resultante es identica punto a punto, solo cambia donde gira.
        head.addOrReplaceChild("strand_side_left",
                CubeListBuilder.create().texOffs(9, 99).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 13.0F, 3.0F),
                PartPose.offsetAndRotation(5.51F, -7.78F, 3.5F, 0.0F, 0.0F, SIDE_TILT_LEFT));
        head.addOrReplaceChild("strand_side_right",
                CubeListBuilder.create().texOffs(9, 99).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 8.0F, 3.0F),
                PartPose.offsetAndRotation(-4.62F, -8.25F, 3.5F, 0.0F, 0.0F, SIDE_TILT_RIGHT));
        // ---- fin del vuelco desde Blockbench ----

        // 1. Pieza del busto grande (TUBE_BIG 3.0F)
        CubeListBuilder tube = CubeListBuilder.create()
                .texOffs(0, 121)
                .addBox(-4.0F, -1.5F, -1.5F, 8.0F, 3.0F, 3.0F);
        body.addOrReplaceChild("breasts", tube, PartPose.offsetAndRotation(
                0.0F, 3.79F, -2.0F, 45.0F * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }
}
