# BURNICE WHITE — estado real (condensado del MODLOG + CHANGELOG, verificado en código)

Fuentes: `MODLOG.md` (raíz del proyecto), `gachawaifus/CHANGELOG.md`, código actual de la rama `multiloader` @ `b03fedc` (mod 7.6.4). Nada inferido sin evidencia; lo no confirmado está al final.

## Habilidades y cómo están implementadas hoy
`common/src/main/java/com/gachawaifus/entity/BurniceWhiteEntity.java` (599 líneas) — `extends AbstractWaifuEntity implements GeoEntity`.
- El **kit** (daños/partículas/sonidos) vive en `performNormalAttack` / `performSpecialSkill` / `performUltimate`, métodos `private void` sin `@Override` llamados desde su propio `aiStep()`. `AbstractWaifuEntity` no tiene `aiStep`.
- `aiStep()`: decrementa `normalAttackCooldown` (20 t), `specialSkillCooldown` (220 t) y `ultimateCooldown` (700 t); `invulnerableTicks` aparte. Prioridades por distancia: ultimate ≤16 bl → especial ≤20 bl (`FLAME_RANGE`) → normal ≤4 bl. Estados plantados (`flameChanneling`, `ultimatePhase`, `recoveryTicks`) cortan navegación y devuelven antes de atacar.
- `ultimatePhase`: 1 = salto (`setNoGravity`, sube 0.34/t, 12 t) → 2 = lluvia de fuego aérea 50 t (`AIR_RAIN_LENGTH`) → `beginRecovery()`. El especial es **canalizado 60 t plantada** (`CHANNEL_LENGTH`) con `sprayConeFlames()`/`burnConeTargets()` en cono de ~35° (`FLAME_COS=0.82`).
- **No usa `triggerAnim`** (a diferencia de Miyabi/Miku/Teto). El clip lo decide `actionClip()` desde `ACTION`, una `EntityDataAccessor<Integer>` sincronizada (0 idle, 1 flame_channel, 2 leap, 3 flame_rain, 4 recovery, 5 attack): así servidor y cliente ven el mismo clip. `attack_controller` pregunta `actionClip()` y devuelve STOP si es `null`; `movement` se congela mientras `isPerformingAction()`.
- `pendingStrikeTicks`/`STRIKE_DELAY=4`: el daño del tajo se aplica 4 ticks después de lanzar la animación, no al instante.
- `resetActionState()` (sentada/muerta/carga de chunk) + `addAdditionalSaveData` guardando `BurniceAerialAbility`; sin eso quedaba flotando e invulnerable para siempre (bug de 6.4.0).
- `BurniceWhiteModel.setCustomAnimations()` (114 líneas): gira cabeza (`head`, peso 0.7) y torso (**`root`**, peso 0.15, tope ±35°) hacia el objetivo; se apaga si el clip de habilidad ya keya `head`, comprobación leída del animation.json (`keysBone`), no hardcodeada. También `BustBones.drive` y `WaifuGeoSitPose`. `BurniceWhiteRenderer` (17 líneas) = `GeoEntityRenderer` + `shadowRadius 0.5` + `shouldShowName→true`.

## Animaciones actuales (`burnice_white.animation.json`, format_version 1.8.0, 11 clips)
idle 2.4 l (coletas+brazos) · walk 0.48 l (piernas/brazos/coletas, `rx = clamp(ground_speed*4,0,1)*±57.3/±49.6/±28.6`) · run 0.6 l (añadido 6.6.0) · attack 0.4 (right_arm, body, coletas) · special 0.8 (brazos al frente) · flame_channel 3.2 · leap 0.6 · flame_rain 2.4 l · flame_rest 0.5 l · rest 0.5 l (13 huesos) · recovery 0.4 (añadido 6.6.0).
Huesos del geo (`burnice_white.geo.json`, format 1.12.0, 15 huesos, textura 64×192): root → body → {breasts, fuel_pack, head→{left/right_twintail→_tip}, right_arm→right_torch, left_arm→left_torch}; piernas (`left/right_leg`) cuelgan de **root**, no de body.

## Bugs reportados, intento de arreglo y estado
1. **Brazos congelados al terminar EX/ultimate** (reportado 2026-10-04 tras 3.1.3: «puede pasar que se buguee la animación y sus brazos estén hacia atrás al caminar o al atacar con la ultimate y EX»). Causa diagnosticada en **3.1.4**: `attack_controller` devolvía siempre STOP y los clips *triggerable* dejan los huesos en su último fotograma. Arreglo: clip `rest` de 13 huesos generado con `tools/_gen_rest_anims.py`. **El reporte original nunca se cerró como resuelto.**
2. **No caminaba** (3.2.9 → 3.6.1): el fallback `rest` se aplica después de `movement` y pisaba el `walk`. Arreglo 3.6.1: `if (state.isMoving()) return PlayState.STOP`.
3. **De pie quedaba congelada / sin respirar** (**7.4.0**): se eliminó el fallback `rest` del predicado → STOP puro (20 de 22 waifus ya lo hacían). Los clips `rest` y `flame_rest` siguen en el JSON.
4. **Cabeza en molinetes** (**7.3.0**): el modelo *sumaba* rotación cada fotograma.
5. **Torso girando contra las piernas** (**7.4.0**): el giro se escribía en `body`; movido a `root`.
6. Parpadeo del pecho (3.2.6): era coplanaridad geométrica, no textura. Sonidos: `sounds.json5` era archivo muerto (3.8.0), migrado a `sounds.json`; los 3 `.ogg` de `sounds/entity/burnice/` **siguen sin existir** (anotado en 6.1.1). Atributos sin registrar → crash (5.3.0). Invulnerable al recargar chunk (6.4.0).
- **Pivotes**: `tools/fix_geo_pivots.py --check` señala **4 pivotes en Burnice** dentro de la mochila de 28 en 13 geos (7.6.2). **No se tocaron** («el resto están bien», dijo el humano), así que el check global no sale en 0.

## Hipótesis nueva para el bug de brazos (evidencia, sin probar en juego)
Con la convención corregida en **7.6.4** (bytecode de GeckoLib: el geo se carga espejado en X, `rx>0` lleva la punta de una pieza colgante hacia la **espalda**), Burnice quedó inconsistente: `flame_rain` mantiene los brazos en **rx +62..+72** y `walk` usa ±57.3, mientras `recovery` (-72), `special` (-70) y los clips de las demás waifus usan rx **negativo** para levantar/brazos al frente. Es decir, la pose que se pretendía "hacia adelante" se renderiza hacia atrás. La corrección de 7.6.4 se aplicó solo a Miku, Roxy y Alice; **Burnice no fue tocada**.

## Archivos involucrados
- `common/src/main/java/com/gachawaifus/entity/BurniceWhiteEntity.java`
- `common/src/main/java/com/gachawaifus/entity/client/BurniceWhiteModel.java` · `BurniceWhiteRenderer.java`
- `common/src/main/resources/assets/gachawaifus/geo/burnice_white.geo.json` (630 líneas) · `animations/burnice_white.animation.json` (2092) · `textures/entity/burnice_white.png` (64×192, 5130 B)
- `common/src/main/java/com/gachawaifus/item/BurniceWhiteTokenItem.java`; registro en `common/.../registry/ModEntities.java`, `WaifuRoster.java`, `WaifuAttributeBindings.java`, `client/ModClientBindings.java`, `color/WaifuColors.java` (naranja); repositorio de fuente Blockbench: `modelos/personajes/burnice_white/` y `chicas/burnice/`
- `tools/fix_geo_pivots.py`, `tools/validate_geo.py`, `tools/_gen_rest_anims.py`

## Sistema VFX (Rama vfx/burnice-test — Noviembre 2026)
- **Origen en brazos/sopletes:** En `VfxBones.java` el hueso objetivo de la cadena ya no resta su propio pivote, por lo que `offsetLocal` se aplica exactamente desde el pivote del soplete (`Y=20` en el geo), situando la salida del chorro de fuego/plasma en la punta de la boquilla (`Y=11.5`) y no en los pies.
- **Auto-apuntado de la Ultimate:** `performUltimate(LivingEntity target)` bloquea el objetivo al saltar. `tickUltimate()` ejecuta `updateFlameAim()` cada tick tanto en la fase de salto como en la fase aérea, permitiendo pitch libre hasta `[-85°, +85°]`. Tanto el daño (`burnConeTargets`) como las partículas van dirigidas en 3D directo al enemigo.
- **Paleta de fuego cálida:** Restaurado el degradado de fuego cálido de 5 paradas: blanco núcleo (`#ffffff`) → amarillo brillante (`#fff299`) → naranja cálido (`#ff7e14`) → rojo brasa (`#c41818`) → humo oscuro (`#2b080c`).
- **Prototipo 3D con cubos de plasma:** Se añadió renderizado de partículas como cubos tridimensionales en `VfxRenderer.java` (`texture: "cube"` con `cube.png`), que rotan, se estiran en 3D en la dirección de la velocidad y reciben sombreado direccional por cara. Accesible mediante el stage `channel_3d` en `/gwvfx waifu burnice_white/special#channel_3d`.


