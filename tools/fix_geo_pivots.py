#!/usr/bin/env python3
"""Recoloca los pivotes de los huesos de accesorio al punto de union con su padre.

GeckoLib carga el geo en un espacio ESPEJADO EN X (`BakedModelFactory$Builtin.constructBone` hace
`updateRotation(-rotX, -rotY, +rotZ)` y `updatePivot(-pivotX, +pivotY, +pivotZ)`; `constructCube` pone
el origen en `-(o.x + s.x)/16`). Leido en espacio JSON eso sale `T(pivote) * Rz(-rotZ) * Ry(rotY) *
Rx(-rotX) * T(-pivote)` sobre los `origin` absolutos de los cubos, o sea `rot_mat()`. En una pieza que
cuelga: `rx > 0` lleva la punta hacia la espalda (+z) y `rz > 0` la lleva hacia dentro (-x).

Si el hueso NO tiene rotacion base, mover el pivote no desplaza nada: solo cambia
el centro de giro. Si SI la tiene, la malla en reposo se desplaza d = (I - R)(p_nuevo - p_viejo)
y el accesorio acaba clavado en el cuerpo (regresion de la 7.6.1: coletas de Miku y capa de Roxy).
Por eso, al recolocar un pivote con rotacion base, este script traslada los cubos del hueso
c = -R^T d para que la pose en reposo quede identica y solo cambie el eje de giro.

Un accesorio con el pivote en [0, 24, 0] (el centro del pecho) no cuelga de donde esta cosido:
orbita alrededor del torso y parece despegarse.

Regla, eje por eje (comparando la caja EN REPOSO del hueso con la de su padre):
  - el hueso cuelga por debajo del padre  -> pivote en la cara SUPERIOR (lo ata arriba)
  - sobresale por el lado + del padre     -> pivote en la cara interior (lo)
  - sobresale por el lado - del padre     -> pivote en la cara interior (hi)
  - solapa con el padre                   -> centro de ese eje

La distancia que se reporta es pivote actual -> punto de union, no pivote -> caja: un pivote
dentro de la malla pero a media altura (coleta izquierda de Alice) giro mal igual.

Los huesos de grupo (sin cubos, p. ej. `coletas`, `tentaculos`, `capa`) toman la media de los
puntos de union de sus hijos, para que el balanceo del grupo tambien salga del cuerpo.

Los objetos que se empunan o se llevan (espada, escudo, katana, maletin...) se excluyen: su
pivote lo decide el arma, no la anatomia.

Uso:
  python3 tools/fix_geo_pivots.py                          # informe de common/.../geo
  python3 tools/fix_geo_pivots.py --write                  # aplica (pivote + compensacion)
  python3 tools/fix_geo_pivots.py --write --root ../modelos/personajes
  python3 tools/fix_geo_pivots.py --min-shift 2.0          # solo los muy despegados
Sale con 1 si queda algun pivote sospechoso y se pidio --check.
"""
import argparse
import json
import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_ROOT = ROOT / "common" / "src" / "main" / "resources" / "assets" / "gachawaifus" / "geo"

# Huesos del cuerpo de jugador: su pivote ya es el correcto y no se toca nunca.
STANDARD = {"root", "body", "head", "breasts", "right_arm", "left_arm", "right_leg", "left_leg"}

# Objetos en mano / equipo: se ignoran a proposito.
HELD = {"sword", "katana", "katana_drawn", "escudo", "shield", "briefcase", "maletin",
        "torch", "fuel_pack", "boot_blade", "arma", "weapon"}


def is_held(name):
    low = name.lower()
    return any(h in low for h in HELD)


def aabb(cubes):
    lo = [min(c["origin"][i] for c in cubes) for i in range(3)]
    hi = [max(c["origin"][i] + c["size"][i] for c in cubes) for i in range(3)]
    return lo, hi


def _mul(A, B):
    return [[sum(A[i][k] * B[k][j] for k in range(len(B))) for j in range(len(B[0]))]
            for i in range(len(A))]


def rot_mat(rot):
    """R = Rz(-rotZ) * Ry(rotY) * Rx(-rotX) en espacio JSON.

    GeckoLib convierte el geo a un espacio espejado en X (`constructBone` hace
    `updatePivot(-pivotX, pivotY, pivotZ)` y `updateRotation(-rotX, -rotY, rotZ)`), y al volver a
    espacio JSON eso queda como `Fx * Rz(rotZ) * Ry(-rotY) * Rx(-rotX) * Fx`, o sea negando rotX y
    rotZ y dejando rotY. Por eso en el juego una `rx` POSITIVA lleva la punta de lo que cuelga hacia
    ATRAS (+z) y una `rz` POSITIVA la lleva hacia DENTRO (-x): lo contrario de lo que dicta la
    intuición de Bedrock.
    """
    rx, ry, rz = (rot or [0, 0, 0])[:3]
    x, y, z = math.radians(-rx), math.radians(ry), math.radians(-rz)
    cx, sx = math.cos(x), math.sin(x)
    cy, sy = math.cos(y), math.sin(y)
    cz, sz = math.cos(z), math.sin(z)
    return _mul([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]],
                _mul([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]], [[1, 0, 0], [0, cx, -sx], [0, sx, cx]]))


def rest_aabb(bone):
    """Caja de la malla ya con la rotacion base aplicada, en el espacio del padre."""
    pivot = bone.get("pivot", [0, 0, 0])
    R = rot_mat(bone.get("rotation"))
    pts = []
    for c in bone["cubes"]:
        o, s = c["origin"], c["size"]
        for i in range(8):
            v = [o[0] + (s[0] if i & 1 else 0), o[1] + (s[1] if i & 2 else 0), o[2] + (s[2] if i & 4 else 0)]
            pts.append([sum(R[r][k] * (v[k] - pivot[k]) for k in range(3)) + pivot[r] for r in range(3)])
    return ([min(p[i] for p in pts) for i in range(3)],
            [max(p[i] for p in pts) for i in range(3)])


def compensation(p_old, p_new, rot):
    """Traslado de cubos que deja la malla en reposo donde estaba tras mover el pivote."""
    R = rot_mat(rot)
    delta = [p_new[i] - p_old[i] for i in range(3)]
    d = [delta[i] - sum(R[i][k] * delta[k] for k in range(3)) for i in range(3)]
    Rt = [[R[j][i] for j in range(3)] for i in range(3)]
    return [-sum(Rt[i][k] * d[k] for k in range(3)) for i in range(3)]


def round3(v):
    return [round(x, 2) for x in v]


def parent_box(bone, bones):
    """Caja en reposo del padre; si no tiene cubos se sube hasta el primer ancestro con malla."""
    seen = set()
    par = bones.get(bone.get("parent"))
    while par is not None and par["name"] not in seen:
        seen.add(par["name"])
        if par.get("cubes"):
            return rest_aabb(par)
        par = bones.get(par.get("parent"))
    return None


def attachment(bone, bones):
    """Punto de union del hueso con su padre (o None si no se puede calcular)."""
    lo, hi = rest_aabb(bone)
    box = parent_box(bone, bones)
    if box is None:
        return None
    plo, phi = box
    out = []
    for i in range(3):
        if i == 1 and lo[1] < plo[1]:
            out.append(hi[1])              # cuelga: se ata por arriba
        elif lo[i] >= phi[i]:
            out.append(lo[i])              # sobresale por el lado +
        elif hi[i] <= plo[i]:
            out.append(hi[i])              # sobresale por el lado -
        else:
            out.append((lo[i] + hi[i]) / 2.0)
    return out


def analyze(geo):
    """Devuelve (cambios, nuevos). cambios = [(nombre, pivote_viejo, pivote_nuevo, d)]."""
    bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
    nuevos = {}
    for name, b in bones.items():
        if name in STANDARD or is_held(name) or not b.get("cubes"):
            continue
        p = attachment(b, bones)
        if p is not None:
            nuevos[name] = p

    cambios = []
    for name, p in nuevos.items():
        b = bones[name]
        viejo = b.get("pivot", [0, 0, 0])
        d = math.dist(viejo, p)
        if d > 0.0 and round3(viejo) != round3(p):
            cambios.append((name, viejo, p, d))

    # grupos sin cubos: media de los puntos de union de sus hijos
    for name, b in bones.items():
        if b.get("cubes") or name in STANDARD or is_held(name):
            continue
        hijos = [nuevos[c["name"]] for c in bones.values()
                 if c.get("parent") == name and c["name"] in nuevos]
        if not hijos:
            continue
        p = [sum(h[i] for h in hijos) / len(hijos) for i in range(3)]
        viejo = b.get("pivot", [0, 0, 0])
        d = math.dist(viejo, p)
        if d > 0.0 and round3(viejo) != round3(p):
            cambios.append((name, viejo, p, d))
    return cambios, nuevos


def descendants_with_cubes(name, bones):
    out = []
    for b in bones.values():
        par, steps = b.get("parent"), 0
        while par and steps < 16:
            if par == name:
                if b.get("cubes"):
                    out.append(b["name"])
                break
            par = bones[par].get("parent") if par in bones else None
            steps += 1
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--root", default=str(DEFAULT_ROOT),
                    help="carpeta (recursiva) o fichero .geo.json; por defecto common/.../geo")
    ap.add_argument("--write", action="store_true", help="aplica los cambios")
    ap.add_argument("--check", action="store_true",
                    help="sale con 1 si queda algun pivote sospechoso")
    ap.add_argument("--min-shift", type=float, default=1.0,
                    help="distancia minima (unidades) para considerar el pivote malo")
    ap.add_argument("--all", action="store_true", help="muestra tambien los cambios pequenos")
    args = ap.parse_args()

    base = Path(args.root)
    files = sorted(base.rglob("*.geo.json")) if base.is_dir() else [base]
    total = aplicados = 0
    for f in files:
        geo = json.loads(f.read_text(encoding="utf-8"))
        cambios, _ = analyze(geo)
        cambios = [c for c in cambios if c[3] >= args.min_shift or args.all]
        if not cambios:
            continue
        total += len(cambios)
        bones = {b["name"]: b for b in geo["minecraft:geometry"][0]["bones"]}
        print(f"\n{f}")
        for name, viejo, nuevo, d in sorted(cambios, key=lambda c: -c[3]):
            marca = "" if d >= 2.0 else "  (sutil)"
            rot = bones[name].get("rotation") or [0, 0, 0]
            comp = compensation(viejo, round3(nuevo), rot)
            need_comp = any(abs(x) > 0.01 for x in comp)
            huesos = [name] if bones[name].get("cubes") else descendants_with_cubes(name, bones)
            aviso = f"  + cubos {str([round(x, 2) for x in comp])} en {','.join(huesos)}" if need_comp else ""
            print(f"  {name:<34} {str(round3(viejo)):<18} -> {str(round3(nuevo)):<18} d={d:4.1f}{marca}{aviso}")
            if args.write and need_comp:
                for bn in huesos:
                    for c in bones[bn]["cubes"]:
                        c["origin"] = [round(c["origin"][i] + comp[i], 3) for i in range(3)]
            if args.write:
                bones[name]["pivot"] = round3(nuevo)
        if args.write:
            f.write_text(json.dumps(geo, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            aplicados += len(cambios)

    if args.write:
        print(f"\nfix_geo_pivots: {aplicados} pivotes recolocados en {len(files)} geo(s)")
    else:
        print(f"\nfix_geo_pivots: {total} pivotes a recolocar ({len(files)} geo revisados) - sin --write no se toca nada")
    if args.check and total:
        sys.exit(1)


if __name__ == "__main__":
    main()
