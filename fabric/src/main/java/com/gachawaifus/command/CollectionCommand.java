package com.gachawaifus.command;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.gacha.WaifuRoster;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@code /coleccion} — cuánto lleva el jugador y qué le falta, partido por rangos.
 *
 * <ul>
 *   <li>{@code /coleccion}: resumen (5★ y 4★) y lo que queda por conseguir.</li>
 *   <li>{@code /coleccion cinco}: las 5★ de la rotación, todas delante.</li>
 *   <li>{@code /coleccion cuatro}: las 4★, que salen con su propio pity y son la vía rápida.</li>
 * </ul>
 *
 * <p>Cada nombre sale con su color de clasificación, así que de paso sirve de tabla para
 * {@code /gwcolor biomas}: con el color se sabe en qué clima hay que buscarla de salvaje.
 */
public final class CollectionCommand {

    private CollectionCommand() {
    }

        public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("coleccion")
                .executes(CollectionCommand::summary)
                .then(Commands.literal("cinco").executes(CollectionCommand::fiveStar))
                .then(Commands.literal("cuatro").executes(CollectionCommand::fourStar)));
        dispatcher.register(Commands.literal("collection")
                .executes(CollectionCommand::summary)
                .then(Commands.literal("five").executes(CollectionCommand::fiveStar))
                .then(Commands.literal("four").executes(CollectionCommand::fourStar)));
    }

    private static int summary(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Set<String> owned = WaifuRoster.ownedIds(player);
        List<WaifuRoster.Entry> cinco = list(false, owned);
        List<WaifuRoster.Entry> cuatro = list(true, owned);

        context.getSource().sendSuccess(() -> Component.translatable(
                "command.gachawaifus.collection.summary",
                WaifuRoster.ROTATION.size() - cinco.size() - cuatro.size(), WaifuRoster.ROTATION.size(),
                total(false) - cinco.size(), total(false),
                total(true) - cuatro.size(), total(true)), false);

        if (cinco.isEmpty() && cuatro.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.translatable("gui.gachawaifus.collection_command.1"), false);
            return 1;
        }
        print(context, Component.translatable("command.gachawaifus.collection.missing_five"), cinco);
        print(context, Component.translatable("command.gachawaifus.collection.missing_four"), cuatro);
        context.getSource().sendSuccess(() -> Component.translatable("gui.gachawaifus.collection_command.2"), false);
        return WaifuRoster.ROTATION.size() - cinco.size() - cuatro.size();
    }

    private static int fiveStar(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return range(context, false, "command.gachawaifus.collection.five_range");
    }

    private static int fourStar(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return range(context, true, "command.gachawaifus.collection.four_range");
    }

    /** Lista completa de un rango, separada entre lo conseguido y lo que falta. */
    private static int range(CommandContext<CommandSourceStack> context, boolean fourStar,
                             String titleKey) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Set<String> owned = WaifuRoster.ownedIds(player);
        List<WaifuRoster.Entry> todas = new ArrayList<>();
        List<WaifuRoster.Entry> conseguidas = new ArrayList<>();
        List<WaifuRoster.Entry> faltan = new ArrayList<>();
        for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
            if (WaifuRoster.isFourStar(entry) != fourStar) {
                continue;
            }
            todas.add(entry);
            (owned.contains(entry.id()) ? conseguidas : faltan).add(entry);
        }
        context.getSource().sendSuccess(() -> Component.translatable("command.gachawaifus.collection.range_head")
                .append(Component.translatable(titleKey))
                .append(Component.translatable("command.gachawaifus.collection.range_count", todas.size())), false);
        print(context, Component.translatable("command.gachawaifus.collection.conseguidas"), conseguidas);
        print(context, Component.translatable("command.gachawaifus.collection.faltan"), faltan);
        return todas.size();
    }

    // ---------------------------------------------------------------------

    /** Una línea por grupo; si el grupo está vacío no se pinta, para no sacar "Faltan: 0". */
    private static void print(CommandContext<CommandSourceStack> context, Component label,
                              List<WaifuRoster.Entry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        final Component line = label.copy()
                .append(Component.translatable("command.gachawaifus.collection.group_count", entries.size()))
                .append(names(entries));
        context.getSource().sendSuccess(() -> line, false);
    }

    /** Las waifus de ese rango que todavía no tiene. */
    private static List<WaifuRoster.Entry> list(boolean fourStar, Set<String> owned) {
        List<WaifuRoster.Entry> out = new ArrayList<>();
        for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
            if (WaifuRoster.isFourStar(entry) == fourStar && !owned.contains(entry.id())) {
                out.add(entry);
            }
        }
        return out;
    }

    private static int total(boolean fourStar) {
        int n = 0;
        for (WaifuRoster.Entry entry : WaifuRoster.ROTATION) {
            if (WaifuRoster.isFourStar(entry) == fourStar) {
                n++;
            }
        }
        return n;
    }

    /** Nombres unidos por comas, cada uno con el color de su clasificación. Lo usa también {@code /gwcolor}. */
    public static Component names(List<WaifuRoster.Entry> entries) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                out.append(Component.literal("§7, "));
            }
            WaifuRoster.Entry entry = entries.get(i);
            WaifuColor color = WaifuColors.of(entry.type().get());
            out.append(Component.literal((color == null ? "§f" : color.chatCode()) + entry.name()));
        }
        return out;
    }
}
