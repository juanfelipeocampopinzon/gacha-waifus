package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.DoctorHouseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Doctor House</b>: modelo de jugador ancho escalado x2 — mismo tamaño que
 * Ricardo Milos (hitbox 1.6x4.2 en {@code ModEntities} + {@code scale()} 2.0F aquí) — con la
 * skin real de {@code bosses/doctor house/} convertida a HD y la cara del actor en la cabeza.
 *
 * <p>Al extender {@link BossHumanoidRenderer}, su nombre (§7) también se ve de lejos.
 */
public class DoctorHouseRenderer extends BossHumanoidRenderer<DoctorHouseEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/doctor_house.png");

    public DoctorHouseRenderer(EntityRendererProvider.Context context) {
        super(context, 1.6F);
    }

    /** Escala visual x2 para que el cuerpo llene la hitbox gigante (mismo tamaño que Ricardo). */
    @Override
    protected void scale(DoctorHouseEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(DoctorHouseEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(DoctorHouseEntity entity) {
        return true;
    }
}
