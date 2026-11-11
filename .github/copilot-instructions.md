# GitHub Copilot Instructions for Gacha Waifus

- This is a Minecraft 1.21.1 multi-loader project (NeoForge 21.1.233 and Fabric 0.116.17).
- `common/` is platform-independent Java code: NEVER import `net.neoforged` or `net.fabricmc`.
- `neoforge/` and `fabric/` must maintain exact parity. Every class in `neoforge/` has an identical package and class name in `fabric/`.
- Worldgen, biomes, waifu biome assignments, and structures are entirely data-driven via JSON in `common/src/main/resources/data/gachawaifus/`. Do NOT modify `WildWaifuSpawner.java` for adding waifus to biomes.
- Entities must register attributes in both loaders (`registerAttributes`).
- When spawning a waifu, ensure full health with `waifu.setHealth(waifu.getMaxHealth())`.
- Always validate with `python3 tools/fabric_sync.py --check` and `python3 tools/validate_collab_data.py`.
