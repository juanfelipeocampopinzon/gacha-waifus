package com.gachawaifus.menu;

import com.gachawaifus.registry.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menú de la Guía del Invocador: no tiene huecos ni datos que sincronizar.
 *
 * <p>El contenido de las páginas vive escrito en el cliente ({@code client/GuideScreen}), así que el
 * menú cumple un solo papel: poder abrir una pantalla desde el servidor (en multijugador un
 * {@code Screen} no se abre a la fuerza desde el ítem, que corre en el lado lógico del jugador) y
 * que la guía funcione igual en un servidor dedicado. La página que se lee es local: no hay nada que
 * el servidor necesite saber.
 */
public class GuideMenu extends AbstractContainerMenu {

    public GuideMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.WAIFU_GUIDE.get(), containerId);
    }

    /** Abre la guía. La página se recuerda en el cliente entre aperturas. */
    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, serverPlayer) -> new GuideMenu(id, inventory),
                Component.translatable("gachawaifus.guide.title")));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // sin huecos: nada que mover
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
