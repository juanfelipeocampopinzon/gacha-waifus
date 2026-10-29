package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.client.bust.WaifuBustLayers;
import com.gachawaifus.client.bust.WaifuBustPlayerModel;
import com.gachawaifus.entity.EvelynChevalierEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class EvelynChevalierRenderer extends LivingEntityRenderer<EvelynChevalierEntity, WaifuBustPlayerModel<EvelynChevalierEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/evelyn_chevalier.png");

    public EvelynChevalierRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuBustPlayerModel<>(context.bakeLayer(WaifuBustLayers.TUBE_BIG), true, 3.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EvelynChevalierEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(EvelynChevalierEntity entity) {
        return true;
    }
}
