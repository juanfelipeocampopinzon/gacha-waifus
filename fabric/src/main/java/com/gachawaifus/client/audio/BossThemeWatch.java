package com.gachawaifus.client.audio;

import com.gachawaifus.entity.RicardoMilosEntity;
import com.gachawaifus.registry.ModSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public final class BossThemeWatch {

    private static final int CHECK_INTERVAL = 10;
    private static final double RANGE = 64.0D;
    private static int cooldown = 0;

    private BossThemeWatch() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(BossThemeWatch::onClientTick);
    }

    public static void onClientTick(Minecraft minecraft) {
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
            minecraft.getSoundManager().stop(ModSounds.RICARDO_MILOS_THEME_ID, SoundSource.MUSIC);
        }
    }
}
