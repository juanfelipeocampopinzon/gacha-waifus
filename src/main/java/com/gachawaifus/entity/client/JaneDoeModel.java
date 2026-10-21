package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.JaneDoeEntity;
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
 * Modelo de Jane Doe: cuerpo de jugadora slim con la pieza del pecho grande y su cola.
 *
 * <p>La cola viene del .geo.json editado en Blockbench ({@code modelos/personajes/jane_doe}), donde
 * son dos huesos: {@code bone} (la base, que sale del bajoespaldo hacia atras y dobla a la derecha)
 * y {@code punta cola} (la borla del extremo). Aqui se llaman {@code tail_base} y {@code tail_tip},
 * cuelgan del torso y giran desde su raiz: el pivote se movio a la union con el cuerpo y cada cubo se
 * desplazo lo mismo (P' = P + R·c, cubo' = cubo − c), asi que la malla es identica punto a punto.
 *
 * <p>El andar sigue siendo el de vanilla; la animacion nueva se aplica despues de
 * {@code super.setupAnim} para no pisar las extremidades ni la fisica del pecho.
 */
public class JaneDoeModel extends WaifuBustPlayerModel<JaneDoeEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "jane_doe"), "main");

    /** Balanceo lateral de reposo (rad): 8 grados de un lado a otro. */
    private static final float WAG_AMP = 0.14F;
    private static final float WAG_SPEED = 0.09F;
    /** La borla del extremo lleva medio ciclo de retraso: la cola persigue a su base. */
    private static final float TIP_LAG = 0.9F;
    private static final float TIP_AMP = 0.20F;
    /** Levantamiento suave, sincronizado con el balanceo. */
    private static final float LIFT_AMP = 0.05F;
    /** Cuanto se atrasa la cola con la zancada (xRot en rad). */
    private static final float STRIDE_AMP = 0.12F;
    /** En el aire la cola se estira hacia atras. */
    private static final float AIR_LIFT = 0.14F;

    private final ModelPart tailBase;
    private final ModelPart tailTip;

    public JaneDoeModel(ModelPart root) {
        super(root, true, 3.0F);
        ModelPart body = root.getChild("body");
        this.tailBase = body.getChild("tail_base");
        this.tailTip = this.tailBase.getChild("tail_tip");
    }

    @Override
    public void setupAnim(JaneDoeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // primero el andar de vanilla y la fisica del pecho; aqui solo se mueve la cola despues
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // yRot mueve la cola de lado a lado (su masa apunta a +Z, la espalda); xRot la sube o baja
        float wag = Mth.sin(ageInTicks * WAG_SPEED) * WAG_AMP;
        float lift = Mth.cos(ageInTicks * WAG_SPEED) * LIFT_AMP;
        float stride = Mth.sin(limbSwing * 0.5F) * STRIDE_AMP * limbSwingAmount;
        float air = entity.onGround() ? 0.0F : AIR_LIFT;

        this.tailBase.yRot = wag + stride * 0.5F;
        this.tailBase.xRot = lift - air;

        // la borla repite el balanceo medio ciclo despues y con mas recorrido
        float tipWag = Mth.sin(ageInTicks * WAG_SPEED - TIP_LAG) * TIP_AMP;
        this.tailTip.yRot = tipWag - wag * 0.35F;
        this.tailTip.xRot = lift * 0.6F + air * 0.5F + stride * 0.4F;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // 1. Pieza del busto grande (TUBE_BIG 3.0F), la misma capa compartida que llevaba antes
        body.addOrReplaceChild("breasts",
                CubeListBuilder.create().texOffs(0, 121)
                        .addBox(-4.0F, -1.5F, -1.5F, 8.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.79F, -2.0F, 45.0F * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        // 2. Cola. Pivote en la union con el torso: geo (0, 10.5, 2) -> java (0, 13.5, 2).
        //    geo_y = 24 - java_y y el origen del cubo va en ABSOLUTO, por eso los locales salen restando.
        PartDefinition tailBase = body.addOrReplaceChild("tail_base",
                CubeListBuilder.create()
                        // munon pegado a la espalda, en horizontal (geo -0.5,10,2 + 1x1x4)
                        .texOffs(5, 69).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 4.0F)
                        // riser vertical (geo -0.5,10,6 + 1x4x1)
                        .texOffs(8, 72).addBox(-0.5F, -3.5F, 4.0F, 1.0F, 4.0F, 1.0F)
                        // tramo que dobla hacia su derecha (geo -8.5,13,6 + 8x1x1)
                        .texOffs(1, 72).addBox(-8.5F, -3.5F, 4.0F, 8.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, 13.5F, 2.0F));

        // 3. La borla del extremo, con el pivote en la cara que comparte con el tramo horizontal:
        //    geo (-8.5, 13.5, 6.5) -> java absoluto (-8.5, 10.5, 6.5) -> local (-8.5, -3.0, 4.5)
        tailBase.addOrReplaceChild("tail_tip",
                CubeListBuilder.create()
                        // aletas planas (geo 1x0x2 y 0x1x2): en Java siguen siendo finas de verdad
                        .texOffs(29, 97).addBox(0.0F, 0.0F, -5.5F, 1.0F, 0.0F, 2.0F)
                        .texOffs(29, 97).addBox(-2.0F, 0.0F, -5.5F, 1.0F, 0.0F, 2.0F)
                        .texOffs(30, 97).addBox(-0.5F, 0.5F, -5.5F, 0.0F, 1.0F, 2.0F)
                        .texOffs(30, 97).addBox(-0.5F, -1.5F, -5.5F, 0.0F, 1.0F, 2.0F)
                        // varilla del extremo, doblada hacia delante (geo -9.5,13,-1 + 1x1x4)
                        .texOffs(27, 90).addBox(-1.0F, -0.5F, -7.5F, 1.0F, 1.0F, 4.0F)
                        .texOffs(5, 69).addBox(-1.0F, -0.5F, -3.5F, 1.0F, 1.0F, 4.0F),
                PartPose.offset(-8.5F, -3.0F, 4.5F));

        return LayerDefinition.create(mesh, 64, 128);
    }
}
