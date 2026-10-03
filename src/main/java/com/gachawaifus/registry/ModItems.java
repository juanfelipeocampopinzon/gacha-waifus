package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.AstraYaoTokenItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, GachaWaifusMod.MODID);

    public static final DeferredHolder<Item, Item> ASTRA_YAO_TOKEN =
            ITEMS.register("astra_yao_token", () -> new AstraYaoTokenItem(new Item.Properties().stacksTo(16)));
}
