#!/usr/bin/env python3
"""Genera los sprites base de VFX ("pixel con luz"): escala de grises en 3 niveles + alfa.

El motor los tiñe con el degradado de cada efecto, así un mismo sprite sirve para todas las waifus.
Niveles: 255 = núcleo, 170 = medio, 90 = borde (bandas duras = aspecto pixel-art).
La llama es una tira horizontal de 8 cuadros de 16x16 (128x16) que hace bucle perfecto.

Uso:  python3 tools/gen_vfx_sprites.py [--out DIR] [--seed N] [--preview]
Requiere Pillow (pip install pillow). Es determinista: misma semilla, mismos PNG.
"""
import argparse
import math
import random
from pathlib import Path

from PIL import Image

CORE, MID, EDGE = 255, 170, 90
ROOT = Path(__file__).resolve().parent.parent
DEFAULT_OUT = ROOT / "common/src/main/resources/assets/gachawaifus/textures/vfx"


def band(v):
    """Intensidad 0..1 -> (gris, alfa) en 3 bandas duras; None = transparente."""
    if v >= 0.72:
        return CORE
    if v >= 0.46:
        return MID
    if v >= 0.22:
        return EDGE
    return None


def make(w, h, fn):
    img = Image.new("LA", (w, h), (0, 0))
    for y in range(h):
        for x in range(w):
            g = band(fn(x, y))
            if g is not None:
                img.putpixel((x, y), (g, 255))
    return img


# ---------- ruido de valor con envoltura (bucle perfecto) ----------
class WrapNoise:
    def __init__(self, seed, nx, ny):
        r = random.Random(seed)
        self.nx, self.ny = nx, ny
        self.g = [[r.random() for _ in range(ny)] for _ in range(nx)]

    @staticmethod
    def _s(t):
        return t * t * (3 - 2 * t)

    def at(self, x, y):
        x0, y0 = math.floor(x), math.floor(y)
        fx, fy = self._s(x - x0), self._s(y - y0)
        def v(i, j):
            return self.g[i % self.nx][j % self.ny]
        a = v(x0, y0) * (1 - fx) + v(x0 + 1, y0) * fx
        b = v(x0, y0 + 1) * (1 - fx) + v(x0 + 1, y0 + 1) * fx
        return a * (1 - fy) + b * fy


def flame_strip(seed, frames=8, size=16):
    n1 = WrapNoise(seed, 6, 4)          # periodo vertical: 4 celdas = 16 px
    n2 = WrapNoise(seed + 1, 12, 8)
    sway = WrapNoise(seed + 2, 2, 4)    # balanceo lateral que sube con la llama
    out = Image.new("LA", (size * frames, size), (0, 0))
    for f in range(frames):
        scroll = size * f / frames       # sube exactamente un periodo en 'frames' cuadros
        phase = 2 * math.pi * f / frames
        def fn(x, y):
            yy = y / (size - 1)          # 0 arriba .. 1 abajo
            ys = (y + scroll) / size
            # la linea central serpentea mas arriba que abajo
            wob = (sway.at(0.5, ys * 4) - 0.5) * 5.0 * (1.0 - yy) ** 0.7
            wob += math.sin(phase + yy * 3.2) * 0.9 * (1.0 - yy)
            cx = (x - (size - 1) / 2 - wob) / (size / 2)
            n = 0.6 * n1.at(x / size * 6, ys * 4) + 0.4 * n2.at(x / size * 12, ys * 8)
            width = (0.10 + 0.50 * (yy ** 0.9)) * (0.85 + 0.5 * n) * (1.0 + 0.07 * math.sin(phase))
            dist = abs(cx) / max(width, 0.04)
            shape = 1.0 - dist ** 1.5
            if yy > 0.86:                # redondea la base
                shape *= max(0.0, 1.0 - (yy - 0.86) * 7)
            # las puntas se rompen en lenguas: el ruido recorta lo alto
            shape -= (1.0 - yy) ** 1.3 * (0.55 - n) * 0.9
            body = shape * (0.40 + 0.85 * n)
            core = max(0.0, 1.0 - dist * 2.1) * (0.10 + 0.95 * yy ** 1.6) * (0.75 + 0.5 * n)
            return max(body * 0.88, core * 1.05)
        frame = make(size, size, fn)
        out.paste(frame, (f * size, 0))
    return out


def flame_puff_strip(seed, frames=8, size=16):
    """Nube de llama redonda que se deshilacha: fotograma 0 = bola compacta y caliente,
    7 = jirones. Pensada para dibujarse estirada en la dirección del chorro y recorrer la
    tira con la edad (parámetro `frame_by_age` del motor, implícito en las texturas *_8f)."""
    n1 = WrapNoise(seed + 11, 6, 6)
    n2 = WrapNoise(seed + 12, 12, 12)
    out = Image.new("LA", (size * frames, size), (0, 0))
    for f in range(frames):
        t = f / (frames - 1)
        body = 0.92 * (0.82 + 0.30 * math.sin(t * math.pi * 0.9)) * (1.0 - 0.30 * t)
        core = 0.62 * (1.0 - 0.55 * t)

        def fn(x, y, t=t, body=body, core=core):
            cx = (x - (size - 1) / 2) / (size / 2)
            cy = (y - (size - 1) / 2) / (size / 2)
            d = math.hypot(cx, cy)
            n = (0.55 * n1.at(x / size * 6, y / size * 6)
                 + 0.45 * n2.at(x / size * 12, y / size * 12))
            edge = body * (0.70 + 0.55 * n)          # borde irregular, no un círculo
            shape = 1.0 - (d / max(edge, 0.02)) ** 2.1
            shape -= t * 0.85 * (1.0 - n) ** 2       # la edad abre agujeros
            hot = 1.0 - (d / max(core * (0.85 + 0.30 * n), 0.02)) ** 2.0
            return max(shape * (0.75 + 0.45 * n), hot * (1.05 - 0.45 * t))

        out.paste(make(size, size, fn), (f * size, 0))
    return out


def flame_core(size=16):
    """Núcleo caliente: disco casi lleno del nivel 255. El motor lo dibuja dentro de cada
    partícula de llama (clave `core`) para dar volumen sin una capa más de partículas."""
    c = (size - 1) / 2

    def fn(x, y):
        d = math.hypot(x - c, y - c) / (size / 2)
        return 1.15 - d ** 1.2 * 1.15
    return make(size, size, fn)


# ---------- sprites sueltos ----------
def soft_glow(size=32):
    c = (size - 1) / 2
    return make(size, size, lambda x, y: 1.05 - math.hypot(x - c, y - c) / (size / 2))


def star4(size=16):
    c = (size - 1) / 2
    def fn(x, y):
        dx, dy = abs(x - c) / (size / 2), abs(y - c) / (size / 2)
        return 1.2 - (dx ** 0.5 + dy ** 0.5) ** 1.3 * 0.62
    return make(size, size, fn)


def spark(size=8):
    c = (size - 1) / 2
    def fn(x, y):
        dx, dy = abs(x - c) / (size / 2), abs(y - c) / (size / 2)
        return 1.0 - (dx + dy) * 0.9
    return make(size, size, fn)


def mote(size=4):
    c = (size - 1) / 2
    return make(size, size, lambda x, y: 1.0 - math.hypot(x - c, y - c) / (size / 1.6))


def orb(size=16):
    c = (size - 1) / 2
    def fn(x, y):
        d = math.hypot(x - c, y - c) / (size / 2)
        v = 1.1 - d
        # punto de brillo arriba a la izquierda
        if math.hypot(x - (c - 2.5), y - (c - 2.5)) < 1.6:
            v = 1.0
        return v
    return make(size, size, fn)


def petal(size=8):
    def fn(x, y):
        u, v = (x - 3.5) / 3.5, (y - 3.5) / 3.5
        # elipse girada 35 grados
        a = math.radians(35)
        ru, rv = u * math.cos(a) + v * math.sin(a), -u * math.sin(a) + v * math.cos(a)
        return 1.15 - math.sqrt((ru / 0.85) ** 2 + (rv / 1.1) ** 2) * 0.85
    return make(size, size, fn)


def shard(w=8, h=16):
    def fn(x, y):
        u, v = abs(x - (w - 1) / 2) / (w / 2), abs(y - (h - 1) / 2) / (h / 2)
        return 1.15 - (u * 0.95 + v * 0.8)
    return make(w, h, fn)


def smoke(seed, size=16):
    n = WrapNoise(seed, 5, 5)
    c = (size - 1) / 2
    def fn(x, y):
        d = math.hypot(x - c, y - c) / (size / 2)
        return (1.05 - d) * (0.55 + 0.9 * n.at(x / size * 5, y / size * 5))
    return make(size, size, fn)


def ring(size=32, thick=0.16, soft=False):
    c = (size - 1) / 2
    def fn(x, y):
        d = math.hypot(x - c, y - c) / (size / 2)
        t = 1.0 - abs(d - 0.82) / (thick * (1.8 if soft else 1.0))
        return t
    return make(size, size, fn)


def streak(w=32, h=4):
    def fn(x, y):
        u = x / (w - 1)
        v = abs(y - (h - 1) / 2) / (h / 2)
        return (0.2 + 0.95 * u) * (1.1 - v)
    return make(w, h, fn)


def arc_streak(w=32, h=8):
    def fn(x, y):
        u = x / (w - 1)
        v = abs(y - (h - 1) / 2) / (h / 2)
        return (0.15 + 1.0 * math.sin(u * math.pi) ** 0.8) * (1.15 - v * 1.1)
    return make(w, h, fn)


def ribbon_soft(w=16, h=8):
    def fn(x, y):
        v = abs(y - (h - 1) / 2) / (h / 2)
        return 1.2 - v * 1.15
    return make(w, h, fn)


# ---------- notas musicales y haz de foco (Miku y cualquier waifu "de escenario") ----------
def _capsule(px, py, ax, ay, bx, by, r):
    """Distancia con signo de un punto a un trazo grueso (cápsula) de radio r."""
    dx, dy = bx - ax, by - ay
    t = max(0.0, min(1.0, ((px - ax) * dx + (py - ay) * dy) / max(1e-6, dx * dx + dy * dy)))
    return math.hypot(px - (ax + dx * t), py - (ay + dy * t)) - r


def _head(px, py, cx, cy, rx=3.1, ry=2.2, angle=-25):
    """Distancia aproximada a la cabeza ovalada (inclinada) de una nota."""
    a = math.radians(angle)
    u, v = px - cx, py - cy
    ru, rv = u * math.cos(a) - v * math.sin(a), u * math.sin(a) + v * math.cos(a)
    return (math.hypot(ru / rx, rv / ry) - 1.0) * min(rx, ry)


def _glyph(sdf):
    """Distancia con signo -> 3 bandas: núcleo dentro, borde medio en el contorno, halo fuera."""
    def fn(x, y):
        d = sdf(x + 0.5, y + 0.5)
        if d < -0.6:
            return 1.0
        if d < 0.2:
            return 0.6
        if d < 1.3:
            return 0.3
        return 0.0
    return fn


def note(size=16):
    """Corchea: cabeza abajo a la izquierda, plica a la derecha y banderín que cae."""
    def sdf(x, y):
        flag = min(_capsule(x, y, 9.4, 2.0, 12.6, 5.0, 0.9),
                   _capsule(x, y, 12.6, 5.0, 12.8, 8.6, 0.8))
        return min(_head(x, y, 6.0, 12.0), _capsule(x, y, 8.8, 2.0, 8.8, 11.4, 0.75), flag)
    return make(size, size, _glyph(sdf))


def note_double(size=16):
    """Dos semicorcheas unidas por dos barras."""
    def sdf(x, y):
        heads = min(_head(x, y, 3.6, 12.6, 2.6, 1.9), _head(x, y, 11.4, 11.4, 2.6, 1.9))
        stems = min(_capsule(x, y, 5.9, 2.6, 5.9, 12.0, 0.7), _capsule(x, y, 13.7, 1.6, 13.7, 10.8, 0.7))
        beams = min(_capsule(x, y, 5.9, 2.6, 13.7, 1.6, 1.0), _capsule(x, y, 5.9, 5.4, 13.7, 4.4, 0.8))
        return min(heads, stems, beams)
    return make(size, size, _glyph(sdf))


def light_beam(w=8, h=32):
    """Haz vertical de foco: línea central caliente, bordes suaves y extremos que se desvanecen.
    El motor lo dibuja siempre vertical y 4 veces más alto que ancho (textura light_beam)."""
    def fn(x, y):
        u = abs(x - (w - 1) / 2) / (w / 2)
        v = y / (h - 1)
        ends = min(1.0, v / 0.12) * min(1.0, (1.0 - v) / 0.25)
        return (1.15 - u * 1.25) * (0.55 + 0.6 * ends) * min(1.0, ends * 2.5)
    return make(w, h, fn)


# ---------- reloj (Zafkiel de Kurumi; vale para cualquier efecto de "tiempo") ----------
def clock_face(size=64):
    """Esfera: doble aro exterior, aro interior fino, 12 marcas (las de 12/3/6/9 dobles y con
    rombo) y un botón central. Las agujas son sprites aparte (clock_hand, clock_hand_short)."""
    c = (size - 1) / 2

    def sdf(x, y):
        dx, dy = x - c, y - c
        r = math.hypot(dx, dy) / (size / 2)          # 0 centro .. 1 borde
        ang = math.atan2(dx, -dy) % math.tau           # 0 = arriba, sentido horario
        d = min(abs(r - 0.94) - 0.035, abs(r - 0.86) - 0.018, abs(r - 0.62) - 0.012)
        k = round(ang / (math.tau / 12)) % 12
        off = abs(((ang - k * math.tau / 12) + math.pi) % math.tau - math.pi) * r   # distancia angular
        main = k % 3 == 0
        tick = max(off - (0.035 if main else 0.018), abs(r - (0.74 if main else 0.77)) - (0.09 if main else 0.06))
        d = min(d, tick)
        if main:                                       # rombo en las cuatro principales
            d = min(d, (off + abs(r - 0.66)) - 0.04)
        d = min(d, r - 0.055)                          # botón central
        return d * size / 2

    return make(size, size, _glyph(sdf))


def clock_hand(size=64, length=0.84, width=1.4):
    """Aguja desde el centro hacia arriba con punta de rombo y contrapeso corto."""
    c = (size - 1) / 2
    tip = c - length * size / 2

    def sdf(x, y):
        shaft = _capsule(x, y, c, c + 0.12 * size / 2, c, tip + 3.5, width * 0.5)
        diamond = (abs(x - c) / 2.4 + abs(y - (tip + 3.5)) / 4.0 - 1.0) * 2.0
        return min(shaft, diamond)

    return make(size, size, _glyph(sdf))


def solid(size=4):
    """Blanco liso: superficie de las esferas y cúpulas (el motor la tiñe y le pone borde)."""
    return make(size, size, lambda x, y: 1.0)


def sigil(size=64):
    """Sello de disparo: aro doble, estrella de 4 puntas que lo atraviesa y 4 muescas en las
    diagonales. Se dibuja de pie delante del arma (decal vertical)."""
    c = (size - 1) / 2

    def sdf(x, y):
        dx, dy = (x - c) / (size / 2), (y - c) / (size / 2)
        r = math.hypot(dx, dy)
        ang = math.atan2(dx, -dy) % math.tau
        d = min(abs(r - 0.70) - 0.045, abs(r - 0.58) - 0.015)
        ax, ay = abs(dx), abs(dy)
        star = (ax ** 0.5 + ay ** 0.5) - 0.62          # estrella cóncava de 4 puntas
        d = min(d, star * 0.35)
        k = round((ang - math.tau / 8) / (math.tau / 4))
        off = abs(((ang - math.tau / 8 - k * math.tau / 4) + math.pi) % math.tau - math.pi) * r
        d = min(d, max(off - 0.03, abs(r - 0.82) - 0.07))
        return d * size / 2

    return make(size, size, _glyph(sdf))


def spike_burst(size=64, seed=5):
    """Halo erizado: corona con púas irregulares hacia fuera y hueco en el centro (el colapso
    del agujero negro y los impactos fuertes)."""
    c = (size - 1) / 2
    r0 = random.Random(seed)
    spikes = [((i + r0.random() * 0.6) * math.tau / 36, 0.10 + r0.random() ** 2 * 0.30, 0.04 + r0.random() * 0.05)
              for i in range(36)]

    def fn(x, y):
        dx, dy = (x - c) / (size / 2), (y - c) / (size / 2)
        r = math.hypot(dx, dy)
        ang = math.atan2(dy, dx)
        reach = 0.58
        for a, length, width in spikes:
            off = abs((ang - a + math.pi) % math.tau - math.pi)
            if off < width:
                reach = max(reach, 0.58 + length * (1 - off / width))
        if r > reach:
            return 0.0
        if r < 0.46:
            return 0.0 if r < 0.40 else 0.3
        return 1.0 if r < 0.56 else 0.6 if r < reach - 0.04 else 0.3

    return make(size, size, fn)


def _segment_dist(px, py, ax, ay, bx, by):
    vx, vy = bx - ax, by - ay
    t = max(0.0, min(1.0, ((px - ax) * vx + (py - ay) * vy) / (vx * vx + vy * vy)))
    return math.hypot(px - (ax + vx * t), py - (ay + vy * t))


def ice_cracks(size=128, seed=11):
    """Grietas de hielo en el suelo: 9 ramas quebradas que salen del centro, con brotes, y un
    cráter pequeño en medio. Se dibuja tumbada (decal ground)."""
    rng = random.Random(seed)
    c = size / 2
    segs = []
    for i in range(12):
        ang = (i + rng.random() * 0.6) * math.tau / 12
        x, y, r = c, c, 0.0
        width = 0.75
        while r < size * 0.47:
            step = size * (0.06 + rng.random() * 0.05)
            ang += (rng.random() - 0.5) * 0.7
            nx, ny = x + math.cos(ang) * step, y + math.sin(ang) * step
            segs.append((x, y, nx, ny, width))
            if rng.random() < 0.35:
                ba = ang + (rng.random() - 0.5) * 1.8
                bl = step * (0.6 + rng.random())
                segs.append((nx, ny, nx + math.cos(ba) * bl, ny + math.sin(ba) * bl, width * 0.6))
            x, y = nx, ny
            r = math.hypot(x - c, y - c)
            width = max(0.3, width * 0.86)

    def fn(px, py):
        px, py = px + 0.5, py + 0.5
        d = min(_segment_dist(px, py, a, b, e, f) - w for a, b, e, f, w in segs)
        crater = abs(math.hypot(px - c, py - c) - size * 0.05) - 0.6
        d = min(d, crater)
        return 1.0 if d < -0.2 else 0.6 if d < 0.5 else 0.3 if d < 1.4 else 0.0

    return make(size, size, fn)


def cut_lines(size=64):
    """Líneas de corte en el suelo: cuatro tajos largos y finos que se cruzan, con las puntas
    afiladas (el rastro de muchos cortes de espada)."""
    lines = [(0.04, 0.22, 0.96, 0.40), (0.10, 0.92, 0.70, 0.08), (0.02, 0.70, 0.98, 0.78), (0.45, 0.98, 0.95, 0.12)]

    def fn(px, py):
        x, y = (px + 0.5) / size, (py + 0.5) / size
        best = 0.0
        for ax, ay, bx, by in lines:
            vx, vy = bx - ax, by - ay
            t = max(0.0, min(1.0, ((x - ax) * vx + (y - ay) * vy) / (vx * vx + vy * vy)))
            d = math.hypot(x - (ax + vx * t), y - (ay + vy * t)) * size
            w = 1.2 * math.sin(math.pi * t) ** 0.6
            best = max(best, 1.0 if d < w * 0.45 else 0.6 if d < w else 0.3 if d < w + 0.9 else 0.0)
        return best

    return make(size, size, fn)


def rune_ring(size=128):
    """Círculo mágico (los que Megumin apila en el cielo): aro exterior grueso, aro medio y aro
    interior finos, 24 radios entre el medio y el exterior, 8 radios cortos en el interior y una
    estrella de 4 puntas en el centro. El interior es TRANSPARENTE a propósito: si se rellena, con
    blend aditivo se lee como un plato macizo en vez de un círculo, y cuatro apilados tapan el cielo.
    """
    c = (size - 1) / 2
    tau24 = math.tau / 24
    tau8 = math.tau / 8

    def fn(x, y):
        dx, dy = (x - c) / (size / 2), (y - c) / (size / 2)
        r = math.hypot(dx, dy)
        if r > 1.0:
            return 0.0
        ang = math.atan2(dy, dx) % math.tau
        # Aros concéntricos: exterior grueso, medio y interior finos.
        if 0.895 <= r <= 0.975:
            return 1.0
        if 0.735 <= r <= 0.760:
            return 0.66
        if 0.560 <= r <= 0.578:
            return 0.52
        # 24 radios entre el aro medio y el exterior (los "dientes" del círculo).
        if 0.760 < r < 0.895:
            off = abs((ang % tau24) - tau24 / 2)
            if off * r < 0.010:
                return 0.60
            return 0.0
        # 8 radios cortos entre el aro interior y el medio.
        if 0.578 < r < 0.735:
            off = abs((ang % tau8) - tau8 / 2)
            if off * r < 0.012:
                return 0.44
            return 0.0
        # Estrella de 4 puntas en el hueco central.
        ax, ay = abs(dx), abs(dy)
        if (ax ** 0.5 + ay ** 0.5) - 0.40 < 0.0 and r < 0.46:
            return 0.50
        if 0.300 <= r <= 0.318:
            return 0.40
        return 0.0

    return make(size, size, fn)


def scorch(size=128, seed=21):
    """Hollín del cráter: mancha irregular con grietas radiales transparentes. Se dibuja tumbada
    (decal ground) con blend alfa y un degradado oscuro."""
    noise = WrapNoise(seed, 8, 8)

    def fn(x, y):
        dx, dy = (x - (size - 1) / 2) / (size / 2), (y - (size - 1) / 2) / (size / 2)
        r = math.hypot(dx, dy)
        if r > 1.0:
            return 0.0
        edge = 0.70 + 0.22 * noise.at(dx * 3.0 + 5.0, dy * 3.0 + 5.0)
        if r > edge:
            return 0.0
        if r > 0.22:
            ang = math.atan2(dy, dx) % math.tau
            k = round(ang / (math.tau / 16))
            off = abs(((ang - k * math.tau / 16) + math.pi) % math.tau - math.pi)
            if off * max(r, 0.001) < 0.010:
                return 0.0
        if r < 0.18:
            return 1.0
        if r < 0.42:
            return 0.62
        return 0.34

    return make(size, size, fn)


def build(seed):
    return {
        "flame_8f": flame_strip(seed),
        "flame_puff_8f": flame_puff_strip(seed),
        "flame_core": flame_core(),
        "soft_glow": soft_glow(),
        "star4": star4(),
        "spark": spark(),
        "mote": mote(),
        "orb": orb(),
        "petal": petal(),
        "shard": shard(),
        "smoke": smoke(seed + 7),
        "ring_thin": ring(32, 0.10),
        "ring_soft": ring(32, 0.16, soft=True),
        "streak": streak(),
        "arc_streak": arc_streak(),
        "ribbon_soft": ribbon_soft(),
        "note": note(),
        "note_double": note_double(),
        "light_beam": light_beam(),
        "clock_face": clock_face(),
        "clock_hand": clock_hand(),
        "clock_hand_short": clock_hand(length=0.52, width=2.0),
        "solid": solid(),
        "sigil": sigil(),
        "spike_burst": spike_burst(),
        "ice_cracks": ice_cracks(),
        "cut_lines": cut_lines(),
        "rune_ring": rune_ring(),
        "scorch": scorch(),
    }


def tint(img, ramp):
    """Aplica un degradado de 3 colores (EDGE, MID, CORE) para previsualizar."""
    out = Image.new("RGBA", img.size, (0, 0, 0, 0))
    m = {EDGE: ramp[0], MID: ramp[1], CORE: ramp[2]}
    for y in range(img.height):
        for x in range(img.width):
            g, a = img.getpixel((x, y))
            if a:
                out.putpixel((x, y), m[g] + (255,))
    return out


def preview(sprites, path, scale=6):
    ramps = {
        "burnice": ((194, 24, 91), (255, 154, 46), (255, 246, 200)),
        "frost": ((60, 120, 200), (130, 210, 255), (240, 252, 255)),
    }
    bg = (24, 20, 36, 255)
    pad = 6
    rows = []
    for name, ramp in ramps.items():
        rows.append([(k, tint(v, ramp)) for k, v in sprites.items()])
    width = max(sum(i.width * scale + pad for _, i in r) for r in rows) + pad
    height = sum(max(i.height for _, i in r) * scale + pad for r in rows) + pad
    sheet = Image.new("RGBA", (width, height), bg)
    y = pad
    for r in rows:
        x = pad
        for _, img in r:
            big = img.resize((img.width * scale, img.height * scale), Image.NEAREST)
            sheet.alpha_composite(big, (x, y))
            x += big.width + pad
        y += max(i.height for _, i in r) * scale + pad
    sheet.save(path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", default=str(DEFAULT_OUT))
    ap.add_argument("--seed", type=int, default=1337)
    ap.add_argument("--preview", help="ruta del PNG de hoja de previsualización")
    args = ap.parse_args()
    out = Path(args.out)
    out.mkdir(parents=True, exist_ok=True)
    sprites = build(args.seed)
    for name, img in sprites.items():
        img.save(out / f"{name}.png")
    print(f"gen_vfx_sprites: {len(sprites)} sprites en {out}")
    if args.preview:
        preview(sprites, args.preview)
        print(f"previsualización: {args.preview}")


if __name__ == "__main__":
    main()
