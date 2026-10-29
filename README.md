# Gacha Waifus Mod (Minecraft 1.21.1 Multi-Loader: NeoForge & Fabric)

Mod de waifus coleccionables con combate inspirado en *Zenless Zone Zero (ZZZ)*, mecánicas de gacha (banners promocional y permanente con pity), sistema de hogar, modos tácticos de comportamiento y animaciones fluidas con **GeckoLib**.

> **Versión:** `6.6.0` · **Minecraft:** 1.21.1 · **NeoForge:** `21.1.233` · **Fabric:** `0.116.17` · **Java:** 21

---

## 🌟 Características Principales

- **Más de 60 Waifus Coleccionables:** Procedentes de ZZZ y otros universos (Miyabi, Ellen Joe, Burnice, Nicole, Anby, Kurumi, Ye Shunguang, Astra Yao, Rina, Lycaon, Koleda, Grace, Soldier 11, etc.).
- **Kit de Combate de 3 Habilidades:** Ataque Normal, Habilidad Especial y Ultimate por waifu (con animaciones GeckoLib sincronizadas con el ataque).
- **Sistema de Colores y Roles (Matriz de Daño ±33 %):** 
  - 11 Colores (Rojo, Naranja, Amarillo, Verde, Azul, Morado, Rosa, Marrón, Negro, Blanco, Gris).
  - 5 Roles de equipo: *Attack, Stun, Anomaly, Support, Defense*.
- **Sistema de Gacha Completo:**
  - Máquina Gacha y Terminal Gacha interactivo.
  - Pestaña **Promocional** (Bolitas Rosas, rotación diaria, pity 64, 50/50).
  - Pestaña **Permanente** (Bolitas Azules, pity 64 independiente, entrega directa de waifus estándar no poseídas).
- **Gestión de Mascotas y Hogar:**
  - **Bloque de Hogar (`WaifuHomeBlock`):** Establece el punto de base de tus waifus (con soporte para camuflaje/mímica de bloques).
  - **Panel de Modos (`Shift + Clic derecho`):** Configura el comportamiento en tiempo real (*Pasivo, Neutro, Agresivo, Sentada, En Casa*).
  - **Baliza Waifu (`WaifuBeaconItem`):** Teletransporte y llamada individual o de escuadrón completo.
  - **Cápsula Waifu:** Si una waifu es derrotada, queda registrada en la cápsula para poder ser revivida con diamantes.
- **Caminar y Físicas Vanilla:** Animaciones de caminata adaptativas con cadencia según la velocidad real del suelo y soporte de físicas dinámicas para modelos compatibles.

---

## 🏗️ Arquitectura del Repositorio (Multi-Loader)

El proyecto utiliza una arquitectura de código compartido en un único repositorio:

```text
gachawaifus/
├── common/             # 270+ clases comunes y TODOS los recursos compartidos:
│   ├── src/main/java/  # Entidades, renderers, tokens, modelos GeckoLib, IA, gacha, GUI.
│   └── src/main/resources/ # Texturas, animaciones, geo JSON, sonidos, lang (es_es, en_us).
├── neoforge/           # Adaptadores y registros para NeoForge 1.21.1:
│   └── src/main/java/  # DeferredRegister, eventos de bus, networking NeoForge.
├── fabric/             # Adaptadores para Fabric 1.21.1:
│   └── src/main/java/  # Callbacks Fabric API, registros vanilla, networking Fabric.
├── tools/
│   └── fabric_sync.py  # Script de validación de frontera entre loaders.
├── gradlew / gradlew.bat
└── agents.md           # Reglas de desarrollo y API para asistentes de IA.
```

---

## 🛠️ Cómo Compilar el Proyecto

### Requisitos
- **JDK 21** (OpenJDK 21 LTS recomendado).
- Conexión inicial o entorno Gradle configurado.

### En Windows (PowerShell / CMD)
```powershell
cd gachawaifus

# Compilar solo el JAR de NeoForge:
.\gradlew.bat :neoforge:build

# Compilar solo el JAR de Fabric:
.\gradlew.bat :fabric:build

# Compilar ambos loaders a la vez:
.\gradlew.bat build
```

### En Linux / macOS (Bash)
```bash
cd gachawaifus
J="$(cd ../tools/jdk-21-linux && pwd)" # O tu ruta habitual a JAVA_HOME (JDK 21)

# Compilar solo NeoForge:
env JAVA_HOME="$J" ./gradlew :neoforge:build -Dorg.gradle.java.home="$J" --offline --no-daemon

# Compilar solo Fabric:
env JAVA_HOME="$J" ./gradlew :fabric:build -Dorg.gradle.java.home="$J" --offline --no-daemon

# Compilar ambos loaders:
env JAVA_HOME="$J" ./gradlew build -Dorg.gradle.java.home="$J" --offline --no-daemon
```

### Jars Generados
- **NeoForge:** `neoforge/build/libs/gachawaifus-6.6.0.jar`
- **Fabric:** `fabric/build/libs/gachawaifus-fabric-6.6.0-fabric.1.jar`

---

## 🧪 Cómo Probar en Juego

1. Copia el archivo `.jar` correspondiente a tu carpeta `.minecraft/mods/` (renombra cualquier versión previa a `*.jar.bak`).
2. **NeoForge:** Ejecuta con **NeoForge 21.1.x** para Minecraft 1.21.1 (requiere GeckoLib 4.9.3 para NeoForge).
3. **Fabric:** Ejecuta con **Fabric Loader 0.19.x+** para Minecraft 1.21.1 (requiere Fabric API 0.116+ y GeckoLib 4.9.3 para Fabric).

---

## 🤝 Reglas de Integración y Trabajo

Si vas a programar o integrar una nueva waifu:
- **Crea el código en `common/`:** Entidad, Token y Renderer viven en `common/`.
- **Registros:** Registra la entidad y token tanto en `neoforge/` como en `fabric/`.
- **Validación automática:** Ejecuta `python3 tools/fabric_sync.py --check` para verificar que ninguna clase compartida contenga imports directos de una plataforma específica.
- Consulta detallada en [`agents.md`](agents.md) y [`AGENTS.md`](../AGENTS.md).
