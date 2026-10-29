package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.JaneDoeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class JaneDoeRenderer extends LivingEntityRenderer<JaneDoeEntity, JaneDoeModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/jane_doe.png");

    public JaneDoeRenderer(EntityRendererProvider.Context context) {
        // Modelo propio: busto 3.0F + la cola editada en Blockbench, con su balanceo
        super(context, new JaneDoeModel(context.bakeLayer(JaneDoeModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(JaneDoeEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(JaneDoeEntity entity) {
        return true;
    }
}
