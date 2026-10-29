package com.gachawaifus.menu;

import com.gachawaifus.gacha.GachaSavedData;
import com.gachawaifus.gacha.GachaService;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Menú del gacha: las dos pestañas (promocional y permanente) en una sola pantalla.
 *
 * <p>El servidor es quien manda: la pantalla solo envía clics de botón
 * ({@link #clickMenuButton}) y pinta lo que le llega. El estado viaja en dos canales:
 *
 * <ul>
 *   <li><b>{@link ContainerData}</b> — enteros: pestaña activa, pity de cada banner, garantía,
 *       bolitas que llevas y qué waifu enseña el banner.</li>
 *   <li><b>Slots de resultado</b> — un {@link SimpleContainer} de 10 huecos con los tokens
 *       (y la comida) de la última tirada. Se sincronizan solos y no se pueden robar.</li>
 * </ul>
 *
 * <p>La economía no cambia: las probabilidades viven en {@link GachaService}, compartidas con los
 * atajos de Shift + clic.
 */
public class GachaMenu extends AbstractContainerMenu {

    /* ── Botones que manda la pantalla ─────────────────────────────────────────────────────── */
    public static final int BUTTON_TAB_PROMO = 0;
    public static final int BUTTON_TAB_STANDARD = 1;
    /**
     * Tiradas. Llevan el banner DENTRO del id a propósito: si el jugador cambia de pestaña y
     * pulsa TIRAR antes de que llegue el nuevo {@code DATA_TAB}, el servidor tira igualmente del
     * banner que el jugador está viendo (y gasta las bolitas de ese banner), no del anterior.
     */
    public static final int BUTTON_PULL_1 = 10;
    public static final int BUTTON_PULL_10 = 11;
    public static final int BUTTON_PULL_1_STANDARD = 12;
    public static final int BUTTON_PULL_10_STANDARD = 13;

    /* ── Índices de ContainerData ──────────────────────────────────────────────────────────── */
    public static final int DATA_TAB = 0;
    public static final int DATA_PITY = 1;
    public static final int DATA_GUARANTEED = 2;
    public static final int DATA_PITY4 = 3;
    public static final int DATA_STANDARD_PITY = 4;
    public static final int DATA_PINK = 5;
    public static final int DATA_BLUE = 6;
    /** Sube en cada tirada: el cliente lo usa para disparar la animación de revelado. */
    public static final int DATA_SERIAL = 7;
    /** Índice de la waifu que enseña el banner (en PROMO_ROTATION si es promocional, en el pool si no). */
    public static final int DATA_SPOTLIGHT = 8;
    public static final int DATA_LAST_FIVE = 9;
    public static final int DATA_LAST_FOUR = 10;
    public static final int DATA_LAST_FOOD = 11;
    /** 1 si el banner todavía puede entregar algo. */
    public static final int DATA_PROMO_TARGETS = 12;
    public static final int DATA_STANDARD_TARGETS = 13;
    /** Máscara de bits del pool estándar: bit {@code i} = ya la tienes. */
    public static final int DATA_STANDARD_MASK = 14;
    /** 1 si ya tienes a la waifu que enseña el banner activo. */
    public static final int DATA_SPOTLIGHT_OWNED = 15;
    /**
     * Máscara de la última tirada: el bit {@code i} está a 1 si el hueco {@code i} es una waifu que
     * <b>no tenías</b> antes de tirar. La pantalla lo usa para la cinta de "¡NUEVA!".
     */
    public static final int DATA_NEW_MASK = 16;
    public static final int DATA_COUNT = 17;

    /** Huecos donde se pintan los tokens de la última tirada. */
    public static final int RESULT_SLOTS = 10;
    /** Coordenadas de esos huecos dentro de la pantalla (las usa también el menú). */
    public static final int RESULT_X = 14;
    public static final int RESULT_Y = 182;
    public static final int RESULT_STEP = 18;

    /** Cada cuántos ticks se recalculan las waifus poseídas (el barrido de entidades no es gratis). */
    private static final int OWNED_REFRESH_TICKS = 10;
    /** Ticks de bloqueo tras una tirada: evita que un doble clic dispare dos sorteos. */
    private static final int PULL_COOLDOWN_TICKS = 8;

    private final Player player;
    /** Posición de la Máquina Gacha si se abrió desde el bloque; {@code null} si fue desde el ítem. */
    @Nullable
    private final BlockPos machinePos;

    private final SimpleContainer results = new SimpleContainer(RESULT_SLOTS);
    private final int[] data = new int[DATA_COUNT];

    /** Pestaña activa SOLO en el servidor: el cliente la lee de {@link #DATA_TAB}. */
    private GachaService.Banner banner;
    private int cooldown;
    private int ownedRefresh;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index >= 0 && index < DATA_COUNT ? data[index] : 0;
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

    /** Constructor que usa el registro de menús (cliente y servidor). */
    public GachaMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, GachaService.Banner.PROMO, null);
    }

    public GachaMenu(int containerId, Inventory playerInventory, GachaService.Banner banner,
                     @Nullable BlockPos machinePos) {
        super(ModMenuTypes.GACHA_BANNER.get(), containerId);
        this.player = playerInventory.player;
        this.banner = banner;
        this.machinePos = machinePos;

        this.addDataSlots(this.dataAccess);

        for (int i = 0; i < RESULT_SLOTS; i++) {
            this.addSlot(new Slot(this.results, i, RESULT_X + i * RESULT_STEP, RESULT_Y) {
                @Override
                public boolean mayPickup(Player p) {
                    return false; // es un espejo del resultado, no un cofre
                }
            });
        }

        this.data[DATA_TAB] = banner == GachaService.Banner.STANDARD ? 1 : 0;
        if (!this.player.level().isClientSide) {
            refreshAll((ServerPlayer) this.player);
        }
    }

    /* ══ Apertura ══════════════════════════════════════════════════════════════════════════ */

    /** Abre el banner desde un ítem del inventario. */
    public static void open(ServerPlayer player, GachaService.Banner banner) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new GachaMenu(id, inv, banner, null),
                Component.translatable("gachawaifus.gacha.title")));
    }

    /** Abre el banner desde la Máquina Gacha (a distancia de bloque, por eso se guarda la posición). */
    public static void openAt(ServerPlayer player, GachaService.Banner banner, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new GachaMenu(id, inv, banner, pos),
                Component.translatable("gachawaifus.gacha.title")));
    }

    /* ══ Estado ════════════════════════════════════════════════════════════════════════════ */

    @Override
    public void broadcastChanges() {
        if (this.player instanceof ServerPlayer serverPlayer) {
            // Cada tick el servidor refresca contadores y pity: así la pantalla nunca enseña algo
            // que ya no es verdad (bolitas gastadas, colección completada, cambio de día...).
            if (this.cooldown > 0) {
                this.cooldown--;
            }
            refreshBallsAndPity(serverPlayer);
            if (++this.ownedRefresh >= OWNED_REFRESH_TICKS) {
                this.ownedRefresh = 0;
                refreshOwnership(serverPlayer);
            }
        }
        super.broadcastChanges();
    }

    private void refreshAll(ServerPlayer player) {
        this.refreshBallsAndPity(player);
        this.refreshOwnership(player);
    }

    private void refreshBallsAndPity(ServerPlayer serverPlayer) {
        this.data[DATA_PINK] = GachaService.countBalls(serverPlayer, GachaService.Banner.PROMO);
        this.data[DATA_BLUE] = GachaService.countBalls(serverPlayer, GachaService.Banner.STANDARD);

        ServerLevel level = serverPlayer.serverLevel();
        GachaSavedData.PlayerState state = GachaSavedData.get(level).state(serverPlayer.getUUID());
        this.data[DATA_PITY] = state.pity;
        this.data[DATA_GUARANTEED] = state.guaranteed ? 1 : 0;
        this.data[DATA_PITY4] = state.pity4;
        this.data[DATA_STANDARD_PITY] = state.standardPity;

        // La destacada del banner ACTIVO: índice dentro de la lista que le toca (rotación o pool).
        WaifuRoster.Entry spot = GachaService.spotlight(level, this.banner);
        this.data[DATA_SPOTLIGHT] = this.banner == GachaService.Banner.PROMO
                ? WaifuRoster.PROMO_ROTATION.indexOf(spot)
                : WaifuRoster.STANDARD_POOL.indexOf(spot);
    }

    /** Recalcula qué waifus tiene el jugador en UNA pasada (barrido de entidades compartido). */
    private void refreshOwnership(ServerPlayer serverPlayer) {
        Set<String> owned = WaifuRoster.ownedIds(serverPlayer);
        this.data[DATA_PROMO_TARGETS] =
                GachaService.hasTargets(GachaService.Banner.PROMO, owned) ? 1 : 0;
        this.data[DATA_STANDARD_TARGETS] =
                GachaService.hasTargets(GachaService.Banner.STANDARD, owned) ? 1 : 0;
        this.data[DATA_STANDARD_MASK] = GachaService.standardOwnedMask(owned);
        WaifuRoster.Entry spot = spotlight();
        this.data[DATA_SPOTLIGHT_OWNED] = spot != null && owned.contains(spot.id()) ? 1 : 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        switch (buttonId) {
            case BUTTON_TAB_PROMO -> {
                this.banner = GachaService.Banner.PROMO;
                this.data[DATA_TAB] = 0;
                refreshAll(serverPlayer);
                return true;
            }
            case BUTTON_TAB_STANDARD -> {
                this.banner = GachaService.Banner.STANDARD;
                this.data[DATA_TAB] = 1;
                refreshAll(serverPlayer);
                return true;
            }
            case BUTTON_PULL_1 -> {
                pull(serverPlayer, GachaService.Banner.PROMO, 1);
                return true;
            }
            case BUTTON_PULL_10 -> {
                pull(serverPlayer, GachaService.Banner.PROMO, 10);
                return true;
            }
            case BUTTON_PULL_1_STANDARD -> {
                pull(serverPlayer, GachaService.Banner.STANDARD, 1);
                return true;
            }
            case BUTTON_PULL_10_STANDARD -> {
                pull(serverPlayer, GachaService.Banner.STANDARD, 10);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void pull(ServerPlayer serverPlayer, GachaService.Banner banner, int count) {
        if (this.cooldown > 0) {
            return; // doble clic / spam de botón: se ignora sin gastar nada
        }
        // El botón manda el banner: la pestaña del servidor se alinea con lo que ve el jugador.
        if (this.banner != banner) {
            this.banner = banner;
            this.data[DATA_TAB] = banner == GachaService.Banner.STANDARD ? 1 : 0;
        }
        // Retrato de la colección ANTES de tirar: así se puede marcar lo que es nuevo.
        Set<String> before = WaifuRoster.ownedIds(serverPlayer);
        GachaService.Result result = GachaService.pull(serverPlayer, banner, count);
        if (!result.success) {
            // El aviso del motivo ya lo manda GachaService.pull.
            refreshAll(serverPlayer);
            return;
        }
        this.cooldown = PULL_COOLDOWN_TICKS;
        this.showResult(result);
        this.data[DATA_SERIAL]++;
        this.data[DATA_LAST_FIVE] = result.fiveStars;
        this.data[DATA_LAST_FOUR] = result.fourStars;
        this.data[DATA_LAST_FOOD] = result.food.size();
        this.data[DATA_NEW_MASK] = newMask(result, before);
        refreshAll(serverPlayer);
    }

    /** Bit {@code i} = la waifu del hueco {@code i} no estaba en la colección antes de la tirada. */
    private static int newMask(GachaService.Result result, Set<String> before) {
        int mask = 0;
        for (int i = 0; i < result.waifus.size() && i < RESULT_SLOTS; i++) {
            if (!before.contains(result.waifus.get(i).id())) {
                mask |= 1 << i;
            }
        }
        return mask;
    }

    /** Vuelca el resultado en los 10 huecos visuales: primero las waifus, después la comida. */
    private void showResult(GachaService.Result result) {
        this.results.clearContent();
        int slot = 0;
        for (WaifuRoster.Entry entry : result.waifus) {
            if (slot >= RESULT_SLOTS) return;
            this.results.setItem(slot++, new ItemStack(entry.token().get()));
        }
        for (ItemStack food : result.food) {
            if (slot >= RESULT_SLOTS) return;
            this.results.setItem(slot++, food.copy());
        }
    }

    /* ══ Lectura para la pantalla ══════════════════════════════════════════════════════════ */

    public int tab() {
        return this.data[DATA_TAB];
    }

    public GachaService.Banner banner() {
        return this.data[DATA_TAB] == 1 ? GachaService.Banner.STANDARD : GachaService.Banner.PROMO;
    }

    public int pity() {
        return this.data[DATA_PITY];
    }

    public boolean guaranteed() {
        return this.data[DATA_GUARANTEED] != 0;
    }

    public int pity4() {
        return this.data[DATA_PITY4];
    }

    public int standardPity() {
        return this.data[DATA_STANDARD_PITY];
    }

    public int pinkBalls() {
        return this.data[DATA_PINK];
    }

    public int blueBalls() {
        return this.data[DATA_BLUE];
    }

    public int serial() {
        return this.data[DATA_SERIAL];
    }

    public int lastFive() {
        return this.data[DATA_LAST_FIVE];
    }

    public int lastFour() {
        return this.data[DATA_LAST_FOUR];
    }

    public int lastFood() {
        return this.data[DATA_LAST_FOOD];
    }

    public int fineBalls() {
        return banner() == GachaService.Banner.PROMO ? pinkBalls() : blueBalls();
    }

    public boolean hasTargets() {
        return (banner() == GachaService.Banner.PROMO
                ? this.data[DATA_PROMO_TARGETS] : this.data[DATA_STANDARD_TARGETS]) != 0;
    }

    public int standardMask() {
        return this.data[DATA_STANDARD_MASK];
    }

    /** ¿Ya tienes a la waifu que enseña el banner activo? */
    public boolean spotlightOwned() {
        return this.data[DATA_SPOTLIGHT_OWNED] != 0;
    }

    /** La waifu que enseña el banner activo, o {@code null} si el índice aún no ha llegado. */
    @Nullable
    public WaifuRoster.Entry spotlight() {
        List<WaifuRoster.Entry> list = banner() == GachaService.Banner.PROMO
                ? WaifuRoster.PROMO_ROTATION : WaifuRoster.STANDARD_POOL;
        int index = this.data[DATA_SPOTLIGHT];
        return index >= 0 && index < list.size() ? list.get(index) : null;
    }

    public boolean canPull(int count) {
        return hasTargets() && this.cooldown <= 0 && fineBalls() >= count;
    }

    /** ¿El último resultado trajo una 5★? (la pantalla lo usa para el destello). */
    public boolean lastPullHadFive() {
        return lastFive() > 0;
    }

    /** Máscara de "esta waifu es nueva" de la última tirada (bit por hueco). */
    public int newMask() {
        return this.data[DATA_NEW_MASK];
    }

    /** La pila del hueco {@code index} de resultados; vacía si no hay nada. */
    public ItemStack resultStack(int index) {
        return index >= 0 && index < RESULT_SLOTS ? this.results.getItem(index) : ItemStack.EMPTY;
    }

    /**
     * ¿El hueco pertenece al resultado de la tirada? La pantalla dibuja animaciones solo en estos:
     * los del inventario del jugador se pintan normales aunque lleven un token.
     */
    public boolean isResultSlot(Slot slot) {
        return slot.container == this.results;
    }

    /* ══ Contrato del menú ═════════════════════════════════════════════════════════════════ */

    @Override
    public boolean stillValid(Player player) {
        if (this.machinePos == null) {
            return true; // abierto desde el ítem: no depende de ninguna distancia
        }
        return player.distanceToSqr(this.machinePos.getX() + 0.5D, this.machinePos.getY() + 0.5D,
                this.machinePos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < RESULT_SLOTS) {
            return; // los huecos de resultado no se tocan
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
