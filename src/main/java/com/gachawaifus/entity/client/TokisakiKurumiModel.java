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
 *   <li>Mosquete de chispa: rifle largo en mano derecha. Hoy no se ve: su desplegado UV
 *       (32,64 / 32,70 / 32,74) esta sin pintar en la skin.</li>
 * </ul>
 */
public class TokisakiKurumiModel extends WaifuBustPlayerModel<TokisakiKurumiEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "tokisaki_kurumi"), "main");

    public TokisakiKurumiModel(ModelPart root) {
        super(root, true, 3.0F);
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
        PartDefinition rightArm = root.getChild("right_arm");

        // El giro se hereda TAL CUAL del .geo.json: con X e Z iguales en los dos espacios y la Y
        // reflejada, el signo de Z no cambia (probarlo con el mechon lateral, que sin ese giro
        // quedaria a 6 px de la cabeza y con él cae sobre la oreja).
        head.addOrReplaceChild("strand_top_right",
                CubeListBuilder.create().texOffs(2, 84).addBox(-2.0F, -4.0F, -1.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(-3.0F, -5.0F, 3.0F, 0.0F, 0.0F, -15.0F * Mth.DEG_TO_RAD));
        head.addOrReplaceChild("strand_top_left",
                CubeListBuilder.create().texOffs(2, 84).addBox(-0.96F, -0.14F, -1.0F, 2.0F, 1.0F, 3.0F),
                PartPose.offsetAndRotation(4.96F, -8.36F, 3.0F, 0.0F, 0.0F, 15.0F * Mth.DEG_TO_RAD));
        head.addOrReplaceChild("strand_side_left",
                CubeListBuilder.create().texOffs(9, 99).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 13.0F, 3.0F),
                PartPose.offsetAndRotation(11.0F, 4.0F, 3.0F, 0.0F, 0.0F, -25.0F * Mth.DEG_TO_RAD));
        head.addOrReplaceChild("strand_side_right",
                CubeListBuilder.create().texOffs(9, 99).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 3.0F),
                PartPose.offsetAndRotation(-8.0F, -1.0F, 3.0F, 0.0F, 0.0F, 25.0F * Mth.DEG_TO_RAD));
        // ---- fin del vuelco desde Blockbench ----

        // 1. Pieza del busto grande (TUBE_BIG 3.0F)
        CubeListBuilder tube = CubeListBuilder.create()
                .texOffs(0, 121)
                .addBox(-4.0F, -1.5F, -1.5F, 8.0F, 3.0F, 3.0F);
        body.addOrReplaceChild("breasts", tube, PartPose.offsetAndRotation(
                0.0F, 3.79F, -2.0F, 45.0F * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        // 2. Mosquete de chispa — cañón apunta en Y+ (dirección del brazo, hacia abajo)
        //    En el brazo slim: pivot en hombro, mano ≈ Y=10. Offset del rifle (0,2,0).
        //    Culata arriba (cerca del hombro), cañón se extiende pasando la mano (Y+).
        rightArm.addOrReplaceChild("rifle",
                CubeListBuilder.create()
                        // Culata de madera caoba (entre hombro y mano)
                        .texOffs(32, 64).addBox(-0.5F, 2.0F, -1.0F, 1.0F, 4.0F, 2.0F)
                        // Llave de chispa dorada (a la altura de la mano)
                        .texOffs(32, 70).addBox(-0.5F, 5.5F, 0.0F, 1.0F, 2.0F, 1.0F)
                        // Cañón largo de acero pavonado (se extiende 14u pasando la mano)
                        .texOffs(32, 74).addBox(-0.5F, 7.0F, -0.5F, 1.0F, 14.0F, 1.0F),
                PartPose.offset(0.0F, 2.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 128);
    }
}
