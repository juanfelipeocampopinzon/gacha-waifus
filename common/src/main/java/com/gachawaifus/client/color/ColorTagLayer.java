package com.gachawaifus.client.color;

import com.gachawaifus.color.ColorCombat;
import com.gachawaifus.color.ColorConfig;
import com.gachawaifus.color.ColorSettings;
import com.gachawaifus.color.WaifuColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Etiqueta lejana con el color del enemigo, encima de su cabeza: solo el cuadradito del color
 * (sin texto), legible desde cualquier distancia y con la misma curva de escala que el nametag
 * lejano de {@code BossHumanoidRenderer}.
 *
 * <p>No se usa {@code setCustomName} a propósito:
 * {@code Mob.checkDespawn} no borra los mobs con nombre (se volverían permanentes y romperían
 * las granjas), así que el color sigue siendo puro render.
 *
 * <p>Se dibuja solo para mobs hostiles ({@link Enemy}) y dentro del rango configurado en
 * {@link ColorConfig#LABEL_RANGE} (menú Mods → Config). La pasada SEE_THROUGH + NORMAL es la
 * del nametag vanilla, para que el color no se recorte contra el propio cuerpo del mob.
 */
public class ColorTagLayer<T extends LivingEntity, M extends net.minecraft.client.model.EntityModel<T>>
        extends RenderLayer<T, M> {

    /** Cuánto por encima de la cabeza se dibuja el color cuando el mob no tiene nombre. */
    private static final double HEIGHT_OFFSET = 0.55D;
    /** Con nombre propio, un poco más arriba para no pisar el nametag (que va a +0.5). */
    private static final double HEIGHT_OFFSET_NAMED = 0.9D;

    public ColorTagLayer(LivingEntityRenderer<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!ColorSettings.ENABLED || !ColorConfig.FLOATING_LABEL.get()) return;
        if (!(entity instanceof Enemy)) return;
        if (entity.isInvisible() || entity.isDeadOrDying()) return;

        Minecraft minecraft = Minecraft.getInstance();
        Player viewer = minecraft.player;
        if (viewer == null || entity == viewer) return;

        WaifuColor color = ColorCombat.colorOf(entity);
        if (color == null) return;

        int argb = argb(color);
        // Solo el cuadradito del color, sin texto: el color entra por los ojos sin leer nada.
        MutableComponent label = Component.literal("\u2588\u2588")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(argb & 0xFFFFFF)));

        double distanceSq = minecraft.getEntityRenderDispatcher().distanceToSqr(entity);
        int range = ColorConfig.LABEL_RANGE.get();
        if (distanceSq > (double) range * range) return;
        // Misma curva que el nametag lejano de los jefes: tamaño de nametag hasta 64 bloques y
        // crece con la distancia (hasta 6x) por si se sube el rango en la config.
        float scale = 0.025F * (float) Mth.clamp(Math.sqrt(distanceSq) / 64.0D, 1.0D, 6.0D);
        double offset = entity.hasCustomName() ? HEIGHT_OFFSET_NAMED : HEIGHT_OFFSET;

        Font font = minecraft.font;
        double height = entity.getBbHeight() + offset;

        // Oclusión por bloques: la etiqueta se dibuja con SEE_THROUGH (traspasa el cuerpo del propio
        // mob), así que sin este rayo funcionaba como wallhack: se veía el color de los enemigos
        // escondidos detrás de paredes. Se comprueba la línea entre el ojo del jugador y el punto
        // exacto donde se pinta el cuadradito.
        Vec3 eye = viewer.getEyePosition(partialTick);
        Vec3 anchor = entity.position().add(0.0D, height, 0.0D);
        HitResult hit = entity.level().clip(new ClipContext(eye, anchor, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, viewer));
        if (hit.getType() != HitResult.Type.MISS) return;

        pose.pushPose();
        // Esta capa se ejecuta DENTRO del pose del modelo (LivingEntityRenderer: rotación del
        // cuerpo, Y invertida con scale(-1,-1,1), escala del atributo getScale() y el override
        // scale() de los jefes, 2.0F). Traducir "hacia arriba" ahí salía HACIA ABAJO y el
        // tamaño se multiplicaba: por eso el color quedaba bajo los pies y los jefes no
        // coincidían con los mobs naturales. Se deshace la rotación (quaternion del pose) y se
        // divide por la escala para dibujar la etiqueta como el nametag vanilla: a `height`
        // sobre los pies y con escala solo de distancia, idéntica en todos los seres vivos.
        Matrix4f modelMatrix = pose.last().pose();
        float modelScale = modelMatrix.getScale(new Vector3f()).x;
        if (modelScale > 1.0E-4F) {
            Quaternionf modelRotation = new Quaternionf();
            modelMatrix.getUnnormalizedRotation(modelRotation);
            pose.mulPose(modelRotation.invert());
            // El origen del espacio del modelo está a 1.501 (el translate(-1.501) vanilla) por
            // encima de los pies; con la rotación deshecha el eje Y ya es el del mundo.
            pose.translate(0.0D, height / modelScale - 1.501D, 0.0D);
            pose.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            float textScale = scale / modelScale;
            pose.scale(textScale, -textScale, textScale);

            Matrix4f matrix = pose.last().pose();
            float x = -font.width(label) / 2.0F;
            font.drawInBatch(label, x, 0.0F, argb, true, matrix, buffer,
                    Font.DisplayMode.SEE_THROUGH, 0, packedLight);
            font.drawInBatch(label, x, 0.0F, -1, true, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, packedLight);
        }
        pose.popPose();
    }

    /** El color del mob como ARGB opaco, ya aclarado si es tan oscuro que no se vería. */
    private static int argb(WaifuColor color) {
        float[] rgb = ColorCombat.visibleRgb(color);
        return 0xFF000000
                | (Math.round(rgb[0] * 255.0F) << 16)
                | (Math.round(rgb[1] * 255.0F) << 8)
                | Math.round(rgb[2] * 255.0F);
    }
}
