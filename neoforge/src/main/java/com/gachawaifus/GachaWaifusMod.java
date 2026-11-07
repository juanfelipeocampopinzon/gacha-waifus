package com.gachawaifus;

import com.gachawaifus.client.ModClientBindings;
import com.gachawaifus.color.ColorConfig;
import com.gachawaifus.entity.WaifuAttributeBindings;
import com.gachawaifus.registry.ModBlockEntities;
import com.gachawaifus.registry.ModBlocks;
import com.gachawaifus.registry.ModCreativeTabs;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModMenuTypes;
import com.gachawaifus.registry.ModSounds;
import com.gachawaifus.team.TeamConfig;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GachaWaifusMod.MODID)
public class GachaWaifusMod {
    public static final String MODID = "gachawaifus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public GachaWaifusMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Gacha Waifus Mod for NeoForge 1.21.1!");

        // Configs: CLIENT (etiqueta de color) y COMMON (límite del equipo + radio de paseo de las
        // waifus). Solo puede haber UN spec por tipo y mod: dos contra gachawaifus-common.toml
        // revientan el arranque con "Detected config file conflict". Las secciones COMMON se juntan
        // en TeamConfig.SPEC. Todo sale en el menú Mods → Gacha Waifus → Config.
        modContainer.registerConfig(ModConfig.Type.CLIENT, ColorConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, TeamConfig.SPEC);

        // Pantalla de configuración para el botón "Config" del menú Mods: sin este
        // registro el botón sale desactivado (IConfigScreenFactory.getForMod vacío).
        // ConfigurationScreen es de cliente: solo se registra en Dist.CLIENT.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }

        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerLayers);
        modEventBus.addListener(this::registerScreens);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        for (WaifuAttributeBindings.Binding binding : WaifuAttributeBindings.ATTRIBUTES) {
            event.put(binding.type(), binding.attributes().get().build());
        }
    }

    // Las tablas comparten los pares (tipo, proveedor) con Fabric, así que el compilador ya no
    // puede emparejar los genéricos de cada registro: la correspondencia se garantiza en el
    // momento de llenar la tabla y aquí se suelta el comodín.
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (ModClientBindings.BlockRenderer binding : ModClientBindings.BLOCK_RENDERERS) {
            event.registerBlockEntityRenderer((BlockEntityType) binding.type(),
                    (BlockEntityRendererProvider) binding.provider());
        }
        for (ModClientBindings.Renderer binding : ModClientBindings.RENDERERS) {
            event.registerEntityRenderer((EntityType) binding.type(),
                    (EntityRendererProvider) binding.provider());
        }
    }

    /**
     * Mallas con la pieza del pecho para las waifus que usan el modelo de jugador vanilla, más los
     * modelos propios de Kurumi, Remielle y Jane Doe.
     */
    private void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (ModClientBindings.Layer binding : ModClientBindings.LAYERS) {
            event.registerLayerDefinition(binding.location(), binding.definition()::get);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerScreens(RegisterMenuScreensEvent event) {
        for (ModClientBindings.Screen binding : ModClientBindings.SCREENS) {
            event.register((MenuType) binding.type(), (MenuScreens.ScreenConstructor) binding.screen());
        }
    }
}
