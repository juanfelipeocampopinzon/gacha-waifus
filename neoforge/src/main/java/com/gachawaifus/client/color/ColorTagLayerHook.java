package com.gachawaifus.client.color;

import com.gachawaifus.GachaWaifusMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Cuelga {@link ColorTagLayer} de <b>todos</b> los renderers de entidades vivas, así el color se
 * ve en cualquier mob del juego (y en los de otros mods) sin tocar vanilla ni registrar nada raro.
 *
 * <p>Los genéricos de {@code EntityRenderersEvent.AddLayers} no se pueden resolver con un
 * {@code EntityType<?>}, de ahí el raw cast con aviso suprimido: es el patrón habitual para este
 * evento.
 *
 * <p>El {@code bus = MOD} es a propósito: {@code AddLayers} es un evento del <b>mod bus</b>
 * (implementa {@code IModBusEvent}). La anotación lo marca como deprecado porque NeoForge ya
 * registra solo, pero dejarlo explícito es lo que garantiza que el evento llegue.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ColorTagLayerHook {

    private ColorTagLayerHook() {
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer((EntityType) type);
            if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new ColorTagLayer(living));
            }
        }
    }
}
