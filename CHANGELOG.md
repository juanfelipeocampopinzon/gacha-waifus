# Changelog — Gacha Waifus Mod

Todas las notas de versión del mod en orden cronológico descendente.

---

## [4.9.2] — 2026-10-10

> Nota: entre 4.3.0 y 4.9.2 se acumularon varias versiones sin apuntar aquí (jefes Doctor House /
> Wilson / Fernanfloo, terminal estándar, bloque de máquina gacha, sonidos). Esta entrada documenta
> lo de 4.9.2; el resto se puede reconstruir por `git diff 24df866..HEAD`.

### 🎨 Piezas de pecho, pestaña creativa, Cápsula por páginas y configs nuevas
- **Zhu Yuan e Yi Xuan con pieza grande**: convertidas a `WaifuBustPlayerModel` + `TUBE_BIG` (3.0F) y
  sus skins pasadas a 64×128. La pieza no se dibuja a mano: `research/bust/tube_v5_sizes.py` la
  genera muestreando las filas del torso y pintando el desplegado en (0,121);
  `validate_v5.py` → *TODO OK: 45 waifus con su pieza*.
- **Claret** entra por ahora **sin pieza** (skin 64×64, `PlayerModel` slim), a la espera de su arte.
- **Pestaña creativa ordenada**: `ModCreativeTabs` ya no lista 86 items a mano — recorre
  `WaifuRoster.ROTATION` (los tokens quedan en el orden real del gacha) y después van jefes,
  bolitas, terminales, máquina y cápsula.
- **Cápsula Waifu paginada** (`WaifuStorageMenu` + `WaifuStorageScreen`): rejilla de 27 por página,
  lista continua (primero las vivas, luego las caídas), páginas que crecen con la colección, cambio
  de página con « » o con la rueda del ratón. El estado de página viaja al cliente por
  `ContainerData`, así que sigue siendo autoritativo del servidor. Se quitó el tope de 27+9.
  La 4ª fila de casillas (vacía) se forra como barra de navegación para que los botones no parezcan
  flotando sobre la rejilla; los botones se grisan cuando no hay más páginas. **Parche visual**: la
  GUI propia de la cápsula sigue pendiente.
- **Radio de deambulación configurable** (`wander.range`, 4–64, por defecto 16): las waifus pasean
  dentro de un disco alrededor del dueño y solo teletransportan al superarlo.
  - `WaifuFollowOwnerGoal` sustituye al vanilla (cuyas distancias son `final` y no releían la config)
    y lee el radio en vivo; `AbstractWaifuEntity#tick` centra la restricción del mob en el dueño con
    `restrictTo()`, que es lo que ya respetan los goals de paseo de vanilla → cada waifu **conserva
    su velocidad de paseo** (0.6/0.7/0.8/1.0).
  - Se aplica desde `WanderGoalsHandler` (`EntityJoinLevelEvent`) para no tocar los 63
    `registerGoals()`.
- **La etiqueta de color ya no es un wallhack**: `ColorTagLayer` tira un rayo desde el ojo del
  jugador hasta el punto donde se pinta el cuadradito y, si hay un bloque en medio, no la dibuja.
- **Fix de arranque — conflicto de configs**: registrar dos `ModConfigSpec` COMMON del mismo mod
  (`TeamConfig` y `WanderConfig`) reventaba al cargar con *"Detected config file conflict on
  gachawaifus-common.toml"*. NeoForge admite **un spec por tipo y mod**: la sección `wander` se
  construye ahora dentro de `TeamConfig.SPEC` vía `WanderConfig.appendTo(builder)`.

---

## [4.3.0] — 2026-10-08

### ⚙️ Botón "Config" del menú Mods + límite de equipo configurable + 3 modos de combate
- **Botón Config activado:** `GachaWaifusMod` registra `IConfigScreenFactory` (pantalla
  `ConfigurationScreen` de NeoForge, solo en cliente) — antes salía desactivado porque nadie
  registraba la fábrica. Ahora **Mods → Gacha Waifus → Config** abre la configuración del mod
  (etiqueta de color y límite de equipo, con traducciones en `en_us`/`es_es`).
- **Límite de waifus en el equipo** (nueva config COMMON, `config/gachawaifus-common.toml`):
  | Opción | Por defecto | Rango |
  |---|---|---|
  | `team.limit` | `0` (ilimitado) | 0–64 waifus invocadas por jugador |
  - Se comprueba al invocar: en los **19 tokens** (evento `RightClickBlock`, cancelado antes de
    `useOn`, sin repetir código en cada token) y en la **Cápsula** (`summon()`/`revive()`).
  - Recuento = waifus vivas del jugador en todas las dimensiones; guardar con Shift + clic en la
    Cápsula libera sitio. Aviso en la barra de acción: `§c[GachaWaifus] Equipo completo: ...`.
- **3 modos de combate por waifu:** **Pasivo** (§a) · **Neutro** (§e) · **Agresivo** (§c, por
  defecto). Se cambian con **Shift + clic (mano vacía) sobre la waifu** y persisten en su NBT.
  - Implementado una sola vez en `AbstractWaifuEntity` filtrando `setTarget`: PASIVO no acepta
    objetivos (kits apagados), NEUTRO solo represalia/defender al dueño (nunca aggro a la vista),
    AGRESIVO = comportamiento clásico. Fix en Astra Yao y Nicole, que capturaban el clic y no
    dejaban llegar el Shift.

---

## [4.2.0] — 2026-10-08

### 🏷️ Etiqueta de color: arreglo de posición + solo el cuadradito + configuración en el menú
- **Bug corregido:** la etiqueta del color salía **debajo** del mob (y mal escalada en los jefes
  Ricardo Milos / Doctor House / Fernanfloo). Ahora se dibuja **sobre la cabeza** en todos los
  seres vivos, con el mismo tamaño que un nametag (se deshace la rotación y escala del pose del
  renderer dentro de `ColorTagLayer`).
- **Solo el cuadradito:** la etiqueta ya no lleva el nombre del color en texto, únicamente `██`
  con el color RGB del mob: el color se ve de un vistazo sin leer nada.
- **Configuración de cliente** (nuevo `color/ColorConfig.java`, menú **Mods → Gacha Waifus →
  Config** o `config/gachawaifus-client.toml`):
  | Opción | Por defecto | Rango |
  |---|---|---|
  | `floating_label.enabled` | `true` | on/off — apaga o enciende la etiqueta |
  | `floating_label.range` | `64` | 16–256 bloques — más allá no se dibuja |
- **Descripción del menú de Mods:** `neoforge.mods.toml` ahora describe el mod completo (gacha,
  combate, colores, jefes) en vez del texto de prototipo.

---

## [4.1.0] — 2026-10-07

### 🎰 Máquina Gacha como bloque (solo diseño, sin lógica aún)
- **`GachaMachineBlock`** + **`ModBlocks`** (primer bloque del mod): `FACING` horizontal, mira al
  jugador al colocarla, `MapColor.COLOR_PINK`, sonido de cristal, `noOcclusion()` (cupula y
  cartel sobresalen del cubo).
- **Modelo por elementos (17 piezas):** base rosa, cuerpo con marco, pomo giratorio, ranura de
  monedas, hueco de salida, cupula de cristal translúcido con **4 cápsulas gashapon** dentro y
  cartel de frutas pixel-art encima (sube hasta y=24, 1.5 bloques).
- 4 variantes de `blockstate` por rotación, modelo de ítem 3D y **primera loot table** del mod
  (suelte el bloque). 8 texturas 16×16 placeholder.
- `lang`: "Gacha Machine" / "Máquina Gacha".
- *Pendiente:* lógica de tiradas (fase posterior): BlockEntity para la cupula, VoxelShape con
  rotación y receta.

---

## [4.0.0] — 2026-10-06

### 🔵 Terminal Estándar & Banner Permanente
Llega el banner permanente al mod para obtener directamente a las 7 waifus estándar sin depender del banner rotativo diario:

| Componente | Detalle |
|---|---|
| **Ítem y Registro** | `StandardTerminalItem` registrado como `STANDARD_TERMINAL` en `ModItems`, en pestaña de creativos `ModCreativeTabs` y modelo `models/item/standard_terminal.json`. |
| **Moneda** | Gasta **Bolitas Azules** (`BLUE_BALL`), 1 por tirada (clic) o 10 tiradas (Shift + clic). |
| **Pity independiente** | Variable `standardPity` en `GachaSavedData.PlayerState` con guardado/carga NBT (`standard_pity`). No interfiere con la pity ni la garantizada del Terminal Gacha destacado. |
| **Probabilidades** | 1.6 % base, soft pity a partir de la tirada 50 (+6 % por tirada adicional), hard pity en la tirada 64. |
| **Pool cerrado** | Solo entrega las 7 waifus estándar del `WaifuRoster.STANDARD_POOL` (Rina, Lycaon, Nicole, Koleda, Grace, Soldier 11, Nekomata) que el jugador no posea. Sin 50/50 ni garantizada. Si ya tiene las 7, el banner avisa y no consume bolitas. |
| **Consuelo** | Las tiradas que no dan 5★ entregan comida variada (galletas, pan, carne, tarta, manzana dorada encantada). |

### 🔷 Receta de la Bolita Azul
- Nuevo crafteo `recipe/blue_ball.json`: 1 lingote de hierro en el centro + 4 lapislázuli en cruz (1 hierro + 4 lapis → 1 bolita azul).
- Textos actualizados en `en_us.json` y `es_es.json` para reflejar el crafteo y su uso en el Terminal Estándar.

---

## [3.10.0] — 2026-10-06

### 🐱 Nekomiya Mana (Nekomata) entra al roster + pieza del pecho para las 5 que faltaban
Decimonovena waifu (Cunning Hares, ZZZ — RAMA A), gata blanca veloz: Blanco / Attack, nametag §8,
modelo `WaifuBustLayers.TUBE` (2×2, slim). Stats: HP 95 · Speed 0.34 · ATK 12 · Armor 3.

| Dato | Valor |
|---|---|
| Normal | Phantom Double Stab (18 ticks) — melee con partículas CRIT y `PLAYER_ATTACK_CRIT` (pitch 1.4) |
| Especial | Cat Step Show (240 ticks) — 16 de daño + MOVEMENT_SLOWDOWN 100t, `SWEEP_ATTACK` + `CAT_HISS` |
| Ultimate | Blade Claw Assault (600 ticks) — mensaje "¿Te gustaron mis garras, nya?", self SPEED 200t y AoE 8 bloques de 30.0F mágico (filtro obligatorio), `SWEEP_ATTACK` + `CAT_HISS` (pitch 0.8) |

Y la pieza del pecho llega a las cinco waifus que aún no la tenían (Lycaon sigue sin, es hombre):

| Waifu | Pieza | Renderer |
|---|---|---|
| Koleda Belobog | 2×2 `TUBE` | `WaifuBustPlayerModel(TUBE, true, 2.0F)` |
| Grace Howard | **Grande 3×3 `TUBE_BIG`** | `WaifuBustPlayerModel(TUBE_BIG, true, 3.0F)` (talla grande, como Rina) |
| Soldier 11 | 2×2 **`TUBE_WIDE`** (nueva capa) | `WaifuBustPlayerModel(TUBE_WIDE, false, 2.0F)` — conserva la malla ancha de Steve |
| Tobichi Origami | 2×2 `TUBE` | `WaifuBustPlayerModel(TUBE, true, 2.0F)` |
| Nekomiya Mana | 2×2 `TUBE` | `WaifuBustPlayerModel(TUBE, true, 2.0F)` |

Skins de las 5 extendidas a 64×128 y desplegado UV pintado en (0,121) con el pipeline habitual
(`skin_processor.py` solo para las dos que faltaban assets: origami y nekomata; `extend_skin_128.py`,
`tube_v5_sizes.py`, `validate_v5.py` y `compare_originals.py` para las cinco). Resultado: **18 waifus
con pieza** (10 normales + 8 grandes), 0 choques de UV y **0 píxeles dañados** frente a las originales.

### 🎰 Pérdida del 50/50: pool fijo de 7 estándar
Ya no consuela con "cualquiera no poseída": `WaifuRoster.STANDARD_POOL` fija los 7 clásicos
(Rina, Von Lycaon, Nicole, Koleda, Grace Howard, Soldier 11, Nekomata) y
`randomStandardUnowned(player, featured)` elige al azar entre los de ese pool que no tengas. Si ya
los tienes todos, cae al `randomUnowned` de la rotación completa (nunca null mientras quede alguna).

### ⚔️ Tobichi Origami (Date A Live) — completado en esta versión
Blanco / Defense, modelo slim. Lo que se hizo aquí sobre el primer pase:

| Dato | Valor |
|---|---|
| Stats | HP 105 · Speed 0.32 · ATK 9 · Armor 8 |
| Normal | Corte de Luz (22 ticks) — melee, partículas CRIT |
| Especial | Escudo de Alas (240 ticks) — 12 de daño + ABSORPTION al objetivo (mismo patrón que Rina) |
| Ultimate | Mandamiento Divino (700 ticks) — 35 de daño mágico en 8 bloques + REGENERATION al owner. Frase: "¡Que mi luz sea tu escudo!" [INVENTADA] |

- **Fix de build:** `TobichiOrigamiTokenItem` escribía `ToolTipFlag` (T mayúscula); la clase real es
  `TooltipFlag`. El meta-prompt (`research/prompt`, Regla 13) ya advierte de la grafía exacta.
- **Fix de runtime:** `TOBICHI_ORIGAMI` no estaba en `registerAttributes` (la entidad crasheaba al
  invocarse por mapa de atributos vacío). Añadido junto al de `NEKOMATA`.
- **Assets procesados** (antes pendientes): `textures/entity/tobichi_origami.png` 64×128 con su
  desplegado, medalla del token y `models/item/tobichi_origami_token.json`.

**Ojo de diseño (no tocado):** la Especial le da ABSORPTION **al enemigo**, no al owner. Es el
patrón heredado de la PLANTILLA 1 (aplica `[EFECTO_ESP]` sobre `target`), y Rina ya lo tiene igual:
si se corrige, se corrige para las dos a la vez y en la plantilla.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR `gachawaifus-3.10.0.jar`. Registro completo: `ModEntities`, `ModItems`,
`GachaWaifusMod` (atributos + renderer + capa `TUBE_WIDE`), `ModCreativeTabs`, `WaifuRoster`
(`ROTATION` con 19 + `STANDARD_POOL`), `WaifuColors` (BLANCO ×2: Promeia y Nekomata), lang
en_us/es_es y `models/item/nekomata_token.json`. `validate_v5.py`: 18/18 piezas OK.
`compare_originals.py`: 19/19 skins con 0 píxeles distintos del original.

---

## [3.9.0] — 2026-10-06

### 🐺 Rina (Alexandrina) y Von Lycaon entran al roster
Dos personajes nuevos, y **no compilaban**: siete errores, todos de la misma familia de siempre.

| Error | Arreglo |
|---|---|
| `VonLycaonTokenItem`: `import ...TooltipContext` | **Cuarta vez.** Esa clase no existe en 1.21.1 → import fuera |
| `RinaTokenItem` y `VonLycaonTokenItem`: usaban `ColorTooltip` y `WaifuColor` sin importarlos | Los dos imports añadidos (es el color del tooltip y su fila en la matriz de daño) |
| `GachaWaifusMod`: usaba `VonLycaonEntity` y `VonLycaonRenderer` sin importarlos | Imports añadidos |
| `en_us.json` / `es_es.json`: `entity.gachawaifus.rina` estaba **dos veces** | Duplicado eliminado (92 claves únicas e idénticas en los dos idiomas) |

**Reparto de la pieza del pecho (lo que pediste):**

| Personaje | Pieza | Skin | Modelo |
|---|---|---|---|
| **Rina (Alexandrina)** | **Grande 3×3** (`TUBE_BIG`, `3.0F`) | 64×128 con el desplegado en (0,121) | Jugador **slim** |
| **Von Lycaon** | **Ninguna** (es hombre) | **64×64** (sin mitad de abajo) | Jugador **ancho (`ModelLayers.PLAYER`, el de Steve)** + `scale(1.15)` para que se vea más grande que las waifus |

Von Lycaon queda **fuera** de `SECTION`/`VANILLA_IDS` y del validador: si se apuntara, el script le
pintaría un desplegado que nadie usa y `validate_v5.py` fallaría buscando una pieza inexistente. Su
mensaje de invocación también se corrigió a "ha sido **invocado**".

### 🎰 El 50/50 del gacha (verificado, no tocado)
Comprobado en el código: **no hay ningún nombre escrito a mano** en `GachaTerminalItem`. El consuelo
del 50/50 perdido lo decide `WaifuRoster.randomUnowned(player, destacada)`, que elige **al azar entre
todas las no poseídas de la rotación**. Con Lycaon y Rina ya dentro de `ROTATION` (14 entradas), los
dos entran solos en el banner diario, en el 50/50 y en el consuelo: en cuanto la destacada sea una
de ellos, o al perder el 50/50, pueden salir. Si antes salía siempre Nicole, era porque en ese
momento era la única que faltaba (no hay lista fija).

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.9.0.jar`** · `validate_v5.py` **13/13** (6 normales + 7
grandes; Rina 3×3 con 114/114 celdas, 0 choques; Lycaon correctamente ausente) ·
`compare_originals.py` **0 píxeles dañados en las 14 skins** · `validate_colors.py` **OK** (11 colores,
matriz regular, roster con color — Lycaon azul, Rina rosa — y los dos idiomas completos) ·
`um publish check` **PASS (556 archivos, 0 fallos, 0 avisos)** · desplegado en Prism `1.21.1` y
`%appdata%\.minecraft\mods\`.

---

## [3.8.2] — 2026-10-06

### 🔇 La música de Ricardo Milos se corta cuando muere (garantizado)
Pedías que la música parase al morir el jefe, así que el corte ya no depende de un solo aviso: ahora
hay **tres capas**, y cualquiera de ellas basta.

| Capa | Cuándo actúa |
|---|---|
| `RicardoMilosEntity.stopTheme()` en `die()` | Al morir: manda `ClientboundStopSoundPacket` (id + `MUSIC`) a todos los jugadores del nivel |
| Lo mismo en `remove(RemovalReason)` | Cubre muertes/eliminaciones que no pasan por `die()` (despawn, `/kill` raro, descarga de chunk) |
| **`client/audio/BossThemeWatch` (nuevo)** | Cada 10 ticks el cliente comprueba si queda algún jefe **vivo y ya herido** a ≤64 bloques; si no queda ninguno, corta el tema por su id con `SoundManager.stop(id, MUSIC)` |

La señal del cliente es **la vida** (`getHealth() < getMaxHealth()`): el jefe solo consigue objetivo
después de que alguien le pegue, así que "herido" = "pelea en curso", y la vida de un mob está
sincronizada con el cliente — no hace falta ni un paquete nuevo. Esto también corta la música si te
alejas a media pelea o si lo mata otro.

Y al revés: si **no hay ningún jugador** dentro del alcance audible (64 bloques), el servidor
**rearma** el tema, así que si vuelves a la pelea la música arranca otra vez en vez de quedarse muda.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.8.2.jar`** · `um publish check` **PASS (0 fallos, 0 avisos)** ·
desplegado en Prism `1.21.1` y `%appdata%\.minecraft\mods\`.

---

## [3.8.1] — 2026-10-06

### ❄️ Yidhari con el arte nuevo (y la pieza intacta)
Actualizaste su skin (`chicas/yidhari/skin.png`, 64×64) y **la pieza del pecho no se ha tocado**: sigue
siendo la **grande 3×3**, UV `(0,121)`, pivote vanilla 3.79, el mismo cubo `8×3×3`. Lo único que se
hizo fue volver a pasar el pipeline para que el arte nuevo llegue al mod:

```powershell
python tools/skin_processor.py --id yidhari --skin "chicas/yidhari/skin.png"   # skin + medallón
python research/bust/extend_skin_128.py yidhari                                # 64x64 -> 64x128
python research/bust/tube_v5_sizes.py                                          # repinta la pieza
```

Lo que cambió de verdad en tu skin: **243 píxeles de la mitad de abajo** (y 30..61, piernas/botas y
zona inferior). La **cabeza, el torso y los brazos están idénticos**, así que el medallón del token y
la ventana del pecho (filas 22-25) salen exactamente iguales que antes — de ahí que la pieza se vea
como estaba, que es justo lo que pediste.

También se actualizó la ruta del original en `compare_originals.py` y `restore_originals.py`: el
archivo viejo (`Yidhari-Murphy-…-planetminecraft-com.png`) ya no está, ahora es `yidhari\skin.png`.

### 👑 Ricardo Milos con su skin de verdad
Instalada la skin que dejaste en `bosses/ricardo milos/` (la del meme, 64×64) en
`textures/entity/ricardo_milos.png`, convertida a **RGBA** (venía en PNG indexado) y comprobada cara
por cara: **las 36 caras de las 6 cajas están pintadas** (nada invisible desde ningún lado). El
modelo sigue siendo el de jugador **ancho** (`ModelLayers.PLAYER`), que es el que le pega.

El generador del placeholder (`research/mobs/make_mob_skins.py`) ahora **no pisa** una skin ya
instalada: avisa y se para salvo que le pases `--force`.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.8.1.jar`** · `validate_v5.py` **12/12** (Yidhari 3×3,
114/114 celdas, 0 choques) · `compare_originals.py` **0 píxeles dañados** en las 12 skins (Yidhari
comparada contra su arte nuevo) · `um publish check` **PASS (550 archivos, 0 fallos, 0 avisos)** ·
desplegado en Prism `1.21.1` y `%appdata%\.minecraft\mods\`.

> 📁 `bosses/<mob>/` es desde ahora la carpeta de originales de los mobs, igual que `chicas/` lo es
> para las waifus: ahí vive el arte de verdad, y el mod solo tiene la copia instalada.

---

## [3.8.0] — 2026-10-06

### 🎵 Música de pelea para Ricardo Milos
Cuando el jefe **entra en combate** (o sea, cuando consigue un objetivo: en la práctica, cuando una
waifu le pega) suena su tema; si se queda sin pelea 20 s, se rearma para la siguiente, y **al morir
se corta** (paquete de parar sonido, no se queda sonando de fondo).

| Dato | Valor |
|---|---|
| Pista | *Ricardo Milos – basshunter dota* (la del enlace que pasaste) |
| Archivo | `assets/gachawaifus/sounds/boss/ricardo_milos.ogg` (**OGG Vorbis, mono, 44.1 kHz**, 3:15, 1,8 MB) |
| Id | `gachawaifus:boss/ricardo_milos` (`sounds.json` + `ModSounds.RICARDO_MILOS_THEME`) |
| Alcance | volumen 4 → el juego atenúa linealmente hasta `max(volumen,1) × 16 = **64 bloques**` |
| Disparo | `aiStep` del jefe al tener objetivo; `ClientboundStopSoundPacket` al morir |

**Respuesta a tu duda del formato:** el **mp3 no sirve**. Minecraft solo reproduce **OGG Vorbis**, y
para que el sonido se pueda situar en el mundo (que se oiga más fuerte de cerca) tiene que ser
**mono**: un estéreo se oye al mismo volumen en toda la zona. Para no repetir el trabajo hay un
script nuevo, `tools/add_music.py`, que convierte cualquier audio:
```powershell
python tools/add_music.py "C:/ruta/cancion.mp3" boss/mi_tema
```
Deja el `.ogg` en su sitio, guarda copia del original en `musica/` e imprime el bloque exacto que hay
que pegar en `sounds.json`.

⚠️ **`sounds.json5` era un archivo muerto:** Minecraft (y NeoForge 21.1) **no** leen JSON5, así que
las tres entradas de Burnice que había ahí nunca sonaron. Ahora el manifiesto es `sounds.json` de
verdad (con las tres entradas de Burnice conservadas y la música nueva). De paso quedó anotado en
`AGENTS.md` y en el manual de colores.

### 🎯 Indicador de color para cada enemigo
El sistema de colores ya pintaba el color encima de los mobs, pero se leía como texto suelto. Ahora:

- **Etiqueta flotante con cuadradito** (`ColorTagLayer`): `██ Morado` — el color entra por los ojos
  sin tener que leer el nombre. Interruptor `ColorSettings.LABEL_SWATCH`.
- **Indicador en la mirilla** (`EnemyColorHud`, nuevo): al apuntar a un enemigo sale justo debajo de
  la mirilla un cuadro con **su color** y **lo que le hace tu waifu activa**: `▲ Fuerte` (+33 %),
  `▼ Débil` (−33 %) o `— Neutro`; si no tienes waifu cerca, lo dice. Es una capa de GUI registrada
  sobre la mirilla vanilla (`RegisterGuiLayersEvent.registerAbove(VanillaGuiLayers.CROSSHAIR, …)`),
  todo calculado en el cliente (color del mob por UUID, color tuyo por tu waifu más cercana): no
  añade ni un paquete de red. Interruptor `ColorSettings.AIM_INDICATOR`.
- Claves nuevas en los dos idiomas: `gachawaifus.color.aim_strong/weak/neutral/no_waifu` (86 claves
  en `en_us` y `es_es`, idénticas).

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.8.0.jar`** · `validate_v5.py` **12/12** · paridad de
idiomas 86/86 · `um publish check` **PASS (549 archivos, 0 fallos, 0 avisos)** · desplegado en Prism
`1.21.1` y `%appdata%\.minecraft\mods\`.

> ℹ️ *Basshunter – DotA* es música con derechos: mientras el mod sea privado no hay problema, pero si
> algún día lo publicas, cambia el `.ogg` por una pista libre (el resto del sistema no cambia).

---

## [3.7.0] — 2026-10-06

### ❄️ Yidhari Murphy, terminada (y compilando)
El código que había dejado a medias la IA local **no compilaba**: cinco errores, todos de piezas
que faltaban.

| Error | Arreglo |
|---|---|
| `YidhariTokenItem`: `import net.minecraft.world.item.TooltipContext` | Esa clase **no existe en 1.21.1**; el tipo `TooltipContext` que pide el método es el anidado `Item.TooltipContext` (como en `GachaTerminalItem`) |
| `YidhariTokenItem`: imports **después** de la clase | Movidos arriba |
| `ModEntities` usaba `YidhariEntity` sin importarla | Import añadido |
| `ModItems` usaba `YidhariTokenItem` sin importarla | Import añadido |
| `GachaWaifusMod` usaba `YidhariEntity`/`YidhariRenderer` sin importarlas | Imports añadidos y registro simplificado |

Y le faltaba todo el arte: ahora tiene **skin** (su original de `chicas/yidhari/`, pasado a 64×128
con el nuevo `research/bust/extend_skin_128.py`), **medallón de token** (`tools/skin_processor.py`)
y la **pieza del pecho grande** (sección 3×3, UV `(0,121)`, capa `WaifuBustLayers.TUBE_BIG`).
En inglés faltaban sus dos claves de idioma (el nombre y el token salían en crudo): añadidas, y la
invocación ya usa `message.gachawaifus.yidhari_summoned` en vez de un literal.

### 👑 Ricardo Milos — el jefe de recompensa (mob nuevo)
Un enemigo **neutral** que suelta **10 tiradas** (Bolitas Rosas) al morir, pensado para que no lo
puedas tumbar tú solo:

| Regla | Cómo está hecho |
|---|---|
| **Neutral** | Solo lleva `HurtByTargetGoal`: no busca a nadie, devuelve el golpe a quien le pegue. Las waifus sí lo ven como `Enemy` y empiezan ellas la pelea |
| **El jugador casi no le hace daño** | Todo daño cuyo atacante sea un jugador se multiplica por **0,2** (un 80 % menos) y suelta el aviso *"Tu arma apenas le hace cosquillas"*. Con 500 de vida y 10 de armadura, sin waifus no se tumba |
| **Fuerte** | 500 de vida, 10 de armadura + 4 de dureza, 14 de daño, 0,75 de resistencia a empujones y **Sobrecarga Etérea**: telegrafía un anillo en el suelo 1,5 s y estalla (18 de daño, lentitud y empujón en 7 bloques) |
| **Recompensa** | Las 10 Bolitas Rosas van al dueño de la waifu que dio el golpe de gracia (o al jugador que lo remató); si no hay nadie cerca, caen al suelo |

Es de categoría `MONSTER`, no desaparece por alejarse (`setPersistenceRequired`) y tiene **huevo de
invocación** en el menú creativo (violeta oscuro + brillo magenta). Además **aparece rara vez en el
Overworld** por su modificador de bioma `data/gachawaifus/neoforge/biome_modifier/add_ricardo_milos.json`
(peso 4, de uno en uno; como es neutral, encontrártelo no es peligroso). Su skin es un **placeholder**
generado por `research/mobs/make_mob_skins.py`: el arte definitivo se decide más adelante y solo hay
que sustituir `textures/entity/ricardo_milos.png` — no se toca código.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.7.0.jar`** · `validate_v5.py` **12/12** (12 waifus, 6
normales + 6 grandes, 0 choques de UV, todas las celdas pintadas) · `compare_originals.py` **0
píxeles dañados en las 12 skins** · desplegado en Prism `1.21.1` y `%appdata%\.minecraft\mods\`.

---

## [3.6.2] — 2026-10-06

### 📏 La punta ahora se mueve en CUADROS (y se nota)
Tenías razón: la deformación no se notaba en la punta. El problema era que los canales estaban en
**escalas** (`1.0 ± tanto`) y la punta no está a la misma distancia del pivote en la pieza normal
(2×2) que en la grande (3×3), así que el mismo número movía la punta una cantidad distinta en cada
waifu y, con resortes duros, se quedaba en nada.

Ahora la física pide **cuadros de modelo** (1 cuadro = 1 unidad = **1 píxel de skin**) y quien
aplica convierte con el medio lado del rombo, que lee del propio cubo de la pieza:

```
escalaZ = 1 + cuadros / medioLado,   medioLado = sección × 0.7071
→ 1 cuadro de petición = 1 cuadro de movimiento real en la punta, en las dos piezas
```

| Canal | Intervalo |
|---|---|
| Punta hacia fuera | **hasta 1 cuadro** (con un 10 % de rebote por la inercia) |
| Punta hacia dentro | 0,35 cuadros |
| Hinchazón vertical | ±0,30 cuadros |

### 🌊 Movida por la inercia (y con velocidad propia)
Los resortes son **blandos a propósito** (rigidez 30, freno 4,2) para que el movimiento **dure** y
se vea el bamboleo en vez de un tirón seco, y lo que los mueve es la inercia real:

| Inercia | Efecto |
|---|---|
| **Caída** (velocidad vertical) | la punta **flota hacia fuera** (1 bloque/tick de caída = 1 cuadro) |
| **Aterrizaje** (frenazo) | la punta se mete y la pieza se aplasta |
| **Caminar** (velocidad de avance) | la punta **se queda atrás**, más el vaivén del paso |
| **Girar** | balanceo lateral suave |

Comprobado con `research/bust/sim_physics.py`, que reproduce los resortes fuera del juego:

```
caida suave (-0.3 bl/tick)   punta +0.02..+0.38 cuadros
caida fuerte (-1.0 bl/tick)  punta +0.08..+1.10 cuadros
aterrizaje (frenazo)         punta -0.09..+0.37 cuadros
caminar (bob del paso)       punta -0.16..+0.64 cuadros
bamboleo libre tras un tirón de 1 cuadro: 10 cruces por cero y se apaga solo (no oscila sin fin)
```

**Para ajustarlo** (todo en `com/gachawaifus/bust/BustPhysics.java`): `TIP_OUT_UNITS` /
`TIP_IN_UNITS` (el intervalo, en cuadros), `SWELL_UNITS`, `SWAY_DEGREES`, los multiplicadores
`FALL_TO_UNITS`, `LAND_TO_UNITS`, `WALK_LAG_UNITS`, `STEP_UNITS`, `BREATH_UNITS`, y `STIFFNESS` /
`DAMPING` (más bajos = bamboleo más largo). `ENABLED = false` la deja quieta.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.6.2.jar`** · `validate_v5.py` 11/11 (piezas y texturas) ·
desplegado en Prism `1.21.1` y `.minecraft/mods`.

---

## [3.6.1] — 2026-10-06

### 🚶 Miyabi y Burnice vuelven a caminar
No era cosa de la 3.6.0: el fallback de la **pose neutra** en su segundo controlador de animación
estaba ahí desde la 3.2.9. GeckoLib aplica los controladores **en orden y sin mezclar**, y el
`attack_controller` se registra después del de movimiento, así que devolver la pose `rest` (un único
keyframe a cero en piernas, brazos, cuerpo y cabeza) **pisaba la animación de caminar**: se
calculaba y se borraba en el mismo fotograma. Solo les pasaba a ellas dos porque las otras cuatro
devuelven `PlayState.STOP`.

- **Miyabi**: la pose neutra solo manda **parada** (`ultimatePhase == 0 && !state.isMoving()`).
- **Burnice**: caminando devuelve `PlayState.STOP` y manda el controlador de movimiento.

De paso, `flame_rain` (que se disparaba sin estar declarado, así que se ignoraba) ya está
registrado como animación disparable.

### 🎰 Lógica del Gacha rectificada
Auditoría completa de la lógica (tasas reales: **1,6 %** base, soft pity desde la **51** con +6 por
tirada, **64** garantizada, media ~37 tiradas) y arreglo de lo que iba raro:

| Lo que pasaba | Cómo queda |
|---|---|
| **Un 5★ podía no dar nada** y aun así resetear la pity y quemar la garantía (si el "consuelo" era la propia destacada) | Un 5★ **nunca sale vacío**: destacada → no poseída al azar → copia de repuesto. La pity solo se resetea cuando se entrega |
| **Si ya tenías la destacada, el terminal se bloqueaba entero** (con la colección completa, muerto para siempre) | Tenerla **no bloquea**: solo avisa. Solo se bloquea con la colección **completa**, sin cobrar bolitas |
| **La pity y la garantía eran por dimensión** (tirar en el Nether empezaba de cero) | Se guardan **siempre en el Overworld**: una sola pity por jugador y mundo (igual en la Cápsula: una waifu caída en el Nether ya aparece en el Overworld) |
| El "consuelo" del 50/50 era **siempre la primera no poseída** (Miyabi, luego Ye...) | Se elige **al azar** entre las que faltan |
| Un 5★ en tirada simple **no decía nada**; el resumen del x10 no cuadraba | Resumen **siempre**, con las waifus que cayeron y la pity; avisa cuando la próxima 5★ está garantizada |
| `owns()` **ignoraba la cápsula** → duplicados al guardar una waifu | Cuenta también las guardadas y las caídas |
| La bolita **en la mano secundaria no contaba** | Cuenta inventario + offhand |

### 🎲 Tirada DOBLE (nuevo)
Cuando sale un 5★ hay un **15 %** (`DOUBLE_CHANCE` en `GachaTerminalItem`) de que caiga **también la
siguiente waifu de la rotación**, con su propio aviso y sonido.

### 🧰 Extra: la cápsula ya no se come el inventario
Con la cápsula abierta no se podía mover **nada** del inventario (el menú se tragaba los clics) y
**invocar podía borrar la waifu** sin crearla. Ahora los clics de los slots del jugador van al
comportamiento normal, y la waifu solo se quita de la cápsula (y los diamantes solo se cobran en el
revive) **si la entidad se ha creado de verdad**.

### 🧪 Verificación
`BUILD SUCCESSFUL` · `validate_v5.py` **11/11** (ahora comprueba también que la textura de la pieza
no esté invertida, celda a celda contra la skin) · desplegado en Prism `1.21.1` y `.minecraft/mods`.

---

## [3.6.0] — 2026-10-06

### 🌈 El sistema de clasificación pasa de elementos a COLORES
Los elementos de ZZZ (Fuego, Éter, Hielo…) eran solo texto en el tooltip: **no hacían nada**. Ahora
cada waifu tiene un **color** sacado de su paleta (traje, pelo o poder) y los colores **sí tienen
mecánica**. El **rol** (Attack/Support/Anomaly/Stun/Defense) se queda como estaba.

| Waifu | Color | | Waifu | Color |
|---|---|---|---|---|
| Tokisaki Kurumi | 🔴 Rojo | | Anby Demara | 🟣 Morado |
| Burnice White | 🟠 Naranja | | Nicole Demara | 🩷 Rosa |
| Astra Yao | 🟡 Amarillo | | Ye Shunguang | 🟤 Marrón |
| Ukinami Yuzuha | 🟢 Verde | | Remielle | ⚫ Negro |
| Hoshimi Miyabi | 🔵 Azul | | Promeia | ⚪ Blanco |
| | | | Ellen Joe | 🩶 Gris |

### ⚔️ La matriz de daño
Cada color hace **+33 % de daño a 3 colores** y **−33 % a otros 3**; los 4 restantes (y él mismo) son
neutros. La rueda es `Rojo → Naranja → Amarillo → Verde → Azul → Morado → Rosa → Marrón → Negro →
Blanco → Gris` y cada color pega fuerte a los que están 4, 5 y 6 pasos por delante.

- **Es regular:** los 11 colores tienen 3 fuertes, 3 débiles y 4 neutros, y cada uno **recibe**
  exactamente 3 bonos y 3 penalizaciones: ningún color es mejor que otro.
- **La media no cambia:** 1.33 y 0.67 promedian 1.00, así que el daño medio del roster sigue igual.
- Con 11 colores es imposible repartir 3+3 sin dejar 4 neutros: es estructural, no un descuido.
- Lo comprueba un validador que lee el Java: `research/colors/validate_colors.py`.

### 👹 Los enemigos salen con color aleatorio (y el daño recibido también cuenta)
- Cada mob hostil recibe **1 de los 11 colores al azar**, derivado de su **UUID**: aleatorio por
  aparición, **estable** al guardar el mundo, sin gastar NBT y sin necesitar sincronización.
- **Se ve de dos formas:** una **etiqueta flotante** con el nombre del color encima del mob (≤24
  bloques) y un **aura de partículas** de su color exacto. Además hay **avisos** en la barra de acción
  al pegar fuerte/flojo y al recibirlo.
- **Tu color es el de tu waifu activa** (la más cercana, radio 32): el color del enemigo también
  modifica **el daño que tú y tus waifus recibís**.
- Comandos: **`/gwcolor`** (color de lo que miras, o el tuyo) y **`/gwcolor list`** (los 11 colores con
  su fuerte y su débil).
- ⚠️ **Detalle técnico importante:** el color **no** se pone como nombre del mob. Un mob con nombre
  propio **nunca desaparece** (`Mob.checkDespawn` no borra mobs con nombre), así que las granjas se
  llenarían de mobs. Por eso el color va como **capa de render**, y el mob sigue siendo anónimo.

### 🎌 Buff de Tokisaki Kurumi
Su Especial hacía **5.0** de daño, la más baja del mod (banda del roster: 8-18). Ahora hace **10.0** y
el stasis dura **100 ticks** (5 s) en vez de 60.

### 🧹 Limpieza
- Los tooltips de los **11 tokens** ya no dicen el elemento: muestran el **color con su hex**, contra
  qué 3 colores pega fuerte (`+33 %`) y contra qué 3 pega flojo (`−33 %`), vía `ColorTooltip`.
- Los `.desc` de los dos idiomas cambian el elemento por el color.

### 🧪 Verificación
- **Build:** `BUILD SUCCESSFUL` · Artifact **`gachawaifus-3.6.0.jar`** (276 KB).
- **`validate_colors.py`:** 11 colores, matriz regular, roster con color y los 2 idiomas completos.
- **`validate_v5.py`:** 11/11 piezas del pecho intactas · **`compare_originals.py`:** 0 píxeles dañados.
- **`um publish check`:** PASS — 518 archivos, 0 fallos, 0 avisos.

---

## [3.5.0] — 2026-10-06

### 🎌 Tokisaki Kurumi — la primera waifu que NO viene de un videojuego
Kurumi (**Date A Live**, anime/light novel) entra al roster como undécima waifu. Como su obra no
tiene kit de juego que copiar, su **set de movimientos se inventó** a partir de su canon (Zafkiel,
el ángel del tiempo) siguiendo la **RAMA B** del meta-prompt `research/prompt`.

| | |
|---|---|
| **Elemento / Rol** | Éter / Anomaly |
| **Rareza** | ★★★★★ |
| **Nametag** | `§9` azul (color nuevo: los 6 clásicos ya estaban ocupados) |
| **Normal** | Shadow Strike |
| **Especial** | Temporal Stasis |
| **Ultimate** | Zafkiel: Clockwork Demon — *"I'll take your time."* |

### 🧩 Hueco que cubre
Es la primera waifu **de anime** del mod: el pipeline de personajes deja de estar atado a ZZZ.

### 👗 Pieza del pecho (sección 3×3)
Kurumi usa el modelo de jugador slim, así que la pieza va **horneada por código** con la capa
`WaifuBustLayers.TUBE_BIG` (la grande), como Astra, Nicole, Remielle y Burnice. Su desplegado vive
en `(0, 121)` de su skin 64×128 y copia el traje real de su pecho (negro/rojo con la ventana del
escote). Su skin original queda guardada en `chicas/tokisaki-kurumi/`.

### 🔧 Arreglos de la IA local y del script del pecho
- **Error de compilación corregido:** el código generado importaba `net.minecraft.world.item.TooltipContext`,
  una clase que **no existe** (ya estaba avisado en la Regla 12 del meta-prompt). Quitando ese `import`
  la build pasa.
- **`research/bust/tube_v5_sizes.py` ahora es idempotente:** al volver a correrlo, el desplegado de las
  6 de GeckoLib se **mudaba** un cuadro (el hueco anterior ya no estaba transparente) y dejaba píxeles
  viejos de basura. Ahora **reutiliza el UV del hueso `breasts`** si ya existe.
- `validate_v5.py` coge **el jar más nuevo** de `build/libs` (antes estaba fijado a 3.3.3 y validaba un
  jar viejo), `compare_originals.py` ya no revienta al dibujar la hoja de diferencias y
  `mockup_tube_v5.py` refleja los tamaños reales (Astra es 3×3 desde hace varias versiones).
- **Clave duplicada en `es_es.json`:** `item.gachawaifus.anby_demara_token` estaba dos veces, así que en
  español el token de Anby mostraba el texto de ayuda como nombre y su descripción no existía. Corregida
  a `.desc`; los dos idiomas quedan con **50 claves idénticas**.
- La invocación de Kurumi usa ya `Component.translatable("message.gachawaifus.tokisaki_kurumi_summoned")`
  (la clave existía en los dos idiomas pero el código la ignoraba con un literal hardcodeado).
- **Meta-prompt (`research/prompt`):** bandas de daño explícitas por habilidad (Especial **8-18**,
  Ultimate **28-45**) medidas sobre el roster, porque la Especial inventada de Kurumi salió con **5.0**
  de daño, la más baja del mod. También: fila de Kurumi en la tabla del roster, colores ocupados al día
  (`§9` pasa a ocupado; libres `§5, §2, §3, §4, §f, §8`), la nota de huecos de Éter corregida a 5, y la
  línea `import net.minecraft.world.item.TooltipContext;` marcada como **prohibida** con un checklist de
  cierre (el prompt generado para Kurumi traía los imports bien: el modelo local se inventó la línea igual).

### 🧪 Verificación
- **Build:** `BUILD SUCCESSFUL` · Artifact **`gachawaifus-3.5.0.jar`** (260 KB).
- **`validate_v5.py`:** 11/11 · 0 choques de UV · todas las celdas pintadas.
- **`compare_originals.py`:** **0 píxeles dañados** en las 11 skins.

---

## [3.4.0] — 2026-10-06

### 🔮 Bolitas de tirada en vez de diamantes
Los tiros ya no se pagan con diamantes: ahora hay dos **bolitas**, dibujadas a partir de la imagen
que mandaste (mismos colores: relleno `#FDA6F2`, banda `#F759C9`, brillo `#FEF9FE`, borde `#120D3B`).

| Item | Qué es |
|---|---|
| **Bolita Rosa — Tirada** | la moneda del Gacha: **1 bolita = 1 tirada** (Shift + clic = x10) |
| **Bolita Azul — Tirada** | reservada para más adelante; de momento no se gasta en nada |

Las dos están en el **menú creativo**, con su modelo y su nombre en español e inglés.

### 🧪 Receta de la Bolita Rosa (en cruz)
```
 . C .        C = lingote de cobre
 L D I        L = lapislázuli
 . O .        D = diamante (centro)
              I = lingote de hierro
              O = carbón
```
El diamante va **en el centro** y el cobre, lapislázuli, hierro y carbón **en las cuatro casillas de
al lado**, en cruz. Da 1 bolita.

### 📅 Tirada diaria
Una **Bolita Rosa gratis cada día** de verdad (no cada día de Minecraft, que son 20 minutos):

- **al entrar al mundo** te la dan sola si no la has reclamado hoy, con su mensajito y su sonido;
- y con el comando **`/tirada`**, por si te quedas conectado y cambia el día sin reconectar.

El día se guarda en los datos persistentes del jugador (sobreviven a la muerte), así que no necesita
ningún archivo extra. Si ya la reclamaste, `/tirada` te lo dice.

### 🔁 Terminal Gacha
El terminal ahora cuenta y gasta **Bolitas Rosas** (antes diamantes) y su mensaje de error explica
cómo conseguirlas. La **revive** de la Cápsula Waifu sigue costando **4 diamantes**, porque ahí el
diamante tiene su gracia.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.4.0.jar`** · JSON de idiomas validados (45 y 46 claves) ·
desplegado en Prism `1.21.1` y `.minecraft/mods`.

---

## [3.3.3] — 2026-10-05

### 🫧 Física, reintentada y ahora sí con el ángulo bueno
La 3.3.1 fallaba porque al aplicar el balanceo se **borraba el giro de 45°** de la pieza
(`setRotX(sway)`). Arreglado: el balanceo ahora **se suma** al ángulo base.

```java
public static final float BASE_TILT = 45.0F * Mth.DEG_TO_RAD;
bone.setRotX(BASE_TILT + sample.swayX() * Mth.DEG_TO_RAD);   // ✅ conserva los 45°
```

Y además se subieron los recorridos, porque en el intento anterior apenas se notaba:

| Canal | Antes | Ahora | Qué lo mueve |
|---|---|---|---|
| `tip` (escala Z) | ±0,42 | **±0,55** | la **velocidad** de caída/salto (no solo la aceleración), el frenazo al aterrizar, el vaivén del paso y la respiración |
| `swell` (escala Y) | ±0,26 | **±0,30** | la caída y el aterrizaje (se aplasta y se ensancha), más el paso |
| `swayX` / `swayZ` | ±5° | **±8°** | la velocidad al avanzar, el frenazo, el meneo del paso y los giros de cabeza/cuerpo |

Lo importante sigue siendo lo de siempre: **solo escala y unos pocos grados**, así que las puntas
de arriba y abajo del rombo siguen apoyadas en la cara del pecho (a 8° se separan menos de 0,2
unidades, y solo en el pico de una caída).

**Para ajustar o quitar** (en `com/gachawaifus/bust/BustPhysics.java`): `TIP_RANGE`, `SWELL_RANGE`,
`SWAY_DEGREES`, los multiplicadores `*_FROM_*`, `STIFFNESS`, `DAMPING`, y **`ENABLED = false`** deja
la pieza completamente quieta.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.3.3.jar`** · `validate_v5.py` 10/10 · desplegado en Prism
`1.21.1` y `.minecraft/mods`.

---

## [3.3.2] — 2026-10-05

### ↩️ Física deshecha: el fallo fue sobrescribir el giro de 45°
La física de la 3.3.1 se quitó porque **no funcionó**: la pieza **no se movía** y además **perdía el
ángulo de 45°** y se veía fuera de su sitio. El motivo era un fallo claro en mi código:

```java
bone.setRotX(sample.swayX());     // ❌ esto BORRA los 45° que trae el hueso del .geo.json
bone.setRotX(45° + sample.swayX());  // ✅ así se conserva el ángulo base
```

El hueso `breasts` trae `rotation: [45, 0, 0]` en el `.geo.json` (y la capa de las de modelo de
jugador su `PartPose.offsetAndRotation(..., 45°, 0, 0)`). Al asignar solo el balanceo, **se
sobrescribía ese giro**: el rombo quedaba como una caja recta, mal colocada, y como el balanceo ronda
cero no se veía movimiento. Lo mismo pasaba en las 4 de modelo de jugador.

El mod vuelve a estar **exactamente como la 3.3.0** (pieza fija con su giro de 45°), revertido con
`git revert` (`f38fdab`). Si se retoma, la regla es: **la física SUMA al giro base, nunca lo
reemplaza** — y si algún día se quiere que la silueta se deforme de verdad (solo la punta, con las
esquinas clavadas en el pecho), el camino es **dibujar la pieza a mano** cada fotograma con sus UVs,
no las transformaciones de hueso.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.3.2.jar`** con **0 clases de físicas** · desplegado en
Prism `1.21.1` y `.minecraft/mods`.
## [3.3.1] — 2026-10-05

### 🫧 Física de la pieza, pero con las puntas pegadas al pecho
Tu idea era la buena: **si las puntas de arriba y abajo se quedan fijas y solo se mueve la punta
del pecho, se ve natural**. Y resulta que se puede, porque el **pivote está justo en la cara
delantera del torso** (z = −2): las puntas del rombo quedan a z = 0 respecto a ese pivote, así que
al **escalar** el hueso se quedan exactamente apoyadas en el pecho.

Antes no salía bien porque la física movía la **posición** del hueso (y eso despegaba la pieza, se
veía flotando). Ahora solo se toca la **escala** y, como mucho, unos pocos grados:

| Canal | Qué hace | Efecto |
|---|---|---|
| `tip` (escala Z) | la punta sale y entra | el pecho respira hacia delante sin despegarse |
| `swell` (escala Y) | se hincha o se aplasta | las puntas resbalan por el pecho pero siguen apoyadas |
| `swayX` / `swayZ` (±5°) | balanceo adelante/atrás y lateral | acompaña el arranque, el freno y los giros |

Se mueve con la **aceleración real** (medida por desplazamiento por tick, que sí funciona en el
cliente, a diferencia de `getDeltaMovement()`), más el ciclo de andar y una respiración muy leve.
Aplica a las **10 waifus**: en las 6 de GeckoLib desde `BustBones.drive(...)` y en las 4 de modelo
de jugador desde `WaifuBustPlayerModel`.

**Para ajustarlo o quitarlo** (todo en `com/gachawaifus/bust/BustPhysics.java`): `TIP_RANGE`,
`SWELL_RANGE`, `SWAY_DEGREES`, `STIFFNESS`, `DAMPING`, y **`ENABLED = false` deja la pieza
completamente quieta**.

El manual estrena una **sección 10** con el truco del pivote, para que ninguna IA vuelva a intentar
la física moviendo la posición.

### 🧪 Verificación
`BUILD SUCCESSFUL` · JAR **`gachawaifus-3.3.1.jar`** (236 KB) · desplegado en Prism `1.21.1` y
`.minecraft/mods`.

---

## [3.3.0] — 2026-10-05

### 💗 Astra Yao, un cuadro más grande
Astra pasa a **sección 3×3** (como Nicole, Remielle y Burnice): su pieza crece un cuadro hacia
arriba, con banda frontal de 8 × 4,24. En las de modelo de jugador eso es cambiar su renderer a
`WaifuBustLayers.TUBE_BIG` y volver a pintar su trozo de skin.

### 🕳️ Nicole: el agujero negro de verdad (kit real de ZZZ)
Se revisó su kit en internet y **el que ya trae el juego no era coherente con el personaje**:

| Real (ZZZ) | Qué es | Cómo estaba en el mod |
|---|---|---|
| Basic Attack "Cunning Combo" | 3 golpes físicos | — (el mod usa un disparo a distancia) |
| Special "Sugarcoated Bullet" | disparo Éter a distancia | estaba como **ataque normal** |
| **EX Special "Stuffed Sugarcoated Bullet"** | **campo de energía que atrae a los enemigos al centro y hace daño Éter por ticks** | una explosión instantánea de 14 💥 |
| Chain "Ether Shellacking" | golpe + campo atractor | — |
| **Ultimate "Ether Grenade"** | **el mismo campo pero mucho más potente** (~1416 % vs ~915 %) + Energía al equipo | una explosión instantánea de 26 💥 |

Ahora, como en el personaje:

- **EX Special** (`blackhole_ex`): abre un **vórtice sobre el objetivo** que **atrae** a los enemigos
  al centro y les hace **2,5 de daño cada 0,5 s durante 6 s** (radio 8), con debilidad encima
  (que aquí representa su rotura de DEF). Cooldown 11 s.
- **Ultimate** (`blackhole_ult`): **el mismo campo, más potente** — radio 12, **4 de daño cada
  0,5 s durante 8 s**, lentitud y el fogonazo final, además de curar y buffear al dueño. Cooldown 32,7 s.
- El campo **se queda actuando** aunque Nicole se mueva o pierda el objetivo, va dibujando un
  vórtice de partículas que **cierra hacia el centro** y empuja a los enemigos cada tick (con
  `hurtMarked` para que el cliente reciba el tirón).

### 🎬 Animaciones nuevas para Nicole
Dos animaciones propias en `nicole_demara.animation.json` (antes la ultimate reutilizaba la del
especial, que es el mismo fallo que tenía Miyabi con la katana):

- `blackhole_ex` (1,2 s): lanza con el maletín-cañón y canaliza con las dos manos al frente, con
  tembleque y el pelo hacia atrás.
- `blackhole_ult` (1,9 s): abre los brazos, remata hacia delante y mantiene el vórtico más tiempo,
  con coletas y pelo volando.

### 📖 Manual
Nueva **sección 8** de `research/manual-pieza-pecho.md`: reglas para que una futura IA haga
texturas de un modelo GeckoLib **sin dañar lo que ya hay** (la skin original de 64×64 es intocable,
lo nuevo va en la mitad de abajo o en zonas transparentes, hay que mirar **todos** los cubos del
modelo antes de pintar, y cómo comprobarlo y repararlo con `compare_originals.py` /
`restore_originals.py`).

### 🧪 Verificación
`BUILD SUCCESSFUL` · `validate_v5.py`: 10/10 · 0 clases de físicas · desplegado en Prism `1.21.1` y
`.minecraft/mods` · **cambios guardados en git** (`23d2e05` en `origin/main`).

---

## [3.2.9] — 2026-10-05

### 🔁 Intercambio de franjas, resuelto (ahora sí)
En el render, la cara que queda **encima** de la arista es la celda **`arriba`** del desplegado y
la de **debajo** es la **`frontal`** — justo al revés de lo que sugiere el nombre, y al revés de lo
que asumí en la 3.2.7/3.2.8. Por eso seguía viéndose invertido. Ahora `arriba` lleva las filas de
encima y `frontal` las que continúan, **sin invertir nada dentro de las celdas**:

| | `arriba` (encima) | `frontal` (debajo) |
|---|---|---|
| Normal (2×2) | filas 23, 24 | filas 24, 25 |
| Grande (3×3) | filas 22, 23, 23 | filas 24, 24, 25 |
| Burnice | filas 23, 24, 24 | filas 25, 25, 26 |

### 🩹 El traje vuelve a ser el original
Tenías razón con lo de las texturas dañadas: comparando cada skin con su **original de `chicas/`**
aparecieron **33 cuadros repintados** en el pecho de seis skins (Burnice, Ellen, Miyabi, Nicole,
Anby y Ye) — la textura de la pieza se había pintado **encima del traje**. Están devueltos a su
arte original con `research/bust/restore_originals.py`, y la pieza se ha vuelto a pintar copiando
ya el arte bueno. Las otras cuatro skins estaban intactas (0 cuadros distintos).

Se ve de un vistazo en `research/bust/out/skins_damage.png` (original | actual antes del arreglo).

### 🧪 Verificación
`BUILD SUCCESSFUL` · `validate_v5.py`: 10/10 con las dos caras visibles distintas y 0 choques de
UV · 0 clases de físicas · respaldo de las skins dañadas en
`research/bust/backup_skins_danadas/` · desplegado en Prism `1.21.1` y `.minecraft/mods`.

---

## [3.2.8] — 2026-10-05

### 🔄 La celda `arriba` se lee al revés: la banda salía invertida
Con la 3.2.7 la banda ya llevaba dos texturas distintas, pero **del revés**: la cara visible de
abajo (la celda `arriba` del desplegado) la lee el render **invertida** — su fila de abajo en la
textura es la que toca la arista. Por eso `arriba`/`abajo` se guardan ahora con las filas
invertidas, y en el juego el pecho continúa hacia abajo:

| | Frontal (encima) | `arriba` guardada | En el juego, de la arista hacia abajo |
|---|---|---|---|
| Normal (2×2) | 23, 24 | 25, 24 | **24, 25** |
| Grande (3×3) | 22, 23, 23 | 25, 24, 24 | **24, 24, 25** |
| Burnice | 23, 24, 24 | 26, 25, 25 | **25, 25, 26** |

Lo confirmé leyendo el mapeo UV exacto de la caja vanilla (`ModelPart.Cube`) y montando un
mini-render en `research/bust/render_box.py` que proyecta la caja con su textura: con la rotación
tal como la aplica el motor, la cara de **arriba** del desplegado es la que queda **abajo** y
delante, y sus filas entran por la arista.

### 🖼️ Burnice: ventana corregida
Tu segunda imagen era la misma zona **una fila más arriba**: la localicé con
`research/bust/find_ref2.py` en **`x 19..28, y 23..26`** (error medio 46,9 frente a 110,8 del
siguiente candidato). Su pieza usa ahora las 8 columnas centrales y las filas **23-26**.

### 🧪 Verificación
`BUILD SUCCESSFUL` · `validate_v5.py`: 10/10 con las dos caras visibles distintas y 0 choques de
UV · 0 clases de físicas · `mockup_tube_v5.py` voltea la celda `arriba` al dibujarla, así que la
previsualización enseña exactamente lo que se verá · desplegado en Prism `1.21.1` y
`.minecraft/mods`.

---

## [3.2.7] — 2026-10-05

### 🩹 La banda salía duplicada: la cara visible de abajo es la de **arriba**, no la de abajo
En el desplegado de la caja, la cara que se ve **por debajo** de la arista es la de **arriba**
(la de `abajo` queda escondida dentro del pecho). Yo la pintaba con las mismas filas que la
frontal, así que en el render las dos franjas de la banda mostraban **la misma textura**: es lo que
se reportó como "pusiste otra vez la cara de arriba". Ahora `arriba` y `abajo` llevan los cuadros
que **continúan** por debajo de la arista y solo la `frontal` los de encima, así que la banda es un
trozo continuo del pecho:

| | Frontal (encima) | La visible de debajo |
|---|---|---|
| Normal (2×2) | filas 23, 24 | filas **24, 25** |
| Grande (3×3) | filas 22, 23, 23 | filas **24, 24, 25** |
| Burnice | filas 24, 25, 25 | filas **26, 26, 27** |

Las filas repetidas van pegadas a la arista (donde se dobla), así que no se notan.

### 🖼️ Burnice: su ventana de textura
Mandaste el trozo exacto de su skin y lo localicé con `research/bust/find_ref2.py`: es
**`x 19..28, y 24..27`** (error de coincidencia 36,7 frente a 109 del siguiente candidato). Su
pieza toma ahora las 8 columnas centrales de ahí (`x 20..27`, filas **24-27**), que es donde está
de verdad el pecho de su traje, en vez de las filas altas que salían más sosas.

### ✅ El validador ahora lo caza
`validate_v5.py` compara las dos caras que se ven y **falla si son idénticas**: ese fallo se coló
porque el validador solo miraba que las celdas estuvieran pintadas, no *qué* llevaban.

### 📖 Manual actualizado
`research/manual-piezo-pecho.md` → [research/manual-pieza-pecho.md](../research/manual-pieza-pecho.md)
explica ya cuál es la cara visible de debajo, el reparto de filas con la tabla real, la tabla
`SOURCE_OVERRIDE` para ventanas a mano, y el fallo nuevo en la lista de "no repetir".

### 🧪 Verificación
`BUILD SUCCESSFUL` · `validate_v5.py`: **10/10** con textura, UV sin choques y las dos caras
visibles distintas · 0 clases de físicas · desplegado en Prism `1.21.1` y `.minecraft/mods`.

---

## [3.2.6] — 2026-10-05

### 📐 Tamaños por waifu: Nicole, Remielle y Burnice un cuadro más grandes
Las cinco que diste por buenas (Anby, Yuzuha, Promeia, Ye Shunguang, Miyabi) se quedan **igual**
(sección 2×2, y su geometría sale exactamente igual que en la 3.2.5). Nicole, Remielle y Burnice
pasan a **sección 3×3**: la pieza crece **un cuadro hacia arriba** (el borde de abajo se queda
donde estaba), así que sigue siendo un rombo simétrico y su textura tapa **4 cuadros** de skin en
vez de 3.

| | Normal (2×2) | Grande (3×3) |
|---|---|---|
| Cubo | 8×2×2 | 8×3×3 |
| Pivote (geo) | `[0, 19.5, −2]` | `[0, 20.21, −2]` |
| Banda de frente | 8 × 2,83 | 8 × 4,24 |
| Sobresale | 1,41 | 2,12 |
| Cuadros que tapa | 3 filas (23-25) | 4 filas (22-25) |
| La llevan | Astra, Yuzuha, Promeia, Anby, Ellen, Miyabi, Ye | **Nicole, Remielle, Burnice** |

En las 4 de modelo de jugador hay ahora dos capas: `TUBE` (normal) y `TUBE_BIG` (Remielle).

### 🩹 Burnice: el parpadeo y la parte izquierda
No era la textura corrupta (lo comprobé: su trozo está entero opaco y no choca con nada). La causa
es **geométrica**: la pieza mide exactamente el ancho del torso, así que sus **caras de los
extremos son coplanarias con las caras laterales del torso** (las dos en x = ±4). Donde se solapan,
el motor dibuja una u otra según el píxel → parpadeo, y en los extremos se copiaba el pecho (no el
lateral), así que el cambio de textura se notaba. **Arreglo:** los extremos pasan a copiar las
**columnas laterales del torso** de la skin (x 17-19 y 28-30), que es justo la textura de la
superficie con la que son coplanarios; el parpadeo deja de notarse. Aplica a las diez.

### 🔍 Bug encontrado de paso: Miyabi compartía textura con su katana
La zona del tubo de Miyabi (27,185) **pisaba la katana** (que usa v 160..186): las dos piezas
compartían trozo de skin. Ahora el script busca hueco libre **teniendo en cuenta todos los cubos
del modelo**, y su pieza vive en un hueco libre de verdad.

### 📖 Manual para las otras IAs
Nuevo **[research/manual-pieza-pecho.md](../research/manual-pieza-pecho.md)**: qué es la pieza, la
regla de oro (1 cuadro de skin = 1 unidad de modelo), cómo se cuentan los cuadros que tapa, el
desplegado UV con sus dos trampas (la cara de abajo va girada 180° y se quedó sin pintar), los
extremos y el parpadeo, dónde vive cada archivo, cómo añadir una waifu nueva o cambiarle el tamaño,
y la tabla de **todos los fallos ya cometidos** con su arreglo.

### 🧪 Verificación
`BUILD SUCCESSFUL` · `validate_v5.py`: **10/10** con su sección, pivote, tamaño, las celdas del
desplegado pintadas y **0 choques de UV** · 0 clases de físicas · desplegado en Prism `1.21.1` y
`.minecraft/mods`.

### 💾 Copias de seguridad
`research/bust/backup_skins_v325/` (las skins de la 3.2.5) y las anteriores (`v324`, `v323`, `v322`,
`v321`).

---

## [3.2.5] — 2026-10-05

### 📏 Ancho del torso entero (8) y textura contada cuadro a cuadro
Fuera los colores fijos: la textura vuelve a salir **de la skin**, del trozo exacto que la pieza
tapa. Las cuentas (`research/bust/count_skin_squares.py`):

- De **izquierda a derecha** el rombo tapa **8 cuadros**: x 20..27, **el torso entero** — así que la
  pieza pasa a medir **8 de ancho** (antes 7, x −4..4), igual que el torso.
- De **arriba abajo** tapa `(alto + fondo) · cos 45 = 4 · 0,7071 =` **2,83 cuadros** centrados en la
  fila 24,5 de la skin → las filas **23, 24 y 25**.
- Como **1 cuadro de skin = 1 unidad de modelo**, la altura aparente (2,83) muestra esas 3 filas a
  escala 1:1: no hay estiramiento, y la textura de la pieza **cuadra con la del torso de al lado**
  (mismas columnas, mismas filas). Ese era el "se ve deforme".

Reparto en los texeles de las dos caras que se ven de frente (2 filas cada una):

| Cara | Filas de skin |
|---|---|
| frontal (arriba del rombo) | 23 y 24 |
| abajo (bajo el rombo) | 24 y 25 (la 24 se repite en la arista, como al doblar tela) |
| extremos oeste/este (2×2) | columnas 20-21 y 26-27 de las mismas filas |
| arriba y trasera | ocultas, rellenas con las mismas filas |

El desplegado pasa a **20×4** (cubo 8×2×2) y la geometría sigue igual en todo lo demás: cuadrado
2×2 girado 45°, sentado sobre la cara delantera del pecho.

### 🧪 Verificación
`BUILD SUCCESSFUL` · `verify_jar_v3.py`: los 6 `.geo.json` con cubo `8×2×2` en `[-4,18.5,-3]`,
pivote `[0,19.5,-2]`, rot `[45,0,0]` y **6/6 caras pintadas en las 10 skins** (16/16 y 4/4) ·
`validate_tubes.py`: 10/10 · 0 clases de físicas.

### 🔍 Comprobación visual
`research/bust/mockup_tube_v4.py` dibuja al lado el torso con su textura real y marca en amarillo
la zona que tapa la pieza: se ve que coinciden las columnas y las filas.

---

## [3.2.4] — 2026-10-05

### 🧼 Textura limpia: el trozo del pecho a la altura correcta
La forma ya era la buena, pero la textura salía fea por **dos motivos concretos**, los dos
medidos antes de tocar nada:

1. **Se tomaba del cuello, no del pecho.** La ventana estaba en las filas 21-24 de la skin, que en
   estas skins es el escote/cuello: en varias se colaba la piel y hasta parecía una cara. La pieza
   está a y 18.5..20.5 del modelo y el torso va de 12 (cintura) a 24 (hombros), así que le
   corresponden las filas **23-26** de la skin. Corregido.
2. **La cara de abajo del desplegado va girada 180°** respecto a la frontal (es así como se pliega
   la caja). Al pintar cada franja con un trozo distinto, las dos que se ven de frente no casaban:
   de ahí el aspecto deforme. Se acabó pintando **el mismo contenido en las seis caras**, así que
   ninguna orientación puede quedar mal.

Ahora, para cada waifu, se busca el **cuadro de 2×2 más plano del pecho** (filas 23-26) y se usa en
todas las caras: si es liso (varianza ≤ 1500, que es el caso de las diez) se repite tal cual — al
ser casi uniforme no se ven costuras — y si una skin tuviera mucho dibujo se usaría su color medio.
El resultado es una pieza de **un tono limpio del traje** de cada personaje:

| Waifu | Tono | Origen |
|---|---|---|
| Astra Yao | blanco crema | (21,24) |
| Ukinami Yuzuha | rosa claro | (24,26) |
| Promeia | azul muy oscuro | (23,26) |
| Remielle | lavanda claro | (21,23) |
| Miyabi | blanco grisáceo | (23,24) |
| Ye Shunguang | dorado | (22,24) |
| Anby Demara | gris oscuro | (22,25) |
| Ellen Joe | gris | (24,23) |
| Nicole Demara | crema rosado | (21,24) |
| Burnice White | rojo oscuro | (20,23) |

La geometría sigue igual (cuadrado 2×2 a 45°, sentado en el pecho) y el desplegado sigue siendo
18×4 con las seis caras pintadas.

### 🔧 Para ajustarlo
Todo está en `research/bust/repaint_tubes_v3.py`: `Y0/Y1` elige de qué filas del pecho se toma
(23-26), `FLAT_LIMIT` decide a partir de qué varianza se usa el color medio en vez de repetir el
cuadro, y `PATCH` el tamaño del cuadro (2 = 2×2). Si prefieres que se vea el estampado del traje en
vez de un tono liso, basta subir `FLAT_LIMIT`.

### 💾 Copias de seguridad
`research/bust/backup_skins_v323/` (textura anterior), `backup_skins_v322/` (5 filas, sección 3×2) y
`backup_skins_v321/` (la que pintó la otra IA).

---

## [3.2.3] — 2026-10-05

### 🔷 Sección cuadrada: el rombo ahora es simétrico y va sentado en el pecho
El problema era la **forma de la sección**. El tubo era un rectángulo de **2 de fondo × 3 de
alto**: girado 45° daba un rombo con los lados desiguales (2, 3, 2, 3) — de ahí el aspecto
"deforme" — y además su centro estaba en z = −3, **una unidad por delante** de la cara del torso,
así que la pieza parecía flotar.

Ahora es un **cuadrado 2×2** girado 45°, con el centro justo **sobre la cara delantera del pecho
(z = −2)**. Medido sobre el modelo:

| | Antes (3×2) | Ahora (2×2) |
|---|---|---|
| Lados del rombo | 2 / 3 / 2 / 3 (desiguales) | **2 / 2 / 2 / 2 (iguales)** |
| Puntas de arriba y abajo | a 0,65 y 1,35 de la cara | **exactamente en la cara del pecho** |
| Banda frontal (ancho × alto) | 7 × 3,54 | **7 × 2,83** |
| Sobresale del pecho | 1,77 | 1,41 |

De frente se sigue viendo un rectángulo de costilla a costilla; de costado, un **rombo simétrico**
apoyado en el pecho. Coincide con el dibujo de referencia: banda frontal de 7 × 2,89 y rombo
simétrico.

### 🧵 Textura de 4 filas: 2 arriba y 2 abajo
Con la sección cuadrada el desplegado UV pasa a **18×4** (antes 18×5), así que la textura son
**4 filas del pecho de la propia skin** (x 20..26, y 21..24): las **2 de arriba** van a la cara
frontal y las **2 de abajo** a la cara de abajo, que son las dos que se ven de frente. La banda
frontal queda en **dos franjas iguales de 2 filas**, como en el ejemplo. Los extremos toman las
columnas laterales de esa misma ventana. Las seis caras siguen pintadas en las diez skins.

### 🧪 Verificación
`BUILD SUCCESSFUL` · `verify_jar_v3.py`: los 6 `.geo.json` con `pivot [0,19.5,-2]`, `rot [45,0,0]`,
cubo `7×2×2` en `[-3.5,18.5,-3]` y **6/6 caras pintadas en las 10 skins** · `validate_tubes.py`:
10/10 · 0 clases de físicas en el jar.

### 💾 Copias de seguridad
`research/bust/backup_skins_v322/`: las skins con la textura de 5 filas (sección 3×2).
`research/bust/backup_skins_v321/`: las de la versión anterior a esa.

---

## [3.2.2] — 2026-10-05

### 🩹 La cara que faltaba: por eso de frente se veía a medias
El desplegado UV de una caja de 7×3×2 tiene **seis caras**, y solo estaban pintadas cinco: la de
**abajo** (columnas 9..15 de las dos primeras filas) estaba **transparente en las diez skins**. Con
el tubo girado 45°, esa cara es justo una de las **dos que se ven de frente**, así que de frente se
veía una cara con textura y la otra vacía. Ahora las seis caras están pintadas en las diez.

### 🎨 Y con la textura del pecho (la de los pechos planos)
Cada cara del tubo se pinta con **el mismo trozo de pecho de la skin** que usaba la versión de
pechos planos: se toma su parche de 3×3 como ancla y se extiende a una ventana de 7×3 (dos píxeles
a cada lado) para cubrir la cara frontal; las caras de arriba/abajo salen de las dos primeras filas
de esa ventana y los dos extremos de sus columnas laterales. Así el tubo se ve con el traje de cada
personaje, sin colores planos inventados:

| Cara | Zona del pecho |
|---|---|
| frontal y trasera (7×3) | la ventana completa |
| arriba y abajo (7×2) | las dos primeras filas |
| extremos oeste/este (2×3) | las columnas laterales |

La geometría **no se tocó**: el tubo sigue a 45°, de costilla a costilla (7 de ancho sobre un torso
de 8) y pegado a la cara delantera del pecho (z −4..−2), que es lo que ya estaba bien.

### 🧪 Verificación
`BUILD SUCCESSFUL` · `verify_faces_jar.py`: **6/6 caras pintadas en las 10 skins (0 incompletas)** ·
`validate_tubes.py`: 10/10 OK (el validador ahora comprueba las 82 celdas que usa una caja, no 68:
antes se me colaba la cara de abajo, que era justo el fallo).

### 💾 Copia de seguridad
Las skins anteriores (con la textura que había pintado la otra IA) están en
`research/bust/backup_skins_v321/` por si prefieres volver a aquella versión de la textura.

---

## [3.2.1] — 2026-10-05

### 💗 Terminado el tubo del pecho en las 10 waifus
La idea que quedó a medias (una sola pieza, un **tubo rotado 45° pegado al pecho de costilla a
costilla en la parte delantera**) ya está en todas. Se revisó cómo se había implementado y se
completó con el mismo criterio en las que faltaban:

- **El tubo:** un cubo de **7×3×2**, atravesado de costilla a costilla (x −3.5..3.5; el torso mide
  8 de ancho), en la cara delantera del pecho (z −4..−2, pegada al torso que empieza en z −2),
  a la altura del pecho (y 18..21) y **rotado 45° sobre su eje largo**. El pivote es el propio
  centro del tubo, así que gira sobre sí mismo.
- **Las 6 de GeckoLib** (Miyabi, Ye, Anby, Ellen, Nicole, Burnice): hueso `breasts` colgado de
  `body` en el `.geo.json`. **Ellen y Miyabi se quedaron con la versión vieja** (un cubo 3×3×2 con
  la rotación en Z y el UV del traje): ya se pasaron a la misma que las otras cuatro, apuntando a
  la zona de textura que ya tenían pintada.
- **Las 4 de modelo de jugador** (Astra, Yuzuha, Promeia, Remielle) no tenían nada hecho:
  estrenan `WaifuBustLayers`, que hornea el mismo tubo sobre una **copia propia** de la malla de
  jugador slim (no se toca la capa compartida: si se mutara, todos los jugadores tendrían tubo).
  Sus skins pasan a **64×128** (formato doble: la mitad de arriba es la skin de siempre, la de
  abajo queda libre) y llevan el desplegado UV del tubo pintado en `(0, 121)`, igual que
  Anby/Ellen/Nicole. Por eso la capa declara alto 128 y no 64.

### 🎨 La textura del tubo es de cada personaje
Cada skin lleva pintado un trozo de **18×5** al final (el desplegado UV de un cubo 7×3×2) con el
color del traje de esa waifu: cara de arriba clara, laterales medios, cara frontal con el estampado
y la sombra, y trasera oscura — la misma estructura que ya usaban las seis que estaban hechas.
En las cuatro nuevas la cara frontal copia **los píxeles reales del pecho de su propia skin**, así
que hereda el traje sin inventar arte.

### ❌ Físicas fuera
Se eliminó todo el sistema de resortes que movía el pecho (`BustPhysics`, `BustState`, `BustSpec`,
el conductor del hueso y el modelo con física): el tubo es geometría fija con su rotación de 45°.
Los renderers de las cuatro de modelo de jugador vuelven al `PlayerModel` normal.

### 🎯 Alcance ampliado de las waifus a distancia (se mantiene del 3.2.0)
| Waifu | Antes | Ahora |
|---|---|---|
| Astra Yao — Ether Blast | 16 bloques | **30** |
| Astra — Vocal Solo / ultimate | 8 / 12 | **10 / 14** |
| Nicole — Sugarcoated Bullet | 16 | **30** |
| Nicole — Ether Grenade | 14 (centrada en ella) | **20, centrada en el objetivo** |
| Nicole — Black Hole | 22 (centrada en ella) | **24, centrado en el objetivo** |
| Burnice — lanzallamas | 12 | **20** |
| Burnice — ultimate aérea | 12 | **16** |
| Rango de seguimiento (las tres) | 32 (Nicole sin atributo) | **48** |

### 🧪 Verificación
`BUILD SUCCESSFUL` · los 6 `.geo.json` con el tubo idéntico (rotación [45,0,0], un cubo 7×3×2),
las 10 skins con su zona pintada y el JAR sin ninguna clase de físicas.

### ⚠️ Si el tubo se ve inclinado al revés
En las cuatro de modelo de jugador la inclinación se aplica con `PartPose.offsetAndRotation(..., 45°)`
en `WaifuBustLayers`: la silueta de un tubo girado +45° y −45° es la misma, pero cambia cuál de las
caras (la de arriba o la de abajo) asoma junto a la frontal. Si en el juego se ve al revés respecto a
las seis de GeckoLib, es cambiar ese `TILT_DEGREES` por `-45.0F`.

---

## [3.1.5] — 2026-10-04

### ⚔️ Miyabi — dirección de la hoja, resuelta midiendo (no adivinando)
Me diste la pista clave: *"es dirección opuesta a la funda"*. Así que medí la geometría con la nueva
herramienta `tools/inspect_geo.py`:

| Pieza | Extensión en Z | Dirección |
|---|---|---|
| **Saya** (funda) | Z de 3 a 29 | hacia **atrás** |
| **Hoja dibujada** | Z negativo | hacia **adelante** ⇒ ya era la opuesta |

El problema real es que **en la ronda 4 la giré 180° hacia atrás**, quedando en la MISMA dirección que
la saya cuando debía ser la contraria. Revertido y fijado con una convención explícita y documentada:

- **pitch 0°** → hoja hacia **adelante** (desenvainada).
- **pitch +90°** → hoja hacia **atrás-abajo** = misma dirección que la saya (guardada/oculta).
- Reposo del hueso en el `.geo.json`: `rotation [90,0,0]` + `position [0,-200,0]`.
- Los 18 keyframes de las poses visibles volvieron a la orientación hacia adelante (~0°), y los de
  "guardada" pasaron a +90°.

### 🔥 Burnice — la ultimate ahora extiende los brazos
- **Los brazos de `flame_rain` estaban a 42°** (casi pegados al cuerpo), por eso no se veía la lluvia
  de fuego extendida. Ahora van a **62-72° con más apertura** (roll ±46° a ±52°), que es el gesto de
  rociar fuego con los dos tubos por delante.
- Además se añadió movimiento de barrido entre keyframes (62 → 72 → 62 → 70) en vez de un valor fijo.

### 🐛 Bug encontrado revisando: `flame_rest` no existía
El controlador de Burnice referenciaba `animation.burnice_white.flame_rest` para el cierre de la
ultimate, pero **esa animación nunca se creó** (la ronda 4 la había referenciado sin definirla).
GeckoLib habría intentado reproducir una animación inexistente. Creada: 0.5 s que baja los brazos del
frente a la postura neutra antes de devolver el control al reposo.

### 🧰 Herramienta nueva: `tools/inspect_geo.py`
Imprime, hueso por hueso, la extensión real (X/Y/Z) de cada cubo de un `.geo.json`, con la leyenda
`Z negativo = DELANTE / Z positivo = DETRÁS`. Sirve para saber hacia dónde apunta un arma o si una pieza
sobresale, **sin abrir Minecraft**. Es lo que permitió confirmar la dirección de la saya y la hoja.

### Verificación
- `tools/validate_geo.py` → TODO CORRECTO (6/6).
- Animaciones: Miyabi 8, Burnice **9** (con `flame_rest`).
- `BUILD SUCCESSFUL` · **`gachawaifus-3.1.5.jar`** (222,830 bytes), 0 `.bak` dentro · desplegado.

---

## [3.1.4] — 2026-10-04

### ⚔️ Miyabi — la saya ahora cubre toda la hoja
- **Saya alargada de 19 a 24 px** con una segunda abrazadera intermedia y contera recolocada, para que
  no se vea el interior de la funda por ningún lado. Antes la vaina era más corta que la hoja y se veía
  el "tubo hueco".
- **La katana guardada se oculta mucho más lejos** (offset 80 → 200 px hacia abajo): antes, con la hoja
  de 17 px, el extremo asomaba por debajo de la pierna y parecía que la espada estaba fuera de la funda.

### 🔥 Burnice — tubos desde el codo y mochila pegada a la espalda
- **Tubos dorados acortados de 20 a 7 px** y recolocados para que arranquen **desde el codo** (y 18)
  y bajen hasta la muñeca, en vez de recorrer todo el brazo.
- **Mochila pegada al cuerpo:** los cilindros estaban en `z 2.0–8.0` (flotando detrás). Ahora viven en
  `z 0.2–3.2`, apoyados en la espalda. También se acortaron los cilindros (13 → 10 px) y las tuberías.

### 🐛 Corregido — animaciones que se quedaban pegadas (Burnice y Miyabi)
**Síntoma reportado:** "puede pasar que se bugee la animación y sus brazos estén hacia atrás al caminar
o al atacar con la ultimate y EX".

**Causa raíz:** el controlador de ataques devolvía siempre `PlayState.STOP` y las animaciones estaban
declaradas como *animación disparada* (no en bucle). Cuando una de esas animaciones termina, GeckoLib
**deja los huesos en el último fotograma**: los brazos se quedaban al frente (pose del lanzallamas) o
abiertos (pose del giro) indefinidamente, porque ningún otro controlador reescribía esos huesos.

**Solución:** animaciones de **reposo** que devuelven todos los huesos a su rotación por defecto del modelo.
- Nuevas `animation.burnice_white.rest` (13 huesos) y `animation.miyabi.rest` (10 huesos).
- El predicado del controlador ahora reproduce el reposo **cuando no hay habilidad activa**, así los brazos
  vuelven solos al terminar la EX y la ultimate.
- Los huesos de arma (`katana`, `katana_drawn`) conservan su rotación de reposo del modelo, así que el
  reposo **tampoco** saca la espada de la funda.
- La lluvia de fuego de la ultimate tiene ahora su propia pose de cierre (`flame_rest`) antes de volver al reposo.
- Aplicado a **Miyabi y Burnice**; es el mismo patrón que conviene replicar en las otras 4 GeckoLib.

---

## [3.1.3] — 2026-10-04

### ⚔️ Miyabi — ajustes de la katana (según prueba en juego)
- 🗡️ **La saya vuelve al diseño alargado:** se restauró la pieza larga (boca + abrazadera + cordón
  + cuerpo de 19 px + contera) porque la versión corta no se leía como funda. Ahora cuelga del costado
  izquierdo con caída natural, en laca negra con herrajes dorados.
- ✋ **El agarre ya está EN la mano:** el pivote del hueso `katana_drawn` se movió al centro del puño
  (-6, 13, -0.25). Antes el pivote estaba detrás, así que la hoja salía por detrás de la mano y parecía
  empuñada al revés. Ahora al rotar de -90° a 0° el puño queda dentro del puño cerrado y la hoja apunta
  hacia adelante.
- ⬛ **Empuñadura NEGRA** (con rombos más oscuros), como pediste; la tsuba sigue dorada.
- 🙈 **Guardada = invisible de verdad:** la katana desenvainada se oculta desplazándola 80 px hacia abajo
  (antes 60, y con la hoja más larga asomaba). El `idle` no toca el hueso, así que hereda la posición de
  reposo del geo.
- 🖼️ **Textura ampliada a 64×192** para tener banda limpia donde pintar saya + katana sin pisar la skin.
  Se validó que ninguna UV se sale de la textura.

### 🔥 Burnice — mochila rediseñada + tubos dorados
- 🎒 **Nueva mochila según referencia:** **dos cilindros** verticales plateados (con tapas doradas,
  abrazadera e indicador de combustible) y un **cuadrado central negro con emblema dorado** entre ambos,
  más dos tuberías que bajan hacia los brazos. Antes era una caja con dos cubos que no se parecía.
- 🟡 **Un tubo dorado por brazo**, pegado al lateral exterior y subiendo hasta por encima del hombro
  (20 px de alto) con una boquilla dorada en la punta. Sustituyen a los "sopletes" de muñeca: ahora son
  parte fija del brazo, así que siguen el movimiento del brazo sin necesidad de animarlos.
- 🖼️ **Textura ampliada a 64×192** con las UVs nuevas; validación de UVs incluida.
- Un solo hueso por brazo (`left_torch` / `right_torch`), sin tracks de animación: rotan con el brazo.

### 🧰 Herramientas
- Nuevo **`tools/validate_geo.py`**: comprueba para las 6 waifus con GeckoLib que ninguna UV se salga de
  la textura declarada en el `.geo.json` y que el PNG en disco coincida de tamaño. Detecta de un golpe los
  errores de UV que antes solo se veían en juego.
- Los backups `.bak` ya no se empaquetan dentro del JAR (se guardan en `tools/backups/`).

---

## [3.1.2] — 2026-10-04

### 🆕 Remielle — nueva waifu (Éter / Anomaly) + ajustes del pipeline
- **Remielle** (`remielle`, ★5 Éter/Anomaly): "Rainbow's End" (golpe de 8, `ENCHANTED_HIT`),
  "Ode to Dawn" (15 + `WEAKNESS`) y "Dazzling Curtain Call" (40 en 8 bloques + `REGENERATION` al dueño).
  Nametag `§6`, HP 115, Speed 0.32, ATK 11, Armor 8.
- Registrada en `ModEntities`, `ModItems`, `GachaWaifusMod`, tab creativo, `WaifuRoster.ROTATION`
  (así el gacha puede entregarla y la Cápsula puede guardarla/revivirla) y lang `es_es`/`en_us`.
- **Solucionados 3 fallos del código generado por la IA local** (ver más abajo: el prompt ya los previene):
  1. `\n` **literales** en vez de saltos de línea reales en `GachaWaifusMod.java` (4 veces) y
     `ModCreativeTabs.java` (1 vez) → el Java no compilaba.
  2. **Import inventado** `net.minecraft.world.item.TooltipContext` en `RemielleTokenItem.java`.
  3. Textura de entidad y medallón faltantes (el prompt no los genera): creados con
     `tools/skin_processor.py` → `remielle.png` (64×64) y `remielle_token.png` (32×32).

### 🔥 Burnice White — lanzallamas real (mochila de combustible + sopletes de brazo)
Antes: la especial era un **dash** con un ramillete de partículas y la ultimate un AoE instantáneo.
No existía ningún lanzallamas pese a ser su identidad.
- **Modelo 3D:** nueva pieza `fuel_pack` (dos tanques plateados con tapas doradas, placa central y
  quemador superior) anclada al cuerpo, y **dos sopletes** (`left_torch`/`right_torch`) pivoteados en las
  muñecas para que sigan el movimiento de los brazos. UVs pintadas en la franja libre de su textura.
- **Especial (Jet Flamethrower) — canalizada:** Burnice se queda **plantada en el sitio** 3 segundos y
  escupe fuego en **cono de 12 bloques** desde ambos brazos. Daño cada 6 ticks (3 de fuego) + ignición.
  Ya no hace dash. Nueva animación `flame_channel` (3.2 s) con retroceso en las coletas.
- **Ultimate (Burnice Supernova) — aérea:** salta con gravedad desactivada, queda **suspendida en el aire**
  2.5 segundos y descarga la lluvia de fuego hacia abajo en cono (daño cada 8 ticks, 4 de fuego), luego cae.
  Animaciones nuevas `leap` (0.6 s) y `flame_rain` (2.4 s en bucle).
- Título en pantalla al dueño (`🔥 ¡Jet Flamethrower!` / `🔥 ¡BURNICE SUPERNOVA!`).

### ⚔️ Miyabi — la katana sale de la funda
Antes: la "katana" era una pieza pegada al brazo izquierdo que parecía, como dijo el usuario, **una funda**.
- **La pieza vieja ahora ES la saya** (vaina): recolocada colgando del costado izquierdo, con caída natural.
- **Nueva katana desenvainada** (`katana_drawn`) anclada a la **mano derecha**, con hoja de 17 px,
  tsuba dorada y tsuka con rombos. Por defecto está **guardada (oculta)**; se desenvaina al atacar.
- **Todas las animaciones la desenvainan y la envainan:** `attack` (tajo cruzado, 0.45 s), `special`
  (desenvaine en el dash + corte), y la ultimate completa (`ultimate_charge` la saca con ambas manos,
  `ultimate_slash` la extiende en el giro de 360°, `ultimate_recover` hace el *chiburi* y la envaina).
- `idle` y `walk` la mantienen guardada para que Miyabi camine con las manos libres.
- UVs nuevas pintadas en la franja inferior vacía de `miyabi.png` (saya, hoja con hamon, tsuba y tsuka).

### 📚 `research/prompt` — dos reglas nuevas y mejor investigación de movimiento
- **Regla 11:** prohibido escribir `\n` literal en el código (el fallo que rompió Remielle).
- **Regla 12/13:** lista cerrada de imports y firma exacta de `appendHoverText` (el import inventado `TooltipContext`).
- **Regla 14:** nada de bloques markdown con ``` ni la palabra `java` antes del `package`.
- **Investigación de movimiento (Sección A, punto 3):** la IA con internet ahora debe describir postura en
  reposo, cómo ataca, si canaliza/queda quieta, la ultimate como **secuencia de 2-4 pasos con duraciones**,
  qué arma lleva y dónde, si hay desenvaine, y qué piezas se mueven aparte (coletas, cola, capa, orejas).
  Esa descripción entra al prompt como bloque `MOVIMIENTO Y ANIMACIONES` y alimenta la fase 3D.
- Nuevo arquetipo **CANALIZADO** para waifus que se quedan quietas escupiendo un efecto.

### 🧹 Herramientas
- Scripts de análisis UV y parcheo de geometría/textura usados para Burnice y Miyabi; backups `.bak`
  conservados de ambas texturas y geometrías.

---

## [3.1.1] — 2026-10-04

### ❄️ Miyabi — la Ultimate ahora es una secuencia cinemática real
**Antes:** la ultimate *Cherry Blossom Frost* disparaba `triggerAnim("attack_controller", "special")`,
o sea **la misma animación que la habilidad especial**, y todo el efecto era un golpe instantáneo
sin aviso. El momento más caro del kit (700 ticks de cooldown) se veía igual que el especial.

- 🎬 **3 animaciones nuevas** en `miyabi.animation.json`:
  - `ultimate_charge` (1.2 s): postura **iaido** — se agacha, gira el torso, lleva la katana sobre el
    hombro en alto, orejas de kitsune hacia atrás. La animación **se queda en la pose final** hasta que
    llega el impacto.
  - `ultimate_slash` (0.55 s): desenvaine con **giro de 360°** del cuerpo y la cabeza, katana extendida,
    y el cuerpo se eleva (`root` +0.5).
  - `ultimate_recover` (0.75 s): cae de rodillas, katana baja, orejas caídas y vuelta a la postura normal.
  - Se encadenan en un único disparador `ultimate` (`thenPlay` × 3).
- ⚠️ **Telégrafo en el suelo:** durante los 20 ticks de carga se dibuja un **círculo de 10 bloques** con
  `SNOWFLAKE` + `END_ROD` que rota lentamente, más un aura que **crece** sobre Miyabi. El jugador ahora
  ve dónde va a caer el golpe.
- 💥 **Impacto con onda de choque:** 3 anillos expansivos de `FLASH` + `EXPLOSION`, flash central,
  y fanfarria (`GENERIC_EXPLODE`, `GLASS_BREAK`, `LIGHTNING_BOLT_THUNDER`).
- 🌊 **Daño escalonado (se siente un vórtice, no un botón):** golpe inicial de 19 con **caída por
  distancia** (100% en el centro → 40% en el borde) + **marca de escarcha** de 36 ticks que aplica
  3 ticks de daño residual de 4 y vuelve a aplicar `MOVEMENT_SLOWDOWN`.
- 🎯 **Título en pantalla** para el dueño: `❄ CHERRY BLOSSOM FROST ❄`.
- 🛑 Durante la secuencia Miyabi **no se mueve ni ataca** (`ultimatePhase != 0` corta el `aiStep`),
  y queda invulnerable 70 ticks para que el telégrafo no se desincronice del golpe.
- ✨ **Mejoras de animaciones existentes:** `attack` ahora tiene **anticipación** (keyframe a 0.05 s con
  la katana atrás en vez de arrancar en reposo) y `special` se inclina hacia adelante porque es un dash.

### 📚 `research/prompt` — selección de personaje simplificada
- Nueva sección **"CANDIDATAS YA ESTUDIADAS"** con los huecos reales del roster (★1 Zhu Yuan, ★2 Qingyi,
  alternativas Caesar King y Jane Doe) y la instrucción de editar **solo 2 campos** para elegir personaje.

---

## [3.1.0] — 2026-10-04

### ⚰️ Rediseño total de la Resurrección — fin del bug "siempre te da Astra Yao"
- ❌ **Eliminado el Núcleo Durmiente** (`dormant_waifu_core`) y sus 3 recetas de revivir.
  Eran recetas **ambiguas**: las tres usaban exactamente el mismo patrón (4 diamantes + 1 núcleo)
  con distinto resultado, así que el crafteo resolvía siempre a la primera cargada → **token de Astra Yao**.
  Además solo existían para 3 de las 9 waifus, dejando el resto sin forma de revivir.
- ✅ **Al morir, el alma de la waifu se resguarda en la Cápsula Waifu** (server-side, por jugador).
  El juego ahora **sabe qué waifu cayó**, porque la identidad se guarda en datos del mundo y no en un item anónimo.
- ✅ **Revivir desde la GUI de la cápsula:** nueva fila dedicada a las waifus caídas (marcadas `[CAÍDA]`).
  Clic en una entrada = revivirla con **4 diamantes** del inventario (mismo coste que el crafteo anterior).
  Aparece viva con la mitad de la vida, partículas de tótem y mensaje de confirmación.
- ✅ Las waifus vivas guardadas se muestran como `[VIVA]` y se invocan con un clic, como antes.
  El menú pasó a 5 filas (3 vivas · 1 separador · 1 caídas) sobre la textura de cofre doble vanilla.
- 🛡️ **Recuperable:** la colección y las caídas viven en `gachawaifus_waifu_storage`, así que perder la
  cápsula no borra nada — al craftear otra, todo vuelve.

### 🎰 Gacha desacoplado del roster
- El consuelo del 50/50 perdido ya **no está fijado a Nicole Demara** (era un placeholder): ahora entrega
  cualquier waifu de la rotación que **aún no poseas**. Al crecer el roster, las tiradas siguen aportando
  personajes nuevos en vez de repetir siempre a la misma.
- Nueva utilidad `WaifuRoster.anyUnowned(player)`.

### 📚 Documentación y pipeline de IA
- `research/prompt` reescrito con la arquitectura real: obliga a registrar la waifu en `WaifuRoster`,
  documenta la variante sin GeckoLib, los rangos numéricos correctos y las reglas de import/nametag.
- Nuevo `research/promptejemplo-promeia.txt`: ejemplo verificado de los 3 archivos + los 8 puntos de
  integración, para que la IA local **copie y renombre** en lugar de improvisar (ahorra tokens y errores).

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
