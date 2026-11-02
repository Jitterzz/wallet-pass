"""Shared setup and building blocks for Pass Studio's page art (1032 x 2240, the whole pass page).

Each theme has a scene band (SW x SH, drawn in its own coordinates) that sits at the bottom of the page, and a
sky above it with decorations kept to the edges, away from the logo, name and barcode. The top (status bar and
the back, star and palette buttons) stays plain sky.
"""
import math
import os
import random

HERE = os.path.dirname(os.path.abspath(__file__))
SW, SH = 1032, 860              # the scene band
TALL_W, TALL_H = 1032, 2240     # the page
BAND_Y = TALL_H - SH            # where the band starts on the page
TOP_CLEAR = 270                 # nothing but sky above this

exec(open(os.path.join(HERE, "scene_class.py.txt")).read())


# ---------- where the moving parts are ----------
# Drawing records where things the app animates sit (chimneys, flames, lights, water...), in the drawing's own
# coordinates; render.py moves them onto the page and writes them out for the app.
def anchor(s, kind, x, y, v=0.0):
    if not hasattr(s, "anchors"):
        s.anchors = []
    s.anchors.append((kind, float(x), float(y), float(v)))


def rgb(c):
    """A #rrggbb color as a number (it fits a float exactly)."""
    return float(int(c.lstrip("#")[:6], 16))


_sun, _candle, _water, _aurora = S.sun, S.candle, S.water, S.aurora


def _rec_sun(self, x, y, r, *a, **k):
    anchor(self, "sun", x, y, r)
    return _sun(self, x, y, r, *a, **k)


def _rec_candle(self, x, base, h, w, *a, **k):
    anchor(self, "flame", x, base - h - w * 0.8, w)
    return _candle(self, x, base, h, w, *a, **k)


def _rec_water(self, y, *a, **k):
    anchor(self, "water", 0, y, 0)
    return _water(self, y, *a, **k)


def _rec_aurora(self, colors, y=170, a=0.75):
    anchor(self, "aurora", 0, y, a)
    return _aurora(self, colors, y, a)


def _rec_skyline(self, base, color, lit="#ffd27a", seed_density=0.35, heights=(90, 260)):
    """S.skyline, also noting some lit windows (with their building's color) so a few can switch off and on."""
    x = -10
    while x < SW + 10:
        w = self.r.uniform(48, 96)
        h = self.r.uniform(*heights)
        self.add(f'<rect x="{x:.1f}" y="{base - h:.1f}" width="{w:.1f}" height="{h + 2:.1f}" fill="{color}"/>')
        if self.r.random() < 0.25:
            self.add(f'<rect x="{x + w * 0.42:.1f}" y="{base - h - 26:.1f}" width="3" height="26" fill="{color}"/>')
        for wy in range(int(base - h + 14), int(base - 12), 18):
            for wx in range(int(x + 9), int(x + w - 12), 15):
                if self.r.random() < seed_density:
                    self.add(f'<rect x="{wx}" y="{wy}" width="6" height="8" fill="{lit}" '
                             f'opacity="{self.r.uniform(0.55, 1):.2f}"/>')
                    if self.r.random() < 0.08:
                        anchor(self, "citywin", wx + 3, wy + 4, rgb(color))
        x += w + self.r.uniform(2, 10)


S.sun, S.candle, S.water, S.aurora, S.skyline = _rec_sun, _rec_candle, _rec_water, _rec_aurora, _rec_skyline


# ---------- landscape ----------
def hill(s, y0, amp, waves, phase, c1, c2=None, jag=0.0):
    """A rolling ridge filled down to the bottom, shading from c1 at its top to c2."""
    fill = c1 if c2 is None else s.lin([(0, c1, 1), (1, c2, 1)], 0, y0 - amp * 1.4, 0, SH, "userSpaceOnUse")
    s.ridge(y0, amp, waves, phase, fill, jag)


def tufts(s, y0, amp, waves, phase, color, n=60, h=(8, 16), dy=4, x0=0, x1=SW, a=0.9):
    """Little clumps of grass along a ridge line."""
    for _ in range(n):
        x = s.r.uniform(x0, x1)
        y = s.ridge_y(y0, amp, waves, phase, x) + dy
        hh = s.r.uniform(*h)
        s.add(f'<path d="M{x - 4:.1f},{y:.1f} q2,-{hh * 0.6:.1f} -1,-{hh:.1f} M{x:.1f},{y:.1f} q1,-{hh * 0.7:.1f} '
              f'2,-{hh * 1.15:.1f} M{x + 4:.1f},{y:.1f} q-1,-{hh * 0.6:.1f} 3,-{hh * 0.9:.1f}" stroke="{color}" '
              f'stroke-width="2.4" fill="none" stroke-linecap="round" opacity="{a}"/>')


def rays(s, x, y, r, color, n=12, a=0.16, spread=0.09):
    """Soft beams of light fanning out from a point (a low sun, a lamp), fading as they go."""
    fill = s.rad([(0, color, a), (0.55, color, a * 0.5), (1, color, 0)], x, y, r)
    for i in range(n):
        ang = -math.pi + math.pi * (i + 0.5) / n
        a1, a2 = ang - spread, ang + spread
        s.add(f'<path d="M{x},{y} L{x + r * math.cos(a1):.1f},{y + r * math.sin(a1):.1f} '
              f'L{x + r * math.cos(a2):.1f},{y + r * math.sin(a2):.1f}Z" fill="{fill}"/>')


def long_cloud(s, x, y, w, h, c1, c2=None, a=0.9):
    """A long, flat evening cloud, lit from below."""
    fill = c1 if c2 is None else s.lin([(0, c1, 1), (1, c2, 1)])
    s.add(f'<path d="M{x - w / 2},{y} Q{x - w * 0.42},{y - h * 0.9} {x - w * 0.2},{y - h * 0.7} '
          f'Q{x - w * 0.08},{y - h * 1.5} {x + w * 0.1},{y - h * 0.9} Q{x + w * 0.3},{y - h * 1.2} {x + w * 0.36},{y - h * 0.5} '
          f'Q{x + w * 0.5},{y - h * 0.4} {x + w / 2},{y} Z" fill="{fill}" opacity="{a}"/>')


# ---------- trees ----------
def leafy_tree(s, x, base, h, colors, trunk="#5a3a28", lean=0.0):
    """A round, leafy tree: a forked trunk and a canopy of overlapping clumps, lit from the upper left."""
    r = s.r
    tw = h * 0.06
    s.add(f'<path d="M{x - tw},{base} C{x - tw * 0.6},{base - h * 0.3} {x - tw * 0.5},{base - h * 0.45} '
          f'{x - tw * 2.2 + lean},{base - h * 0.64} L{x - tw * 1.4 + lean},{base - h * 0.68} '
          f'C{x - tw * 0.1},{base - h * 0.54} {x},{base - h * 0.52} {x + tw * 0.2 + lean},{base - h * 0.52} '
          f'C{x + tw * 0.7},{base - h * 0.58} {x + tw * 1.3},{base - h * 0.64} {x + tw * 2.2 + lean},{base - h * 0.7} '
          f'L{x + tw * 2.6 + lean},{base - h * 0.64} C{x + tw * 1.1},{base - h * 0.5} {x + tw * 0.7},{base - h * 0.4} '
          f'{x + tw},{base}Z" fill="{trunk}"/>')
    dark, mid, light = colors
    cx, cy, R = x + lean, base - h * 0.7, h * 0.34
    blobs = [(cx, cy, R * 0.72)]
    for i in range(9):
        a = 2 * math.pi * i / 9 + r.uniform(-0.3, 0.3)
        d = R * r.uniform(0.4, 0.78)
        blobs.append((cx + math.cos(a) * d * 1.2, cy + math.sin(a) * d * 0.8, R * r.uniform(0.4, 0.58)))
    for bx, by, br in blobs:
        s.add(f'<circle cx="{bx:.1f}" cy="{by:.1f}" r="{br:.1f}" fill="{dark}"/>')
    for bx, by, br in blobs:
        s.add(f'<circle cx="{bx - br * 0.12:.1f}" cy="{by - br * 0.16:.1f}" r="{br * 0.8:.1f}" fill="{mid}"/>')
    for bx, by, br in sorted(blobs, key=lambda b: b[0] + b[1])[:5]:
        s.add(f'<circle cx="{bx - br * 0.3:.1f}" cy="{by - br * 0.34:.1f}" r="{br * 0.36:.1f}" fill="{light}" opacity="0.9"/>')


def pine2(s, x, base, h, c1, c2, snow=None, tiers=4):
    """A pine in tiers, its left side lit and right side in shade, with snow on the tiers if given."""
    s.add(f'<rect x="{x - h * 0.035}" y="{base - h * 0.14}" width="{h * 0.07}" height="{h * 0.16}" fill="#4a3324"/>')
    for k in range(tiers):
        t = k / tiers
        ty = base - h + h * 0.82 * t
        by = ty + h * (0.34 + 0.08 * t)
        hw = h * (0.13 + 0.17 * (k + 1) / tiers)
        s.add(f'<path d="M{x},{ty} L{x + hw},{by} Q{x},{by - h * 0.05} {x - hw},{by}Z" fill="{c1}"/>')
        s.add(f'<path d="M{x},{ty} L{x - hw},{by} Q{x - hw * 0.4},{by - h * 0.03} {x},{by - h * 0.045}Z" fill="{c2}"/>')
        if snow:
            s.add(f'<path d="M{x},{ty} L{x + hw * 0.45},{ty + h * 0.12} Q{x + hw * 0.2},{ty + h * 0.09} {x},{ty + h * 0.13} '
                  f'Q{x - hw * 0.2},{ty + h * 0.09} {x - hw * 0.45},{ty + h * 0.12}Z" fill="{snow}"/>')


# ---------- buildings ----------
def house(s, x, base, w, h, wall, roof, win="#ffd27a", lit=True, door="#5a3424", chimney=True, smoke=False,
          snow=False, trim="#ffffff", roof_h=None, windows=2, shade=None):
    """A cottage: walls, a pitched roof (snow on it if asked), windows (glowing when lit), a door and a chimney."""
    rh = roof_h or h * 0.6
    left, right, top = x - w / 2, x + w / 2, base - h
    if chimney:
        cx = right - w * 0.26
        s.add(f'<rect x="{cx}" y="{top - rh * 0.85}" width="{w * 0.12}" height="{rh * 0.8}" fill="{shade or roof}"/>')
        if smoke:
            anchor(s, "smoke", cx + w * 0.06, top - rh * 0.95, 1)
            for k in range(6):
                s.add(f'<circle cx="{cx + w * 0.06 + k * 9 + s.r.uniform(-3, 3):.1f}" cy="{top - rh * 0.95 - k * 26:.1f}" '
                      f'r="{9 + k * 4}" fill="#ffffff" opacity="{0.42 - k * 0.06:.2f}"/>')
    s.add(f'<rect x="{left}" y="{top}" width="{w}" height="{h}" fill="{wall}"/>')
    if shade:
        s.add(f'<rect x="{x + w * 0.18}" y="{top}" width="{w * 0.32}" height="{h}" fill="{shade}" opacity="0.18"/>')
    s.add(f'<path d="M{left - w * 0.09},{top + 2} L{x},{top - rh} L{right + w * 0.09},{top + 2}Z" fill="{roof}"/>')
    if snow:
        s.add(f'<path d="M{left - w * 0.11},{top + 4} L{x},{top - rh - 6} L{right + w * 0.11},{top + 4} '
              f'L{right + w * 0.05},{top - 4} Q{x + w * 0.2},{top - rh * 0.4} {x},{top - rh + 10} '
              f'Q{x - w * 0.2},{top - rh * 0.4} {left - w * 0.05},{top - 4}Z" fill="#ffffff"/>')
    ww, wh = w * 0.18, h * 0.3
    xs = [x - w * 0.3, x + w * 0.3 - ww] if windows == 2 else [x - ww / 2]
    for wx in xs:
        wy = top + h * 0.2
        if lit:
            anchor(s, "window", wx + ww / 2, wy + wh / 2, ww)
            s.glow(wx + ww / 2, wy + wh / 2, ww * 1.8, win, 0.45)
        s.add(f'<rect x="{wx:.1f}" y="{wy:.1f}" width="{ww:.1f}" height="{wh:.1f}" rx="2" fill="{win if lit else "#5d6b7a"}"/>')
        s.add(f'<path d="M{wx + ww / 2:.1f},{wy:.1f} V{wy + wh:.1f} M{wx:.1f},{wy + wh / 2:.1f} H{wx + ww:.1f}" '
              f'stroke="{trim}" stroke-width="2.5"/>')
        s.add(f'<rect x="{wx - 3:.1f}" y="{wy + wh:.1f}" width="{ww + 6:.1f}" height="4" fill="{trim}"/>')
    if windows == 2:
        dw, dh = w * 0.16, h * 0.5
        s.add(f'<rect x="{x - dw / 2:.1f}" y="{base - dh:.1f}" width="{dw:.1f}" height="{dh:.1f}" rx="2" fill="{door}"/>')
        s.add(f'<circle cx="{x + dw * 0.28:.1f}" cy="{base - dh * 0.45:.1f}" r="2.5" fill="#ffd27a"/>')


def fence(s, x0, x1, base, color, h=46, gap=36, shade=None):
    """A wooden fence: pointed posts and two rails."""
    s.add(f'<rect x="{x0}" y="{base - h * 0.72}" width="{x1 - x0}" height="6" fill="{color}"/>')
    s.add(f'<rect x="{x0}" y="{base - h * 0.32}" width="{x1 - x0}" height="6" fill="{color}"/>')
    x = x0
    while x <= x1:
        s.add(f'<path d="M{x - 5},{base} V{base - h} L{x},{base - h - 8} L{x + 5},{base - h} V{base}Z" fill="{color}"/>')
        if shade:
            s.add(f'<rect x="{x + 1}" y="{base - h}" width="4" height="{h}" fill="{shade}" opacity="0.5"/>')
        x += gap


# ---------- small things ----------
MAPLE = [(0, -1.0), (0.12, -0.62), (0.3, -0.72), (0.27, -0.38), (0.62, -0.55), (0.56, -0.28), (0.95, -0.22),
         (0.72, -0.02), (0.82, 0.12), (0.38, 0.12), (0.42, 0.32), (0.06, 0.2), (0.04, 0.66), (-0.04, 0.66),
         (-0.06, 0.2), (-0.42, 0.32), (-0.38, 0.12), (-0.82, 0.12), (-0.72, -0.02), (-0.95, -0.22), (-0.56, -0.28),
         (-0.62, -0.55), (-0.27, -0.38), (-0.3, -0.72), (-0.12, -0.62)]


def maple(s, x, y, size, color, rot, a=1.0, vein="#00000026"):
    """A maple leaf, with its veins."""
    pts = " ".join(f"{x + px * size:.1f},{y + py * size:.1f}" for px, py in MAPLE)
    v = " ".join(f"M{x:.1f},{y + 0.2 * size:.1f} L{x + tx * size * 0.8:.1f},{y + ty * size * 0.8:.1f}"
                 for tx, ty in ((0, -1), (0.9, -0.22), (-0.9, -0.22), (0.58, -0.52), (-0.58, -0.52)))
    s.add(f'<g transform="rotate({rot:.0f} {x:.1f} {y:.1f})" opacity="{a}"><polygon points="{pts}" fill="{color}"/>'
          f'<path d="{v} M{x:.1f},{y + 0.2 * size:.1f} V{y + 0.64 * size:.1f}" stroke="{vein}" stroke-width="{max(1, size * 0.05):.1f}" '
          f'fill="none"/></g>')


def goose(s, x, y, sc, color, a=0.9):
    """A goose in flight, seen from the side: body, long neck and wings up or down."""
    s.add(f'<g transform="translate({x:.1f},{y:.1f}) scale({sc:.2f})" opacity="{a}" fill="{color}">'
          f'<path d="M-14,0 Q-4,-3 8,-1 Q13,-1 16,-4 Q18,-5 19,-3 Q16,0 10,2 Q-2,5 -14,0Z"/>'
          f'<path d="M-4,-1 Q-1,-14 6,-20 Q2,-10 3,-1Z"/><path d="M-6,1 Q-3,9 3,13 Q1,6 2,1Z" opacity="0.8"/></g>')


def geese(s, x, y, n, sc, color, a=0.85, spacing=34):
    """A V of geese heading left, the leader at (x, y)."""
    goose(s, x, y, sc, color, a)
    for k in range(1, n):
        side = 1 if k % 2 else -1
        rank = (k + 1) // 2
        goose(s, x + rank * spacing * sc, y + side * rank * spacing * sc * 0.42, sc * (1 - rank * 0.04), color, a)


def string_lights(s, x0, x1, y, sag, colors, n, glow=0.5, wire="#2b1d14"):
    """A wire of bulbs strung between two points."""
    s.add(f'<path d="M{x0},{y} Q{(x0 + x1) / 2},{y + sag * 2} {x1},{y}" fill="none" stroke="{wire}" stroke-width="2.5"/>')
    for i in range(n):
        t = (i + 0.5) / n
        bx = x0 + (x1 - x0) * t
        by = y + 2 * sag * 2 * t * (1 - t) + 7
        c = colors[i % len(colors)]
        anchor(s, "bulb", bx, by + 6, rgb(c))
        s.glow(bx, by + 6, 22, c, glow)
        s.add(f'<ellipse cx="{bx:.1f}" cy="{by + 6:.1f}" rx="5.5" ry="8" fill="{c}"/>')
        s.add(f'<rect x="{bx - 3:.1f}" y="{by - 3:.1f}" width="6" height="5" fill="{wire}"/>')


def snowflake(s, x, y, r, color="#ffffff", a=0.9, rot=0):
    arms = []
    for k in range(6):
        ang = math.radians(rot + k * 60)
        ex, ey = x + r * math.cos(ang), y + r * math.sin(ang)
        arms.append(f"M{x:.1f},{y:.1f} L{ex:.1f},{ey:.1f}")
        for f in (0.5, 0.75):
            mx, my = x + r * f * math.cos(ang), y + r * f * math.sin(ang)
            for side in (-1, 1):
                ba = ang + side * math.radians(40)
                arms.append(f"M{mx:.1f},{my:.1f} L{mx + r * 0.25 * math.cos(ba):.1f},{my + r * 0.25 * math.sin(ba):.1f}")
    s.add(f'<path d="{" ".join(arms)}" stroke="{color}" stroke-width="{max(1.5, r * 0.11):.1f}" stroke-linecap="round" '
          f'fill="none" opacity="{a}"/>')


# Where the pass sits on the page (logo, card name, name, barcode, number), as boxes in page pixels.
PASS_BOXES = [(380, 255, 655, 532), (280, 548, 752, 632), (56, 638, 976, 880), (60, 884, 972, 1332), (320, 1336, 712, 1412)]


def clear_of_pass(x, y, pad=0):
    """True if something [pad] across around (x, y) stays off the logo, text and barcode."""
    return all(not (bx0 - pad < x < bx1 + pad and by0 - pad < y < by1 + pad) for bx0, by0, bx1, by1 in PASS_BOXES)


def spot(r, y0, y1, pad=20, center=0.08, tries=60):
    """A random spot in the page's sky, near the edges and clear of the pass; None if none turns up."""
    for _ in range(tries):
        x, y = edge(r, y0, y1, center)
        if clear_of_pass(x, y, pad) and y > TOP_CLEAR + pad * 0.5:
            return x, y
    return None


def edge(r, y0, y1, center=0.12):
    """A spot near the left or right edge of the page (where it won't sit behind the pass), now and then anywhere."""
    if r.random() < center:
        return r.uniform(40, SW - 40), r.uniform(y0, y1)
    x = r.uniform(16, 200) if r.random() < 0.5 else r.uniform(SW - 200, SW - 16)
    return x, r.uniform(y0, y1)


def stars_top_safe(s, n, y_max, color="#ffffff", big=0.1, y_min=8):
    """Stars all over the sky; the bigger twinkles stay below the buttons at the top."""
    for _ in range(n):
        x, y = s.r.uniform(10, SW - 10), s.r.uniform(y_min, y_max)
        if s.r.random() < big and y > TOP_CLEAR:
            s.sparkle(x, y, s.r.uniform(6, 10), color, s.r.uniform(0.7, 1))
        else:
            s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(0.9, 2.2):.1f}" fill="{color}" '
                  f'opacity="{s.r.uniform(0.45, 1):.2f}"/>')
