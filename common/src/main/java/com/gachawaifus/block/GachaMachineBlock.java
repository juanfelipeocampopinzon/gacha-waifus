package com.gachawaifus.block;

import com.gachawaifus.gacha.GachaService;
import com.gachawaifus.menu.GachaMenu;
import com.gachawaifus.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Máquina Gacha — el mueble del gacha: ya no sortea a ciegas, abre la pantalla del banner.
 *
 * <ul>
 *   <li><b>Bolita Rosa</b> en la mano → pestaña PROMOCIONAL (la destacada de la rotación diaria).</li>
 *   <li><b>Bolita Azul</b> en la mano → pestaña PERMANENTE (solo el pool estándar de 7).</li>
 *   <li><b>Mano vacía</b> → la misma pantalla, en la pestaña promocional.</li>
 *   <li><b>Shift + clic con bolita</b> → tirada x10 instantánea, sin abrir nada (atajo).</li>
 * </ul>
 *
 * <p>El sorteo sigue siendo uno solo ({@link GachaService}), compartido con los dos terminales: la
 * máquina no consume el ítem de la mano, son las bolitas del inventario las que se gastan dentro.
 *
 * <p>La máquina mira hacia el jugador al colocarla ({@code FACING} horizontal), igual que las camas.
 * La cúpula de cristal y el cartel superior sobresalen del cubo de 16, así que el bloque se registra
 * con {@code noOcclusion()}.
 */
public class GachaMachineBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GachaMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** Clic con una bolita: abre el banner de su color (o tira x10 si se agacha). */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack usedItem, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        GachaService.Banner banner;
        if (usedItem.is(ModItems.PINK_BALL.get())) {
            banner = GachaService.Banner.PROMO;
        } else if (usedItem.is(ModItems.BLUE_BALL.get())) {
            banner = GachaService.Banner.STANDARD;
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            // El cliente no sortea: el servidor abre la pantalla o hace la tirada.
            return ItemInteractionResult.CONSUME;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemInteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            GachaService.pull(serverPlayer, banner, 10);
        } else {
            GachaMenu.openAt(serverPlayer, banner, pos);
        }
        return ItemInteractionResult.CONSUME;
    }

    /** Clic con la mano vacía: abre el banner promocional (la cara del gacha). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        GachaMenu.openAt(serverPlayer, GachaService.Banner.PROMO, pos);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
