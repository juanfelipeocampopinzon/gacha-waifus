package com.gachawaifus.command;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.ColorCombat;
import com.gachawaifus.color.WaifuColor;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.Nullable;

/**
 * {@code /gwcolor} — consulta el sistema de colores en el juego.
 *
 * <ul>
 *   <li>{@code /gwcolor}: color de lo que estás mirando (o el tuyo, si no miras a nadie).</li>
 *   <li>{@code /gwcolor list}: los 11 colores con sus 3 fuertes y sus 3 débiles.</li>
 * </ul>
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class ColorCommand {

    private static final double REACH = 16.0D;

    private ColorCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("gwcolor")
                .executes(ColorCommand::inspect)
                .then(Commands.literal("list").executes(ColorCommand::list)));
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
