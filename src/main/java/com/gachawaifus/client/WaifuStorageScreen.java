package com.gachawaifus.client;

import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla Grimorio / Compendio de la Cápsula Waifu.
 *
 * <p>Diseñada con estética procedural de alta definición heredada del Terminal Gacha
 * (sin depender de texturas de cofre vanilla que limiten la interfaz):
 * <ul>
 *   <li><b>Página Izquierda:</b> Catálogo interactivo con dos vistas (Cápsula activa y Glosario completo),
 *       filtros rápidos, cuadrícula de tokens con badges de estado y paginación.</li>
 *   <li><b>Página Derecha:</b> Ficha técnica detallada, visor 3D en tiempo real (animación y rotación),
 *       marco dedicado para ilustraciones/pixel art de <b>128 × 128 píxeles</b> y botón contextual de acción
 *       (Invocación y Ritual de Resurrección con diamantes).</li>
 * </ul>
 */
public class WaifuStorageScreen extends AbstractContainerScreen<WaifuStorageMenu> {

    /* ── Geometría (Panel Doble Tipo Libro) ─────────────────────────────────────────────────── */
    private static final int W = 380;
    private static final int H = 222;
    private static final int HEADER_H = 22;

    private static final int TAB_W = 76;
    private static final int TAB_H = 16;
    private static final int TAB_Y = 4;
    private static final int TAB_CAPSULE_X = 130;
    private static final int TAB_GLOSSARY_X = 212;
    private static final int CLOSE_X = W - 20;

    // Cuadrícula izquierda (5 columnas x 4 filas = 20 por página)
    private static final int COLS = 5;
    private static final int ROWS = 4;
    private static final int PER_PAGE = COLS * ROWS; // 20
    private static final int CELL_SIZE = 28;
    private static final int GRID_START_X = 22;
    private static final int GRID_START_Y = 48;

    // Marco Multimedia Derecho (128x128)
    private static final int FRAME_W = 128;
    private static final int FRAME_H = 128;
    private static final int FRAME_X = 226;
    private static final int FRAME_Y = 56;

    // Modos de visualización multimedia
    private static final int VIEW_3D = 0;
    private static final int VIEW_PIXEL_ART = 1;

    // Subfiltros del Glosario
    private static final int FILTER_ALL = 0;
    private static final int FILTER_OWNED = 1;
    private static final int FILTER_MISSING = 2;

    /* ── Paleta de Colores Gacha ───────────────────────────────────────────────────────────── */
    private static final int PANEL_TOP = 0xFF1A1424;
    private static final int PANEL_BOTTOM = 0xFF0C0912;
    private static final int BORDER = 0xFF463A5C;
    private static final int BORDER_DARK = 0xFF0E0A14;
    private static final int SLOT_BG = 0xFF15101E;
    private static final int TEXT = 0xFFEDE7F5;
    private static final int TEXT_DIM = 0xFF9A8FAD;
    private static final int GOLD = 0xFFFFD86B;
    private static final int GREEN = 0xFF6BE08A;
    private static final int RED = 0xFFFF5A6E;
    private static final int PINK_ACCENT = 0xFFFF6FB5;
    private static final int BLUE_ACCENT = 0xFF62C8FF;

    /* ── Estado Local de la Interfaz ───────────────────────────────────────────────────────── */
    private int activeTab = 0; // 0 = Cápsula, 1 = Glosario
    private int glossaryFilter = FILTER_ALL;
    private int page = 0;
    private int viewMode = VIEW_3D; // 0 = 3D, 1 = Pixel Art 128x128

    @Nullable
    private WaifuRoster.Entry selectedEntry;
    private long lastClickTime = 0L;
    @Nullable
    private WaifuRoster.Entry lastClickedEntry = null;

    public WaifuStorageScreen(WaifuStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.literal("§dCápsula Waifu §7& Compendio"));
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        this.activeTab = this.menu.activeTab();
        ensureInitialSelection();
        WaifuPreviewRenderer.reset();
    }

    @Override
    public void removed() {
        super.removed();
        WaifuPreviewRenderer.reset();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void ensureInitialSelection() {
        if (this.selectedEntry != null) {
            return;
        }
        List<WaifuRoster.Entry> currentList = getActiveList();
        if (!currentList.isEmpty()) {
            this.selectedEntry = currentList.get(0);
        } else if (!WaifuRoster.ROTATION.isEmpty()) {
            this.selectedEntry = WaifuRoster.ROTATION.get(0);
        }
    }

    private List<WaifuRoster.Entry> getActiveList() {
        List<WaifuRoster.Entry> list = new ArrayList<>();
        List<WaifuRoster.Entry> rotation = WaifuRoster.ROTATION;

        if (this.activeTab == WaifuStorageMenu.TAB_CAPSULE) {
            for (int i = 0; i < rotation.size(); i++) {
                if (this.menu.isStored(i) || this.menu.isFallen(i)) {
                    list.add(rotation.get(i));
                }
            }
        } else {
            // Glosario con filtros
            for (int i = 0; i < rotation.size(); i++) {
                WaifuRoster.Entry entry = rotation.get(i);
                boolean owned = this.menu.isOwned(i);
                if (this.glossaryFilter == FILTER_ALL) {
                    list.add(entry);
                } else if (this.glossaryFilter == FILTER_OWNED && owned) {
                    list.add(entry);
                } else if (this.glossaryFilter == FILTER_MISSING && !owned) {
                    list.add(entry);
                }
            }
        }
        return list;
    }

    private int accentColor() {
        if (this.selectedEntry == null) {
            return PINK_ACCENT;
        }
        WaifuColor color = WaifuColors.of(this.selectedEntry.id());
        int raw = color == null ? PINK_ACCENT : color.rgb();
        return this.activeTab == WaifuStorageMenu.TAB_CAPSULE ? mix(raw, PINK_ACCENT, 0.4F) : mix(raw, BLUE_ACCENT, 0.4F);
    }

    /* ══ Renderizado Principal ═══════════════════════════════════════════════════════════════ */

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderCustomTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // No pintamos las etiquetas automáticas de cofre vanilla para controlar todo a medida.
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        long now = Util.getMillis();
        int accent = accentColor();

        // 1. Velo de oscurecimiento exterior
        graphics.fill(0, 0, this.width, this.height, 0xB2000000);

        // 2. Chasis del Panel Estilo Grimorio
        graphics.fill(left + 2, top, left + W - 2, top + H, PANEL_TOP);
        graphics.fill(left, top + 2, left + W, top + H - 2, PANEL_TOP);
        graphics.fillGradient(left + 2, top + HEADER_H, left + W - 2, top + H - 2, PANEL_TOP, PANEL_BOTTOM);
        frame(graphics, left, top, W, H, BORDER);

        // 3. Cabecera con degradado
        graphics.fillGradient(left + 1, top + 1, left + W - 1, top + HEADER_H,
                0xFF000000 | lighten(accent, 0.20F), 0xFF140F1C);
        graphics.fill(left + 1, top + HEADER_H, left + W - 1, top + HEADER_H + 1, 0xFF000000 | accent);

        graphics.drawString(this.font, "★", left + 10, top + 7, GOLD, true);
        graphics.drawString(this.font, "CÁPSULA & GLOSARIO", left + 20, top + 7, 0xFFFFFFFF, true);

        // Pestañas Superiores y Cerrar
        drawTabs(graphics, left, top, mouseX, mouseY);
        drawClose(graphics, left, top, mouseX, mouseY);

        // Lomo/Divisor Central del Libro
        graphics.fillGradient(left + 188, top + HEADER_H + 3, left + 189, top + H - 4, 0x10FFFFFF, 0x40FFFFFF);
        graphics.fill(left + 189, top + HEADER_H + 3, left + 190, top + H - 4, 0x30000000);

        // 4. Página Izquierda (Índice y Cuadrícula)
        drawLeftPage(graphics, left, top, mouseX, mouseY);

        // 5. Página Derecha (Ficha, Visores 3D/Pixel Art y Acciones)
        drawRightPage(graphics, left, top, now, accent, mouseX, mouseY);
    }

    /* ── Pestañas Superiores ───────────────────────────────────────────────────────────────── */

    private void drawTabs(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        drawTab(graphics, left + TAB_CAPSULE_X, top + TAB_Y, TAB_W, TAB_H,
                "CÁPSULA (" + (this.menu.storedWaifus() + this.menu.fallenWaifus()) + ")",
                this.activeTab == WaifuStorageMenu.TAB_CAPSULE, PINK_ACCENT, mouseX, mouseY);

        drawTab(graphics, left + TAB_GLOSSARY_X, top + TAB_Y, TAB_W, TAB_H,
                "GLOSARIO (" + this.menu.ownedWaifus() + "/" + WaifuRoster.ROTATION.size() + ")",
                this.activeTab == WaifuStorageMenu.TAB_GLOSSARY, BLUE_ACCENT, mouseX, mouseY);
    }

    private void drawTab(GuiGraphics graphics, int x, int y, int w, int h, String label,
                         boolean active, int accent, int mouseX, int mouseY) {
        boolean hover = inRect(mouseX, mouseY, x, y, w, h);
        int bg = active ? mix(0xFF261D36, accent, 0.35F) : (hover ? 0xFF1C1628 : 0xFF120E1A);
        graphics.fill(x, y, x + w, y + h, bg);

        if (active) {
            graphics.fill(x, y, x + w, y + 2, 0xFF000000 | accent);
            frame(graphics, x, y, w, h, 0xFF000000 | lighten(accent, 0.25F));
        } else {
            frame(graphics, x, y, w, h, BORDER_DARK);
        }

        int textCol = active ? 0xFFFFFFFF : (hover ? TEXT : TEXT_DIM);
        graphics.drawString(this.font, label, x + (w - this.font.width(label)) / 2, y + 4, textCol, active);
    }

    private void drawClose(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        int x = left + CLOSE_X;
        int y = top + TAB_Y + 1;
        boolean hover = inRect(mouseX, mouseY, x, y, 14, 14);
        graphics.fill(x, y, x + 14, y + 14, hover ? 0x60FF5A6E : 0x20000000);
        frame(graphics, x, y, 14, 14, hover ? RED : BORDER_DARK);
        graphics.drawString(this.font, "✕", x + 4, y + 3, hover ? RED : TEXT_DIM, false);
    }

    /* ── Página Izquierda: Catálogo y Cuadrícula ───────────────────────────────────────────── */

    private void drawLeftPage(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        List<WaifuRoster.Entry> entries = getActiveList();
        int total = entries.size();
        int maxPages = Math.max(1, (total + PER_PAGE - 1) / PER_PAGE);
        this.page = Mth.clamp(this.page, 0, maxPages - 1);

        // Subfiltros del Glosario
        if (this.activeTab == WaifuStorageMenu.TAB_GLOSSARY) {
            drawSubFilter(graphics, left + 14, top + 27, 48, 13, "TODAS", this.glossaryFilter == FILTER_ALL, mouseX, mouseY);
            drawSubFilter(graphics, left + 66, top + 27, 54, 13, "OBTENIDAS", this.glossaryFilter == FILTER_OWNED, mouseX, mouseY);
            drawSubFilter(graphics, left + 124, top + 27, 54, 13, "FALTANTES", this.glossaryFilter == FILTER_MISSING, mouseX, mouseY);
        } else {
            graphics.drawString(this.font, "§dWAIFUS EN TU CÁPSULA", left + 16, top + 30, TEXT, true);
        }

        // Si la cápsula está vacía
        if (this.activeTab == WaifuStorageMenu.TAB_CAPSULE && total == 0) {
            int cx = left + 16;
            int cy = top + 64;
            graphics.fill(cx, cy, cx + 158, cy + 96, 0x40100C18);
            frame(graphics, cx, cy, 158, 96, BORDER);
            graphics.drawString(this.font, "§e★ CÁPSULA VACÍA ★", cx + 22, cy + 12, GOLD, true);
            graphics.drawString(this.font, "§7Guarda a tu waifu con:", cx + 18, cy + 30, TEXT_DIM, false);
            graphics.drawString(this.font, "§fShift + Clic derecho", cx + 18, cy + 42, 0xFFFFFF, false);
            graphics.drawString(this.font, "§7con la cápsula en mano.", cx + 18, cy + 54, TEXT_DIM, false);
            graphics.drawString(this.font, "§a¡Quedará a salvo aquí!", cx + 18, cy + 72, GREEN, false);
            return;
        }

        // Dibujo de la cuadrícula (5 x 4 = 20 celdas)
        int startIdx = this.page * PER_PAGE;
        int endIdx = Math.min(startIdx + PER_PAGE, total);

        for (int i = 0; i < PER_PAGE; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cellX = left + GRID_START_X + col * (CELL_SIZE + 3);
            int cellY = top + GRID_START_Y + row * (CELL_SIZE + 2);

            int entryIdx = startIdx + i;
            if (entryIdx < endIdx) {
                WaifuRoster.Entry entry = entries.get(entryIdx);
                int rotationIndex = WaifuRoster.ROTATION.indexOf(entry);
                boolean isSelected = this.selectedEntry != null && this.selectedEntry.id().equals(entry.id());
                boolean isOwned = this.menu.isOwned(rotationIndex);
                boolean isStored = this.menu.isStored(rotationIndex);
                boolean isFallen = this.menu.isFallen(rotationIndex);

                boolean hover = inRect(mouseX, mouseY, cellX, cellY, CELL_SIZE, CELL_SIZE);

                // Fondo de la celda
                int cellBg = isSelected ? mix(SLOT_BG, accentColor(), 0.50F) : (hover ? 0xFF221A30 : SLOT_BG);
                graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, cellBg);

                // Borde
                if (isSelected) {
                    frame(graphics, cellX, cellY, CELL_SIZE, CELL_SIZE, 0xFF000000 | accentColor());
                    corner(graphics, cellX, cellY, CELL_SIZE, CELL_SIZE, 0xFFFFFFFF, 4);
                } else {
                    frame(graphics, cellX, cellY, CELL_SIZE, CELL_SIZE, hover ? BORDER : BORDER_DARK);
                }

                // Render del token
                ItemStack tokenStack = new ItemStack(entry.token().get());
                if (!isOwned && this.activeTab == WaifuStorageMenu.TAB_GLOSSARY) {
                    // Silueta oscurecida para waifus bloqueadas
                    graphics.renderItem(tokenStack, cellX + 6, cellY + 6);
                    graphics.fill(cellX + 2, cellY + 2, cellX + CELL_SIZE - 2, cellY + CELL_SIZE - 2, 0xB0000000);
                    graphics.drawString(this.font, "🔒", cellX + 10, cellY + 10, 0xFF888888, true);
                } else {
                    graphics.renderItem(tokenStack, cellX + 6, cellY + 6);

                    // Indicador de estado en esquina
                    if (isFallen) {
                        graphics.drawString(this.font, "§c✖", cellX + CELL_SIZE - 8, cellY + 2, RED, true);
                    } else if (isStored) {
                        graphics.drawString(this.font, "§a✔", cellX + CELL_SIZE - 8, cellY + 2, GREEN, true);
                    }
                }
            } else {
                // Hueco vacío
                graphics.fill(cellX, cellY, cellX + CELL_SIZE, cellY + CELL_SIZE, 0x20000000);
                frame(graphics, cellX, cellY, CELL_SIZE, CELL_SIZE, 0x301E1728);
            }
        }

        // Barra inferior de paginación
        int navY = top + 172;
        boolean canPrev = this.page > 0;
        boolean canNext = this.page < maxPages - 1;

        pillButton(graphics, left + 20, navY, 20, 14, "«", canPrev, accentColor(), mouseX, mouseY);
        pillButton(graphics, left + 148, navY, 20, 14, "»", canNext, accentColor(), mouseX, mouseY);

        String pageText = "Pág. " + (this.page + 1) + " / " + maxPages;
        graphics.drawString(this.font, pageText,
                left + 20 + (148 - 20 - this.font.width(pageText)) / 2 + 10, navY + 3, TEXT, false);

        // Resumen
        int footY = top + 196;
        if (this.activeTab == WaifuStorageMenu.TAB_CAPSULE) {
            String stats = "§a" + this.menu.storedWaifus() + " vivas §7· §c" + this.menu.fallenWaifus() + " caídas";
            graphics.drawString(this.font, Component.literal(stats), left + 24, footY, 0xFFFFFF, false);
        } else {
            int pct = (this.menu.ownedWaifus() * 100) / Math.max(1, WaifuRoster.ROTATION.size());
            String stats = "§6" + this.menu.ownedWaifus() + " / " + WaifuRoster.ROTATION.size() + " (" + pct + "%)";
            graphics.drawString(this.font, Component.literal(stats), left + 24, footY, 0xFFFFFF, false);
        }
    }

    private void drawSubFilter(GuiGraphics graphics, int x, int y, int w, int h, String text,
                               boolean active, int mouseX, int mouseY) {
        boolean hover = inRect(mouseX, mouseY, x, y, w, h);
        int bg = active ? 0xFF35264E : (hover ? 0xFF241B33 : 0xFF140F20);
        graphics.fill(x, y, x + w, y + h, bg);
        frame(graphics, x, y, w, h, active ? BLUE_ACCENT : (hover ? BORDER : BORDER_DARK));
        int textCol = active ? 0xFFFFFFFF : (hover ? TEXT : TEXT_DIM);
        graphics.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + 3, textCol, false);
    }

    /* ── Página Derecha: Ficha y Visores Multimedia ────────────────────────────────────────── */

    private void drawRightPage(GuiGraphics graphics, int left, int top, long now, int accent, int mouseX, int mouseY) {
        if (this.selectedEntry == null) {
            return;
        }

        WaifuRoster.Entry entry = this.selectedEntry;
        int rotationIndex = WaifuRoster.ROTATION.indexOf(entry);

        boolean isOwned = rotationIndex >= 0 && this.menu.isOwned(rotationIndex);
        boolean isStored = rotationIndex >= 0 && this.menu.isStored(rotationIndex);
        boolean isFallen = rotationIndex >= 0 && this.menu.isFallen(rotationIndex);
        boolean isActive = rotationIndex >= 0 && this.menu.isActiveInWorld(rotationIndex);

        WaifuColor waifuColor = WaifuColors.of(entry.id());

        // Cabecera: Nombre e Insignias
        int nameY = top + 26;
        ChatFormatting nameFormat = waifuColor == null ? ChatFormatting.WHITE : waifuColor.format();
        graphics.drawString(this.font, Component.literal(entry.name()).withStyle(nameFormat, ChatFormatting.BOLD),
                left + 198, nameY, 0xFFFFFFFF, true);

        // Insignia de Color
        if (waifuColor != null) {
            String colName = waifuColor.name();
            int badgeW = this.font.width(colName) + 8;
            int badgeX = left + W - 18 - badgeW;
            graphics.fill(badgeX, nameY - 1, badgeX + badgeW, nameY + 9, 0xFF000000 | darken(waifuColor.rgb(), 0.5F));
            frame(graphics, badgeX, nameY - 1, badgeW, 10, 0xFF000000 | waifuColor.rgb());
            graphics.drawString(this.font, colName, badgeX + 4, nameY, 0xFFFFFFFF, false);
        }

        // Selector de Visor: [ 3D ] vs [ Pixel Art 128px ]
        int viewTabY = top + 40;
        drawViewToggle(graphics, left + FRAME_X, viewTabY, 52, 12, "3D VIVO", this.viewMode == VIEW_3D, accent, mouseX, mouseY);
        drawViewToggle(graphics, left + FRAME_X + 56, viewTabY, 72, 12, "ARTE 128px", this.viewMode == VIEW_PIXEL_ART, accent, mouseX, mouseY);

        // Marco Multimedia 128x128
        int fx = left + FRAME_X;
        int fy = top + FRAME_Y;

        graphics.fill(fx, fy, fx + FRAME_W, fy + FRAME_H, 0xFF0F0B17);
        frame(graphics, fx, fy, FRAME_W, FRAME_H, BORDER);
        corner(graphics, fx, fy, FRAME_W, FRAME_H, 0xFF000000 | accent, 8);

        if (this.viewMode == VIEW_3D) {
            // Fondo con sombra circular en el suelo
            graphics.fill(fx + 30, fy + FRAME_H - 14, fx + FRAME_W - 30, fy + FRAME_H - 6, 0x40000000);
            float idleAngle = WaifuPreviewRenderer.idleAngle(now);
            WaifuPreviewRenderer.render(graphics, entry, fx, fy, fx + FRAME_W, fy + FRAME_H, idleAngle);
        } else {
            // Vista Pixel Art 128x128
            drawPixelArtViewport(graphics, fx, fy, entry, accent);
        }

        // Botón de Acción / Estado Contextual
        int actionY = top + 190;
        int actionW = FRAME_W;
        int actionX = fx;

        if (isStored) {
            pillButton(graphics, actionX, actionY, actionW, 18, "✔ INVOCAR AHORA", true, GREEN, mouseX, mouseY);
        } else if (isFallen) {
            int diamonds = this.menu.diamondCount();
            boolean canRevive = diamonds >= WaifuStorageMenu.REVIVE_COST;
            String label = canRevive ? "❤ REVIVIR (4 💎)" : "FALTAN 💎 (" + diamonds + "/4)";
            pillButton(graphics, actionX, actionY, actionW, 18, label, canRevive, RED, mouseX, mouseY);
        } else if (isActive) {
            drawStatusPill(graphics, actionX, actionY, actionW, 18, "★ ACTIVA EN EL MUNDO", GOLD);
        } else if (isOwned) {
            drawStatusPill(graphics, actionX, actionY, actionW, 18, "❖ EN TU INVENTARIO", BLUE_ACCENT);
        } else {
            drawStatusPill(graphics, actionX, actionY, actionW, 18, "🔒 EN EL GACHA / SALVAJE", TEXT_DIM);
        }
    }

    private void drawViewToggle(GuiGraphics graphics, int x, int y, int w, int h, String text,
                                boolean active, int accent, int mouseX, int mouseY) {
        boolean hover = inRect(mouseX, mouseY, x, y, w, h);
        int bg = active ? mix(0xFF261D36, accent, 0.40F) : (hover ? 0xFF1C1628 : 0xFF120E1A);
        graphics.fill(x, y, x + w, y + h, bg);
        frame(graphics, x, y, w, h, active ? 0xFF000000 | accent : BORDER_DARK);
        int col = active ? 0xFFFFFFFF : (hover ? TEXT : TEXT_DIM);
        graphics.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + 2, col, false);
    }

    private void drawPixelArtViewport(GuiGraphics graphics, int fx, int fy, WaifuRoster.Entry entry, int accent) {
        ResourceLocation artLoc = ResourceLocation.fromNamespaceAndPath("gachawaifus", "textures/gui/art/" + entry.id() + ".png");

        if (Minecraft.getInstance().getResourceManager().getResource(artLoc).isPresent()) {
            graphics.blit(artLoc, fx, fy, 0.0F, 0.0F, FRAME_W, FRAME_H, FRAME_W, FRAME_H);
        } else {
            // Placeholder estilizado tipo cuadrícula de lienzo 128x128 px
            for (int gx = 0; gx < FRAME_W; gx += 16) {
                graphics.fill(fx + gx, fy, fx + gx + 1, fy + FRAME_H, 0x14FFFFFF);
            }
            for (int gy = 0; gy < FRAME_H; gy += 16) {
                graphics.fill(fx, fy + gy, fx + FRAME_W, fy + gy + 1, 0x14FFFFFF);
            }

            // Mira central y token
            graphics.renderItem(new ItemStack(entry.token().get()), fx + 56, fy + 36);

            String s1 = "128 × 128 px";
            String s2 = "textures/gui/art/";
            String s3 = entry.id() + ".png";

            graphics.drawString(this.font, s1, fx + (FRAME_W - this.font.width(s1)) / 2, fy + 64, GOLD, false);
            graphics.drawString(this.font, s2, fx + (FRAME_W - this.font.width(s2)) / 2, fy + 78, TEXT_DIM, false);
            graphics.drawString(this.font, s3, fx + (FRAME_W - this.font.width(s3)) / 2, fy + 90, 0xFFFFFFFF, false);
        }
    }

    private void drawStatusPill(GuiGraphics graphics, int x, int y, int w, int h, String text, int color) {
        graphics.fill(x, y, x + w, y + h, 0x30000000);
        frame(graphics, x, y, w, h, 0xFF000000 | darken(color, 0.4F));
        graphics.drawString(this.font, text, x + (w - this.font.width(text)) / 2, y + 5, color, false);
    }

    /* ── Tooltips Flotantes ────────────────────────────────────────────────────────────────── */

    private void renderCustomTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // Tooltip del marco multimedia o cabecera derecha
        if (this.selectedEntry != null && inRect(mouseX, mouseY, left + FRAME_X, top + FRAME_Y, FRAME_W, FRAME_H)) {
            List<Component> tip = new ArrayList<>();
            tip.add(Component.literal(this.selectedEntry.name()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

            WaifuColor wc = WaifuColors.of(this.selectedEntry.id());
            if (wc != null) {
                tip.add(Component.literal("Clasificación: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(wc.name()).withStyle(wc.format())));
            }

            tip.add(Component.translatable("item.gachawaifus." + this.selectedEntry.id() + "_token.desc")
                    .withStyle(ChatFormatting.DARK_GRAY));

            graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
            return;
        }

        // Tooltips de las celdas de la cuadrícula
        List<WaifuRoster.Entry> entries = getActiveList();
        int total = entries.size();
        int startIdx = this.page * PER_PAGE;
        int endIdx = Math.min(startIdx + PER_PAGE, total);

        for (int i = 0; i < PER_PAGE; i++) {
            int entryIdx = startIdx + i;
            if (entryIdx >= endIdx) break;

            int col = i % COLS;
            int row = i / COLS;
            int cellX = left + GRID_START_X + col * (CELL_SIZE + 3);
            int cellY = top + GRID_START_Y + row * (CELL_SIZE + 2);

            if (inRect(mouseX, mouseY, cellX, cellY, CELL_SIZE, CELL_SIZE)) {
                WaifuRoster.Entry e = entries.get(entryIdx);
                int rIdx = WaifuRoster.ROTATION.indexOf(e);
                boolean owned = this.menu.isOwned(rIdx);
                boolean stored = this.menu.isStored(rIdx);
                boolean fallen = this.menu.isFallen(rIdx);

                List<Component> tip = new ArrayList<>();
                tip.add(Component.literal(e.name()).withStyle(ChatFormatting.WHITE));

                if (fallen) {
                    tip.add(Component.literal("§c✖ Caída en combate (Revivir en Cápsula)"));
                } else if (stored) {
                    tip.add(Component.literal("§a✔ Almacenada en Cápsula (Lista para invocar)"));
                } else if (owned) {
                    tip.add(Component.literal("§6★ Desbloqueada"));
                } else {
                    tip.add(Component.literal("§7🔒 Bloqueada"));
                }

                graphics.renderComponentTooltip(this.font, tip, mouseX, mouseY);
                return;
            }
        }
    }

    /* ── Manejo de Clics e Interacciones ───────────────────────────────────────────────────── */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int left = this.leftPos;
        int top = this.topPos;

        // Botón Cerrar
        if (inRect(mouseX, mouseY, left + CLOSE_X, top + TAB_Y + 1, 14, 14)) {
            playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
            this.onClose();
            return true;
        }

        // Pestaña Cápsula
        if (inRect(mouseX, mouseY, left + TAB_CAPSULE_X, top + TAB_Y, TAB_W, TAB_H)) {
            if (this.activeTab != WaifuStorageMenu.TAB_CAPSULE) {
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.2F);
                this.activeTab = WaifuStorageMenu.TAB_CAPSULE;
                this.page = 0;
                sendMenuAction(WaifuStorageMenu.BUTTON_TAB_CAPSULE);
                ensureInitialSelection();
            }
            return true;
        }

        // Pestaña Glosario
        if (inRect(mouseX, mouseY, left + TAB_GLOSSARY_X, top + TAB_Y, TAB_W, TAB_H)) {
            if (this.activeTab != WaifuStorageMenu.TAB_GLOSSARY) {
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.2F);
                this.activeTab = WaifuStorageMenu.TAB_GLOSSARY;
                this.page = 0;
                sendMenuAction(WaifuStorageMenu.BUTTON_TAB_GLOSSARY);
                ensureInitialSelection();
            }
            return true;
        }

        // Subfiltros del Glosario
        if (this.activeTab == WaifuStorageMenu.TAB_GLOSSARY) {
            if (inRect(mouseX, mouseY, left + 14, top + 27, 48, 13)) {
                this.glossaryFilter = FILTER_ALL;
                this.page = 0;
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                return true;
            }
            if (inRect(mouseX, mouseY, left + 66, top + 27, 54, 13)) {
                this.glossaryFilter = FILTER_OWNED;
                this.page = 0;
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                return true;
            }
            if (inRect(mouseX, mouseY, left + 124, top + 27, 54, 13)) {
                this.glossaryFilter = FILTER_MISSING;
                this.page = 0;
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                return true;
            }
        }

        // Paginación Izquierda
        int navY = top + 172;
        List<WaifuRoster.Entry> entries = getActiveList();
        int maxPages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);

        if (inRect(mouseX, mouseY, left + 20, navY, 20, 14) && this.page > 0) {
            this.page--;
            playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
            return true;
        }
        if (inRect(mouseX, mouseY, left + 148, navY, 20, 14) && this.page < maxPages - 1) {
            this.page++;
            playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
            return true;
        }

        // Selección en la cuadrícula
        int startIdx = this.page * PER_PAGE;
        int endIdx = Math.min(startIdx + PER_PAGE, entries.size());

        for (int i = 0; i < PER_PAGE; i++) {
            int entryIdx = startIdx + i;
            if (entryIdx >= endIdx) break;

            int col = i % COLS;
            int row = i / COLS;
            int cellX = left + GRID_START_X + col * (CELL_SIZE + 3);
            int cellY = top + GRID_START_Y + row * (CELL_SIZE + 2);

            if (inRect(mouseX, mouseY, cellX, cellY, CELL_SIZE, CELL_SIZE)) {
                WaifuRoster.Entry clicked = entries.get(entryIdx);
                long now = Util.getMillis();

                // Doble clic para invocar o revivir rápidamente
                if (clicked.equals(this.lastClickedEntry) && now - this.lastClickTime < 350L) {
                    triggerContextAction(clicked);
                } else {
                    this.selectedEntry = clicked;
                    this.lastClickedEntry = clicked;
                    this.lastClickTime = now;
                    playUi(SoundEvents.UI_BUTTON_CLICK, 1.1F);
                }
                return true;
            }
        }

        // Conmutador de Visor: 3D vs Pixel Art
        int viewTabY = top + 40;
        if (inRect(mouseX, mouseY, left + FRAME_X, viewTabY, 52, 12)) {
            this.viewMode = VIEW_3D;
            playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
            return true;
        }
        if (inRect(mouseX, mouseY, left + FRAME_X + 56, viewTabY, 72, 12)) {
            this.viewMode = VIEW_PIXEL_ART;
            playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
            return true;
        }

        // Botón de Acción Principal (Invocar / Revivir)
        int actionY = top + 190;
        if (this.selectedEntry != null && inRect(mouseX, mouseY, left + FRAME_X, actionY, FRAME_W, 18)) {
            triggerContextAction(this.selectedEntry);
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void triggerContextAction(WaifuRoster.Entry entry) {
        int rIdx = WaifuRoster.ROTATION.indexOf(entry);
        if (rIdx < 0) return;

        if (this.menu.isStored(rIdx)) {
            playUi(SoundEvents.PLAYER_LEVELUP, 1.2F);
            sendMenuAction(WaifuStorageMenu.BUTTON_SUMMON_BASE + rIdx);
        } else if (this.menu.isFallen(rIdx)) {
            if (this.menu.diamondCount() >= WaifuStorageMenu.REVIVE_COST) {
                playUi(SoundEvents.AMETHYST_BLOCK_CHIME, 1.2F);
                sendMenuAction(WaifuStorageMenu.BUTTON_REVIVE_BASE + rIdx);
            } else {
                playUi(SoundEvents.VILLAGER_NO, 1.0F);
            }
        }
    }

    private void sendMenuAction(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDirection, double scrollAmount) {
        List<WaifuRoster.Entry> entries = getActiveList();
        int maxPages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);

        if (maxPages > 1 && scrollDirection != 0.0) {
            if (scrollDirection > 0 && this.page > 0) {
                this.page--;
                playUi(SoundEvents.UI_BUTTON_CLICK, 0.9F);
                return true;
            } else if (scrollDirection < 0 && this.page < maxPages - 1) {
                this.page++;
                playUi(SoundEvents.UI_BUTTON_CLICK, 1.0F);
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDirection, scrollAmount);
    }

    /* ── Utilidades de Dibujo ──────────────────────────────────────────────────────────────── */

    private void playUi(net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, float pitch) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
        }
    }

    private void playUi(net.minecraft.sounds.SoundEvent sound, float pitch) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
        }
    }

    private static boolean inRect(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void pillButton(GuiGraphics graphics, int x, int y, int w, int h, String label,
                            boolean enabled, int accent, int mouseX, int mouseY) {
        boolean hover = enabled && inRect(mouseX, mouseY, x, y, w, h);
        int base = enabled ? mix(0xFF1D1628, accent, hover ? 0.55F : 0.35F) : 0xFF161020;
        graphics.fill(x, y, x + w, y + h, base);
        graphics.fill(x, y, x + w, y + 1, enabled ? 0xFF000000 | lighten(accent, hover ? 0.5F : 0.25F) : 0xFF2A2238);
        graphics.fill(x, y + h - 1, x + w, y + h, BORDER_DARK);
        if (enabled) {
            graphics.fill(x, y, x + 1, y + h, 0xFF000000 | accent);
            graphics.fill(x + w - 1, y, x + w, y + h, 0xFF000000 | accent);
        }
        int textColor = enabled ? 0xFFFFFFFF : 0xFF6B6180;
        graphics.drawString(this.font, label, x + (w - this.font.width(label)) / 2, y + (h - 8) / 2 + 1, textColor, enabled);
    }

    private static void frame(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void corner(GuiGraphics graphics, int x, int y, int w, int h, int color, int len) {
        graphics.fill(x, y, x + len, y + 1, color);
        graphics.fill(x, y, x + 1, y + len, color);
        graphics.fill(x + w - len, y, x + w, y + 1, color);
        graphics.fill(x + w - 1, y, x + w, y + len, color);
        graphics.fill(x, y + h - 1, x + len, y + h, color);
        graphics.fill(x, y + h - len, x + 1, y + h, color);
        graphics.fill(x + w - len, y + h - 1, x + w, y + h, color);
        graphics.fill(x + w - 1, y + h - len, x + w, y + h, color);
    }

    private static int mix(int colorA, int colorB, float amount) {
        int r = (int) (((colorA >> 16) & 0xFF) * (1 - amount) + ((colorB >> 16) & 0xFF) * amount);
        int g = (int) (((colorA >> 8) & 0xFF) * (1 - amount) + ((colorB >> 8) & 0xFF) * amount);
        int b = (int) ((colorA & 0xFF) * (1 - amount) + (colorB & 0xFF) * amount);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int lighten(int rgb, float amount) {
        return mix(rgb, 0xFFFFFF, amount);
    }

    private static int darken(int rgb, float amount) {
        return mix(rgb, 0x000000, amount);
    }
}
