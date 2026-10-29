package com.gachawaifus.item;

import com.gachawaifus.menu.GuideMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Guía del Invocador — el manual del mod en una pantalla de libro.
 *
 * <p>No hay que encontrarla: se entrega sola la primera vez que se entra a un mundo
 * ({@code GuideGiver}) y se craftea con madera, un libro y papel. Un clic la abre; {@code /guia} la
 * vuelve a abrir si se perdió. El contenido lo escribe {@code client/GuideScreen}, así que añadir
 * páginas no toca este ítem.
 */
public class WaifuGuideItem extends Item {

    public WaifuGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            GuideMenu.open(serverPlayer);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.waifu_guide.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
