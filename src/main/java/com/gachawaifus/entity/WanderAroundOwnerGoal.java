package com.gachawaifus.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Paseo alrededor del dueño: elige un punto dentro del disco de radio {@link WanderConfig#RANGE}
 * centrado en él, en vez del cuadrado fijo de 10 bloques alrededor de la propia waifu que usa
 * {@link WaterAvoidingRandomStrollGoal}.
 *
 * <p>Sin esto la waifu pasea sobre su propia posición y solo se aleja del jugador cuando él
 * camina; con el radio configurable puede explorar alrededor de donde está el dueño.
 */
public class WanderAroundOwnerGoal extends WaterAvoidingRandomStrollGoal {

    private final AbstractWaifuEntity waifu;

    public WanderAroundOwnerGoal(AbstractWaifuEntity waifu, double speedModifier) {
        super(waifu, speedModifier);
        this.waifu = waifu;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        Vec3 aroundOwner = this.positionAroundOwner();
        return aroundOwner != null ? aroundOwner : super.getPosition();
    }

    /**
     * Intenta 10 veces un punto transitable del disco del dueño. Devuelve {@code null} si no hay
     * dueño, si se perdió de vista (ahí manda el seguimiento) o si ningún candidato es válido:
     * el paseo normal evita que la waifu se quede plantada.
     */
    @Nullable
    private Vec3 positionAroundOwner() {
        LivingEntity owner = this.waifu.getOwner();
        if (owner == null) {
            return null;
        }

        int range = WanderConfig.range();
        // Demasiado lejos del dueño: no la llamo más hacia allá, que se la trague el FollowOwner.
        if (this.waifu.distanceToSqr(owner) > (double) range * range) {
            return null;
        }

        PathNavigation navigation = this.mob.getNavigation();
        boolean restricted = GoalUtils.mobRestricted(this.mob, range);

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = this.mob.getRandom().nextFloat() * (float) (Math.PI * 2.0);
            // Raíz de un uniforme: puntos repartidos por área del disco, no amontonados en el borde.
            double radius = range * (0.3 + 0.7 * Math.sqrt(this.mob.getRandom().nextFloat()));
            BlockPos candidate = BlockPos.containing(
                    owner.getX() + Math.cos(angle) * radius,
                    owner.getY(),
                    owner.getZ() + Math.sin(angle) * radius);
            BlockPos lifted = LandRandomPos.movePosUpOutOfSolid(this.mob, candidate);
            if (lifted == null
                    || GoalUtils.isOutsideLimits(lifted, this.mob)
                    || GoalUtils.isRestricted(restricted, this.mob, lifted)
                    || GoalUtils.isNotStable(navigation, lifted)) {
                continue;
            }
            return Vec3.atBottomCenterOf(lifted);
        }
        return null;
    }
}
