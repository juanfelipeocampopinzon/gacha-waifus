# Changelog — Gacha Waifus Mod

Todas las notas de versión del mod en orden cronológico descendente.

---

## [2.1.0-beta.1] — 2026-10-04 (BETA)

> ⚠️ **Esta es una versión BETA.** Contiene modelos 3D experimentales (GeckoLib) y está pensada para pruebas.

### 🆕 Novedades — Modelos 3D GeckoLib
- 🦈 **Ellen Joe:** cola de tiburón en 3 articulaciones con animaciones `idle`, `walk`, `attack` y `special` (`ellen_joe.geo.json` / `ellen_joe.animation.json`).
- 🦊 **Hoshimi Miyabi:** grandes orejas de kitsune (mismo tono del pelo en la parte trasera y gris oscuro al frente) + katana con tsuba, tsuka y saya en el brazo (`miyabi.geo.json` / `miyabi.animation.json`).
- 👜 **Nicole Demara:** coletas superiores, mechones gruesos de nuca (pieza inferior retirada) y maletín cañón, con colorimetría 1:1 extraída de la skin (`nicole_demara.geo.json` / `nicole_demara.animation.json`).

### 🔧 Correcciones
- 🖼️ **Anby Demara:** restaurada la textura de entidad (`anby_demara.png`) que había quedado corrupta en el commit anterior; vuelta a la versión buena de 64×64.

### 📋 Roster incluido (9 waifus)
Miyabi, Astra Yao, Ellen Joe, Burnice White, Ye Shunguang, Nicole Demara, Anby Demara, Ukinami Yuzuha, Promeia.

### ⚠️ Limitaciones conocidas de la beta
- **Anby Demara** todavía usa el modelo de jugador vanilla (`PlayerModel`). Su modelo GeckoLib (pico en la rodilla derecha saliendo de la bota) está pendiente.
- Los modelos GeckoLib usan geometría cúbica estilo vanilla, no mallas redondeadas completas.

---

## [2.0.0] — 2026-10-03

- Primer roster completo con 9 waifus jugables y habilidades inspiradas en Zenless Zone Zero.
- Sistemas globales: sin fuego amigo, instancia única por jugador y resurrección con Núcleo Durmiente.
- Autotomatización de skins (`tools/skin_processor.py`) y meta-prompt (`research/prompt`).
