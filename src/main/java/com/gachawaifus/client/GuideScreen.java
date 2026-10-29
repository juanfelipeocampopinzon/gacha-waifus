package com.gachawaifus.client;

import com.gachawaifus.menu.GuideMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * La cara de la Guía del Invocador: un manual de páginas navegables dibujado a mano.
 *
 * <p>Igual que {@link GachaBannerScreen}, todo se pinta con rectángulos y texto: el mod no tiene
 * {@code textures/gui/} y así la pantalla no se rompe si falta un PNG. El contenido es estático
 * (vive en {@link #PAGES}), por eso el menú no sincroniza nada: la página que se lee es local y el
 * {@link GuideMenu} solo existe para poder abrir la pantalla desde el servidor.
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

    private int page;

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

    private void drawPage(GuiGraphics graphics, int left, int top) {
        String[] lines = PAGES[this.page];
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
        graphics.disableScissor();
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
     * ═════════════════════════════════════════════════════════════════════════════════════ */

    private static final String[][] PAGES = {
            { // 1
                    "§6GUÍA DEL INVOCADOR",
                    "§8Gacha Waifus · NeoForge 1.21.1",
                    "§f",
                    "§fEste mod mete en tu mundo waifus de Zenless",
                    "§fZone Zero, Honkai, Genshin y varios animes más.",
                    "§fSon mascotas tamables: te siguen, pelean a tu",
                    "§flado y se pueden quedar de guardia en tu base.",
                    "§f",
                    "§dHay §f62 §dwaifus invocables en el roster.",
                    "§eEMPIEZA AQUÍ",
                    "§f· §a/inicio §f— reclama tu waifu de regalo (pág. 2).",
                    "§f· §a/guia §f— reabre este manual al cerrarlo.",
                    "§f· La rueda, las flechas o un clic en la hoja",
                    "§f  pasan de página."
            },
            { // 2
                    "§dLAS TRES FORMAS DE CONSEGUIRLA",
                    "§f",
                    "§a1 §f§dGACHA §f— crafteas bolitas y tiras en el",
                    "§f   Terminal o en la Máquina. Es la vía de las",
                    "§f   §f5★ §fdel banner del día. Páginas 3 a 6.",
                    "§a2 §f§bTOKENS §f— los sueltan los jefes del mundo",
                    "§f   y las misiones diarias. Página 7.",
                    "§a3 §f§aDOMANDO §f— nacen salvajes por los biomas",
                    "§f   y se doman a bolitazos. Página 9.",
                    "§f",
                    "§eEL REGALO DE BIENVENIDA",
                    "§f· §a/inicio §fenseña las §f7 §fclásicas del banner",
                    "§f  permanente; §a/inicio <número> §fda su token.",
                    "§f· Es una sola vez por mundo."
            },
            { // 3
                    "§cBOLITAS: LA MONEDA DEL GACHA",
                    "§f",
                    "§cBOLITA ROSA §7— banner promocional",
                    "§f· Crafteo en cruz: §fdiamante §fal centro, §fcobre",
                    "§f  arriba, §flapis §fcon §fhierro §fa los lados y",
                    "§f  §fcarbón §fabajo.",
                    "§f· Cada una da un intento del §f5★ §fdel día.",
                    "§f",
                    "§bBOLITA AZUL §7— banner permanente",
                    "§f· Crafteo: §fhierro §fal centro rodeado de §flapis",
                    "§f  §fen cruz §f(los §f4 §flapis + §fhierro§f).",
                    "§f· Da un intento del pool de las §f7 §fclásicas.",
                    "§f· Se craftean en cualquier mesa: no hace falta",
                    "§f  horno ni máquina."
            },
            { // 4
                    "§6DÓNDE SE TIRA",
                    "§f",
                    "§6TERMINAL GACHA",
                    "§f· Crafteo: tres bloques de amatista arriba,",
                    "§f  diamante al centro y cobblestone abajo.",
                    "§f· §fClic §f— abre la pantalla con las dos pestañas:",
                    "§f  §cPROMOCIONAL §fy §bPERMANENTE§f. Ahí ves tu pity,",
                    "§f  la garantía y las bolitas que llevas.",
                    "§f· §fShift + clic §f— x10 instantáneo del promocional,",
                    "§f  por el chat, sin abrir nada.",
                    "§f",
                    "§6MÁQUINA GACHA",
                    "§f· El bloque que se coloca en el mundo: mismo",
                    "§f  sorteo y mismo gasto, versión decorativa."
            },
            { // 5
                    "§dPROBABILIDADES REALES",
                    "§f",
                    "§6RANGO 5★ §7(la waifu del banner)",
                    "§f· Base: §a1,6 % §fpor tirada.",
                    "§f· §eSoft pity§f: desde la tirada §f51 §fsube un",
                    "§f  §a6 % §facumulativo en cada tirada.",
                    "§f· §cHard pity§f: en la tirada §f64 §fes tuya sí o sí.",
                    "§f· Al sacarla hay un §f50 % §fde que caiga la",
                    "§f  destacada. Si fallas, te llevas una waifu",
                    "§f  del pool estándar de consuelo.",
                    "§f",
                    "§eCONSEJO",
                    "§f· El contador de pity se ve en la pantalla del",
                    "§f  Terminal, y §a/tirada §ftambién lo imprime."
            },
            { // 6
                    "§d4★, DOBLES Y EL PERMANENTE",
                    "§6RANGO 4★ §7(el resto del roster)",
                    "§f· §a10 % §fpor tirada, con pity propia:",
                    "§f  garantizada en la tirada §f32§f.",
                    "§f· §a15 % §fde que una tirada salga doble.",
                    "§f· La destacada del banner rota cada día de",
                    "§f  Minecraft: §a/banner §fdice quién es hoy.",
                    "§f",
                    "§bBANNER PERMANENTE §7(bolitas azules)",
                    "§f· Sin 50 %: entrega directa de una de las §f7",
                    "§f  clásicas que aún no tengas.",
                    "§f· Su pity (§f64§f) va aparte de la del promocional.",
                    "§f· Los fallos no son vacíos: dan §6comida§f.",
                    "§f· Es la vía más barata: solo lapis y hierro."
            },
            { // 7
                    "§aTOKENS Y JEFES",
                    "§f",
                    "§fUn token invoca a su waifu en el bloque que",
                    "§fmiras: clic derecho al suelo. Si ya la tienes",
                    "§finvocada a menos de §f128 §fbloques, no hace nada.",
                    "§f",
                    "§6LOS JEFES SUELTAN BOLITAS",
                    "§f· §cRicardo Milos §f— §c10 §fbolitas rosas.",
                    "§f· §cFernanfloo §f— §c3 §fbolitas rosas.",
                    "§f· §bDoctor House §f— §b5 §fbolitas azules.",
                    "§f· §bTung Tung Sahur §f— §b4 §fbolitas azules.",
                    "§f· §7Curly §fy §7Wilson §fno dan bolitas: pelean por",
                    "§f  el honor (y por el logro).",
                    "§f· Sus huevos están en la pestaña creativa."
            },
            { // 8
                    "§eMISIONES Y LÍMITE DE EQUIPO",
                    "§f",
                    "§6MISIONES DIARIAS",
                    "§f· §a/misiones §fmuestra los objetivos de hoy y",
                    "§f  cuánto llevas hecho.",
                    "§f· Se pagan con bolitas, tokens y comida.",
                    "§f· Rotan con el día de Minecraft.",
                    "§f",
                    "§6LÍMITE DE WAIFUS INVOCADAS",
                    "§f· Menú Mods → Gacha Waifus → Config → sección",
                    "§f  §fteam§f. §f0 §fes ilimitado; §f1-64 §fes el tope.",
                    "§f· Con tope puesto, los tokens y la cápsula no",
                    "§f  invocan más: guarda a una con Shift + clic",
                    "§f  en ella y libera el hueco (página 10)."
            },
            { // 9
                    "§aWAIFUS SALVAJES",
                    "§f",
                    "§fPor el mundo deambulan waifus sin dueño, cada",
                    "§fbioma con su lista. Se doman dándoles §cBolitas",
                    "§fRosas §fa clic: §f1 §fpor intento.",
                    "§f",
                    "§6LAS CIFRAS",
                    "§f· Probabilidad por intento: §a1 de 110 §f(0,9 %).",
                    "§f  Es más caro que el gacha, pero la salvaje te",
                    "§f  espera quieta: el contador no se reinicia.",
                    "§f· §ePity de doma§f: al intento §f128 §fse doman fijo.",
                    "§f· No se doman las que ya posees: el intento ni",
                    "§f  se paga.",
                    "§f· §a/biomas §fdice qué toca en tu bioma actual."
            },
            { // 10
                    "§dCÁPSULA WAIFU",
                    "§f",
                    "§fCrafteo: un §fcobblestone §fsobre un §fpalo§f.",
                    "§6QUÉ HACE",
                    "§f· §fClic §f— abre tu colección: todas las que",
                    "§f  tienes, invocadas o guardadas.",
                    "§f· §fShift + clic sobre tu waifu §f— la guardas.",
                    "§f  Se despawnea segura, sin morir por el camino.",
                    "§f· Clic en una entrada de la colección: la",
                    "§f  invocas a tu lado.",
                    "§f",
                    "§6REVIVIR",
                    "§f· Si una waifu cae en combate, su alma queda en",
                    "§f  la cápsula: la revives ahí con §a4 diamantes§f."
            },
            { // 11
                    "§eMODOS DE COMBATE",
                    "§f",
                    "§fSe cambia con §fShift + clic con la MANO VACÍA",
                    "§fsobre ella§f. El ciclo siempre va en este orden:",
                    "§a· PASIVO §f— no ataca a nada. Sus habilidades",
                    "§f  tampoco se disparan.",
                    "§e· NEUTRO §f— solo defiende: va a lo que le pegue",
                    "§f  a ella o a ti.",
                    "§c· AGRESIVO §f— embiste a los hostiles de cerca.",
                    "§9· SENTADA §f— quieta donde está: no sigue, no",
                    "§f  pasea, no se teletransporta y no ataca.",
                    "§2· EN CASA §f— custodia su hogar (página 12).",
                    "§f· El modo se guarda en la entidad: sobrevive al",
                    "§f  recargar el mundo."
            },
            { // 12
                    "§2HOGAR Y BALIZA",
                    "§2BALIZA DE HOGAR §f— crafteo en columna: placa",
                    "§f  de peso de oro ligera, §fperla de ender §fy §fcobre§f.",
                    "§f· §fClic §f— trae a §ftodas §ftus waifus invocadas a",
                    "§f  tu lado, aunque estén en §fotra dimensión§f.",
                    "§f· §fClic sobre una tuya §f— solo ella viene.",
                    "§f· §fShift + clic sobre tu waifu §f— le fija el",
                    "§f  §fhogar §fen el bloque que miras.",
                    "§f· §fShift + clic en un bloque §f— hogar de todas",
                    "§f  las tuyas en ese bloque.",
                    "§f· §fShift + clic al aire §f— hogar a tus pies.",
                    "§2MODO EN CASA §f— con hogar asignado se queda de",
                    "§f  guardia ahí en vez de seguirte, y ataca a lo",
                    "§f  que se le acerque dentro de su radio."
            },
            { // 13
                    "§6SE PERDIÓ: AUTO-RECUERDO",
                    "§f",
                    "§fVanilla falla en tres casos, y los tres son el",
                    "§f«mi waifu se perdió»: dueño en §fotra dimensión",
                    "§f(no la ve), §fsin camino §f(atascada en su base) y",
                    "§fchunk §fsin cargar §fmientras te alejabas.",
                    "§f",
                    "§6EL MOD LO ARREGLA SOLO",
                    "§f· Cada §f5 §fsegundos el servidor revisa todas las",
                    "§f  dimensiones. Si una está muy lejos de su destino",
                    "§f  §fy quieta §fdos revisas seguidas, la trae a tu",
                    "§f  lado (o la manda a su hogar si está en modo",
                    "§f  §2En casa§f) y te avisa por la barra de acción.",
                    "§f· La que aún va caminando hacia ti no se jala."
            },
            { // 14
                    "§dCONFIGURACIÓN",
                    "§f",
                    "§fTodo se ajusta en §fMenú Mods → Gacha Waifus →",
                    "§fConfig§f, o editando §fgachawaifus-common.toml§f.",
                    "§f· §fteam§f — límite de waifus invocadas (§f0 §f= libre).",
                    "§f· §fwander§f — radio de deambulación alrededor tuyo.",
                    "§f· §frecall§f — auto-recuerdo: activarlo, cruzar o no",
                    "§f  dimensiones y distancia de aviso (§f96 §fbloques).",
                    "§f· §fbosses§f — rareza de sus apariciones naturales.",
                    "§f· §fmissions§f — objetivos diarios.",
                    "§f· §fwild§f — cuántas waifus salvajes por bioma y con",
                    "§f  qué probabilidad de doma.",
                    "§f· Es config §fCOMMON§f: la lee también el servidor",
                    "§f  dedicado y la comparten todos los jugadores."
            },
            { // 15
                    "§dLOS ONCE COLORES",
                    "§f",
                    "§fCada waifu es de un color, y el color es la seña:",
                    "§fno son elementos, son la clasificación del mod.",
                    "§f",
                    "§c· rojo §6· naranja §e· amarillo §a· verde",
                    "§b· azul §d· morado §d· rosa §9· marrón",
                    "§8· negro §f· blanco §7· gris",
                    "§f",
                    "§6PARA QUÉ SIRVE",
                    "§f· El color cambia el daño una §a±33 % §fcontra",
                    "§f  otros colores. La matriz la aplica el mod sola.",
                    "§f· Cada token lleva el color escrito en su tooltip.",
                    "§f· §a/gwcolor §fmuestra el tuyo y el de cada waifu."
            },
            { // 16
                    "§aCOMANDOS §7(los escribes en el chat)",
                    "§a/inicio §f— tu waifu de regalo.",
                    "§a/guia §f— este manual.",
                    "§a/tirada §f— tirada del día por chat.",
                    "§a/banner §f— quién es la destacada de hoy.",
                    "§a/coleccion §f— tus poseídas.",
                    "§a/coleccion cinco §f| §acuatro §f— por rango.",
                    "§a/misiones §f— objetivos de hoy.",
                    "§a/biomas §f— salvajes de esta zona.",
                    "§a/aqui §f— dónde están las tuyas y su dimensión.",
                    "§a/list §f— roster completo.",
                    "§a/gwcolor §f— colores de la colección.",
                    "§8Fan content sin ánimo de lucro: los modelos y",
                    "§8las texturas son recreaciones propias del mod."
            },
    };
}
