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

## Reglas del proyecto (obligatorias)

Están en el `AGENTS.md` de la **carpeta padre**:
`E:\super proyecto de moding gachas minecraft\AGENTS.md`. **Léelo antes de tocar nada** (API de
NeoForge 1.21.1, plantilla de habilidades, assets, sonidos, animaciones).

## Cosas que NO están en esta carpeta (usa rutas absolutas)

| Qué | Dónde |
|---|---|
| Arte ORIGINAL de cada waifu (no se sobrescribe nunca) | `E:\super proyecto de moding gachas minecraft\chicas\<waifu>\` |
| Arte original de jefes/mobs | `E:\super proyecto de moding gachas minecraft\bosses\<mob>\` |
| Diario de cambios (registrar aquí cada avance) | `E:\super proyecto de moding gachas minecraft\MODLOG.md` |
| Herramientas (sprites, música, backups) | `E:\super proyecto de moding gachas minecraft\tools\` |
| Universal Modder CLI | `E:\super proyecto de moding gachas minecraft\universal-modder\` |

## Cómo trabajar aquí (evita los errores habituales)

1. **Rutas absolutas** siempre que salgas de esta carpeta; dentro de ella, relativas a esta raíz.
2. **Lee un fichero antes de editarlo.** Los parches por texto fallan si no has leído el contenido
   exacto; si el fichero es corto, reescríbelo entero.
3. **Nada de métodos inventados.** Comprueba la API real de 1.21.1/NeoForge antes de usarla.
4. **Una tarea = un fichero o un cambio concreto.** Los refactors de 10+ ficheros de golpe acaban en
   líos; ve paso a paso y compila entre pasos.
5. **Para una waifu nueva, copia un patrón existente.** Con renderer de modelo de jugador (sin
   GeckoLib): `entity/VonLycaonEntity.java` + `entity/client/VonLycaonRenderer.java` +
   `item/VonLycaonTokenItem.java`. Si la skin es de 64x64 **no** lleva pieza del pecho.
6. **Checklist de una waifu nueva:** entidad + token + textura de entidad + textura de token (32x32)
   + modelo del item + registro en `ModEntities`, `ModItems`, `GachaWaifusMod` (atributos **y**
   renderer), bloque en `ModCreativeTabs`, entrada en `WaifuRoster`, color en `WaifuColors` y claves
   en `en_us.json` + `es_es.json`.
7. **Nada de emojis fuera del rango que dibuja Minecraft** (los ✨/★ se usan ya en el mod; los
   emojis tipo 🔥 salen como cuadro vacío).
