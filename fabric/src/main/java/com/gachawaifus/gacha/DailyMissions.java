package com.gachawaifus.gacha;

import com.gachawaifus.registry.ModItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class DailyMissions {

    private static final String ID_KILLS = "kills";
    private static final String ID_FOOD = "food";
    private static final String ID_LOGS = "logs";
    private static final int SCAN_PERIOD = 40;

    private static class PlayerMissions {
        long day;
        final Map<String, Integer> progress = new HashMap<>();
        final Map<String, Integer> reference = new HashMap<>();
    }

    private static final Map<UUID, PlayerMissions> PLAYER_DATA = new HashMap<>();

    private DailyMissions() {
    }

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            rollDay(handler.player);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (!(entity instanceof Enemy)) return;
            if (!(source.getEntity() instanceof ServerPlayer player)) return;
            if (!MissionConfig.active()) return;
            rollDay(player);
            addProgress(player, ID_KILLS, MissionConfig.kills(), "Combate", 1);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!MissionConfig.active()) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.tickCount % SCAN_PERIOD != 0) continue;
                rollDay(player);
                scan(player, ID_FOOD, MissionConfig.food(), "Cocina", DailyMissions::countFood);
                scan(player, ID_LOGS, MissionConfig.logs(), "Tala", DailyMissions::countLogs);
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(Commands.literal("misiones")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        rollDay(player);
                        listStatus(player);
                        return 1;
                    }));
        });
    }

    private static PlayerMissions getData(ServerPlayer player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), k -> new PlayerMissions());
    }

    private static void rollDay(ServerPlayer player) {
        PlayerMissions data = getData(player);
        long today = LocalDate.now().toEpochDay();
        if (data.day == today) {
            return;
        }
        data.day = today;
        data.progress.put(ID_KILLS, 0);
        data.progress.put(ID_FOOD, 0);
        data.progress.put(ID_LOGS, 0);
        data.reference.put(ID_FOOD, countFood(player));
        data.reference.put(ID_LOGS, countLogs(player));
    }

    private static void addProgress(ServerPlayer player, String id, int goal, String label, int amount) {
        if (goal <= 0 || amount <= 0) {
            return;
        }
        PlayerMissions data = getData(player);
        int done = data.progress.getOrDefault(id, 0);
        if (done >= goal) {
            return;
        }
        int next = Math.min(goal, done + amount);
        data.progress.put(id, next);
        if (next >= goal) {
            reward(player, label);
        }
    }

    private static void scan(ServerPlayer player, String id, int goal, String label,
                             ToIntFunction<ServerPlayer> counter) {
        if (goal <= 0) {
            return;
        }
        PlayerMissions data = getData(player);
        int current = counter.applyAsInt(player);
        int reference = data.reference.getOrDefault(id, 0);
        data.reference.put(id, current);
        if (current > reference) {
            addProgress(player, id, goal, label, current - reference);
        }
    }

    private static void reward(ServerPlayer player, String label) {
        ItemStack ball = new ItemStack(ModItems.PINK_BALL.get(), 1);
        if (!player.getInventory().add(ball)) {
            player.drop(ball, false);
        }
        player.sendSystemMessage(Component.literal(
                "§d[GachaWaifus] §f¡Misión cumplida! §7(" + label + ") §dHas ganado §f1 Bolita Rosa§d."
                        + " §7Mira las otras con §f/misiones§d."));
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS, 0.7F, 1.5F);
    }

    private static void listStatus(ServerPlayer player) {
        if (!MissionConfig.active()) {
            player.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] Las misiones diarias están apagadas en la config (missions.enabled)."));
            return;
        }
        player.sendSystemMessage(Component.literal("§d[GachaWaifus] §fMisiones de hoy §7(1 Bolita Rosa cada una)"));
        line(player, ID_KILLS, "§cCombate", "mobs hostiles abatidos", MissionConfig.kills());
        line(player, ID_FOOD, "§6Cocina", "comida conseguida", MissionConfig.food());
        line(player, ID_LOGS, "§aTala", "troncos conseguidos", MissionConfig.logs());
    }

    private static void line(ServerPlayer player, String id, String name, String what, int goal) {
        if (goal <= 0) {
            player.sendSystemMessage(Component.literal("§7" + name + " §7— desactivada"));
            return;
        }
        int done = getData(player).progress.getOrDefault(id, 0);
        if (done >= goal) {
            player.sendSystemMessage(Component.literal("§2✔ " + name + " §7— " + goal + "/" + goal + " " + what));
            return;
        }
        player.sendSystemMessage(Component.literal(
                "§f▸ " + name + " §7— §f" + done + "§7/" + goal + " " + what
                        + " §7(faltan " + (goal - done) + ")"));
    }

    private static int countFood(ServerPlayer player) {
        return countInInventory(player, stack -> stack.has(DataComponents.FOOD));
    }

    private static int countLogs(ServerPlayer player) {
        return countInInventory(player, stack -> stack.is(ItemTags.LOGS));
    }

    private static int countInInventory(ServerPlayer player, Predicate<ItemStack> filter) {
        Inventory inventory = player.getInventory();
        int total = 0;
        for (ItemStack stack : inventory.items) {
            if (filter.test(stack)) {
                total += stack.getCount();
            }
        }
        for (ItemStack stack : inventory.offhand) {
            if (filter.test(stack)) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
