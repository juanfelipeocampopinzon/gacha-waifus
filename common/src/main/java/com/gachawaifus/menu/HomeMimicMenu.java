package com.gachawaifus.menu;

import com.gachawaifus.block.WaifuHomeBlockEntity;
import com.gachawaifus.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * El selector de textura del Bloque de Hogar: shift + clic derecho (mano vacía) sobre el bloque.
 *
 * <p>No tiene huecos — se le pueden pasar cosas por encima, pero no hay nada que meter. Toda la
 * interfaz es una rejilla de iconos, y cada clic es un {@code clickMenuButton} con el índice del
 * bloque elegido.
 *
 * <p>Ese índice es el motivo de las listas compartidas de {@link #choices()}: el cliente solo manda
 * un entero, así que las dos copias del menú tienen que construir la misma lista. Se ordena por
 * nombre de registro (no por el orden interno del registro, que depende de cuándo arrancó cada mod),
 * así que la lista es idéntica en ambos lados pase lo que pase con el orden de carga.
 */
public class HomeMimicMenu extends AbstractContainerMenu {

    /** La lista de bloques, calculada una vez por lado. */
    private static List<Block> cachedChoices;

    /** La imitación actual, para el resaltado de la pantalla: la del servidor al abrir, luego lo
     *  que el cliente pulsó (el servidor ya hizo el cambio, no hace falta que lo vuelva a contar). */
    private String currentMimic;

    /** dónde está el bloque; solo lo usa el servidor para resolver el BlockEntity */
    private final Level level;
    private final BlockPos pos;

    /** Copia del servidor (la que construye el BlockEntity como {@code MenuProvider}). */
    public HomeMimicMenu(int containerId, Inventory inventory, Level level, BlockPos pos) {
        super(ModMenuTypes.HOME_MIMIC.get(), containerId);
        this.level = level;
        this.pos = pos;
        this.currentMimic = mimicIdOf(level.getBlockEntity(pos));
    }

    /** Copia del cliente: lee la posición y la imitación actual de los datos extra. */
    public HomeMimicMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        super(ModMenuTypes.HOME_MIMIC.get(), containerId);
        this.level = inventory.player.level();
        this.pos = extraData.readBlockPos();
        this.currentMimic = extraData.readUtf(128);
    }

    /**
     * Los bloques que se pueden imitar, en el orden que ve el jugador: todos los que tengan ítem
     * (así se caen solos el aire, los cabezales de piston y los estados técnicos) ordenados por id de
     * registro.
     */
    public static List<Block> choices() {
        if (cachedChoices == null) {
            List<Block> list = new ArrayList<>();
            BuiltInRegistries.BLOCK.forEach(block -> {
                if (block != Blocks.AIR && block.asItem() != Items.AIR) {
                    list.add(block);
                }
            });
            list.sort(Comparator.comparing(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
            cachedChoices = List.copyOf(list);
        }
        return cachedChoices;
    }

    /** El id de lo que se está imitando — siempre hay algo, la por defecto también es un bloque. */
    private static String mimicIdOf(@Nullable BlockEntity blockEntity) {
        return blockEntity instanceof WaifuHomeBlockEntity home
                ? BuiltInRegistries.BLOCK.getKey(home.mimicState().getBlock()).toString()
                : "";
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        List<Block> choices = choices();
        if (index < 0 || index >= choices.size() || !(this.level.getBlockEntity(this.pos)
                instanceof WaifuHomeBlockEntity home)) {
            return false;
        }
        home.setMimic(choices.get(index));
        player.displayClientMessage(Component.translatable("gachawaifus.mimic_screen.set",
                home.mimicName()), true);
        return true;
    }

    /** Id del bloque que hay que venir resaltado en la rejilla. */
    public String currentMimic() {
        return this.currentMimic;
    }

    /** El cliente apunta su propio resaltado al mandar el clic. */
    public void setDisplayedMimic(String id) {
        this.currentMimic = id;
    }

    @Override
    public boolean stillValid(Player player) {
        // El panel se cierra si el bloque dejó de ser un hogar (lo rompieron mientras mirabas).
        return this.level.isLoaded(this.pos)
                && this.level.getBlockEntity(this.pos) instanceof WaifuHomeBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // sin huecos que mover
    }
}
