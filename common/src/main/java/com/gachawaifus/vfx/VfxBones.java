package com.gachawaifus.vfx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Posa de un hueso de GeckoLib en coordenadas del mundo, para anclar efectos a los sopletes.
 *
 * <p>La cadena la hace GeckoLib en {@code GeoEntityRenderer#renderRecursively} y se reproduce
 * aquí paso a paso (verificado en las fuentes de 4.9.3):
 * {@code T(-pos/16) · T(pivote/16) · Rz·Ry·Rx · S · T(-pivote/16)}, donde el geo ya viene
 * espejado en X ({@code updatePivot(-x,y,z)}, {@code updateRotation(-rx,-ry,+rz)}) y el vértice
 * de un cubo vale {@code (-(ox+sx)/16, oy/16, oz/16)}. Los ángulos del hueso ya están en radianes.
 *
 * <p>Al final se aplica la raíz de la entidad: {@code Ry(180° - yaw del cuerpo)} y la posición.
 * El resultado se lee del último fotograma renderizado, así que va un frame por detrás: suficiente
 * para partículas.
 */
public final class VfxBones {

    /**
     * @param origin posición del mundo del punto pedido (pivote + {@code offsetLocal})
     * @param axis   eje de la boquilla del hueso en coordenadas del mundo, normalizado.
     *               Con la convención por defecto (+Y local) apunta hacia delante cuando
     *               los brazos se levantan en {@code flame_channel}.
     */
    public record Pose(Vec3 origin, Vec3 axis) {
    }

    private VfxBones() {
    }

    /**
     * @param offsetLocal desplazamiento en bloques dentro del hueso ({@code [0,-0.5,0]} = media
     *                    unidad hacia la punta); se aplica con la matriz del hueso.
     * @return null si la entidad no es GeckoLib o el hueso no existe (el llamante decide).
     */
    public static Pose of(Entity entity, String boneName, Vector3f offsetLocal, float partialTick) {
        return of(entity, boneName, offsetLocal, partialTick, new Vector3f(0.0F, -1.0F, 0.0F));
    }

    /**
     * Variante que permite elegir el eje local del hueso que apunta hacia la punta/boquilla.
     *
     * <p>El eje local {@code (0, -1, 0)} (-Y) es el eje que recorre el hueso desde el pivote
     * hacia la boquilla y que, al levantarse los brazos en {@code flame_channel}, apunta
     * hacia delante en el mundo.
     *
     * @param localAxis eje local del hueso que da la dirección de la boquilla (normalizado).
     * @return null si la entidad no es GeckoLib o el hueso no existe.
     */
    public static Pose of(Entity entity, String boneName, Vector3f offsetLocal, float partialTick, Vector3f localAxis) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        EntityRenderer<?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(entity);
        if (!(renderer instanceof GeoEntityRenderer<?> geo)) {
            return null;
        }
        GeoBone bone = geo.getGeoModel().getBone(boneName).orElse(null);
        if (bone == null) {
            return null;
        }

        // Cadena de padres, de la raíz hacia el hueso.
        List<GeoBone> chain = new ArrayList<>();
        for (GeoBone current = bone; current != null; current = current.getParent()) {
            chain.add(0, current);
        }
        Matrix4f matrix = new Matrix4f();
        for (int i = 0; i < chain.size(); i++) {
            GeoBone current = chain.get(i);
            boolean isTarget = (i == chain.size() - 1);
            matrix.translate(-current.getPosX() / 16.0F, current.getPosY() / 16.0F, current.getPosZ() / 16.0F);
            matrix.translate(current.getPivotX() / 16.0F, current.getPivotY() / 16.0F, current.getPivotZ() / 16.0F);
            matrix.rotate(new Quaternionf().rotationZYX(current.getRotZ(), current.getRotY(), current.getRotX()));
            matrix.scale(current.getScaleX(), current.getScaleY(), current.getScaleZ());
            if (!isTarget) {
                matrix.translate(-current.getPivotX() / 16.0F, -current.getPivotY() / 16.0F, -current.getPivotZ() / 16.0F);
            }
        }


        Vector3f point = new Vector3f(offsetLocal == null ? 0.0F : offsetLocal.x,
                offsetLocal == null ? 0.0F : offsetLocal.y,
                offsetLocal == null ? 0.0F : offsetLocal.z);
        matrix.transformPosition(point);

        Vector3f axis = new Vector3f(localAxis.x, localAxis.y, localAxis.z);
        matrix.transformDirection(axis);
        if (axis.lengthSquared() < 1.0E-8F) {
            axis.set(localAxis.x, localAxis.y, localAxis.z);
        }
        axis.normalize();


        // Raíz de la entidad: rotación del cuerpo y traslación (el geo no añade escala propia).
        float bodyYaw = entity instanceof net.minecraft.world.entity.LivingEntity living
                ? Mth.rotLerp(partialTick, living.yBodyRotO, living.yBodyRot)
                : entity.getYRot();
        Quaternionf root = new Quaternionf().rotationY((float) Math.toRadians(180.0D - bodyYaw));
        point.rotate(root);
        axis.rotate(root);

        Vec3 origin = entity.getPosition(partialTick).add(point.x, point.y, point.z);
        return new Pose(origin, new Vec3(axis.x, axis.y, axis.z));
    }
}
