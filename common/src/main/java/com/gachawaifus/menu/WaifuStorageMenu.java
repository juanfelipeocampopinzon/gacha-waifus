package com.gachawaifus.menu;

import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import com.gachawaifus.registry.ModMenuTypes;
import com.gachawaifus.team.TeamRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Menú de la Cápsula Waifu & Glosario de Colección.
 *
 * <p>Sincroniza con el cliente mediante {@link ContainerData} (17 enteros):
 * <ul>
 *   <li>Pestaña activa (Cápsula vs. Glosario).</li>
 *   <li>Contadores: waifus almacenadas vivas, caídas, conseguidas en total y diamantes del jugador.</li>
 *   <li>Máscaras de bits (3 x 32 bits = hasta 96 waifus) para:
 *       poseídas en el glosario, guardadas vivas, caídas en combate y activas en el mundo/inventario.</li>
 * </ul>
 *
 * <p>Las acciones (cambio de pestaña, invocación o resurrección) viajan limpiamente
 * por {@link #clickMenuButton(Player, int)} sin necesidad de paquetes de red personalizados.
 */
public class WaifuStorageMenu extends AbstractContainerMenu {

    public static final int REVIVE_COST = 4;

    /* ── Pestañas ─────────────────────────────────────────────────────────────────────────── */
    public static final int TAB_CAPSULE = 0;
    public static final int TAB_GLOSSARY = 1;

    /* ── Botones que envía el cliente por handleInventoryButtonClick ─────────────────────── */
    public static final int BUTTON_TAB_CAPSULE = 0;
    public static final int BUTTON_TAB_GLOSSARY = 1;
    public static final int BUTTON_SUMMON_BASE = 100; // 100 + rotationIndex
    public static final int BUTTON_REVIVE_BASE = 300; // 300 + rotationIndex

    /* ── Índices de ContainerData ──────────────────────────────────────────────────────────── */
    public static final int DATA_TAB = 0;
    public static final int DATA_STORED_COUNT = 1;
    public static final int DATA_FALLEN_COUNT = 2;
    public static final int DATA_OWNED_COUNT = 3;
    public static final int DATA_DIAMONDS = 4;
    public static final int DATA_OWNED_0 = 5;
    public static final int DATA_OWNED_1 = 6;
    public static final int DATA_OWNED_2 = 7;
    public static final int DATA_STORED_0 = 8;
    public static final int DATA_STORED_1 = 9;
    public static final int DATA_STORED_2 = 10;
    public static final int DATA_FALLEN_0 = 11;
    public static final int DATA_FALLEN_1 = 12;
    public static final int DATA_FALLEN_2 = 13;
    public static final int DATA_ACTIVE_0 = 14;
    public static final int DATA_ACTIVE_1 = 15;
    public static final int DATA_ACTIVE_2 = 16;
    public static final int DATA_COUNT = 17;

    private final Player player;
    private final int[] data = new int[DATA_COUNT];

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return (index >= 0 && index < DATA_COUNT) ? data[index] : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index >= 0 && index < DATA_COUNT) {
                data[index] = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private int refreshTimer = 0;

    public WaifuStorageMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.WAIFU_STORAGE.get(), containerId);
        this.player = playerInventory.player;
        this.addDataSlots(this.dataAccess);

        if (!this.player.level().isClientSide && this.player instanceof ServerPlayer serverPlayer) {
            refreshAll(serverPlayer);
        }
    }

    /* ── Sincronización Server-Side ────────────────────────────────────────────────────────── */

    @Override
    public void broadcastChanges() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            if (++this.refreshTimer >= 10) {
                this.refreshTimer = 0;
                refreshAll(serverPlayer);
            }
        }
        super.broadcastChanges();
    }

    /**
     * Recalcula el estado completo de la colección y de la cápsula para sincronizar con el cliente.
     */
    public void refreshAll(ServerPlayer serverPlayer) {
        UUID playerId = serverPlayer.getUUID();
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(serverPlayer.level());
        List<String> storedList = storage.get(playerId);
        List<String> fallenList = storage.getFallen(playerId);
        Set<String> ownedSet = WaifuRoster.ownedIds(serverPlayer);

        int[] ownedMasks = new int[3];
        int[] storedMasks = new int[3];
        int[] fallenMasks = new int[3];
        int[] activeMasks = new int[3];

        int stored = 0;
        int fallen = 0;
        int owned = 0;

        List<WaifuRoster.Entry> rotation = WaifuRoster.ROTATION;
        for (int i = 0; i < rotation.size(); i++) {
            WaifuRoster.Entry entry = rotation.get(i);
            String id = entry.id();
            boolean isStored = storedList.contains(id);
            boolean isFallen = fallenList.contains(id);
            boolean isOwned = ownedSet.contains(id);
            boolean isActive = WaifuRoster.hasActive(serverPlayer, entry);

            if (isStored) {
                setBit(storedMasks, i);
                stored++;
            }
            if (isFallen) {
                setBit(fallenMasks, i);
                fallen++;
            }
            if (isOwned) {
                setBit(ownedMasks, i);
                owned++;
            }
            if (isActive) {
                setBit(activeMasks, i);
            }
        }

        this.data[DATA_STORED_COUNT] = stored;
        this.data[DATA_FALLEN_COUNT] = fallen;
        this.data[DATA_OWNED_COUNT] = owned;
        this.data[DATA_DIAMONDS] = countDiamonds(serverPlayer);

        this.data[DATA_OWNED_0] = ownedMasks[0];
        this.data[DATA_OWNED_1] = ownedMasks[1];
        this.data[DATA_OWNED_2] = ownedMasks[2];

        this.data[DATA_STORED_0] = storedMasks[0];
        this.data[DATA_STORED_1] = storedMasks[1];
        this.data[DATA_STORED_2] = storedMasks[2];

        this.data[DATA_FALLEN_0] = fallenMasks[0];
        this.data[DATA_FALLEN_1] = fallenMasks[1];
        this.data[DATA_FALLEN_2] = fallenMasks[2];

        this.data[DATA_ACTIVE_0] = activeMasks[0];
        this.data[DATA_ACTIVE_1] = activeMasks[1];
        this.data[DATA_ACTIVE_2] = activeMasks[2];
    }

    /* ── Acciones de Botón ────────────────────────────────────────────────────────────────── */

    @Override
    public boolean clickMenuButton(Player p, int buttonId) {
        if (p.level().isClientSide) {
            return true;
        }

        if (buttonId == BUTTON_TAB_CAPSULE) {
            this.data[DATA_TAB] = TAB_CAPSULE;
            return true;
        }
        if (buttonId == BUTTON_TAB_GLOSSARY) {
            this.data[DATA_TAB] = TAB_GLOSSARY;
            return true;
        }

        List<WaifuRoster.Entry> rotation = WaifuRoster.ROTATION;

        // Invocación desde la cápsula
        if (buttonId >= BUTTON_SUMMON_BASE && buttonId < BUTTON_SUMMON_BASE + rotation.size()) {
            int index = buttonId - BUTTON_SUMMON_BASE;
            summonEntry(rotation.get(index), p);
            return true;
        }

        // Resurrección con diamantes
        if (buttonId >= BUTTON_REVIVE_BASE && buttonId < BUTTON_REVIVE_BASE + rotation.size()) {
            int index = buttonId - BUTTON_REVIVE_BASE;
            reviveEntry(rotation.get(index), p);
            return true;
        }

        return false;
    }

    private void summonEntry(WaifuRoster.Entry entry, Player p) {
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(p.level());
        if (!storage.contains(p.getUUID(), entry.id())) {
            p.sendSystemMessage(Component.literal("§c[GachaWaifus] " + entry.name() + " no está en tu cápsula."));
            return;
        }

        if (!TeamRules.canSummon(p)) {
            TeamRules.messageFull(p);
            return;
        }

        if (WaifuRoster.hasActive(p, entry)) {
            p.sendSystemMessage(Component.literal("§c[GachaWaifus] Ya tienes una " + entry.name() + " activa en el mundo."));
            return;
        }

        if (!spawnWaifu(entry, p, 1.0F)) {
            p.sendSystemMessage(Component.literal("§c[GachaWaifus] No se ha podido invocar a " + entry.name() + "."));
            return;
        }

        storage.remove(p.getUUID(), entry.id());

        ServerLevel serverLevel = (ServerLevel) p.level();
        serverLevel.sendParticles(ParticleTypes.NOTE, p.getX(), p.getY() + 1.2, p.getZ(), 12, 0.4, 0.6, 0.4, 0.3);
        serverLevel.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

        p.sendSystemMessage(Component.literal("§a[GachaWaifus] §f" + entry.name() + "§a invocada desde la cápsula."));

        if (p instanceof ServerPlayer sp) {
            refreshAll(sp);
            this.broadcastChanges();
        }
    }

    private void reviveEntry(WaifuRoster.Entry entry, Player p) {
        WaifuStorageSavedData storage = WaifuStorageSavedData.get(p.level());
        if (!storage.getFallen(p.getUUID()).contains(entry.id())) {
            p.sendSystemMessage(Component.literal("§c[GachaWaifus] " + entry.name() + " no está caída en combate."));
            return;
        }

        if (!TeamRules.canSummon(p)) {
            TeamRules.messageFull(p);
            return;
        }

        if (WaifuRoster.hasActive(p, entry)) {
            p.sendSystemMessage(Component.literal("§c[GachaWaifus] " + entry.name() + " ya está activa en el mundo."));
            return;
        }

        int diamonds = countDiamonds(p);
        if (diamonds < REVIVE_COST) {
            p.sendSystemMessage(Component.literal(
                    "§c[GachaWaifus] El ritual necesita §f" + REVIVE_COST + " diamantes§c (tienes " + diamonds + ")."));
            return;
        }

        if (!spawnWaifu(entry, p, 0.5F)) {
            p.sendSystemMessage(Component.literal("§c[GachaWaifus] No se ha podido revivir a " + entry.name() + "."));
            return;
        }

        consumeDiamonds(p, REVIVE_COST);
        storage.revive(p.getUUID(), entry.id());

        ServerLevel serverLevel = (ServerLevel) p.level();
        serverLevel.sendParticles(ParticleTypes.FLASH, p.getX(), p.getY() + 1.0, p.getZ(), 3, 0.3, 0.5, 0.3, 0.0);
        serverLevel.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 1.2, p.getZ(), 14, 0.5, 0.6, 0.5, 0.25);
        serverLevel.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.4, p.getZ(), 30, 0.5, 0.7, 0.5, 0.06);
        serverLevel.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        serverLevel.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.0F);

        p.sendSystemMessage(Component.literal("§d[GachaWaifus] §f" + entry.name() + "§d ha vuelto a la vida."));

        if (p instanceof ServerPlayer sp) {
            refreshAll(sp);
            this.broadcastChanges();
        }
    }

    private boolean spawnWaifu(WaifuRoster.Entry entry, Player p, float healthFraction) {
        AbstractWaifuEntity waifu = entry.type().get().create(p.level());
        if (waifu == null) return false;
        double x = p.getX() + (-Math.sin(Math.toRadians(p.getYRot())) * 1.5);
        double z = p.getZ() + (Math.cos(Math.toRadians(p.getYRot())) * 1.5);
        waifu.moveTo(x, p.getY(), z, p.getYRot(), 0.0F);
        waifu.tame(p);
        if (healthFraction < 1.0F) {
            waifu.setHealth(Math.max(1.0F, waifu.getMaxHealth() * healthFraction));
        }
        return p.level().addFreshEntity(waifu);
    }

    private int countDiamonds(Player p) {
        int count = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(Items.DIAMOND)) count += s.getCount();
        }
        return count;
    }

    private void consumeDiamonds(Player p, int amount) {
        int remaining = amount;
        for (int i = 0; i < p.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.is(Items.DIAMOND)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
    }

    /* ── Lectores de Estado para el Cliente ────────────────────────────────────────────────── */

    public int activeTab() {
        return this.data[DATA_TAB];
    }

    public int storedWaifus() {
        return this.data[DATA_STORED_COUNT];
    }

    public int fallenWaifus() {
        return this.data[DATA_FALLEN_COUNT];
    }

    public int ownedWaifus() {
        return this.data[DATA_OWNED_COUNT];
    }

    public int diamondCount() {
        return this.data[DATA_DIAMONDS];
    }

    public boolean isOwned(int index) {
        return isBitSet(this.data[DATA_OWNED_0], this.data[DATA_OWNED_1], this.data[DATA_OWNED_2], index);
    }

    public boolean isStored(int index) {
        return isBitSet(this.data[DATA_STORED_0], this.data[DATA_STORED_1], this.data[DATA_STORED_2], index);
    }

    public boolean isFallen(int index) {
        return isBitSet(this.data[DATA_FALLEN_0], this.data[DATA_FALLEN_1], this.data[DATA_FALLEN_2], index);
    }

    public boolean isActiveInWorld(int index) {
        return isBitSet(this.data[DATA_ACTIVE_0], this.data[DATA_ACTIVE_1], this.data[DATA_ACTIVE_2], index);
    }

    /* ── Bitmask Helpers ──────────────────────────────────────────────────────────────────── */

    public static boolean isBitSet(int mask0, int mask1, int mask2, int index) {
        if (index < 0) return false;
        if (index < 32) return (mask0 & (1 << index)) != 0;
        if (index < 64) return (mask1 & (1 << (index - 32))) != 0;
        if (index < 96) return (mask2 & (1 << (index - 64))) != 0;
        return false;
    }

    public static void setBit(int[] masks, int index) {
        if (index < 0) return;
        if (index < 32) masks[0] |= (1 << index);
        else if (index < 64) masks[1] |= (1 << (index - 32));
        else if (index < 96) masks[2] |= (1 << (index - 64));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
