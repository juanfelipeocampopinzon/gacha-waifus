package com.gachawaifus.item;

import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * El bate de béisbol de Tung Tung Sahur: "arma" decorada que sostiene el jefe (envía lejos a
 * quien pegue). Se renderiza con GeckoLib ({@code geo/item/beisbol_bate.geo.json}).
 */
public class BateBeisbolItem extends Item implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BateBeisbolItem(Properties properties) {
        super(properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Modelo estático: sin animaciones.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
