package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.ColorConfig;
import com.gachawaifus.entity.AbstractWaifuEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;

/**
 * Interruptor de cliente para los nombres flotantes de las waifus
 * ({@code gachawaifus-client.toml} → {@code nametags.waifu_name_tags}).
 *
 * <p>Se hace aqui y no en cada renderer porque {@code shouldShowName()} esta reescrito a mano en los
 * 46 renderers del mod; con el evento se apagan todas de golpe sin tocarlos.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, value = Dist.CLIENT)
public final class WaifuNameTagHook {

    private WaifuNameTagHook() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (event.getEntity() instanceof AbstractWaifuEntity && !ColorConfig.WAIFU_NAME_TAGS.get()) {
            event.setCanRender(TriState.FALSE);
        }
    }
}
