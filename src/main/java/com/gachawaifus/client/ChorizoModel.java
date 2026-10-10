package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.ChorizoItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

/**
 * Modelo geo del chorizo: resuelve por defecto {@code geo/item/chorizo.geo.json},
 * {@code animations/item/chorizo.animation.json} y {@code textures/item/chorizo.png}.
 */
public class ChorizoModel extends DefaultedItemGeoModel<ChorizoItem> {

    public ChorizoModel() {
        super(ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "chorizo"));
    }
}
