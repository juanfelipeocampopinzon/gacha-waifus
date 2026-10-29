package com.gachawaifus.menu;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.entity.WaifuMode;
import com.gachawaifus.registry.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Menú del panel de modos: shift + clic derecho con la mano vacía sobre una waifu propia.
 *
 * <p>El estado lo aplica siempre el servidor: la pantalla manda el ordinal del modo con
 * {@code handleInventoryButtonClick} y aquí se resuelve la entidad y se llama a
 * {@link AbstractWaifuEntity#setWaifuMode(WaifuMode)}. El ordinal que se resalta en pantalla es el
 * que viajó en los datos extra al abrir (y el que el cliente apunta al pulsar un botón): {@code
 * waifuMode} se guarda en el NBT, no en datos sincronizados de la entidad, y meter un
 * contenedor de datos por un solo entero no merecía la pena.
 */
public class WaifuModeMenu extends AbstractContainerMenu {

    /** La waifu, en el servidor. En el cliente es nulo: allí solo se dibuja. */
    private final AbstractWaifuEntity waifu;

    /** Dueña que abrió el panel, para el {@link #stillValid(Player)}. */
    private final UUID ownerUuid;

    /** Nombre limpio de códigos de color, para el encabezado de la pantalla. */
    private final String waifuName;

    /** Modo que se viene resaltando: el inicial del servidor, luego lo que el cliente pulsó. */
    private int displayedMode;

    /** Hogar de la waifu al abrir el panel, o null si no tiene. Viaja en los datos extra. */
    private final String homeText;

    /** Copia del servidor (la que construye el {@link SimpleMenuProvider} de {@link #open}). */
    public WaifuModeMenu(int containerId, Inventory inventory, AbstractWaifuEntity waifu) {
        super(ModMenuTypes.WAIFU_MODE.get(), containerId);
        this.waifu = waifu;
        this.ownerUuid = waifu.getOwnerUUID();
        this.waifuName = plain(waifu.getDisplayName().getString());
        this.displayedMode = waifu.waifuMode().ordinal();
        this.homeText = waifu.hasHome()
                ? waifu.homePos().toShortString() + " §7("
                        + waifu.level().dimension().location().getPath() + ")"
                : null;
    }

    /** Copia del cliente: lee el id y el modo que el servidor escribió en los datos extra. */
    public WaifuModeMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        super(ModMenuTypes.WAIFU_MODE.get(), containerId);
        this.waifu = null;
        this.ownerUuid = inventory.player.getUUID();
        int waifuId = extraData.readVarInt();
        this.displayedMode = extraData.readVarInt();
        this.homeText = extraData.readBoolean() ? extraData.readUtf(64) : null;
        Player player = inventory.player;
        this.waifuName = player.level().getEntity(waifuId) instanceof AbstractWaifuEntity seen
                ? plain(seen.getDisplayName().getString())
                : "";
    }

    /** Abre el panel sobre la waifu. */
    public static void open(ServerPlayer player, AbstractWaifuEntity waifu) {
        int waifuId = waifu.getId();
        int mode = waifu.waifuMode().ordinal();
        String home = waifu.hasHome()
                ? waifu.homePos().toShortString() + " §7("
                        + waifu.level().dimension().location().getPath() + ")"
                : "";
        player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, serverPlayer) -> new WaifuModeMenu(containerId, inventory, waifu),
                        Component.translatable("gachawaifus.mode_screen.title", plain(waifu.getDisplayName().getString()))),
                buf -> {
                    buf.writeVarInt(waifuId);
                    buf.writeVarInt(mode);
                    buf.writeBoolean(!home.isEmpty());
                    buf.writeUtf(home, 64);
                });
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        WaifuMode[] modes = WaifuMode.values();
        if (this.waifu == null || buttonId < 0 || buttonId >= modes.length
                || !player.getUUID().equals(this.ownerUuid)) {
            return false;
        }
        this.waifu.setWaifuMode(modes[buttonId]);
        player.displayClientMessage(Component.translatable("gachawaifus.mode_screen.set",
                this.waifuName, modes[buttonId].plainName()), true);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        // solo la evalúa el servidor; la copia cliente nunca decide nada
        return this.waifu == null || (this.waifu.isAlive() && player.getUUID().equals(this.ownerUuid));
    }

    /** Modo que hay que venir resaltado en la pantalla. */
    public WaifuMode displayedMode() {
        WaifuMode[] modes = WaifuMode.values();
        return this.displayedMode >= 0 && this.displayedMode < modes.length
                ? modes[this.displayedMode]
                : WaifuMode.AGRESIVO;
    }

    /** El cliente apunta su propio resaltado al mandar el clic (el servidor ya hizo el cambio). */
    public void setDisplayedMode(int modeOrdinal) {
        this.displayedMode = modeOrdinal;
    }

    public String waifuName() {
        return this.waifuName;
    }

    /** Hogar con el que se abrió el panel, o null si la waifu no tiene. */
    public String homeText() {
        return this.homeText;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // sin huecos que mover
    }

    /** Los códigos § de los nametags no los pinta la fuente de la pantalla: se quitan. */
    private static String plain(String name) {
        return name.replaceAll("(?i)§.", "");
    }
}
