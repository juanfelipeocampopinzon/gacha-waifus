package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.BustBones;
import com.gachawaifus.entity.BurniceWhiteEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashMap;
import java.util.Map;

public class BurniceWhiteModel extends GeoModel<BurniceWhiteEntity> {
    /** Cuánto del giro lo lleva la cabeza y cuánto el torso. */
    private static final float HEAD_WEIGHT = 0.7F;
    private static final float BODY_WEIGHT = 0.15F;
    /** Tope del ángulo de cuello, en grados: más ya es torcer la nuca. */
    private static final float MAX_TURN_DEG = 35.0F;
    /** Hueso cuya rotación decide si un clip de habilidad se ocupa él solo de la cabeza. */
    private static final String HEAD_BONE = "head";
    /** Hueso que lleva el giro del torso: el padre común del torso y las piernas. */
    private static final String ROOT_BONE = "root";

    private final BoneTurn headTurn = new BoneTurn();
    private final BoneTurn bodyTurn = new BoneTurn();
    /** Si el clip keya {@code head}; se rellena leyendo el animation.json, nunca a mano. */
    private final Map<String, Boolean> headKeying = new HashMap<>();
    private long turnedInstanceId = Long.MIN_VALUE;

    @Override
    public ResourceLocation getModelResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "geo/burnice_white.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/burnice_white.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BurniceWhiteEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "animations/burnice_white.animation.json");
    }
    /**
     * GeckoLib llama aqui en cada fotograma, despues de aplicar las animaciones, así que lo que se
     * escriba en los huesos manda sobre lo que haya puesto el clip.
     *
     * <p>Giro de cabeza y torso hacia el objetivo. Se apaga al estar sentada (WaifuGeoSitPose ya le
     * pone la cabeza donde quiere) y mientras suena un clip de habilidad que keya la cabeza, para no
     * pelear con él; el tajo no la keya, así que ahí el giro se mantiene.
     *
     * <p>El torso gira sobre {@code root}, no sobre {@code body}: las piernas son hermanas del torso
     * bajo el root, así que girar solo el torso deja la cadera clavada en el yaw de la entidad y el
     * patrón del vestido —que es continuo a través de la cadera— se corta en la costura. Girando el
     * root entero, torso y piernas apuntan juntos y el cuello hace el resto desde ahí.
     */
    @Override
    public void setCustomAnimations(BurniceWhiteEntity animatable, long instanceId,
                                    AnimationState<BurniceWhiteEntity> animationState) {
        float turn = animatable.isAlive() && !animatable.isInSittingPose()
                && !this.clipTurnsHeadHerself(animatable)
                ? Mth.clamp(Mth.wrapDegrees(animatable.getYHeadRot() - animatable.yBodyRot),
                        -MAX_TURN_DEG, MAX_TURN_DEG) * Mth.DEG_TO_RAD
                : 0.0F;
        if (instanceId != this.turnedInstanceId) {
            this.turnedInstanceId = instanceId;
            this.headTurn.reset();
            this.bodyTurn.reset();
        }
        this.headTurn.apply(this.getBone(HEAD_BONE).orElse(null), turn * HEAD_WEIGHT);
        this.bodyTurn.apply(this.getBone(ROOT_BONE).orElse(null), turn * BODY_WEIGHT);
        BustBones.drive(this.getBone(BustBones.BONE_NAME).orElse(null), animatable,
                animationState.getPartialTick());
        WaifuGeoSitPose.apply(this, animatable, animationState.getPartialTick());
    }

    /**
     * Si el clip de habilidad que suena ahora mismo anima la cabeza él solo ({@code flame_channel},
     * {@code leap}, {@code flame_rain}, {@code flame_rest}, {@code recovery}): en esos clips el
     * gesto de la cabeza está coreografiado y pintarle encima el giro del objetivo la descuadra.
     * {@code attack} no keya {@code head}, y sin habilidad manda {@code idle}/{@code walk}/{@code run}.
     */
    private boolean clipTurnsHeadHerself(BurniceWhiteEntity animatable) {
        String clip = animatable.currentActionClip();
        if (clip == null) {
            return false;
        }
        Boolean known = this.headKeying.get(clip);
        if (known == null) {
            Animation animation = this.getAnimation(animatable, clip);
            known = animation != null && keysBone(animation, HEAD_BONE);
            this.headKeying.put(clip, known);
        }
        return known;
    }

    /** Si el clip trae keyframes para ese hueso. {@code boneAnimations()} lista los que animan. */
    private static boolean keysBone(Animation animation, @Nullable String boneName) {
        BoneAnimation[] bones = animation.boneAnimations();
        if (bones == null || boneName == null) {
            return false;
        }
        for (BoneAnimation bone : bones) {
            if (boneName.equals(bone.boneName())) {
                return true;
            }
        }
        return false;
    }
}
