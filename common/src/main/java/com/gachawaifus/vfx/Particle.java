package com.gachawaifus.vfx;

import net.minecraft.world.phys.Vec3;

/**
 * Partícula del prototipo: una posición, una velocidad, una edad y todo lo que el renderer
 * necesita para dibujarla como un quad.
 */
public final class Particle {

    public Vec3 pos;
    public Vec3 vel;
    /** Edad en ticks. Admite fracciones: al emitir se reparte dentro del tick. */
    public float age;
    public final float life;

    public final float sizeStart;
    public final float sizeEnd;
    public final Gradient color;
    public final String texture;
    public final boolean additive;
    /** Fase aleatoria 0..1: rota el quad de las partículas sin fotogramas. */
    public final float phase;
    public final float gravity;
    /** 0 = quad cuadrado; 1 = se estira en la dirección en que viaja (chorro). */
    public final float stretch;
    /** 0 = sin núcleo; >0 = dibuja dentro un disco caliente de ese tamaño relativo. */
    public final float core;
    /** Ondulación lateral en bloques por tick, para que la llama se rompa. */
    public final float swirl;
    /** Eje perpendicular a la velocidad sobre el que ondula. */
    public final Vec3 wobble;

    public Particle(Vec3 pos, Vec3 vel, float life, float sizeStart, float sizeEnd,
                    Gradient color, String texture, boolean additive, float phase, float gravity,
                    float stretch, float core, float swirl, Vec3 wobble) {
        this.pos = pos;
        this.vel = vel;
        this.life = life;
        this.sizeStart = sizeStart;
        this.sizeEnd = sizeEnd;
        this.color = color;
        this.texture = texture;
        this.additive = additive;
        this.phase = phase;
        this.gravity = gravity;
        this.stretch = stretch;
        this.core = core;
        this.swirl = swirl;
        this.wobble = wobble;
    }

    public boolean alive() {
        return age < life;
    }

    public void tick() {
        pos = pos.add(vel);
        if (swirl != 0.0F) {
            // Vaivén perpendicular: dos senos desfasados para que no parezca una onda regular.
            double wave = Math.sin(age * 0.45D + phase * 6.283D);
            pos = pos.add(wobble.scale(swirl * 0.02D * wave));
        }
        if (gravity != 0.0F) {
            // Escala de juguete: el valor del JSON es "aceleración suave", no 9,8 m/s².
            vel = vel.add(0.0D, -gravity * 0.05D, 0.0D);
        }
        age++;
    }

    /** Edad normalizada 0..1 (con el partialTick para que no vaya a saltos). */
    public float normalizedAge(float partialTick) {
        return Math.min(1.0F, (age + partialTick) / Math.max(1.0F, life));
    }
}
