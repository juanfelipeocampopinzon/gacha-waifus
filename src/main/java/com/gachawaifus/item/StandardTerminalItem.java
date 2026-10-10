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

/**
 * Banner PERMANENTE: gasta {@link ModItems#BLUE_BALL} y solo entrega waifus del
 * {@link WaifuRoster#STANDARD_POOL} (las 7 estándar del 50/50 perdido). Sin destacada,
 * sin 50/50 y sin garantizada: cada 5★ es una estándar que no tengas, al azar. Pity propia
 * ({@code standardPity}), independiente del terminal destacado.
 */
public class StandardTerminalItem extends Item {

    private static final double BASE_RATE = 0.016D;
    private static final double SOFT_PITY_STEP = 0.06D;
    private static final int SOFT_PITY_START = 50;
    private static final int HARD_PITY = 64;

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

    public StandardTerminalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        GachaSavedData data = GachaSavedData.get(level);
        GachaSavedData.PlayerState state = data.state(player.getUUID());

        if (standardUnowned(player).isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] Ya tienes a las §f" + WaifuRoster.STANDARD_POOL.size()
                            + "§e waifus estándar: el banner permanente no da más. No se gastan bolitas azules."));
            return InteractionResultHolder.fail(stack);
        }

        int pulls = player.isShiftKeyDown() ? 10 : 1;
        if (countBlueBalls(player) < pulls) {
            player.sendSystemMessage(Component.literal(
                    "§c[GachaWaifus] Necesitas §f" + pulls + " Bolita(s) Azul(es)§c para tirar (tienes " + countBlueBalls(player) + ")."
                            + " §7Se craftean con un lingote de hierro en el centro y lapislázuli en cruz."));
            return InteractionResultHolder.fail(stack);
        }
        consumeBlueBalls(player, pulls);

        List<String> foodNames = new ArrayList<>();
        List<String> wonNames = new ArrayList<>();
        int fiveStars = 0;
        for (int i = 0; i < pulls; i++) {
            int pullNumber = state.standardPity + 1;
            double chance = BASE_RATE + (pullNumber > SOFT_PITY_START ? SOFT_PITY_STEP * (pullNumber - SOFT_PITY_START) : 0.0D);
            boolean fiveStar = pullNumber >= HARD_PITY || player.getRandom().nextDouble() < chance;

            if (fiveStar) {
                WaifuRoster.Entry won = randomStandardUnowned(player);
                if (won == null) {
                    // Ya las tiene todas (puede pasar a mitad de una x10): comida y la tirada
                    // cuenta como fallida, sin resetear la pity.
                    state.standardPity++;
                    giveFood(player, level.getRandom(), foodNames);
                    continue;
                }
                fiveStars++;
                state.standardPity = 0;
                giveToken(player, won);
                wonNames.add(won.name());
                announceFiveStar(serverLevel, player, won);
            } else {
                state.standardPity++;
                giveFood(player, level.getRandom(), foodNames);
            }
        }
        data.setDirty();

        if (pulls > 1) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Tirada permanente x10: §b" + fiveStars + "× 5★§7"
                            + (wonNames.isEmpty() ? "" : " §f(" + String.join("§7, §f", wonNames) + ")§7")
                            + ", §f" + foodNames.size() + " comida(s)§7. Pity: §b"
                            + state.standardPity + "/" + HARD_PITY));
        } else if (fiveStars == 0) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] El banner permanente te cocinó: §f" + String.join("§7, §f", foodNames)
                            + "§7. Pity: §b" + state.standardPity + "/" + HARD_PITY));
        } else {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Pity: §b" + state.standardPity + "/" + HARD_PITY));
        }
        return InteractionResultHolder.consume(stack);
    }

    private List<WaifuRoster.Entry> standardUnowned(Player player) {
        List<WaifuRoster.Entry> libres = new ArrayList<>();
        for (WaifuRoster.Entry e : WaifuRoster.STANDARD_POOL) {
            if (!WaifuRoster.owns(player, e)) {
                libres.add(e);
            }
        }
        return libres;
    }

    private WaifuRoster.Entry randomStandardUnowned(Player player) {
        List<WaifuRoster.Entry> libres = standardUnowned(player);
        if (libres.isEmpty()) {
            return null;
        }
        return libres.get(player.getRandom().nextInt(libres.size()));
    }

    private void giveToken(Player player, WaifuRoster.Entry entry) {
        ItemStack token = new ItemStack(entry.token().get());
        if (!player.getInventory().add(token)) {
            player.drop(token, false);
        }
    }

    private void announceFiveStar(ServerLevel level, Player player, WaifuRoster.Entry won) {
        player.sendSystemMessage(Component.literal(
                "§b§l★★★★★ ¡5★ ESTÁNDAR! §f" + won.name()));
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

    /** Cuántas bolitas azules lleva el jugador encima (inventario + mano secundaria). */
    private int countBlueBalls(Player player) {
        int count = 0;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.BLUE_BALL.get())) count += s.getCount();
        }
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(ModItems.BLUE_BALL.get())) count += offhand.getCount();
        return count;
    }

    private void consumeBlueBalls(Player player, int amount) {
        int remaining = amount;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(ModItems.BLUE_BALL.get())) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (remaining > 0 && offhand.is(ModItems.BLUE_BALL.get())) {
            offhand.shrink(Math.min(offhand.getCount(), remaining));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.standard_terminal.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
