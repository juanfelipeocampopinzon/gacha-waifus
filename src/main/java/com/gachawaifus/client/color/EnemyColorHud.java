package com.gachawaifus.client.color;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.ColorCombat;
import com.gachawaifus.color.ColorSettings;
import com.gachawaifus.color.WaifuColor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Indicador de color del enemigo al que apuntas: justo debajo de la mirilla aparece un cuadradito
 * del color exacto del mob, su nombre de color y si tu waifu activa le pega <b>fuerte</b> (▲),
 * <b>flojo</b> (▼) o <b>neutro</b> (—).
 *
 * <p>Es la versión "en el momento de pelear" de la etiqueta flotante ({@link ColorTagLayer}, que
 * pinta el color encima de cada enemigo): ahí ves el color de todos los mobs cercanos, aquí ves
 * <i>lo que te importa del que tienes delante</i>, con la matriz de daño ya resuelta.
 *
 * <p>Todo se calcula en el cliente con los mismos datos que usa el servidor: el color de un mob
 * hostil sale de su UUID ({@link ColorCombat#colorOf}) y el del jugador, del de su waifu activa más
 * cercana. No manda ni pide nada por red.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class EnemyColorHud {

    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "enemy_color");

    /** Lado del cuadradito de color, en píxeles de pantalla. */
    private static final int SWATCH = 9;
    /** Separación por encima del centro de la pantalla (la mirilla). */
    private static final int Y_OFFSET = 14;
    private static final int PADDING = 3;
    private static final int BACKGROUND = 0x90000000;

    private EnemyColorHud() {
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, LAYER_ID, EnemyColorHud::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!ColorSettings.ENABLED || !ColorSettings.AIM_INDICATOR) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer viewer = minecraft.player;
        if (viewer == null || minecraft.options.hideGui || minecraft.screen != null) {
            return;
        }
        // Lo que está debajo de la mirilla, tal cual lo calcula el propio juego.
        if (!(minecraft.hitResult instanceof EntityHitResult hit)) {
            return;
        }
        if (!(hit.getEntity() instanceof LivingEntity looked) || !(looked instanceof Enemy)) {
            return;
        }
        WaifuColor color = ColorCombat.colorOf(looked);
        if (color == null) {
            return;
        }

        WaifuColor own = ColorCombat.playerColor(viewer);
        Component label;
        if (own == null) {
            label = Component.translatable("gachawaifus.color.aim_no_waifu", color.styledName());
        } else {
            label = switch (ColorCombat.matchup(own, color)) {
                case STRONG -> Component.translatable("gachawaifus.color.aim_strong", color.styledName());
                case WEAK -> Component.translatable("gachawaifus.color.aim_weak", color.styledName());
                case NEUTRAL -> Component.translatable("gachawaifus.color.aim_neutral", color.styledName());
            };
        }

        Font font = minecraft.font;
        int boxWidth = SWATCH + 6 + font.width(label) + 2 * PADDING;
        int boxHeight = Math.max(SWATCH, font.lineHeight) + 2 * PADDING;
        int x = (graphics.guiWidth() - boxWidth) / 2;
        int y = graphics.guiHeight() / 2 + Y_OFFSET;

        graphics.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND);
        graphics.fill(x + PADDING, y + PADDING, x + PADDING + SWATCH, y + PADDING + SWATCH, argb(color));
        graphics.drawString(font, label, x + PADDING + SWATCH + 4,
                y + (boxHeight - font.lineHeight) / 2 + 1, 0xFFFFFFFF, true);
    }

    /** El color exacto del mob, ya aclarado si es tan oscuro que no se vería. */
    private static int argb(WaifuColor color) {
        float[] rgb = ColorCombat.visibleRgb(color);
        return 0xFF000000
                | (Math.round(rgb[0] * 255.0F) << 16)
                | (Math.round(rgb[1] * 255.0F) << 8)
                | Math.round(rgb[2] * 255.0F);
    }
}
