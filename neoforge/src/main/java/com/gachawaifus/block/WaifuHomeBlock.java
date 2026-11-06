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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Bloque de Hogar — el ancla de las waifus. <b>Colocarlo</b> fija el hogar de todas las waifus
 * invocadas del que lo pone, y <b>clic derecho</b> sobre él lo vuelve a fijar (por si invocaste una
 * nueva después, o porque mover la casa es tan fácil como poner otro bloque).
 *
 * <p><b>Clic derecho con un bloque en la mano</b> lo hace imitar: {@link #useItemOn} intercepta el clic
 * antes de que el ítem coloque nada, y {@link #onRightClickBlock} hace lo mismo cuando vas agachado
 * (vanilla se salta el hook del bloque en ese caso). Solo se aceptan bloques con modelo estático:
 * cofres, hornos, carteles y demás BlockEntities no se imitan, para que el hogar nunca herede un
 * menú ni una animación que no le corresponde. El bloque puede aparentar el bloque elegido
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
     * Clic derecho con la mano vacía: re-fija el hogar. Agachado no hace nada especial; el
     * camuflaje se hace con un bloque en la mano (ver {@link #onRightClickBlock}).
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
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
     * <p>Cualquier otro ítem (o un bloque que no se puede imitar) devuelve {@code PASS} y el clic
     * hace lo de siempre. Agachado no se llega aquí: vanilla se salta el hook del bloque y pone el
     * bloque, así que esa variante la cubre {@link #onRightClickBlock}.
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
        if (level.isClientSide) {
            return WaifuHomeBlockEntity.isMimicable(mimic)
                    ? ItemInteractionResult.sidedSuccess(true)
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!home.setMimic(mimic)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        player.displayClientMessage(
                Component.translatable("gachawaifus.home_block.mimic", home.mimicName()), true);
        return ItemInteractionResult.CONSUME;
    }

    /**
     * La misma ruta que {@link #useItemOn}, pero para cuando el jugador va agachado: en 1.21.1
     * vanilla se salta el hook del bloque si hay un ítem en la mano, así que sin este evento el
     * bloque se colocaría encima del hogar en vez de camuflarlo.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isSecondaryUseActive()) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(event.getPos()) instanceof WaifuHomeBlockEntity home)
                || !(event.getItemStack().getItem() instanceof BlockItem blockItem)
                || !WaifuHomeBlockEntity.isMimicable(blockItem.getBlock())) {
            return;
        }
        if (!event.getLevel().isClientSide) {
            home.setMimic(blockItem.getBlock());
            event.getEntity().displayClientMessage(
                    Component.translatable("gachawaifus.home_block.mimic", home.mimicName()), true);
        }
        event.setCanceled(true);
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
