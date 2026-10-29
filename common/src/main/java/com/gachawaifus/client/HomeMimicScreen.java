package com.gachawaifus.client;

import com.gachawaifus.menu.HomeMimicMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * El selector de textura del Bloque de Hogar: rejilla de bloques con buscador y paginas.
 *
 * <p>Sigue la estética de las otras pantallas del mod (rectángulos y texto, sin PNG): el panel se
 * dibuja con rellenos degradados y el {@code frame} de un píxel, y el icono de cada bloque lo pone
 * vanilla con {@code renderItem}, que es justamente lo que hace que merezca la pena — la previsualización
 * es el bloque real, modelo y tinte incluidos.
 *
 * <p>Las casillas no son huecos del menú: son zonas dibujadas, y lo que se manda al servidor es el
 * <b>índice global</b> del bloque en {@link HomeMimicMenu#choices()}. El filtro y el paginado son
 * puramente locales, así que el buscador puede recortar la lista sin que el servidor tenga que saber
 * que existe.
 */
public class HomeMimicScreen extends AbstractContainerScreen<HomeMimicMenu> {

    /* ── Geometría ───────────────────────────────────────────────────────────────────────── */
    private static final int COLS = 9;
    private static final int ROWS = 5;
    private static final int CELL = 18;
    private static final int PER_PAGE = COLS * ROWS;

    private static final int W = COLS * CELL + 16;
    private static final int HEADER_H = 20;
    private static final int SEARCH_Y = HEADER_H + 6;
    private static final int GRID_X = 8;
    private static final int GRID_Y = SEARCH_Y + 22;
    private static final int H = GRID_Y + ROWS * CELL + 34;

    /* ── Paleta (la misma familia que el panel de modos y la Guía) ────────────────────────── */
    private static final int PANEL_TOP = 0xFF1A1424;
    private static final int PANEL_BOTTOM = 0xFF0C0912;
    private static final int BORDER = 0xFF463A5C;
    private static final int CELL_BG = 0xFF241C33;
    private static final int CELL_EDGE = 0xFF5A4A76;
    private static final int CELL_HOVER = 0xFF312645;
    private static final int TEXT = 0xFFEDE7F5;
    private static final int TEXT_DIM = 0xFF9A8FAD;
    private static final int PINK_ACCENT = 0xFFFF6FB5;

    /** Índices de {@link HomeMimicMenu#choices()} que pasan el filtro, en orden. */
    private final List<Integer> matching = new ArrayList<>();
    private String filter = "";
    private int page = 0;

    private EditBox search;

    public HomeMimicScreen(HomeMimicMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        this.search = this.addRenderableWidget(new EditBox(this.font,
                this.leftPos + GRID_X, this.topPos + SEARCH_Y, W - 16, 16,
                Component.translatable("gachawaifus.mimic_screen.search")));
        this.search.setHint(Component.translatable("gachawaifus.mimic_screen.search.hint"));
        this.search.setMaxLength(48);
        this.search.setResponder(text -> {
            this.filter = text.trim().toLowerCase();
            this.page = 0;
            refreshMatches();
        });
        // Al redimensionar la ventana vanilla recrea los widgets: el texto vuelve vacío, así que se
        // devuelve el filtro que había (el responder se dispara otra vez y deja la lista como estaba).
        if (!this.filter.isEmpty()) {
            this.search.setValue(this.filter);
        }
        refreshMatches();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** El rótulo de vanilla no: la cabecera y el pie son dibujados a mano. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    /* ══ Dibujo ═══════════════════════════════════════════════════════════════════════════ */

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(0, 0, this.width, this.height, 0xB2000000);
        graphics.fillGradient(left + 2, top + HEADER_H, left + W - 2, top + H - 2, PANEL_TOP, PANEL_BOTTOM);
        frame(graphics, left, top, W, H, BORDER);

        graphics.fillGradient(left + 1, top + 1, left + W - 1, top + HEADER_H, 0xFF3A2A12, 0xFF140F1C);
        graphics.fill(left + 1, top + HEADER_H, left + W - 1, top + HEADER_H + 1, PINK_ACCENT);
        graphics.drawString(this.font, Component.translatable("gachawaifus.mimic_screen.header"),
                left + 8, top + 6, 0xFFFFFFFF, true);

        for (int slot = 0; slot < PER_PAGE; slot++) {
            drawCell(graphics, left, top, slot, mouseX, mouseY);
        }
        drawFooter(graphics, left, top);
    }

    private void drawCell(GuiGraphics graphics, int left, int top, int slot, int mouseX, int mouseY) {
        int x = left + GRID_X + (slot % COLS) * CELL;
        int y = top + GRID_Y + (slot / COLS) * CELL;
        Integer global = globalAt(slot);
        if (global == null) {
            graphics.fill(x, y, x + CELL - 2, y + CELL - 2, CELL_BG);
            return;
        }
        Block block = HomeMimicMenu.choices().get(global);
        boolean hover = mouseX >= x && mouseX < x + CELL - 2 && mouseY >= y && mouseY < y + CELL - 2;
        boolean current = BuiltInRegistries.BLOCK.getKey(block).toString().equals(this.menu.currentMimic());
        graphics.fill(x, y, x + CELL - 2, y + CELL - 2, hover ? CELL_HOVER : CELL_BG);
        frame(graphics, x, y, CELL - 2, CELL - 2, current ? PINK_ACCENT : CELL_EDGE);
        if (current) {
            graphics.fill(x + 1, y + 1, x + CELL - 3, y + 2, PINK_ACCENT);
        }
        graphics.renderItem(new ItemStack(block.asItem()), x + 1, y + 1);
    }

    /** lo que se imita ahora, cuántos resultados hay, la página y cómo salir. */
    private void drawFooter(GuiGraphics graphics, int left, int top) {
        graphics.drawString(this.font,
                Component.translatable("gachawaifus.mimic_screen.current", currentName()),
                left + 8, top + H - 28, TEXT, false);

        int y = top + H - 18;
        graphics.drawString(this.font, Component.translatable("gachawaifus.mimic_screen.showing",
                        this.matching.size(), this.page + 1, pages()),
                left + 8, y, TEXT_DIM, false);
        graphics.drawString(this.font, arrow(false), left + W - 30, y, arrowEnabled(false) ? TEXT : TEXT_DIM, false);
        graphics.drawString(this.font, arrow(true), left + W - 18, y, arrowEnabled(true) ? TEXT : TEXT_DIM, false);

        graphics.drawString(this.font, Component.translatable("gachawaifus.mimic_screen.close_hint"),
                left + 8, top + H - 9, TEXT_DIM, false);
    }

    /** Nombre del bloque que se está imitando, resuelto por registro (no barrriendo la lista). */
    private Component currentName() {
        ResourceLocation id = ResourceLocation.tryParse(this.menu.currentMimic());
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return Component.translatable("gachawaifus.mimic_screen.current.none");
        }
        return BuiltInRegistries.BLOCK.get(id).getName();
    }

    /* ══ Interacción ═══════════════════════════════════════════════════════════════════════ */

    /**
     * El tooltip de la casilla. Se dibuja aquí y no en {@code renderTooltip} porque el
     * {@code render} de {@code AbstractContainerScreen} no llama a ese hook — solo pinta el tooltip del
     * hueco bajo el puntero, y este menú no tiene huecos.
     */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        int slot = slotAt((int) (mouseX - this.leftPos), (int) (mouseY - this.topPos));
        Integer global = slot < 0 ? null : globalAt(slot);
        if (global != null) {
            graphics.renderTooltip(this.font,
                    new ItemStack(HomeMimicMenu.choices().get(global).asItem()), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true; // el buscador u otro widget ya se lo comieron
        }
        if (button != 0) {
            return false;
        }
        int dx = (int) (mouseX - this.leftPos);
        int dy = (int) (mouseY - this.topPos);

        // flechas del pie: -18 y -30 desde la derecha, en la fila del contador
        if (dy >= H - 18 && dy < H - 8) {
            if (dx >= W - 30 && dx < W - 18) {
                return turnPage(-1);
            }
            if (dx >= W - 18 && dx < W - 6) {
                return turnPage(1);
            }
        }

        int slot = slotAt(dx, dy);
        Integer global = slot < 0 ? null : globalAt(slot);
        if (global == null) {
            return false;
        }
        if (this.minecraft != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, global);
            this.menu.setDisplayedMimic(
                    BuiltInRegistries.BLOCK.getKey(HomeMimicMenu.choices().get(global)).toString());
            this.minecraft.getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.2F));
        }
        return true;
    }

    /** La rueda del ratón pasa de página, igual que en una lista de vanilla. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
        if (scrollDeltaY != 0.0D) {
            return turnPage(scrollDeltaY > 0.0D ? -1 : 1);
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDeltaX, scrollDeltaY);
    }

    private boolean turnPage(int direction) {
        int target = this.page + direction;
        if (target < 0 || target >= pages()) {
            return false;
        }
        this.page = target;
        return true;
    }

    /* ══ Utilidades de la lista ═════════════════════════════════════════════════════════════ */

    /** Recalcula qué índices globales pasan el filtro (solo afecta a lo local). */
    private void refreshMatches() {
        this.matching.clear();
        List<Block> choices = HomeMimicMenu.choices();
        for (int i = 0; i < choices.size(); i++) {
            Block block = choices.get(i);
            if (this.filter.isEmpty() || matches(block)) {
                this.matching.add(i);
            }
        }
        clampPage();
    }

    private boolean matches(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath().contains(this.filter)
                || block.getName().getString().toLowerCase().contains(this.filter);
    }

    private void clampPage() {
        if (this.page >= pages()) {
            this.page = pages() - 1;
        }
        if (this.page < 0) {
            this.page = 0;
        }
    }

    private int pages() {
        return Math.max(1, (this.matching.size() + PER_PAGE - 1) / PER_PAGE);
    }

    /** Índice global del bloque de la casilla, o null si la casilla va vacía. */
    private Integer globalAt(int slot) {
        int index = this.page * PER_PAGE + slot;
        if (index < 0 || index >= this.matching.size()) {
            return null;
        }
        return this.matching.get(index);
    }

    /** Qué casilla cae bajo el puntero, o -1 si el clic no aterrizó en la rejilla. */
    private int slotAt(int dx, int dy) {
        if (dx < GRID_X || dx >= GRID_X + COLS * CELL || dy < GRID_Y || dy >= GRID_Y + ROWS * CELL) {
            return -1;
        }
        return ((dy - GRID_Y) / CELL) * COLS + (dx - GRID_X) / CELL;
    }

    private static Component arrow(boolean forward) {
        return Component.translatable(forward ? "gachawaifus.mimic_screen.next" : "gachawaifus.mimic_screen.prev");
    }

    private boolean arrowEnabled(boolean forward) {
        return forward ? this.page + 1 < pages() : this.page > 0;
    }

    private static void frame(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
