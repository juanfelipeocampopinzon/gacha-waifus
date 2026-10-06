# Changelog — Gacha Waifus Mod

Todas las notas de versión del mod en orden cronológico descendente.

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
