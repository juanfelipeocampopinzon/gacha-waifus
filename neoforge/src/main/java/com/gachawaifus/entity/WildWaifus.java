package com.gachawaifus.entity;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.gacha.GachaSavedData;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.team.TeamRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Waifus salvajes: las que nacen deambulando solas por el mundo (sin dueño) y se pueden domar.
 *
 * <p>Una waifu es salvaje si {@link WildWaifuSpawner} la marcó y sigue sin dueño. La marca vive en
 * los datos persistentes de la entidad (se guardan con ella), así que una waifu salvaje sobrevive a
 * reiniciar el mundo y deja de serlo en cuanto alguien la reclama, aunque el dueño muera después.
 *
 * <p><b>Domar es un camino con suerte, no una compra.</b> Cada clic con Bolitas Rosas en la mano
 * paga {@link WildWaifuConfig#claimCost()} (1 bolita) y tira un dado: sale <b>1 de cada
 * {@link WildWaifuConfig#tameOdds()}</b> intentos (0,9 %), casi el doble de caro que la tirada del
 * Terminal Gacha (1,6 % por tirada y pity 64). Como la salvaje te espera, el contador sube
 * <b>de 1 en 1</b> y a los
 * {@link WildWaifuConfig#tamePity()} (128) la doma es segura. Lo que sigue dando la vía de la doma
 * frente al Terminal es que <b>tú eliges a quién te llevas</b>.
 *
 * <p>Dos reglas cierran los agujeros contra el gacha:
 * <ul>
 *   <li>No se puede domar una waifu que ya posees ({@link WaifuRoster#owns}): el intento ni se paga.
 *   <li>La doma se sella en {@link GachaSavedData.PlayerState#wildClaims}, que es permanente y vive
 *       en el Overworld. Sin eso, una domada no deja token ni hueco en la Cápsula y el Terminal la
 *       volvía a dar en cuanto ella se alejaba más de 128 bloques.
 * </ul>
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class WildWaifus {

    /** Clave de los datos persistentes que marca a la waifu como salvaje. */
    private static final String KEY_WILD = "gachawaifus_wild";

    /** Etiquetas del jugador: cada id de waifu guarda ahí sus intentos fallidos. */
    private static final String KEY_ATTEMPTS = "gachawaifus_wild_attempts";

    /**
     * Sufijo que se le pone al nombre de una salvaje. Es lo único que el jugador puede ver desde
     * fuera: los datos persistentes no se sincronizan con el cliente, así que sin etiqueta no habría
     * forma de saber si la waifu que pasea por ahí es tuya o de nadie.
     */
    private static final String WILD_SUFFIX = " §7(salvaje)";

    private WildWaifus() {
    }

    /**
     * Una waifu salvaje no hiere a los jugadores. Sus ataques directos ya los bloquea
     * {@code wantsToAttack}, pero sus ultimas (el área de daño que calculan a mano con
     * {@code e != owner}) no tienen dueño al que respetar: con {@code owner == null} ese filtro deja
     * pasar a cualquier jugador que esté dentro del radio. Se corta aquí, en el único punto central,
     * en vez de tocar los 59 ficheros de entidad.
     */
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof AbstractWaifuEntity waifu && isWild(waifu)
                && event.getEntity() instanceof Player) {
            event.setCanceled(true);
        }
    }

    /** Marca a una waifu recién nacida como salvaje e impide que despawnee por distancia. */
    public static void markWild(AbstractWaifuEntity waifu) {
        waifu.getPersistentData().putBoolean(KEY_WILD, true);
        waifu.setPersistenceRequired();
        waifu.setCustomName(Component.literal(waifu.getDisplayName().getString() + WILD_SUFFIX));
    }

    /** ¿Sigue salvaje? Pierde la condición en cuanto un jugador la reclama (o si se corrompe el flag). */
    public static boolean isWild(AbstractWaifuEntity waifu) {
        return waifu.getOwnerUUID() == null && waifu.getPersistentData().getBoolean(KEY_WILD);
    }

    /**
     * Respuesta al clic derecho (mano principal vacía o con bolitas) sobre una waifu salvaje: un
     * intento de doma. Solo corre en el servidor, que es quien cobra, tira el dado y decide.
     */
    public static InteractionResult onInteract(AbstractWaifuEntity waifu, Player player) {
        if (!WildWaifuConfig.active() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        WaifuRoster.Entry entry = rosterOf(waifu);
        if (entry == null) {
            return InteractionResult.PASS; // no está en el roster: no hay forma de registrarla
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

    /** El sí: pasa a ser tuya, deja de ser salvaje y queda sellada para el gacha. */
    private static void tame(AbstractWaifuEntity waifu, ServerPlayer player, WaifuRoster.Entry entry,
                             int attempt, int pity) {
        waifu.tame(player);
        CompoundTag data = waifu.getPersistentData();
        data.remove(KEY_WILD);
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

    /** El no: se aparta y queda anotado el intento. Va a la barra de acciones, no al chat. */
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
        // Sin sonido: el balido de aldeano sonaba en cada intento fallido y resultaba molesto; la
        // negativa ya se ve en la barra de acciones y con las partículas.
        if (waifu.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, waifu.getX(), waifu.getY() + 1.6D,
                    waifu.getZ(), 4, 0.3D, 0.3D, 0.3D, 0.0D);
        }
    }

    /** Entrada de la rotación que corresponde a esta entidad, o {@code null} si no está en el roster. */
    private static WaifuRoster.Entry rosterOf(AbstractWaifuEntity waifu) {
        return WaifuRoster.byId(BuiltInRegistries.ENTITY_TYPE.getKey(waifu.getType()).getPath());
    }

    /** Cuenta el intento que se tira ahora (empieza en 1) y lo deja anotado en el jugador. */
    private static int nextAttempt(ServerPlayer player, WaifuRoster.Entry entry) {
        CompoundTag counts = player.getPersistentData().getCompound(KEY_ATTEMPTS);
        int attempt = counts.getInt(entry.id()) + 1;
        counts.putInt(entry.id(), attempt);
        player.getPersistentData().put(KEY_ATTEMPTS, counts);
        return attempt;
    }

    private static void resetAttempts(ServerPlayer player, WaifuRoster.Entry entry) {
        CompoundTag counts = player.getPersistentData().getCompound(KEY_ATTEMPTS);
        if (counts.contains(entry.id())) {
            counts.remove(entry.id());
            player.getPersistentData().put(KEY_ATTEMPTS, counts);
        }
    }

    /** Pista en la barra de acciones: enseña el precio y las probabilidades sin llenar el chat. */
    private static void hint(ServerPlayer player, int cost) {
        player.displayClientMessage(Component.literal(
                "§dWaifu salvaje §7· §f" + cost + " Bolita Rosa§7 en la mano por intento, sale §f1 de cada "
                        + WildWaifuConfig.tameOdds() + "§7 y se asegura a los §f" + WildWaifuConfig.tamePity()),
                true);
    }
}
