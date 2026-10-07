package com.gachawaifus.entity.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.KoledaEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Render de <b>Koleda Belobog</b> con el <b>modelo de jugador slim</b>
 * ({@code ModelLayers.PLAYER_SLIM}), el mismo que usan las waifus femeninas del mod.
 *
 * <p><b>No usa GeckoLib</b>: Koleda no tiene {@code .geo.json} ni animaciones, así que no puede
 * apoyarse en {@code GeoEntityRenderer}. Sigue el patrón de {@code VonLycaonRenderer} /
 * {@code RinaRenderer}.
 *
 * <p><b>Todavía no lleva la pieza del pecho</b>: su skin es de <b>64×64</b> y el desplegado del
 * rombo vive en la mitad inferior (V = 121), que en esta skin no existe. Por eso se usa un
 * {@code PlayerModel} normal y no {@code WaifuBustPlayerModel}. Para añadírsela habrá que extender
 * la skin a 64×128 pintando el desplegado ({@code manual-pieza-pecho.md} §7) y entonces pasar a
 * {@code WaifuBustPlayerModel} con {@code WaifuBustLayers.TUBE} y sección 2.0F, como Promeia o
 * Ukinami Yuzuha.
 */
public class KoledaRenderer extends LivingEntityRenderer<KoledaEntity, PlayerModel<KoledaEntity>> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, "textures/entity/koleda.png");

    public KoledaRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(KoledaEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldShowName(KoledaEntity entity) {
        return true;
    }
}
