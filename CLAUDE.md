# Reglas del Proyecto para Claude Code / Asistentes IA

Proyecto: Mod de Minecraft 1.21.1 (Multi-loader: NeoForge 21.1.233 + Fabric 0.116.17, Java 21).

## 1. REGLAS DURAS DE ARQUITECTURA
- **Frontera:**
  - `common/src/main/java/` NUNCA puede importar `net.neoforged` ni `net.fabricmc`. Es código neutro.
  - Toda API específica vive en `neoforge/` y en `fabric/` con EXACTAMENTE el mismo paquete y nombre de clase.
  - Si tocas un archivo en `neoforge/src/main/java/`, DEBES actualizar su gemelo en `fabric/src/main/java/`.
  - Corre SIEMPRE antes de terminar: `python3 tools/fabric_sync.py --check`

## 2. ESTRUCTURAS, BIOMAS Y SPAWNS (Data-Driven)
- **NO modifiques `WildWaifuSpawner.java`** para alterar biomas o lista de waifus por bioma.
- El sistema de generación es 100% Data-Driven vía JSON en `common/src/main/resources/data/gachawaifus/`:
  - `structures/`: archivos `.nbt` exportados con Structure Block.
  - `worldgen/template_pool/`, `worldgen/structure/`, `worldgen/structure_set/`: definición Jigsaw y aparición.
  - `worldgen/biome/`: nuevos biomas en JSON.
  - `tags/worldgen/biome/climate/`: asignación de biomas a climas (`frio.json`, `calido.json`, `templado.json`, etc.).
  - `tags/worldgen/biome/`: exclusiones globales (`no_wild_spawns.json`) y por waifu (`whitelist/<id>.json`, `blacklist/<id>.json`, `spawn_extra/<id>.json`).
  - `tags/worldgen/structure/no_wild_spawns.json`: estructuras donde no deben nacer waifus salvajes (aldeas).
- Valida siempre con: `python3 tools/validate_collab_data.py`.

## 3. VIDA Y ATRIBUTOS (Cero crashes)
- Toda waifu extiende `AbstractWaifuEntity`.
- Toda waifu DEBE registrarse en `registerAttributes` en ambos loaders (`neoforge` y `fabric`). Sin proveedor de atributos, la entidad crashea al spawnear.
- Toda entidad nacida por spawn custom o doma debe tener vida completa asegurada: `waifu.setHealth(waifu.getMaxHealth())`.
- Cada waifu debe estar en `WaifuRoster.ROTATION`, `WaifuColors` y en ambos archivos `lang/`.

## 4. API DE MINECRAFT 1.21.1
- NUNCA usar `new ResourceLocation(...)`. Usar `ResourceLocation.fromNamespaceAndPath("gachawaifus", "path")`.
- NUNCA importar `net.minecraft.world.item.TooltipContext`. Es una clase anidada interna de `Item`.
- NUNCA inventar métodos de versiones anteriores (`ticksExisted`, etc.).

## 5. COMANDOS DE VALIDACIÓN
```bash
python3 tools/fabric_sync.py --check
python3 tools/validate_collab_data.py
./gradlew :neoforge:compileJava :fabric:compileJava
```
