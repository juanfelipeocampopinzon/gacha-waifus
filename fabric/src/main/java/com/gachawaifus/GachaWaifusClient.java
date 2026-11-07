package com.gachawaifus;

import com.gachawaifus.client.ModClientBindings;
import com.gachawaifus.client.audio.BossThemeWatch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Arranque de cliente de Fabric. NeoForge hace lo mismo recorriendo las mismas listas de
 * {@link ModClientBindings} dentro de sus eventos; aquí se recorriendo los registries de
 * Fabric API, y MenuScreens.register (que es privado en vanilla y se abre con el access widener).
 *
 * <p>Sin esta clase el juego arranca sin errores pero no se pinta nada: las entidades salen como
 * cajas negras, las mallas del busto no existen y el terminal gacha lanza
 * "No screen factory registered".
 */
public class GachaWaifusClient implements ClientModInitializer {
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void onInitializeClient() {
        GachaWaifusMod.LOGGER.info("Initializing Gacha Waifus client for Fabric 1.21.1!");

        for (ModClientBindings.Layer binding : ModClientBindings.LAYERS) {
            EntityModelLayerRegistry.registerModelLayer(binding.location(), () -> binding.definition().get());
        }
        for (ModClientBindings.BlockRenderer binding : ModClientBindings.BLOCK_RENDERERS) {
            BlockEntityRendererRegistry.register((BlockEntityType) binding.type(),
                    (BlockEntityRendererProvider) binding.provider());
        }
        for (ModClientBindings.Renderer binding : ModClientBindings.RENDERERS) {
            EntityRendererRegistry.register((EntityType) binding.type(),
                    (EntityRendererProvider) binding.provider());
        }
        for (ModClientBindings.Screen binding : ModClientBindings.SCREENS) {
            MenuScreens.register((MenuType) binding.type(),
                    (MenuScreens.ScreenConstructor) binding.screen());
        }

        // La sintonía del jefe se corta cuando Ricardo Milos muere o sale del rango. NeoForge lo
        // hace un TickEvent subscrito; en Fabric hay que engancharlo a mano.
        BossThemeWatch.init();
    }
}
