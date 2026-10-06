package com.gachawaifus.color;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Engancha el sistema de colores al combate real.
 *
 * <p><b>Un solo punto de entrada:</b> {@link LivingIncomingDamageEvent} cubre el golpe normal
 * ({@code doHurtTarget}), la especial ({@code mobAttack}), la ultimate ({@code magic}) y los
 * proyectiles de cualquier waifu, así que no hay que tocar las 11 clases de entidad.
 *
 * <p><b>Qué modifica:</b> el daño de las waifus hacia cualquier cosa con color, y el daño que
 * <i>reciben</i> las waifus y los jugadores (el color del jugador es el de su waifu activa).
 * Mobs contra mobs queda neutro a propósito: no se ve y solo añadiría ruido.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID)
public final class ColorEvents {

    /** Cada cuántos ticks se repinta el aura de los mobs. */
    private static final int AURA_INTERVAL = 10;
    /** Distancia a la que un jugador "ve" el color de un mob. */
    private static final double AURA_RANGE = 24.0D;
    /** Tope de mobs con aura por jugador y pulso, para no castigar el rendimiento. */
    private static final int AURA_MAX_MOBS = 40;
    /** Anti-spam de los avisos, en ticks. */
    private static final int MESSAGE_COOLDOWN = 20;

    private static final Map<UUID, Long> LAST_MESSAGE = new HashMap<>();

    private ColorEvents() {
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!ColorSettings.ENABLED) return;

        LivingEntity victim = event.getEntity();
        Entity source = event.getSource().getEntity();

        boolean fromWaifu = source instanceof AbstractWaifuEntity;
        boolean toProtected = victim instanceof AbstractWaifuEntity || victim instanceof Player;
        if (!fromWaifu && !toProtected) return;
        if (source instanceof Player && !ColorSettings.PLAYER_OUTGOING_USES_COLOR) return;

        WaifuColor attacker = ColorCombat.attackerColor(source);
        WaifuColor defender = ColorCombat.colorOf(victim);
        ColorCombat.Matchup matchup = ColorCombat.matchup(attacker, defender);
        if (matchup == ColorCombat.Matchup.NEUTRAL) return;

        event.setAmount(event.getAmount() * ColorCombat.multiplier(attacker, defender));

        if (victim.level() instanceof ServerLevel level) {
            boolean strong = matchup == ColorCombat.Matchup.STRONG;
            ParticleOptions particles = strong ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.LARGE_SMOKE;
            level.sendParticles(particles, victim.getX(), victim.getY() + victim.getBbHeight() * 0.6D,
                    victim.getZ(), strong ? 8 : 4, 0.35D, 0.4D, 0.35D, 0.02D);
        }

        if (ColorSettings.MESSAGES) {
            announce(source, victim, attacker, defender, matchup);
        }
    }

    private static void announce(Entity source, LivingEntity victim, WaifuColor attacker,
                                 WaifuColor defender, ColorCombat.Matchup matchup) {
        if (attacker == null || defender == null) return;
        boolean strong = matchup == ColorCombat.Matchup.STRONG;

        // A quien le pegan: el jugador ve por qué su waifu pega más o menos.
        if (source instanceof AbstractWaifuEntity waifu && waifu.getOwner() instanceof ServerPlayer owner) {
            send(owner, Component.translatable(
                    strong ? "gachawaifus.color.hit_strong" : "gachawaifus.color.hit_weak",
                    attacker.styledName(), defender.styledName()));
            return;
        }
        // A quien recibe: se entera de que el color del mob le está castigando.
        if (victim instanceof ServerPlayer player) {
            send(player, Component.translatable(
                    strong ? "gachawaifus.color.took_strong" : "gachawaifus.color.took_weak",
                    attacker.styledName(), defender.styledName()));
        }
    }

    private static void send(ServerPlayer player, Component message) {
        long now = player.level().getGameTime();
        Long last = LAST_MESSAGE.get(player.getUUID());
        if (last != null && now - last < MESSAGE_COOLDOWN) return;
        LAST_MESSAGE.put(player.getUUID(), now);
        player.displayClientMessage(message, true);
    }

    /**
     * Aura de color: cada 10 ticks marca los mobs hostiles cercanos a cada jugador con partículas
     * de su color. Es la forma de "ver" el color sin tocar nombres: ponerle nombre a un mob lo
     * vuelve permanente ({@code Mob.checkDespawn} no borra mobs con nombre) y rompería las granjas.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!ColorSettings.ENABLED || !ColorSettings.ENEMY_COLORS || !ColorSettings.ENEMY_AURA) return;

        MinecraftServer server = event.getServer();
        if (server.getTickCount() % AURA_INTERVAL != 0) return;

        for (ServerLevel level : server.getAllLevels()) {
            if (level.players().isEmpty()) continue;
            for (ServerPlayer player : level.players()) {
                AABB box = player.getBoundingBox().inflate(AURA_RANGE);
                List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box,
                        mob -> mob.isAlive() && mob instanceof Enemy);
                int shown = 0;
                for (Mob mob : mobs) {
                    if (shown >= AURA_MAX_MOBS) break;
                    WaifuColor color = ColorCombat.colorOf(mob);
                    if (color == null) continue;
                    shown++;
                    float[] rgb = ColorCombat.visibleRgb(color);
                    level.sendParticles(
                            new DustParticleOptions(new Vector3f(rgb[0], rgb[1], rgb[2]), 0.9F),
                            mob.getX(), mob.getY() + mob.getBbHeight() * 0.55D, mob.getZ(),
                            2, 0.25D, 0.3D, 0.25D, 0.0D);
                }
            }
        }
    }
}
