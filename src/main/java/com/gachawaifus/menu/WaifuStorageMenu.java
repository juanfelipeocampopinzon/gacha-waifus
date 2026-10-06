package com.gachawaifus.menu;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import com.gachawaifus.registry.ModMenuTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Cofre virtual con las waifus guardadas. Clic en una entrada = invocarla.
 * Ningún item puede entrar ni salir: es solo una vista de la colección server-side.
 *
 * Distribución del menú (5 filas de 9):
 *   - Fila 0-2: waifus guardadas y listas para invocar.
 *   - Fila 3:   separador decorativo (cristales).
 *   - Fila 4:   waifus CAÍDAS en combate — clic = revivirlas con 4 diamantes.
 */
public class WaifuStorageMenu extends AbstractContainerMenu {

    /** Filas de waifus guardadas vivas. */
    private static final int CAPTURED_ROWS = 3;
    /** Una fila de separación + una fila para las caídas. */
    private static final int TOTAL_ROWS = 5;
    private static final int FALLEN_ROW = 4;
    private static final int SIZE = TOTAL_ROWS * 9;
    private static final int FALLEN_START = FALLEN_ROW * 9;
    private static final int REVIVE_COST = 4;

    /** Marca de estado en el nombre del item display (viaja al cliente con el slot). */
    private static final String FALLEN_MARK = "[CAÍDA]";
    private static final String ALIVE_MARK = "[VIVA]";

    private final SimpleContainer container = new SimpleContainer(SIZE);
    private final Inventory playerInventory;

    public WaifuStorageMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.WAIFU_STORAGE.get(), containerId);
        this.playerInventory = playerInventory;

        for (int row = 0; row < TOTAL_ROWS; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }
        int invY = 18 + TOTAL_ROWS * 18 + 13;
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

    /** Item display de una waifu, con nombre que indica si está viva o caída. */
    private static ItemStack displayStack(WaifuRoster.Entry entry, boolean fallen) {
        ItemStack stack = new ItemStack(entry.token().get());
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(
                (fallen ? ChatFormatting.DARK_RED + FALLEN_MARK + " " : ChatFormatting.GREEN + ALIVE_MARK + " ")
                        + ChatFormatting.WHITE + entry.name()));
        return stack;
    }

    /** Separador decorativo de la fila intermedia (bloqueado para el jugador). */
    private static ItemStack separatorStack() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("§8· Waifus caídas ·"));
        return stack;
    }

    private void rebuildContainer() {
        this.container.clearContent();
        if (this.playerInventory.player.level().getServer() == null) {
            return; // cliente: las ranuras se llenan por sincronización
        }
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(this.playerInventory.player.level());
        java.util.UUID playerId = this.playerInventory.player.getUUID();

        int slot = 0;
        for (String id : storage.get(playerId)) {
            WaifuRoster.Entry entry = WaifuRoster.byId(id);
            if (entry != null && slot < CAPTURED_ROWS * 9) {
                this.container.setItem(slot++, displayStack(entry, false));
            }
        }

        int separator = CAPTURED_ROWS * 9;
        for (int i = separator; i < FALLEN_START; i++) {
            this.container.setItem(i, separatorStack());
        }

        int fallenSlot = FALLEN_START;
        for (String id : storage.getFallen(playerId)) {
            WaifuRoster.Entry entry = WaifuRoster.byId(id);
            if (entry != null && fallenSlot < SIZE) {
                this.container.setItem(fallenSlot++, displayStack(entry, true));
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < SIZE && clickType == ClickType.PICKUP) {
            ItemStack display = this.container.getItem(slotId);
            if (!display.isEmpty() && !isSeparator(slotId)) {
                if (!player.level().isClientSide) {
                    if (slotId >= FALLEN_START) {
                        revive(slotId, player);
                    } else {
                        summon(slotId, player);
                    }
                }
            }
            return; // jamás super.clicked: los items display no se pueden tomar
        }
        // resto de interacciones bloqueadas
    }

    private boolean isSeparator(int slotId) {
        return slotId >= CAPTURED_ROWS * 9 && slotId < FALLEN_START;
    }

    private void summon(int slotId, Player player) {
        WaifuRoster.Entry entry = WaifuRoster.byToken(this.container.getItem(slotId).getItem());
        if (entry == null) return;

        if (WaifuRoster.hasActive(player, entry)) {
            player.sendSystemMessage(Component.literal("§c[GachaWaifus] Ya tienes una " + entry.name() + " activa en el mundo."));
            return;
        }

        spawnWaifu(entry, player, 1.0F);
        WaifuStorageSavedData.get(player.level()).remove(player.getUUID(), entry.id());

        ServerLevel serverLevel = (ServerLevel) player.level();
        serverLevel.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.2, player.getZ(), 12, 0.4, 0.6, 0.4, 0.3);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.sendSystemMessage(Component.literal("§a[GachaWaifus] §f" + entry.name() + "§a invocada desde la cápsula."));
        rebuildContainer();
        this.broadcastChanges();
    }

    /**
     * Revivificación: consume 4 diamantes del inventario y devuelve a la waifu caída a la vida.
     */
    private void revive(int slotId, Player player) {
        WaifuRoster.Entry entry = WaifuRoster.byToken(this.container.getItem(slotId).getItem());
        if (entry == null) return;

        if (WaifuRoster.hasActive(player, entry)) {
            player.sendSystemMessage(Component.literal("§c[GachaWaifus] " + entry.name() + " ya está activa en el mundo."));
            return;
        }

        int diamonds = countDiamonds(player);
        if (diamonds < REVIVE_COST) {
            player.sendSystemMessage(Component.literal(
                    "§c[GachaWaifus] El ritual necesita §f" + REVIVE_COST + " diamantes§c (tienes " + diamonds + ")."));
            return;
        }
        consumeDiamonds(player, REVIVE_COST);

        spawnWaifu(entry, player, 0.5F);
        WaifuStorageSavedData.get(player.level()).revive(player.getUUID(), entry.id());

        ServerLevel serverLevel = (ServerLevel) player.level();
        serverLevel.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.5, 0.3, 0.0);
        serverLevel.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 14, 0.5, 0.6, 0.5, 0.25);
        serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.4, player.getZ(), 30, 0.5, 0.7, 0.5, 0.06);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.0F);

        player.sendSystemMessage(Component.literal("§d[GachaWaifus] §f" + entry.name() + "§d ha vuelto a la vida."));
        rebuildContainer();
        this.broadcastChanges();
    }

    /** Crea y coloca la waifu 1.5 bloques frente al jugador, domesticada y con la vida indicada. */
    private void spawnWaifu(WaifuRoster.Entry entry, Player player, float healthFraction) {
        AbstractWaifuEntity waifu = entry.type().get().create(player.level());
        if (waifu == null) return;
        double x = player.getX() + (-Math.sin(Math.toRadians(player.getYRot())) * 1.5);
        double z = player.getZ() + (Math.cos(Math.toRadians(player.getYRot())) * 1.5);
        waifu.moveTo(x, player.getY(), z, player.getYRot(), 0.0F);
        waifu.tame(player);
        if (healthFraction < 1.0F) {
            waifu.setHealth(Math.max(1.0F, waifu.getMaxHealth() * healthFraction));
        }
        player.level().addFreshEntity(waifu);
    }

    private int countDiamonds(Player player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.DIAMOND)) count += s.getCount();
        }
        return count;
    }

    private void consumeDiamonds(Player player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.DIAMOND)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
