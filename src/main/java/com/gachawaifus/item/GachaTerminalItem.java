package com.gachawaifus.item;

import com.gachawaifus.gacha.GachaSavedData;
import com.gachawaifus.gacha.WaifuRoster;
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

        if (WaifuRoster.owns(player, featured)) {
            player.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] Ya tienes a §f" + featured.name() + "§e. La destacada cambia cada día de Minecraft — vuelve mañana."));
            return InteractionResultHolder.fail(stack);
        }

        int pulls = player.isShiftKeyDown() ? 10 : 1;
        if (countDiamonds(player) < pulls) {
            player.sendSystemMessage(Component.literal(
                    "§c[GachaWaifus] Necesitas §f" + pulls + " diamante(s)§c para tirar (tienes " + countDiamonds(player) + ")."));
            return InteractionResultHolder.fail(stack);
        }
        consumeDiamonds(player, pulls);

        List<String> foodNames = new ArrayList<>();
        int fiveStars = 0;
        for (int i = 0; i < pulls; i++) {
            int pullNumber = state.pity + 1;
            double chance = BASE_RATE + (pullNumber > SOFT_PITY_START ? SOFT_PITY_STEP * (pullNumber - SOFT_PITY_START) : 0.0D);
            boolean fiveStar = pullNumber >= HARD_PITY || player.getRandom().nextDouble() < chance;

            if (fiveStar) {
                fiveStars++;
                WaifuRoster.Entry won = resolveFiveStar(player, featured, state);
                state.pity = 0;
                if (won != null) {
                    giveToken(player, won);
                    announceFiveStar(serverLevel, player, won, featured);
                } else {
                    // Colección completa a mitad de tirada múltiple: premio de comida
                    giveFood(player, level.getRandom(), foodNames);
                }
            } else {
                state.pity++;
                giveFood(player, level.getRandom(), foodNames);
            }
        }
        data.setDirty();

        if (pulls > 1) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Tirada x10: §d" + fiveStars + "× 5★§7, §f" + foodNames.size() + " comidas§7. Pity: §b" + state.pity + "/64"));
        } else if (fiveStars == 0) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] " + featured.name() + " te cocinó: §f" + String.join("§7, §f", foodNames)
                            + "§7. Pity: §b" + state.pity + "/64"));
        }
        return InteractionResultHolder.consume(stack);
    }

    private WaifuRoster.Entry resolveFiveStar(Player player, WaifuRoster.Entry featured, GachaSavedData.PlayerState state) {
        if (state.guaranteed) {
            state.guaranteed = false;
            return WaifuRoster.owns(player, featured) ? null : featured;
        }
        if (player.getRandom().nextBoolean()) {
            return WaifuRoster.owns(player, featured) ? null : featured;
        }
        if (!WaifuRoster.owns(player, WaifuRoster.NICOLE)) {
            state.guaranteed = true;
            return WaifuRoster.NICOLE;
        }
        // Nicole ya poseída: la "derrota" del 50/50 cae a la destacada
        return WaifuRoster.owns(player, featured) ? null : featured;
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

    private int countDiamonds(Player player) {
        int count = 0;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.DIAMOND)) count += s.getCount();
        }
        return count;
    }

    private void consumeDiamonds(Player player, int amount) {
        int remaining = amount;
        net.minecraft.world.entity.player.Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(Items.DIAMOND)) {
                int take = Math.min(s.getCount(), remaining);
                s.shrink(take);
                remaining -= take;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.gacha_terminal.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
