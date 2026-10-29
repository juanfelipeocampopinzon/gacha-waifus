package com.gachawaifus.command;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.ColorCombat;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.color.WaifuColors;
import com.gachawaifus.entity.WildWaifuSpawner;
import com.gachawaifus.gacha.WaifuRoster;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.mojang.brigadier.CommandDispatcher;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code /gwcolor} — consulta el sistema de colores en el juego.
 *
 * <ul>
 *   <li>{@code /gwcolor}: color de lo que estás mirando (o el tuyo, si no miras a nadie).</li>
 *   <li>{@code /gwcolor list}: los 11 colores con sus 3 fuertes y sus 3 débiles.</li>
 *   <li>{@code /gwcolor biomas}: en qué clima nacen las salvajes de cada color, con ejemplos de bioma.</li>
 *   <li>{@code /gwcolor aqui}: el clima en el que estás parado y qué waifus pueden salir ahí.</li>
 * </ul>
 */
public final class ColorCommand {

    private static final double REACH = 16.0D;

    /** Cuántos nombres de bioma se llegan a listar por clima antes de resumir el resto. */
    private static final int BIOMES_SHOWN = 3;

    private ColorCommand() {
    }

        public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> register(dispatcher));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gwcolor")
                .executes(ColorCommand::inspect)
                .then(Commands.literal("list").executes(ColorCommand::list))
                .then(Commands.literal("biomas").executes(ColorCommand::biomes))
                .then(Commands.literal("aqui").executes(ColorCommand::here)));
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        LivingEntity looked = lookedAt(player);

        if (looked != null) {
            WaifuColor color = ColorCombat.colorOf(looked);
            if (color == null) {
                player.displayClientMessage(Component.translatable("gachawaifus.color.inspect_entity_none",
                        looked.getDisplayName()), false);
                return 0;
            }
            player.displayClientMessage(Component.translatable("gachawaifus.color.inspect_entity",
                    looked.getDisplayName(), color.styledName()).append(Component.literal(" " + color.hex()).withStyle(ChatFormatting.DARK_GRAY)), false);
            return 1;
        }

        WaifuColor own = ColorCombat.playerColor(player);
        if (own == null) {
            player.displayClientMessage(Component.translatable("gachawaifus.color.inspect_self_none"), false);
            return 0;
        }
        player.displayClientMessage(Component.translatable("gachawaifus.color.inspect_self", own.styledName()), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.translatable("gachawaifus.color.list_header"), false);
        for (WaifuColor color : WaifuColor.values()) {
            source.sendSuccess(() -> Component.empty()
                    .append(color.styledName())
                    .append(Component.literal("  ▲ ").withStyle(ChatFormatting.GREEN))
                    .append(WaifuColor.names(color.strong()))
                    .append(Component.literal("  ▼ ").withStyle(ChatFormatting.RED))
                    .append(WaifuColor.names(color.weak())), false);
        }
        return WaifuColor.count();
    }

    /**
     * {@code /gwcolor biomas}: en qué biomas nace cada color de waifu salvaje. Es la tabla de climas
     * del {@link WildWaifuSpawner} leída al revés — manda el color, contesta el clima — porque lo que
     * se pregunta es "dónde busco yo una de este color". Los nombres salen del registro de biomas, así
     * que no hace falta viajar a ninguno.
     */
    private static int biomes(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Registry<Biome> registry = source.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        source.sendSuccess(() -> Component.translatable("gachawaifus.color.biomes_header"), false);
        int shown = 0;
        for (WaifuColor color : WaifuColor.values()) {
            List<WildWaifuSpawner.Climate> climas = new ArrayList<>();
            Set<String> quienes = new HashSet<>();
            for (WildWaifuSpawner.Climate climate : WildWaifuSpawner.Climate.values()) {
                if (!llevaColor(climate, color)) {
                    continue;
                }
                climas.add(climate);
                for (WaifuRoster.Entry entry : WildWaifuSpawner.waifusOf(climate)) {
                    if (WaifuColors.of(entry.type().get()) == color) {
                        quienes.add(entry.name());
                    }
                }
            }
            if (climas.isEmpty()) {
                continue;
            }
            MutableComponent line = Component.empty().append(color.styledName())
                    .append(Component.literal(" §7(" + quienes.size() + " waifus)§7 → "));
            for (int i = 0; i < climas.size(); i++) {
                if (i > 0) {
                    line.append(Component.literal(" §8· "));
                }
                WildWaifuSpawner.Climate climate = climas.get(i);
                line.append(Component.literal("§d" + climate.label() + " §7"))
                        .append(biomeNames(registry, climate, BIOMES_SHOWN));
            }
            final Component out = line;
            source.sendSuccess(() -> out, false);
            shown++;
        }
        source.sendSuccess(() -> Component.translatable("gachawaifus.color.biomes_footer"), false);
        return shown;
    }

    /** ¿Este color está en la fauna del clima? */
    private static boolean llevaColor(WildWaifuSpawner.Climate climate, WaifuColor color) {
        for (WaifuColor fauna : WildWaifuSpawner.faunaOf(climate)) {
            if (fauna == color) {
                return true;
            }
        }
        return false;
    }

    /** {@code /gwcolor aqui}: el clima del bloque en el que está el jugador y lo que puede nacer ahí. */
    private static int here(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (!(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        BlockPos pos = player.blockPosition();
        Holder<Biome> biome = level.getBiome(pos);
        WildWaifuSpawner.Climate climate = WildWaifuSpawner.climateOf(biome, pos);
        List<WaifuRoster.Entry> pool = WildWaifuSpawner.waifusOf(climate);
        Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);

        context.getSource().sendSuccess(() -> Component.empty()
                .append(Component.literal("§d[GachaWaifus] §f" + climate.label()))
                .append(Component.literal("§7 (" + climate.where() + ")"))
                .append(Component.literal(" §7· bioma: ")).append(biomeName(registry, biome))
                .append(Component.literal(" §7· colores: ")).append(WaifuColor.names(WildWaifuSpawner.faunaOf(climate))),
                false);
        context.getSource().sendSuccess(() -> Component.literal("§7Pueden nacer aquí §f(" + pool.size() + "§7): ")
                .append(CollectionCommand.names(pool)), false);
        return pool.size();
    }

    /** Ejemplos de biomas del clima, con el total si hay más de los que se enseñan. */
    private static Component biomeNames(Registry<Biome> registry, WildWaifuSpawner.Climate climate,
                                        int mostrados) {
        List<Holder.Reference<Biome>> todos = new ArrayList<>();
        for (Holder.Reference<Biome> holder : registry.holders().toList()) {
            if (WildWaifuSpawner.climateOf(holder, BlockPos.ZERO) == climate) {
                todos.add(holder);
            }
        }
        MutableComponent out = Component.empty();
        for (int i = 0; i < Math.min(mostrados, todos.size()); i++) {
            if (i > 0) {
                out.append(Component.literal("§7, "));
            }
            out.append(biomeName(registry, todos.get(i)));
        }
        if (todos.size() > mostrados) {
            out.append(Component.literal(" §7+" + (todos.size() - mostrados) + " más"));
        }
        return out;
    }

    /** El nombre localizado del bioma (así en español salen "Picos helados", "Desierto"...). */
    private static Component biomeName(Registry<Biome> registry, Holder<Biome> holder) {
        ResourceLocation key = registry.getKey(holder.value());
        if (key == null) {
            return Component.literal("§fdesconocido");
        }
        return Component.literal("§f").append(
                Component.translatable("biome." + key.getNamespace() + "." + key.getPath()));
    }

    /** La entidad viva a la que apunta el jugador, si hay una dentro del alcance. */
    @Nullable
    private static LivingEntity lookedAt(ServerPlayer player) {
        HitResult hit = player.pick(REACH, 0.0F, false);
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof LivingEntity living) return living;
        }
        return null;
    }
}
