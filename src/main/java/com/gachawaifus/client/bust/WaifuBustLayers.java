package com.gachawaifus.client.bust;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * La pieza del pecho para las waifus que usan el modelo de jugador vanilla
 * (Astra, Yuzuha, Promeia y Remielle): un tubo de seccion cuadrada girado 45 grados, del
 * ancho del torso, pegado a la cara delantera del pecho.
 *
 * <p>Es una sola pieza por waifu y sale de su propia skin: el desplegado UV de la caja se
 * pinta al final de la textura copiando los cuadros del pecho que la pieza tapa (ver
 * {@code research/bust/tube_v5_sizes.py}). Aqui solo se hornea la geometria.
 *
 * <p>Dos tamanos, los mismos que en los {@code .geo.json}:
 * <ul>
 *   <li>Normal (2x2): cubo 8x2x2, centro a y=19.5 del modelo y z=-2, tapa 3 cuadros de skin.</li>
 *   <li>Grande (3x3): cubo 8x3x3, centro a y=20.21 y z=-2, tapa 4 cuadros (crece un cuadro
 *       hacia arriba; el borde de abajo se queda igual).</li>
 * </ul>
 *
 * <p>El centro va en z = -2, que es justo la cara delantera del torso: al girar 45 grados las
 * puntas de arriba y abajo del rombo caen en esa cara, o sea que la pieza queda sentada sobre
 * el pecho y no flotando.
 *
 * <p>Se hornea sobre una copia propia de la malla de jugador slim: no se toca la capa
 * compartida de jugador (si se mutara, todos los jugadores tendrian pieza).
 */
public final class WaifuBustLayers {

    /** Esquina del desplegado UV dentro de la skin (20x4 la normal, 22x6 la grande). */
    private static final int UV_U = 0;
    private static final int UV_V = 121;

    /** Ancho: el torso entero (x -4..4). */
    private static final float WIDTH = 8.0F;

    /** Cara delantera del torso, donde se apoya la pieza. */
    private static final float PIVOT_Z = -2.0F;

    /** Inclinacion de la pieza, la misma que en los .geo.json. */
    private static final float TILT_DEGREES = 45.0F;

    /** Centro en el modelo de jugador (su Y crece hacia los pies: 24 - y). */
    private static final float PIVOT_Y_NORMAL = 24.0F - 19.5F;
    private static final float PIVOT_Y_BIG = 24.0F - 20.21F;

    public static final ModelLayerLocation TUBE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "waifu_bust"), "main");
    public static final ModelLayerLocation TUBE_BIG = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "waifu_bust_big"), "main");

    private WaifuBustLayers() {
    }

    /** Pieza normal: 8x2x2 (Astra, Yuzuha, Promeia). */
    public static LayerDefinition create() {
        return build(PIVOT_Y_NORMAL, 2.0F);
    }

    /** Pieza grande: 8x3x3 (Remielle). */
    public static LayerDefinition createBig() {
        return build(PIVOT_Y_BIG, 3.0F);
    }

    private static LayerDefinition build(float pivotY, float section) {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition body = mesh.getRoot().getChild("body");

        CubeListBuilder tube = CubeListBuilder.create()
                .texOffs(UV_U, UV_V)
                .addBox(-WIDTH / 2.0F, -section / 2.0F, -section / 2.0F,
                        WIDTH, section, section);

        body.addOrReplaceChild("breasts", tube, PartPose.offsetAndRotation(
                0.0F, pivotY, PIVOT_Z, TILT_DEGREES * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        // La capa declara alto 128: la mitad de arriba es la skin de siempre y la de abajo
        // lleva el desplegado de la pieza.
        return LayerDefinition.create(mesh, 64, 128);
    }
}
