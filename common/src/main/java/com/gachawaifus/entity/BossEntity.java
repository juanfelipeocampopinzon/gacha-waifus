package com.gachawaifus.entity;

/**
 * Marcador para los jefes del mod (Ricardo Milos, Doctor House, Fernanfloo, Tung Tung Sahur).
 *
 * <p>Lo usan los sistemas que necesitan distinguir "jefes propios del mod" de cualquier mob
 * hostil de vanilla:
 * <ul>
 *   <li>{@link com.gachawaifus.client.color.ColorTagLayer} — el cuadradito de color flotante solo
 *       aparece sobre los jefes, no sobre zombies, skeletons ni mobs de otros mods.</li>
 * </ul>
 */
public interface BossEntity {
}
