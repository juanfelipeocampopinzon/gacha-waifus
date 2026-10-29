package com.gachawaifus.entity.client;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;
import software.bernie.geckolib.model.GeoModel;

/**
 * Aplica la pose de sentada en el suelo a los modelos GeckoLib (Miyabi, Ellen, Anby, Burnice, Nicole, Ye Shunguang).
 *
 * <p>Diferencias clave con el modelo de jugador vanilla:
 * <ul>
 *   <li>En Blockbench/GeckoLib, la rotación X <b>positiva</b> rota los miembros hacia adelante.</li>
 *   <li>{@code GeoEntityRenderer} no hereda de {@code LivingEntityRenderer}, por lo que
 *       {@code RenderLivingEvent.Pre} no se dispara para estas entidades. Para bajarlas hasta el
 *       suelo se desplaza el hueso {@code "root"} con {@code root.setPosY(-10.0F)}, lo que
 *       equivale a 10 / 16 = 0,625 bloques hacia abajo.</li>
 * </ul>
 */
public final class WaifuGeoSitPose {

    /** Hundimiento del hueso root en unidades de modelo (16 unidades = 1 bloque -> 10/16 = 0,625 bloques). */
    private static final float ROOT_FLOOR_SINK = -10.0F;

    /** Piernas estiradas hacia adelante: +85° (+1,48 rad en rotación X positiva de GeckoLib). */
    private static final float LEGS_FORWARD = 1.48F;

    /**
     * Cuánto se separan las caderas al sentarse, en unidades de modelo (1/16 de bloque por pierna).
     * Las piernas de un geo suelen estar a ±2 de pivote con cubos de 4 de ancho: sus caras internas
     * se tocan o se solapan, y sentadas ese solape es la costura que mezcla la textura. El lado lo
     * dice el propio pivote del hueso, así que no hay que saber si la izquierda es +X o -X.
     */
    private static final float LEGS_APART_X = 1.0F;

    /** Cuánto se separan las puntas de los pies (rotación Y). */
    private static final float LEGS_APART_Y = 0.28F;

    /** Inclinación lateral de las piernas (rotación Z). */
    private static final float LEGS_TILT_Z = 0.06F;

    /** Brazos descansando sobre el regazo y muslos (+43° hacia adelante/abajo en GeckoLib). */
    private static final float ARMS_DOWN = 0.75F;
    private static final float ARMS_INWARD_Y = 0.12F;
    private static final float ARMS_RELAX_Z = 0.10F;

    /** Amplitud de respiración en unidades de modelo y velocidad por tick. */
    private static final float BREATH_UNITS = 0.20F;
    private static final float BREATH_SPEED = 0.055F;

    private WaifuGeoSitPose() {
    }

    public static void apply(GeoModel<? extends AbstractWaifuEntity> model, AbstractWaifuEntity waifu) {
        apply(model, waifu, 0.0F);
    }

    public static void apply(GeoModel<? extends AbstractWaifuEntity> model, AbstractWaifuEntity waifu, float partialTick) {
        if (!waifu.isInSittingPose()) {
            // Asegura que al levantarse el hueso raíz vuelva a la altura normal
            model.getBone("root").ifPresent(root -> root.setPosY(0.0F));
            // y que las caderas recuperen su sitio: el desplazamiento es memoria del hueso
            model.getBone("left_leg").ifPresent(leg -> leg.setPosX(0.0F));
            model.getBone("right_leg").ifPresent(leg -> leg.setPosX(0.0F));
            return;
        }
        waifu.markModelSitPose();

        float age = waifu.tickCount + partialTick;
        float breath = Mth.sin(age * BREATH_SPEED) * BREATH_UNITS;

        // Bajar al suelo (0,625 bloques) con respiración
        model.getBone("root").ifPresent(root -> root.setPosY(ROOT_FLOOR_SINK + breath));

        // Piernas hacia adelante sobre el suelo (rotación X positiva) y caderas abiertas
        model.getBone("left_leg").ifPresent(bone -> {
            bone.setPosX(Math.signum(bone.getPivotX()) * LEGS_APART_X);
            bone.setRotX(LEGS_FORWARD);
            bone.setRotY(LEGS_APART_Y);
            bone.setRotZ(-LEGS_TILT_Z);
        });
        model.getBone("right_leg").ifPresent(bone -> {
            bone.setPosX(Math.signum(bone.getPivotX()) * LEGS_APART_X);
            bone.setRotX(LEGS_FORWARD);
            bone.setRotY(-LEGS_APART_Y);
            bone.setRotZ(LEGS_TILT_Z);
        });

        // Brazos descansando en el regazo (rotación X positiva)
        model.getBone("left_arm").ifPresent(bone -> {
            bone.setRotX(ARMS_DOWN);
            bone.setRotY(-ARMS_INWARD_Y);
            bone.setRotZ(ARMS_RELAX_Z);
        });
        model.getBone("right_arm").ifPresent(bone -> {
            bone.setRotX(ARMS_DOWN);
            bone.setRotY(ARMS_INWARD_Y);
            bone.setRotZ(-ARMS_RELAX_Z);
        });
    }
}
