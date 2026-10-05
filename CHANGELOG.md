# Changelog — Gacha Waifus Mod

Todas las notas de versión del mod en orden cronológico descendente.

---

## [3.0.0] — 2026-10-04

### 🎰 Sistema Gacha — Terminal Gacha
- **Terminal Gacha** (`gacha_terminal`): máquina de tiradas crafteable en survival (receta: domo de 8 bloques de amatista + 1 diamante al centro + base de 3 piedras).
- **Tiradas con diamantes:** 1 diamante = 1 tirada; Shift + clic = x10. Tasa base 1.6%, soft pity +6% desde la tirada 51 y 5★ garantizada en la 64 (un stack).
- **50/50:** al perder recibes a Nicole Demara (placeholder) y la siguiente 5★ es garantizada; si ya la tienes, cae directo a la destacada.
- **Anti-dupe:** no permite tirar si ya posees la waifu destacada; un 5★ duplicada a mitad de x10 se convierte en comida.
- **Banner diario:** la destacada rota cada día de Minecraft entre las 9 waifus.
- **Consuelo:** cada tirada sin 5★ da comida de una tabla ponderada (hasta manzana dorada encantada).
- **Persistencia:** pity y garantizado por jugador, guardados en el mundo (`GachaSavedData`).

### 💊 Cápsula Waifu — almacén portátil
- **Cápsula Waifu** (`waifu_capsule`): item crafteable (piedra sobre palo) que funciona como cofre de waifus.
- **Guardar:** Shift + clic en tu waifu la almacena (despawnea a salvo). **Invocar:** clic derecho abre el cofre virtual y clic en una entrada la trae de vuelta.
- **Pérdida segura:** la colección vive en datos del servidor por jugador (`WaifuStorageSavedData`), no en el item: si se destruye, al craftear otra recuperas todo.

### 🔧 Correcciones y ajustes
- ⚔️ **Ye Shunguang:** los cubos conectores de las coletas laterales ahora quedan perpendiculares a la cabeza (rotación `[0,0,∓76]`, pasador horizontal) en vez de seguir el ángulo del pelo.

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
