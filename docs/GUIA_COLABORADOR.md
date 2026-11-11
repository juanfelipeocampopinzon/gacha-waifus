# Guía para el Creador de Contenido: Estructuras, Biomas y Spawns

Bienvenido al equipo de **Gacha Waifus**. Esta guía está diseñada para que puedas agregar tus propias estructuras, crear y configurar biomas, y controlar exactamente dónde y cómo aparecen las waifus y los bosses **sin necesidad de programar en Java**.

En Minecraft 1.21.1, todo este sistema funciona mediante **archivos de datos (JSON y NBT)**. 

---

## 1. Dónde colocar cada archivo

Todos tus archivos de trabajo viven dentro de la carpeta:
`common/src/main/resources/data/gachawaifus/`

| Contenido | Carpeta donde se guarda | Tipo de archivo |
|---|---|---|
| Construcciones físicas | `structures/` | `.nbt` |
| Piezas de estructura | `worldgen/template_pool/` | `.json` |
| Reglas de estructura (biomas, jefes) | `worldgen/structure/` | `.json` |
| Frecuencia en el mapa | `worldgen/structure_set/` | `.json` |
| Biomas personalizados | `worldgen/biome/` | `.json` |
| Asignación de clima a biomas | `tags/worldgen/biome/climate/` | `.json` |
| Reglas de spawn de waifus | `tags/worldgen/biome/` | `.json` |
| Zonas/estructuras protegidas | `tags/worldgen/structure/` | `.json` |

---

## 2. Cómo agregar una Estructura al juego

### Paso 1: Construir en Minecraft y exportar a `.nbt`
1. Entra a un mundo creativo en Minecraft.
2. Construye tu templo, santuario o ruina.
3. Dame un bloque de estructura con:
   ```mcfunction
   /give @p structure_block
   ```
4. Pon el bloque en una esquina de tu construcción.
5. Cambia el modo a **GUARDAR (Save)**, define el tamaño que cubre tu estructura y ponle un nombre (por ejemplo: `waifu_shrine`).
6. Presiona **Guardar**. Minecraft generará un archivo `.nbt` en tu carpeta del mundo (`saves/<tu_mundo>/generated/minecraft/structures/waifu_shrine.nbt`).
7. Copia ese archivo `.nbt` a:
   `common/src/main/resources/data/gachawaifus/structures/waifu_shrine.nbt`

---

### Paso 2: Crear el Template Pool (`worldgen/template_pool/waifu_shrine.json`)
Este archivo le dice al juego qué archivo `.nbt` cargar:
```json
{
  "name": "gachawaifus:waifu_shrine",
  "fallback": "minecraft:empty",
  "elements": [
    {
      "weight": 1,
      "element": {
        "element_type": "minecraft:single_pool_element",
        "location": "gachawaifus:waifu_shrine",
        "processors": "minecraft:empty",
        "projection": "rigid"
      }
    }
  ]
}
```

---

### Paso 3: Configurar la Estructura (`worldgen/structure/waifu_shrine.json`)
Aquí defines en qué biomas aparece y si debe spawnear algún boss o waifu dentro:
```json
{
  "type": "minecraft:jigsaw",
  "biomes": "#minecraft:is_overworld",
  "step": "surface_structures",
  "spawn_overrides": {
    "monster": {
      "bounding_box": "piece",
      "spawns": [
        {
          "type": "gachawaifus:ricardo_milos",
          "weight": 1,
          "minCount": 1,
          "maxCount": 1
        }
      ]
    }
  },
  "terrain_adaptation": "beard_thin",
  "start_pool": "gachawaifus:waifu_shrine",
  "size": 1,
  "start_height": {
    "absolute": 0
  },
  "project_start_to_heightmap": "WORLD_SURFACE_WG",
  "max_distance_from_center": 80,
  "use_expansion_hack": false
}
```
> **Tip:** En `biomes` puedes usar `#minecraft:is_overworld` para cualquier bioma normal, o un bioma específico como `minecraft:cherry_grove`.

---

### Paso 4: Frecuencia de aparición (`worldgen/structure_set/waifu_shrine.json`)
Controla qué tan rara es la estructura:
```json
{
  "structures": [
    {
      "structure": "gachawaifus:waifu_shrine",
      "weight": 1
    }
  ],
  "placement": {
    "type": "minecraft:random_spread",
    "spacing": 34,
    "separation": 12,
    "salt": 9821435
  }
}
```
- `spacing`: distancia media (en chunks) entre estructuras (ej. 34 = moderadamente rara, 60 = muy rara).
- `separation`: distancia mínima obligatoria entre estructuras del mismo tipo.
- `salt`: un número aleatorio cualquiera para que no coincida con otras estructuras.

---

## 3. Control de Spawns de Waifus y Biomas

El mod cuenta con un sistema inteligente de waifus salvajes basado en **colores y climas**:
- **Frío:** Blanco, Azul, Gris (nieve, hielo, picos).
- **Templado:** Verde, Rosa, Morado, Blanco (bosques, llanuras, cerezos).
- **Cálido:** Rojo, Naranja, Amarillo, Marrón (desiertos, sabanas, junglas).
- **Nether:** Rojo, Naranja, Marrón, Negro.
- **End:** Morado, Negro, Gris, Azul.

Tú puedes modificar y ampliar este comportamiento usando **Tags JSON**:

### A. Asignar un bioma a un clima
Para que un bioma (sea vanilla, creado por ti o de otro mod) tenga la fauna de un clima:
Abre el archivo correspondiente en `common/src/main/resources/data/gachawaifus/tags/worldgen/biome/climate/`:
- `frio.json`
- `templado.json`
- `calido.json`
- `nether.json`
- `end.json`

Y añade el ID del bioma a la lista `"values"`:
```json
{
  "replace": false,
  "values": [
    "minecraft:cherry_grove",
    "gachawaifus:waifu_garden"
  ]
}
```

---

### B. Prohibir que nazcan waifus en un bioma específico
Si quieres que en cierto bioma NO nazca ninguna waifu salvaje (por ejemplo el vacío o una dimensión específica):
Edita `tags/worldgen/biome/no_wild_spawns.json`:
```json
{
  "replace": false,
  "values": [
    "minecraft:deep_dark"
  ]
}
```

---

### C. Restringir una waifu a ciertos biomas (Whitelist)
Si quieres que una waifu **SOLO** pueda aparecer en ciertos biomas:
Crea el archivo `tags/worldgen/biome/whitelist/<waifu_id>.json`:
*(Ejemplo: `whitelist/tokisaki_kurumi.json`)*
```json
{
  "replace": false,
  "values": [
    "minecraft:dark_forest",
    "minecraft:soul_sand_valley"
  ]
}
```
*Si este archivo existe para esa waifu, ella NUNCA aparecerá fuera de los biomas listados.*

---

### D. Prohibir una waifu en un bioma (Blacklist)
Si quieres que una waifu aparezca normalmente según su clima, pero quieres excluirla de un bioma concreto:
Crea `tags/worldgen/biome/blacklist/<waifu_id>.json`:
```json
{
  "replace": false,
  "values": [
    "minecraft:swamp"
  ]
}
```

---

### E. Añadir una waifu como aparición extra en un bioma
Si quieres que una waifu aparezca en un bioma aunque su color normalmente no encaje allí (por ejemplo, Miyabi en un bosque de cerezos):
Crea `tags/worldgen/biome/spawn_extra/<waifu_id>.json`:
```json
{
  "replace": false,
  "values": [
    "minecraft:cherry_grove"
  ]
}
```

---

### F. Proteger aldeas o estructuras contra waifus salvajes
Para evitar que las waifus salvajes nazcan dentro de aldeas de aldeanos o dentro de una estructura tuya:
Edita `tags/worldgen/structure/no_wild_spawns.json`:
```json
{
  "replace": false,
  "values": [
    "#minecraft:village",
    "gachawaifus:waifu_shrine"
  ]
}
```

---

## 4. Crear un Bioma Nuevo

Para crear un bioma completamente nuevo:
1. Copia la plantilla `common/src/main/resources/data/gachawaifus/worldgen/biome/waifu_garden.json` y renómbrala (ej. `bosque_magico.json`).
2. Configura los colores visuales:
   - `sky_color`: color del cielo (número decimal entero del color hex).
   - `fog_color`: color de la niebla.
   - `grass_color`: color del césped.
   - `foliage_color`: color de las hojas de los árboles.
3. Agrégalo a tu clima favorito en `tags/worldgen/biome/climate/templado.json`.

---

## 5. Herramienta de Validación Automática

Antes de compilar o subir tus cambios a GitHub, puedes comprobar si cometiste algún error de escritura o si te faltó un `.nbt`:

Ejecuta en tu terminal:
```bash
python tools/validate_collab_data.py
```

El programa te avisará en verde `[OK]` si todo está bien o en rojo `[ERROR]` si alguna coma o corchete en tus JSONs está mal colocado.

---

## 6. Cómo Compilar y Probar en Minecraft

Para generar el mod con tus cambios:

### En Windows:
```cmd
.\gradlew.bat :neoforge:build
```

### En Linux / Mac:
```bash
./gradlew :neoforge:build
```

El archivo resultante estará en:
`neoforge/build/libs/gachawaifus-<version>.jar`

Copia ese `.jar` a tu carpeta `.minecraft/mods/` y pruébalo en el juego con:
- `/locate structure gachawaifus:waifu_shrine` (para teletransportarte a tu estructura).
- `/gwcolor biomas` (para ver qué waifus pueden salir en el bioma actual).
