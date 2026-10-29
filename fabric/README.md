# Estructura multi-loader

Un solo mod, dos loaders. **No hay copia del código**: los dos compilan las mismas fuentes.

```
common/src/main/java        272 archivos   no saben de ningún loader
neoforge/src/main/java       36 archivos   capa NeoForge
fabric/src/main/java         36 archivos   capa Fabric (mismo paquete y clase)
```

La **carpeta es la frontera**: `common/` no tiene ni un `import net.neoforged`, y eso lo vigila
`tools/fabric_sync.py --check`.

## Comandos

```bash
./gradlew build              # los dos jars
./gradlew :neoforge:build    # solo NeoForge  -> neoforge/build/libs/gachawaifus-<v>.jar
./gradlew :fabric:build      # solo Fabric    -> fabric/build/libs/gachawaifus-fabric-<v>-fabric.1.jar
./gradlew :neoforge:runClient
python3 tools/fabric_sync.py --check
```

La versión del mod se escribe **una vez** en `gradle.properties` (raíz). Los dos jars la leen.

## Por qué compila sin traducir nada

- Las fuentes se escriben en **mappings de Mojang** (los mismos que usa NeoForge). Loom remapea a
  intermediary al empaquetar el jar de Fabric. Por eso `common/` compila tal cual en los dos.
- Gradle 9.7.1 es la versión del wrapper: ModDevGradle 2.0.78 y Loom 1.17.21 funcionan con ella.

## Qué hay en cada capa

| Ruta | Qué es |
|---|---|
| `common/` | Entidades, gacha, colores, menús, bust, items, animaciones. |
| `neoforge/` | `DeferredRegister`, eventos, `@Mod`, `ModConfigSpec`, `Dist`, `getPersistentData`. |
| `fabric/` | `Registry.register`, `ModInitializer`, config Gson, mixins. |
| `fabric/platform-files.txt` | Los 36 archivos que necesitan gemelo en Fabric. |
| `tools/fabric_sync.py` | Guardia de la frontera. |

## Estado

- [x] Reestructuración: `common/` + `neoforge/` + `fabric/` en un solo build Gradle.
- [x] Jar de NeoForge verificado tras la mudanza (15.262.813 B, 728 entradas, idéntico al anterior).
- [ ] Los 36 adaptadores de Fabric.
