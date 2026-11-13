package com.gachawaifus.vfx;

import java.util.List;

/** Un stage del JSON: una porción de la habilidad con sus capas y su tier. */
public final class VfxStage {

    public final String effectId;
    public final String stage;
    public final String tier;
    public final float duration;
    /** Segundos desde el inicio del casteo ({@code trigger.at}); 0 si no lo declara. */
    public final float at;
    public final List<VfxLayer> layers;

    public VfxStage(String effectId, String stage, String tier, float duration, float at,
                    List<VfxLayer> layers) {
        this.effectId = effectId;
        this.stage = stage;
        this.tier = tier;
        this.duration = duration;
        this.at = at;
        this.layers = layers;
    }

    /** Tope de partículas del tier × 1.5: el prototipo no permite crecer sin límite. */
    public int particleCap() {
        int base = switch (tier) {
            case "S" -> 40;
            case "M" -> 150;
            case "XL" -> 1200;
            default -> 400;
        };
        return (int) (base * 1.5F);
    }
}
