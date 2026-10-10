package com.gachawaifus.client;

import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI de la Cápsula Waifu en páginas de 27. Reutiliza la textura de cofre vanilla: las 3 primeras
 * filas son la rejilla y la cuarta se pinta como una barra de navegación (sin casillas) con los
 * botones « » y el contador de página. La rueda del ratón también pasa de página.
 */
public class WaifuStorageScreen extends AbstractContainerScreen<WaifuStorageMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    /** Filas de casillas del cofre: 3 de waifus + 1 reutilizada como barra de navegación. */
    private final int containerRows = 4;
    /** Altura de la fila de casillas que se convierte en barra de navegación. */
    private static final int NAV_ROW = 3;

    private Button previousButton;
    private Button nextButton;

    public WaifuStorageScreen(WaifuStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.literal("§dCápsula Waifu §7· waifus guardadas y caídas"));
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    /** Y de la tela donde empieza la franja de navegación (donde vanilla pintaría su 4ª fila). */
    private int navTop() {
        return this.topPos + 18 + NAV_ROW * 18;
    }

    @Override
    protected void init() {
        super.init();
        int navY = this.navTop() + 3;
        this.previousButton = Button.builder(Component.literal("«"), button ->
                        this.sendPageRequest(WaifuStorageMenu.PAGE_PREVIOUS))
                .bounds(this.leftPos + 8, navY, 20, 14)
                .tooltip(Tooltip.create(Component.literal("Página anterior")))
                .build();
        this.nextButton = Button.builder(Component.literal("»"), button ->
                        this.sendPageRequest(WaifuStorageMenu.PAGE_NEXT))
                .bounds(this.leftPos + this.imageWidth - 28, navY, 20, 14)
                .tooltip(Tooltip.create(Component.literal("Página siguiente")))
                .build();
        this.addRenderableWidget(this.previousButton);
        this.addRenderableWidget(this.nextButton);
    }

    private void sendPageRequest(int buttonId) {
        if (this.minecraft != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDirection, double scrollAmount) {
        if (this.menu.totalPages() > 1 && scrollDirection != 0.0) {
            this.sendPageRequest(scrollDirection > 0
                    ? WaifuStorageMenu.PAGE_PREVIOUS
                    : WaifuStorageMenu.PAGE_NEXT);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDirection, scrollAmount);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Estado de los botones antes de que Screen#render los dibuje: inactivos salen grisados
        // y no aceptan clic, así se ve de un vistazo si ya no hay más páginas.
        this.previousButton.active = this.menu.currentPage() > 1;
        this.nextButton.active = this.menu.currentPage() < this.menu.totalPages();
        super.render(graphics, mouseX, mouseY, partialTick);
        String page = "§dPág. " + this.menu.currentPage() + " de " + this.menu.totalPages();
        graphics.drawString(this.font, Component.literal(page),
                this.leftPos + (this.imageWidth - this.font.width(page)) / 2,
                this.navTop() + 5, 0xFFFFFF, true);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    /** Cabecera con el recuento real de la colección + rótulo de Inventario (sin super: el title
     *  fijo del menú se sustituye aquí). */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String header = "§dCápsula Waifu §7· §a" + this.menu.storedWaifus() + " vivas §7· §c"
                + this.menu.fallenWaifus() + " caídas";
        graphics.drawString(this.font, Component.literal(header),
                this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("container.inventory"),
                8, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        graphics.blit(TEXTURE, left, top + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);

        // La 4ª fila del cofre vanilla está vacía (las waifus caben en 3) y sus casillas huecas
        // hacían parecer que los botones flotaban encima de la rejilla. Se forra en el color del
        // panel, con su rebaje, para que se lea como una barra de navegación.
        int navTop = top + 18 + NAV_ROW * 18;
        int navRight = left + this.imageWidth - 4;
        graphics.fill(left + 4, navTop, navRight, navTop + 18, 0xFFC6C6C6);
        graphics.fill(left + 4, navTop, navRight, navTop + 1, 0x40000000);
        graphics.fill(left + 4, navTop + 17, navRight, navTop + 18, 0x40FFFFFF);
    }
}
