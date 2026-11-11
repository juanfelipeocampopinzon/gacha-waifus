package com.gachawaifus.entity.client;

import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

/**
 * Pose de concierto para los modelos GeckoLib (Miku, Teto y Neru).
 *
 * <p>Es el equivalente Geo de la pose de idol de {@code WaifuPlayerModel}: micrófono imaginario
 * delante de la boca con el brazo izquierdo, brazo derecho abierto al público y un balanceo suave
 * al ritmo de la canción.
 *
 * <p>GeckoLib aplica las rotaciones en orden <b>Z → Y → X</b> ({@code RenderUtil.optionalRotateZYX}),
 * así que el pitch (X) actúa primero sobre el brazo colgando y luego el yaw (Y) lo abre hacia fuera.
 * Con el brazo de 10 unidades por debajo del hombro:
 * <ul>
 *   <li>X ≈ 145° deja la mano a la altura de la boca, delante de la cara.</li>
 *   <li>X ≈ 90° + Y ≈ 57° extiende el brazo derecho en horizontal hacia el público.</li>
 * </ul>
 *
 * <p>Como {@code setCustomAnimations} corre después del procesador de animaciones, esta pose pisa
 * el {@code idle} mientras dura el concierto. Al terminar hay que devolver los huesos a su
 * snapshot inicial: el {@code idle} reescribe torso, cabeza y brazos cada frame, pero <b>no las
 * piernas</b>, y se quedarían giradas.
 */
public final class WaifuGeoConcertPose {

    /** Huesos que toca la pose; se restauran al salir. */
    private static final String[] TOUCHED = {
            "root", "body", "head", "left_arm", "right_arm", "left_leg", "right_leg"
    };

    /** Balanceo por tick (0,12 rad/s ≈ un compás cada 2 s a 128 BPM). */
    private static final float SWAY_SPEED = 0.12F;

    /** Amplitud del rebote vertical del cuerpo, en unidades de modelo (16 = 1 bloque). */
    private static final float BOUNCE_UNITS = 0.18F;

    /** Brazo izquierdo: mano delante de la boca sujetando el micro. */
    private static final float MIC_ARM_X = 2.20F;
    private static final float MIC_ARM_Y = 0.35F;

    /** Brazo derecho: extendido al público, marcando el compás. */
    private static final float OPEN_ARM_X = 1.75F;
    private static final float OPEN_ARM_Y = 0.90F;

    private WaifuGeoConcertPose() {
    }

    public static void apply(GeoModel<? extends AbstractWaifuEntity> model, AbstractWaifuEntity waifu) {
        apply(model, waifu, 0.0F);
    }

    public static void apply(GeoModel<? extends AbstractWaifuEntity> model, AbstractWaifuEntity waifu, float partialTick) {
        if (!waifu.isSinging()) {
            if (waifu.hasModelConcertPose()) {
                for (String name : TOUCHED) {
                    restore(model, name);
                }
                waifu.clearModelConcertPose();
            }
            return;
        }

        waifu.markModelConcertPose();

        float age = waifu.tickCount + partialTick;
        float t = age * SWAY_SPEED;
        float sway = Mth.sin(t);
        float bounce = Mth.sin(t * 2.0F);
        float beat = Mth.sin(t * 1.5F);

        // Rebote vertical muy leve, como si marcara el ritmo con las rodillas.
        model.getBone("root").ifPresent(bone -> bone.setPosY(bounce * BOUNCE_UNITS));

        // Torso y cabeza: vaivén de idol, nunca quieta del todo.
        model.getBone("body").ifPresent(bone -> {
            bone.setRotX(0.02F + bounce * 0.015F);
            bone.setRotY(sway * 0.07F);
            bone.setRotZ(sway * 0.035F);
        });
        model.getBone("head").ifPresent(bone -> {
            bone.setRotX(-0.10F + bounce * 0.06F);
            bone.setRotY(sway * 0.14F);
            bone.setRotZ(-sway * 0.05F);
        });

        // Brazo izquierdo: mano delante de la boca, como sujetando el micrófono.
        model.getBone("left_arm").ifPresent(bone -> {
            bone.setRotX(MIC_ARM_X + beat * 0.07F);
            bone.setRotY(MIC_ARM_Y);
            bone.setRotZ(-0.08F + beat * 0.03F);
        });

        // Brazo derecho: abierto hacia el público, subiendo y bajando con el compás.
        model.getBone("right_arm").ifPresent(bone -> {
            bone.setRotX(OPEN_ARM_X + Mth.sin(t * 1.3F + 0.8F) * 0.18F);
            bone.setRotY(OPEN_ARM_Y);
            bone.setRotZ(sway * 0.12F);
        });

        // Piernas ligeramente abiertas y con un pequeño rebote alterno.
        model.getBone("left_leg").ifPresent(bone -> {
            bone.setRotX(beat * 0.02F);
            bone.setRotY(0.06F);
            bone.setRotZ(-0.055F);
        });
        model.getBone("right_leg").ifPresent(bone -> {
            bone.setRotX(-beat * 0.02F);
            bone.setRotY(-0.06F);
            bone.setRotZ(0.055F);
        });
    }

    /** Devuelve un hueso a la rotación y posición con las que se cargó el geo. */
    private static void restore(GeoModel<? extends AbstractWaifuEntity> model, String name) {
        model.getBone(name).ifPresent(bone -> {
            BoneSnapshot initial = bone.getInitialSnapshot();
            bone.setRotX(initial.getRotX());
            bone.setRotY(initial.getRotY());
            bone.setRotZ(initial.getRotZ());
            bone.setPosX(initial.getOffsetX());
            bone.setPosY(initial.getOffsetY());
            bone.setPosZ(initial.getOffsetZ());
        });
    }
}
