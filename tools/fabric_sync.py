#!/usr/bin/env python3
"""Guardia de la frontera entre código compartido y capa de loader.

Estructura:

    common/src/main/java   -> 272 archivos que NO pueden saber de ningún loader
    neoforge/src/main/java -> 36 archivos con la API de NeoForge
    fabric/src/main/java   -> los 36 adaptadores equivalentes de Fabric

La carpeta ES la frontera. Este script solo comprueba que nadie la cruce por accidente:

  1. Ningún archivo de common/ puede importar net.neoforged (ni usar sus marcas típicas).
  2. Cada archivo de neoforge/ tiene que estar en fabric/platform-files.txt y tener su
     adaptador homónimo en fabric/ (mismo paquete y clase).
  3. Todo archivo de neoforge/ tiene que estar declarado en el manifiesto.

Uso:
    python3 tools/fabric_sync.py --check     # sale con 1 si hay desajustes
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
COMMON = ROOT / "common" / "src" / "main" / "java"
NEOFORGE = ROOT / "neoforge" / "src" / "main" / "java"
FABRIC = ROOT / "fabric" / "src" / "main" / "java"
MANIFEST = ROOT / "fabric" / "platform-files.txt"

# Marcas de que un archivo NO puede ir en common/.
LOADER_MARKS = re.compile(
    r"net\.neoforged"
    r"|net\.minecraftforge"
    r"|getPersistentData"
    r"|doesSneakBypassUse"
    r"|@Mod\b"
    r"|api\.distmarker\.Dist"
)


def read_manifest():
    entries = []
    for line in MANIFEST.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        entries.append(line)
    return entries


def rels(base):
    if not base.is_dir():
        return set()
    return {p.relative_to(base).as_posix() for p in base.rglob("*.java")}


def check():
    problems = []
    manifest = read_manifest()
    manifest_set = set(manifest)

    if len(manifest) != len(manifest_set):
        dups = sorted({p for p in manifest if manifest.count(p) > 1})
        problems.append("rutas duplicadas en platform-files.txt: %s" % ", ".join(dups))

    common = rels(COMMON)
    neoforge = rels(NEOFORGE)
    fabric = rels(FABRIC)

    for rel in sorted(common):
        text = (COMMON / rel).read_text(encoding="utf-8", errors="replace")
        if LOADER_MARKS.search(text):
            problems.append("common/ usa API de loader (debe ir a neoforge/): %s" % rel)

    for rel in sorted(neoforge):
        if rel not in manifest_set:
            problems.append("en neoforge/ pero no en el manifiesto: %s" % rel)
        if rel not in fabric:
            problems.append("sin adaptador Fabric: %s" % rel)

    for rel in manifest:
        if rel not in neoforge:
            problems.append("en el manifiesto pero no existe en neoforge/: %s" % rel)
        if rel not in fabric:
            problems.append("sin adaptador Fabric: %s" % rel)

    if problems:
        problems = sorted(set(problems))
        print("fabric_sync: %d problema(s)" % len(problems))
        for p in problems:
            print("  - " + p)
        print("\nArreglo: si un archivo de common/ toca la API del loader, muévelo a neoforge/ y")
        print("escribe su gemelo en fabric/. Si es un adaptador nuevo, añádelo a")
        print("fabric/platform-files.txt.")
        return 1

    print(
        "fabric_sync: OK (%d compartidos, %d de plataforma, %d adaptadores Fabric)"
        % (len(common), len(neoforge), len(fabric))
    )
    return 0


if __name__ == "__main__":
    if "--check" not in sys.argv:
        print("uso: python3 tools/fabric_sync.py --check")
        sys.exit(2)
    sys.exit(check())
