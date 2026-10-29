package com.gachawaifus.client.audio;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.RicardoMilosEntity;
import com.gachawaifus.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Red de seguridad del tema del jefe: <b>si Ricardo Milos muere, la música se corta</b>.
 *
 * <p>El servidor ya manda {@code ClientboundStopSoundPacket} al morir (ver
 * {@code RicardoMilosEntity.stopTheme()}), pero eso depende de que el paquete llegue y de que el
 * jugador siga dentro del alcance. Esto lo cierra desde el lado del cliente y sin paquetes nuevos:
 * cada medio segundo se pregunta si queda algún jefe <b>vivo y ya herido</b> cerca. Si no queda
 * ninguno —porque ha muerto, porque lo ha matado otra persona o porque te has ido— se manda parar
 * el sonido por su id.
 *
 * <p>La señal es <b>la vida</b>: {@code getHealth() < getMaxHealth()} significa "esta pelea ya ha
 * empezado" (el jefe solo consigue objetivo después de que alguien le pegue), y la vida de un mob
 * está sincronizada con el cliente. Nada de inventar campos nuevos ni de pedirle nada al servidor.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, value = Dist.CLIENT)
public final class BossThemeWatch {

    /** Cada cuántos ticks se comprueba (10 = medio segundo; tick a tick no aporta nada). */
    private static final int CHECK_INTERVAL = 10;
    /** Mismo radio que el alcance audible del tema: {@code 16 × volumen 4}. */
    private static final double RANGE = 64.0D;

    private static int cooldown = 0;

    private BossThemeWatch() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        if (++cooldown < CHECK_INTERVAL) {
            return;
        }
        cooldown = 0;

        Player player = minecraft.player;
        boolean fighting = !minecraft.level.getEntitiesOfClass(RicardoMilosEntity.class,
                player.getBoundingBox().inflate(RANGE),
                boss -> boss.isAlive() && !boss.isRemoved() && boss.getHealth() < boss.getMaxHealth())
                .isEmpty();

        if (!fighting) {
            // Idempotente: si no suena nada, no hace nada.
            minecraft.getSoundManager().stop(ModSounds.RICARDO_MILOS_THEME_ID, SoundSource.MUSIC);
        }
    }
}
