package com.gachawaifus.item;

import com.gachawaifus.gacha.GachaService;
import com.gachawaifus.menu.GachaMenu;
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
 * Terminal Gacha — abre el banner <b>PROMOCIONAL</b> (Bolita Rosa, la destacada del día).
 *
 * <p>El sorteo ya no vive aquí: lo hace {@link GachaService}, compartido con el bloque Máquina
 * Gacha y con la pantalla. Este ítem solo decide <i>qué</i> abrir:
 * <ul>
 *   <li>clic normal → {@link GachaMenu} en la pestaña promocional;</li>
 *   <li>Shift + clic → tirada x10 instantánea por chat (el atajo de siempre).</li>
 * </ul>
 */
public class GachaTerminalItem extends Item {

    public GachaTerminalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown()) {
            GachaService.pull(serverPlayer, GachaService.Banner.PROMO, 10);
            return InteractionResultHolder.consume(stack);
        }
        GachaMenu.open(serverPlayer, GachaService.Banner.PROMO);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.gacha_terminal.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
