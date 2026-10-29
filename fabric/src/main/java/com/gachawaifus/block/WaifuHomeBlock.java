package com.gachawaifus.block;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.entity.WaifuRecallHandler;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {
            if (!(level.getBlockEntity(pos) instanceof WaifuHomeBlockEntity home)) {
                return InteractionResult.FAIL;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(home);
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

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            assign(player, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel) {
            for (ServerPlayer owner : serverLevel.getServer().getPlayerList().getPlayers()) {
                int cleared = 0;
                for (AbstractWaifuEntity waifu : WaifuRecallHandler.waifusOf(serverLevel.getServer(), owner.getUUID())) {
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
        super.onRemove(state, level, pos, newState, isMoving);
    }

    private static void assign(ServerPlayer player, BlockPos pos) {
        int set = WaifuRecallHandler.setHome(player, pos, null);
        player.displayClientMessage(Component.translatable(set > 0
                ? "gachawaifus.home_block.set" : "gachawaifus.home_block.none"), true);
        if (set > 0) {
            player.displayClientMessage(Component.translatable("gachawaifus.home_block.hint"), true);
        }
    }
}
