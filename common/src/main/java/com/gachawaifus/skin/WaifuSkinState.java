package com.gachawaifus.skin;

/**
 * Forma visual que enseña ahora mismo una waifu.
 *
 * <p>Se sincroniza al cliente como un byte: el renderizador no puede mirar el objetivo ni los
 * cooldowns (viven en el servidor), así que la decisión se toma allí y viaja en el
 * {@code SynchedEntityData}. {@link #BASE} es la skin de siempre; el resto solo se usa si el
 * {@link WaifuSkinProfile} de esa waifu define textura para ese estado.
 */
public enum WaifuSkinState {
    /** Skin de reposo (pasivo, paseando o sentada). */
    BASE,
    /** Skin de combate: se enciende al tener objetivo y se apaga al perderlo. */
    COMBAT,
    /** Skin de la habilidad especial: dura unos segundos después de usarla. */
    SPECIAL,
    /** Skin de la ultimate: dura unos segundos después de lanzarla. */
    ULTIMATE
}
