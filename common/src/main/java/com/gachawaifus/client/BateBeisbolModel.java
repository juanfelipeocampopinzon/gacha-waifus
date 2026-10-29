package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.BateBeisbolItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

/**
 * Modelo geo del bate: resuelve por defecto {@code geo/item/beisbol_bate.geo.json},
 * {@code animations/item/beisbol_bate.animation.json} y {@code textures/item/beisbol_bate.png}.
 */
public class BateBeisbolModel extends DefaultedItemGeoModel<BateBeisbolItem> {

    public BateBeisbolModel() {
        super(ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "beisbol_bate"));
    }
}
