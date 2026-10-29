package com.gachawaifus.client;

import com.gachawaifus.entity.WaifuMode;
import com.gachawaifus.menu.WaifuModeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;

/**
 * El panel de modos: shift + clic derecho (mano vacía) sobre una waifu propia.
 *
 * <p>Igual que {@link GuideScreen}, todo se dibuja con rectángulos y texto: el mod no tiene
 * {@code textures/gui/}, así que la pantalla no se rompe si falta un PNG. Cada fila es un botón
 * propio (se golpea con {@code mouseClicked}, no con un {@code Button}) y manda su ordinal al
 * servidor con {@code handleInventoryButtonClick}; aquí no se cambia nada, solo se pinta.
 */
public class WaifuModeScreen extends AbstractContainerScreen<WaifuModeMenu> {

    /* ── Geometría ───────────────────────────────────────────────────────────────────────── */
    private static final int W = 232;
    private static final int ROW_H = 24;
    private static final int H = 26 + WaifuMode.values().length * ROW_H + 26;
    private static final int HEADER_H = 20;

    private static final int ROW_X = 8;
    private static final int ROW_W = W - 16;
    private static final int ROWS_Y = HEADER_H + 8;
    private static final int FOOT_Y = H - 22;

    /* ── Paleta (la misma familia que la Guía) ───────────────────────────────────────────── */
    private static final int PANEL_TOP = 0xFF1A1424;
    private static final int PANEL_BOTTOM = 0xFF0C0912;
    private static final int BORDER = 0xFF463A5C;
    private static final int ROW_BG = 0xFF241C33;
    private static final int ROW_EDGE = 0xFF5A4A76;
    private static final int ROW_HOVER = 0xFF312645;
    private static final int TEXT = 0xFFEDE7F5;
    private static final int TEXT_DIM = 0xFF9A8FAD;
    private static final int PINK_ACCENT = 0xFFFF6FB5;

    /** Colores de los § de las filas, en el orden del enum. */
    private static final int[] MODE_COLOR = {
            0xFF6BE08A, // PASIVO   §a
            0xFFFFF0A0, // NEUTRO   §e
            0xFFFF8C8C, // AGRESIVO §c
            0xFF9DB4FF, // SENTADA  §9
            0xFF6BE08A, // EN_CASA  §2 -> mismo verde claro que PASIVO, pero con su propio rótulo
    };

    public WaifuModeScreen(WaifuModeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** El panel no pinta el rótulo de vanilla: la cabecera y las filas son suyas. */
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
        graphics.drawString(this.font, Component.translatable("gachawaifus.mode_screen.header"),
                left + 8, top + 6, 0xFFFFFFFF, true);

        WaifuMode current = this.menu.displayedMode();
        for (WaifuMode mode : WaifuMode.values()) {
            drawRow(graphics, left, top, mode, mode == current, mouseY - (top + rowY(mode)),
                    mouseX - (left + ROW_X));
        }

        drawFoot(graphics, left, top);
    }

    private void drawRow(GuiGraphics graphics, int left, int top, WaifuMode mode, boolean active,
                         int localY, int localX) {
        int y = top + rowY(mode);
        boolean hover = active ? false : localY >= 0 && localY < ROW_H - 2 && localX >= 0 && localX < ROW_W;
        graphics.fill(left + ROW_X, y, left + ROW_X + ROW_W, y + ROW_H - 2, hover ? ROW_HOVER : ROW_BG);
        frame(graphics, left + ROW_X, y, ROW_W, ROW_H - 2, active ? PINK_ACCENT : ROW_EDGE);
        if (active) {
            graphics.fill(left + ROW_X + 1, y + 1, left + ROW_X + 3, y + ROW_H - 3, PINK_ACCENT);
        }
        int color = MODE_COLOR[mode.ordinal()];
        graphics.drawString(this.font, Component.translatable("gachawaifus.mode." + key(mode)),
                left + ROW_X + 7, y + 3, color, false);
        graphics.drawString(this.font, Component.translatable("gachawaifus.mode." + key(mode) + ".desc"),
                left + ROW_X + 7, y + 12, TEXT_DIM, false);
    }

    /** El pie: el hogar (o su ausencia) y la tecla para cerrar. */
    private void drawFoot(GuiGraphics graphics, int left, int top) {
        String home = this.menu.homeText();
        Component line;
        int color = TEXT_DIM;
        if (home == null) {
            line = Component.translatable("gachawaifus.mode_screen.no_home");
        } else {
            line = Component.literal("§2" + Component.translatable("gachawaifus.mode_screen.home")
                    .getString() + " §f" + home);
            color = TEXT;
        }
        graphics.drawString(this.font, line, left + 8, top + FOOT_Y, color, false);
        graphics.drawString(this.font, Component.translatable("gachawaifus.mode_screen.close_hint"),
                left + 8, top + FOOT_Y + 11, TEXT_DIM, false);
    }

    /* ══ Clics ═════════════════════════════════════════════════════════════════════════════ */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            WaifuMode mode = rowAt(mouseX - this.leftPos, mouseY - this.topPos);
            if (mode != null && this.minecraft != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, mode.ordinal());
                this.menu.setDisplayedMode(mode.ordinal());
                this.minecraft.getSoundManager().play(
                        SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.4F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Qué fila cae bajo el puntero, o null si no es ninguna. */
    private static WaifuMode rowAt(double dx, double dy) {
        if (dx < ROW_X || dx >= ROW_X + ROW_W) {
            return null;
        }
        for (WaifuMode mode : WaifuMode.values()) {
            if (dy >= rowY(mode) && dy < rowY(mode) + ROW_H - 2) {
                return mode;
            }
        }
        return null;
    }

    private static int rowY(WaifuMode mode) {
        return ROWS_Y + mode.ordinal() * ROW_H;
    }

    /** Clave de lang del modo, en minúscula y sin guiones bajos dobles. */
    private static String key(WaifuMode mode) {
        return switch (mode) {
            case PASIVO -> "pasivo";
            case NEUTRO -> "neutro";
            case AGRESIVO -> "agresivo";
            case SENTADA -> "sentada";
            case EN_CASA -> "en_casa";
        };
    }

    private static void frame(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }
}
