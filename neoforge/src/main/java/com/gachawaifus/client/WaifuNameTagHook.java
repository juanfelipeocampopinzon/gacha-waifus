package com.gachawaifus.client;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.color.ColorConfig;
import com.gachawaifus.entity.AbstractWaifuEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.joml.Matrix4f;

/**
 * Interruptor de cliente para los nombres flotantes de las waifus
 * ({@code gachawaifus-client.toml} → {@code nametags.waifu_name_tags}).
 *
 * <p>Se hace aqui y no en cada renderer porque {@code shouldShowName()} esta reescrito a mano en los
 * 46 renderers del mod; con el evento se apagan todas de golpe sin tocarlos.
 *
 * <p>Además, cuando el nametag SÍ se muestra, se cancela el renderizado vanilla y se redibuja
 * con {@link LightTexture#FULL_BRIGHT} en vez del {@code packedLight} ambiental. Esto soluciona
 * que los nombres se vean oscuros con shaders que aplican tone-mapping a la luz.
 */
@EventBusSubscriber(modid = GachaWaifusMod.MODID, value = Dist.CLIENT)
public final class WaifuNameTagHook {

    private WaifuNameTagHook() {
    }

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof AbstractWaifuEntity waifuEntity)) {
            return;
        }

        // Si los nametags de waifus están desactivados en config, no mostrar nada.
        if (!ColorConfig.WAIFU_NAME_TAGS.get()) {
            event.setCanRender(TriState.FALSE);
            return;
        }

        // Cancelar el render vanilla y redibujar con luz máxima para evitar el oscurecimiento
        // por shaders que aplican tone-mapping al packedLight ambiental.
        event.setCanRender(TriState.FALSE);
        renderBrightNameTag(
                waifuEntity,
                event.getContent(),
                event.getEntityRenderer(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPartialTick()
        );
    }

    /**
     * Replica {@code EntityRenderer#renderNameTag} pero usando {@link LightTexture#FULL_BRIGHT}
     * en lugar del packedLight ambiental, para que los shaders no oscurezcan el texto.
     *
     * <p>Además comprueba línea de visión: si hay un bloque sólido entre la cámara y la entidad
     * no se muestra el nametag. Esto evita el fondo negro cuando hay un bloque encima, y también
     * que la etiqueta se vea a través del suelo/bloques.
     */
    private static void renderBrightNameTag(AbstractWaifuEntity entity, Component content,
                                            EntityRenderer<?> renderer, PoseStack poseStack,
                                            MultiBufferSource buffer, float partialTick) {
        // No mostrar si hay bloques sólidos de por medio (evita fondo negro + visibilidad atravesando bloques)
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !mc.player.hasLineOfSight(entity)) {
            return;
        }

        Vec3 anchor = entity.getAttachments().getNullable(
                EntityAttachment.NAME_TAG, 0, entity.getViewYRot(partialTick));
        if (anchor == null) {
            return;
        }

        boolean seeThrough = !entity.isDiscrete();
        // El huevo de pascua de deadmau5: el nombre flota un poco más arriba
        int yOffset = "deadmau5".equals(content.getString()) ? -10 : 0;

        poseStack.pushPose();
        poseStack.translate(anchor.x, anchor.y + 0.5, anchor.z);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix = poseStack.last().pose();

        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int background = (int) (opacity * 255.0F) << 24;
        Font font = renderer.getFont();
        float x = (float) (-font.width(content) / 2);

        // Primer pass: fondo semitransparente. SEE_THROUGH si la entidad no es discreta.
        font.drawInBatch(content, x, (float) yOffset, 553648127, false, matrix, buffer,
                seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                background, LightTexture.FULL_BRIGHT);

        // Segundo pass (solo para entidades no-discretas): texto blanco opaco encima, NORMAL.
        if (seeThrough) {
            font.drawInBatch(content, x, (float) yOffset, -1, false, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        }

        poseStack.popPose();
    }
}
