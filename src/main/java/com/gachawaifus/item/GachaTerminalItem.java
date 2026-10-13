package com.gachawaifus.item;

import com.gachawaifus.gacha.GachaSavedData;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class GachaTerminalItem extends Item {

    private static final double BASE_RATE = 0.016D;
    private static final double SOFT_PITY_STEP = 0.06D;
    private static final int SOFT_PITY_START = 50;
    private static final int HARD_PITY = 64;

    /**
     * Probabilidad de <b>4★</b> en una tirada que no dio 5★: la vía fácil para completar la
     * colección, con su propia pity aparte. Se resuelve contra
     * {@link WaifuRoster#FOUR_STAR}, así que clasificar una waifu nueva es mover su id de ahí.
     */
    private static final double FOUR_STAR_RATE = 0.10D;
    /** Pity dura de la 4★: si llegan 32 tiradas sin ninguna, cae una segura. */
    private static final int HARD_PITY_FOUR = 32;

    /**
     * Probabilidad de <b>tirada doble</b>: cuando sale un 5★, hay este porcentaje de que caiga
     * ADEMÁS la waifu siguiente de la rotación (dos de golpe). Subirlo o bajarlo aquí.
     */
    private static final float DOUBLE_CHANCE = 0.15F;

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

    public GachaTerminalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        WaifuRoster.Entry featured = WaifuRoster.featured(serverLevel);
        GachaSavedData data = GachaSavedData.get(level);
        GachaSavedData.PlayerState state = data.state(player.getUUID());

        if (WaifuRoster.anyUnowned(player) == null) {
            // Colección completa: NO se cobra nada ni se toca la pity. Tener a la destacada nunca
            // bloquea (antes bloqueaba todas las tiradas y el terminal quedaba inservible).
            player.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] Ya tienes a §f" + WaifuRoster.ROTATION.size()
                            + "§e waifus: la colección está completa. No se gastan bolitas."));
            return InteractionResultHolder.fail(stack);
        }
        if (WaifuRoster.owns(player, featured)) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Ya tienes a §f" + featured.name()
                            + "§7: si sale un 5★ te dará otra que no tengas (o una copia de repuesto)."));
        }

        int pulls = player.isShiftKeyDown() ? 10 : 1;
        if (countPinkBalls(player) < pulls) {
            player.sendSystemMessage(Component.literal(
                    "§c[GachaWaifus] Necesitas §f" + pulls + " Bolita(s) Rosa(s)§c para tirar (tienes " + countPinkBalls(player) + ")."
                    + " §7Se craftean con un diamante en el centro y cobre, lapislázuli, hierro y carbón en cruz,"
                    + " y cada día recibes una gratis al entrar."));
            return InteractionResultHolder.fail(stack);
        }
        consumePinkBalls(player, pulls);

        List<String> foodNames = new ArrayList<>();
        List<String> wonNames = new ArrayList<>();
        int fiveStars = 0;
        int fourStars = 0;
        for (int i = 0; i < pulls; i++) {
            int pullNumber = state.pity + 1;
            double chance = BASE_RATE + (pullNumber > SOFT_PITY_START ? SOFT_PITY_STEP * (pullNumber - SOFT_PITY_START) : 0.0D);
            boolean fiveStar = pullNumber >= HARD_PITY || player.getRandom().nextDouble() < chance;

            if (fiveStar) {
                WaifuRoster.Entry won = resolveFiveStar(player, featured, state);
                if (won == null) {
                    // No queda nada por conseguir: se devuelve la tirada como comida y NO se toca
                    // la pity (antes se reseteaba a 0 y se quemaba la garantía para nada).
                    state.pity++;
                    giveFood(player, level.getRandom(), foodNames);
                    continue;
                }
                fiveStars++;
                state.pity = 0;
                // Un 5★ manda sobre la vía fácil: la pity 4★ también se reinicia (no se cobra como
                // premio doble, simplemente no se acumula por encima de un premio mayor).
                state.pity4 = 0;
                giveToken(player, won);
                wonNames.add(won.name());
                announceFiveStar(serverLevel, player, won, featured);

                // Tirada DOBLE: un porcentaje de que caiga TAMBIÉN la siguiente de la rotación.
                if (player.getRandom().nextFloat() < DOUBLE_CHANCE) {
                    WaifuRoster.Entry bonus = WaifuRoster.next(won);
                    if (bonus != null && WaifuRoster.owns(player, bonus)) {
                        // La siguiente ya es tuya (por ejemplo, la domaste de salvaje): la doble no
                        // puede entregar duplicados, así que cae otra que falte.
                        bonus = WaifuRoster.randomUnowned(player, won);
                    }
                    if (bonus != null && !WaifuRoster.owns(player, bonus)) {
                        giveToken(player, bonus);
                        wonNames.add(bonus.name());
                        announceDouble(serverLevel, player, bonus);
                    }
                }
            } else {
                state.pity++;
                if (state.pity4 + 1 >= HARD_PITY_FOUR || player.getRandom().nextDouble() < FOUR_STAR_RATE) {
                    WaifuRoster.Entry four = WaifuRoster.randomFourStarUnowned(player);
                    state.pity4 = 0;
                    if (four != null) {
                        fourStars++;
                        giveToken(player, four);
                        wonNames.add(four.name());
                        announceFourStar(serverLevel, player, four);
                        continue;
                    }
                    // Ya tiene todas las 4★: la pity se reinicia igual (si no, cada tirada sería un
                    // 4★ garantizado que no entrega nada) y la tirada se resuelve como comida.
                } else {
                    state.pity4++;
                }
                giveFood(player, level.getRandom(), foodNames);
            }
        }
        data.setDirty();

        // Resumen SIEMPRE, en x1 y en x10: antes una tirada simple con 5★ no decía nada.
        String pityLine = "§7Pity 5★ §b" + state.pity + "/" + HARD_PITY
                + (state.guaranteed ? "§7 · §e¡la próxima 5★ está garantizada!" : "")
                + "§7 · 4★ §e" + state.pity4 + "/" + HARD_PITY_FOUR + "§f.";
        if (pulls > 1) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Tirada x10: §d" + fiveStars + "× 5★§7, §e" + fourStars + "× 4★§7"
                            + (wonNames.isEmpty() ? "" : " §f(" + String.join("§7, §f", wonNames) + ")§7")
                            + ", §f" + foodNames.size() + " comida(s)§7. " + pityLine));
        } else if (fiveStars == 0 && fourStars == 0) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] " + featured.name() + " te cocinó: §f" + String.join("§7, §f", foodNames)
                            + "§7. " + pityLine));
        } else {
            player.sendSystemMessage(Component.literal("§7[GachaWaifus] " + pityLine));
        }
        return InteractionResultHolder.consume(stack);
    }

    private WaifuRoster.Entry resolveFiveStar(Player player, WaifuRoster.Entry featured, GachaSavedData.PlayerState state) {
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
        // Colección completa: copia de repuesto de la destacada. Antes esto devolvía null y el 5★
        // se convertía en comida, gastando el pity para nada.
        return featured;
    }

    /** Aviso de que la tirada ha sido doble: además de la 5★, cae la siguiente de la rotación. */
    private void announceDouble(ServerLevel level, Player player, WaifuRoster.Entry bonus) {
        player.sendSystemMessage(Component.literal(
                "§b§l★★ TIRADA DOBLE ★★ §r§f¡También cae §b" + bonus.name() + "§f!"));
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 3, 0.3, 0.5, 0.3, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 30, 0.6, 0.8, 0.6, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 1.8F);
    }

    /** Aviso de una 4★: celebra menos que un 5★, pero se nota (es la vía fácil del Terminal). */
    private void announceFourStar(ServerLevel level, Player player, WaifuRoster.Entry won) {
        player.sendSystemMessage(Component.literal(
                "§e§l★★★★ §f¡" + won.name() + " §f(4★)§e es tuya!"));
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(),
                18, 0.4, 0.6, 0.4, 0.06);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.PLAYERS, 1.2F, 1.4F);
    }

    private void giveToken(Player player, WaifuRoster.Entry entry) {
        ItemStack token = new ItemStack(entry.token().get());
        if (!player.getInventory().add(token)) {
            player.drop(token, false);
        }
    }

    private void announceFiveStar(ServerLevel level, Player player, WaifuRoster.Entry won, WaifuRoster.Entry featured) {
        boolean featuredWin = won.id().equals(featured.id());
        player.sendSystemMessage(Component.literal(
                (featuredWin ? "§d§l★★★★★ ¡5★ DESTACADA! " : "§c§l★★★★★ ¡5★! ")
                        + "§f" + won.name() + (featuredWin ? "" : " §7(50/50 perdido — la próxima 5★ es garantizada)")));
        level.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.2, 0.4, 0.2, 0.0);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2, player.getZ(), 40, 0.6, 0.8, 0.6, 0.08);
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.4, player.getZ(), 20, 0.5, 0.6, 0.5, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.0F);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.2F);
    }

    private void giveFood(Player player, RandomSource random, List<String> foodNames) {
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
        ItemStack food = new ItemStack(prize.item());
        if (!player.getInventory().add(food)) {
            player.drop(food, false);
        }
        foodNames.add(new ItemStack(prize.item()).getHoverName().getString());
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    /** Cuántas bolitas rojas lleva el jugador encima (inventario + mano secundaria). */
    private int countPinkBalls(Player player) {
        int count = 0;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.PINK_BALL.get())) count += s.getCount();
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.PINK_BALL.get())) count += offhand.getCount();
        return count;
    }

    private void consumePinkBalls(Player player, int amount) {
        int remaining = amount;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.PINK_BALL.get())) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (remaining > 0 && offhand.is(ModItems.PINK_BALL.get())) {
            offhand.shrink(Math.min(offhand.getCount(), remaining));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.gacha_terminal.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
