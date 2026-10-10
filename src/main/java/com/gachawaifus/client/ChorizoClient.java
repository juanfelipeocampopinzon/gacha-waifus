package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.ChorizoItem;
import com.gachawaifus.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Registro de cliente del chorizo: le cuelga el {@link GeoItemRenderer} de GeckoLib para que el
 * modelo geo se dibuje en la mano (tercera persona incluida) y en el inventario.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ChorizoClient {

    private ChorizoClient() {
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private GeoItemRenderer<ChorizoItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new ChorizoModel()).withScale(0.0625F, 0.0625F);
                }
                return this.renderer;
            }
        }, ModItems.CHORIZO.get());
    }
}
