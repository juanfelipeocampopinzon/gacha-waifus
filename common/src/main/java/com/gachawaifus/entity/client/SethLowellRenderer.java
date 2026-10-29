package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.SethLowellEntity;
import com.gachawaifus.client.bust.WaifuPlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class SethLowellRenderer extends LivingEntityRenderer<SethLowellEntity, WaifuPlayerModel<SethLowellEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/seth_lowell.png");

    public SethLowellRenderer(EntityRendererProvider.Context context) {
        super(context, new WaifuPlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(SethLowellEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(SethLowellEntity entity) {
        return true;
    }
}
