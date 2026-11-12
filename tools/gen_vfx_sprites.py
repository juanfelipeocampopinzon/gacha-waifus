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
