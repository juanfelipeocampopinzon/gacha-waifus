package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.menu.GuideMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * La cara de la Guía del Invocador: un manual de páginas navegables dibujado a mano.
 *
 * <p>Igual que {@link GachaBannerScreen}, todo se pinta con rectángulos y texto: el mod no tiene
 * {@code textures/gui/} y así la pantalla no se rompe si falta un PNG. El texto de las páginas es
 * estático (vive en {@link #PAGES}), por eso el menú no sincroniza nada: la página que se lee es
 * local y el {@link GuideMenu} solo existe para poder abrir la pantalla desde el servidor.
 *
 * <p>Las dos últimas páginas no son texto: son las <b>rejillas de crafteo</b>, leídas del
 * {@code RecipeManager} del mundo en el momento de dibujar. Por eso muestran la receta que de verdad
 * lleva el pack instalado (incluido un resource pack que la cambie) en vez de una descripción escrita
 * a mano que se quedaría vieja.
 *
 * <p>Cada línea empieza con un {@code §} que elige su color desde {@link #PALETTE}: no son los
 * colores de chat literales, sino su versión subida de brillo, porque en el panel oscuro el verde o
 * el azul oscuros de vanilla no se leen. Los {@code §} de dentro de la línea también se respetan,
 * así que una misma línea puede mezclar dos colores.
 */
public class GuideScreen extends AbstractContainerScreen<GuideMenu> {

    /* ── Geometría ───────────────────────────────────────────────────────────────────────── */
    private static final int W = 300;
    private static final int H = 210;

    private static final int HEADER_H = 20;
    private static final int PAGE_X = 10;
    private static final int PAGE_Y = 26;
    private static final int PAGE_W = W - 20;
    private static final int PAGE_H = H - PAGE_Y - 24;

    private static final int TEXT_X = PAGE_X + 8;
    private static final int TEXT_Y = PAGE_Y + 8;
    private static final int LINE_H = 10;
    private static final int LINES = (PAGE_H - 18) / LINE_H;

    private static final int FOOT_Y = H - 20;
    private static final int PREV_X = 10;
    private static final int NEXT_X = W - 66;
    private static final int BTN_W = 56;
    private static final int BTN_H = 14;
    private static final int CLOSE_X = W - 18;

    /* ── Páginas de crafteo ────────────────────────────────────────────────────────────────── */

    /** Páginas de crafteo: las últimas dos de {@link #PAGES}, una rejilla por cada cuatro recetas. */
    private static final int CRAFT_PAGES = 2;

    /** Cuatro por página: dos filas de dos celdas es lo que cabe entre el título y el pie. */
    private static final int CRAFTS_PER_PAGE = 4;

    /** Qué se dibuja y en qué orden. Un id sin receta en el mundo simplemente no aparece. */
    private static final String[] CRAFT_ITEMS = {
            "pink_ball", "blue_ball", "gacha_terminal", "waifu_capsule",
            "waifu_beacon", "waifu_home_block", "waifu_guide",
    };

    private static final int SLOT = 18;
    private static final int GRID = SLOT * 3;
    private static final int CELL_X = 138;
    private static final int CELL_Y = 68;

    /** Donde empieza la primera celda: justo debajo de la única línea de título de la página. */
    private static final int CRAFT_Y0 = TEXT_Y + LINE_H + 4;

    /* ── Paleta del panel ────────────────────────────────────────────────────────────────── */
    private static final int PANEL_TOP = 0xFF1A1424;
    private static final int PANEL_BOTTOM = 0xFF0C0912;
    private static final int BORDER = 0xFF463A5C;
    private static final int PAGE_BG = 0xFF241C33;
    private static final int PAGE_EDGE = 0xFF372C4A;
    private static final int TEXT = 0xFFEDE7F5;
    private static final int TEXT_DIM = 0xFF9A8FAD;
    private static final int PINK_ACCENT = 0xFFFF6FB5;

    /** Colores de los dígitos {@code 0-9a-f} que siguen a un §, en ese orden. */
    private static final int[] PALETTE = {
            0xFF555555, // 0 negro -> gris muy oscuro (solo sombras)
            0xFF7FA8FF, // 1 azul oscuro
            0xFF6BE08A, // 2 verde
            0xFF5FE3E3, // 3 aqua
            0xFFE06B6B, // 4 rojo
            0xFFC78BFF, // 5 morado
            0xFFFFC46B, // 6 dorado
            0xFFC9C3D6, // 7 gris
            0xFF847D92, // 8 gris oscuro
            0xFF9DB4FF, // 9 azul
            0xFF8CFFB0, // a verde claro
            0xFF8CF5FF, // b cyan
            0xFFFF8C8C, // c rojo claro
            0xFFFF9CE0, // d rosa
            0xFFFFF0A0, // e amarillo
            0xFFFFFFFF, // f blanco
    };

    /** Página que se muestra entre aperturas de la misma sesión: se recuerda al cerrar el libro. */
    private static int rememberedPage;

    /** Receta con su resultado ya resuelto: se busca una sola vez por apertura de la guía. */
    private record Craft(ShapedRecipe recipe, ItemStack result) {
    }

    private int page;

    @Nullable
    private List<Craft> craftCache;

    public GuideScreen(GuideMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.translatable("gachawaifus.guide.title"));
        this.imageWidth = W;
        this.imageHeight = H;
        this.page = Mth.clamp(rememberedPage, 0, PAGES.length - 1);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        rememberedPage = this.page;
    }

    /** El libro no pinta el título de vanilla ni la etiqueta «Inventario»: la cabecera es suya. */
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    /* ══ Dibujo ═══════════════════════════════════════════════════════════════════════════ */

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(0, 0, this.width, this.height, 0xB2000000);

        graphics.fill(left + 2, top, left + W - 2, top + H, PANEL_TOP);
        graphics.fill(left, top + 2, left + W, top + H - 2, PANEL_TOP);
        graphics.fillGradient(left + 2, top + HEADER_H, left + W - 2, top + H - 2, PANEL_TOP, PANEL_BOTTOM);
        frame(graphics, left, top, W, H, BORDER);

        graphics.fillGradient(left + 1, top + 1, left + W - 1, top + HEADER_H, 0xFF3A2A12, 0xFF140F1C);
        graphics.fill(left + 1, top + HEADER_H, left + W - 1, top + HEADER_H + 1, PINK_ACCENT);
        graphics.drawString(this.font, "*", left + 8, top + 6, PALETTE[13], true);
        graphics.drawString(this.font, Component.translatable("gachawaifus.guide.title"),
                left + 18, top + 6, 0xFFFFFFFF, true);
        drawClose(graphics, left, top, mouseX, mouseY);

        // La hoja: un borde escalonado imita el canto de las páginas apiladas.
        graphics.fill(left + PAGE_X - 2, top + PAGE_Y - 2, left + PAGE_X + PAGE_W + 2,
                top + PAGE_Y + PAGE_H + 2, PAGE_EDGE);
        graphics.fill(left + PAGE_X, top + PAGE_Y, left + PAGE_X + PAGE_W, top + PAGE_Y + PAGE_H, PAGE_BG);
        frame(graphics, left + PAGE_X, top + PAGE_Y, PAGE_W, PAGE_H, 0xFF5A4A76);

        drawPage(graphics, left, top);
        drawFooter(graphics, left, top, mouseX, mouseY);
    }

    /** Convierte las claves de lang de una pagina en su texto en el idioma del jugador. */
    private static String[] resolvePage(String[] keys) {
        String[] out = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            out[i] = keys[i].startsWith("gachawaifus.guide.")
                    ? Component.translatable(keys[i]).getString() : keys[i];
        }
        return out;
    }

    private void drawPage(GuiGraphics graphics, int left, int top) {
        String[] lines = resolvePage(PAGES[this.page]);
        // Recorte a la hoja: el ancho del texto depende de la fuente del jugador (GUI scale, ASCII
        // ancho, resource pack de fuente). Sin esto una línea larga se sale del panel y pinta encima
        // de los botones del pie.
        graphics.enableScissor(left + PAGE_X + 2, top + PAGE_Y + 2,
                left + PAGE_X + PAGE_W - 2, top + PAGE_Y + PAGE_H - 2);
        int y = top + TEXT_Y;
        for (int i = 0; i < lines.length && i < LINES; i++) {
            drawMarkedLine(graphics, left + TEXT_X, y, lines[i]);
            y += LINE_H;
        }
        if (this.page >= PAGES.length - CRAFT_PAGES) {
            drawCrafts(graphics, left, top, this.page - (PAGES.length - CRAFT_PAGES));
        }
        graphics.disableScissor();
    }

    /* ══ Rejillas de crafteo ═════════════════════════════════════════════════════════════════ */

    private void drawCrafts(GuiGraphics graphics, int left, int top, int craftPage) {
        List<Craft> crafts = crafts();
        for (int i = 0; i < CRAFTS_PER_PAGE; i++) {
            int index = craftPage * CRAFTS_PER_PAGE + i;
            if (index >= crafts.size()) {
                return;
            }
            drawCraft(graphics, crafts.get(index),
                    left + TEXT_X + (i % 2) * CELL_X,
                    top + CRAFT_Y0 + (i / 2) * CELL_Y);
        }
    }

    /** Una receta: nombre arriba, rejilla de 3x3 centrada con lo que lleva y el resultado a la derecha. */
    private void drawCraft(GuiGraphics graphics, Craft craft, int x, int y) {
        ShapedRecipe recipe = craft.recipe();
        int width = recipe.getWidth();
        int height = recipe.getHeight();
        String name = this.font.plainSubstrByWidth(plain(craft.result().getHoverName().getString()), CELL_X - 16);
        graphics.drawString(this.font, name, x, y, PALETTE[13], false);

        int areaY = y + 11;
        int gridX = x + (GRID - width * SLOT) / 2;
        int gridY = areaY + (GRID - height * SLOT) / 2;
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int slotX = gridX + col * SLOT;
                int slotY = gridY + row * SLOT;
                drawSlot(graphics, slotX, slotY);
                ItemStack icon = iconOf(ingredients.get(row * width + col));
                if (!icon.isEmpty()) {
                    graphics.renderItem(icon, slotX + 1, slotY + 1);
                    graphics.renderItemDecorations(this.font, icon, slotX + 1, slotY + 1);
                }
            }
        }

        int centerY = areaY + GRID / 2;
        drawMarkedLine(graphics, x + GRID + 2, centerY - 5, "§f→");
        int resultX = x + GRID + 15;
        drawSlot(graphics, resultX, centerY - 9);
        graphics.renderItem(craft.result(), resultX + 1, centerY - 8);
        graphics.renderItemDecorations(this.font, craft.result(), resultX + 1, centerY - 8);
    }

    /** El hueco vacío de la rejilla: marco y fondo, como los slots de la mesa de crafteo. */
    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + SLOT, y + SLOT, 0xFF171226);
        frame(graphics, x, y, SLOT, SLOT, 0xFF4A4066);
    }

    /** La primera variante del ingrediente: es la que enseña la guía, no un catálogo de alternativas. */
    private static ItemStack iconOf(Ingredient ingredient) {
        ItemStack[] stack = ingredient.getItems();
        return stack.length == 0 ? ItemStack.EMPTY : stack[0];
    }

    /** Recetas resueltas, cacheadas: buscar en la lista del mundo cada frame sería tonto. */
    private List<Craft> crafts() {
        if (this.craftCache == null) {
            this.craftCache = resolveCrafts();
        }
        return this.craftCache;
    }

    private List<Craft> resolveCrafts() {
        List<Craft> found = new ArrayList<>();
        if (this.minecraft == null || this.minecraft.level == null) {
            return found;
        }
        for (String id : CRAFT_ITEMS) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, id));
            if (item == Items.AIR) {
                continue;
            }
            for (RecipeHolder<CraftingRecipe> holder
                    : this.minecraft.level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
                if (holder.value() instanceof ShapedRecipe shaped) {
                    ItemStack result = shaped.getResultItem(this.minecraft.level.registryAccess());
                    if (result.is(item)) {
                        found.add(new Craft(shaped, result));
                        break;
                    }
                }
            }
        }
        return found;
    }

    /** Los nombres de item no llevan §, pero el recurso de un pack ajeno sí podría. */
    private static String plain(String text) {
        return text.replaceAll("(?i)§.", "");
    }

    /** Pinta una línea respetando los § de dentro, corrida a corrida. */
    private void drawMarkedLine(GuiGraphics graphics, int x, int y, String line) {
        int color = TEXT;
        int runStart = 0;
        int i = 0;
        while (i < line.length()) {
            if (line.charAt(i) == '§' && i + 1 < line.length()) {
                if (i > runStart) {
                    x = drawRun(graphics, line.substring(runStart, i), x, y, color);
                }
                color = paletteFor(line.charAt(i + 1), color);
                i += 2;
                runStart = i;
                continue;
            }
            i++;
        }
        if (i > runStart) {
            drawRun(graphics, line.substring(runStart), x, y, color);
        }
    }

    /** Devuelve la x de destino para que la siguiente corrida empiece donde terminó esta. */
    private int drawRun(GuiGraphics graphics, String text, int x, int y, int color) {
        graphics.drawString(this.font, text, x, y, color, false);
        return x + this.font.width(text);
    }

    private static int paletteFor(char code, int current) {
        if (code == 'r' || code == 'R') {
            return TEXT;
        }
        int index = "0123456789abcdef".indexOf(Character.toLowerCase(code));
        return index >= 0 ? PALETTE[index] : current;
    }

    private void drawFooter(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        drawPageButton(graphics, left + PREV_X, top + FOOT_Y, "gachawaifus.guide.prev", this.page > 0, mouseX, mouseY);
        drawPageButton(graphics, left + NEXT_X, top + FOOT_Y, "gachawaifus.guide.next",
                this.page < PAGES.length - 1, mouseX, mouseY);
        String pageText = Component.translatable("gachawaifus.guide.page", this.page + 1, PAGES.length).getString();
        graphics.drawString(this.font, pageText,
                left + (W - this.font.width(pageText)) / 2, top + FOOT_Y + 4, TEXT_DIM, false);
    }

    private void drawPageButton(GuiGraphics graphics, int x, int y, String key, boolean enabled,
                                int mouseX, int mouseY) {
        boolean hover = enabled && inRect(mouseX, mouseY, x, y, BTN_W, BTN_H);
        graphics.fill(x, y, x + BTN_W, y + BTN_H, enabled ? (hover ? 0xFF4A3A66 : 0xFF312645) : 0xFF1C1726);
        frame(graphics, x, y, BTN_W, BTN_H, enabled ? 0xFF6B5A8C : 0xFF2A2436);
        String label = this.font.plainSubstrByWidth(Component.translatable(key).getString(), BTN_W - 8);
        graphics.drawString(this.font, label, x + (BTN_W - this.font.width(label)) / 2, y + 4,
                enabled ? TEXT : TEXT_DIM, false);
    }

    private void drawClose(GuiGraphics graphics, int left, int top, int mouseX, int mouseY) {
        boolean hover = inRect(mouseX, mouseY, left + CLOSE_X, top + 3, 14, 14);
        graphics.fill(left + CLOSE_X, top + 3, left + CLOSE_X + 14, top + 17, hover ? 0xFF7A2438 : 0xFF3A1622);
        graphics.drawString(this.font, "x", left + CLOSE_X + 5, top + 7, 0xFFFFD5DE, true);
    }

    private static void frame(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    /* ══ Navegación ════════════════════════════════════════════════════════════════════════ */

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int left = this.leftPos;
            int top = this.topPos;
            if (inRect(mouseX, mouseY, left + CLOSE_X, top + 3, 14, 14)) {
                this.onClose();
                return true;
            }
            if (this.page > 0 && inRect(mouseX, mouseY, left + PREV_X, top + FOOT_Y, BTN_W, BTN_H)) {
                turn(-1);
                return true;
            }
            if (this.page < PAGES.length - 1 && inRect(mouseX, mouseY, left + NEXT_X, top + FOOT_Y, BTN_W, BTN_H)) {
                turn(1);
                return true;
            }
            // Clic en la hoja: a la derecha avanza, a la izquierda retrocede, como un libro real.
            if (inRect(mouseX, mouseY, left + PAGE_X, top + PAGE_Y, PAGE_W, PAGE_H)) {
                turn(mouseX > left + PAGE_X + PAGE_W / 2 ? 1 : -1);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0D) {
            turn(scrollY < 0.0D ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 262) { // flecha derecha
            turn(1);
            return true;
        }
        if (keyCode == 263) { // flecha izquierda
            turn(-1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void turn(int delta) {
        int target = Mth.clamp(this.page + delta, 0, PAGES.length - 1);
        if (target != this.page) {
            this.page = target;
            playUi(SoundEvents.BOOK_PAGE_TURN);
        }
    }

    private void playUi(SoundEvent sound) {
        if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F));
        }
    }

    /* ══ Contenido ═════════════════════════════════════════════════════════════════════════
     * Una entrada de PAGES = una página. Cada línea empieza con § (su color) y cabe de ancho
     * en la hoja; como mucho LINES líneas, y la primera es el título. Si una página crece más
     * de LINES líneas, lo que sobra NO se pinta: hay que partir el contenido en otra página.
     *
     * Las últimas CRAFT_PAGES entradas llevan una sola línea (el título): debajo se dibuja la
     * rejilla de cada receta, así que meterles texto extra solo les come el sitio de las celdas.
     * Si sale un item nuevo que se craftee, se añade a CRAFT_ITEMS y, si desborda las cuatro
     * por página, se añade su página aquí.
     * ═════════════════════════════════════════════════════════════════════════════════════ */

    private static final String[][] PAGES = {
            { // 1
                    "gachawaifus.guide.p1.l1",
                    "gachawaifus.guide.p1.l2",
                    "§f",
                    "gachawaifus.guide.p1.l4",
                    "gachawaifus.guide.p1.l5",
                    "gachawaifus.guide.p1.l6",
                    "gachawaifus.guide.p1.l7",
                    "§f",
                    "gachawaifus.guide.p1.l9",
                    "gachawaifus.guide.p1.l10",
                    "gachawaifus.guide.p1.l11",
                    "gachawaifus.guide.p1.l12",
                    "gachawaifus.guide.p1.l13",
                    "gachawaifus.guide.p1.l14"
            },
            { // 2
                    "gachawaifus.guide.p2.l1",
                    "§f",
                    "gachawaifus.guide.p2.l3",
                    "gachawaifus.guide.p2.l4",
                    "gachawaifus.guide.p2.l5",
                    "gachawaifus.guide.p2.l6",
                    "gachawaifus.guide.p2.l7",
                    "gachawaifus.guide.p2.l8",
                    "gachawaifus.guide.p2.l9",
                    "§f",
                    "gachawaifus.guide.p2.l11",
                    "gachawaifus.guide.p2.l12",
                    "gachawaifus.guide.p2.l13",
                    "gachawaifus.guide.p2.l14"
            },
            { // 3
                    "gachawaifus.guide.p3.l1",
                    "§f",
                    "gachawaifus.guide.p3.l3",
                    "gachawaifus.guide.p3.l4",
                    "gachawaifus.guide.p3.l5",
                    "gachawaifus.guide.p3.l6",
                    "gachawaifus.guide.p3.l7",
                    "§f",
                    "gachawaifus.guide.p3.l9",
                    "gachawaifus.guide.p3.l10",
                    "gachawaifus.guide.p3.l11",
                    "gachawaifus.guide.p3.l12",
                    "gachawaifus.guide.p3.l13",
                    "gachawaifus.guide.p3.l14"
            },
            { // 4
                    "gachawaifus.guide.p4.l1",
                    "§f",
                    "gachawaifus.guide.p4.l3",
                    "gachawaifus.guide.p4.l4",
                    "gachawaifus.guide.p4.l5",
                    "gachawaifus.guide.p4.l6",
                    "gachawaifus.guide.p4.l7",
                    "gachawaifus.guide.p4.l8",
                    "gachawaifus.guide.p4.l9",
                    "gachawaifus.guide.p4.l10",
                    "§f",
                    "gachawaifus.guide.p4.l12",
                    "gachawaifus.guide.p4.l13",
                    "gachawaifus.guide.p4.l14"
            },
            { // 5
                    "gachawaifus.guide.p5.l1",
                    "§f",
                    "gachawaifus.guide.p5.l3",
                    "gachawaifus.guide.p5.l4",
                    "gachawaifus.guide.p5.l5",
                    "gachawaifus.guide.p5.l6",
                    "gachawaifus.guide.p5.l7",
                    "gachawaifus.guide.p5.l8",
                    "gachawaifus.guide.p5.l9",
                    "gachawaifus.guide.p5.l10",
                    "§f",
                    "gachawaifus.guide.p5.l12",
                    "gachawaifus.guide.p5.l13",
                    "gachawaifus.guide.p5.l14"
            },
            { // 6
                    "gachawaifus.guide.p6.l1",
                    "gachawaifus.guide.p6.l2",
                    "gachawaifus.guide.p6.l3",
                    "gachawaifus.guide.p6.l4",
                    "gachawaifus.guide.p6.l5",
                    "gachawaifus.guide.p6.l6",
                    "gachawaifus.guide.p6.l7",
                    "§f",
                    "gachawaifus.guide.p6.l9",
                    "gachawaifus.guide.p6.l10",
                    "gachawaifus.guide.p6.l11",
                    "gachawaifus.guide.p6.l12",
                    "gachawaifus.guide.p6.l13",
                    "gachawaifus.guide.p6.l14"
            },
            { // 7
                    "gachawaifus.guide.p7.l1",
                    "§f",
                    "gachawaifus.guide.p7.l3",
                    "gachawaifus.guide.p7.l4",
                    "gachawaifus.guide.p7.l5",
                    "§f",
                    "gachawaifus.guide.p7.l7",
                    "gachawaifus.guide.p7.l8",
                    "gachawaifus.guide.p7.l9",
                    "gachawaifus.guide.p7.l10",
                    "gachawaifus.guide.p7.l11",
                    "gachawaifus.guide.p7.l12",
                    "gachawaifus.guide.p7.l13",
                    "gachawaifus.guide.p7.l14"
            },
            { // 8
                    "gachawaifus.guide.p8.l1",
                    "§f",
                    "gachawaifus.guide.p8.l3",
                    "gachawaifus.guide.p8.l4",
                    "gachawaifus.guide.p8.l5",
                    "gachawaifus.guide.p8.l6",
                    "gachawaifus.guide.p8.l7",
                    "§f",
                    "gachawaifus.guide.p8.l9",
                    "gachawaifus.guide.p8.l10",
                    "gachawaifus.guide.p8.l11",
                    "gachawaifus.guide.p8.l12",
                    "gachawaifus.guide.p8.l13",
                    "gachawaifus.guide.p8.l14"
            },
            { // 9
                    "gachawaifus.guide.p9.l1",
                    "§f",
                    "gachawaifus.guide.p9.l3",
                    "gachawaifus.guide.p9.l4",
                    "gachawaifus.guide.p9.l5",
                    "§f",
                    "gachawaifus.guide.p9.l7",
                    "gachawaifus.guide.p9.l8",
                    "gachawaifus.guide.p9.l9",
                    "gachawaifus.guide.p9.l10",
                    "gachawaifus.guide.p9.l11",
                    "gachawaifus.guide.p9.l12",
                    "gachawaifus.guide.p9.l13",
                    "gachawaifus.guide.p9.l14"
            },
            { // 10
                    "gachawaifus.guide.p10.l1",
                    "§f",
                    "gachawaifus.guide.p10.l3",
                    "gachawaifus.guide.p10.l4",
                    "gachawaifus.guide.p10.l5",
                    "gachawaifus.guide.p10.l6",
                    "gachawaifus.guide.p10.l7",
                    "gachawaifus.guide.p10.l8",
                    "gachawaifus.guide.p10.l9",
                    "gachawaifus.guide.p10.l10",
                    "§f",
                    "gachawaifus.guide.p10.l12",
                    "gachawaifus.guide.p10.l13",
                    "gachawaifus.guide.p10.l14"
            },
            { // 11
                    "gachawaifus.guide.p11.l1",
                    "§f",
                    "gachawaifus.guide.p11.l3",
                    "gachawaifus.guide.p11.l4",
                    "gachawaifus.guide.p11.l5",
                    "§f",
                    "gachawaifus.guide.p11.l7",
                    "gachawaifus.guide.p11.l8",
                    "gachawaifus.guide.p11.l9",
                    "gachawaifus.guide.p11.l10",
                    "gachawaifus.guide.p11.l11",
                    "gachawaifus.guide.p11.l12",
                    "gachawaifus.guide.p11.l13",
                    "gachawaifus.guide.p11.l14"
            },
            { // 12
                    "gachawaifus.guide.p12.l1",
                    "§f",
                    "gachawaifus.guide.p12.l3",
                    "gachawaifus.guide.p12.l4",
                    "gachawaifus.guide.p12.l5",
                    "gachawaifus.guide.p12.l6",
                    "gachawaifus.guide.p12.l7",
                    "gachawaifus.guide.p12.l8",
                    "gachawaifus.guide.p12.l9",
                    "gachawaifus.guide.p12.l10",
                    "gachawaifus.guide.p12.l11",
                    "gachawaifus.guide.p12.l12",
                    "gachawaifus.guide.p12.l13",
                    "gachawaifus.guide.p12.l14"
            },
            { // 13
                    "gachawaifus.guide.p13.l1",
                    "§f",
                    "gachawaifus.guide.p13.l3",
                    "gachawaifus.guide.p13.l4",
                    "gachawaifus.guide.p13.l5",
                    "gachawaifus.guide.p13.l6",
                    "§f",
                    "gachawaifus.guide.p13.l8",
                    "gachawaifus.guide.p13.l9",
                    "gachawaifus.guide.p13.l10",
                    "gachawaifus.guide.p13.l11",
                    "gachawaifus.guide.p13.l12",
                    "gachawaifus.guide.p13.l13",
                    "gachawaifus.guide.p13.l14"
            },
            { // 14
                    "gachawaifus.guide.p14.l1",
                    "§f",
                    "gachawaifus.guide.p14.l3",
                    "gachawaifus.guide.p14.l4",
                    "gachawaifus.guide.p14.l5",
                    "gachawaifus.guide.p14.l6",
                    "gachawaifus.guide.p14.l7",
                    "gachawaifus.guide.p14.l8",
                    "gachawaifus.guide.p14.l9",
                    "gachawaifus.guide.p14.l10",
                    "gachawaifus.guide.p14.l11",
                    "gachawaifus.guide.p14.l12",
                    "gachawaifus.guide.p14.l13",
                    "gachawaifus.guide.p14.l14"
            },
            { // 15
                    "gachawaifus.guide.p15.l1",
                    "§f",
                    "gachawaifus.guide.p15.l3",
                    "gachawaifus.guide.p15.l4",
                    "§f",
                    "gachawaifus.guide.p15.l6",
                    "gachawaifus.guide.p15.l7",
                    "gachawaifus.guide.p15.l8",
                    "§f",
                    "gachawaifus.guide.p15.l10",
                    "gachawaifus.guide.p15.l11",
                    "gachawaifus.guide.p15.l12",
                    "gachawaifus.guide.p15.l13",
                    "gachawaifus.guide.p15.l14"
            },
            { // 16
                    "gachawaifus.guide.p16.l1",
                    "gachawaifus.guide.p16.l2",
                    "gachawaifus.guide.p16.l3",
                    "gachawaifus.guide.p16.l4",
                    "gachawaifus.guide.p16.l5",
                    "gachawaifus.guide.p16.l6",
                    "gachawaifus.guide.p16.l7",
                    "gachawaifus.guide.p16.l8",
                    "gachawaifus.guide.p16.l9",
                    "gachawaifus.guide.p16.l10",
                    "gachawaifus.guide.p16.l11",
                    "gachawaifus.guide.p16.l12",
                    "gachawaifus.guide.p16.l13",
                    "gachawaifus.guide.p16.l14"
            },
            { // 17 — rejilla de crafteo (1/2): lo que se pinta debajo es la receta del mundo
                    "gachawaifus.guide.p17.l1"
            },
            { // 18 — rejilla de crafteo (2/2)
                    "gachawaifus.guide.p18.l1"
            },
    };
}
