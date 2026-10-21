# INSTRUCCIÓN OBLIGATORIA PARA LA IA LOCAL
> **LEER DESDE LA LÍNEA 1:** Debes leer íntegramente este archivo `agents.md` antes de realizar cualquier acción. Este archivo contiene las reglas críticas del proyecto y la API que se está utilizando.

# Instrucciones para agentes: mod Gacha Waifus

Esta carpeta **es la raíz del mod** (`gachawaifus`): aquí vive el código Java, los recursos y el
build. Si tu directorio de trabajo es esta carpeta, usa rutas como `src/main/java/...` y
`src/main/resources/...` **sin** prefijo.

## Build e instalación

```powershell
.\gradlew.bat build
```

- Salida: `build/libs/gachawaifus-<version>.jar`.
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

> El arte y las texturas PNG los aporta el humano (nunca los generes). Aquí solo se escribe código
> y, si acaso, los JSON de modelos/lang.

## Cómo trabajar aquí (evita los errores habituales)

1.  **Rutas absolutas** siempre que salgas de esta carpeta; dentro de ella, relativas a esta raíz.
2.  **Lectura y Edición Precisa:** Antes de usar `edit_file_tool`, DEBES leer el archivo con `read_file_lines` para asegurar la coincidencia exacta. El texto en `replace_from` debe ser idéntico (incluyendo espacios, tabulaciones y saltos de línea LF). Si hay riesgo de error "no match found", usa `replace_file` reescribiendo el archivo completo.
3.  **Parámetros de Herramientas:** Asegúrate siempre de incluir todos los parámetros requeridos en las llamadas a herramientas (por ejemplo, `start_line` es obligatorio en `read_file_lines`).
4.  **Una tarea = un fichero o un cambio concreto.** Los refactors de 10+ ficheros de golpe acaban en
    líos; ve paso a paso y compila entre pasos.
5.  **Para una waifu nueva, copia un patrón existente** (sin GeckoLib; eso es una fase manual
    posterior). El renderer tiene 4 variantes ya en uso en el mod — elige la que te indiquen:
    - `WaifuBustPlayerModel` con `WaifuBustLayers.TUBE` (2.0F) o `TUBE_BIG` (3.0F): waifus con
      pieza del pecho (skin 64×128). Ejemplos: Promeia/Yuzuha (TUBE), Rina/Astra (TUBE_BIG).
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
    - Compilar con `.\gradlew.bat build` y pegar la salida literal.
7.  **Nada de emojis fuera del rango que dibuja Minecraft** (los ✨/★ se usan ya en el mod; los
    emojis tipo 🔥 salen como cuadro vacío).

### ¿Se corta a mitad? (se queda sin tokens de salida)

- Si la respuesta llega incompleta, **NO compactes**: pide SOLO lo que falte en un mensaje nuevo.
- No re-executes el chat desde cero en el mismo hilo a medio borrar. El resumen a pegar:

  ```
  Contexto: estoy añadiendo la waifu <nombre> al mod (carpeta gachawaifus).
  Ya tengo los puntos <lista de los que has aplicado>. Genera SOLO el resto (puntos <lista>),
  con la misma estructura del prompt autoconcluso de esta waifu.
  No compiles.
  ```