package com.gachawaifus.gacha;

import com.gachawaifus.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * El sorteo del gacha, en un solo sitio.
 *
 * <p>Antes la lógica vivía duplicada dentro de {@code GachaTerminalItem} (banner promocional) y
 * del antiguo terminal del banner permanente: mismas constantes, misma tabla de comida y
 * mismos contadores de bolitas copiados dos veces. Ahora los ítems, el bloque Máquina Gacha y la
 * pantalla {@code GachaBannerScreen} llaman aquí, así que pity, 50/50, garantizada, rango 4★,
 * tirada doble y mensajes no se pueden bifurcar.
 *
 * <p>Todo esto corre <b>solo en el servidor</b>: la pantalla únicamente manda clics y pinta lo que
 * le llega por {@code ContainerData} y por los slots de resultado.
 */
public final class GachaService {

    /** Los dos banners del mod. */
    public enum Banner {
        /** Banner PROMOCIONAL: la destacada de la rotación diaria, 50/50 y garantizada. Bolita Rosa. */
        PROMO,
        /** Banner PERMANENTE: solo el pool estándar, sin destacada ni 50/50. Bolita Azul. */
        STANDARD
    }

    /* ── Probabilidades (banner promocional) ──────────────────────────────────────────────── */
    /** Probabilidad base de 5★ por tirada. */
    public static final double BASE_RATE = 0.016D;
    /** Cuánto sube la probabilidad por tirada pasada la {@link #SOFT_PITY_START}. */
    public static final double SOFT_PITY_STEP = 0.06D;
    /** Tirada a partir de la cual empieza a subir la probabilidad. */
    public static final int SOFT_PITY_START = 50;
    /** Pity dura del 5★: a la 64 cae seguro. */
    public static final int HARD_PITY = 64;

    /** Probabilidad de <b>4★</b> en una tirada que no dio 5★ (la vía fácil del Terminal). */
    public static final double FOUR_STAR_RATE = 0.10D;
    /** Pity dura de la 4★: si llegan 32 tiradas sin ninguna, cae una segura. */
    public static final int HARD_PITY_FOUR = 32;

    /** Probabilidad de <b>tirada doble</b>: cae también la siguiente waifu de la rotación. */
    public static final float DOUBLE_CHANCE = 0.15F;

    /** Comida del fallo, con pesos (misma tabla para los dos banners). */
    private static final List<FoodPrize> FOOD_TABLE = List.of(
            new FoodPrize(Items.COOKIE, 5),
            new FoodPrize(Items.BREAD, 4),
            new FoodPrize(Items.APPLE, 4),
            new FoodPrize(Items.COOKED_BEEF, 4),
            new FoodPrize(Items.BAKED_POTATO, 3),
            new FoodPrize(Items.COOKED_PORKCHOP, 3),
            new FoodPrize(Items.COOKED_CHICKEN, 3),
            new FoodPrize(Items.COOKED_SALMON, 2),
            new FoodPrize(Items.GOLDEN_CARROT, 2),
            new FoodPrize(Items.PUMPKIN_PIE, 2),
            new FoodPrize(Items.ENCHANTED_GOLDEN_APPLE, 1)
    );

    private record FoodPrize(Item item, int weight) {
    }

    /**
     * Resultado de una tirada. Si {@link #success} es {@code false}, {@link #denial} trae el motivo
     * (ya con formato de chat) y no se ha gastado nada.
     */
    public static final class Result {
        public final Banner banner;
        public boolean success = true;
        @Nullable
        public String denial;

        /** Waifus entregadas, en el orden en que salieron (incluye las de la tirada doble). */
        public final List<WaifuRoster.Entry> waifus = new ArrayList<>();
        /** Un ItemStack por comida entregada (para pintar el resultado en la GUI). */
        public final List<ItemStack> food = new ArrayList<>();

        public int fiveStars;
        public int fourStars;

        /* Estado de la pity DESPUÉS de la tirada. */
        public int pity;
        public int pity4;
        public int standardPity;
        public boolean guaranteed;

        Result(Banner banner) {
            this.banner = banner;
        }

        void deny(String message) {
            this.success = false;
            this.denial = message;
        }
    }

    private GachaService() {
    }

    /* ══ API pública ══════════════════════════════════════════════════════════════════════ */

    /**
     * Cuántas bolitas del banner lleva el jugador encima.
     *
     * <p>Se recorre el inventario entero ({@code getContainerSize()} ya incluye armadura y mano
     * secundaria). Antes se sumaba la mano secundaria otra vez por fuera y las bolitas del offhand
     * contaban doble.
     */
    public static int countBalls(Player player, Banner banner) {
        Item wanted = ballItem(banner);
        int count = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(wanted)) count += s.getCount();
        }
        return count;
    }

    /** El ítem que gasta ese banner: Bolita Rosa (promocional) o Bolita Azul (permanente). */
    public static Item ballItem(Banner banner) {
        return banner == Banner.PROMO ? ModItems.PINK_BALL.get() : ModItems.BLUE_BALL.get();
    }

    /**
     * ¿Queda algo que este banner pueda entregar? El promocional, cualquier waifu del roster; el
     * permanente, alguna de las 7 estándar. Si no, el botón sale desactivado y no se cobra.
     *
     * <p>Se apoya en {@link WaifuRoster#ownedIds(Player)} (un solo barrido) en vez de preguntar
     * waifu por waifu: {@code owns()} recorre 128 bloques de entidades y con 60 waifus eso eran 60
     * barridos seguidos.
     */
    public static boolean hasTargets(Player player, Banner banner) {
        return hasTargets(banner, WaifuRoster.ownedIds(player));
    }

    /** La waifu que enseña el banner: la destacada del día o la del pool estándar que toque. */
    public static WaifuRoster.Entry spotlight(ServerLevel level, Banner banner) {
        if (banner == Banner.PROMO) {
            return WaifuRoster.featured(level);
        }
        long day = level.getServer().overworld().getDayTime() / 24000L;
        return WaifuRoster.STANDARD_POOL.get((int) Math.floorMod(day, WaifuRoster.STANDARD_POOL.size()));
    }

    /**
     * Igual que {@link #hasTargets(Player, Banner)} pero con la lista de ids ya calculada: el menú
     * llama a {@code WaifuRoster.ownedIds()} una vez cada 10 ticks y reutiliza el resultado.
     */
    public static boolean hasTargets(Banner banner, Set<String> ownedIds) {
        if (banner == Banner.PROMO) {
            for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
                if (!ownedIds.contains(entry.id())) return true;
            }
            return false;
        }
        for (WaifuRoster.Entry entry : WaifuRoster.STANDARD_POOL) {
            if (!ownedIds.contains(entry.id())) return true;
        }
        return false;
    }

    /** Máscara de bits del pool estándar: bit {@code i} encendido = ya la tienes. */
    public static int standardOwnedMask(Set<String> ownedIds) {
        int mask = 0;
        for (int i = 0; i < WaifuRoster.STANDARD_POOL.size(); i++) {
            if (ownedIds.contains(WaifuRoster.STANDARD_POOL.get(i).id())) {
                mask |= 1 << i;
            }
        }
        return mask;
    }

    /** Máscara de bits del pool estándar: bit {@code i} encendido = ya la tienes. */
    public static int standardOwnedMask(Player player) {
        return standardOwnedMask(WaifuRoster.ownedIds(player));
    }

    /**
     * Hace la tirada: descuenta las bolitas, sortea y entrega los premios (al inventario o al
     * suelo). Manda los avisos de chat/partículas/sonido, igual que antes.
     *
     * @param count 1 o 10 (la pantalla y el atajo Shift + clic usan estos mismos valores)
     */
    public static Result pull(ServerPlayer player, Banner banner, int count) {
        Result result = banner == Banner.PROMO ? pullPromo(player, count) : pullStandard(player, count);
        // El motivo del rechazo se avisa AQUÍ (una sola vez), no en cada llamante: el atajo de
        // Shift + clic de los terminales y de la Máquina Gacha se saltaba el aviso y el jugador
        // pulsaba sin que pasara nada.
        if (!result.success && result.denial != null) {
            player.sendSystemMessage(Component.literal(result.denial));
        }
        return result;
    }

    /* ══ Banner promocional ═══════════════════════════════════════════════════════════════ */

    private static Result pullPromo(ServerPlayer player, int count) {
        Result result = new Result(Banner.PROMO);
        ServerLevel level = player.serverLevel();
        GachaSavedData data = GachaSavedData.get(level);
        GachaSavedData.PlayerState state = data.state(player.getUUID());

        if (!hasTargets(player, Banner.PROMO)) {
            // Colección completa: NO se cobra nada ni se toca la pity. Tener a la destacada nunca
            // bloquea (antes bloqueaba todas las tiradas y el terminal quedaba inservible).
            result.deny("§e[GachaWaifus] Ya tienes a §f" + WaifuRoster.ROTATION.size()
                    + "§e waifus: la colección está completa. No se gastan bolitas.");
            return result;
        }

        WaifuRoster.Entry featured = WaifuRoster.featured(level);

        int balls = countBalls(player, Banner.PROMO);
        if (balls < count) {
            result.deny("§c[GachaWaifus] Necesitas §f" + count + " Bolita(s) Rosa(s)§c para tirar (tienes "
                    + balls + "). §7Se craftean con un diamante en el centro y cobre, lapislázuli, hierro y"
                    + " carbón en cruz, y cada día recibes una gratis al entrar.");
            return result;
        }
        consumeBalls(player, Banner.PROMO, count);

        if (WaifuRoster.owns(player, featured)) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Ya tienes a §f" + featured.name()
                            + "§7: si sale un 5★ te dará otra que no tengas (o una copia de repuesto)."));
        }

        List<String> foodNames = new ArrayList<>();
        List<String> wonNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int pullNumber = state.pity + 1;
            double chance = BASE_RATE + (pullNumber > SOFT_PITY_START
                    ? SOFT_PITY_STEP * (pullNumber - SOFT_PITY_START) : 0.0D);
            boolean fiveStar = pullNumber >= HARD_PITY || player.getRandom().nextDouble() < chance;

            if (fiveStar) {
                WaifuRoster.Entry won = resolveFiveStar(player, featured, state);
                if (won == null) {
                    // No queda nada por conseguir: se devuelve la tirada como comida y NO se toca
                    // la pity (antes se reseteaba a 0 y se quemaba la garantía para nada).
                    state.pity++;
                    result.food.add(giveFood(player, level.getRandom(), foodNames));
                    continue;
                }
                result.fiveStars++;
                result.waifus.add(won);
                state.pity = 0;
                // Un 5★ manda sobre la vía fácil: la pity 4★ también se reinicia.
                state.pity4 = 0;
                giveToken(player, won);
                wonNames.add(won.name());
                announceFiveStar(level, player, won, featured);

                // Tirada DOBLE: un porcentaje de que caiga TAMBIÉN la siguiente de la rotación.
                if (player.getRandom().nextFloat() < DOUBLE_CHANCE) {
                    WaifuRoster.Entry bonus = WaifuRoster.next(won);
                    if (bonus != null && WaifuRoster.owns(player, bonus)) {
                        // La siguiente ya es tuya: cae otra que falte.
                        bonus = WaifuRoster.randomUnowned(player, won);
                    }
                    if (bonus != null && !WaifuRoster.owns(player, bonus)) {
                        result.waifus.add(bonus);
                        giveToken(player, bonus);
                        wonNames.add(bonus.name());
                        announceDouble(level, player, bonus);
                    }
                }
            } else {
                state.pity++;
                if (state.pity4 + 1 >= HARD_PITY_FOUR || player.getRandom().nextDouble() < FOUR_STAR_RATE) {
                    WaifuRoster.Entry four = WaifuRoster.randomFourStarUnowned(player);
                    state.pity4 = 0;
                    if (four != null) {
                        result.fourStars++;
                        result.waifus.add(four);
                        giveToken(player, four);
                        wonNames.add(four.name());
                        announceFourStar(level, player, four);
                        continue;
                    }
                    // Ya tiene todas las 4★: la pity se reinicia igual y la tirada es comida.
                } else {
                    state.pity4++;
                }
                result.food.add(giveFood(player, level.getRandom(), foodNames));
            }
        }
        data.setDirty();

        // Resumen SIEMPRE, en x1 y en x10: antes una tirada simple con 5★ no decía nada.
        String pityLine = "§7Pity 5★ §b" + state.pity + "/" + HARD_PITY
                + (state.guaranteed ? "§7 · §e¡la próxima 5★ está garantizada!" : "")
                + "§7 · 4★ §e" + state.pity4 + "/" + HARD_PITY_FOUR + "§f.";
        if (count > 1) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Tirada x10: §d" + result.fiveStars + "× 5★§7, §e" + result.fourStars
                            + "× 4★§7" + (wonNames.isEmpty() ? "" : " §f(" + String.join("§7, §f", wonNames) + ")§7")
                            + ", §f" + result.food.size() + " comida(s)§7. " + pityLine));
        } else if (result.fiveStars == 0 && result.fourStars == 0) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] " + featured.name() + " te cocinó: §f"
                            + String.join("§7, §f", foodNames) + "§7. " + pityLine));
        } else {
            player.sendSystemMessage(Component.literal("§7[GachaWaifus] " + pityLine));
        }

        fillPity(result, state);
        return result;
    }

    private static WaifuRoster.Entry resolveFiveStar(Player player, WaifuRoster.Entry featured,
                                                     GachaSavedData.PlayerState state) {
        if (state.guaranteed) {
            // Garantizada: la destacada, aunque ya la tengas (se entrega como copia de repuesto).
            state.guaranteed = false;
            return featured;
        }
        if (player.getRandom().nextBoolean() && !WaifuRoster.owns(player, featured)) {
            return featured;
        }
        // 50/50 perdido (o la destacada ya es tuya): cae otra del POOL FIJO de 7 que no tengas,
        // AL AZAR, y la próxima 5★ queda garantizada.
        WaifuRoster.Entry consolation = WaifuRoster.randomStandardUnowned(player, featured);
        if (consolation != null) {
            state.guaranteed = true;
            return consolation;
        }
        // Colección completa: copia de repuesto de la destacada.
        return featured;
    }

    /* ══ Banner permanente ════════════════════════════════════════════════════════════════ */

    private static Result pullStandard(ServerPlayer player, int count) {
        Result result = new Result(Banner.STANDARD);
        ServerLevel level = player.serverLevel();
        GachaSavedData data = GachaSavedData.get(level);
        GachaSavedData.PlayerState state = data.state(player.getUUID());

        if (!hasTargets(player, Banner.STANDARD)) {
            result.deny("§e[GachaWaifus] Ya tienes a las §f" + WaifuRoster.STANDARD_POOL.size()
                    + "§e waifus estándar: el banner permanente no da más. No se gastan bolitas azules.");
            return result;
        }

        int balls = countBalls(player, Banner.STANDARD);
        if (balls < count) {
            result.deny("§c[GachaWaifus] Necesitas §f" + count + " Bolita(s) Azul(es)§c para tirar (tienes "
                    + balls + "). §7Se craftean con un lingote de hierro en el centro y lapislázuli en cruz.");
            return result;
        }
        consumeBalls(player, Banner.STANDARD, count);

        List<String> foodNames = new ArrayList<>();
        List<String> wonNames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int pullNumber = state.standardPity + 1;
            double chance = BASE_RATE + (pullNumber > SOFT_PITY_START
                    ? SOFT_PITY_STEP * (pullNumber - SOFT_PITY_START) : 0.0D);
            boolean fiveStar = pullNumber >= HARD_PITY || player.getRandom().nextDouble() < chance;

            if (fiveStar) {
                WaifuRoster.Entry won = randomStandardUnowned(player);
                if (won == null) {
                    // Ya las tiene todas (puede pasar a mitad de una x10): comida y la tirada
                    // cuenta como fallida, sin resetear la pity.
                    state.standardPity++;
                    result.food.add(giveFood(player, level.getRandom(), foodNames));
                    continue;
                }
                result.fiveStars++;
                result.waifus.add(won);
                state.standardPity = 0;
                giveToken(player, won);
                wonNames.add(won.name());
                announceStandardFiveStar(level, player, won);
            } else {
                state.standardPity++;
                result.food.add(giveFood(player, level.getRandom(), foodNames));
            }
        }
        data.setDirty();

        if (count > 1) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Tirada permanente x10: §b" + result.fiveStars + "× 5★§7"
                            + (wonNames.isEmpty() ? "" : " §f(" + String.join("§7, §f", wonNames) + ")§7")
                            + ", §f" + result.food.size() + " comida(s)§7. Pity: §b"
                            + state.standardPity + "/" + HARD_PITY));
        } else if (result.fiveStars == 0) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] El banner permanente te cocinó: §f"
                            + String.join("§7, §f", foodNames) + "§7. Pity: §b" + state.standardPity
                            + "/" + HARD_PITY));
        } else {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Pity: §b" + state.standardPity + "/" + HARD_PITY));
        }

        result.standardPity = state.standardPity;
        result.pity = state.pity;
        result.pity4 = state.pity4;
        result.guaranteed = state.guaranteed;
        return result;
    }

    @Nullable
    private static WaifuRoster.Entry randomStandardUnowned(Player player) {
        List<WaifuRoster.Entry> libres = new ArrayList<>();
        for (WaifuRoster.Entry e : WaifuRoster.STANDARD_POOL) {
            if (!WaifuRoster.owns(player, e)) libres.add(e);
        }
        if (libres.isEmpty()) return null;
        return libres.get(player.getRandom().nextInt(libres.size()));
    }

    /* ══ Entrega / economía ═══════════════════════════════════════════════════════════════ */

    private static void fillPity(Result result, GachaSavedData.PlayerState state) {
        result.pity = state.pity;
        result.pity4 = state.pity4;
        result.standardPity = state.standardPity;
        result.guaranteed = state.guaranteed;
    }

    private static void giveToken(Player player, WaifuRoster.Entry entry) {
        ItemStack token = new ItemStack(entry.token().get());
        if (!player.getInventory().add(token)) {
            player.drop(token, false);
        }
    }

    private static ItemStack giveFood(Player player, RandomSource random, List<String> foodNames) {
        int total = FOOD_TABLE.stream().mapToInt(FoodPrize::weight).sum();
        int roll = random.nextInt(total);
        FoodPrize prize = FOOD_TABLE.get(0);
        for (FoodPrize f : FOOD_TABLE) {
            roll -= f.weight();
            if (roll < 0) {
                prize = f;
                break;
            }
        }
        // OJO: Inventory.add() VACÍA la pila que recibe (le deja el sobrante). Por eso se entrega
        // una copia y se devuelve la pila intacta: es la que enseña la tira de resultado.
        ItemStack display = new ItemStack(prize.item());
        ItemStack forPlayer = display.copy();
        if (!player.getInventory().add(forPlayer)) {
            player.drop(forPlayer, false);
        }
        foodNames.add(display.getHoverName().getString());
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 0.6F, 1.4F);
        return display;
    }

    private static void consumeBalls(Player player, Banner banner, int amount) {
        Item wanted = ballItem(banner);
        int remaining = amount;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(wanted)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
    }

    /* ══ Avisos ═══════════════════════════════════════════════════════════════════════════ */

    /** Aviso de que la tirada ha sido doble: además de la 5★, cae la siguiente de la rotación. */
    private static void announceDouble(ServerLevel level, Player player, WaifuRoster.Entry bonus) {
        player.sendSystemMessage(Component.literal(
                "§b§l★★ TIRADA DOBLE ★★ §r§f¡También cae §b" + bonus.name() + "§f!"));
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(),
                3, 0.3, 0.5, 0.3, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(),
                30, 0.6, 0.8, 0.6, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.4F, 1.8F);
    }

    /** Aviso de una 4★: celebra menos que un 5★, pero se nota. */
    private static void announceFourStar(ServerLevel level, Player player, WaifuRoster.Entry won) {
        player.sendSystemMessage(Component.literal(
                "§e§l★★★★ §f¡" + won.name() + " §f(4★)§e es tuya!"));
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(),
                18, 0.4, 0.6, 0.4, 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.PLAYERS, 1.2F, 1.4F);
    }

    private static void announceFiveStar(ServerLevel level, Player player, WaifuRoster.Entry won,
                                         WaifuRoster.Entry featured) {
        boolean featuredWin = won.id().equals(featured.id());
        player.sendSystemMessage(Component.literal(
                (featuredWin ? "§d§l★★★★★ ¡5★ DESTACADA! " : "§c§l★★★★★ ¡5★! ")
                        + "§f" + won.name()
                        + (featuredWin ? "" : " §7(50/50 perdido — la próxima 5★ es garantizada)")));
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(),
                2, 0.2, 0.4, 0.2, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(),
                40, 0.6, 0.8, 0.6, 0.08);
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.4, player.getZ(),
                20, 0.5, 0.6, 0.5, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.5F, 1.2F);
    }

    private static void announceStandardFiveStar(ServerLevel level, Player player, WaifuRoster.Entry won) {
        player.sendSystemMessage(Component.literal("§b§l★★★★★ ¡5★ ESTÁNDAR! §f" + won.name()));
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(),
                2, 0.2, 0.4, 0.2, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(),
                40, 0.6, 0.8, 0.6, 0.08);
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.4, player.getZ(),
                20, 0.5, 0.6, 0.5, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.5F, 1.2F);
    }
}
