package com.gachawaifus.vfx;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

/**
 * {@code /gwvfx play burnice_white/special#channel} — delante del jugador.
 * {@code /gwvfx waifu burnice_white/special#channel} — sobre la waifu más cercana (a 24 bloques),
 * y el efecto la sigue mientras dure (anclado a sus huesos si los declara).
 * {@code /gwvfx probe left_torch} — imprime dónde cae ese hueso y hacia dónde apunta.
 * {@code /gwvfx waifu hatsune_miku/ultimate} — sin {@code #}: todos los stages del efecto que tengan
 * {@code trigger}, cada uno en su {@code at} (la habilidad entera sin esperar al combate).
 * {@code /gwvfx flash on|off} — destellos de pantalla (también se apagan con la opción de
 * accesibilidad de vanilla "Ocultar destellos").
 * {@code /gwvfx reload} — olvida el JSON cacheado.
 *
 * <p>Brigadier genérico: el mismo constructor sirve para el dispatcher de cliente de NeoForge
 * ({@code CommandSourceStack}) y para el de Fabric ({@code FabricClientCommandSource}). El comando
 * tira siempre del jugador local, así que no necesita la fuente.
 */
public final class VfxCommand {

    private VfxCommand() {
    }

    public static <S> LiteralArgumentBuilder<S> build() {
        return LiteralArgumentBuilder.<S>literal("gwvfx")
                .then(LiteralArgumentBuilder.<S>literal("play")
                        .then(RequiredArgumentBuilder.<S, String>argument("id", StringArgumentType.greedyString())
                                .executes(VfxCommand::play)))
                .then(LiteralArgumentBuilder.<S>literal("waifu")
                        .then(RequiredArgumentBuilder.<S, String>argument("id", StringArgumentType.greedyString())
                                .executes(VfxCommand::onWaifu)))
                .then(LiteralArgumentBuilder.<S>literal("probe")
                        .then(RequiredArgumentBuilder.<S, String>argument("bone", StringArgumentType.word())
                                .executes(VfxCommand::probe)))
                .then(LiteralArgumentBuilder.<S>literal("flash")
                        .then(LiteralArgumentBuilder.<S>literal("on").executes(context -> flash(true)))
                        .then(LiteralArgumentBuilder.<S>literal("off").executes(context -> flash(false))))
                .then(LiteralArgumentBuilder.<S>literal("reload").executes(VfxCommand::reload));
    }

    private static int flash(boolean enabled) {
        VfxScreen.flashEnabled = enabled;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal("VFX flash " + (enabled ? "on" : "off")), false);
        }
        return 1;
    }

    private static <S> int play(CommandContext<S> context) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        String[] spec = split(player, StringArgumentType.getString(context, "id"));
        if (spec == null) {
            return 0;
        }
        Vec3 look = player.getLookAngle();
        // Pies del jugador + 2 bloques hacia delante: delante se ve, y no tapa la cámara.
        VfxManager.play(spec[0], spec[1], player.position().add(look.scale(2.0D)), look);
        player.displayClientMessage(Component.literal("VFX " + spec[0] + "#" + spec[1]), false);
        return 1;
    }

    private static <S> int onWaifu(CommandContext<S> context) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        String raw = StringArgumentType.getString(context, "id");
        if (raw.indexOf('#') < 0) {
            AbstractWaifuEntity whole = nearest(player);
            if (whole == null) {
                player.displayClientMessage(Component.literal("no hay ninguna waifu a 24 bloques"), false);
                return 0;
            }
            int stages = VfxTriggers.playAll(raw, whole);
            player.displayClientMessage(Component.literal(
                    "VFX " + raw + " (" + stages + " stages) sobre " + whole.getName().getString()), false);
            return stages > 0 ? 1 : 0;
        }
        String[] spec = split(player, raw);
        if (spec == null) {
            return 0;
        }
        AbstractWaifuEntity target = nearest(player);
        if (target == null) {
            player.displayClientMessage(Component.literal("no hay ninguna waifu a 24 bloques"), false);
            return 0;
        }
        VfxManager.playOn(spec[0], spec[1], target);
        player.displayClientMessage(Component.literal(
                "VFX " + spec[0] + "#" + spec[1] + " sobre " + target.getName().getString()), false);
        return 1;
    }

    /** Diagnóstico del ancla: dónde cae el hueso y hacia dónde apunta, sin adivinar por la pantalla. */
    private static <S> int probe(CommandContext<S> context) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        AbstractWaifuEntity target = nearest(player);
        if (target == null) {
            player.displayClientMessage(Component.literal("no hay ninguna waifu a 24 bloques"), false);
            return 0;
        }
        String bone = StringArgumentType.getString(context, "bone");
        VfxBones.Pose pose = VfxBones.of(target, bone, new org.joml.Vector3f(0.0F, 0.0F, 0.0F), 0.0F);
        if (pose == null) {
            player.displayClientMessage(Component.literal(
                    "sin hueso '" + bone + "' en el modelo de " + target.getName().getString()), false);
            return 0;
        }
        Vec3 local = pose.origin().subtract(target.position());
        Vec3 axis = pose.axis();
        player.displayClientMessage(Component.literal(String.format(
                "bone %s: local[%.2f %.2f %.2f] eje[%.2f %.2f %.2f]",
                bone, local.x, local.y, local.z, axis.x, axis.y, axis.z)), false);
        return 1;
    }

    /** La waifu viva más cercana al jugador, a 24 bloques. */
    private static AbstractWaifuEntity nearest(Player player) {
        return player.level()
                .getEntitiesOfClass(AbstractWaifuEntity.class, player.getBoundingBox().inflate(24.0D),
                        candidate -> candidate.isAlive())
                .stream()
                .min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(player)))
                .orElse(null);
    }

    private static <S> int reload(CommandContext<S> context) {
        VfxLoader.clearCache();
        VfxTriggers.clearCache();
        VfxCasters.clearCache();
        VfxManager.clear();
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.displayClientMessage(Component.literal("VFX recargados"), false);
        }
        return 1;
    }

    /** "waifu/efecto#stage" -> {"waifu/efecto", "stage"}; null (y aviso) si falta el #. */
    private static String[] split(Player player, String spec) {
        int hash = spec.indexOf('#');
        if (hash <= 0 || hash == spec.length() - 1) {
            player.displayClientMessage(Component.literal("uso: /gwvfx play|waifu <waifu>/<efecto>#<stage>"), false);
            return null;
        }
        return new String[]{spec.substring(0, hash), spec.substring(hash + 1)};
    }
}
