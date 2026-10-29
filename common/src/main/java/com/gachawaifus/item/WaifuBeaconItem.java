package com.gachawaifus.item;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.entity.WaifuRecallHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

/**
 * Baliza de Teletransporte — el mando que trae a las waifus a tu lado.
 *
 * <ul>
 *   <li><b>Clic:</b> trae a <i>todas</i> las waifus invocadas del jugador a su lado, aunque estén en
 *       otra dimensión.</li>
 *   <li><b>Clic sobre una de las suyas:</b> solo ella viene.</li>
 * </ul>
 *
 * <p>Desde la 6.5.0 el hogar ya no se fija aquí: es el {@code waifu_home_block}, un bloque que se
 * coloca y se convierte en la casa de todas tus waifus. Separar los dos objetos evita el accidente
 * de toda la vida: querer traerlas y acabar moviéndoles el punto de guardia.
 *
 * <p>El clic sobre una waifu está cubierto por las dos rutas a propósito: según cómo el cliente
 * resuelva el raycast, el clic puede llegar por {@code interactLivingEntity} o caer en {@code use()}
 * tras el {@code pick}, así que el ítem responde igual por los dos caminos.
 */
public class WaifuBeaconItem extends Item {

    /** Alcance del pick: lo que se apunta a mano, no lo que alcanza una tirada. */
    private static final double PICK_RANGE = 6.0D;

    public WaifuBeaconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof AbstractWaifuEntity waifu) || player.level().isClientSide
                || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        return recallOne(serverPlayer, waifu);
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
        // La ruta alternativa del clic sobre una waifu (ver el javadoc): si apunta a una, solo ella.
        if (serverPlayer.pick(PICK_RANGE, 0.0F, false) instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof AbstractWaifuEntity waifu) {
            return toHolder(recallOne(serverPlayer, waifu), stack);
        }
        int moved = WaifuRecallHandler.recallAll(serverPlayer);
        say(serverPlayer, moved > 0
                ? "§a[GachaWaifus] §f" + moved + " waifu(s) teletransportada(s) a tu lado."
                : "§7[GachaWaifus] §fNinguna de tus waifus invocadas está perdida.");
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.waifu_beacon.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    /** Trae a una sola waifu al lado del jugador. */
    private static InteractionResult recallOne(ServerPlayer player, AbstractWaifuEntity waifu) {
        if (!player.getUUID().equals(waifu.getOwnerUUID())) {
            say(player, "§c[GachaWaifus] §fEsa waifu no es tuya.");
            return InteractionResult.FAIL;
        }
        boolean moved = waifu.recallTo(player);
        WaifuRecallHandler.forget(waifu);
        say(player, moved
                ? "§a[GachaWaifus] §f" + waifu.getDisplayName().getString() + " §fse vino a tu lado."
                : "§c[GachaWaifus] §fNo hubo hueco libre junto a ti para "
                        + waifu.getDisplayName().getString() + ".");
        return moved ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    private static void say(ServerPlayer player, String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    private static InteractionResultHolder<ItemStack> toHolder(InteractionResult result, ItemStack stack) {
        return result == InteractionResult.FAIL
                ? InteractionResultHolder.fail(stack)
                : InteractionResultHolder.consume(stack);
    }
}
