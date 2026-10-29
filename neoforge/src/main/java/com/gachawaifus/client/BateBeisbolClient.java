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
 *
 * <p><b>Ojo con la escala.</b> GeckoLib ya pasa las unidades del geo a bloques al hornear el
 * modelo ({@code BakedModelFactory} divide entre 16), así que un geo de 24 unidades mide
 * <b>1,5 bloques</b>, no 1. {@code withScale} es un multiplicador sobre eso, no una conversión:
 * para dejar el bate en 1 bloque hay que multiplicar por {@code 16/24 = 2/3}. Con {@code 1/24}
 * (lo que había antes) el bate medía 1/16 de bloque —un píxel en el inventario— y por eso
 * parecía invisible.
 *
 * <p>El geo está centrado en el origen (convención de los modelos de ítem de GeckoLib: el
 * {@code preRender} del renderer suma +0,5 y el {@code ItemRenderer} de vanilla resta −0,5), así
 * que el bate se pinta alrededor del origen del ítem y los transforms del JSON funcionan como los
 * de un ítem normal.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BateBeisbolClient {

    /** 24 unidades de geo = 1,5 bloques; ×2/3 = 1 bloque de largo, como un ítem vanilla. */
    private static final float MODEL_SCALE = 16F / 24F;

    private BateBeisbolClient() {
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private GeoItemRenderer<BateBeisbolItem> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GeoItemRenderer<>(new BateBeisbolModel())
                            .withScale(MODEL_SCALE, MODEL_SCALE);
                }
                return this.renderer;
            }
        }, ModItems.BATE_BEISBOL.get());
    }
}
