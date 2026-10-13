package com.gachawaifus.block;

import com.gachawaifus.gacha.GachaSavedData;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
 * Maquina Gacha — el bloque hace las tiradas:
 *
 * <ul>
 *   <li><b>Bolita Rosa</b> → banner PROMOCIONAL (la destacada de la rotación diaria, con 50/50 y
 *       garantizada): la lógica de {@link com.gachawaifus.item.GachaTerminalItem}.</li>
 *   <li><b>Bolita Azul</b> → banner PERMANENTE (solo el pool estándar, sin destacada):
 *       {@link com.gachawaifus.item.StandardTerminalItem}.</li>
 *   <li><b>Mano vacía</b> → resumen de la destacada de hoy y de la pity de los dos banners.</li>
 * </ul>
 *
 * <p>Se delega en los terminales en vez de copiar el sorteo: el gacha es una sola fuente de verdad
 * (regla del proyecto), y así pity, 50/50, garantizada, tirada doble y mensajes no se bifurcan. La
 * máquina no consume el item de la mano: los terminales descuentan sus bolitas. Con Shift + clic se
 * tira de 10 en 10, igual que con los terminales.
 *
 * <p>La máquina mira hacia el jugador al colocarla ({@code FACING} horizontal), igual que las camas
 * o las hornallas vanilla. La cúpula de cristal y el cartel superior sobresalen del cubo de 16, así
 * que el bloque se registra con {@code noOcclusion()}.
 */
public class GachaMachineBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GachaMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** Clic con una bolita: dispara el banner de su color. Cualquier otro item se ignora. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack usedItem, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        boolean pink = usedItem.is(ModItems.PINK_BALL.get());
        if (!pink && !usedItem.is(ModItems.BLUE_BALL.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            // El cliente no sortea: CONSUME hace que se reproduzca el gesto y el servidor manda
            // el resultado por chat.
            return ItemInteractionResult.CONSUME;
        }
        // Los terminales leen el nivel como ServerLevel y descuentan las bolitas del inventario.
        if (pink) {
            ModItems.GACHA_TERMINAL.get().use(level, player, hand);
        } else {
            ModItems.STANDARD_TERMINAL.get().use(level, player, hand);
        }
        return ItemInteractionResult.CONSUME;
    }

    /** Clic con la mano vacía: cartel con la destacada de hoy y la pity de cada banner. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        WaifuRoster.Entry featured = WaifuRoster.featured(serverLevel);
        GachaSavedData.PlayerState pity = GachaSavedData.get(level).state(player.getUUID());

        player.sendSystemMessage(Component.literal("§d§l─ Máquinas Gacha ─"));
        player.sendSystemMessage(Component.literal(
                "§7Promocional §d(rosa)§7: §f" + featured.name() + " §7· pity 5★ §b" + pity.pity
                        + (pity.guaranteed ? " §e· ¡próxima 5★ garantizada!" : "")
                        + " §7· pity 4★ §e" + pity.pity4 + "/32"));
        player.sendSystemMessage(Component.literal(
                "§7Permanente §b(azul)§7: pool estándar §7· pity §b" + pity.standardPity));
        player.sendSystemMessage(Component.literal(
                "§7Clic a la máquina con una bolita para tirar · §fShift + clic = x10"));
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
