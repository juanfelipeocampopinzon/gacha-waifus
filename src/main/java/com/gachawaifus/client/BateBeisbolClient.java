package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.item.BateBeisbolItem;
import com.gachawaifus.registry.ModItems;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Registro de cliente del bate: {@link GeoItemRenderer} en mano (tercera persona incluida)
 * e inventario.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BateBeisbolClient {

    private BateBeisbolClient() {
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private GeoItemRenderer<BateBeisbolItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new BateBeisbolModel()).withScale(0.125F, 0.125F);
                }
                return this.renderer;
            }
        }, ModItems.BATE_BEISBOL.get());
    }
}
