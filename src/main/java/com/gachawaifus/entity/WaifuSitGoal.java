package com.gachawaifus.entity;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * El sentarse de las waifus, sin las sorpresas del de vanilla.
 *
 * <p>{@code SitWhenOrderedToGoal} sienta a la mascota en cuanto {@code getOwner()} devuelve
 * {@code null}, y eso le pasa a cualquiera cuyo dueño esté desconectado o en otra dimensión: la
 * waifu se quedaba sentada (y quieta) por defecto sin que nadie se lo hubiera pedido. Aquí la única
 * orden que manda es {@link TamableAnimal#isOrderedToSit()}, que en este mod pone
 * {@code AbstractWaifuEntity#applyWaifuMode} con el modo Sentada, y las caricias de Astra y Nicole.
 */
public class WaifuSitGoal extends Goal {

    private static final EnumSet<Goal.Flag> FLAGS = EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE);

    private final TamableAnimal waifu;

    public WaifuSitGoal(TamableAnimal waifu) {
        this.waifu = waifu;
    }

    @Override
    public boolean canUse() {
        return this.waifu.isOrderedToSit() && this.waifu.isTame() && this.waifu.onGround()
                && !this.waifu.isInWaterOrBubble();
    }

    @Override
    public boolean canContinueToUse() {
        return this.waifu.isOrderedToSit();
    }

    @Override
    public void start() {
        this.waifu.getNavigation().stop();
        this.waifu.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.waifu.setInSittingPose(false);
    }

    @Override
    public EnumSet<Goal.Flag> getFlags() {
        return FLAGS;
    }
}
