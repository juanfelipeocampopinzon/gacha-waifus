# Gacha Waifus Mod (Minecraft 1.21.1 NeoForge)

Mod de waifus coleccionables con combate inspirado en *Zenless Zone Zero (ZZZ)*.

> **Versión:** `2.1.0-beta.1` ⚠️ BETA · **Minecraft:** 1.21.1 · **NeoForge:** 21.1.233

Consulta el [CHANGELOG.md](CHANGELOG.md) para las notas de actualización completas.

## 🌟 Prototipo: Astra Yao
- **Rol:** Soporte Éter / Cantante Idol (Stars of Lyra)
- **Skin:** Modelo slim/alex personalizado
- **Ataque Normal:** Disparos de pulsos etéreos a distancia (`gachawaifus:ether_blast`)
- **Habilidad Especial (EX Special - Vocal Solo):** Onda sónica en área que aturde y daña enemigos mientras otorga Fuerza II y Velocidad II al jugador invocador.
- **Ultimate (Grand Finale Concert):** Espectáculo sónico masivo en 10 bloques, daño devastador a hostiles, curación completa, absorción y regeneración al jugador.

## 🔥 Prototipo: Burnice White
- **Rol:** Daño Fuego / Atrayente (Sons of Calydon)
- **Skin:** Modelo slim/alex personalizado
- **Ataque Normal:** Cortada ígnea que pega fuego al objetivo por 3s + partículas de lava y flamas. Cooldown 1s.
- **Habilidad Especial (Jet Flamethrower):** Embestida con invulnerabilidad temporal (25 ticks), 16 de daño de fuego, Slowness I y Weakness I en abanico. Cooldown 10s.
- **Ultimate (Burnice Supernova):** Explosión térmica en 10 bloques con 25 de daño de fuego a enemigos, e imbuye al jugador invocador con Resistencia al Fuego y Fuerza II. Cooldown 35s.

## ⚔️ Nuevo: Ye Shunguang
- **Rol:** Investigador Élite / Alto Preceptor (Yunkui Summit)
- **Elemento:** Ether | **Atributo Especial:** Auric Ink
- **Skin:** Modelo slim/alex personalizado con colores púrpura/delgado
- **Ataque Normal (Auric Slash):** Corte con ink que causa daño directo + indirecto mágico, partículas ENCHANTED_HIT y WITCH. Cooldown 25 ticks.
- **Habilidad Especial (Ink Eruption):** Canalización especial con invulnerabilidad temporal (20 ticks), AoE en 5 bloques con 22 de daño indirecto mágico, debuffs Weakness II y Blindness 10s. Cooldown 250 ticks.
- **Ultimate (Endless Talisman):** Fase de meditación invulnerable (30 ticks), AoE masivo en 12 bloques con 35 de daño indirecto mágico, buffs al invocador (DAMAGE_BOOST II, SPEED II, ABSORPTION III) y a la propia entidad. Cooldown 750 ticks.

## 📦 Instalación
1. Colocar `gachawaifus-2.1.0-beta.1.jar` en `%appdata%\.minecraft\mods\`.
2. Iniciar Minecraft con el perfil de **NeoForge 21.1.x** para **1.21.1**.
3. Obtener el **Talismán de Invocación de Ye Shunguang** desde la pestaña creativa "Gacha Waifus" o crafteo.
4. Click derecho en el suelo para invocar a Ye Shunguang en el escenario.

## 🛠️ Desarrollo (Para Modders)
- **Project Root:** `e:/super proyecto de moding gachas minecraft/gachawaifus`
- **Compiler / JDK:** OpenJDK 21 LTS
- **Build Tool:** `./gradlew.bat`
- **To Compile & Build:**
  ```powershell
  cd "E:\super proyecto de moding gachas minecraft\gachawaifus"
  .\gradlew.bat build
  ```
- **Built Jar:** `gachawaifus/build/libs/gachawaifus-2.1.0-beta.1.jar`

## 🎵 Sonidos y Animaciones
- Convierte audio a `.ogg` (44.1kHz).
- Coloca en `assets/gachawaifus/sounds/<nombre>/sonido.ogg`.
- Regístrala en `sounds.json`.

## 💃 Animaciones Fluidas (GeckoLib)
- Exporta `.geo.json` y `.animation.json` de Blockbench.
- Implementa `GeoEntity` para animaciones claveframe.

## 🌐 Investigación de Personajes
Antes de crear una nueva waifu, usa:
```powershell
$env:PYTHONIOENCODING="utf-8"
python "e:/super proyecto de moding gachas minecraft/tools/research_character.py" "Character Name"
```
