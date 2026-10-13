package com.gachawaifus.gacha;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.registry.ModItems;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.time.LocalDate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Misiones diarias: cada una completa regala <b>una Bolita Rosa</b>, así que un día entero de juego
 * rasta hasta 3 tiradas extra sin tocar la tirada fija de {@link DailyPulls}.
 *
 * <p>Las tres misiones y cómo se cuentan:
 * <ul>
 *   <li><b>Combate</b> — muertas por el propio jugador con {@link LivingDeathEvent}, solo mobs
 *       hostiles (los que implementan {@link Enemy}); las waifus y los animales no suman.</li>
 *   <li><b>Cocina</b> — unidades de comida <em>conseguídas</em>: se compara el inventario cada
 *       {@value #SCAN_PERIOD} ticks contra el último recuento. Cuenta cocinar, cosechar, pescar y
 *       saquear sin necesidad de un evento por cada fuente.</li>
 *   <li><b>Tala</b> — igual, pero con troncos del tag {@code #minecraft:logs}.</li>
 * </ul>
 *
 * <p>Contar por delta de inventario en vez de por eventos evita el agujero de &quot;empecé a picar
 * el tronco y no lo rompí&quot;, y castiga comerse la comida antes de contarla: el progreso se suma
 * cuando la comida <em>entra</em>, y lo que ya llevabas encima al cambiar el día no cuenta.
 *
 * <p>El estado vive en los datos persistentes del jugador (sobreviven a la muerte y al cambio de
 * dimensión), con el mismo criterio de día que {@link DailyPulls}: día de época del calendario real.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class DailyMissions {

    /** Día de época al que pertenecen los contadores guardados. */
    private static final String KEY_DAY = "gachawaifus_mission_day";
    /** Prefijo del progreso acumulado de una misión. */
    private static final String KEY_PROGRESS = "gachawaifus_mission_progress_";
    /** Prefijo del último recuento de inventario, para saber cuánto ha <em>entrado</em>. */
    private static final String KEY_REFERENCE = "gachawaifus_mission_ref_";

    private static final String ID_KILLS = "kills";
    private static final String ID_FOOD = "food";
    private static final String ID_LOGS = "logs";

    /** Cada cuántos ticks se compara el inventario (2 segundos: es un contador, no un reloj fino). */
    private static final int SCAN_PERIOD = 40;

    private DailyMissions() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Sin avisar: el jugador no tiene por qué recibir un mensaje por conectarse.
            rollDay(player);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!MissionConfig.active()) {
            return;
        }
        rollDay(player);
        addProgress(player, ID_KILLS, MissionConfig.kills(), "Combate", 1);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // El Post de PlayerTickEvent también corre en el lado cliente; ServerPlayer solo existe en el
        // servidor, así que el instanceof ya filtra las dos cosas.
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % SCAN_PERIOD != 0 || !MissionConfig.active()) {
            return;
        }
        rollDay(player);
        scan(player, ID_FOOD, MissionConfig.food(), "Cocina", DailyMissions::countFood);
        scan(player, ID_LOGS, MissionConfig.logs(), "Tala", DailyMissions::countLogs);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("misiones")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    rollDay(player);
                    listStatus(player);
                    return 1;
                }));
    }

    /** Reinicia los contadores si el día real cambió; deja de hacer nada si ya es de hoy. */
    private static void rollDay(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        long today = LocalDate.now().toEpochDay();
        if (data.getLong(KEY_DAY) == today) {
            return;
        }
        data.putLong(KEY_DAY, today);
        data.putInt(KEY_PROGRESS + ID_KILLS, 0);
        data.putInt(KEY_PROGRESS + ID_FOOD, 0);
        data.putInt(KEY_PROGRESS + ID_LOGS, 0);
        // La referencia arranca en lo que ya lleva encima: lo traído de casa no cuenta como misión.
        data.putInt(KEY_REFERENCE + ID_FOOD, countFood(player));
        data.putInt(KEY_REFERENCE + ID_LOGS, countLogs(player));
    }

    /** Suma progreso a una misión y entrega la bolita justo cuando llega al objetivo. */
    private static void addProgress(ServerPlayer player, String id, int goal, String label, int amount) {
        if (goal <= 0 || amount <= 0) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        int done = data.getInt(KEY_PROGRESS + id);
        if (done >= goal) {
            return;
        }
        int next = Math.min(goal, done + amount);
        data.putInt(KEY_PROGRESS + id, next);
        if (next >= goal) {
            reward(player, label);
        }
    }

    /** Misión de inventario: cuenta lo que ha <em>entrado</em> desde el último vistazo. */
    private static void scan(ServerPlayer player, String id, int goal, String label,
                             ToIntFunction<ServerPlayer> counter) {
        if (goal <= 0) {
            return;
        }
        CompoundTag data = player.getPersistentData();
        int current = counter.applyAsInt(player);
        int reference = data.getInt(KEY_REFERENCE + id);
        data.putInt(KEY_REFERENCE + id, current);
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

    /** Texto de {@code /misiones}: las tres líneas con su progreso, o el aviso de estar apagadas. */
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
        int done = player.getPersistentData().getInt(KEY_PROGRESS + id);
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

    /** Suma unidades del inventario principal y la mano secundaria que cumplan el filtro. */
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
