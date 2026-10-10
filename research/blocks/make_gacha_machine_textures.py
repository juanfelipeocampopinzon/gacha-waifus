# -*- coding: utf-8 -*-
"""Texturas de la Maquina Gacha (placeholder programatico; el arte definitivo lo aporta el humano).

Genera 8 PNG de 16x16 en textures/block/ siguiendo la foto de referencia:
cuerpo rosa claro, acentos rosa fuerte, panel blanco con marco, cúpula de cristal,
cápsula gashapon (mitad rosa / mitad blanca) y cartel superior con frutas.
"""
import os
import random

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "gachawaifus", "textures", "block")
os.makedirs(OUT, exist_ok=True)
random.seed(21)

PINK = (248, 168, 199, 255)
HOT = (232, 95, 145, 255)
HOT_D = (214, 70, 128, 255)
WHITE = (252, 247, 249, 255)


def blank(color):
    return Image.new("RGBA", (16, 16), color)


def save(img, name):
    img.save(os.path.join(OUT, name))
    print("ok", name)


# 1) plastico rosa claro del cuerpo
img = blank(PINK)
for y in range(16):
    for x in range(16):
        d = random.randint(-6, 6) - (10 if y >= 12 else 0)
        img.putpixel((x, y), (PINK[0] + d, PINK[1] + d, PINK[2] + d, 255))
save(img, "gacha_machine_pink.png")

# 2) panel blanco con marco rosa integrado (la cara frontal muestrea uv [0,0,14,5])
img = blank(WHITE)
for y in range(16):
    for x in range(16):
        d = random.randint(-3, 3)
        img.putpixel((x, y), (WHITE[0] + d, WHITE[1] + d, WHITE[2] + d, 255))
for x in range(0, 14):
    img.putpixel((x, 0), HOT)
    img.putpixel((x, 4), HOT)
for y in range(0, 5):
    img.putpixel((0, y), HOT)
    img.putpixel((13, y), HOT)
save(img, "gacha_machine_white.png")

# 3) cristal de la cúpula (translúcido, brillo diagonal)
img = blank((255, 255, 255, 36))
for i in range(16):
    img.putpixel((i, 0), (255, 255, 255, 110))
    img.putpixel((i, 15), (255, 255, 255, 110))
    img.putpixel((0, i), (255, 255, 255, 110))
    img.putpixel((15, i), (255, 255, 255, 110))
for i in range(3, 9):
    img.putpixel((i, 11 - i), (255, 255, 255, 95))
    img.putpixel((i + 1, 11 - i), (255, 255, 255, 95))
save(img, "gacha_machine_glass.png")

# 4) ranura de monedas (linea oscura; uv [0,0,4,1])
img = blank(HOT)
for x in range(16):
    img.putpixel((x, 0), (74, 58, 70, 255))
    img.putpixel((x, 1), (48, 38, 46, 255))
save(img, "gacha_machine_slot.png")

# 5) hueco de salida (marco rosa + centro oscuro; uv [0,0,4,4])
img = blank((40, 32, 38, 255))
for x in range(4):
    img.putpixel((x, 0), HOT)
    img.putpixel((x, 3), HOT)
    img.putpixel((0, x), HOT)
    img.putpixel((3, x), HOT)
save(img, "gacha_machine_hole.png")

# 6) pomo giratorio (rosa con centro oscuro; uv [0,0,4,4])
img = blank(HOT)
for x in range(2, 4):
    for y in range(2, 4):
        img.putpixel((x, y), HOT_D)
save(img, "gacha_machine_knob.png")

# 7) capsula gashapon (mitad rosa / mitad blanca con linea oscura)
img = blank((250, 250, 250, 255))
for y in range(0, 7):
    for x in range(16):
        img.putpixel((x, y), (245, 125, 168, 255))
for x in range(16):
    img.putpixel((x, 7), (125, 112, 118, 255))
img.putpixel((3, 2), (255, 235, 242, 255))
img.putpixel((4, 1), (255, 235, 242, 255))
save(img, "gacha_machine_capsule.png")

# 8) cartel superior: frutas y lunares sobre blanco (uv [0,0,16,9])
img = blank(WHITE)
for y in range(16):
    for x in range(16):
        d = random.randint(-3, 3)
        img.putpixel((x, y), (WHITE[0] + d, WHITE[1] + d, WHITE[2] + d, 255))
DOT = (240, 130, 170, 255)
for x, y in [(0, 0), (6, 0), (11, 0), (15, 0), (3, 6), (8, 7), (13, 6), (0, 8), (15, 8)]:
    img.putpixel((x, y), DOT)
# naranja
for x in range(1, 4):
    for y in range(2, 5):
        img.putpixel((x, y), (255, 150, 42, 255))
img.putpixel((2, 3), (255, 185, 90, 255))
# fresa: copa verde + cuerpo rojo
for x in range(5, 8):
    img.putpixel((x, 1), (70, 175, 80, 255))
for x in range(5, 8):
    for y in range(2, 5):
        img.putpixel((x, y), (235, 55, 85, 255))
img.putpixel((6, 3), (255, 240, 244, 255))
# sandia: rojo con borde verde
for x in range(9, 13):
    for y in range(3, 5):
        img.putpixel((x, y), (245, 80, 95, 255))
for x in range(9, 13):
    img.putpixel((x, 5), (95, 190, 80, 255))
# manzana verde
for x in range(13, 16):
    for y in range(2, 5):
        img.putpixel((x, y), (135, 200, 80, 255))
img.putpixel((14, 3), (110, 175, 65, 255))
save(img, "gacha_machine_sign.png")

print("listo ->", OUT)
