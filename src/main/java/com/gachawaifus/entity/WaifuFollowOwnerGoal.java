package com.gachawaifus.entity;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

/**
 * Versión configurable de {@link net.minecraft.world.entity.ai.goal.FollowOwnerGoal}.
 *
 * <p>La clase vanilla guarda las distancias como campos {@code final} copiados en el constructor,
 * así que cambiar {@link WanderConfig#RANGE} en el juego no la afectaba. Aquí el radio se lee en
 * vivo en cada comprobación: mover el deslizador se nota sin reinvocar a la waifu.
 */
public class WaifuFollowOwnerGoal extends Goal {

    private final TamableAnimal tamable;
    @Nullable
    private LivingEntity owner;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public WaifuFollowOwnerGoal(TamableAnimal tamable, double speedModifier) {
        this.tamable = tamable;
        this.speedModifier = speedModifier;
        this.navigation = tamable.getNavigation();
        if (!(this.navigation instanceof GroundPathNavigation)
                && !(this.navigation instanceof FlyingPathNavigation)) {
            throw new IllegalArgumentException("Unsupported mob type for WaifuFollowOwnerGoal");
        }
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** Desde qué distancia empieza a seguir al dueño (el radio configurado). */
    private float startDistance() {
        return (float) WanderConfig.range();
    }

    /** Distancia a la que deja de caminar hacia él: un tercio del radio, sin bajar de 2 bloques. */
    private float stopDistance() {
        return Math.max(2.0F, WanderConfig.range() * 0.35F);
    }

    @Override
    public boolean canUse() {
        LivingEntity livingentity = this.tamable.getOwner();
        if (livingentity == null) {
            return false;
        } else if (this.tamable.unableToMoveToOwner()) {
            return false;
        } else if (this.tamable.distanceToSqr(livingentity) < (double) (this.startDistance() * this.startDistance())) {
            return false;
        } else {
            this.owner = livingentity;
            return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) {
            return false;
        }
        return !this.tamable.unableToMoveToOwner()
                && !(this.tamable.distanceToSqr(this.owner) <= (double) (this.stopDistance() * this.stopDistance()));
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
        this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        boolean teleporting = this.tamable.shouldTryTeleportToOwner();
        if (!teleporting) {
            this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float) this.tamable.getMaxHeadXRot());
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (teleporting) {
                this.tamable.tryToTeleportToOwner();
            } else {
                this.navigation.moveTo(this.owner, this.speedModifier);
            }
        }
    }
}
