#!/usr/bin/env python3
"""Previsualizador offline de efectos VFX: simula un JSON de vfx/ y lo dibuja sin abrir el juego.

Sirve para iterar un efecto (y para que una IA que no puede abrir Minecraft VEA lo que escribe):
genera una hoja de fotogramas con dos vistas (3/4 delante-derecha y cenital) y, opcionalmente, un GIF.

Replica el motor de common/.../vfx/ (VfxInstance, Particle, VfxRenderer, VfxScreen):
- emisores: count/rate, at, duration, shapes, directions, gravity, swirl, stretch, core, sub-tick,
  tope de partículas por tier, sprites *_8f por edad, notas que se balancean, light_beam vertical;
- ring_wave, glow, telegraph, slash_arc; flash como velo de color; shake como rótulo.
Limitaciones (dilo al revisar): los huesos no existen aquí (se usa la MISMA aproximación de pecho
que usa el motor cuando no encuentra el hueso), no hay profundidad (se pinta en orden) y no se
anima el modelo: la figura gris es una silueta de 1,8 bloques que mira hacia +Z.

Uso (dentro de gachawaifus/):
  python3 tools/vfx_preview.py hatsune_miku/ultimate            # todos los stages con su trigger.at
  python3 tools/vfx_preview.py hatsune_miku/ultimate#drop       # un stage
  python3 tools/vfx_preview.py burnice_white/special#channel --frames 10 --gif
Salida por defecto: ../agents-bus/captures/<waifu>_<efecto>[_<stage>].png (y .gif).
Requiere Pillow y numpy. Determinista con --seed.
"""
import argparse
import json
import math
import random
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "common" / "src" / "main" / "resources" / "assets" / "gachawaifus"
CAPTURES = ROOT.parent / "agents-bus" / "captures"
TPS = 20.0
CAPS = {"S": 40, "M": 150, "L": 400}
TAU = math.tau


# --------------------------------------------------------------------------- datos
def gradient(value):
    if not isinstance(value, list) or len(value) < 2:
        return [(0.0, (1.0, 1.0, 1.0, 1.0)), (1.0, (1.0, 1.0, 1.0, 1.0))]
    stops = []
    for t, hexa, a in value:
        rgb = int(hexa[1:], 16)
        stops.append((float(t), (((rgb >> 16) & 255) / 255, ((rgb >> 8) & 255) / 255, (rgb & 255) / 255, float(a))))
    return stops


def sample_gradient(stops, t):
    if t <= stops[0][0]:
        return stops[0][1]
    for (t0, c0), (t1, c1) in zip(stops, stops[1:]):
        if t <= t1:
            k = 0.0 if t1 - t0 < 1e-5 else (t - t0) / (t1 - t0)
            return tuple(a + (b - a) * k for a, b in zip(c0, c1))
    return stops[-1][1]


def curve(value):
    if value is None:
        return None
    if isinstance(value, (int, float)):
        return [(0.0, float(value))]
    return [(float(t), float(v)) for t, v in value]


def sample_curve(points, t):
    if len(points) == 1 or t <= points[0][0]:
        return points[0][1]
    for (t0, v0), (t1, v1) in zip(points, points[1:]):
        if t <= t1:
            k = 0.0 if t1 - t0 < 1e-5 else (t - t0) / (t1 - t0)
            return v0 + (v1 - v0) * k
    return points[-1][1]


def layer_defaults(raw, stage_duration):
    """Mismo reparto de valores por defecto que VfxLayer.parse."""
    L = dict(raw)
    t = L.get("type", "")
    L.setdefault("at", 0.0)
    L.setdefault("anchor", "entity")
    L["offset"] = tuple(L.get("offset", (0, 0, 0)))
    L.setdefault("shape", "point")
    if t == "ring_wave":
        L["ring_radius"] = tuple(raw.get("radius", (0.5, 3.0)))
        L["radius"] = L["ring_radius"][1]
    L.setdefault("radius", 0.0)
    if t == "shake" and "radius" not in raw:
        L["radius"] = 16.0
    L.setdefault("texture", "spark")
    L["additive"] = raw.get("blend", "alpha") == "additive"
    L.setdefault("count", 0)
    L["rate_c"] = curve(raw.get("rate"))
    if t == "emitter":
        L["duration"] = raw.get("duration", stage_duration)
    elif t == "telegraph":
        L["duration"] = max(0.05, raw.get("duration", stage_duration - L["at"]))
    else:
        L["duration"] = raw.get("duration", 0.2)
    L.setdefault("lifetime", (0.3, 0.6))
    L["size"] = tuple(raw.get("size", (0.3, 0.05)))
    L["grad"] = gradient(raw.get("color")) if t != "flash" else None
    L.setdefault("speed", (0.0, 1.0))
    L.setdefault("direction", "random")
    for key in ("gravity", "stretch", "core", "swirl", "spin", "fill_alpha", "fade_in", "tilt", "y"):
        L.setdefault(key, 0.0)
    L.setdefault("thickness", (0.3, 0.1))
    L["tel_thickness"] = raw.get("thickness", 0.25) if t == "telegraph" else 0.25
    L.setdefault("ease", "linear")
    L.setdefault("arc_degrees", 120.0)
    L["arc_width"] = raw.get("width", 0.6) if t == "slash_arc" else 0.6
    L.setdefault("intensity", 0.5)
    L.setdefault("scope", "world")
    if t == "flash":
        c = int(str(raw.get("color", "#ffffff"))[1:], 16)
        L["flash_rgb"] = (((c >> 16) & 255) / 255, ((c >> 8) & 255) / 255, (c & 255) / 255)
    L.setdefault("amplitude", 0.1)
    L.setdefault("falloff", "linear")
    return L


# --------------------------------------------------------------------------- vectores
def v(x, y, z):
    return np.array([x, y, z], dtype=float)


def norm(a):
    n = np.linalg.norm(a)
    return a / n if n > 1e-9 else a


def rand_unit(rng):
    z = rng.random() * 2 - 1
    ang = rng.random() * TAU
    r = math.sqrt(max(0.0, 1 - z * z))
    return v(math.cos(ang) * r, z, math.sin(ang) * r)


UP = v(0, 1, 0)


# --------------------------------------------------------------------------- simulación
class Particle:
    __slots__ = ("pos", "vel", "age", "life", "s0", "s1", "grad", "tex", "add", "phase", "grav",
                 "stretch", "core", "swirl", "wobble")


class Instance:
    """Espejo de VfxInstance. La waifu está en el origen y mira hacia +Z."""

    def __init__(self, stage, delay, rng):
        self.stage = stage
        self.rng = rng
        self.origin = v(0, 0, 0)
        self.look = v(0, 0, 1)
        self.ticks = -round(max(0.0, delay) * TPS)
        self.particles = []
        self.acc = [0.0] * len(stage["layers"])
        self.fired = [False] * len(stage["layers"])
        self.cap = int(CAPS.get(stage["tier"], 400) * 1.5)

    def local(self, off):
        fwd = norm(v(self.look[0], 0, self.look[2]))
        right = v(-fwd[2], 0, fwd[0])
        return right * off[0] + v(0, off[1], 0) + fwd * off[2]

    def anchor(self, L):
        base = self.origin
        if L["anchor"].startswith("bone:"):
            # La misma aproximación que el motor cuando no encuentra el hueso.
            right = norm(np.cross(self.look, UP))
            left = L["anchor"].startswith("bone:left")
            base = self.origin + v(0, 1.35, 0) + right * (-0.35 if left else 0.35) + self.look * 0.2
        return base + self.local(L["offset"])

    def tick(self):
        self.ticks += 1
        seconds = self.ticks / TPS
        for i, L in enumerate(self.stage["layers"]):
            if L["type"] != "emitter":
                continue
            local = seconds - L["at"]
            if local < 0:
                continue
            if L["count"] > 0 and not self.fired[i]:
                self.fired[i] = True
                self.spawn(L, L["count"])
            if L["rate_c"] is None or local > L["duration"]:
                continue
            progress = min(1.0, max(0.0, local / max(0.01, L["duration"])))
            self.acc[i] += sample_curve(L["rate_c"], progress) * 0.05
            n = int(self.acc[i])
            if n > 0:
                self.acc[i] -= n
                self.spawn(L, n)
        alive = []
        for p in self.particles:
            p.pos = p.pos + p.vel
            if p.swirl:
                p.pos = p.pos + p.wobble * (p.swirl * 0.02 * math.sin(p.age * 0.45 + p.phase * 6.283))
            if p.grav:
                p.vel = p.vel + v(0, -p.grav * 0.05, 0)
            p.age += 1
            if p.age < p.life:
                alive.append(p)
        self.particles = alive

    def spawn(self, L, amount):
        rng = self.rng
        base = self.anchor(L)
        for i in range(amount):
            if len(self.particles) >= self.cap:
                return
            pos = self.shape(L, base)
            d = self.direction(L, pos, base)
            if L["shape"] == "cone":
                d = norm(d + rand_unit(rng) * max(0.12, L["radius"]))
            speed = (L["speed"][0] + (L["speed"][1] - L["speed"][0]) * rng.random()) / TPS
            life = L["lifetime"][0] + (L["lifetime"][1] - L["lifetime"][0]) * rng.random()
            vel = d * speed
            off = (i + rng.random()) / max(1, amount)
            pos = pos + vel * off
            wob = np.cross(vel, rand_unit(rng))
            wob = norm(wob) if np.linalg.norm(wob) > 1e-4 else v(0, 1, 0)
            p = Particle()
            p.pos, p.vel, p.age, p.life = pos, vel, off, max(1.0, life * TPS)
            p.s0, p.s1, p.grad, p.tex, p.add = L["size"][0], L["size"][1], L["grad"], L["texture"], L["additive"]
            p.phase, p.grav, p.stretch, p.core, p.swirl, p.wobble = (
                rng.random(), L["gravity"], L["stretch"], L["core"], L["swirl"], wob)
            self.particles.append(p)

    def shape(self, L, base):
        rng = self.rng
        ang = rng.random() * TAU
        s, r = L["shape"], L["radius"]
        if s == "disc":
            d = r * math.sqrt(rng.random())
            return base + v(math.cos(ang) * d, 0, math.sin(ang) * d)
        if s == "ring":
            return base + v(math.cos(ang) * r, 0, math.sin(ang) * r)
        if s == "sphere":
            return base + rand_unit(rng) * (r * rng.random() ** (1 / 3))
        if s == "line":
            right = norm(np.cross(self.look, UP))
            return base + right * ((rng.random() * 2 - 1) * r)
        if s == "cone":
            return base + rand_unit(rng) * (r * 0.25 * rng.random())
        return base.copy()

    def direction(self, L, pos, base):
        d = L["direction"]
        if d == "up":
            return UP.copy()
        if d == "down":
            return v(0, -1, 0)
        if d in ("forward", "look", "bone"):
            return self.look.copy()
        if d == "inward":
            t = base - pos
            return norm(t) if np.linalg.norm(t) > 1e-3 else rand_unit(self.rng)
        if d == "outward":
            a = v(pos[0] - base[0], 0, pos[2] - base[2])
            if np.linalg.norm(a) < 1e-3:
                ang = self.rng.random() * TAU
                return v(math.cos(ang), 0, math.sin(ang))
            return norm(a)
        return rand_unit(self.rng)

    def time(self):
        return self.ticks / TPS

    def progress(self, L):
        return min(1.0, max(0.0, (self.time() - L["at"]) / max(0.01, L["duration"])))

    def visible(self, L):
        now = self.time()
        return L["at"] <= now <= L["at"] + L["duration"]


# --------------------------------------------------------------------------- cámara y quads
class Camera:
    def __init__(self, pos, target, up_hint, w, h, fov=55):
        self.pos, self.w, self.h = np.asarray(pos, float), w, h
        self.f = norm(np.asarray(target, float) - self.pos)
        self.r = norm(np.cross(self.f, up_hint))
        self.u = np.cross(self.r, self.f)
        self.focal = (w / 2) / math.tan(math.radians(fov) / 2)

    def project(self, p):
        d = p - self.pos
        z = float(np.dot(d, self.f))
        if z < 0.2:
            return None
        return (self.w / 2 + self.focal * float(np.dot(d, self.r)) / z,
                self.h / 2 - self.focal * float(np.dot(d, self.u)) / z)


def homography(src, dst):
    a = []
    for (x, y), (X, Y) in zip(src, dst):
        a.append([x, y, 1, 0, 0, 0, -X * x, -X * y])
        a.append([0, 0, 0, x, y, 1, -Y * x, -Y * y])
    b = [c for pt in dst for c in pt]
    try:
        h = np.linalg.solve(np.array(a, float), np.array(b, float))
    except np.linalg.LinAlgError:
        return None
    return np.append(h, 1.0).reshape(3, 3)


TEXTURES = {}


def texture(name):
    if name not in TEXTURES:
        path = RES / "textures" / "vfx" / f"{name}.png"
        if not path.is_file():
            path = RES / "textures" / "vfx" / "spark.png"
        img = Image.open(path).convert("LA")
        arr = np.asarray(img, dtype=float) / 255.0
        TEXTURES[name] = arr
    return TEXTURES[name]


def draw_quad(canvas, cam, corners, uv, tex, rgba, additive):
    """corners = [bottomLeft, bottomRight, topRight, topLeft] en el mundo; uv = (u0, v0, u1, v1)."""
    scr = [cam.project(c) for c in corners]
    if any(s is None for s in scr) or rgba[3] <= 0.004:
        return
    u0, v0, u1, v1 = uv
    src = [(u0, v1), (u1, v1), (u1, v0), (u0, v0)]
    H = homography(src, scr)
    if H is None:
        return
    try:
        Hi = np.linalg.inv(H)
    except np.linalg.LinAlgError:
        return
    xs = [s[0] for s in scr]
    ys = [s[1] for s in scr]
    x0, x1 = max(0, int(math.floor(min(xs)))), min(cam.w, int(math.ceil(max(xs))) + 1)
    y0, y1 = max(0, int(math.floor(min(ys)))), min(cam.h, int(math.ceil(max(ys))) + 1)
    if x1 <= x0 or y1 <= y0 or (x1 - x0) * (y1 - y0) > cam.w * cam.h * 4:
        return
    gx, gy = np.meshgrid(np.arange(x0, x1) + 0.5, np.arange(y0, y1) + 0.5)
    den = Hi[2, 0] * gx + Hi[2, 1] * gy + Hi[2, 2]
    with np.errstate(divide="ignore", invalid="ignore"):
        uu = (Hi[0, 0] * gx + Hi[0, 1] * gy + Hi[0, 2]) / den
        vv = (Hi[1, 0] * gx + Hi[1, 1] * gy + Hi[1, 2]) / den
    lo_u, hi_u = min(u0, u1), max(u0, u1)
    inside = (uu >= lo_u) & (uu < hi_u) & (vv >= 0) & (vv < 1) & np.isfinite(uu) & np.isfinite(vv)
    if not inside.any():
        return
    th, tw = tex.shape[:2]
    tx = np.clip((uu * tw).astype(int), 0, tw - 1)
    ty = np.clip((vv * th).astype(int), 0, th - 1)
    lum = tex[ty, tx, 0]
    alpha = tex[ty, tx, 1] * rgba[3] * inside
    region = canvas[y0:y1, x0:x1]
    col = np.stack([lum * rgba[0], lum * rgba[1], lum * rgba[2]], axis=-1)
    a = alpha[..., None]
    if additive:
        region += col * a
    else:
        region *= (1 - a)
        region += col * a


def billboard(centre, axis, across, ha, hc):
    return [centre - axis * ha - across * hc, centre + axis * ha - across * hc,
            centre + axis * ha + across * hc, centre - axis * ha + across * hc]


def ease(t, mode):
    if mode == "in":
        return t * t
    if mode == "out":
        return 1 - (1 - t) * (1 - t)
    return t


# --------------------------------------------------------------------------- dibujo de una instancia
def draw_instance(canvas, cam, inst):
    right, up = cam.r, cam.u
    flat, rest = [], []
    for L in inst.stage["layers"]:
        if not inst.visible(L):
            continue
        (flat if L["type"] in ("telegraph", "ring_wave") else rest).append(L)
    for L in flat:
        if L["type"] == "telegraph":
            draw_telegraph(canvas, cam, inst, L)
        else:
            draw_ring(canvas, cam, inst, L)
    for L in rest:
        if L["type"] == "glow":
            pr = inst.progress(L)
            half = (L["size"][0] + (L["size"][1] - L["size"][0]) * pr) * 0.5
            c = sample_gradient(L["grad"], pr)
            col = (c[0], c[1], c[2], c[3])
            draw_quad(canvas, cam, billboard(inst.anchor(L), right, up, half, half), (0, 0, 1, 1),
                      texture("soft_glow"), col, True)
        elif L["type"] == "slash_arc":
            draw_arc(canvas, cam, inst, L)
    for p in inst.particles:
        draw_particle(canvas, cam, p)


def draw_particle(canvas, cam, p):
    right, up = cam.r, cam.u
    prog = min(1.0, p.age / max(1.0, p.life))
    size = p.s0 + (p.s1 - p.s0) * prog
    if size <= 0.002:
        return
    half = size * 0.5
    ha = hc = half
    if p.tex.endswith("_8f"):
        axis, across = right.copy(), up.copy()
    elif p.tex.startswith("note"):
        ang = (p.phase - 0.5) * 0.63 * math.cos(prog * 6 + p.phase * TAU)
        axis = right * math.cos(ang) + up * math.sin(ang)
        across = up * math.cos(ang) - right * math.sin(ang)
    elif p.tex == "light_beam":
        to_cam = cam.pos - p.pos
        side = v(-to_cam[2], 0, to_cam[0])
        axis = norm(side) if np.linalg.norm(side) > 1e-3 else right.copy()
        across = UP.copy()
        ha = half * 0.25
    else:
        ang = p.phase * TAU
        axis = right * math.cos(ang) + up * math.sin(ang)
        across = up * math.cos(ang) - right * math.sin(ang)
    if p.stretch > 0:
        ar, au = float(np.dot(p.vel, right)), float(np.dot(p.vel, up))
        ln = math.hypot(ar, au)
        if ln > 1e-4:
            axis = right * (ar / ln) + up * (au / ln)
            across = norm(np.cross(np.cross(right, up), axis))
            ha = half * (1 + p.stretch * float(np.linalg.norm(p.vel)) * 1.6)
            hc = half * min(1.0, max(0.45, 0.78 - prog * 0.22))
    u0, u1 = 0.0, 1.0
    if p.tex.endswith("_8f"):
        frame = min(7, max(0, int(prog * 8)))
        u0, u1 = frame / 8, (frame + 1) / 8
    c = sample_gradient(p.grad, prog)
    r, g, b = c[0], c[1], c[2]
    if p.add:
        r, g, b = min(1, r * 1.25), min(1, g * 1.25), min(1, b * 1.25)
    corners = billboard(p.pos, axis, across, ha, hc)
    draw_quad(canvas, cam, corners, (u0, 0, u1, 1), texture(p.tex), (r, g, b, c[3]), p.add)
    if p.core > 0:
        cs = p.core * (0.80 - 0.28 * prog)
        ca = min(1.0, c[3] * (1 - prog) * 0.70)
        if ca > 0.01 and cs > 0.01:
            w = min(1, max(0, 0.40 + 0.55 * (1 - prog)))
            draw_quad(canvas, cam, billboard(p.pos, axis, across, ha * cs, hc * cs), (0, 0, 1, 1),
                      texture("flame_core"), (r + (1 - r) * w, g + (1 - g) * w, b + (1 - b) * w, ca), True)


def draw_annulus(canvas, cam, c, radius, thickness, phase, pulse, additive, col):
    """Espejo de VfxRenderer.addAnnulus: 64 tramos con ribbon_soft de través."""
    if col[3] <= 0.004 or radius <= 0.01:
        return
    inner, outer = max(0.0, radius - thickness * 0.5), radius + thickness * 0.5
    tex = texture("ribbon_soft")
    for i in range(64):
        a0, a1 = TAU * i / 64, TAU * (i + 1) / 64
        k = 1 - pulse + pulse * math.cos(8 * ((a0 + a1) * 0.5 - phase)) if pulse > 0 else 1.0
        d0, d1 = v(math.cos(a0), 0, math.sin(a0)), v(math.cos(a1), 0, math.sin(a1))
        draw_quad(canvas, cam, [c + d0 * inner, c + d1 * inner, c + d1 * outer, c + d0 * outer], (0, 0, 1, 1),
                  tex, (col[0], col[1], col[2], col[3] * max(0.0, k)), additive)


def draw_ring(canvas, cam, inst, L):
    pr = inst.progress(L)
    e = ease(pr, L["ease"])
    radius = L["ring_radius"][0] + (L["ring_radius"][1] - L["ring_radius"][0]) * e
    thickness = L["thickness"][0] + (L["thickness"][1] - L["thickness"][0]) * e
    if L["texture"] == "ring_soft":
        thickness *= 1.6
    c = inst.anchor(L) + v(0, L["y"], 0)
    draw_annulus(canvas, cam, c, radius, thickness, 0, 0, L["additive"], sample_gradient(L["grad"], pr))


def draw_telegraph(canvas, cam, inst, L):
    local = inst.time() - L["at"]
    pr = inst.progress(L)
    fade = min(1.0, max(0.0, local / L["fade_in"])) if L["fade_in"] > 0 else 1.0
    fade *= min(1.0, max(0.0, (L["duration"] - local) / 0.15))
    if fade <= 0.005:
        return
    col = sample_gradient(L["grad"], pr)
    c = inst.anchor(L) + v(0, 0.06 + L["y"], 0)
    if L["fill_alpha"] > 0:
        f = L["radius"] * 1.15
        draw_quad(canvas, cam, [c + v(-f, -0.01, -f), c + v(f, -0.01, -f), c + v(f, -0.01, f), c + v(-f, -0.01, f)],
                  (0, 0, 1, 1), texture("soft_glow"), (col[0], col[1], col[2], min(1, L["fill_alpha"] * fade)), True)
    draw_annulus(canvas, cam, c, L["radius"], L["tel_thickness"], local * L["spin"],
                 0.45 if L["spin"] else 0.0, True, (col[0], col[1], col[2], col[3] * fade))


def draw_arc(canvas, cam, inst, L):
    pr = inst.progress(L)
    head = min(1.0, max(0.0, pr / 0.35))
    if head <= 0:
        return
    col = sample_gradient(L["grad"], pr)
    centre = inst.anchor(L)
    fwd = norm(v(inst.look[0], 0, inst.look[2]))
    right = v(-fwd[2], 0, fwd[0])
    tilt = math.radians(L["tilt"])
    side = right * math.cos(tilt) + v(0, math.sin(tilt), 0)
    arc = math.radians(L["arc_degrees"])
    inner = max(0.05, L["radius"] - L["arc_width"] * 0.5)
    outer = L["radius"] + L["arc_width"] * 0.5
    seg = max(6, int(L["arc_degrees"] / 7.5))
    shown = max(1, round(seg * head))
    tex = texture(L["texture"])
    for i in range(shown):
        s0, s1 = i / seg, (i + 1) / seg
        t0, t1 = arc * 0.5 - s0 * arc, arc * 0.5 - s1 * arc
        d0 = fwd * math.cos(t0) + side * math.sin(t0)
        d1 = fwd * math.cos(t1) + side * math.sin(t1)
        trail = 0.3 + 0.7 * min(1.0, max(0.0, 1 - (head - s1) / 0.6))
        mid = (inner + outer) * 0.5
        h0 = (outer - inner) * 0.5 * (0.25 + 0.75 * math.sin(s0 * math.pi))
        h1 = (outer - inner) * 0.5 * (0.25 + 0.75 * math.sin(s1 * math.pi))
        corners = [centre + d0 * (mid - h0), centre + d1 * (mid - h1), centre + d1 * (mid + h1), centre + d0 * (mid + h0)]
        draw_quad(canvas, cam, corners, (s0, 0, s1, 1), tex, (col[0], col[1], col[2], col[3] * trail), True)


def screen_effects(inst_list, cam):
    """(velo de flash rgba o None, amplitud de shake) en el instante actual."""
    best, color, shake = 0.0, None, 0.0
    for inst in inst_list:
        for L in inst.stage["layers"]:
            if not inst.visible(L):
                continue
            pr = inst.progress(L)
            if L["type"] == "flash":
                env = pr / 0.15 if pr < 0.15 else (1 - pr) * (1 - pr) / 0.7225
                a = L["intensity"] * min(1.0, max(0.0, env))
                if L["scope"] == "world":
                    a *= min(1.0, max(0.0, 1 - np.linalg.norm(inst.anchor(L) - cam.pos) / 48))
                if a > best:
                    best, color = a, L["flash_rgb"]
            elif L["type"] == "shake":
                local = inst.time() - L["at"]
                env = min(1.0, local / 0.05) * min(1.0, (1 - pr) / 0.3)
                fall = 1.0
                if L["falloff"] == "linear":
                    fall = max(0.0, 1 - np.linalg.norm(inst.anchor(L) - cam.pos) / max(1.0, L["radius"]))
                shake += L["amplitude"] * max(0.0, env) * fall
    return (None if color is None else (*color, min(best, 0.85))), shake


# --------------------------------------------------------------------------- escena
def background(cam, w, h, radius):
    canvas = np.zeros((h, w, 3), float)
    canvas[:] = np.linspace(0.10, 0.05, h)[:, None, None] * np.array([0.9, 0.85, 1.2])
    img = Image.fromarray((np.clip(canvas, 0, 1) * 255).astype(np.uint8))
    d = ImageDraw.Draw(img)
    R = int(math.ceil(radius))
    for i in range(-R, R + 1):
        for a, b in (((i, -R), (i, R)), ((-R, i), (R, i))):
            pa, pb = cam.project(v(a[0], 0, a[1])), cam.project(v(b[0], 0, b[1]))
            if pa and pb:
                d.line([pa, pb], fill=(52, 50, 70) if i % 5 else (75, 72, 98), width=1)
    # silueta de 1,8 bloques (0,6 de ancho) y flecha de la mirada (+Z)
    for y0, y1, hw in ((0.0, 0.75, 0.25), (0.75, 1.5, 0.3), (1.5, 1.8, 0.25)):
        quad = [cam.project(v(-hw, y0, 0)), cam.project(v(hw, y0, 0)), cam.project(v(hw, y1, 0)), cam.project(v(-hw, y1, 0))]
        if all(quad):
            d.polygon(quad, fill=(95, 95, 110))
    pa, pb = cam.project(v(0, 0.02, 0)), cam.project(v(0, 0.02, 1.2))
    if pa and pb:
        d.line([pa, pb], fill=(160, 160, 190), width=2)
    return np.asarray(img, dtype=float) / 255.0


def load_effect(effect_id):
    path = RES / "vfx" / f"{effect_id}.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    stages = {}
    for name, raw in data["stages"].items():
        dur = float(raw["duration"])
        stages[name] = {"name": name, "tier": raw.get("tier", "L"), "duration": dur,
                        "at": float((raw.get("trigger") or {}).get("at", 0.0)),
                        "has_trigger": "trigger" in raw,
                        "layers": [layer_defaults(l, dur) for l in raw["layers"]]}
    return stages


def max_reach(stages):
    r = 4.0
    for st in stages:
        for L in st["layers"]:
            r = max(r, float(L.get("radius", 0) or 0), *(L.get("ring_radius", (0, 0))))
    return min(r, 16.0)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("spec", help="<waifu>/<efecto>[#<stage>]")
    ap.add_argument("--frames", type=int, default=8)
    ap.add_argument("--size", default="360x270", help="tamaño de cada vista (ancho x alto)")
    ap.add_argument("--out", help="PNG de salida (por defecto en agents-bus/captures/)")
    ap.add_argument("--gif", action="store_true", help="además, GIF de la vista 3/4 a 20 fps")
    ap.add_argument("--seed", type=int, default=7)
    args = ap.parse_args()

    effect_id, _, stage_name = args.spec.partition("#")
    stages = load_effect(effect_id)
    if stage_name:
        if stage_name not in stages:
            sys.exit(f"vfx_preview: el stage '{stage_name}' no existe (hay: {', '.join(stages)})")
        chosen = [(stages[stage_name], 0.0)]
    else:
        chosen = [(s, s["at"]) for s in stages.values() if s["has_trigger"]] or [(s, 0.0) for s in stages.values()]
    rng = random.Random(args.seed)
    insts = [Instance(s, delay, rng) for s, delay in chosen]
    total = max(delay + s["duration"] for s, delay in chosen)
    total_ticks = int(math.ceil(total * TPS)) + 6
    w, h = (int(x) for x in args.size.lower().split("x"))
    reach = max_reach([s for s, _ in chosen])
    cams = [("3/4", Camera(v(-reach * 0.75, reach * 0.55 + 1.5, reach * 1.05), v(0, 1.0, reach * 0.15), UP, w, h)),
            ("cenital", Camera(v(0, reach * 2.2 + 2, 0.001), v(0, 0, 0), v(0, 0, 1), w, h))]
    bgs = [background(cam, w, h, reach) for _, cam in cams]
    sheet_ticks = sorted({max(1, round(total_ticks * (i + 0.5) / args.frames)) for i in range(args.frames)})
    gif_frames, cells = [], []

    for tick in range(1, total_ticks + 1):
        for inst in insts:
            inst.tick()
        want_cell = tick in sheet_ticks
        if not want_cell and not args.gif:
            continue
        row = []
        for idx, (label, cam) in enumerate(cams):
            if not want_cell and idx > 0:
                break
            canvas = bgs[idx].copy()
            for inst in insts:
                draw_instance(canvas, cam, inst)
            veil, shake = screen_effects(insts, cam)
            if veil:
                canvas = canvas * (1 - veil[3]) + np.array(veil[:3]) * veil[3]
            img = Image.fromarray((np.clip(canvas, 0, 1) * 255).astype(np.uint8))
            d = ImageDraw.Draw(img)
            n = sum(len(i.particles) for i in insts)
            text = f"{label}  t={tick / TPS:.2f}s  {n} part."
            if shake > 0.005:
                text += f"  shake {shake:.2f}"
            d.text((6, 4), text, fill=(230, 230, 240))
            row.append(img)
            if idx == 0 and args.gif:
                gif_frames.append(img)
        if want_cell:
            cells.append(row)

    name = effect_id.replace("/", "_") + (f"_{stage_name}" if stage_name else "")
    out = Path(args.out) if args.out else CAPTURES / f"{name}.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    pad = 4
    sheet = Image.new("RGB", (len(cells) * (w + pad) + pad, len(cams) * (h + pad) + pad), (12, 10, 18))
    for c, row in enumerate(cells):
        for r, img in enumerate(row):
            sheet.paste(img, (pad + c * (w + pad), pad + r * (h + pad)))
    sheet.save(out)
    print(f"vfx_preview: {out}  ({len(cells)} fotogramas, {total:.2f} s, stages: "
          f"{', '.join(f'{s['name']}@{d:.2f}' for s, d in chosen)})")
    if args.gif and gif_frames:
        gif = out.with_suffix(".gif")
        gif_frames[0].save(gif, save_all=True, append_images=gif_frames[1:], duration=50, loop=0)
        print(f"vfx_preview: {gif}")


if __name__ == "__main__":
    main()
