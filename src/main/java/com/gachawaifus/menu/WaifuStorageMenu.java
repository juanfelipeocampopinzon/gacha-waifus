package com.gachawaifus.menu;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import com.gachawaifus.registry.ModMenuTypes;
import com.gachawaifus.team.TeamRules;
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
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Cofre virtual con las waifus guardadas, paginado de 27 en 27 para soportar colecciones grandes.
 *
 * Distribución del menú (rejilla de 3x9 + fila de navegación):
 *   - Filas 0-2: la página actual. La lista es continua: primero las guardadas vivas y después
 *     las caídas; el estado viaja en el nombre del item ([VIVA] / [CAÍDA]).
 *   - Fila 3:   botones ◀ ▶ y el rótulo de página (los botones llaman a {@link #clickMenuButton}).
 *   Clic en una viva = invocarla · clic en una caída = revivirla con 4 diamantes.
 */
public class WaifuStorageMenu extends AbstractContainerMenu {

    /** Ranuras de waifu por página (3 filas de 9). */
    private static final int PER_PAGE = 27;
    /** Filas visuales del texto: la rejilla (3) + la fila de navegación. */
    private static final int TOTAL_ROWS = 4;
    private static final int REVIVE_COST = 4;

    /** Ids de botón que envía la pantalla (viajan al servidor por serverAction). */
    public static final int PAGE_PREVIOUS = 0;
    public static final int PAGE_NEXT = 1;

    /** Marca de estado en el nombre del item display (viaja al cliente con el slot). */
    private static final String FALLEN_MARK = "[CAÍDA]";
    private static final String ALIVE_MARK = "[VIVA]";

    private final SimpleContainer container = new SimpleContainer(PER_PAGE);
    private final Inventory playerInventory;
    /** Por ranura de la página: {@code true} si esa waifu está caída (revivirla cuesta diamantes). */
    private final boolean[] fallenInSlot = new boolean[PER_PAGE];

    private int pageIndex = 0;
    private int pageCount = 1;
    private int storedCount = 0;
    private int fallenCount = 0;

    /** Sincroniza página actual / total / recuentos con el cliente para pintar la cabecera. */
    private final ContainerData pageInfo = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> pageIndex;
                case 1 -> pageCount;
                case 2 -> storedCount;
                case 3 -> fallenCount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> pageIndex = value;
                case 1 -> pageCount = value;
                case 2 -> storedCount = value;
                case 3 -> fallenCount = value;
                default -> { }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public WaifuStorageMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.WAIFU_STORAGE.get(), containerId);
        this.playerInventory = playerInventory;
        this.addDataSlots(this.pageInfo);

        for (int row = 0; row < 3; row++) {
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

    private void rebuildContainer() {
        this.container.clearContent();
        java.util.Arrays.fill(this.fallenInSlot, false);
        if (this.playerInventory.player.level().getServer() == null) {
            return; // cliente: las ranuras se llenan por sincronización
        }
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(this.playerInventory.player.level());
        java.util.UUID playerId = this.playerInventory.player.getUUID();

        java.util.List<ItemStack> pageSource = new java.util.ArrayList<>();
        java.util.List<Boolean> fallenFlags = new java.util.ArrayList<>();
        for (String id : storage.get(playerId)) {
            WaifuRoster.Entry entry = WaifuRoster.byId(id);
            if (entry != null) {
                pageSource.add(displayStack(entry, false));
                fallenFlags.add(false);
            }
        }
        for (String id : storage.getFallen(playerId)) {
            WaifuRoster.Entry entry = WaifuRoster.byId(id);
            if (entry != null) {
                pageSource.add(displayStack(entry, true));
                fallenFlags.add(true);
            }
        }

        this.storedCount = (int) fallenFlags.stream().filter(f -> !f).count();
        this.fallenCount = fallenFlags.size() - this.storedCount;
        this.pageCount = Math.max(1, (pageSource.size() + PER_PAGE - 1) / PER_PAGE);
        this.pageIndex = Math.min(Math.max(this.pageIndex, 0), this.pageCount - 1);

        int start = this.pageIndex * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < pageSource.size(); i++) {
            this.container.setItem(i, pageSource.get(start + i));
            this.fallenInSlot[i] = fallenFlags.get(start + i);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < PER_PAGE && clickType == ClickType.PICKUP) {
            if (!player.level().isClientSide && !this.container.getItem(slotId).isEmpty()) {
                if (this.fallenInSlot[slotId]) {
                    revive(slotId, player);
                } else {
                    summon(slotId, player);
                }
            }
            return; // jamás super.clicked: los items display no se pueden tomar
        }
        // Cualquier otra interacción (los slots del inventario del jugador, QUICK_MOVE, etc.) va al
        // comportamiento normal del contenedor: antes se tragaba todo y con la cápsula abierta no
        // se podía mover nada del inventario.
        super.clicked(slotId, button, clickType, player);
    }

    /**
     * Recibe los clics de los botones de página ( {@link #PAGE_PREVIOUS} / {@link #PAGE_NEXT} ),
     * que la pantalla manda con {@code handleInventoryButtonClick}. Solo corre en el servidor;
     * después de cambiar la página resincroniza la rejilla.
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        int target = buttonId == PAGE_NEXT ? this.pageIndex + 1 : this.pageIndex - 1;
        target = Math.min(Math.max(target, 0), this.pageCount - 1);
        if (target != this.pageIndex) {
            this.pageIndex = target;
            rebuildContainer();
            this.broadcastChanges();
        }
        return true;
    }

    /** Página mostrada, desde 1 (para el rótulo de la pantalla). */
    public int currentPage() {
        return this.pageIndex + 1;
    }

    public int totalPages() {
        return this.pageCount;
    }

    public int storedWaifus() {
        return this.storedCount;
    }

    public int fallenWaifus() {
        return this.fallenCount;
    }

    private void summon(int slotId, Player player) {
        WaifuRoster.Entry entry = WaifuRoster.byToken(this.container.getItem(slotId).getItem());
        if (entry == null) return;

        if (!TeamRules.canSummon(player)) {
            TeamRules.messageFull(player);
            return;
        }

        if (WaifuRoster.hasActive(player, entry)) {
            player.sendSystemMessage(Component.literal("§c[GachaWaifus] Ya tienes una " + entry.name() + " activa en el mundo."));
            return;
        }

        if (!spawnWaifu(entry, player, 1.0F)) {
            // Si la entidad no se ha podido crear, la waifu se queda en la cápsula (antes se
            // borraba la entrada igualmente y se perdía sin haber invocado nada).
            player.sendSystemMessage(Component.literal("§c[GachaWaifus] No se ha podido invocar a " + entry.name() + "."));
            return;
        }
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

        if (!TeamRules.canSummon(player)) {
            TeamRules.messageFull(player);
            return;
        }

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
        // Se invoca ANTES de cobrar: si la entidad no se puede crear, no se gastan los diamantes.
        if (!spawnWaifu(entry, player, 0.5F)) {
            player.sendSystemMessage(Component.literal("§c[GachaWaifus] No se ha podido revivir a " + entry.name() + "."));
            return;
        }
        consumeDiamonds(player, REVIVE_COST);
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

    /**
     * Crea y coloca la waifu 1.5 bloques frente al jugador, domesticada y con la vida indicada.
     *
     * @return {@code false} si la entidad no se ha podido crear (entonces quien llama NO debe
     *         gastar diamantes ni borrar la entrada de la cápsula).
     */
    private boolean spawnWaifu(WaifuRoster.Entry entry, Player player, float healthFraction) {
        AbstractWaifuEntity waifu = entry.type().get().create(player.level());
        if (waifu == null) return false;
        double x = player.getX() + (-Math.sin(Math.toRadians(player.getYRot())) * 1.5);
        double z = player.getZ() + (Math.cos(Math.toRadians(player.getYRot())) * 1.5);
        waifu.moveTo(x, player.getY(), z, player.getYRot(), 0.0F);
        waifu.tame(player);
        if (healthFraction < 1.0F) {
            waifu.setHealth(Math.max(1.0F, waifu.getMaxHealth() * healthFraction));
        }
        return player.level().addFreshEntity(waifu);
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
