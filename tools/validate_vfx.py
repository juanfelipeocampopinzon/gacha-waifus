#!/usr/bin/env python3
"""Validador de efectos VFX (v0). Solo biblioteca estándar.

Comprueba (incluido el estándar de tier S/M/L) cada JSON de assets/gachawaifus/vfx/**: claves permitidas por tipo de capa, listas
cerradas (texturas, formas, anclas), rangos numéricos, degradados y un presupuesto de partículas.
Cada stage puede declarar un "trigger": {"clip": "animation.<waifu>.<clip>", "at": segundos}. El
validador cruza ese clip con animations/<waifu>.animation.json y cada ancla "bone:<x>" con
geo/<waifu>.geo.json. Así, si al volver del paquete de edición (Blockbench) se renombra un clip o
un hueso, se detecta aquí y no en el juego. Los VFX NO se escriben dentro de los .animation.json.

Uso:  python3 tools/validate_vfx.py            # rutas por defecto de common/
      python3 tools/validate_vfx.py --vfx-dir X --anim-dir Y --geo-dir Z
Sale con 1 si hay errores.
"""
import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "common" / "src" / "main" / "resources" / "assets" / "gachawaifus"

TEXTURES = {"soft_glow", "spark", "streak", "petal", "shard", "mote", "smoke",
            "ring_thin", "ring_soft", "arc_streak", "ribbon_soft", "star4", "flame_8f",
            "flame_puff_8f", "orb", "cube", "note", "note_double", "light_beam",
            "clock_face", "clock_hand", "clock_hand_short", "solid", "sigil", "spike_burst", "ice_cracks", "cut_lines",
            "rune_ring", "scorch"}

BLENDS = {"additive", "alpha"}
SHAPES = {"point", "disc", "ring", "cone", "line", "sphere"}
DIRECTIONS = {"outward", "inward", "up", "down", "forward", "look", "random", "bone"}
EASES = {"linear", "in", "out"}
ANCHOR_RE = re.compile(r"^(entity|ground|target|bone:[a-z0-9_]+)$")
HEX_RE = re.compile(r"^#[0-9a-fA-F]{6}$")
ID_RE = re.compile(r"^[a-z0-9_]+(/[a-z0-9_]+)+$")
CLIP_RE = re.compile(r"^animation\.[a-z0-9_]+\.[a-z0-9_]+$")

MAX_LAYERS = 20
MAX_LAYER_PARTICLES = 600
# Estándar de calidad por slot: S = golpe, M = habilidad, L = ultimate, XL = ultimate insignia
# (una sola por roster: la Explosión de Megumin, con carga, estallido y poso de 6 s).
TIERS = {"S": {"particles": 40, "layers": 3, "duration": 0.6},
         "M": {"particles": 150, "layers": 5, "duration": 4.0},
         "L": {"particles": 400, "layers": 8, "duration": 3.5},
         "XL": {"particles": 1200, "layers": 20, "duration": 10.0}}

COMMON = {"type": str, "at": "t", "anchor": "anchor", "offset": "vec3"}
# tipo -> {clave: regla}. Las claves de COMMON se aceptan en todos los tipos.
SPEC = {
    "emitter": {"shape": SHAPES, "radius": (0, 64), "texture": TEXTURES, "blend": BLENDS,
                "count": ("int", 0, 600), "rate": "curve", "duration": (0.05, 10),
                "lifetime": ("pair", 0.05, 10), "size": ("pair", 0.01, 32),
                "color": "gradient", "speed": ("pair", 0, 40), "direction": DIRECTIONS,
                "gravity": (-2, 2), "swirl": (0, 20), "fade_in": (0, 5),
                "stretch": (0, 4), "core": (0, 1)},
    "ring_wave": {"radius": ("pair", 0, 64), "thickness": ("pair", 0.05, 8),
                  "duration": (0.05, 5), "ease": EASES, "y": (-4, 8),
                  "color": "gradient", "texture": {"ring_thin", "ring_soft"}, "blend": BLENDS},
    "telegraph": {"radius": (0.5, 64), "spin": (-20, 20), "fill_alpha": (0, 1),
                  "color": "gradient", "fade_in": (0, 5), "duration": (0.05, 10), "thickness": (0.02, 4),
                  "texture": {"ring_thin"}},
    "glow": {"size": ("pair", 0.1, 64), "color": "gradient", "duration": (0.05, 10),
             "texture": {"soft_glow"}},
    "slash_arc": {"radius": (0.3, 16), "arc_degrees": (30, 360), "width": (0.1, 6),
                  "duration": (0.05, 2), "tilt": (-90, 90), "texture": {"arc_streak", "streak"},
                  "reveal": (0.02, 2),
                  "color": "gradient"},
    "ribbon": {"bone": "bone", "width": ("pair", 0.02, 3), "lifetime": (0.05, 2),
               "duration": (0.05, 10), "color": "gradient",
               "texture": {"streak", "ribbon_soft"}},
    "shake": {"amplitude": (0, 1), "duration": (0.05, 5), "radius": (1, 64),
              "falloff": {"linear", "none"}},
    "flash": {"intensity": (0, 1), "duration": (0.02, 1), "color": "hex",
              "scope": {"world", "screen"}},
    # textura plana en el suelo o de pie tras la waifu (relojes, sellos, sombras). "angle" = curva de
    # grados sobre la vida de la capa ([[0,0],[1,720]] = dos vueltas); sin angle gira a "spin" rad/s.
    "decal": {"radius": (0.1, 64), "texture": TEXTURES, "blend": BLENDS, "color": "gradient",
              "duration": (0.05, 10), "fade_in": (0, 5), "spin": (-20, 20), "angle": "angle",
              "plane": {"ground", "vertical"}, "y": (-4, 64)},
    # la waifu redibujada en otro sitio, translúcida y teñida (su renderer debe soportar VfxClones)
    "clone": {"color": "gradient", "duration": (0.05, 10), "rise": (0, 4)},
    # esfera o cúpula translúcida con borde fresnel (agujeros negros, campos). radius = [inicio, fin]
    # recorrido en "grow" segundos; el cuerpo usa "color" con alfa normal (puede ser oscuro).
    "sphere": {"radius": ("pair", 0, 64), "grow": (0.02, 10), "ease": EASES, "hemisphere": {True, False},
               "color": "gradient", "rim": (0, 4), "rim_color": "hex", "rim_color2": "hex",
               "duration": (0.05, 10), "fade_in": (0, 5), "y": (-4, 64), "texture": {"solid"},
               "blend": BLENDS},
    # proyectil del ancla (o hueso) al objetivo: "to" target = punto de VfxTargeted
    "projectile": {"to": {"target", "forward"}, "duration": (0.05, 3), "ease": EASES, "arc": (-8, 8),
                   "trail": (0, 1), "size": ("pair", 0.01, 6), "texture": TEXTURES, "blend": BLENDS,
                   "color": "gradient", "y": (-4, 8)},
}
REQUIRED = {"emitter": {"shape", "texture", "color", "lifetime", "size"},
            "ring_wave": {"radius", "duration", "color"},
            "telegraph": {"radius", "color"},
            "glow": {"size", "color", "duration"},
            "slash_arc": {"radius", "arc_degrees", "duration", "color"},
            "ribbon": {"bone", "lifetime", "width", "color"},
            "shake": {"amplitude", "duration"},
            "flash": {"intensity", "duration"},
            "decal": {"radius", "texture", "color"},
            "clone": {"offset", "color"},
            "sphere": {"radius", "color"},
            "projectile": {"size", "texture", "color", "duration"}}


def num(v):
    return isinstance(v, (int, float)) and not isinstance(v, bool)


def check_value(path, key, rule, v, err):
    where = f"{path}.{key}"
    if isinstance(rule, set):
        if v not in rule:
            err(f"{where}: '{v}' no está en {sorted(rule)}")
    elif rule == "t":
        if not (num(v) and 0 <= v <= 10):
            err(f"{where}: debe ser número 0..10")
    elif rule == "anchor":
        if not (isinstance(v, str) and ANCHOR_RE.match(v)):
            err(f"{where}: ancla inválida '{v}' (entity|ground|target|bone:<nombre>)")
    elif rule == "bone":
        if not (isinstance(v, str) and re.match(r"^[a-z0-9_]+$", v)):
            err(f"{where}: nombre de hueso inválido")
    elif rule == "vec3":
        if not (isinstance(v, list) and len(v) == 3 and all(num(x) and -48 <= x <= 48 for x in v)):
            err(f"{where}: debe ser [x,y,z] entre -48 y 48")
    elif rule == "hex":
        if not (isinstance(v, str) and HEX_RE.match(v)):
            err(f"{where}: color #RRGGBB")
    elif rule == "gradient":
        ok = isinstance(v, list) and len(v) >= 2
        last = -1.0
        if ok:
            for s in v:
                if not (isinstance(s, list) and len(s) == 3 and num(s[0]) and 0 <= s[0] <= 1
                        and isinstance(s[1], str) and HEX_RE.match(s[1])
                        and num(s[2]) and 0 <= s[2] <= 1 and s[0] >= last):
                    ok = False
                    break
                last = s[0]
            ok = ok and v[0][0] == 0 and v[-1][0] == 1
        if not ok:
            err(f"{where}: degradado = [[0,\"#rrggbb\",alfa], ..., [1,\"#rrggbb\",alfa]] con t ascendente")
    elif rule == "angle":
        if not (num(v) and -3600 <= v <= 3600) and not (isinstance(v, list) and len(v) >= 2 and all(
                isinstance(p, list) and len(p) == 2 and num(p[0]) and 0 <= p[0] <= 1
                and num(p[1]) and -3600 <= p[1] <= 3600 for p in v)):
            err(f"{where}: grados o curva [[t0..1, grados], ...] entre -3600 y 3600")
    elif rule == "curve":
        if num(v):
            if not 0 <= v <= 400:
                err(f"{where}: fuera de 0..400")
        elif not (isinstance(v, list) and len(v) >= 2 and all(
                isinstance(p, list) and len(p) == 2 and num(p[0]) and 0 <= p[0] <= 1
                and num(p[1]) and 0 <= p[1] <= 400 for p in v)):
            err(f"{where}: número o curva [[t0..1, valor], ...]")
    elif isinstance(rule, tuple) and len(rule) == 2:
        if not (num(v) and rule[0] <= v <= rule[1]):
            err(f"{where}: debe estar entre {rule[0]} y {rule[1]}")
    elif isinstance(rule, tuple) and rule[0] == "int":
        if not (isinstance(v, int) and not isinstance(v, bool) and rule[1] <= v <= rule[2]):
            err(f"{where}: entero entre {rule[1]} y {rule[2]}")
    elif isinstance(rule, tuple) and rule[0] == "pair":
        if not (isinstance(v, list) and len(v) == 2 and all(num(x) and rule[1] <= x <= rule[2] for x in v)):
            err(f"{where}: [a,b] con valores entre {rule[1]} y {rule[2]}")


def peak_particles(layer, stage_duration):
    if layer.get("type") != "emitter":
        return 0
    rate = layer.get("rate", 0)
    if isinstance(rate, list):
        rmax = max((p[1] for p in rate if isinstance(p, list) and len(p) == 2 and num(p[1])), default=0)
    else:
        rmax = rate if num(rate) else 0
    life = layer.get("lifetime", [0, 0])
    lmax = life[1] if isinstance(life, list) and len(life) == 2 and num(life[1]) else 0
    count = layer.get("count", 0) if isinstance(layer.get("count", 0), int) else 0
    return count + rmax * min(lmax, stage_duration)


def validate_file(path, rel, errors):
    def err(msg):
        errors.append(f"{rel}: {msg}")
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as e:  # noqa: BLE001
        err(f"JSON inválido: {e}")
        return {}
    if not isinstance(data, dict):
        err("la raíz debe ser un objeto")
        return {}
    for k in data:
        if k not in {"schema", "id", "doc", "stages"}:
            err(f"clave desconocida en la raíz: '{k}'")
    if data.get("schema") != 1:
        err("schema debe ser 1")
    expected = rel[:-5] if rel.endswith(".json") else rel
    if data.get("id") != expected or not ID_RE.match(str(data.get("id", ""))):
        err(f"id debe coincidir con la ruta: '{expected}'")
    stages = data.get("stages")
    if not isinstance(stages, dict) or not stages:
        err("stages debe ser un objeto con al menos un stage")
        return {}
    for sname, stage in stages.items():
        sp = f"stages.{sname}"
        if not re.match(r"^[a-z0-9_]+$", sname):
            err(f"{sp}: nombre de stage inválido")
        if not isinstance(stage, dict) or set(stage) - {"duration", "layers", "doc", "trigger", "tier"}:
            err(f"{sp}: solo admite tier, duration, layers, trigger y doc")
            continue
        dur = stage.get("duration")
        if not (num(dur) and 0.05 <= dur <= 10):
            err(f"{sp}.duration: número 0.05..10")
            dur = 1.0
        tier = stage.get("tier")
        if tier not in TIERS:
            err(f"{sp}.tier: debe ser uno de {sorted(TIERS)} (S golpe, M habilidad, L ultimate)")
            tier = "L"
        lim = TIERS[tier]
        if num(dur) and dur > lim["duration"]:
            err(f"{sp}: duración {dur} s supera el máximo del tier {tier} ({lim['duration']} s)")
        trig = stage.get("trigger")
        if trig is not None:
            # Dos formas: con clip de GeckoLib ({"clip": "animation.x.y", "at": s}) o solo "at"
            # para los efectos que arranca el servidor/casteo (VfxCasters), sin animación.
            if not (isinstance(trig, dict) and set(trig) <= {"clip", "at"}
                    and (trig.get("at") is None or (num(trig["at"]) and 0 <= trig["at"] <= 10))
                    and ("clip" not in trig or (isinstance(trig["clip"], str) and CLIP_RE.match(trig["clip"])))):
                err(f"{sp}.trigger: debe ser {{\"clip\": \"animation.<waifu>.<clip>\", \"at\": segundos}}"
                    f" o {{\"at\": segundos}} para efectos por casteo")
        layers = stage.get("layers")
        if not isinstance(layers, list) or not layers or len(layers) > MAX_LAYERS:
            err(f"{sp}.layers: lista de 1..{MAX_LAYERS} capas")
            continue
        if len(layers) > lim["layers"]:
            err(f"{sp}: {len(layers)} capas superan el máximo del tier {tier} ({lim['layers']})")
        total = 0
        for i, layer in enumerate(layers):
            lp = f"{sp}.layers[{i}]"
            ltype = layer.get("type") if isinstance(layer, dict) else None
            if ltype not in SPEC:
                err(f"{lp}.type: '{ltype}' no está en {sorted(SPEC)}")
                continue
            allowed = set(COMMON) | set(SPEC[ltype]) | {"doc"}
            for k in layer:
                if k not in allowed:
                    err(f"{lp}: clave '{k}' no existe para type '{ltype}'")
            for req in REQUIRED[ltype]:
                if req not in layer:
                    err(f"{lp}: falta '{req}'")
            for k, v in layer.items():
                if k in COMMON and k != "type":
                    check_value(lp, k, COMMON[k], v, err)
                elif k in SPEC[ltype]:
                    check_value(lp, k, SPEC[ltype][k], v, err)
            if num(layer.get("at")) and layer["at"] > dur:
                err(f"{lp}.at ({layer['at']}) supera la duración del stage ({dur})")
            peak = peak_particles(layer, dur)
            if peak > MAX_LAYER_PARTICLES:
                err(f"{lp}: pico estimado {int(peak)} > {MAX_LAYER_PARTICLES} partículas")
            total += peak
        if total > lim["particles"]:
            err(f"{sp}: pico estimado {int(total)} supera el máximo del tier {tier} ({lim['particles']} partículas)")
    return data


def geo_bones(path):
    """Nombres de huesos de un .geo.json (formato Bedrock moderno o antiguo)."""
    names = set()

    def walk(node):
        if isinstance(node, dict):
            b = node.get("bones")
            if isinstance(b, list):
                names.update(x["name"] for x in b if isinstance(x, dict) and "name" in x)
            for v in node.values():
                walk(v)
        elif isinstance(node, list):
            for v in node:
                walk(v)
    walk(json.loads(path.read_text(encoding="utf-8")))
    return names


def cross_check(data, rel, anim_dir, geo_dir, errors):
    waifu = rel.split("/")[0]
    anim_f = anim_dir / f"{waifu}.animation.json"
    geo_f = geo_dir / f"{waifu}.geo.json"
    clips, bones = None, None
    if anim_f.is_file():
        try:
            clips = set((json.loads(anim_f.read_text(encoding="utf-8")).get("animations") or {}))
        except Exception:  # noqa: BLE001
            errors.append(f"{rel}: no pude leer {anim_f.name}")
    if geo_f.is_file():
        try:
            bones = geo_bones(geo_f)
        except Exception:  # noqa: BLE001
            errors.append(f"{rel}: no pude leer {geo_f.name}")
    n = 0
    for sname, stage in (data.get("stages") or {}).items():
        trig = stage.get("trigger") if isinstance(stage, dict) else None
        if isinstance(trig, dict) and isinstance(trig.get("clip"), str):
            n += 1
            if clips is None:
                errors.append(f"{rel}: stage '{sname}' usa trigger pero no existe animations/{waifu}.animation.json")
            elif trig["clip"] not in clips:
                errors.append(f"{rel}: stage '{sname}': el clip '{trig['clip']}' no existe en {anim_f.name}")
        for layer in (stage.get("layers") or []) if isinstance(stage, dict) else []:
            if not isinstance(layer, dict):
                continue
            used = []
            if isinstance(layer.get("anchor"), str) and layer["anchor"].startswith("bone:"):
                used.append(layer["anchor"][5:])
            if isinstance(layer.get("bone"), str):
                used.append(layer["bone"])
            for b in used:
                if bones is None:
                    errors.append(f"{rel}: stage '{sname}' usa el hueso '{b}' pero no existe geo/{waifu}.geo.json")
                elif b not in bones:
                    errors.append(f"{rel}: stage '{sname}': el hueso '{b}' no existe en {geo_f.name}")
    return n


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--vfx-dir", default=str(RES / "vfx"))
    ap.add_argument("--anim-dir", default=str(RES / "animations"))
    ap.add_argument("--geo-dir", default=str(RES / "geo"))
    args = ap.parse_args()
    vfx_dir, anim_dir, geo_dir = Path(args.vfx_dir), Path(args.anim_dir), Path(args.geo_dir)
    errors, n_stages, n_trig = [], 0, 0
    files = sorted(vfx_dir.rglob("*.json")) if vfx_dir.is_dir() else []
    for f in files:
        rel = f.relative_to(vfx_dir).as_posix()
        data = validate_file(f, rel, errors)
        if isinstance(data.get("stages"), dict):
            n_stages += len(data["stages"])
            n_trig += cross_check(data, rel, anim_dir, geo_dir, errors)
    if errors:
        print(f"validate_vfx: {len(errors)} problema(s)")
        for e in sorted(set(errors)):
            print("  - " + e)
        return 1
    print(f"validate_vfx: OK ({len(files)} archivos, {n_stages} stages, {n_trig} triggers de clip verificados)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
