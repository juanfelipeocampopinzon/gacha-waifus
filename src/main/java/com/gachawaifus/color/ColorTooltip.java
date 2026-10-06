package com.gachawaifus.color;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Las tres líneas de color que llevan todos los tokens: el color de la waifu, contra qué colores
 * pega más fuerte y contra cuáles pega menos. Un solo sitio para que los 11 tokens digan lo mismo.
 */
public final class ColorTooltip {

    private ColorTooltip() {
    }

    public static void append(List<Component> tooltip, WaifuColor color) {
        tooltip.add(Component.translatable("gachawaifus.color.label").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(color.styledName())
                .append(Component.literal(" " + color.hex()).withStyle(ChatFormatting.DARK_GRAY)));

        tooltip.add(Component.literal("▲ ").withStyle(ChatFormatting.GREEN)
                .append(Component.translatable("gachawaifus.color.strong").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(WaifuColor.names(color.strong()))
                .append(Component.literal(" §8(+33%)")));

        tooltip.add(Component.literal("▼ ").withStyle(ChatFormatting.RED)
                .append(Component.translatable("gachawaifus.color.weak").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(WaifuColor.names(color.weak()))
                .append(Component.literal(" §8(−33%)")));
    }
}
