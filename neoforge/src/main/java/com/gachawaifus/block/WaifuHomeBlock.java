package com.gachawaifus.block;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.entity.WaifuRecallHandler;
import com.gachawaifus.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Bloque de Hogar — el ancla de las waifus. <b>Colocarlo</b> fija el hogar de todas las waifus
 * invocadas del que lo pone, y <b>clic derecho</b> sobre él lo vuelve a fijar (por si invocaste una
 * nueva después, o porque mover la casa es tan fácil como poner otro bloque).
 *
 * <p><b>Clic derecho con un bloque en la mano</b> lo hace imitar: {@link #useItemOn} intercepta el clic
 * antes de que el ítem coloque nada. Es la ruta normal para disfrazar la casa de madera, de vidrio o de
 * lo que haya en el inventario.
 *
 * <p><b>Shift + clic derecho con la mano vacía</b> abre el selector de textura, para cuando no se tiene
 * el bloque a mano. El bloque puede aparentar cualquier otro bloque del mundo
 * ({@link WaifuHomeBlockEntity} guarda cuál, y {@code WaifuHomeBlockRenderer} lo dibuja). Por eso
 * extiende {@link BaseEntityBlock} en vez de {@link Block} — su {@link RenderShape} es
 * {@code INVISIBLE} y quien pinta el cubo es el BlockEntityRenderer.
 *
 * <p>No tiene dueño guardado: el hogar vive en el NBT de cada waifu
 * ({@link AbstractWaifuEntity#setHome}), así que el bloque es solo el <i>momento</i> en que se
 * decide el punto. Romperlo, en cambio, sí limpia los hogares que apuntaban a él: sin eso una waifu
 * en modo {@code EN_CASA} se quedaría de guardia de por vida frente al aire.
 *
 * <p>La Baliza ({@code waifu_beacon}) es lo contrario: solo las trae a tu lado. Hogar y
 * teletransporte son dos acciones distintas y cada una tiene su propio objeto.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public class WaifuHomeBlock extends BaseEntityBlock {

    public static final MapCodec<WaifuHomeBlock> CODEC = simpleCodec(WaifuHomeBlock::new);

    public WaifuHomeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WaifuHomeBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaifuHomeBlockEntity(pos, state);
    }

    /**
     * Clic derecho: re-fija el hogar. Shift + clic derecho: abre el selector de textura. Se separan
     * con {@code isSecondaryUseActive()} (mano vacía agachada) porque con un ítem en la mano agachado
     * lo que se pone es bloque, no menú.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {
            if (!(level.getBlockEntity(pos) instanceof WaifuHomeBlockEntity home)) {
                return InteractionResult.FAIL;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(home, home::writeToMenuBuf);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        assign(serverPlayer, pos);
        return InteractionResult.CONSUME;
    }

    /**
     * Clic derecho <b>con un bloque en la mano</b>: el hogar pasa a imitar su textura. Es la ruta
     * rápida, y manda sobre colocar el bloque: el servidor consulta este hook antes de usar el ítem,
     * así que devolver un resultado que consume la acción deja la colocación en nada.
     *
     * <p>Cualquier otro ítem devuelve {@code PASS} y el clic hace lo de siempre (un token invoca, una
     * espada ataca). Agachado no se llega aquí: vanilla se salta el hook del bloque y pone el bloque,
     * que es justo lo que uno quiere cuando va agachado construyendo.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (!(heldItem.getItem() instanceof BlockItem blockItem)
                || !(level.getBlockEntity(pos) instanceof WaifuHomeBlockEntity home)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Block mimic = blockItem.getBlock();
        if (mimic == this || level.isClientSide) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        home.setMimic(mimic);
        player.displayClientMessage(
                Component.translatable("gachawaifus.home_block.mimic", home.mimicName()), true);
        return ItemInteractionResult.CONSUME;
    }

    /** Se coloca y ya es el hogar: no hay que darle ningún otro uso. */
    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)
                || !(event.getEntity() instanceof ServerPlayer player)
                || !event.getPlacedBlock().is(ModBlocks.WAIFU_HOME_BLOCK.get())) {
            return;
        }
        assign(player, event.getPos());
    }

    /** Al romperlo, las waifus que apuntaban aquí se quedan sin hogar (y sin guardarlo a la nada). */
    @SubscribeEvent
    public static void onBroken(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos pos = event.getPos();
        for (ServerPlayer owner : serverLevel.getServer().getPlayerList().getPlayers()) {
            int cleared = 0;
            for (AbstractWaifuEntity waifu : WaifuRecallHandler.waifusOf(serverLevel.getServer(),
                    owner.getUUID())) {
                if (pos.equals(waifu.homePos()) && serverLevel.dimension().equals(waifu.homeDimension())) {
                    waifu.setHome(null, null);
                    WaifuRecallHandler.forget(waifu);
                    cleared++;
                }
            }
            if (cleared > 0) {
                owner.displayClientMessage(
                        Component.translatable("gachawaifus.home_block.broken", cleared), true);
            }
        }
    }

    /** Fija el hogar de todas las waifus invocadas del jugador en el bloque. */
    private static void assign(ServerPlayer player, BlockPos pos) {
        int set = WaifuRecallHandler.setHome(player, pos, null);
        player.displayClientMessage(Component.translatable(set > 0
                ? "gachawaifus.home_block.set" : "gachawaifus.home_block.none"), true);
        if (set > 0) {
            player.displayClientMessage(Component.translatable("gachawaifus.home_block.hint"), true);
        }
    }
}
