package com.gachawaifus.menu;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import com.gachawaifus.registry.ModMenuTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Cofre virtual con las waifus guardadas. Clic en una entrada = invocarla.
 * Ningún item puede entrar ni salir: es solo una vista de la colección server-side.
 */
public class WaifuStorageMenu extends AbstractContainerMenu {

    private static final int ROWS = 3;
    private final SimpleContainer container = new SimpleContainer(ROWS * 9);
    private final Inventory playerInventory;

    public WaifuStorageMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.WAIFU_STORAGE.get(), containerId);
        this.playerInventory = playerInventory;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        int invY = 18 + ROWS * 18 + 12;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        int hotbarY = invY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }

        rebuildContainer();
    }

    private void rebuildContainer() {
        this.container.clearContent();
        if (this.playerInventory.player.level().getServer() == null) {
            return; // cliente: las ranuras se llenan por sincronización
        }
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(this.playerInventory.player.level());
        int slot = 0;
        for (String id : storage.get(this.playerInventory.player.getUUID())) {
            WaifuRoster.Entry entry = WaifuRoster.byId(id);
            if (entry != null && slot < this.container.getContainerSize()) {
                this.container.setItem(slot++, new ItemStack(entry.token().get()));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < ROWS * 9 && clickType == ClickType.PICKUP) {
            if (!player.level().isClientSide && !this.container.getItem(slotId).isEmpty()) {
                summon(slotId, player);
            }
            return; // jamás super.clicked: los items display no se pueden tomar
        }
        // resto de interacciones bloqueadas
    }

    private void summon(int slotId, Player player) {
        WaifuRoster.Entry entry = WaifuRoster.byToken(this.container.getItem(slotId).getItem());
        if (entry == null) return;

        if (WaifuRoster.hasActive(player, entry)) {
            player.sendSystemMessage(Component.literal("§c[GachaWaifus] Ya tienes una " + entry.name() + " activa en el mundo."));
            return;
        }

        AbstractWaifuEntity waifu = entry.type().get().create(player.level());
        if (waifu == null) return;
        double x = player.getX() + (-Math.sin(Math.toRadians(player.getYRot())) * 1.5);
        double z = player.getZ() + (Math.cos(Math.toRadians(player.getYRot())) * 1.5);
        waifu.moveTo(x, player.getY(), z, player.getYRot(), 0.0F);
        waifu.tame(player);
        player.level().addFreshEntity(waifu);

        WaifuStorageSavedData.get(player.level()).remove(player.getUUID(), entry.id());

        ServerLevel serverLevel = (ServerLevel) player.level();
        serverLevel.sendParticles(ParticleTypes.FLASH, x, player.getY() + 1.0, z, 2, 0.2, 0.4, 0.2, 0.0);
        serverLevel.sendParticles(ParticleTypes.NOTE, x, player.getY() + 1.2, z, 12, 0.4, 0.6, 0.4, 0.3);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.sendSystemMessage(Component.literal("§a[GachaWaifus] §f" + entry.name() + "§a invocada desde la cápsula."));
        rebuildContainer();
        this.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
