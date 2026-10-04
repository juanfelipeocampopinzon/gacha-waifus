package com.gachawaifus.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class DormantWaifuCoreItem extends Item {

    public DormantWaifuCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Contiene la esencia durmiente de una Waifu caída."));
        tooltipComponents.add(Component.literal("§bRodea este núcleo con 4 Diamantes en la mesa de crafteo para revivirla."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
