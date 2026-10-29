package com.gachawaifus.item;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.entity.WaifuRecallHandler;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Baliza de Hogar — el mando a distancia de las waifus: un solo ítem para traerlas y para fijar
 * dónde tienen que quedarse.
 *
 * <ul>
 *   <li><b>Clic:</b> trae a <i>todas</i> las waifus invocadas del jugador a su lado, aunque estén en
 *       otra dimensión.</li>
 *   <li><b>Clic sobre una de las suyas:</b> solo ella viene.</li>
 *   <li><b>Shift + clic sobre una de las suyas:</b> le fija el hogar en el bloque que se está mirando.</li>
 *   <li><b>Shift + clic en un bloque:</b> hogar de <i>todas</i> las suyas en ese bloque.</li>
 *   <li><b>Shift + clic al aire:</b> hogar a los pies del jugador.</li>
 * </ul>
 *
 * <p>El hogar solo se nota si la waifu se pone en modo {@code EN_CASA} (Shift + clic con la mano
 * vacía sobre ella): sin ese modo el hogar es solo el punto de referencia del auto-recuerdo. Con el
 * modo puesto, ella se queda de guardia alrededor de ese bloque.
 *
 * <p>El shift + clic sobre una waifu está cubierto por las dos rutas a propósito: según cómo el
 * cliente resuelva el raycast, un clic agachado llega por {@code interactLivingEntity} o cae en
 * {@code use()} tras el {@code pick}, así que el ítem responde igual por los dos caminos.
 */
public class WaifuBeaconItem extends Item {

    /** Alcance del pick del shift + clic: lo que se apunta a mano, no lo que alcanza una tirada. */
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
        return serverPlayer.isShiftKeyDown()
                ? setHomeOne(serverPlayer, waifu)
                : recallOne(serverPlayer, waifu);
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
        if (!serverPlayer.isShiftKeyDown()) {
            int moved = WaifuRecallHandler.recallAll(serverPlayer);
            say(serverPlayer, moved > 0
                    ? "§a[GachaWaifus] §f" + moved + " waifu(s) teletransportada(s) a tu lado."
                    : "§7[GachaWaifus] §fNinguna de tus waifus invocadas está perdida.");
            return InteractionResultHolder.consume(stack);
        }

        // Shift + clic: lo que se esté mirando decide si el hogar es de una o de todas.
        HitResult hit = serverPlayer.pick(PICK_RANGE, 0.0F, false);
        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof AbstractWaifuEntity waifu) {
            return toHolder(setHomeOne(serverPlayer, waifu), stack);
        }
        setHomeAll(serverPlayer, hit instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos().above()       // el hogar es el hueco donde ella se queda de pie
                : serverPlayer.blockPosition());
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.waifu_beacon.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // ---------------------------------------------------------------------------- acciones

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

    /** Fija el hogar de una waifu concreta en el bloque apuntado, o en el suyo si no hay bloque. */
    private static InteractionResult setHomeOne(ServerPlayer player, AbstractWaifuEntity waifu) {
        if (!player.getUUID().equals(waifu.getOwnerUUID())) {
            say(player, "§c[GachaWaifus] §fEsa waifu no es tuya.");
            return InteractionResult.FAIL;
        }
        HitResult hit = player.pick(PICK_RANGE, 0.0F, false);
        BlockPos pos = hit instanceof BlockHitResult blockHit
                ? blockHit.getBlockPos().above()
                : waifu.blockPosition();
        waifu.setHome(pos, player.level().dimension());
        WaifuRecallHandler.forget(waifu);
        say(player, "§2[GachaWaifus] §fHogar de " + waifu.getDisplayName().getString()
                + " §2en " + describe(player, pos) + ". §7Ponla en modo §2En casa §7para que lo custodie.");
        return InteractionResult.CONSUME;
    }

    /** Fija el hogar de todas las waifus invocadas del jugador en un bloque. */
    private static void setHomeAll(ServerPlayer player, BlockPos pos) {
        int set = WaifuRecallHandler.setHome(player, pos, null);
        say(player, set > 0
                ? "§2[GachaWaifus] §fHogar de " + set + " waifu(s) en " + describe(player, pos)
                        + ". §7Ponlas en modo §2En casa §7para que se queden ahí."
                : "§7[GachaWaifus] §fNo tienes waifus invocadas a las que fijar el hogar.");
    }

    /** Coordenadas + dimensión, para que el jugador sepa dónde quedó el hogar. */
    private static String describe(ServerPlayer player, BlockPos pos) {
        return pos.toShortString() + " §7(" + player.level().dimension().location().getPath() + ")";
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
