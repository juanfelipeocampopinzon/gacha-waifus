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
 * (Astra, Yuzuha, Promeia, Remielle, Kurumi y Yidhari): un tubo de seccion cuadrada girado 45 grados, del
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
    /**
     * Lienzo de las planas: la malla slim sin pieza, pero declarada en 64x128. Existe para que las
     * filas 64-127 sean sitio real donde pintar cubos nuevos (coletas, lazos, capas): con la capa
     * vanilla {@code ModelLayers.PLAYER_SLIM} el alto es 64 y todo lo que caiga por debajo no se ve.
     */
    public static final ModelLayerLocation SLIM_128 = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "waifu_slim_128"), "main");
    /**
     * Malla de Promeia: la pieza normal más la cola de caballo. Va en capa propia para que la cola
     * no se le pegue a las demás waifus que comparten {@link #TUBE}.
     */
    public static final ModelLayerLocation PROMEIA = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "promeia"), "main");

    /** Cola de caballo del {@code promeia.geo.json}: caja 2x18x2 colgando de la nuca. */
    private static final int TAIL_UV_U = 0;
    private static final int TAIL_UV_V = 69;

    private WaifuBustLayers() {
    }

    /** Pieza normal: 8x2x2 (Yuzuha, Promeia). */
    public static LayerDefinition create() {
        return build(PIVOT_Y_NORMAL, 2.0F, true);
    }

    /** Pieza grande: 8x3x3 (Astra, Remielle, Kurumi, Yidhari). */
    public static LayerDefinition createBig() {
        return build(PIVOT_Y_BIG, 3.0F, true);
    }

    /**
     * Planas (sin pieza) con lienzo de 128: la malla slim de vanilla tal cual, solo que declarada
     * a 64x128 para que el workbench y los cubos nuevos tengan filas por debajo de la 64.
     */
    public static LayerDefinition createSlim128() {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 128);
    }

    /** Promeia: pieza normal sobre malla slim más la cola de caballo colgada de la cabeza. */
    public static LayerDefinition createPromeia() {
        return build(PIVOT_Y_NORMAL, 2.0F, true, true);
    }

    private static LayerDefinition build(float pivotY, float section, boolean slim) {
        return build(pivotY, section, slim, false);
    }

    private static LayerDefinition build(float pivotY, float section, boolean slim, boolean ponytail) {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        PartDefinition body = mesh.getRoot().getChild("body");

        CubeListBuilder tube = CubeListBuilder.create()
                .texOffs(UV_U, UV_V)
                .addBox(-WIDTH / 2.0F, -section / 2.0F, -section / 2.0F,
                        WIDTH, section, section);

        body.addOrReplaceChild("breasts", tube, PartPose.offsetAndRotation(
                0.0F, pivotY, PIVOT_Z, TILT_DEGREES * Mth.DEG_TO_RAD, 0.0F, 0.0F));

        if (ponytail) {
            // Pivote en la nuca (y=0 del modelo de jugador) y caja hacia abajo y atrás, igual que
            // el hueso "cola de caballo" del .geo.json: origen [-1,6,3], tamaño [2,18,2].
            CubeListBuilder tail = CubeListBuilder.create()
                    .texOffs(TAIL_UV_U, TAIL_UV_V)
                    .addBox(-1.0F, 0.0F, 3.0F, 2.0F, 18.0F, 2.0F);
            mesh.getRoot().getChild("head").addOrReplaceChild("ponytail", tail, PartPose.ZERO);
        }

        // La capa declara alto 128: la mitad de arriba es la skin de siempre y la de abajo
        // lleva el desplegado de la pieza.
        return LayerDefinition.create(mesh, 64, 128);
    }
}
