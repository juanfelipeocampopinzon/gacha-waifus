#!/usr/bin/env python3
"""Validador de datos (worldgen, estructuras, tags) para colaboradores.

Permite a cualquier colaborador verificar que sus archivos JSON, NBTs y tags
estén bien formateados antes de compilar o hacer commit, sin necesitar Java.

Uso:
    python tools/validate_collab_data.py
"""

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "common" / "src" / "main" / "resources" / "data" / "gachawaifus"
STRUCTURES_DIR = DATA_DIR / "structures"
WORLDGEN_DIR = DATA_DIR / "worldgen"
TAGS_DIR = DATA_DIR / "tags" / "worldgen"

OK = "\033[92m[OK]\033[0m"
WARN = "\033[93m[AVISO]\033[0m"
FAIL = "\033[91m[ERROR]\033[0m"

errors = 0
warnings = 0


def log_ok(msg: str):
    print(f" {OK} {msg}")


def log_warn(msg: str):
    global warnings
    warnings += 1
    print(f" {WARN} {msg}")


def log_fail(msg: str):
    global errors
    errors += 1
    print(f" {FAIL} {msg}")


def validate_json_files(base_path: Path):
    if not base_path.is_dir():
        return
    for json_path in base_path.rglob("*.json"):
        rel = json_path.relative_to(ROOT)
        try:
            with open(json_path, "r", encoding="utf-8") as f:
                data = json.load(f)
            log_ok(f"JSON sintaxis valida: {rel}")
            
            # Verificaciones basicas de tags
            if "tags" in str(json_path):
                if not isinstance(data, dict) or "values" not in data:
                    log_warn(f"{rel}: Los tags de Minecraft suelen requerir un objeto con 'values': [...]")
                elif not isinstance(data.get("values"), list):
                    log_fail(f"{rel}: El campo 'values' debe ser una lista [...]")
                    
        except json.JSONDecodeError as exc:
            log_fail(f"Error de sintaxis en {rel} (linea {exc.lineno}, col {exc.colno}): {exc.msg}")
        except Exception as exc:
            log_fail(f"No se pudo leer {rel}: {exc}")


def validate_template_pools():
    pool_dir = WORLDGEN_DIR / "template_pool"
    if not pool_dir.is_dir():
        return
    for pool_file in pool_dir.glob("*.json"):
        rel = pool_file.relative_to(ROOT)
        try:
            with open(pool_file, "r", encoding="utf-8") as f:
                data = json.load(f)
            elements = data.get("elements", [])
            for elem in elements:
                inner = elem.get("element", {})
                loc = inner.get("location", "")
                if loc.startswith("gachawaifus:"):
                    struct_name = loc.replace("gachawaifus:", "")
                    nbt_path = STRUCTURES_DIR / f"{struct_name}.nbt"
                    if not nbt_path.is_file():
                        log_warn(f"{rel} referencia a '{loc}', pero no existe el archivo '{nbt_path.name}' en data/gachawaifus/structures/")
                    else:
                        log_ok(f"NBT encontrado para '{loc}': {nbt_path.name}")
        except Exception as exc:
            pass


def main():
    print("==================================================")
    print(" Verificando datos de Worldgen, Tags y Estructuras")
    print("==================================================")

    if not DATA_DIR.is_dir():
        log_fail(f"No se encontro la carpeta de datos en {DATA_DIR}")
        sys.exit(1)

    print("\n1. Comprobando sintaxis JSON...")
    validate_json_files(WORLDGEN_DIR)
    validate_json_files(TAGS_DIR)

    print("\n2. Comprobando correspondencia entre Template Pools y archivos .nbt...")
    validate_template_pools()

    print("\n==================================================")
    if errors > 0:
        print(f"\033[91mResultado: {errors} error(es) y {warnings} aviso(s) encontrados.\033[0m")
        print("Corrige los errores antes de probar en el juego.")
        sys.exit(1)
    elif warnings > 0:
        print(f"\033[93mResultado: Todo valido sintacticamente con {warnings} aviso(s).\033[0m")
        sys.exit(0)
    else:
        print("\033[92mResultado: Todos los datos son perfectamente validos. Listo para probar!\033[0m")
        sys.exit(0)


if __name__ == "__main__":
    main()
