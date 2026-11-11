# INSTRUCCIÓN OBLIGATORIA PARA LA IA LOCAL
> **LEER DESDE LA LÍNEA 1:** Debes leer íntegramente este archivo `agents.md` antes de realizar cualquier acción. Este archivo contiene las reglas críticas del proyecto y la API que se está utilizando.

# Instrucciones para agentes: mod Gacha Waifus

Esta carpeta **es la raíz del mod** (`gachawaifus`): aquí vive el código Java, los recursos y el
build multi-loader (NeoForge + Fabric):
- `common/src/main/java/...` y `common/src/main/resources/...`: código neutral y recursos compartidos (entidades, tokens, renderers, modelos, animaciones y lang van **aquí**).
- `neoforge/src/main/java/...`: registros de NeoForge (`ModEntities`, `ModItems`, `ModCreativeTabs`, `GachaWaifusMod`).
- `fabric/src/main/java/...`: adaptadores de Fabric (gestionados por la auditora con `tools/fabric_sync.py`).

## Build e instalación

```powershell
# Solo NeoForge (el jar habitual de pruebas)
.\gradlew.bat :neoforge:build

# Compilar todo (NeoForge + Fabric)
.\gradlew.bat build
```

- Salida NeoForge: `neoforge/build/libs/gachawaifus-<version>.jar`.
- Salida Fabric: `fabric/build/libs/gachawaifus-fabric-<version>-fabric.1.jar`.
- Para probarlo: copiar ese jar a `%APPDATA%\.minecraft\mods\` (renombrando el anterior a
  `*.jar.bak`, que es la costumbre del proyecto).

## Modelo de trabajo (IA Autónoma)

La IA tiene capacidad para **implementar directamente** las waifus en el código fuente. No se limita a generar texto; su objetivo es dejar el mod funcional tras aplicar los cambios.

1.  **Implementación Directa:** La IA debe usar herramientas (`edit_file_tool`, `replace_file`, `create_folder`) para crear archivos nuevos y modificar los existentes.
2.  **Lectura Previa Obligatoria:** Antes de editar cualquier fichero existente, DEBES leer su contenido actual con `read_file_lines` o similar para asegurar que las ediciones (`edit_file_tool`) sean precisas y no rompan la estructura del archivo.
3.  **Integración Completa:** Para una waifu nueva, la IA debe ejecutar un flujo de trabajo que cubra los 12 puntos esenciales (entidad, token, renderer, modelos, registros, colores, lang, etc.).

## Reglas de oro (API de NeoForge 1.21.1 — incumplirlas rompe el build)

1.  **La entidad SIEMPRE extiende `AbstractWaifuEntity`.** Nunca `TamableAnimal` directo. Nunca
    sobreescribas `die()`, `hurt()`, `isFood()` ni `wantsToAttack()`: ya están resueltos en la base
    (al morir la waifu cae y la Cápsula Waifu la registra con 4 diamantes).
2.  **El token usa `useOn()` con `.create(level)`.** Jamás `use()` con `create(serverLevel, MobSpawnType)`.
3.  **Filtro AoE obligatorio:** `e -> e != this && e != owner && !(e instanceof AbstractWaifuEntity)`.
4.  **Nada de métodos, sonidos, partículas o efectos inventados.** Solo `SoundEvents`, `ParticleTypes`
    y `MobEffects` que existan de verdad en 1.21.1 (los del prompt que te pasen).
5.  **Nunca escribas `import net.minecraft.world.item.TooltipContext;`** — esa clase no existe en ese
    paquete. En `appendHoverText` se usa `TooltipContext` SIN import (es anidado de `Item`).
6.  **Cada waifu se registra en `WaifuRoster` (entrada + `ROTATION`), `WaifuColors` y ambos `lang`.**
    Si falta en `ROTATION`: el gacha no la entrega, la Cápsula no la guarda y no se puede revivir.
7.  **La clasificación es el COLOR, no el elemento.** Prohibido "Fuego", "Hielo", "Éter", etc. La
    matriz de daño (±33 %) ya la aplica el mod solo desde `color/ColorEvents.java`.
8.  **El gacha no se toca.** `GachaTerminalItem` es genérico sobre `WaifuRoster.ROTATION`.
9.  **Cada método se define UNA sola vez.** Nunca repitas `registerGoals()`, `aiStep()` ni ningún
    otro método dentro del mismo fichero: duplicarlo rompe la compilación al instante.
10. **Los ataques son métodos `private void` SIN `@Override`** (`performNormalAttack`,
    `performSpecialSkill`, `performUltimate`): no son hooks de la base; los llama el `aiStep()`
    cuando su cooldown llega a 0.
11. **Firma EXACTA del token:** `public InteractionResult useOn(UseOnContext context)` (import
    `net.minecraft.world.item.context.UseOnContext`). Dentro: spawn con
    `ModEntities.X.get().create(level)` (nunca `new XEntity(...)`), `entity.tame(player)`,
    `level.addFreshEntity(entity)` (nunca `level().addEntity`), `context.getItemInHand().shrink(1)`
    si no es creativo y `return InteractionResult.CONSUME`.
12. **Si un símbolo no está en el patrón, NO existe.** Copia `GraceHowardEntity.java` /
    `GraceHowardTokenItem.java` y cambia solo lo del prompt. Inventado y PROHIBIDO (el 2026-10-09
    obligó a rehacer 8 ficheros): `EntityType.EntityType.of`, `addFreshParticles`,
    `level().explode`, `DamageSource.meleeImpact/explosion`, `InteractionParameters`,
    `ticksExisted` (es `tickCount`), `new ResourceLocation(...)` (es
    `ResourceLocation.fromNamespaceAndPath(...)`), colores en inglés tipo `WaifuColor.YELLOW`
    (los 11 constantes son en español: ROJO NARANJA AMARILLO VERDE AZUL MORADO ROSA MARRON NEGRO
    BLANCO GRIS).
13. **Nunca llames `setOrderedToSit(...)` ni toques el sentarse vanilla.** Desde la 6.4.0 el sentarse
    ES un modo (`WaifuMode.SENTADA`): se cambia con `toggleSitFromPet()` o `setWaifuMode(...)`. Si
    sientas a una waifu por libre, el NBT sigue diciendo `AGRESIVO` y al recargar el chunk
    `applyWaifuMode()` la levanta sola (pasó en Astra y Nicole).
14. **Si sobreescribes `mobInteract`, termina SIEMPRE con `return super.mobInteract(player, hand);`**
    y solo actúas cuando `!player.isShiftKeyDown()` y la mano NO está vacía. La Baliza de Llamado
    (`WaifuBeaconItem`, solo teletransporte) y el panel de modos (`WaifuModeMenu`, Shift + clic con la
    mano vacía) viven en la base: un `mobInteract` que devuelva `SUCCESS` en cualquier otro caso se los
    carga. El **hogar** ya no se fija con un item: lo pone el bloque `WaifuHomeBlock` al colocarse.

> El arte y las texturas PNG los aporta el humano (nunca los generes). Aquí solo se escribe código
> y, si acaso, los JSON de modelos/lang.

## Cómo trabajar aquí (evita los errores habituales)

1.  **Rutas absolutas** siempre que salgas de esta carpeta; dentro de ella, relativas a esta raíz.
2.  **Lectura y Edición Precisa:** Antes de usar `edit_file_tool`, DEBES leer el archivo con `read_file_lines` para asegurar la coincidencia exacta. El texto en `replace_from` debe ser idéntico (incluyendo espacios, tabulaciones y saltos de línea LF). Si hay riesgo de error "no match found", usa `replace_file` reescribiendo el archivo completo.
3.  **Parámetros de Herramientas:** Asegúrate siempre de incluir todos los parámetros requeridos en las llamadas a herramientas (por ejemplo, `start_line` es obligatorio en `read_file_lines`).
4.  **Una tarea = un fichero o un cambio concreto.** Los refactors de 10+ ficheros de golpe acaban en
    líos; ve paso a paso y compila entre pasos.
5.  **Para una waifu nueva, copia un patrón existente.** El renderer tiene 5 variantes ya en uso en el
    mod — elige la que te indiquen (si el prompt no dice, es de modelo de jugador):
    - **GeckoLib** (`GeoEntity` + `GeoEntityRenderer` + `GeoModel`): **22 waifus** así, y las nuevas si
      el humano aporta el modelo de Blockbench. Copia `NormaEntity` / `NormaModel` / `NormaRenderer`
      **tal cual**: la entidad añade `implements GeoEntity`, el campo
      `GeckoLibUtil.createInstanceCache(this)`, `registerControllers` con los dos controladores
      `"movement"` (blend 5, `walk`/`idle` `thenLoop`) y `"attack_controller"` (blend 2,
      `PlayState.STOP`), y `this.triggerAnim("attack_controller", "attack"|"special"|"ultimate")` como
      **primera línea** de cada habilidad. ⛔ **Tú NO escribes `geo/` ni `animations/`**: los .geo.json
      y los .animation.json los exporta el humano de Blockbench y los coloca la auditora; tú solo das
      el Java. Si el prompt te pide "el geo", di que no y sigue con los puntos Java. Quien sí edite
      geos o animaciones (auditoría, humano): `../research/pivotes.md` (ejes, tabla de signos de la
      rotación base) y gate `python3 tools/fix_geo_pivots.py --root <ese geo> --check`.
    - `WaifuBustPlayerModel` con `WaifuBustLayers.TUBE` (2.0F) o `TUBE_BIG` (3.0F): waifus con
      pieza del pecho (skin 64×128). Ejemplos: Yuzuha (TUBE), Astra/Remielle (TUBE_BIG).
      Promeia usa la capa propia `PROMEIA`, que es TUBE más la cola de caballo y `PromeiaModel`.
      (Rina y Qingyi **ya no** están aquí: migraron a GeckoLib en la 6.5.0.)
    - `PlayerModel(PLAYER_SLIM, true)`: waifus femeninas sin pieza (skin 64×64). Ej: Grace, Koleda.
    - `PlayerModel(PLAYER, false)` (modelo ANCHO, de Steve): personajes masculinos o corpulentos,
      sin pieza. Ej: Von Lycaon, Soldier 11. NO se apuntan en `tube_v5_sizes.py` ni `validate_v5.py`.
    - Modelo propio (`EntityModel` de partes): solo si existe ya la clase. Ej: Anby, Ellen, Miyabi.
6.  **Checklist de una waifu nueva** (los 12 puntos; si falta uno no funciona):
    - Entidad (`entity/<Nombre>Entity.java`) + token (`item/<Nombre>TokenItem.java`).
    - Renderer (`entity/client/<Nombre>Renderer.java`) y texturas (las genera el humano).
    - Modelo del item `models/item/<id>_token.json`.
    - `ModEntities.java` (registro del EntityType) y `ModItems.java` (registro del token).
    - `GachaWaifusMod.java`: atributos **y** renderer (2 líneas, con los imports arriba).
    - `ModCreativeTabs.java`: `output.accept(ModItems.<ID>_TOKEN.get());`
    - `WaifuRoster.java`: `Entry` con id/entidad/token/nombre **y** añadirla a `ROTATION`.
    - `WaifuColors.java`: `put("<id>", WaifuColor.COLOR);` (color de los 11 existentes).
    - Token: `ColorTooltip.append(tooltipComponents, WaifuColor.COLOR);` en `appendHoverText`.
    - `lang/en_us.json` **y** `lang/es_es.json`: claves `entity.gachawaifus.<id>`,
      `item.gachawaifus.<id>_token`, `item.gachawaifus.<id>_token.desc` (con el color en el desc)
      y `message.gachawaifus.<id>_summoned` (el mensaje que el token enseña al invocarla).
    - Nametag `setCustomName(...)` con un color LIBRE. Ocupados hoy: §a §b §c §d §e §6 §9 §3 §f §7 §8
      §1 §2. Libres: §0 §4 §5.
    - Compilar con `.\gradlew.bat :neoforge:build` y pegar la salida literal.
7.  **Nada de emojis fuera del rango que dibuja Minecraft** (los ✨/★ se usan ya en el mod; los
    emojis tipo 🔥 salen como cuadro vacío).
8.  **Antes de construir algo, busca la ruta más corta — y avisa al humano si hay una más fácil.**
    Si un encargo se puede resolver reutilizando lo que ya existe (un hook nativo de Minecraft, una
    opción de config, copiar el estado de un objeto válido) en vez de crear menús, pantallas o
    máquinas nuevas, **no te lances por la complicada**: dile primero
    *"hay una forma más fácil: X; la otra es Y"* con el coste de cada una y **deja que él decida**.
    Solo sigue por la difícil si te lo pide. Ejemplo real (2026-10-29): para camuflar el
    `WaifuHomeBlock` se hizo primero un menú con selector de bloques (`HomeMimicMenu` +
    `HomeMimicScreen`) cuando bastaba con clic-derecho llevando el bloque en la mano (`useItemOn`) y
    copiar su textura; funcionó, pero era mucho más código del necesario. En 6.8.0 se borró el menú
    y se dejó el gesto simple.

### ¿Se corta a mitad? (se queda sin tokens de salida)

- Si la respuesta llega incompleta, **NO compactes**: pide SOLO lo que falte en un mensaje nuevo.
- No re-executes el chat desde cero en el mismo hilo a medio borrar. El resumen a pegar:

  ```
  Contexto: estoy añadiendo la waifu <nombre> al mod (carpeta gachawaifus).
  Ya tengo los puntos <lista de los que has aplicado>. Genera SOLO el resto (puntos <lista>),
  con la misma estructura del prompt autoconcluso de esta waifu.
  No compiles.
  ```

## VFX (efectos visuales de habilidades) — añadir a `gachawaifus/agents.md`

> **Estado del motor: FASE 0 — el motor aún NO existe.** No llames a ninguna API de VFX desde Java ni
> sustituyas `sendParticles` hasta que `agents-bus/STATE.md` diga **FASE 1 completa**. Mientras tanto
> solo puedes tocar lo que una tarea del bus te permita explícitamente.

### Qué es
Los efectos son datos: un JSON por habilidad con *stages* (carga, impacto, residual...). Cada stage
es una lista de capas (emisores, anillos, tajos, cintas, brillo, sacudida, destello). El motor (Java,
solo lo toca Claude) los dibuja en el cliente. **Los efectos no leen `WaifuColor` ni el sistema de
colores/roles**: la paleta de cada efecto va escrita en su JSON.

### Dónde vive
- Efectos: `common/src/main/resources/assets/gachawaifus/vfx/<waifu>/<habilidad>.json`
  (el `id` interno es `<waifu>/<habilidad>`, igual que la ruta sin `.json`).
- Disparo: cada stage lleva `"trigger": {"clip": "animation.<waifu>.<clip>", "at": 0.0}`: el motor lo
  arranca cuando el controlador de ataque empieza ese clip. **Los VFX no se escriben dentro de los
  `.animation.json`, `.geo.json` ni PNG**: esos archivos los regenera el humano desde el paquete de
  edición (Blockbench) y se perderían. Si el humano renombra un clip o un hueso, `validate_vfx.py`
  lo detecta (cruza `trigger.clip` con `animations/<waifu>.animation.json` y `bone:<x>`/`bone` con
  `geo/<waifu>.geo.json`). Ejemplo canónico: `vfx/miyabi/ultimate.json`.
- Efectos decididos por el servidor (impactos sobre enemigos, AoE) y waifus que no son GeoEntity:
  paquete `VfxPacket` (**no existe hasta la FASE 1**); esos stages van sin `trigger`.

### Reglas
1. **Copia el ejemplo canónico** y cámbialo; no inventes claves. El validador rechaza cualquier clave,
   textura, forma o ancla que no esté en las listas cerradas:
   - type: `emitter | ring_wave | telegraph | glow | slash_arc | ribbon | shake | flash`
   - texturas: `soft_glow spark streak petal shard mote smoke ring_thin ring_soft arc_streak ribbon_soft star4`
   - blend: `additive | alpha` · shape: `point disc ring cone line sphere`
   - anchor: `entity | ground | target | bone:<hueso>` (el hueso debe existir en el `.geo.json`)
   - color: degradado `[[0,"#rrggbb",alfa], ..., [1,"#rrggbb",alfa]]` (t ascendente, empieza en 0, acaba en 1)
2. **Presupuesto:** ≤ 12 capas por stage, ≤ 600 partículas pico por capa, ≤ 1200 por stage. Prefiere
   pocas partículas grandes y suaves antes que muchas pequeñas.
3. **Los tiempos salen del clip**, no de los ticks de Java: un stage empieza en su `trigger` y su
   `duration` debe cubrir ese clip (mira `animation_length`).
4. **Al migrar `sendParticles`** (solo en FASE 1+ y cuando exista el stage equivalente): quita únicamente
   las llamadas a partículas; conserva daño, sonidos, efectos de poción y cooldowns. Nunca reescribas
   el archivo entero: edita bloques concretos.
5. **No toques** `common/**/vfx/engine/**`, los loaders (`neoforge/`, `fabric/`), `platform-files.txt`,
   ni los assets de arte (`geo/`, `animations/`, `textures/`) ni `paquete-edicion/`.
6. Valida siempre antes de dar la tarea por terminada:
   `python3 tools/validate_vfx.py && python3 tools/fabric_sync.py --check`
   (y el build si tocaste Java). Pega la salida real, no la inventes.

### Fallos típicos a evitar (heredados del historial del mod)
Claves o texturas inventadas · reescribir un JSON/Java entero y perder el final · `id` que no coincide
con la ruta · hueso o clip que no existe (o que el humano renombró) · lanzar `export_editable_pack.py`
(borra `paquete-edicion/` y puede perder ediciones sin devolver).

---

## Directivas para IAs externas y asistentes de código (Cursor, Claude, Copilot, Windsurf)

Si eres un asistente de IA ayudando a un colaborador a modificar este repositorio:

### 1. Estructuras, Biomas y Spawns (NO programar en Java)
- **NO modifiques `WildWaifuSpawner.java`** para añadir o quitar waifus de biomas: el sistema es Data-Driven.
- Para cambiar qué waifu o boss sale en un bioma, usa los tags JSON en `common/src/main/resources/data/gachawaifus/tags/worldgen/biome/`:
  - Climas: `climate/frio.json`, `climate/calido.json`, `climate/templado.json`, `climate/nether.json`, `climate/end.json`.
  - Exclusiones de biomas: `no_wild_spawns.json`.
  - Exclusiones de estructuras: `tags/worldgen/structure/no_wild_spawns.json`.
  - Por waifu: `whitelist/<id>.json`, `blacklist/<id>.json`, `spawn_extra/<id>.json`.
- Para estructuras, añade los `.nbt` a `data/gachawaifus/structures/` y configura los JSONs en `data/gachawaifus/worldgen/`.
- Ejecuta siempre: `python tools/validate_collab_data.py` tras modificar JSONs.

### 2. Si modificas código Java (Reglas estrictas de sincronización)
- **Frontera obligatoria:** `common/` nunca importa `net.neoforged` ni `net.fabricmc`.
- **Espejo NeoForge <-> Fabric:** Cada archivo modificado o añadido en `neoforge/src/main/java/...` DEBE tener su equivalente exacto en `fabric/src/main/java/...` y figurar en `fabric/platform-files.txt`.
- **Validación obligatoria:** Antes de dar cualquier tarea de código por terminada, corre:
  ```bash
  python tools/fabric_sync.py --check
  ```
- **Salud y atributos (Cero crashes):**
  - Si añades una nueva entidad: registra sus atributos en `registerAttributes` tanto en `neoforge` como en `fabric`. Sin esto, el servidor crasheará inmediatamente al intentar spawnearla.
  - Asegura que cualquier método de spawn o doma ejecute `waifu.setHealth(waifu.getMaxHealth())`.

