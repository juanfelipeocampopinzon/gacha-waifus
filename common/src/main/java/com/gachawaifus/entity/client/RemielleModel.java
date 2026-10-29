package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.RemielleEntity;
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
 * Modelo de Remielle: cuerpo de jugadora slim con la pieza del pecho grande y sus alas.
 *
 * <p>Las alas vienen del .geo.json editado en Blockbench
 * ({@code modelos/personajes/remielle}), donde estan dibujadas como 43 cubos sueltos dentro del
 * hueso {@code body}. En Java un cubo no puede girar por si solo, asi que se parten en dos huesos
 * ({@code wing_left} / {@code wing_right}) que comparten el mismo pivote: la union del ala con la
 * espalda, en la cara trasera del torso (z = 3) a la altura del hombro (y = 7). Al colgar del
 * hombro, el aleteo es el pendulo normal del pelo: giro de Z.
 *
 * <p>El andar sigue siendo el de vanilla; la animacion nueva se aplica despues de
 * {@code super.setupAnim} para no pisar ni las extremidades ni la fisica del pecho.
 */
public class RemielleModel extends WaifuBustPlayerModel<RemielleEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "remielle"), "main");

    /** Ala caida y casi pegada al cuerpo, con un respiro de medio grado. */
    private static final float IDLE_OPEN = 0.07F;
    /** Amplitud del respiro en reposo (rad). */
    private static final float IDLE_AMP = 0.05F;
    private static final float IDLE_SPEED = 0.055F;

    /** En el aire el ala se abre hacia arriba para poder ver el aleteo. */
    private static final float AIR_OPEN = 0.34F;
    private static final float AIR_AMP = 0.30F;
    private static final float AIR_SPEED = 0.45F;

    private final ModelPart wingLeft;
    private final ModelPart wingRight;

    public RemielleModel(ModelPart root) {
        super(root, true, 3.0F);
        ModelPart body = root.getChild("body");
        this.wingLeft = body.getChild("wing_left");
        this.wingRight = body.getChild("wing_right");
    }

    @Override
    public void setupAnim(RemielleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // primero el andar de vanilla + brazos + fisica del pecho, despues se anaden las alas
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        boolean airborne = !entity.onGround();
        float open = airborne ? AIR_OPEN : IDLE_OPEN;
        float amp = airborne ? AIR_AMP : IDLE_AMP;
        float speed = airborne ? AIR_SPEED : IDLE_SPEED;

        // punta del ala hacia arriba = Z negativo en el lado izquierdo (+X) y positivo en el
        // derecho (-X); es el mismo pendulo del pelo, con el signo heredado del geo.
        float flap = open + Mth.cos(ageInTicks * speed) * amp;
        if (!airborne) {
            // al caminar las alas se balancean con la zancada, sin separarse del paso
            flap += Mth.sin(limbSwing * 0.5F) * 0.05F * limbSwingAmount;
        }

        this.wingLeft.zRot = -flap;
        this.wingRight.zRot = flap;

        // barrido: la superficie se inclina hacia atras y adelante (+X = la punta hacia +Z, atras)
        float sweep = Mth.cos(ageInTicks * speed + 1.5707963F) * (airborne ? 0.16F : 0.04F);
        this.wingLeft.xRot = sweep;
        this.wingRight.xRot = sweep;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");

        // 1. Pieza del pecho grande (TUBE_BIG 3.0F), igual que en la capa compartida
        body.addOrReplaceChild("breasts",
                CubeListBuilder.create()
                        .texOffs(0, 121).addBox(-4.0F, -1.5F, -1.5F, 8.0F, 3.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 3.79F, -2.0F, 45.0F * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        // ---- 2. alas volcadas desde modelos/personajes/remielle/remielle.geo.json ----
        // Alas del lado izquierdo del personaje (hacia +X en Java): 21 cubos
        body.addOrReplaceChild("wing_left",
                CubeListBuilder.create()
                        .texOffs(13, 74).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(15, 75).addBox(-0.5F, -1.0F, 1.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(13, 74).addBox(8.5F, -3.0F, 1.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(13, 73).addBox(10.5F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(16, 74).addBox(11.5F, -2.0F, -1.0F, 1.0F, 9.0F, 0.0F)
                        .texOffs(16, 74).addBox(10.5F, -2.0F, 1.0F, 1.0F, 5.0F, 0.0F)
                        .texOffs(14, 75).addBox(8.5F, -2.0F, 2.0F, 2.0F, 4.0F, 0.0F)
                        .texOffs(12, 75).addBox(4.5F, -1.0F, 3.0F, 4.0F, 2.0F, 0.0F)
                        .texOffs(11, 75).addBox(1.5F, 0.0F, 3.0F, 3.0F, 1.0F, 0.0F)
                        .texOffs(14, 75).addBox(-0.5F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F)
                        .texOffs(59, 73).addBox(12.5F, -2.0F, -3.0F, 0.0F, 13.0F, 2.0F)
                        .texOffs(14, 73).addBox(11.5F, -2.0F, -1.0F, 0.0F, 7.0F, 2.0F)
                        .texOffs(16, 73).addBox(10.5F, -2.0F, 1.0F, 0.0F, 4.0F, 1.0F)
                        .texOffs(19, 75).addBox(8.5F, -1.0F, 2.0F, 0.0F, 2.0F, 1.0F)
                        .texOffs(3, 1).addBox(1.5F, 0.0F, 2.0F, 0.0F, 1.0F, 1.0F)
                        .texOffs(3, 1).addBox(-0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F)
                        .texOffs(13, 73).addBox(11.5F, -3.0F, -3.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(14, 74).addBox(3.5F, -2.0F, 2.0F, 1.0F, 2.0F, 1.0F)
                        .texOffs(13, 74).addBox(1.5F, -1.0F, 2.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(13, 74).addBox(4.5F, -2.0F, 2.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(13, 74).addBox(6.5F, -2.0F, 2.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(1.5F, 7.0F, 3.0F, 0.0F, 0.0F, 0.0F));

        // Alas del lado derecho del personaje (hacia -X en Java): 22 cubos
        body.addOrReplaceChild("wing_right",
                CubeListBuilder.create()
                        .texOffs(13, 74).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F)
                        .texOffs(10, 75).addBox(-1.5F, -1.0F, 1.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(15, 75).addBox(-10.5F, -3.0F, 1.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(13, 73).addBox(-11.5F, -3.0F, -1.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(13, 73).addBox(-12.5F, -3.0F, -3.0F, 1.0F, 1.0F, 2.0F)
                        .texOffs(16, 74).addBox(-12.5F, -2.0F, -1.0F, 1.0F, 9.0F, 0.0F)
                        .texOffs(16, 74).addBox(-11.5F, -2.0F, 1.0F, 1.0F, 5.0F, 0.0F)
                        .texOffs(55, 79).addBox(-10.5F, -2.0F, 2.0F, 2.0F, 4.0F, 0.0F)
                        .texOffs(51, 75).addBox(-8.5F, -1.0F, 3.0F, 4.0F, 2.0F, 0.0F)
                        .texOffs(13, 75).addBox(-4.5F, 0.0F, 3.0F, 3.0F, 1.0F, 0.0F)
                        .texOffs(33, 84).addBox(-1.5F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F)
                        .texOffs(14, 75).addBox(-1.5F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F)
                        .texOffs(59, 73).addBox(-12.5F, -2.0F, -3.0F, 0.0F, 13.0F, 2.0F)
                        .texOffs(14, 73).addBox(-11.5F, -2.0F, -1.0F, 0.0F, 7.0F, 2.0F)
                        .texOffs(16, 73).addBox(-10.5F, -2.0F, 1.0F, 0.0F, 4.0F, 1.0F)
                        .texOffs(16, 74).addBox(-8.5F, -1.0F, 2.0F, 0.0F, 2.0F, 1.0F)
                        .texOffs(3, 1).addBox(-1.5F, 0.0F, 2.0F, 0.0F, 1.0F, 1.0F)
                        .texOffs(3, 1).addBox(0.5F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F)
                        .texOffs(14, 74).addBox(-4.5F, -2.0F, 2.0F, 1.0F, 2.0F, 1.0F)
                        .texOffs(15, 75).addBox(-3.5F, -1.0F, 2.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(15, 75).addBox(-6.5F, -2.0F, 2.0F, 2.0F, 1.0F, 1.0F)
                        .texOffs(12, 76).addBox(-8.5F, -2.0F, 2.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offsetAndRotation(-1.5F, 7.0F, 3.0F, 0.0F, 0.0F, 0.0F));
        // ---- fin del vuelco desde Blockbench ----

        return LayerDefinition.create(mesh, 64, 128);
    }
}
