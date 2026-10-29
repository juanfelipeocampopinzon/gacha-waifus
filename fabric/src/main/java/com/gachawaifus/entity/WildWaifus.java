package com.gachawaifus.entity;

import com.gachawaifus.gacha.GachaSavedData;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.team.TeamRules;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WildWaifus {

    private static final String KEY_WILD = "gachawaifus_wild";
    private static final String WILD_SUFFIX = " §7(salvaje)";
    private static final Map<UUID, Map<String, Integer>> ATTEMPTS = new HashMap<>();

    public static void init() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            Entity attacker = source.getEntity();
            if (attacker instanceof AbstractWaifuEntity waifu && isWild(waifu) && entity instanceof Player) {
                return false;
            }
            return true;
        });
    }

    public static void markWild(AbstractWaifuEntity waifu) {
        waifu.addTag(KEY_WILD);
        waifu.setPersistenceRequired();
        waifu.setCustomName(Component.literal(waifu.getDisplayName().getString() + WILD_SUFFIX));
    }

    public static boolean isWild(AbstractWaifuEntity waifu) {
        return waifu.getOwnerUUID() == null && waifu.getTags().contains(KEY_WILD);
    }

    public static InteractionResult onInteract(AbstractWaifuEntity waifu, Player player) {
        if (!WildWaifuConfig.active() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        WaifuRoster.Entry entry = rosterOf(waifu);
        if (entry == null) {
            return InteractionResult.PASS;
        }
        if (WaifuRoster.owns(serverPlayer, entry)) {
            serverPlayer.displayClientMessage(Component.literal(
                    "§cYa tienes a " + entry.name() + ". §7Una salvaje no se une a quien ya la tiene:"
                            + " §fno se ha gastado nada§7."), true);
            return InteractionResult.PASS;
        }

        int cost = WildWaifuConfig.claimCost();
        ItemStack held = serverPlayer.getMainHandItem();

        if (!held.is(ModItems.PINK_BALL.get())) {
            hint(serverPlayer, cost);
            return InteractionResult.PASS;
        }
        if (held.getCount() < cost) {
            serverPlayer.sendSystemMessage(Component.literal(
                    "§e[GachaWaifus] " + entry.name() + " §fnecesita §d" + cost + " Bolitas Rosas§f en la mano"
                            + " por intento §7(llevas " + held.getCount() + ")§f."));
            return InteractionResult.PASS;
        }
        if (!TeamRules.canSummon(serverPlayer)) {
            TeamRules.messageFull(serverPlayer);
            return InteractionResult.PASS;
        }

        if (!serverPlayer.getAbilities().instabuild) {
            held.shrink(cost);
        }

        int pity = WildWaifuConfig.tamePity();
        int attempt = nextAttempt(serverPlayer, entry);
        if (attempt < pity && serverPlayer.getRandom().nextInt(WildWaifuConfig.tameOdds()) != 0) {
            refused(serverPlayer, waifu, attempt, pity, cost);
            return InteractionResult.CONSUME;
        }

        resetAttempts(serverPlayer, entry);
        tame(waifu, serverPlayer, entry, attempt, pity);
        return InteractionResult.CONSUME;
    }

    private static void tame(AbstractWaifuEntity waifu, ServerPlayer player, WaifuRoster.Entry entry,
                             int attempt, int pity) {
        waifu.tame(player);
        waifu.removeTag(KEY_WILD);
        waifu.setCustomName(Component.literal(waifu.getDisplayName().getString().replace(WILD_SUFFIX, "")));
        GachaSavedData.get(waifu.level()).claimWild(player.getUUID(), entry.id());

        String who = waifu.getDisplayName().getString();
        player.sendSystemMessage(Component.literal(
                "§d[GachaWaifus] §f¡" + who + " §fse ha unido a ti! §7era una waifu salvaje, ahora es tuya"
                        + " §7(ya no deambula sola)§f."));
        if (attempt >= pity) {
            player.sendSystemMessage(Component.literal(
                    "§b[GachaWaifus] §fSe la ha llevado el §bpity§f: §7" + attempt + "/" + pity
                            + " intentos§f."));
        }
        player.sendSystemMessage(Component.literal(
                "§7Shift + clic sobre ella para cambiar su modo de combate, y Shift + clic con la"
                        + " §dCápsula Waifu§7 para guardala."));

        if (waifu.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART, waifu.getX(), waifu.getY() + 1.6D, waifu.getZ(),
                    8, 0.4D, 0.4D, 0.4D, 0.1D);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, waifu.getX(), waifu.getY() + 1.2D,
                    waifu.getZ(), 16, 0.5D, 0.5D, 0.5D, 0.2D);
        }
        waifu.level().playSound(null, waifu.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.0F, 1.5F);
        waifu.level().playSound(null, waifu.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS, 1.0F, 1.2F);
    }

    private static void refused(ServerPlayer player, AbstractWaifuEntity waifu, int attempt, int pity,
                                int cost) {
        player.displayClientMessage(Component.literal(
                "§7" + waifu.getDisplayName().getString() + " se aparta §f(" + attempt + "/" + pity
                        + " intentos) §7· bolitas -" + cost + "§7, quedan "
                        + player.getMainHandItem().getCount()), true);
        if (attempt == pity / 2 || attempt == pity - 1) {
            player.sendSystemMessage(Component.literal(
                    "§7[GachaWaifus] Llevas §f" + attempt + "/" + pity + "§7 intentos con ella: a los §b"
                            + pity + "§7 se doman fijo."));
        }
        if (waifu.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, waifu.getX(), waifu.getY() + 1.6D,
                    waifu.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    private static WaifuRoster.Entry rosterOf(AbstractWaifuEntity waifu) {
        return WaifuRoster.byId(BuiltInRegistries.ENTITY_TYPE.getKey(waifu.getType()).getPath());
    }

    private static int nextAttempt(ServerPlayer player, WaifuRoster.Entry entry) {
        Map<String, Integer> counts = ATTEMPTS.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        int attempt = counts.getOrDefault(entry.id(), 0) + 1;
        counts.put(entry.id(), attempt);
        return attempt;
    }

    private static void resetAttempts(ServerPlayer player, WaifuRoster.Entry entry) {
        Map<String, Integer> counts = ATTEMPTS.get(player.getUUID());
        if (counts != null) {
            counts.remove(entry.id());
        }
    }

    private static void hint(ServerPlayer player, int cost) {
        player.displayClientMessage(Component.literal(
                "§dWaifu salvaje §7· §f" + cost + " Bolita Rosa§7 en la mano por intento, sale §f1 de cada "
                        + WildWaifuConfig.tameOdds() + "§7 y se asegura a los §f" + WildWaifuConfig.tamePity()),
                true);
    }

    private WildWaifus() {
    }
}
