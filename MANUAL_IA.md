# MANUAL IA — Gacha Waifus (NeoForge 1.21.1)

Manual operativo para sesiones de IA futuras. Leer completo antes de tocar nada.
Complementa (no reemplaza) a `agents.md`. Última actualización: 5.5.0 (2026-10-20).

## 0. Dónde vive todo

- Raíz del proyecto: `/run/media/juan/5A66A7D866A7B2E5/super proyecto de moding gachas minecraft/gachawaifus`
- Modelos de Blockbench del usuario: `…/super proyecto de moding gachas minecraft/modelos/personajes/<id>/`
- Skins de jefes crudas: `…/bosses/<nombre>/` (usar **tal cual**, sin retocar, como textura de entidad)
- JDK: `…/tools/jdk-21-linux` · Java 21 · NeoForge 21.1.233 · GeckoLib 4.9.3 · modid `gachawaifus`
- Paquetes Java: entidad `com.gachawaifus.entity`, renderers/modelos `entity.client`, items `item`,
  registros `registry`, clientes de item GeckoLib `client`.

## 1. Compilar, desplegar, probar

```bash
cd "/run/media/juan/5A66A7D866A7B2E5/super proyecto de moding gachas minecraft/gachawaifus"
env JAVA_HOME="…/tools/jdk-21-linux" ./gradlew build \
  -Dorg.gradle.java.home="…/tools/jdk-21-linux" --no-daemon -q
```
- NUNCA pipear la salida por `tail` (trunca los errores): redirigir a un log y `grep "error:"`.
- En fish no usar `set var (…)` dentro de comandos en background; rutas literales completas.
- El jar sale en `build/libs/gachawaifus-<version>.jar`. Desplegar (y **borrar el jar de la versión
  anterior** para no duplicar):
  `cp build/libs/*.jar ~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/1.21.1-neoforge/minecraft/mods/`
- Lanzar: `flatpak run org.prismlauncher.PrismLauncher --launch "1.21.1-neoforge"` (background).
  Log: `<instancia>/minecraft/logs/latest.log`; esperar línea `Reloading ResourceManager`, luego
  grep de `error|exception` descartando benignos.
- Warnings benignos conocidos (NO intentar arreglar): `supernova.ogg` de Burnice, `pyrois_token`,
  refmap de geckolib, OpenAL al apagar, `yggdrasil public key`/skins en offline.

## 2. Subir de versión

1. `gradle.properties` → `mod_version=X.Y.Z`.
2. `CHANGELOG.md`: entrada nueva arriba `## [X.Y.Z] — fecha`, en español, estilo existente.
3. Build → los dos comandos de despliegue del §1.

## 3. Agregar una waifu GeckoLib (patrón actual, 16 ya migradas en 6.3.0)

Checklist (los 12 puntos de `agents.md` adaptados a GeckoLib):

1. **Geo**: copiar el `.geo.json` de Blockbench a `src/main/resources/assets/gachawaifus/geo/<id>.geo.json`.
   Revisar pivotes absurdos (los de Blockbench a veces caen en el origen `[0,0,0]`, o sea los pies):
   **basta con editar el `pivot`** y ponerlo en la junta anatómica. Los `origin` de los cubos y los
   `pivot` de los huesos son ambos coordenadas **absolutas** del modelo (comprobado contra `koleda`:
   `head` pivot `[0,24,0]` con cubos en `y 24..32`), así que NO hay que desplazar los cubos.
   Solo se desplaza `cubo.pivot` cuando ese cubo trae su propia `rotation`.
   (Error real: la fórmula `cubo' = cubo − c` que estaba aquí habría hundido la malla 24 px.)
2. **Textura**: `textures/entity/<id>.png`.
3. **Animaciones**: `animations/<id>.animation.json` con `"format_version": "1.8.0"` y set completo
   `idle / walk / attack / special / ultimate` basado en los **huesos reales del geo**.
   - Nombres de hueso deben coincidir EXACTO, incluidos typos del geo (`coelta izquierda`,
     `oreja drecha`…). No animar nunca `root` ni `breasts`.
   - Rotaciones como mapas `{time: [x,y,z]}`. Si tiene coletas/cola/orejas, animarlas en TODAS las
     animaciones (balanceo con medio ciclo de desfase entre lados).
4. **Entidad** (`entity/<Nombre>Entity.java`): extiende `AbstractWaifuEntity`, implementa
   `GeoEntity` con `AnimatableInstanceCache` (helper real de GeckoLib 4.9.3:
   `GeckoLibUtil.createInstanceCache(this)`),
   `getAnimationStore`/`registerControllers`: controlador `"movement"` (walk/idle) +
   `"attack_controller"` con `TriggerableAnim` para attack/special/ultimate.
   `triggerAnim(...)` como PRIMERA línea de cada método de habilidad.
5. **Modelo** (`entity/client/<Nombre>Model.java`): `GeoModel<>` con rutas `geo/<id>.geo.json`,
   `animations/<id>.animation.json`, `textures/entity/<id>.png`; aplicar `BustBones.drive` y
   `WaifuGeoSitPose.apply` si el personaje usa los poses del proyecto.
6. **Renderer** (`entity/client/<Nombre>Renderer.java`): `GeoEntityRenderer<>`, shadowRadius 0.5F.
7. **Registros**: `ModEntities` (sized + `clientTrackingRange(12)`), atributos y renderer en
   `GachaWaifusMod` (`put(...)`/`registerEntityRenderer`), token en `ModItems`, spawn egg si aplica.
8. **Lang**: `es_es.json` y `en_us.json` con `entity.gachawaifus.<id>`, `item.gachawaifus.<id>_token`,
   `.desc`, etc. (traducir SIEMPRE los dos idiomas).
9. Cooldowns de habilidades anclados a la duración real de las animaciones (referencia de las
   migradas: 24/220/750 o 22/260/750 ticks para attack/special/ultimate).

## 4. Agregar un jefe (patrón Tung Tung Sahur / Fernanfloo / House)

- `extends Monster`, **neutral**: `targetSelector` solo `HurtByTargetGoal`.
- `hurt()`: bloquear creative/spectator, `PLAYER_DAMAGE_MULTIPLIER = 0.2F`, aviso actionbar
  `message.gachawaifus.<id>_resists` con cooldown de 80 ticks (`complainTo`).
- Recompensa: campo `rewardPlayer` (UUID) + `rememberRewardOwner` en `hurt()` + `die()` →
  `handOutBalls` con `resolveWinner` (dueño de waifu → asesino → más cercano en 32 bloques),
  `ModItems.BLUE_BALL`/`PINK_BALL`, mensaje `_defeated` con `%s`, `PLAYER_LEVELUP` pitch 1.4F,
  partículas `END_ROD` (40) en la muerte. `xpReward` 60–70, `setPersistenceRequired()`,
  nombre con código de color (`§6`, `§2`) y `setCustomNameVisible(true)`.
- Tamaño/visual: `sized(1.6F, 4.2F)`, renderer `BossHumanoidRenderer` (PlayerModel ancho + nametag
  lejano) con override `scale(T, PoseStack, float)` → `poseStack.scale(2.0F, 2.0F, 2.0F)`.
- Skins: copia del png crudo de `bosses/<jefe>/` sin retocar.
- spawn egg en `ModItems` (`SpawnEggItem(entity, colorBase, colorPatches, props)`) +
  `models/item/<id>_spawn_egg.json` con `{"parent": "minecraft:item/template_spawn_egg"}`.
- **LA PESTAÑA CREATIVA ES MANUAL**: registrar el item NO basta; hay que `output.accept(...)` en
  `registry/ModCreativeTabs.java` (bloques: tokens → jefes → utilidades). Olvidarlo = invisible en
  creativa (error real de la 5.4.0).

## 5. Arma en mano GeckoLib (patrón bate/antiguo chorizo)

- `item/<Arma>Item.java` implements `GeoItem<...>` + `DefaultedItemGeoModel<...>` (modelo estático).
- `client/<Arma>Model.java`: `DefaultedItemGeoModel<>` con rutas `geo/item/<id>.geo.json`,
  `animations/item/<id>.animation.json` (objeto `animations` puede ir `{}`), `textures/item/<id>.png`.
- `client/<Arma>Client.java`: `@EventBusSubscriber(bus = MOD)` + `RegisterClientExtensionsEvent` →
  `new GeoItemRenderer<>(modelo).withScale(...)`. OJO con la escala: 0.0625 = 1px→1/16 bloque.
- `models/item/<id>.json` parent `minecraft:item/handheld`.
- El renderer del mob necesita `ItemInHandLayer` (los Curlys/Fernanfloo ya no; Tung Tung sí).
- Textura con **box UV** (plantilla Blockbench para cube (u,v,w,h,d): top `(u+d,v)` w×d,
  bottom `(u+d+w,v)`, fila laterales `v+d`: left `(u,v+d)` d×h, front `(u+d,v+d)` w×h,
  right `(u+d+w,v+d)` d×h, back `(u+2d+w,v+d)` w×h). La textura debe ser `texture_width/height`
  del geo (64×64); una textura recortada chafada = item "plano/manchas" (error real de la 5.4.0).
- **PENDIENTE (tarea abierta)**: rediseñar el bate `beisbol_bate` — el usuario lo ve mal; revisar
  orientación/pivote en mano y proporciones. Es 3D real (4 cubos: pomo, grip con cinta, cono,
  barril) a escala 0.125; probar con `withPosition/withRotation` de `GeoItemRenderer`.

## 6. Minions/mascotas (patrón Curly y Wilson)

- Invocador: campos `MAX_MINIONS_ALIVE` (2), `MAX_MINIONS_SUMMONED` (6, anti-granja),
  `SUMMON_COOLDOWN_TICKS` (200), radio 2.5; `List<UUID> minions`; `summonMinions` con búsqueda de
  posición libre (`noCollision`, 4 intentos angulares), partículas `POOF`, sonido característico
  (Fernanfloo: su grito `ModSounds.FERNANFLOO_ATTACK` — NO borrar los sonidos viejos al cambiar el
  arma), `dismissMinions()` en `die()`.
- minion: sincroniza `setTarget(maestro.getTarget())` cada 20 ticks; si el maestro no existe
  100 ticks → `POOF` + `discard()`.
- **Perros**: lobo vanilla SIN domar. `setTame(true)` sin dueño = sentado y pasivo (error real de
  la 5.4.0→5.5.0). Overrides: `registerGoals` propio (Float 1, MeleeAttack 1.25 vel 2, stroll 6,
  look 7/8, `HurtByTargetGoal`), y copiar `Wolf.createAttributes()` en `createAttributes()`.
  Renderer: `MobRenderer<..., WolfModel<...>>` con `new WolfModel<>(ctx.bakeLayer(ModelLayers.WOLF))`
  (un solo arg) y textura vanilla `textures/entity/wolf/wolf.png`; agrandar con override
  `scale(T, PoseStack, float)`.

## 7. Gotchas de API 1.21.1 (todos mordidos ya)

- `WolfModel` constructor: SOLO `(ModelPart)`. Sin boolean "slim".
- `MeleeAttackGoal`: `resetAttackCooldown()` privado/inexistente. Para daño+efecto extra por golpe:
  override `checkAndPerformAttack(LivingEntity target)` → `int before = target.hurtTime;`
  `super.checkAndPerformAttack(target);` `if (target.hurtTime > before) { efecto(); }`.
- Escala de renderer: `MobRenderer` NO tiene `getScale(...)`; es `protected void scale(T, PoseStack, float)`.
- `removeItemInHand(hand, fallback)` no existe → `setItemInHand(hand, ItemStack.EMPTY)`.
- Limpiar equipamiento guardado de mobs viejos: hacerlo en `aiStep` server-side (el constructor corre
  antes de cargar NBT). Mobs con `setPersistenceRequired` sobreviven updates con su item viejo.
- Knockback masivo propio: `target.setDeltaMovement(dir*2.6, 1.35, dir*2.6); target.hurtMarked = true;`
  (+ `hasImpulse`) — el knockback vanilla no basta.
- Retirar un item del mod por completo: borrar clase, registro `ModItems`, geo/anim/textura/model json,
  clave de lang, y `output.accept` en `ModCreativeTabs`. El mundo viejo loggea una vez
  `Tried to load invalid item` y lo tira a aire — es el comportamiento correcto, no tocar.

## 8. Git

- Repo en la carpeta del mod; `origin = https://github.com/juanfelipeocampopinzon/gacha-waifus.git`.
- Identidad: commitear con `git -c user.name="juanfelipeocampopinzon" -c user.email="juanocampo-dot"`
  (no modificar la config global del usuario).
- Mensajes estilo checkpoint: `feat(5.5.0): Tung Tung Sahur jugable, Curlys de ataque, …` en español.
- Auth HTTPS vía `gh` (`~/bin/gh auth login`, ya configurado por el usuario en 2026-10-20).
  Nunca pedir/pushear con contraseña de cuenta; si `git push` pide usuario/token, `gh` ya lo resuelve.
- El usuario revisa en Minecraft ANTES de subir: no hacer push de versiones sin su OK explícito.
