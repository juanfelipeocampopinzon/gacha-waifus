package com.gachawaifus.client;

import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.gacha.GachaService;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.menu.GachaMenu;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * La cara del gacha: un banner con dos pestañas (promocional y permanente).
 *
 * <p>Todo está dibujado a mano (paneles, degradados, barras y halos) para no depender de texturas:
 * el mod no tiene {@code textures/gui/} y así la pantalla no se rompe si falta un PNG. El acento
 * sale del color de la waifu que enseña el banner ({@code WaifuColors}).
 *
 * <p>El servidor manda: los botones solo piden la tirada ({@code handleInventoryButtonClick}) y la
 * pantalla pinta lo que llega por {@link GachaMenu}: pestaña, pity, bolitas y los 10 huecos con el
 * resultado de la última tirada. El revelado escalonado y el destello de 5★ son puramente visuales.
 */
public class GachaBannerScreen extends AbstractContainerScreen<GachaMenu> {

    /* ── Geometría (todo relativo a la esquina del panel) ──────────────────────────────────── */
    private static final int W = 340;
    private static final int H = 214;

    private static final int HEADER_H = 22;

    private static final int PORTRAIT_X = 10;
    private static final int PORTRAIT_Y = 28;
    private static final int PORTRAIT_W = 116;
    private static final int PORTRAIT_H = 136;

    private static final int RIGHT_X = 134;
    private static final int RIGHT_R = W - 16; // 324

    private static final int TAB_W = 76;
    private static final int TAB_H = 16;
    private static final int TAB_Y = 4;
    private static final int TAB_PROMO_X = 140;
    private static final int TAB_STANDARD_X = 220;
    private static final int CLOSE_X = 302;

    private static final int BUTTON_Y = 148;
    private static final int BUTTON_H = 18;
    private static final int PULL1_X = 134;
    private static final int PULL1_W = 72;
    private static final int PULL10_X = 212;
    private static final int PULL10_W = 86;

    private static final int POOL_ICON_Y = 98;
    private static final int POOL_ICON_STEP = 22;

    /* ── Paleta ──────────────────────────────────────────────────────────────────────────── */
    private static final int PANEL_TOP = 0xFF1A1424;
    private static final int PANEL_BOTTOM = 0xFF0C0912;
    private static final int BORDER = 0xFF463A5C;
    private static final int BORDER_DARK = 0xFF0E0A14;
    private static final int SLOT_BG = 0xFF15101E;
    private static final int TEXT = 0xFFEDE7F5;
    private static final int TEXT_DIM = 0xFF9A8FAD;
    private static final int GOLD = 0xFFFFD86B;
    private static final int GREEN = 0xFF6BE08A;
    private static final int PINK_ACCENT = 0xFFFF6FB5;
    private static final int BLUE_ACCENT = 0xFF62C8FF;

    /* ── Ritmo de la animación ──────────────────────────────────────────────────────────── */
    /** Carga mínima antes de destapar nada: si el servidor contesta en 1 tick no se ve un salto. */
    private static final long CHARGE_MS = 650L;
    /** Si el servidor no manda resultado en este tiempo, la tirada no salió (sin bolitas…). */
    private static final long CHARGE_TIMEOUT_MS = 2500L;
    private static final long REVEAL_STEP_MS = 85L;
    /** Cuánto dura el "pop" de una carta al destaparse. */
    private static final long CARD_POP_MS = 160L;
    private static final long FIVE_STAR_MS = 2800L;
    private static final long FOUR_STAR_MS = 1100L;

    /* ── Estados ────────────────────────────────────────────────────────────────────────── */
    private static final int ST_IDLE = 0;
    private static final int ST_CHARGE = 1;
    private static final int ST_REVEAL = 2;

    private int state = ST_IDLE;
    private long stateStart;
    private long revealStart;
    private int lastSerial;

    /** Inicio de la secuencia de 5★/4★ (0 = no hay ninguna en curso). */
    private long fiveStart;
    private long fourStart;
    private boolean raritySoundDone;
    /** Chispas del 5★: {vx, vy, tamaño, fase}. Se generan una vez por tirada. */
    private final List<float[]> sparks = new ArrayList<>();

    public GachaBannerScreen(GachaMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable("gachawaifus.gacha.title"));
        this.imageWidth = W;
        this.imageHeight = H;
    }

    @Override
    protected void init() {
        super.init();
        this.lastSerial = this.menu.serial();
        this.state = ST_IDLE;
        this.stateStart = 0L;
        this.revealStart = 0L;
        this.fiveStart = 0L;
        this.fourStart = 0L;
        this.raritySoundDone = false;
        this.sparks.clear();
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

    /* ══ Estado derivado ══════════════════════════════════════════════════════════════════ */

    private boolean standardTab() {
        return this.menu.tab() == 1;
    }

    private int accent() {
        WaifuRoster.Entry spot = this.menu.spotlight();
        WaifuColor color = spot == null ? null : WaifuColors.of(spot.id());
        int raw = color == null ? TEXT_DIM : color.rgb();
        return standardTab() ? mix(raw, BLUE_ACCENT, 0.35F) : mix(raw, PINK_ACCENT, 0.35F);
    }

    private static int accentFor(int tab) {
        return tab == 0 ? PINK_ACCENT : BLUE_ACCENT;
    }

    /* ══ Dibujo ═══════════════════════════════════════════════════════════════════════════ */

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        long now = Util.getMillis();
        int accent = accent();

        // Velo de toda la pantalla: la caja resalta sin marear lo de detrás.
        graphics.fill(0, 0, this.width, this.height, 0xB2000000);

        // Cuerpo del panel (esquinas rebajadas con dos rectángulos).
        graphics.fill(left + 2, top, left + W - 2, top + H, PANEL_TOP);
        graphics.fill(left, top + 2, left + W, top + H - 2, PANEL_TOP);
        graphics.fillGradient(left + 2, top + HEADER_H, left + W - 2, top + H - 2,
                PANEL_TOP, PANEL_BOTTOM);
        frame(graphics, left, top, W, H, BORDER);

        // Cabecera con el color del banner.
        graphics.fillGradient(left + 1, top + 1, left + W - 1, top + HEADER_H,
                0xFF000000 | lighten(accent, 0.18F), 0xFF140F1C);
        graphics.fill(left + 1, top + HEADER_H, left + W - 1, top + HEADER_H + 1,
                0xFF000000 | accent);
        graphics.drawString(this.font, "★", left + 10, top + 7, GOLD, true);
        graphics.drawString(this.font, "GACHA WAIFUS", left + 20, top + 7, 0xFFFFFFFF, true);

        drawTabs(graphics, left, top, mouseX, mouseY);
        drawClose(graphics, left, top, mouseX, mouseY);

        drawPortrait(graphics, now, accent);
        if (standardTab()) {
            drawStandardBody(graphics, accent, mouseX, mouseY);
        } else {
            drawPromoBody(graphics, accent, now, mouseX, mouseY);
        }

        // Separador y tira de resultados.
        graphics.fill(left + 10, top + 170, left + W - 10, top + 171, 0x60FFFFFF);
        graphics.drawString(this.font, Component.translatable("gachawaifus.gacha.result.title"),
                left + 12, top + 174, TEXT_DIM, false);
        drawResultSlots(graphics, left, top);
    }

    /** Cabecera: pestañas de banner y botón de cerrar. */
    private void drawTabs(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        for (int tab = 0; tab < 2; tab++) {
            boolean active = this.menu.tab() == tab;
            boolean hover = inRect(mouseX, mouseY, left + tabX(tab), top + TAB_Y, TAB_W, TAB_H);
            int accent = accentFor(tab);
            int bg = active ? mix(0xFF1A1424, accent, 0.45F)
                    : (hover ? 0xFF2A2238 : 0xFF161020);
            graphics.fill(left + tabX(tab), top + TAB_Y, left + tabX(tab) + TAB_W, top + TAB_Y + TAB_H, bg);
            if (active) {
                graphics.fill(left + tabX(tab), top + TAB_Y + TAB_H - 2,
                        left + tabX(tab) + TAB_W, top + TAB_Y + TAB_H, 0xFF000000 | accent);
            }
            String key = tab == 0 ? "gachawaifus.gacha.tab.promo" : "gachawaifus.gacha.tab.standard";
            String label = Component.translatable(key).getString();
            graphics.drawString(this.font, label,
                    left + tabX(tab) + (TAB_W - this.font.width(label)) / 2, top + TAB_Y + 4,
                    active ? 0xFFFFFFFF : TEXT_DIM, false);
        }
    }

    private void drawClose(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        boolean hover = inRect(mouseX, mouseY, left + CLOSE_X, top + TAB_Y, 16, TAB_H);
        graphics.fill(left + CLOSE_X, top + TAB_Y, left + CLOSE_X + 16, top + TAB_Y + TAB_H,
                hover ? 0xFF5A2030 : 0xFF1A1424);
        graphics.drawString(this.font, "x", left + CLOSE_X + 6, top + TAB_Y + 4,
                hover ? 0xFFFFFFFF : TEXT_DIM, false);
    }

    /** Retrato: marco, halo, waifu 3D (o token), insignia de colección y esquinas. */
    private void drawPortrait(GuiGraphics graphics, long now, int accent) {
        int left = this.leftPos;
        int top = this.topPos;
        int x = left + PORTRAIT_X;
        int y = top + PORTRAIT_Y;
        int w = PORTRAIT_W;
        int h = PORTRAIT_H;

        graphics.fill(x, y, x + w, y + h, 0xFF08060C);
        graphics.fillGradient(x, y, x + w, y + h, withAlpha(accent, 0x55), 0x00000000);
        glow(graphics, x + w / 2, y + h / 2, 52, accent);

        WaifuRoster.Entry spot = this.menu.spotlight();
        float angle = WaifuPreviewRenderer.idleAngle(now);
        WaifuPreviewRenderer.render(graphics, spot, x + 1, y + 1, x + w - 1, y + h - 1, angle);

        // Insignia de estado de colección.
        boolean owned = this.menu.spotlightOwned();
        String badge = Component.translatable(owned
                ? "gachawaifus.gacha.owned" : "gachawaifus.gacha.not_owned").getString();
        int bw = this.font.width(badge) + 8;
        int bx = x + 4;
        int by = y + 4;
        graphics.fill(bx, by, bx + bw, by + 12, owned ? 0xD0184028 : 0xD02A1830);
        graphics.fill(bx, by, bx + 1, by + 12, owned ? GREEN : accent);
        graphics.drawString(this.font, badge, bx + 4, by + 3, owned ? GREEN : TEXT_DIM, false);

        // Esquinas del marco.
        corner(graphics, x, y, w, h, 0xFF000000 | accent, 9);
        frame(graphics, x, y, w, h, mix(BORDER, accent, 0.4F));
    }

    /** Contenido del banner PROMOCIONAL: nombre, pity 5★/4★, garantía, bolitas y botones. */
    private void drawPromoBody(GuiGraphics graphics, int accent, long now, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        WaifuRoster.Entry spot = this.menu.spotlight();

        drawName(graphics, spot);

        // Pity 5★
        label(graphics, "gachawaifus.gacha.pity.five", left + RIGHT_X, top + 58, TEXT_DIM);
        String pity = this.menu.pity() + " / " + GachaService.HARD_PITY;
        graphics.drawString(this.font, pity, left + RIGHT_R - this.font.width(pity), top + 58,
                pityColor(this.menu.pity()), true);
        bar(graphics, left + RIGHT_X, top + 70, RIGHT_R - RIGHT_X, this.menu.pity(),
                GachaService.HARD_PITY, accent);

        // Pity 4★
        label(graphics, "gachawaifus.gacha.pity.four", left + RIGHT_X, top + 86, TEXT_DIM);
        String pity4 = this.menu.pity4() + " / " + GachaService.HARD_PITY_FOUR;
        graphics.drawString(this.font, pity4, left + RIGHT_R - this.font.width(pity4), top + 86,
                0xFFFFE08A, true);
        bar(graphics, left + RIGHT_X, top + 98, RIGHT_R - RIGHT_X, this.menu.pity4(),
                GachaService.HARD_PITY_FOUR, 0xFFFFC94B);

        // Garantía
        if (this.menu.guaranteed()) {
            int y = top + 112;
            graphics.fill(left + RIGHT_X, y, left + RIGHT_R, y + 13, 0xFF3A2A12);
            graphics.fill(left + RIGHT_X, y, left + RIGHT_X + 2, y + 13, GOLD);
            String g = Component.translatable("gachawaifus.gacha.guaranteed").getString();
            graphics.drawCenteredString(this.font, g, (left + RIGHT_X + RIGHT_R) / 2, y + 3, GOLD);
        }

        drawBallsAndButtons(graphics, accent, mouseX, mouseY);
    }

    /** Contenido del banner PERMANENTE: nombre, pity, rejilla del pool, bolitas y botones. */
    private void drawStandardBody(GuiGraphics graphics, int accent, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        WaifuRoster.Entry spot = this.menu.spotlight();

        drawName(graphics, spot);

        label(graphics, "gachawaifus.gacha.pity.five", left + RIGHT_X, top + 58, TEXT_DIM);
        String pity = this.menu.standardPity() + " / " + GachaService.HARD_PITY;
        graphics.drawString(this.font, pity, left + RIGHT_R - this.font.width(pity), top + 58,
                pityColor(this.menu.standardPity()), true);
        bar(graphics, left + RIGHT_X, top + 70, RIGHT_R - RIGHT_X, this.menu.standardPity(),
                GachaService.HARD_PITY, accent);

        // Rejilla del pool estándar con marca de poseída.
        int mask = this.menu.standardMask();
        int ownedCount = Integer.bitCount(mask);
        String poolLabel = Component.translatable("gachawaifus.gacha.pool.title").getString()
                + "  " + ownedCount + "/" + WaifuRoster.STANDARD_POOL.size();
        graphics.drawString(this.font, poolLabel, left + RIGHT_X, top + 88, TEXT_DIM, false);

        for (int i = 0; i < WaifuRoster.STANDARD_POOL.size(); i++) {
            WaifuRoster.Entry entry = WaifuRoster.STANDARD_POOL.get(i);
            int x = left + RIGHT_X + i * POOL_ICON_STEP;
            int y = top + POOL_ICON_Y;
            boolean owned = (mask & (1 << i)) != 0;
            graphics.fill(x, y, x + 18, y + 18, SLOT_BG);
            frame(graphics, x, y, 18, 18, owned ? mix(BORDER, GREEN, 0.5F) : BORDER);
            ItemStack token = new ItemStack(entry.token().get());
            graphics.renderItem(token, x + 1, y + 1);
            if (owned) {
                // Marca de "ya la tienes" dibujada píxel a píxel (el glifo ✔ no está en la fuente
                // por defecto de Minecraft y saldría como un cuadrito).
                graphics.fill(x + 12, y + 12, x + 18, y + 18, 0xFF10281A);
                graphics.fill(x + 17, y + 12, x + 18, y + 13, GREEN);
                graphics.fill(x + 16, y + 13, x + 17, y + 14, GREEN);
                graphics.fill(x + 13, y + 14, x + 14, y + 15, GREEN);
                graphics.fill(x + 15, y + 14, x + 16, y + 15, GREEN);
                graphics.fill(x + 14, y + 15, x + 15, y + 16, GREEN);
            }
        }

        drawBallsAndButtons(graphics, accent, mouseX, mouseY);
    }

    private void drawName(GuiGraphics graphics, @Nullable WaifuRoster.Entry spot) {
        int left = this.leftPos;
        int top = this.topPos;
        if (spot == null) {
            return;
        }
        WaifuColor color = WaifuColors.of(spot.id());
        int nameColor = color == null ? TEXT : readable(color.rgb());
        String name = this.font.plainSubstrByWidth(spot.name(), RIGHT_R - RIGHT_X - 4);
        graphics.drawString(this.font, name, left + RIGHT_X, top + 30, nameColor, true);

        String rank = WaifuRoster.isFourStar(spot) ? "★★★★" : "★★★★★";
        int rankColor = WaifuRoster.isFourStar(spot) ? 0xFFFFC94B : GOLD;
        graphics.drawString(this.font, rank, left + RIGHT_X, top + 42, rankColor, true);
        String colorName = color == null ? "" : color.displayName().getString().toUpperCase();
        graphics.drawString(this.font, colorName, left + RIGHT_X + this.font.width(rank) + 6,
                top + 42, TEXT_DIM, false);
    }

    /**
     * Fila de bolitas + los dos botones de tirada, compartida por los dos banners.
     */
    private void drawBallsAndButtons(GuiGraphics graphics, int accent, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        boolean standard = standardTab();
        int balls = standard ? this.menu.blueBalls() : this.menu.pinkBalls();
        ItemStack ballIcon = new ItemStack(GachaService.ballItem(this.menu.banner()));

        int rowY = top + (standard ? 126 : 130);
        graphics.fill(left + RIGHT_X, rowY, left + RIGHT_X + 18, rowY + 18, SLOT_BG);
        frame(graphics, left + RIGHT_X, rowY, 18, 18, BORDER);
        graphics.renderItem(ballIcon, left + RIGHT_X + 1, rowY + 1);
        String ballText = Component.translatable(standard
                ? "gachawaifus.gacha.balls.blue" : "gachawaifus.gacha.balls.pink").getString()
                + "  x" + balls;
        graphics.drawString(this.font, ballText, left + RIGHT_X + 24, rowY + 5,
                balls > 0 ? TEXT : TEXT_DIM, true);

        boolean can1 = this.menu.canPull(1);
        boolean can10 = this.menu.canPull(10);
        pillButton(graphics, left + PULL1_X, top + BUTTON_Y, PULL1_W, BUTTON_H,
                Component.translatable("gachawaifus.gacha.pull.one").getString(), can1, accent, mouseX, mouseY);
        pillButton(graphics, left + PULL10_X, top + BUTTON_Y, PULL10_W, BUTTON_H,
                Component.translatable("gachawaifus.gacha.pull.ten").getString(), can10, accent, mouseX, mouseY);
    }

    /** Marco de cada hueco del resultado + tarjetas de revelado. */
    private void drawResultSlots(GuiGraphics graphics, int left, int top) {
        for (int i = 0; i < GachaMenu.RESULT_SLOTS; i++) {
            int x = left + GachaMenu.RESULT_X + i * GachaMenu.RESULT_STEP;
            int y = top + GachaMenu.RESULT_Y;
            graphics.fill(x, y, x + 16, y + 16, SLOT_BG);
            frame(graphics, x, y, 16, 16, BORDER_DARK);
        }

        String summary;
        if (this.menu.lastFive() == 0 && this.menu.lastFour() == 0 && this.menu.lastFood() == 0) {
            summary = Component.translatable("gachawaifus.gacha.result.none").getString();
        } else {
            summary = Component.translatable("gachawaifus.gacha.result.summary",
                    this.menu.lastFive(), this.menu.lastFour(), this.menu.lastFood()).getString();
        }
        // A la derecha de la rejilla y recortado: si el resumen es largo no debe pisar la carta 10.
        int summaryX = left + GachaMenu.RESULT_X + GachaMenu.RESULT_SLOTS * GachaMenu.RESULT_STEP + 8;
        int summaryWidth = left + RIGHT_R - summaryX;
        graphics.drawString(this.font, this.font.plainSubstrByWidth(summary, summaryWidth),
                summaryX, top + GachaMenu.RESULT_Y + 5,
                this.menu.lastFive() > 0 ? GOLD : TEXT_DIM, true);
    }

    /**
     * El revelado: cada carta sale un pelín después de la anterior. Los huecos vacíos no se tapan
     * y las cartas se pintan encima del icono con un reverso oscuro.
     *
     * <p>Durante la carga ({@link #ST_CHARGE}) se tapa también el resultado <b>anterior</b>: no
     * tiene sentido enseñar la tirada vieja mientras la nueva está en el aire.
     */
    @Override
    protected void renderSlot(GuiGraphics graphics, Slot slot) {
        if (!this.menu.isResultSlot(slot)) {
            super.renderSlot(graphics, slot); // inventario del jugador: sin animación ni marcos
            return;
        }
        if (slot.hasItem() && this.state == ST_CHARGE) {
            drawCardBack(graphics, slot);
            return;
        }
        if (slot.hasItem() && this.state == ST_REVEAL) {
            long elapsed = Util.getMillis() - this.revealStart;
            long stamp = slot.index * REVEAL_STEP_MS;
            if (elapsed < stamp) {
                drawCardBack(graphics, slot);
                return;
            }
            // Pop: la carta entra pequeña y sube 5 px mientras se asienta.
            float age = Mth.clamp((elapsed - stamp) / (float) CARD_POP_MS, 0.0F, 1.0F);
            float scale = 0.55F + 0.45F * easeOut(age);
            float dy = (1.0F - age) * 5.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(slot.x + 8.0F, slot.y + 8.0F, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.pose().translate(-slot.x - 8.0F, -slot.y - 8.0F - dy, 0.0F);
            super.renderSlot(graphics, slot);
            graphics.pose().popPose();
            drawCardBadge(graphics, slot);
            return;
        }
        super.renderSlot(graphics, slot);
    }

    /** Reverso de carta (el "?" oscuro) que tapa el hueco hasta que le toca destaparse. */
    private void drawCardBack(GuiGraphics graphics, Slot slot) {
        int x = slot.x;
        int y = slot.y;
        graphics.fill(x, y, x + 16, y + 16, 0xFF120E18);
        graphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF241B33);
        graphics.drawString(this.font, "?", x + 6, y + 4, TEXT_DIM, false);
    }

    /** Marco de rango, brillo de 5★ y cinta de "nueva" sobre una carta ya destapada. */
    private void drawCardBadge(GuiGraphics graphics, Slot slot) {
        if (!slot.hasItem()) {
            return;
        }
        WaifuRoster.Entry entry = WaifuRoster.byToken(slot.getItem().getItem());
        if (entry != null) {
            boolean four = WaifuRoster.isFourStar(entry);
            int color = four ? 0xFFFFC94B : GOLD;
            if (!four) {
                // Las 5★ laten: un marco exterior que sube y baja de alfa.
                int glow = (int) (90 + 120 * (0.5 + 0.5 * Math.sin(Util.getMillis() / 160.0)));
                frame(graphics, slot.x - 2, slot.y - 2, 20, 20, (glow << 24) | (color & 0xFFFFFF));
            }
            frame(graphics, slot.x - 1, slot.y - 1, 18, 18, 0xFF000000 | color);
        }
        // Cinta de "nueva": una esquina verde en la carta. El detalle va en el tooltip.
        if ((this.menu.newMask() & (1 << slot.index)) != 0) {
            graphics.fill(slot.x + 11, slot.y + 11, slot.x + 16, slot.y + 16, 0xFF123A22);
            graphics.fill(slot.x + 12, slot.y + 12, slot.x + 16, slot.y + 13, GREEN);
            graphics.fill(slot.x + 15, slot.y + 12, slot.x + 16, slot.y + 16, GREEN);
        }
    }

    /** Curva de salida suave (la misma que usan las animaciones de UI de vanilla). */
    private static float easeOut(float t) {
        return 1.0F - (1.0F - t) * (1.0F - t);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Toda la pintura va en renderBg con coordenadas absolutas; la etiqueta por defecto
        // ("Cápsula"/"Inventario") no tiene sentido aquí.
    }

    /* ══ Interacción ══════════════════════════════════════════════════════════════════════ */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            long now = Util.getMillis();
            // Un clic en mitad del 5★ lo salta (nadie quiere esperar 3 s cada vez).
            if (this.fiveStart != 0L && now - this.fiveStart < FIVE_STAR_MS) {
                this.fiveStart = now - FIVE_STAR_MS;
                return true;
            }
            if (this.state != ST_IDLE) {
                return true; // tirada en curso: no se aceptan más clics (ni de pestaña)
            }
            int left = this.leftPos;
            int top = this.topPos;
            if (inRect(mouseX, mouseY, left + TAB_PROMO_X, top + TAB_Y, TAB_W, TAB_H)) {
                sendButton(GachaMenu.BUTTON_TAB_PROMO);
                return true;
            }
            if (inRect(mouseX, mouseY, left + TAB_STANDARD_X, top + TAB_Y, TAB_W, TAB_H)) {
                sendButton(GachaMenu.BUTTON_TAB_STANDARD);
                return true;
            }
            if (inRect(mouseX, mouseY, left + CLOSE_X, top + TAB_Y, 16, TAB_H)) {
                this.onClose();
                return true;
            }
            if (inRect(mouseX, mouseY, left + PULL1_X, top + BUTTON_Y, PULL1_W, BUTTON_H)) {
                if (this.menu.canPull(1)) {
                    beginPull(now);
                    sendButton(standardTab() ? GachaMenu.BUTTON_PULL_1_STANDARD : GachaMenu.BUTTON_PULL_1);
                }
                return true;
            }
            if (inRect(mouseX, mouseY, left + PULL10_X, top + BUTTON_Y, PULL10_W, BUTTON_H)) {
                if (this.menu.canPull(10)) {
                    beginPull(now);
                    sendButton(standardTab() ? GachaMenu.BUTTON_PULL_10_STANDARD : GachaMenu.BUTTON_PULL_10);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Arranca la carga: el servidor ya puede contestar, pero el revelado no empieza antes. */
    private void beginPull(long now) {
        this.state = ST_CHARGE;
        this.stateStart = now;
        this.revealStart = 0L;
        this.fiveStart = 0L;
        this.fourStart = 0L;
        this.raritySoundDone = false;
        playUi(SoundEvents.UI_BUTTON_CLICK, 0.9F);
    }

    private void sendButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    /* ══ Tooltips y animación ═══════════════════════════════════════════════════════════════ */

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        tickAnimation(now);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (this.state == ST_CHARGE) {
            drawCharge(graphics, now);
        }
        if (this.fiveStart != 0L && now - this.fiveStart < FIVE_STAR_MS) {
            drawFiveStar(graphics, now);
        } else if (this.fourStart != 0L && now - this.fourStart < FOUR_STAR_MS) {
            drawFourStar(graphics, now);
        }
        if (!renderCustomTooltip(graphics, mouseX, mouseY)) {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    /**
     * Máquina de estados de la tirada.
     *
     * <p>La pantalla manda el botón y el servidor contesta en el tick siguiente, pero el resultado
     * no se destapa hasta que la carga lleva {@link #CHARGE_MS}. Si el servidor no contesta (sin
     * bolitas, banner completo, cooldown) se vuelve a reposo al de {@link #CHARGE_TIMEOUT_MS} para
     * no quedarse colgado.
     */
    private void tickAnimation(long now) {
        if (this.menu.serial() != this.lastSerial) {
            this.lastSerial = this.menu.serial();
            long ready = this.state == ST_CHARGE ? Math.max(now, this.stateStart + CHARGE_MS) : now;
            this.revealStart = ready;
            this.state = ST_REVEAL;
            playUi(SoundEvents.NOTE_BLOCK_CHIME, 1.7F);
            // La secuencia de rango empieza cuando ya se han destapado todas las cartas.
            long after = ready + revealTotalMs() + 150L;
            if (this.menu.lastFive() > 0) {
                this.fiveStart = after;
                spawnSparks(this.lastSerial);
            } else if (this.menu.lastFour() > 0) {
                this.fourStart = after;
            }
        }
        if (this.state == ST_CHARGE && now - this.stateStart > CHARGE_TIMEOUT_MS) {
            this.state = ST_IDLE; // la tirada no salió: no habrá serial nuevo
        }
        if (this.state == ST_REVEAL && now - this.revealStart > revealTotalMs() + 120L) {
            this.state = ST_IDLE;
        }
        // Sonido de rango, una sola vez y en su momento (no al llegar el resultado).
        if (!this.raritySoundDone && this.fiveStart != 0L && now >= this.fiveStart) {
            this.raritySoundDone = true;
            playUi(SoundEvents.BEACON_ACTIVATE, 1.15F, 0.85F);
            playUi(SoundEvents.PLAYER_LEVELUP, 1.45F, 0.7F);
        } else if (!this.raritySoundDone && this.fourStart != 0L && now >= this.fourStart) {
            this.raritySoundDone = true;
            playUi(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5F, 0.9F);
        }
    }

    private static long revealTotalMs() {
        return GachaMenu.RESULT_SLOTS * REVEAL_STEP_MS;
    }

    /* ══ Animaciones ═════════════════════════════════════════════════════════════════════ */

    /**
     * Carga: el panel se oscurece, un aro de puntos se cierra sobre el retrato y la bolita late en
     * el centro. No hay textura: todo son rectángulos, como el resto de la pantalla.
     */
    private void drawCharge(GuiGraphics graphics, long now) {
        int left = this.leftPos;
        int top = this.topPos;
        long elapsed = now - this.stateStart;
        float t = Mth.clamp(elapsed / (float) CHARGE_MS, 0.0F, 1.0F);
        int accent = accent();

        // Tapa el resultado anterior mientras la tirada está en el aire.
        graphics.fill(left, top, left + W, top + H, 0xE008060E);

        int cx = left + PORTRAIT_X + PORTRAIT_W / 2;
        int cy = top + PORTRAIT_Y + PORTRAIT_H / 2;

        // Aro que se cierra sobre el retrato.
        float radius = 86.0F - 60.0F * t;
        int dots = 14;
        for (int i = 0; i < dots; i++) {
            double angle = elapsed / 260.0D + i * (Math.PI * 2.0D / dots);
            int x = (int) (cx + Math.cos(angle) * radius);
            int y = (int) (cy + Math.sin(angle) * radius);
            int size = 2 + (int) (2.0F * t);
            graphics.fill(x, y, x + size, y + size, 0xFF000000 | mix(accent, GOLD, t));
        }

        // Bolita latiendo.
        double pulse = 0.5D + 0.5D * Math.sin(elapsed / 90.0D);
        int core = (int) (9 + 7 * pulse + 5 * t);
        for (int i = 6; i >= 1; i--) {
            int r = core * i / 6;
            int alpha = 28 + (6 - i) * 9;
            graphics.fill(cx - r, cy - r, cx + r, cy + r, (alpha << 24) | (accent & 0xFFFFFF));
        }
        graphics.fill(cx - 7, cy - 7, cx + 7, cy + 7, 0xFF000000 | lighten(accent, 0.35F));
        graphics.fill(cx - 3, cy - 3, cx + 3, cy + 3, 0xFFFFFFFF);

        // Texto con puntos suspensivos animados.
        String label = Component.translatable("gachawaifus.gacha.pulling").getString();
        int dotsShown = (int) (elapsed / 180L) % 4;
        graphics.drawCenteredString(this.font, label + " " + ".".repeat(dotsShown),
                left + W / 2, top + H / 2 + 56, 0xFFFFFFFF);
    }

    /**
     * Secuencia de 5★: fondo oscuro, rayos girando, chispas con gravedad, sacudida corta y el "5★"
     * que entra grande y se asienta. Se salta con un clic (ver {@link #mouseClicked}).
     */
    private void drawFiveStar(GuiGraphics graphics, long now) {
        long elapsed = now - this.fiveStart;
        float t = Mth.clamp(elapsed / (float) FIVE_STAR_MS, 0.0F, 1.0F);
        int cx = this.width / 2;
        int cy = this.height / 2;

        float backdrop = Math.min(1.0F, t * 8.0F)
                * (1.0F - Mth.clamp((t - 0.86F) / 0.14F, 0.0F, 1.0F));
        graphics.fill(0, 0, this.width, this.height, ((int) (0xD8 * backdrop) << 24) | 0x0B0713);

        // Sacudida de la cámara durante los primeros 260 ms.
        int shake = 0;
        if (elapsed < 260L) {
            shake = (int) (Math.sin(elapsed * 1.7D) * (3.0D - elapsed / 90.0D));
        }
        graphics.pose().pushPose();
        graphics.pose().translate(shake, -shake, 0.0F);

        // Rayos.
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy, 0.0F);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(elapsed / 60.0F));
        int rays = 20;
        for (int i = 0; i < rays; i++) {
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(360.0F / rays));
            int len = (int) (110 + 45 * Math.sin(elapsed / 260.0D + i));
            int alpha = 24 + (i % 2) * 24;
            graphics.fill(-1, -len, 1, 0, (alpha << 24) | (GOLD & 0xFFFFFF));
        }
        graphics.pose().popPose();

        // Chispas.
        float seconds = elapsed / 1000.0F;
        for (float[] spark : this.sparks) {
            int x = (int) (cx + spark[0] * seconds * 340.0F);
            int y = (int) (cy + spark[1] * seconds * 340.0F + 260.0F * seconds * seconds);
            int size = (int) (1 + spark[2]);
            int alpha = (int) (210 * (1.0F - t));
            if (alpha <= 0) {
                continue;
            }
            int color = spark[3] > 0.6F ? 0xFFFFFF : (GOLD & 0xFFFFFF);
            graphics.fill(x, y, x + size, y + size, (alpha << 24) | color);
        }

        // "5★": entra de 0,25× a 1,2× y se asienta en 1,0×.
        float pop = t < 0.16F
                ? Mth.lerp(easeOut(t / 0.16F), 0.25F, 1.2F)
                : 1.2F - 0.2F * Mth.clamp((t - 0.16F) / 0.18F, 0.0F, 1.0F);
        float scale = 3.4F * pop;
        String five = "5★";
        int width = this.font.width(five);
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy - 10, 200.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, five, -width / 2, -8, 0xFFFFF3C4, true);
        graphics.pose().popPose();

        String name = fiveStarName();
        if (name != null) {
            graphics.drawCenteredString(this.font, name, cx, cy + 54, 0xFFFFFFFF);
        }
        graphics.drawCenteredString(this.font,
                Component.translatable("gachawaifus.gacha.five.hint"), cx, cy + 86, TEXT_DIM);

        graphics.pose().popPose();
    }

    /** 4★: destello corto y el rango, sin cortar el ritmo de la tirada. */
    private void drawFourStar(GuiGraphics graphics, long now) {
        long elapsed = now - this.fourStart;
        float t = Mth.clamp(elapsed / (float) FOUR_STAR_MS, 0.0F, 1.0F);
        int alpha = (int) (55 * (1.0F - t));
        graphics.fill(0, 0, this.width, this.height, (alpha << 24) | (GOLD & 0xFFFFFF));

        int cx = this.width / 2;
        int cy = this.height / 2;
        float scale = 1.4F + 0.5F * (1.0F - easeOut(t));
        String four = "4★";
        int width = this.font.width(four);
        graphics.pose().pushPose();
        graphics.pose().translate(cx, cy - 10, 200.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, four, -width / 2, -8, 0xFFFFE08A, true);
        graphics.pose().popPose();
    }

    /** Nombre de la primera 5★ del resultado (para el rótulo de la secuencia). */
    @Nullable
    private String fiveStarName() {
        for (int i = 0; i < GachaMenu.RESULT_SLOTS; i++) {
            ItemStack stack = this.menu.resultStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            WaifuRoster.Entry entry = WaifuRoster.byToken(stack.getItem());
            if (entry != null && !WaifuRoster.isFourStar(entry)) {
                return entry.name();
            }
        }
        return null;
    }

    /** Chispas deterministas para la secuencia de 5★ (misma tirada = mismo dibujo). */
    private void spawnSparks(int seed) {
        this.sparks.clear();
        RandomSource random = RandomSource.create(seed * 7919L + 17L);
        for (int i = 0; i < 90; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double speed = 0.35D + random.nextDouble() * 0.95D;
            this.sparks.add(new float[] {
                    (float) (Math.cos(angle) * speed),
                    (float) (Math.sin(angle) * speed),
                    random.nextFloat() * 2.0F,
                    random.nextFloat()
            });
        }
    }

    private void playUi(Holder<SoundEvent> sound, float pitch) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
        }
    }

    private void playUi(SoundEvent sound, float pitch, float volume) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
        }
    }

    /** Tooltips de las barras, pestañas y pool. Devuelve {@code true} si ha pintado alguno. */
    private boolean renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;
        List<Component> lines = new ArrayList<>();
        int width = RIGHT_R - RIGHT_X;

        if (inRect(mouseX, mouseY, left + TAB_PROMO_X, top + TAB_Y, TAB_W, TAB_H)) {
            lines.add(Component.translatable("gachawaifus.gacha.tab.promo.tip"));
        } else if (inRect(mouseX, mouseY, left + TAB_STANDARD_X, top + TAB_Y, TAB_W, TAB_H)) {
            lines.add(Component.translatable("gachawaifus.gacha.tab.standard.tip"));
        } else if (inRect(mouseX, mouseY, left + RIGHT_X, top + 68, width, 12)) {
            lines.add(Component.translatable(standardTab()
                    ? "gachawaifus.gacha.rates.standard" : "gachawaifus.gacha.rates.five"));
            if (!standardTab()) {
                lines.add(Component.translatable("gachawaifus.gacha.rates.double"));
            }
        } else if (!standardTab() && inRect(mouseX, mouseY, left + RIGHT_X, top + 96, width, 12)) {
            lines.add(Component.translatable("gachawaifus.gacha.rates.four"));
        } else if (standardTab() && inRect(mouseX, mouseY, left + RIGHT_X, top + POOL_ICON_Y,
                POOL_ICON_STEP * WaifuRoster.STANDARD_POOL.size(), 18)) {
            int index = (mouseX - (left + RIGHT_X)) / POOL_ICON_STEP;
            if (index >= 0 && index < WaifuRoster.STANDARD_POOL.size()) {
                WaifuRoster.Entry entry = WaifuRoster.STANDARD_POOL.get(index);
                boolean owned = (this.menu.standardMask() & (1 << index)) != 0;
                lines.add(Component.literal(entry.name()).withStyle(
                        owned ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.GRAY));
                lines.add(Component.translatable(owned
                        ? "gachawaifus.gacha.owned" : "gachawaifus.gacha.not_owned"));
            }
        } else if (inRect(mouseX, mouseY, left + GachaMenu.RESULT_X, top + GachaMenu.RESULT_Y,
                GachaMenu.RESULT_SLOTS * GachaMenu.RESULT_STEP, 16)) {
            int index = (mouseX - (left + GachaMenu.RESULT_X)) / GachaMenu.RESULT_STEP;
            addResultTooltip(lines, index);
        } else if (inRect(mouseX, mouseY, left + PULL1_X, top + BUTTON_Y, PULL1_W, BUTTON_H)
                || inRect(mouseX, mouseY, left + PULL10_X, top + BUTTON_Y, PULL10_W, BUTTON_H)) {
            if (!this.menu.hasTargets()) {
                lines.add(Component.translatable("gachawaifus.gacha.complete"));
            } else if (this.menu.fineBalls() < 1) {
                lines.add(Component.translatable("gachawaifus.gacha.no_balls"));
            } else if (!standardTab()) {
                if (this.menu.guaranteed()) {
                    lines.add(Component.translatable("gachawaifus.gacha.guaranteed.tip")
                            .withStyle(net.minecraft.ChatFormatting.GOLD));
                }
            }
        }

        if (lines.isEmpty()) {
            return false;
        }
        graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        return true;
    }

    /**
     * Tooltip de una carta del resultado: nombre, rango, color y, si toca, la marca de nueva.
     * La comida cae al nombre del ítem y ya.
     */
    private void addResultTooltip(List<Component> lines, int index) {
        ItemStack stack = this.menu.resultStack(index);
        if (stack.isEmpty()) {
            return;
        }
        WaifuRoster.Entry entry = WaifuRoster.byToken(stack.getItem());
        if (entry == null) {
            lines.add(stack.getHoverName());
            return;
        }
        boolean four = WaifuRoster.isFourStar(entry);
        lines.add(Component.literal(entry.name())
                .withStyle(four ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.GOLD));
        lines.add(Component.literal(four ? "★★★★" : "★★★★★")
                .withStyle(net.minecraft.ChatFormatting.GOLD));
        WaifuColor color = WaifuColors.of(entry.id());
        if (color != null) {
            lines.add(color.displayName().copy().withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        if ((this.menu.newMask() & (1 << index)) != 0) {
            lines.add(Component.translatable("gachawaifus.gacha.new")
                    .withStyle(net.minecraft.ChatFormatting.GREEN));
        }
    }

    /* ══ Helpers de dibujo ═════════════════════════════════════════════════════════════════ */

    private static int tabX(int tab) {
        return tab == 0 ? TAB_PROMO_X : TAB_STANDARD_X;
    }

    private static boolean inRect(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void label(GuiGraphics graphics, String key, int x, int y, int color) {
        graphics.drawString(this.font, Component.translatable(key), x, y, color, false);
    }

    /** Botón tipo píldora con borde, brillo arriba y texto centrado. */
    private void pillButton(GuiGraphics graphics, int x, int y, int w, int h, String label,
                            boolean enabled, int accent, int mouseX, int mouseY) {
        boolean hover = enabled && inRect(mouseX, mouseY, x, y, w, h);
        int base = enabled ? mix(0xFF1D1628, accent, hover ? 0.55F : 0.35F) : 0xFF161020;
        graphics.fill(x, y, x + w, y + h, base);
        graphics.fill(x, y, x + w, y + 1, enabled
                ? 0xFF000000 | lighten(accent, hover ? 0.5F : 0.25F) : 0xFF2A2238);
        graphics.fill(x, y + h - 1, x + w, y + h, BORDER_DARK);
        if (enabled) {
            graphics.fill(x, y, x + 1, y + h, 0xFF000000 | accent);
            graphics.fill(x + w - 1, y, x + w, y + h, 0xFF000000 | accent);
        }
        int textColor = enabled ? 0xFFFFFFFF : 0xFF6B6180;
        graphics.drawString(this.font, label, x + (w - this.font.width(label)) / 2,
                y + (h - 8) / 2 + 1, textColor, enabled);
    }

    private void bar(GuiGraphics graphics, int x, int y, int w, int value, int max, int color) {
        graphics.fill(x, y, x + w, y + 7, 0xFF221A2C);
        float fraction = max <= 0 ? 0.0F : Mth.clamp(value / (float) max, 0.0F, 1.0F);
        int filled = (int) (w * fraction);
        if (filled > 0) {
            graphics.fillGradient(x, y, x + filled, y + 7,
                    0xFF000000 | lighten(color, 0.35F), 0xFF000000 | darken(color, 0.45F));
        }
        graphics.fill(x, y, x + w, y + 1, 0x40000000);
        frame(graphics, x, y, w, 7, 0xFF3A3048);
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

    /** Halo suave por capas: no hay soporte de degradado radial, así que se apilan rectángulos. */
    private static void glow(GuiGraphics graphics, int centerX, int centerY, int radius, int rgb) {
        for (int i = 6; i >= 1; i--) {
            int r = radius * i / 6;
            int alpha = 6 + (6 - i) * 4;
            graphics.fill(centerX - r, centerY - r, centerX + r, centerY + r,
                    (alpha << 24) | (rgb & 0xFFFFFF));
        }
    }

    private static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static int mix(int colorA, int colorB, float amount) {
        int r = (int) (((colorA >> 16) & 0xFF) * (1 - amount) + ((colorB >> 16) & 0xFF) * amount);
        int g = (int) (((colorA >> 8) & 0xFF) * (1 - amount) + ((colorB >> 8) & 0xFF) * amount);
        int b = (int) ((colorA & 0xFF) * (1 - amount) + (colorB & 0xFF) * amount);
        // Siempre opaco: quien necesite transparencia la añade con withAlpha().
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int lighten(int rgb, float amount) {
        return mix(rgb, 0xFFFFFF, amount);
    }

    private static int darken(int rgb, float amount) {
        return mix(rgb, 0x000000, amount);
    }

    /** Aclara un color demasiado oscuro para que se lea sobre el panel. */
    private static int readable(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int luminance = (r * 299 + g * 587 + b * 114) / 1000;
        return luminance < 80 ? lighten(rgb, 0.65F) : rgb;
    }

    /** El contador de pity se vuelve oro pulsante cuando el 5★ está a 10 tiradas o menos. */
    private static int pityColor(int value) {
        if (value < GachaService.HARD_PITY - 10) {
            return TEXT;
        }
        float wave = (float) Math.sin(Util.getMillis() / 220.0D);
        return mix(GOLD, 0xFFFFFFFF, 0.5F + 0.5F * wave);
    }
}
