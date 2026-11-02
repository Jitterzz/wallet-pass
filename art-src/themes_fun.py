"""Pass Studio theme art: just-for-fun scenes. Laid out like themes.py: each theme has a page sky (top of the page down to the
scene), decorations in that sky (page coordinates, kept to the edges and off the pass), and a scene along the bottom
(band coordinates, 0..SW x 0..SH)."""
from props import *  # noqa: F401,F403
from themes import theme, scatter  # noqa: F401

Front = None  # set by render.py: `with Front(s):` draws in front of the scene band


# ======================= shared helpers =======================
def smooth(pts, closed=False):
    """A smooth path through the points (Catmull-Rom turned into curves)."""
    n = len(pts)
    if closed:
        ext = [pts[-1]] + list(pts) + [pts[0], pts[1]]
        d = f"M{pts[0][0]:.1f},{pts[0][1]:.1f}"
        for i in range(1, n + 1):
            p0, p1, p2, p3 = ext[i - 1], ext[i], ext[i + 1], ext[i + 2]
            c1 = (p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6)
            c2 = (p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6)
            d += f" C{c1[0]:.1f},{c1[1]:.1f} {c2[0]:.1f},{c2[1]:.1f} {p2[0]:.1f},{p2[1]:.1f}"
        return d + "Z"
    d = f"M{pts[0][0]:.1f},{pts[0][1]:.1f}"
    for i in range(n - 1):
        p0 = pts[i - 1] if i > 0 else pts[i]
        p1, p2 = pts[i], pts[i + 1]
        p3 = pts[i + 2] if i + 2 < n else p2
        c1 = (p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6)
        c2 = (p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6)
        d += f" C{c1[0]:.1f},{c1[1]:.1f} {c2[0]:.1f},{c2[1]:.1f} {p2[0]:.1f},{p2[1]:.1f}"
    return d


def blob(r, cx, cy, rx, ry, n=10, jit=0.18, rot=0.0):
    """An organic closed shape around (cx, cy)."""
    pts = []
    for i in range(n):
        a = 2 * math.pi * i / n + rot
        k = 1 + r.uniform(-jit, jit)
        pts.append((cx + rx * k * math.cos(a), cy + ry * k * math.sin(a)))
    return smooth(pts, True)


def mix(c1, c2, t):
    """A color between c1 and c2."""
    a = [int(c1[i:i + 2], 16) for i in (1, 3, 5)]
    b = [int(c2[i:i + 2], 16) for i in (1, 3, 5)]
    return "#" + "".join(f"{round(x + (y - x) * t):02x}" for x, y in zip(a, b))


def grouped(s, start, attrs):
    """Wraps everything drawn since body[start] in one <g attrs>."""
    inner = "".join(s.body[start:])
    del s.body[start:]
    s.add(f"<g {attrs}>{inner}</g>")


def area_blur(s, sd, x=-200, y=-500, w=SW + 400, h=TALL_H + 1000):
    """A blur whose area is the whole scene (so long thin shapes don't get boxy edges)."""
    fid = s.id("ab")
    s.defs.append(f'<filter id="{fid}" filterUnits="userSpaceOnUse" x="{x}" y="{y}" width="{w}" height="{h}">'
                  f'<feGaussianBlur stdDeviation="{sd}"/></filter>')
    return f"url(#{fid})"


def frond(s, x0, y0, ang, length, droop, width, color, rim=None, sweep=0.55, hang=0.6):
    """A palm frond in silhouette: a drooping midrib lined with fine leaflets swept back and hanging down."""
    n = 20
    a = math.radians(ang)
    mid, leaves = [], []
    for i in range(n + 1):
        t = i / n
        px = x0 + math.cos(a) * length * t
        py = y0 - math.sin(a) * length * t + droop * length * t * t
        tx, ty = math.cos(a), -math.sin(a) + 2 * droop * t
        tl = math.hypot(tx, ty)
        tx, ty = tx / tl, ty / tl
        nx, ny = -ty, tx
        mid.append((px, py))
        if i == 0:
            continue
        L = width * math.sin(math.pi * min(1.0, 0.15 + t * 0.92)) ** 0.6
        wv = max(1.6, L * 0.09)
        for side in (1, -1):
            dx = side * nx * 0.8 - tx * sweep
            dy = side * ny * 0.8 - ty * sweep + hang
            dl = math.hypot(dx, dy)
            ex, ey = px + dx / dl * L, py + dy / dl * L
            leaves.append(f"M{px - tx * wv:.1f},{py - ty * wv:.1f} L{ex:.1f},{ey:.1f} L{px + tx * wv:.1f},{py + ty * wv:.1f}Z")
    rib = "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in mid)
    s.add(f'<path d="{" ".join(leaves)}" fill="{color}"/>')
    s.add(f'<path d="{rib}" stroke="{color}" stroke-width="{max(2.5, width * 0.12):.1f}" fill="none" stroke-linecap="round"/>')
    if rim:
        s.add(f'<path d="{rib}" stroke="{rim}" stroke-width="1.4" fill="none" opacity="0.6" transform="translate(0,-1.5)"/>')


def palm_sil(s, x, base, h, lean, color, rim=None):
    """A palm in silhouette: a tapering curved trunk with rings and a crown of drooping fronds."""
    tx, ty = x + h * 0.32 * lean, base - h
    pts = []
    for i in range(13):
        t = i / 12
        cx_ = (1 - t) ** 3 * x + 3 * (1 - t) ** 2 * t * (x + h * 0.02 * lean) + 3 * (1 - t) * t * t * (x + h * 0.14 * lean) + t ** 3 * tx
        cy_ = (1 - t) ** 3 * base + 3 * (1 - t) ** 2 * t * (base - h * 0.45) + 3 * (1 - t) * t * t * (base - h * 0.8) + t ** 3 * ty
        pts.append((cx_, cy_, h * (0.034 - 0.014 * t)))
    left = [(px - w, py) for px, py, w in pts]
    right = [(px + w, py) for px, py, w in pts]
    d = "M" + " L".join(f"{px:.1f},{py:.1f}" for px, py in left + right[::-1]) + "Z"
    s.add(f'<path d="{d}" fill="{color}"/>')
    side = 1 if lean >= 0 else -1
    if rim:
        edge = right if side > 0 else left
        s.add(f'<path d="M' + " L".join(f"{px:.1f},{py:.1f}" for px, py in edge) + f'" stroke="{rim}" stroke-width="2" fill="none" opacity="0.6"/>')
    for px, py, w in pts[1:-1]:
        s.add(f'<path d="M{px - w:.1f},{py:.1f} q{w:.1f},{w * 0.5:.1f} {2 * w:.1f},0" stroke="{rim or color}" stroke-width="1.4" '
              f'fill="none" opacity="{0.35 if rim else 1}"/>')
    for ang, ln, dr in ((178, 0.5, 0.75), (152, 0.52, 0.62), (126, 0.46, 0.55), (100, 0.38, 0.5), (78, 0.4, 0.55),
                        (52, 0.5, 0.6), (26, 0.54, 0.7), (2, 0.5, 0.8), (205, 0.36, 0.9), (-25, 0.36, 0.9)):
        frond(s, tx, ty, ang, h * ln, dr, h * 0.13, color, rim if (math.cos(math.radians(ang)) * side > 0) else None)
    for dx in (-0.02, 0.015):
        s.add(f'<circle cx="{tx + h * dx:.1f}" cy="{ty + h * 0.025:.1f}" r="{h * 0.022:.1f}" fill="{color}"/>')


def fade_mask(s, y0, y1, x0=-60, x1=SW + 60, top=-200, bottom=SH + 200):
    """A mask that fades things out going up from y1 (fully shown) to y0 (gone)."""
    g = s.lin([(0, "#ffffff", 0), (1, "#ffffff", 1)], 0, y0, 0, y1, "userSpaceOnUse")
    mid = s.id("mk")
    s.defs.append(f'<mask id="{mid}" maskUnits="userSpaceOnUse" x="{x0}" y="{top}" width="{x1 - x0}" height="{bottom - top}">'
                  f'<rect x="{x0}" y="{top}" width="{x1 - x0}" height="{bottom - top}" fill="{g}"/></mask>')
    return f"url(#{mid})"


# ======================= Coral Reef: under the sea =======================
def light_ray(s, x0, w0, x1, w1, y0, y1, color="#ffffff", a=0.12, fade_in=0.0):
    """A soft beam of sunlight slanting down through the water (fading in over [fade_in] of its length, if given)."""
    stops = [(0, color, a), (0.7, color, a * 0.4), (1, color, 0)]
    if fade_in:
        stops = [(0, color, 0), (fade_in, color, a)] + stops[1:]
    f = s.lin(stops, 0, y0, 0, y1, "userSpaceOnUse")
    s.add(f'<path d="M{x0:.1f},{y0:.1f} L{x0 + w0:.1f},{y0:.1f} L{x1 + w1:.1f},{y1:.1f} L{x1:.1f},{y1:.1f}Z" fill="{f}"/>')


def manta(s, x, y, k, color, a):
    """A manta ray seen from below, gliding."""
    d = (f"M{x},{y - 12 * k} C{x + 22 * k},{y - 16 * k} {x + 52 * k},{y - 6 * k} {x + 86 * k},{y + 16 * k} "
         f"C{x + 54 * k},{y + 12 * k} {x + 32 * k},{y + 18 * k} {x + 13 * k},{y + 30 * k} L{x},{y + 36 * k} "
         f"L{x - 13 * k},{y + 30 * k} C{x - 32 * k},{y + 18 * k} {x - 54 * k},{y + 12 * k} {x - 86 * k},{y + 16 * k} "
         f"C{x - 52 * k},{y - 6 * k} {x - 22 * k},{y - 16 * k} {x},{y - 12 * k}Z "
         f"M{x - 9 * k},{y - 11 * k} q-3,{-12 * k:.1f} {-11 * k:.1f},{-10 * k:.1f} q{6 * k:.1f},{4 * k:.1f} {5 * k:.1f},{10 * k:.1f}Z "
         f"M{x + 9 * k},{y - 11 * k} q3,{-12 * k:.1f} {11 * k:.1f},{-10 * k:.1f} q{-6 * k:.1f},{4 * k:.1f} {-5 * k:.1f},{10 * k:.1f}Z")
    s.add(f'<g opacity="{a}"><path d="{d}" fill="{color}"/>'
          f'<path d="M{x},{y + 34 * k} Q{x + 6 * k},{y + 70 * k} {x - 4 * k},{y + 104 * k}" stroke="{color}" '
          f'stroke-width="{2.2 * k:.1f}" fill="none" stroke-linecap="round"/></g>')


def fish_sil(s, x, y, k, color, a=1.0, flip=1):
    """A small fish, side on."""
    s.add(f'<g transform="translate({x:.1f},{y:.1f}) scale({flip * k:.2f},{k:.2f})" opacity="{a}" fill="{color}">'
          f'<path d="M-14,0 C-8,-7 6,-8 14,0 C6,8 -8,7 -14,0Z M-12,0 L-22,-7 L-20,0 L-22,7Z"/></g>')


def tang(s, x, y, k, body="#ffd23a", fin="#ffb21e", flip=1, a=1.0):
    """A bright reef fish: a tall round body, a fin on top and below, and a tail."""
    s.add(f'<g transform="translate({x:.1f},{y:.1f}) scale({flip * k:.2f},{k:.2f})" opacity="{a}">'
          f'<path d="M-16,0 Q-20,-8 -28,-11 Q-25,0 -28,11 Q-20,8 -16,0Z" fill="{fin}"/>'
          f'<path d="M-6,-12 Q2,-24 12,-14Z M-6,12 Q2,24 12,14Z" fill="{fin}"/>'
          f'<ellipse cx="0" cy="0" rx="18" ry="13" fill="{body}"/>'
          f'<path d="M-2,-12 Q8,0 -2,12" stroke="#ffffff" stroke-opacity="0.35" stroke-width="2" fill="none"/>'
          f'<circle cx="10" cy="-3" r="2.6" fill="#1b1b2a"/><circle cx="10.8" cy="-3.8" r="0.9" fill="#ffffff"/></g>')


def brain_coral(s, x, base, rx, ry, c1, c2, groove):
    """A brain coral: a low dome covered in winding ridges that follow its shape, lit from above."""
    d = (f"M{x - rx:.1f},{base:.1f} C{x - rx:.1f},{base - ry * 1.33:.1f} {x + rx:.1f},{base - ry * 1.33:.1f} "
         f"{x + rx:.1f},{base:.1f}Z")
    cid = s.id("cl")
    s.defs.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath>')
    fill = s.lin([(0, c1, 1), (1, c2, 1)], 0, base - ry, 0, base, "userSpaceOnUse")
    s.add(f'<path d="{d}" fill="{fill}"/>')
    lines = []
    rows = max(4, int(ry / 8))
    for k in range(1, rows + 1):
        f = k / (rows + 0.6)
        ph = s.r.uniform(0, 6.28)
        fr = s.r.uniform(9, 15)
        pts = []
        for i in range(0, 61):
            t = i / 60
            a = math.pi * t
            w = 3.2 * math.sin(t * fr + ph)
            pts.append((x - rx * f * math.cos(a) + w * math.cos(a) * 0.4, base - ry * f * math.sin(a) * 1.02 + w))
        lines.append("M" + " L".join(f"{px:.1f},{py:.1f}" for px, py in pts))
    path = " ".join(lines)
    s.add(f'<g clip-path="url(#{cid})"><path d="{path}" stroke="{groove}" stroke-width="3.4" fill="none" stroke-linecap="round" '
          f'opacity="0.85"/><path d="{path}" stroke="#ffffff" stroke-width="1.4" fill="none" opacity="0.2" '
          f'transform="translate(-1.5,-2.5)"/>'
          f'<ellipse cx="{x - rx * 0.3:.1f}" cy="{base - ry * 0.78:.1f}" rx="{rx * 0.5:.1f}" ry="{ry * 0.3:.1f}" fill="#ffffff" '
          f'opacity="0.16"/><ellipse cx="{x + rx * 0.55:.1f}" cy="{base - ry * 0.2:.1f}" rx="{rx * 0.65:.1f}" ry="{ry * 0.55:.1f}" '
          f'fill="#001428" opacity="0.18"/></g>')


def staghorn(s, x, base, h, c1, c2, tip, trunks=3, depth=3, spread=0.48, shine=True):
    """Branching staghorn coral: forked arms with pale growing tips."""
    segs = []

    def br(x0, y0, ang, ln, w, d):
        x1 = x0 + ln * math.cos(ang)
        y1 = y0 - ln * math.sin(ang)
        mx = (x0 + x1) / 2 + s.r.uniform(-ln * 0.14, ln * 0.14)
        my = (y0 + y1) / 2 + s.r.uniform(-ln * 0.08, ln * 0.08)
        segs.append((x0, y0, mx, my, x1, y1, w, d))
        if d == 0:
            return
        n = 2 if s.r.random() < 0.72 else 3
        for i in range(n):
            da = -spread + 2 * spread * (i / (n - 1)) + s.r.uniform(-0.16, 0.16)
            br(x1, y1, ang + da, ln * s.r.uniform(0.6, 0.78), w * 0.76, d - 1)

    for k in range(trunks):
        a0 = math.pi / 2 + (k - (trunks - 1) / 2) * 0.42 + s.r.uniform(-0.1, 0.1)
        br(x + (k - (trunks - 1) / 2) * h * 0.06, base, a0, h * s.r.uniform(0.26, 0.33), h * 0.075, depth)
    for (x0, y0, mx, my, x1, y1, w, d) in segs:
        s.add(f'<path d="M{x0:.1f},{y0:.1f} Q{mx:.1f},{my:.1f} {x1:.1f},{y1:.1f}" stroke="{c2}" stroke-width="{w:.1f}" '
              f'fill="none" stroke-linecap="round"/>')
    for (x0, y0, mx, my, x1, y1, w, d) in segs:
        o = w * 0.13
        s.add(f'<path d="M{x0 - o:.1f},{y0:.1f} Q{mx - o:.1f},{my:.1f} {x1 - o:.1f},{y1:.1f}" stroke="{c1}" '
              f'stroke-width="{w * 0.66:.1f}" fill="none" stroke-linecap="round"/>')
    for (x0, y0, mx, my, x1, y1, w, d) in segs:
        if d == 0:
            s.add(f'<circle cx="{x1:.1f}" cy="{y1:.1f}" r="{w * 0.48:.1f}" fill="{tip}"/>')
        if not shine:
            continue
        o = w * 0.26
        s.add(f'<path d="M{x0 - o:.1f},{y0:.1f} Q{mx - o:.1f},{my:.1f} {x1 - o:.1f},{y1:.1f}" stroke="#ffffff" '
              f'stroke-width="{max(1.0, w * 0.14):.1f}" fill="none" stroke-linecap="round" opacity="0.28"/>')


def sea_fan(s, x, base, h, w, color, dark, a=0.9):
    """A sea fan: a flat lacy fan on a short stem, its main veins branching through a fine mesh."""
    shape = (f"M{x:.1f},{base:.1f} C{x - w * 0.12:.1f},{base - h * 0.25:.1f} {x - w * 0.62:.1f},{base - h * 0.42:.1f} "
             f"{x - w * 0.52:.1f},{base - h * 0.78:.1f} Q{x - w * 0.36:.1f},{base - h * 1.05:.1f} {x:.1f},{base - h:.1f} "
             f"Q{x + w * 0.36:.1f},{base - h * 1.05:.1f} {x + w * 0.52:.1f},{base - h * 0.78:.1f} "
             f"C{x + w * 0.62:.1f},{base - h * 0.42:.1f} {x + w * 0.12:.1f},{base - h * 0.25:.1f} {x:.1f},{base:.1f}Z")
    cid = s.id("sf")
    s.defs.append(f'<clipPath id="{cid}"><path d="{shape}"/></clipPath>')
    s.add(f'<path d="{shape}" fill="{s.lin([(0, color, 0.55), (1, dark, 0.4)], 0, base - h, 0, base, "userSpaceOnUse")}"/>')
    mesh = []
    ox, oy = x, base + h * 0.12
    for i in range(46):
        a0 = math.radians(14 + i * 152 / 45 + s.r.uniform(-0.8, 0.8))
        pts = []
        for j in range(17):
            rr = 1.5 * h * j / 16
            a = a0 + 0.025 * math.sin(j * 1.3 + i * 0.7)
            pts.append((ox + rr * math.cos(a), oy - rr * math.sin(a)))
        mesh.append("M" + " L".join(f"{px:.1f},{py:.1f}" for px, py in pts))
    for k in range(1, 36):
        rr = h * 0.04 * k
        pts = []
        for j in range(33):
            a = math.radians(10 + 160 * j / 32)
            r2 = rr * (1 + 0.035 * math.sin(j * 1.9 + k * 2.3))
            pts.append((ox + r2 * math.cos(a), oy - r2 * math.sin(a)))
        mesh.append("M" + " L".join(f"{px:.1f},{py:.1f}" for px, py in pts))
    veins = []

    def vein(x0, y0, ang, ln, wdt, d):
        x1 = x0 + ln * math.cos(ang)
        y1 = y0 - ln * math.sin(ang)
        veins.append((f"M{x0:.1f},{y0:.1f} Q{(x0 + x1) / 2 + s.r.uniform(-5, 5):.1f},{(y0 + y1) / 2:.1f} {x1:.1f},{y1:.1f}", max(1.2, wdt)))
        if d:
            for da in (-0.36, 0.34):
                vein(x1, y1, ang + da + s.r.uniform(-0.1, 0.1), ln * 0.78, wdt * 0.72, d - 1)

    for a0 in (0.9, 1.25, 1.57, 1.9, 2.25):
        vein(x, base, a0, h * 0.24, 5.0, 4)
    s.add(f'<g clip-path="url(#{cid})"><path d="{" ".join(mesh)}" stroke="{color}" stroke-width="1.5" fill="none" '
          f'opacity="{a}"/>' + "".join(f'<path d="{p}" stroke="{dark}" stroke-width="{wd:.1f}" fill="none" '
                                        f'stroke-linecap="round"/>' for p, wd in veins) + "</g>")
    s.add(f'<path d="M{x:.1f},{base + 4:.1f} V{base - h * 0.08:.1f}" stroke="{dark}" stroke-width="9" stroke-linecap="round"/>')


def tube_sponges(s, x, base, h, n, c1, c2, inner, spread=28):
    """A clump of tube sponges: open-topped tubes, lit on the left."""
    order = sorted(range(n), key=lambda i: abs(i - (n - 1) / 2), reverse=True)
    for i in order:
        tx = x + (i - (n - 1) / 2) * spread + s.r.uniform(-4, 4)
        th = h * s.r.uniform(0.55, 1.0)
        tw = spread * s.r.uniform(0.82, 1.0)
        top_x = tx + (i - (n - 1) / 2) * 6 + s.r.uniform(-5, 5)
        top = base - th
        body = (f"M{tx - tw * 0.4:.1f},{base:.1f} C{tx - tw * 0.48:.1f},{base - th * 0.5:.1f} {top_x - tw * 0.6:.1f},{base - th * 0.8:.1f} "
                f"{top_x - tw * 0.56:.1f},{top:.1f} L{top_x + tw * 0.56:.1f},{top:.1f} C{top_x + tw * 0.6:.1f},{base - th * 0.8:.1f} "
                f"{tx + tw * 0.48:.1f},{base - th * 0.5:.1f} {tx + tw * 0.4:.1f},{base:.1f}Z")
        fill = s.lin([(0, c1, 1), (0.55, c1, 1), (1, c2, 1)], top_x - tw * 0.6, 0, top_x + tw * 0.6, 0, "userSpaceOnUse")
        s.add(f'<path d="{body}" fill="{fill}"/>')
        for _ in range(int(th / 9)):
            s.add(f'<circle cx="{tx + s.r.uniform(-tw * 0.35, tw * 0.35):.1f}" cy="{s.r.uniform(top + 8, base - 4):.1f}" '
                  f'r="{s.r.uniform(1, 2.2):.1f}" fill="{c2}" opacity="0.55"/>')
        s.add(f'<ellipse cx="{top_x:.1f}" cy="{top:.1f}" rx="{tw * 0.56:.1f}" ry="{tw * 0.2:.1f}" fill="{c2}"/>')
        s.add(f'<ellipse cx="{top_x:.1f}" cy="{top + 1:.1f}" rx="{tw * 0.42:.1f}" ry="{tw * 0.13:.1f}" fill="{inner}"/>')
        s.add(f'<path d="M{top_x - tw * 0.5:.1f},{top + 4:.1f} Q{top_x - tw * 0.56:.1f},{top + th * 0.4:.1f} {tx - tw * 0.36:.1f},{base - 6:.1f}" '
              f'stroke="#ffffff" stroke-opacity="0.3" stroke-width="2.5" fill="none" stroke-linecap="round"/>')


def anemone(s, x, base, r, body, body2, tent, tent2, tip):
    """A sea anemone: a stubby column, mostly hidden under a bushy crown of waving tentacles."""
    s.add(f'<path d="M{x - r * 0.5:.1f},{base:.1f} C{x - r * 0.54:.1f},{base - r * 0.2:.1f} {x - r * 0.62:.1f},{base - r * 0.36:.1f} '
          f'{x - r * 0.58:.1f},{base - r * 0.46:.1f} L{x + r * 0.58:.1f},{base - r * 0.46:.1f} C{x + r * 0.62:.1f},{base - r * 0.36:.1f} '
          f'{x + r * 0.54:.1f},{base - r * 0.2:.1f} {x + r * 0.5:.1f},{base:.1f}Z" fill="{body}"/>')
    s.add(f'<path d="M{x + r * 0.12:.1f},{base:.1f} C{x + r * 0.2:.1f},{base - r * 0.2:.1f} {x + r * 0.3:.1f},{base - r * 0.38:.1f} '
          f'{x + r * 0.58:.1f},{base - r * 0.46:.1f} C{x + r * 0.62:.1f},{base - r * 0.36:.1f} {x + r * 0.54:.1f},{base - r * 0.2:.1f} '
          f'{x + r * 0.5:.1f},{base:.1f}Z" fill="{body2}"/>')
    for k in range(5):
        s.add(f'<path d="M{x - r * 0.45 + k * r * 0.22:.1f},{base - 2:.1f} V{base - r * 0.42:.1f}" stroke="{body2}" stroke-width="2" opacity="0.5"/>')
    cy = base - r * 0.46
    layers = ((tent2, 30, r * 1.0, r * 0.12, 168, 12), (tent, 34, r * 0.9, r * 0.115, 176, 4), (tent, 24, r * 0.55, r * 0.12, 205, 335))
    for li, (col, n, ln, wd, a0, a1) in enumerate(layers):
        for i in range(n):
            t = i / (n - 1)
            ang = math.radians(a0 + (a1 - a0) * t + s.r.uniform(-8, 8))
            ba = math.pi * (1 - t)
            bx = x + r * 0.56 * math.cos(ba) * (0.9 if li < 2 else 0.8)
            by = cy - r * 0.1 * math.sin(ba) * (1 if li < 2 else -0.6)
            L = ln * s.r.uniform(0.8, 1.12)
            ex = bx + L * math.cos(ang)
            ey = by - L * math.sin(ang)
            nx, ny = -(ey - by) / L, (ex - bx) / L
            wob = s.r.uniform(-0.28, 0.28) * L
            mx = (bx + ex) / 2 + nx * wob
            my = (by + ey) / 2 + ny * wob
            s.add(f'<path d="M{bx:.1f},{by:.1f} Q{mx:.1f},{my:.1f} {ex:.1f},{ey:.1f}" stroke="{col}" stroke-width="{wd:.1f}" '
                  f'fill="none" stroke-linecap="round"/>')
            s.add(f'<path d="M{bx:.1f},{by:.1f} Q{mx - wd * 0.2:.1f},{my - wd * 0.2:.1f} {ex:.1f},{ey:.1f}" stroke="#ffffff" '
                  f'stroke-width="{wd * 0.25:.1f}" fill="none" stroke-linecap="round" opacity="0.22"/>')
            s.add(f'<circle cx="{ex:.1f}" cy="{ey:.1f}" r="{wd * 0.6:.1f}" fill="{tip}"/>')


def clownfish(s, x, y, k, flip=1, rot=0):
    """A clownfish: orange, with three white bands edged in black."""
    cid = s.id("cf")
    body = "M-30,0 C-22,-15 10,-17 23,-6 Q31,0 23,6 C10,17 -22,15 -30,0Z"
    s.defs.append(f'<clipPath id="{cid}"><path d="{body}"/></clipPath>')
    ol = "#1b1414"
    s.add(f'<g transform="translate({x:.1f},{y:.1f}) rotate({rot}) scale({flip * k:.2f},{k:.2f})">'
          f'<path d="M-27,0 L-42,-12 Q-37,0 -42,12Z" fill="#ff7a1a" stroke="{ol}" stroke-width="1.6"/>'
          f'<path d="M-14,-12 Q-6,-24 2,-14 Q8,-22 14,-10Z" fill="#ff7a1a" stroke="{ol}" stroke-width="1.6"/>'
          f'<path d="M-8,12 Q-2,22 6,13Z" fill="#ff7a1a" stroke="{ol}" stroke-width="1.6"/>'
          f'<path d="{body}" fill="#ff7a1a"/>'
          f'<g clip-path="url(#{cid})"><rect x="8" y="-20" width="7" height="40" fill="#ffffff" stroke="{ol}" stroke-width="1.6"/>'
          f'<rect x="-9" y="-20" width="8" height="40" fill="#ffffff" stroke="{ol}" stroke-width="1.6"/>'
          f'<rect x="-28" y="-20" width="5" height="40" fill="#ffffff" stroke="{ol}" stroke-width="1.6"/>'
          f'<ellipse cx="2" cy="-8" rx="18" ry="4" fill="#ffffff" opacity="0.25"/></g>'
          f'<path d="{body}" fill="none" stroke="{ol}" stroke-width="1.4"/>'
          f'<path d="M2,4 Q8,10 2,13Z" fill="#ff8f3a" stroke="{ol}" stroke-width="1.2"/>'
          f'<circle cx="18" cy="-3" r="3.2" fill="{ol}"/><circle cx="18.9" cy="-4" r="1.1" fill="#ffffff"/></g>')


def giant_clam(s, x, base, w, shell="#d9d2bf", shade="#a39a84", mantle="#2c64c8", spot="#5ae3f0"):
    """A giant clam, open, its bright blue mantle between the wavy shell edges."""
    h = w * 0.36
    zig = " ".join(f"L{x - w / 2 + w * i / 8:.1f},{base - h * (0.62 if i % 2 else 0.42):.1f}" for i in range(9))
    s.add(f'<path d="M{x - w / 2:.1f},{base - h * 0.42:.1f} {zig} Q{x + w * 0.52:.1f},{base:.1f} {x:.1f},{base + 4:.1f} '
          f'Q{x - w * 0.52:.1f},{base:.1f} {x - w / 2:.1f},{base - h * 0.42:.1f}Z" fill="{shell}"/>')
    for i in range(1, 8):
        s.add(f'<path d="M{x:.1f},{base + 2:.1f} L{x - w / 2 + w * i / 8:.1f},{base - h * (0.6 if i % 2 else 0.44):.1f}" '
              f'stroke="{shade}" stroke-width="2" opacity="0.7"/>')
    m = smooth([(x - w * 0.46, base - h * 0.55), (x - w * 0.3, base - h * 0.95), (x - w * 0.1, base - h * 0.78),
                (x + w * 0.1, base - h * 1.0), (x + w * 0.3, base - h * 0.8), (x + w * 0.46, base - h * 0.58),
                (x + w * 0.2, base - h * 0.55), (x - w * 0.2, base - h * 0.56)], True)
    s.add(f'<path d="{m}" fill="{mantle}"/>')
    for _ in range(16):
        s.add(f'<circle cx="{x + s.r.uniform(-w * 0.38, w * 0.38):.1f}" cy="{base - h * s.r.uniform(0.62, 0.85):.1f}" '
              f'r="{s.r.uniform(1.6, 3.4):.1f}" fill="{spot}" opacity="0.85"/>')
    zig2 = " ".join(f"L{x - w * 0.46 + w * 0.92 * i / 8:.1f},{base - h * (1.08 if i % 2 else 0.9):.1f}" for i in range(9))
    s.add(f'<path d="M{x - w * 0.46:.1f},{base - h * 0.9:.1f} {zig2}" stroke="{shell}" stroke-width="5" fill="none" stroke-linejoin="round"/>')


def urchin(s, x, y, r, color="#2a1f3a", spine="#3d2c55"):
    """A sea urchin: a dark ball bristling with spines."""
    lines = []
    for i in range(30):
        a = math.pi * (1.0 + i / 29) + s.r.uniform(-0.05, 0.05)
        ln = r * s.r.uniform(1.6, 2.3)
        lines.append(f"M{x:.1f},{y:.1f} L{x + ln * math.cos(a):.1f},{y + ln * math.sin(a) * 0.95:.1f}")
    s.add(f'<path d="{" ".join(lines)}" stroke="{spine}" stroke-width="2" stroke-linecap="round"/>')
    s.add(f'<ellipse cx="{x:.1f}" cy="{y:.1f}" rx="{r:.1f}" ry="{r * 0.8:.1f}" fill="{color}"/>')
    s.add(f'<ellipse cx="{x - r * 0.3:.1f}" cy="{y - r * 0.35:.1f}" rx="{r * 0.3:.1f}" ry="{r * 0.18:.1f}" fill="#ffffff" opacity="0.18"/>')


def table_coral(s, x, base, w, c1, c2, rim):
    """Table coral: flat round plates stacked on a short stalk."""
    s.add(f'<path d="M{x - 9:.1f},{base:.1f} Q{x - 6:.1f},{base - 30:.1f} {x - 12:.1f},{base - 52:.1f} L{x + 12:.1f},{base - 52:.1f} '
          f'Q{x + 6:.1f},{base - 30:.1f} {x + 9:.1f},{base:.1f}Z" fill="{c2}"/>')
    for k, (dy, sc) in enumerate(((52, 1.0), (74, 0.78), (92, 0.5))):
        cy = base - dy
        ww = w * sc
        s.add(f'<ellipse cx="{x:.1f}" cy="{cy + 5:.1f}" rx="{ww / 2:.1f}" ry="{ww * 0.12:.1f}" fill="{c2}"/>')
        s.add(f'<ellipse cx="{x:.1f}" cy="{cy:.1f}" rx="{ww / 2:.1f}" ry="{ww * 0.12:.1f}" fill="{c1}"/>')
        s.add(f'<ellipse cx="{x:.1f}" cy="{cy:.1f}" rx="{ww / 2:.1f}" ry="{ww * 0.12:.1f}" fill="none" stroke="{rim}" '
              f'stroke-width="2.5" stroke-dasharray="3 4"/>')
        if k == 0:
            s.add(f'<ellipse cx="{x - ww * 0.12:.1f}" cy="{cy - 2:.1f}" rx="{ww * 0.25:.1f}" ry="{ww * 0.05:.1f}" fill="#ffffff" opacity="0.25"/>')


def sea_rock(s, x, base, w, h, c1="#4a7fa0", c2="#1f4c6a", seed_jit=0.14):
    """A rounded rock on the sea floor, lit from above."""
    d = blob(s.r, x, base - h * 0.45, w / 2, h * 0.55, 11, seed_jit)
    cid = s.id("rk")
    s.defs.append(f'<clipPath id="{cid}"><rect x="{x - w}" y="{base - h * 2}" width="{w * 2}" height="{h * 2}"/></clipPath>')
    f = s.lin([(0, c1, 1), (1, c2, 1)], 0, base - h, 0, base, "userSpaceOnUse")
    s.add(f'<g clip-path="url(#{cid})"><path d="{d}" fill="{f}"/></g>')
    for _ in range(int(w / 14)):
        s.add(f'<circle cx="{x + s.r.uniform(-w * 0.35, w * 0.35):.1f}" cy="{base - s.r.uniform(h * 0.2, h * 0.85):.1f}" '
              f'r="{s.r.uniform(1.2, 3):.1f}" fill="#9fd0e8" opacity="0.22"/>')


def seaweed(s, x, base, h, color, n=4):
    """A short tuft of seaweed."""
    for i in range(n):
        dx = (i - (n - 1) / 2) * 5
        bend = s.r.uniform(-h * 0.3, h * 0.3)
        hh = h * s.r.uniform(0.6, 1.0)
        s.add(f'<path d="M{x + dx - 3:.1f},{base:.1f} Q{x + dx + bend:.1f},{base - hh * 0.6:.1f} {x + dx + bend * 0.4:.1f},{base - hh:.1f} '
              f'Q{x + dx + bend * 0.9 + 4:.1f},{base - hh * 0.55:.1f} {x + dx + 3:.1f},{base:.1f}Z" fill="{color}"/>')


@theme("reef", [(0, "#1580b8"), (0.5, "#0f6ea6"), (1, "#0b5a92")])
class Reef:
    @staticmethod
    def deco(s):
        # The surface far above: a faint wavy brightness at the very top.
        wav = " ".join(f"L{x},{34 + 9 * math.sin(x / 46) + 5 * math.sin(x / 17):.1f}" for x in range(SW + 24, -24, -24))
        s.add(f'<path d="M-24,0 L{SW + 24},0 {wav}Z" fill="#ffffff" opacity="0.07"/>')
        # Sunlight slanting down at the edges.
        for (x0, w0, x1, w1, a) in ((-30, 70, 120, 40, 0.11), (60, 46, 200, 30, 0.08), (130, 30, 250, 18, 0.06),
                                    (930, 80, 840, 46, 0.11), (860, 44, 760, 28, 0.07), (1000, 40, 950, 24, 0.08)):
            light_ray(s, x0, w0, x1, w1, 0, 1300, "#e8fbff", a)
        manta(s, 150, 470, 1.0, "#0b4f7e", 0.55)
        for (x, y, k) in ((880, 400, 0.9), (912, 418, 0.8), (944, 396, 0.85), (900, 440, 0.7), (960, 432, 0.75),
                          (930, 458, 0.65)):
            fish_sil(s, x, y, k, "#0b5585", 0.6, -1)
        # Specks drifting in the water.
        scatter(s, 70, TOP_CLEAR + 10, BAND_Y - 10, 10, lambda x, y: s.add(
            f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(1, 2.4):.1f}" fill="#d8f4ff" opacity="{s.r.uniform(0.15, 0.4):.2f}"/>'), 0.2)
        with Front(s):
            tang(s, 110, 1352, 0.9, "#ffd23a", "#ffb21e", 1)
            tang(s, 70, 1395, 0.65, "#4fc3ff", "#2a7fd0", 1)
            tang(s, 960, 1360, 0.8, "#ffd23a", "#ffb21e", -1)

    @staticmethod
    def scene(s):
        f = s.lin([(0, "#0b5a92", 1), (1, "#08487a", 1)], 0, 0, 0, 640, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{f}"/>')
        for (x0, w0, x1, w1, a) in ((260, 50, 330, 80, 0.08), (520, 40, 560, 90, 0.07), (700, 60, 690, 70, 0.06)):
            light_ray(s, x0, w0, x1, w1, 0, 640, "#e8fbff", a, fade_in=0.2)
        # Far reef in the blue haze.
        hz = "#2a7fb2"
        s.ridge(520, 26, 1.6, 0.4, hz)
        for (x, base, rx, ry) in ((40, 512, 110, 70), (365, 536, 80, 44), (690, 528, 100, 58), (985, 505, 90, 66)):
            s.add(f'<ellipse cx="{x}" cy="{base}" rx="{rx}" ry="{ry}" fill="{hz}"/>')
        for (x, base, h) in ((170, 530, 150), (500, 540, 120), (840, 522, 160), (610, 536, 100), (300, 536, 90)):
            staghorn(s, x, base, h, hz, hz, hz, trunks=2, depth=3, shine=False)
        for (x, base, h, w) in ((250, 540, 120, 22), (272, 540, 86, 18), (760, 530, 136, 24), (783, 530, 96, 18)):
            s.add(f'<rect x="{x - w / 2}" y="{base - h}" width="{w}" height="{h}" rx="{w / 2}" fill="{hz}"/>')
        s.ridge(575, 18, 2.2, 1.7, "#1f6c9e")
        # Rocky shelves for the corals.
        for (x, base, w, h) in ((90, 700, 260, 190), (960, 690, 230, 200), (560, 700, 300, 90), (330, 690, 160, 80)):
            sea_rock(s, x, base, w, h, "#2f6b90", "#174a6a")
        # The sand.
        sand = s.lin([(0, "#d9c793", 1), (1, "#b8a374", 1)], 0, 600, 0, SH, "userSpaceOnUse")
        top = " ".join(f"L{x},{600 + 10 * math.sin(x / 90 + 1) + 6 * math.sin(x / 33):.1f}" for x in range(0, SW + 24, 24))
        anchor(s, "sand", 0, 600, 0)
        s.add(f'<path d="M0,{SH} L0,606 {top} L{SW},{SH}Z" fill="{sand}"/>')
        for k in range(14):
            y = 630 + k * 17 + s.r.uniform(-4, 4)
            d = "M" + " L".join(f"{x},{y + 4 * math.sin(x / 28 + k * 1.7):.1f}" for x in range(-20, SW + 40, 20))
            s.add(f'<path d="{d}" stroke="#9c8960" stroke-width="2" fill="none" opacity="0.35"/>')
            s.add(f'<path d="{d}" stroke="#f3e8c4" stroke-width="1.5" fill="none" opacity="0.35" transform="translate(0,-3)"/>')
        for _ in range(16):
            x, y = s.r.uniform(0, SW), s.r.uniform(640, 840)
            s.add(f'<ellipse cx="{x:.1f}" cy="{y:.1f}" rx="{s.r.uniform(26, 60):.1f}" ry="{s.r.uniform(6, 12):.1f}" fill="#fff6d6" opacity="0.12"/>')

        # Left: a tall sea fan, staghorn and a brain coral on the rocks.
        sea_fan(s, 110, 560, 270, 220, "#ff6a45", "#c2412b")
        staghorn(s, 225, 600, 210, "#a46ad8", "#6e3fa0", "#e3c6ff")
        brain_coral(s, 62, 690, 78, 58, "#efb466", "#c98a44", "#a4642a")
        tube_sponges(s, 168, 700, 120, 3, "#ffcc3a", "#e09a1a", "#6a3a10")
        seaweed(s, 20, 700, 50, "#3aa060")
        # Middle: table coral, sponges, the anemone with its clownfish, a clam.
        table_coral(s, 340, 650, 170, "#5cc6b0", "#2f8f88", "#a8f0e0")
        tube_sponges(s, 430, 690, 140, 4, "#ff9a3a", "#d4651a", "#5a2a10", 26)
        staghorn(s, 650, 650, 150, "#ffd0e0", "#e88aa8", "#ffffff", trunks=2, depth=3)
        anemone(s, 545, 712, 78, "#7a3aa0", "#5c2a80", "#ff9ad0", "#d86aaa", "#ffe0f2")
        clownfish(s, 505, 592, 0.95, 1, -8)
        clownfish(s, 600, 612, 0.8, -1, 6)
        giant_clam(s, 668, 742, 112)
        brain_coral(s, 455, 752, 52, 36, "#a8d46a", "#79a84a", "#557f2c")
        # Right: staghorn, another fan and a big brain coral.
        staghorn(s, 790, 650, 220, "#ff7aa8", "#c94a7a", "#ffd0e0")
        sea_fan(s, 905, 560, 240, 190, "#ffb03a", "#c9761f")
        brain_coral(s, 975, 700, 84, 62, "#e2c25a", "#b4932e", "#8a6a1a")
        tube_sponges(s, 868, 712, 96, 3, "#b88aff", "#7f55c8", "#3a1f6a", 24)
        seaweed(s, 1010, 720, 46, "#2f8a50")
        # Up front on the sand.
        for (x, base, w, h) in ((40, 875, 180, 90), (330, 880, 120, 60), (770, 880, 150, 70), (1000, 880, 130, 80)):
            sea_rock(s, x, base, w, h, "#3d7596", "#1a4866")
        urchin(s, 122, 800, 15)
        urchin(s, 850, 822, 12)
        starfish(s, 382, 790, 22, "#ff6a4a", 12)
        starfish(s, 905, 812, 18, "#ffb23a", -20)
        starfish(s, 250, 840, 14, "#ff8aa0", 30)
        for (x, y, r, rot, c) in ((228, 812, 12, -15, "#ffd1c2"), (600, 826, 10, 20, "#ffe8d6"), (712, 806, 9, -10, "#ffd1c2")):
            shell(s, x, y, r, c, rot)
        for x in (180, 300, 520, 740, 940):
            seaweed(s, x, 860 - s.r.uniform(0, 30), s.r.uniform(30, 52), s.r.choice(["#3aa060", "#2f8a50", "#5cb870"]))
        small = (("#ff6a45", "#c2412b"), ("#ffb03a", "#c9761f"))
        for (x, base) in ((30, 860), (1000, 862)):
            c1, c2 = small[int(x > 500)]
            sea_fan(s, x, base, 90, 80, c1, c2)
        # Where the app sways kelp and sends bubbles up.
        for (x, y, h) in ((12, 792, 400), (330, 548, 260), (468, 556, 230), (575, 540, 190), (724, 498, 240),
                          (1020, 800, 420)):
            anchor(s, "kelp", x, y, h)
        for (x, y) in ((668, 712), (225, 420), (430, 560), (975, 640), (840, 790)):
            anchor(s, "bubble", x, y, 0)


# ======================= Volcano: an eruption at night by the sea =======================
def puff(s, cx, cy, r, dark, lit, t):
    """A billow of ash: dark on top, lit by the fire below (t: how strongly)."""
    f = s.lin([(0, dark, 1), (0.5, dark, 1), (1, mix(dark, lit, t), 1)], 0, cy - r, 0, cy + r, "userSpaceOnUse")
    s.add(f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r:.1f}" fill="{f}"/>')


def lava_river(s, pts, glow):
    """A river of lava: a soft glow, red edges, an orange core and a white-hot line, widening downhill."""
    segs = []
    for i in range(len(pts) - 1):
        p0 = pts[i - 1] if i > 0 else pts[i]
        p1, p2 = pts[i], pts[i + 1]
        p3 = pts[i + 2] if i + 2 < len(pts) else p2
        c1 = (p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6)
        c2 = (p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6)
        segs.append((f"M{p1[0]:.1f},{p1[1]:.1f} C{c1[0]:.1f},{c1[1]:.1f} {c2[0]:.1f},{c2[1]:.1f} {p2[0]:.1f},{p2[1]:.1f}",
                     (p1[2] + p2[2]) / 2))
    s.add(f'<g filter="{glow}" opacity="0.5">' + "".join(
        f'<path d="{d}" stroke="#ff4a14" stroke-width="{w * 3.6:.1f}" fill="none" stroke-linecap="round"/>' for d, w in segs) + "</g>")
    for (color, k) in (("#b8240e", 1.45), ("#f0561a", 1.05), ("#ffa22a", 0.62), ("#fff1a8", 0.24)):
        s.add("".join(f'<path d="{d}" stroke="{color}" stroke-width="{w * k:.1f}" fill="none" stroke-linecap="round" '
                      f'stroke-linejoin="round"/>' for d, w in segs))


def fern_sil(s, x, base, h, color, rim=None):
    """A fern in silhouette: a fountain of arching fronds."""
    for ang, ln in ((160, 1.0), (128, 0.9), (100, 0.75), (75, 0.8), (48, 0.95), (20, 0.9), (140, 0.6), (60, 0.6)):
        frond(s, x, base, ang, h * ln, 0.9, h * 0.2, color, rim if 60 < ang < 140 else None, sweep=0.2, hang=0.2)


def ash_bank(s, cx, cy, w, h, dark, lit, t):
    """A soft bank of ash cloud, lit from below."""
    start = len(s.body)
    for row, (n, k, dy) in enumerate(((5, 0.62, -0.25), (7, 0.48, 0.1))):
        for i in range(n):
            px = cx - w / 2 + w * (i + 0.5) / n + s.r.uniform(-w * 0.05, w * 0.05)
            r = h * k * (0.7 + 0.3 * math.sin(math.pi * (i + 0.5) / n)) * s.r.uniform(0.85, 1.15)
            puff(s, px, cy + h * dy, r, dark, lit, t * (0.8 if row == 0 else 1.0))
    grouped(s, start, f'filter="{area_blur(s, 2.5)}"')


@theme("volcano", [(0, "#0b0507"), (0.5, "#1b080b"), (0.82, "#3c0f10"), (1, "#5a1512")])
class Volcano:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 70, 1000, "#ffd9c9", 0.04)
        # Ash drifting at the edges, lit red from below, running down into the scene.
        with Front(s):
            for (cx, cy, w, h, t) in ((-30, 1300, 180, 80, 0.4), (1062, 1290, 190, 86, 0.4),
                                      (40, 1405, 280, 110, 0.6), (996, 1398, 290, 118, 0.6)):
                ash_bank(s, cx, cy, w, h, "#1d0c0e", "#9c3418", t)

    @staticmethod
    def scene(s):
        f = s.lin([(0, "#5a1512", 1), (0.55, "#7c2014", 1), (1, "#a8381a", 1)], 0, 0, 0, 600, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{f}"/>')
        for _ in range(20):
            x, y = s.r.uniform(10, SW - 10), s.r.uniform(6, 200)
            s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(0.8, 1.8):.1f}" fill="#ffd9c9" opacity="{s.r.uniform(0.3, 0.7):.2f}"/>')
        glow = area_blur(s, 14)
        big = area_blur(s, 30)
        soft = area_blur(s, 4)
        cx, cy = 490, 304
        # The sea on the right, red with the fire.
        anchor(s, "water", 0, 556, 0)
        sea = s.lin([(0, "#3a0f12", 1), (1, "#0d0507", 1)], 0, 556, 0, SH, "userSpaceOnUse")
        s.add(f'<rect x="0" y="556" width="{SW}" height="{SH - 556}" fill="{sea}"/>')
        s.add(f'<rect x="0" y="553" width="{SW}" height="6" fill="#ff7a3a" opacity="0.4" filter="{soft}"/>')
        for _ in range(40):
            y = s.r.uniform(566, 840)
            x = s.r.uniform(560, SW)
            s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{s.r.uniform(20, 70):.1f}" height="2.4" rx="1.2" fill="#ff8a3a" '
                  f'opacity="{s.r.uniform(0.15, 0.45):.2f}"/>')
        # A low ridge behind on the left.
        s.add('<path d="M-10,600 L-10,540 Q80,512 170,530 Q240,540 300,560 L300,600Z" fill="#2a0f12"/>')
        # The ash plume, fading out at the top of the scene.
        start = len(s.body)
        rnd = s.r
        for (py, spread, r, t) in ((-60, 260, 150, 0.12), (-10, 230, 140, 0.16), (40, 200, 125, 0.22), (90, 160, 105, 0.3),
                                   (140, 120, 90, 0.42), (185, 80, 72, 0.55), (225, 50, 58, 0.72), (258, 26, 44, 0.9)):
            for k in range(5):
                px = cx + (k - 2) / 2 * spread + rnd.uniform(-24, 24)
                puff(s, px, py + rnd.uniform(-14, 14), r * rnd.uniform(0.8, 1.1), "#22100f", "#e2582a", t)
        grouped(s, start, f'mask="{fade_mask(s, 0, 130)}"')
        s.glow(cx, 250, 260, "#ff7a2a", 0.35)
        # The cone.
        cone = (f"M-10,{SH} L-10,612 C110,592 280,486 400,372 L440,316 Q490,300 540,316 L585,375 "
                f"C650,455 730,520 800,560 C720,650 610,780 545,{SH}Z")
        cf = s.lin([(0, "#3a1a1a", 1), (0.35, "#1f0e10", 1), (1, "#140a0c", 1)], 0, 300, 0, 760, "userSpaceOnUse")
        s.add(f'<path d="{cone}" fill="{cf}"/>')
        # Gullies and ridges down the slopes.
        for (x0, x1, y1) in ((452, 300, 600), (470, 380, 640), (510, 560, 700), (528, 640, 640), (548, 700, 600), (440, 200, 620)):
            mx = (x0 + x1) / 2 + rnd.uniform(-20, 20)
            s.add(f'<path d="M{x0},{318} Q{mx:.1f},{(318 + y1) / 2:.1f} {x1},{y1}" stroke="#0a0506" stroke-width="5" fill="none" opacity="0.6"/>')
            s.add(f'<path d="M{x0 + 6},{318} Q{mx + 6:.1f},{(318 + y1) / 2:.1f} {x1 + 8},{y1}" stroke="#a8401c" stroke-width="2" '
                  f'fill="none" opacity="0.3"/>')
        s.add(f'<path d="M400,372 L440,316 Q490,300 540,316 L585,375" stroke="#ff8a3a" stroke-width="4" fill="none" opacity="0.7" '
              f'filter="{glow}"/>')
        # The crater, glowing.
        anchor(s, "crater", cx, cy, 116)
        anchor(s, "glow", cx, cy - 6, 160)
        anchor(s, "plume", cx, cy - 20, 260)
        s.glow(cx, cy, 170, "#ffb03a", 0.6)
        s.add(f'<ellipse cx="{cx}" cy="{cy + 2}" rx="56" ry="13" fill="#ff8a2a"/>')
        s.add(f'<ellipse cx="{cx}" cy="{cy + 1}" rx="40" ry="8" fill="#ffd36b"/>')
        s.add(f'<ellipse cx="{cx}" cy="{cy}" rx="22" ry="4" fill="#fff6c8"/>')
        # Lava rivers: two run down to the sea, one down the left side.
        rivers = {
            "lava1": [(535, 316, 8), (556, 350, 9), (574, 390, 10), (590, 430, 11), (612, 468, 12), (636, 505, 13),
                      (660, 540, 14), (684, 575, 15), (700, 608, 16), (712, 630, 17), (716, 646, 18)],
            "lava2": [(492, 318, 8), (490, 360, 9), (496, 405, 10), (506, 450, 11), (520, 495, 12), (538, 540, 13),
                      (558, 585, 14), (578, 630, 15), (598, 675, 16), (612, 718, 17), (624, 752, 18)],
            "lava3": [(452, 318, 7), (436, 352, 8), (414, 392, 9), (390, 430, 10), (364, 466, 11), (336, 500, 12),
                      (306, 532, 13), (278, 560, 14), (256, 584, 15), (240, 602, 16), (232, 616, 18)],
        }
        for key, pts in rivers.items():
            for (x, y, w) in pts:
                anchor(s, key, x, y, w)
            lava_river(s, pts, glow)
        # A glowing pool where the left river spreads out.
        s.add(f'<ellipse cx="226" cy="628" rx="78" ry="18" fill="#ff5a1a" opacity="0.6" filter="{glow}"/>')
        s.add('<ellipse cx="226" cy="628" rx="52" ry="10" fill="#ffa22a"/><ellipse cx="222" cy="627" rx="24" ry="4.5" fill="#fff1a8"/>')
        # Little glowing cracks on the slopes.
        for _ in range(26):
            x = rnd.uniform(300, 700)
            y = rnd.uniform(380, 640)
            if y < 300 + abs(x - cx) * 0.95:
                continue
            s.add(f'<path d="M{x:.1f},{y:.1f} l{rnd.uniform(-10, 10):.1f},{rnd.uniform(4, 12):.1f} l{rnd.uniform(-8, 8):.1f},{rnd.uniform(4, 10):.1f}" '
                  f'stroke="#ff7a2a" stroke-width="1.8" fill="none" opacity="0.7"/>')
        # Where lava meets the sea: a faint mist and red water.
        for (x, y) in ((716, 646), (624, 752)):
            anchor(s, "steam", x, y, 1)
            s.add(f'<ellipse cx="{x + 30}" cy="{y - 20}" rx="70" ry="30" fill="#ffd6c9" opacity="0.18" filter="{big}"/>')
            s.glow(x + 20, y + 10, 90, "#ff6a2a", 0.4)
        # Sea stacks on the right.
        for (x, base, w, h) in ((900, 640, 60, 70), (980, 690, 80, 110), (830, 760, 50, 40)):
            s.add(f'<path d="M{x - w / 2},{base} L{x - w * 0.4},{base - h * 0.7} L{x - w * 0.1},{base - h} L{x + w * 0.3},{base - h * 0.85} '
                  f'L{x + w / 2},{base}Z" fill="#120809"/>')
            s.add(f'<path d="M{x - w * 0.4},{base - h * 0.7} L{x - w * 0.1},{base - h} L{x + w * 0.3},{base - h * 0.85}" stroke="#c2441e" '
                  f'stroke-width="2" fill="none" opacity="0.7"/>')
            s.add(f'<rect x="{x - w / 2 - 10}" y="{base - 2}" width="{w + 20}" height="3" fill="#ff9a5a" opacity="0.4"/>')
        # Black lava rock in front, lit along its edges, with palms and ferns.
        rock = ("M-10,860 L-10,720 L40,700 L90,712 L140,690 L200,708 L260,700 L320,722 L380,712 L430,734 L480,742 "
                "L530,770 L560,800 L590,860Z")
        s.add(f'<path d="{rock}" fill="#0c0607"/>')
        s.add('<path d="M-10,720 L40,700 L90,712 L140,690 L200,708 L260,700 L320,722 L380,712 L430,734 L480,742 L530,770 L560,800" '
              'stroke="#a8401c" stroke-width="2.5" fill="none" opacity="0.65"/>')
        s.add('<path d="M1032,860 L1032,770 L980,780 L930,800 L880,812 L840,840 L820,860Z" fill="#0c0607"/>')
        s.add('<path d="M1032,770 L980,780 L930,800 L880,812 L840,840" stroke="#a8401c" stroke-width="2.5" fill="none" opacity="0.6"/>')
        palm_sil(s, 34, 866, 280, -0.25, "#070304", "#c2441e")
        palm_sil(s, 1000, 866, 250, -0.55, "#070304", "#c2441e")
        fern_sil(s, 270, 866, 70, "#070304", "#c2441e")
        fern_sil(s, 400, 866, 56, "#070304", "#c2441e")
        fern_sil(s, 890, 866, 52, "#070304", "#c2441e")


# ======================= Outer Space: the Earth rising over the Moon =======================
def nebula(s, x, y, rx, ry, color, a, blur):
    s.add(f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="{color}" opacity="{a}" filter="{blur}"/>')


def ringed_planet(s, x, y, r, c1, c2, band, ring1, ring2, tilt=-16):
    """A planet with rings: the back of the rings behind it, the front across it."""
    back, front = s.id("rb"), s.id("rf")
    s.defs.append(f'<clipPath id="{back}"><rect x="{-3 * r}" y="{-2 * r}" width="{6 * r}" height="{2 * r}"/></clipPath>')
    s.defs.append(f'<clipPath id="{front}"><rect x="{-3 * r}" y="0" width="{6 * r}" height="{2 * r}"/></clipPath>')
    rings = (f'<ellipse cx="0" cy="0" rx="{r * 2.1:.1f}" ry="{r * 0.52:.1f}" fill="none" stroke="{ring1}" stroke-width="{r * 0.16:.1f}"/>'
             f'<ellipse cx="0" cy="0" rx="{r * 1.72:.1f}" ry="{r * 0.42:.1f}" fill="none" stroke="{ring2}" stroke-width="{r * 0.2:.1f}"/>'
             f'<ellipse cx="0" cy="0" rx="{r * 1.45:.1f}" ry="{r * 0.35:.1f}" fill="none" stroke="{ring1}" stroke-width="{r * 0.06:.1f}" opacity="0.6"/>')
    body = s.rad([(0, c1, 1), (1, c2, 1)], -r * 0.4, -r * 0.4, r * 1.5)
    pid = s.id("pl")
    s.defs.append(f'<clipPath id="{pid}"><circle cx="0" cy="0" r="{r}"/></clipPath>')
    bands = "".join(f'<rect x="{-r}" y="{-r + k * r * 0.28:.1f}" width="{2 * r}" height="{r * 0.12:.1f}" fill="{band}" opacity="0.45"/>'
                    for k in range(8))
    s.add(f'<g transform="translate({x},{y}) rotate({tilt})"><g clip-path="url(#{back})">{rings}</g>'
          f'<circle cx="0" cy="0" r="{r}" fill="{body}"/><g clip-path="url(#{pid})">{bands}'
          f'<circle cx="{r * 0.45:.1f}" cy="{r * 0.35:.1f}" r="{r * 1.1:.1f}" fill="#000000" opacity="0.28"/></g>'
          f'<g clip-path="url(#{front})">{rings}</g></g>')


def earth(s, x, y, r):
    """The Earth: oceans, continents and swirling clouds, lit from the left, a thin glowing sky around it."""
    cid = s.id("ea")
    s.defs.append(f'<clipPath id="{cid}"><circle cx="{x}" cy="{y}" r="{r}"/></clipPath>')
    s.glow(x, y, r * 1.45, "#4fa8ff", 0.4)
    ocean = s.rad([(0, "#5ab4f5", 1), (0.6, "#2272cf", 1), (1, "#164c98", 1)], x - r * 0.4, y - r * 0.4, r * 1.5)
    parts = [f'<circle cx="{x}" cy="{y}" r="{r}" fill="{ocean}"/>']
    rr = random.Random(41)
    lands = ((x - r * 0.45, y - r * 0.35, r * 0.34, r * 0.42, "#5aa04a"), (x - r * 0.2, y + r * 0.42, r * 0.2, r * 0.36, "#6aa850"),
             (x + r * 0.4, y - r * 0.2, r * 0.42, r * 0.3, "#7a9a4a"), (x + r * 0.3, y + r * 0.35, r * 0.25, r * 0.2, "#5aa04a"),
             (x - r * 0.75, y + r * 0.1, r * 0.14, r * 0.22, "#6aa850"))
    for (lx, ly, rx_, ry_, c) in lands:
        parts.append(f'<path d="{blob(rr, lx, ly, rx_, ry_, 12, 0.3)}" fill="{c}"/>')
        parts.append(f'<path d="{blob(rr, lx + rx_ * 0.2, ly - ry_ * 0.1, rx_ * 0.5, ry_ * 0.4, 9, 0.35)}" fill="#c4a868" opacity="0.8"/>')
    parts.append(f'<ellipse cx="{x - r * 0.1}" cy="{y - r * 0.98}" rx="{r * 0.6}" ry="{r * 0.14}" fill="#f4f8ff"/>')
    for (ix, iy, ir) in ((0.62, -0.62, 0.05), (0.05, -0.05, 0.04), (-0.55, 0.62, 0.05), (0.7, 0.62, 0.04), (0.15, 0.78, 0.06)):
        parts.append(f'<path d="{blob(rr, x + r * ix, y + r * iy, r * ir * 1.4, r * ir, 8, 0.3)}" fill="#6aa850"/>')
    for k in range(8):
        cy_ = y - r * 0.72 + k * r * 0.2 + rr.uniform(-6, 6)
        cx_ = x + rr.uniform(-r * 0.55, r * 0.3)
        w = rr.uniform(r * 0.22, r * 0.5)
        parts.append(f'<path d="M{cx_ - w / 2:.1f},{cy_:.1f} q{w * 0.25:.1f},{-r * 0.05:.1f} {w * 0.5:.1f},0 t{w * 0.5:.1f},0" '
                     f'stroke="#ffffff" stroke-width="{rr.uniform(2.5, 4.5):.1f}" fill="none" stroke-linecap="round" opacity="0.6"/>')
    for _ in range(22):
        parts.append(f'<path d="{blob(rr, x + rr.uniform(-r * 0.8, r * 0.8), y + rr.uniform(-r * 0.8, r * 0.8), rr.uniform(6, 20), rr.uniform(3, 9), 8, 0.4)}" '
                     f'fill="#ffffff" opacity="{rr.uniform(0.5, 0.8):.2f}"/>')
    sx, sy = x - r * 0.3, y + r * 0.1
    spiral = " ".join(f"L{sx + (3 + t * 3.2) * math.cos(t):.1f},{sy + (3 + t * 3.2) * math.sin(t) * 0.8:.1f}" for t in [i * 0.35 for i in range(28)])
    parts.append(f'<path d="M{sx:.1f},{sy:.1f} {spiral}" stroke="#ffffff" stroke-width="3" fill="none" opacity="0.75" stroke-linecap="round"/>')
    shade = s.lin([(0, "#000814", 0), (0.5, "#000814", 0), (0.72, "#020616", 0.6), (1, "#020616", 0.92)], x - r, 0, x + r, 0, "userSpaceOnUse")
    parts.append(f'<rect x="{x - r}" y="{y - r}" width="{2 * r}" height="{2 * r}" fill="{shade}"/>')
    for _ in range(26):
        lx, ly = x + rr.uniform(r * 0.45, r * 0.9), y + rr.uniform(-r * 0.6, r * 0.6)
        if (lx - x) ** 2 + (ly - y) ** 2 < (r * 0.95) ** 2:
            parts.append(f'<circle cx="{lx:.1f}" cy="{ly:.1f}" r="{rr.uniform(0.8, 1.8):.1f}" fill="#ffd27a" opacity="0.8"/>')
    s.add(f'<g clip-path="url(#{cid})">{"".join(parts)}</g>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{r - 1}" fill="none" stroke="#9fdcff" stroke-width="5" opacity="0.5" filter="{s.blur(3)}"/>')
    s.add(f'<path d="M{x - r * 0.2:.1f},{y - r * 0.98:.1f} A{r},{r} 0 0,0 {x - r * 0.2:.1f},{y + r * 0.98:.1f}" stroke="#d6f2ff" '
          f'stroke-width="2.5" fill="none" opacity="0.7"/>')


def crater(s, x, y, rx, ry, a=1.0):
    """A crater: a raised rim lit on its far side, a dark bowl, the inside wall facing the sun lit."""
    cid = s.id("cr")
    s.defs.append(f'<clipPath id="{cid}"><ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}"/></clipPath>')
    s.add(f'<g opacity="{a}"><ellipse cx="{x}" cy="{y + ry * 0.12:.1f}" rx="{rx * 1.16:.1f}" ry="{ry * 1.3:.1f}" fill="#6e6c80" opacity="0.6"/>'
          f'<ellipse cx="{x}" cy="{y - ry * 0.08:.1f}" rx="{rx * 1.1:.1f}" ry="{ry * 1.2:.1f}" fill="#c9c7d6"/>'
          f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#5c5a6e"/>'
          f'<g clip-path="url(#{cid})"><ellipse cx="{x + rx * 0.35:.1f}" cy="{y + ry * 0.25:.1f}" rx="{rx * 0.92:.1f}" ry="{ry * 0.9:.1f}" '
          f'fill="#8e8ca0"/></g></g>')


def lander(s, x, base, k=1.0):
    """A lunar lander on its four legs: a gold-foil lower stage and the crew cabin on top."""
    def p(dx, dy):
        return f"{x + dx * k:.1f},{base + dy * k:.1f}"
    s.add(f'<ellipse cx="{x + 60 * k:.1f}" cy="{base + 4 * k:.1f}" rx="{140 * k:.1f}" ry="{14 * k:.1f}" fill="#2a2838" opacity="0.5"/>')
    leg = "#b9bcc8"
    for sx in (-1, 1):
        s.add(f'<path d="M{p(sx * 58, -98)} L{p(sx * 100, -8)} M{p(sx * 40, -64)} L{p(sx * 86, -38)} M{p(sx * 58, -78)} L{p(sx * 94, -26)}" '
              f'stroke="{leg}" stroke-width="{4 * k:.1f}" stroke-linecap="round"/>')
        s.add(f'<ellipse cx="{x + sx * 100 * k:.1f}" cy="{base - 4 * k:.1f}" rx="{17 * k:.1f}" ry="{5 * k:.1f}" fill="#d9dbe4"/>')
    # The lower stage: gold foil.
    body = f"M{p(-62, -112)} L{p(-46, -128)} L{p(46, -128)} L{p(62, -112)} L{p(62, -76)} L{p(46, -62)} L{p(-46, -62)} L{p(-62, -76)}Z"
    gold = s.lin([(0, "#f2c55a", 1), (0.5, "#d9a03a", 1), (1, "#a8702a", 1)], x - 62 * k, 0, x + 62 * k, 0, "userSpaceOnUse")
    s.add(f'<path d="{body}" fill="{gold}"/>')
    rr = random.Random(7)
    for _ in range(14):
        x0, y0 = rr.uniform(-55, 50), rr.uniform(-120, -70)
        s.add(f'<path d="M{p(x0, y0)} l{rr.uniform(6, 16) * k:.1f},{rr.uniform(-6, 6) * k:.1f} l{rr.uniform(4, 10) * k:.1f},{rr.uniform(-5, 5) * k:.1f}" '
              f'stroke="{rr.choice(["#fff1b0", "#8a5a1a"])}" stroke-width="{1.4 * k:.1f}" fill="none" opacity="0.6"/>')
    s.add(f'<path d="M{p(-14, -62)} L{p(14, -62)} L{p(20, -42)} L{p(-20, -42)}Z" fill="#4a4a58"/>')
    # The ladder down the front leg, and a little porch.
    s.add(f'<path d="M{p(-8, -62)} L{p(-4, -2)} M{p(8, -62)} L{p(12, -2)}" stroke="{leg}" stroke-width="{2.5 * k:.1f}"/>')
    for i in range(6):
        yy = -56 + i * 10
        s.add(f'<path d="M{p(-8 + (yy + 62) / 15, yy)} L{p(8 + (yy + 62) / 15, yy)}" stroke="{leg}" stroke-width="{2 * k:.1f}"/>')
    s.add(f'<ellipse cx="{x + 4 * k:.1f}" cy="{base:.1f}" rx="{17 * k:.1f}" ry="{5 * k:.1f}" fill="#d9dbe4"/>')
    # The crew cabin.
    cab = f"M{p(-56, -128)} L{p(-60, -168)} L{p(-36, -198)} L{p(18, -205)} L{p(52, -186)} L{p(58, -142)} L{p(50, -128)}Z"
    cg = s.lin([(0, "#e6e8f0", 1), (0.6, "#b8bccb", 1), (1, "#8c90a2", 1)], x - 60 * k, 0, x + 58 * k, 0, "userSpaceOnUse")
    s.add(f'<path d="{cab}" fill="{cg}"/>')
    s.add(f'<path d="M{p(-60, -168)} L{p(58, -160)} M{p(-20, -128)} L{p(-24, -200)}" stroke="#7c8092" stroke-width="{1.6 * k:.1f}" opacity="0.7"/>')
    s.add(f'<path d="M{p(-34, -186)} L{p(-12, -190)} L{p(-14, -170)}Z M{p(4, -191)} L{p(26, -188)} L{p(20, -170)}Z" fill="#1a1c2a"/>')
    s.add(f'<rect x="{x - 14 * k:.1f}" y="{base - 160 * k:.1f}" width="{26 * k:.1f}" height="{26 * k:.1f}" rx="{3 * k:.1f}" fill="#8a8ea0"/>')
    s.add(f'<rect x="{x - 10 * k:.1f}" y="{base - 156 * k:.1f}" width="{18 * k:.1f}" height="{18 * k:.1f}" rx="{2 * k:.1f}" fill="#5c6072"/>')
    s.add(f'<rect x="{x - 58 * k:.1f}" y="{base - 132 * k:.1f}" width="{114 * k:.1f}" height="{6 * k:.1f}" fill="#2a2a36"/>')
    s.add(f'<path d="M{p(-40, -128)} L{p(-44, -168)} M{p(36, -128)} L{p(40, -176)}" stroke="#8c90a2" stroke-width="{1.2 * k:.1f}" opacity="0.7"/>')
    s.add(f'<path d="M{p(-30, -184)} L{p(-18, -186)}" stroke="#6a7aa8" stroke-width="{1.5 * k:.1f}"/>')
    s.add(f'<rect x="{x - 12 * k:.1f}" y="{base - 213 * k:.1f}" width="{24 * k:.1f}" height="{9 * k:.1f}" fill="#9a9eb0"/>')
    for (dx, dy) in ((-62, -164), (60, -150)):
        s.add(f'<path d="M{p(dx - 8, dy)} L{p(dx + 8, dy)} M{p(dx, dy - 8)} L{p(dx, dy + 8)}" stroke="#5a5e70" stroke-width="{3 * k:.1f}"/>')
    # The antenna mast and dish, with its light.
    s.add(f'<path d="M{p(38, -196)} L{p(46, -238)}" stroke="#c9ccd8" stroke-width="{2.4 * k:.1f}"/>')
    s.add(f'<ellipse cx="{x + 46 * k:.1f}" cy="{base - 238 * k:.1f}" rx="{14 * k:.1f}" ry="{5 * k:.1f}" fill="#e6e8f0" '
          f'transform="rotate(-20 {x + 46 * k:.1f} {base - 238 * k:.1f})"/>')
    anchor(s, "antenna", x + 47 * k, base - 246 * k, 0)
    s.add(f'<circle cx="{x + 47 * k:.1f}" cy="{base - 246 * k:.1f}" r="{3 * k:.1f}" fill="#ff5a5a"/>')


@theme("space", [(0, "#04030d"), (0.5, "#0a0820"), (1, "#141238")])
class Space:
    @staticmethod
    def deco(s):
        nb = area_blur(s, 46)
        for (x, y, rx, ry, c, a) in ((990, 820, 230, 170, "#7a3cff", 0.26), (1010, 700, 140, 90, "#ff4fa3", 0.14),
                                     (20, 420, 160, 140, "#2f7aff", 0.14)):
            nebula(s, x, y, rx, ry, c, a, nb)
        # The low ones run on into the scene, so they're drawn over it (no edge where the scene starts).
        with Front(s):
            for (x, y, rx, ry, c, a) in ((30, 1150, 250, 190, "#2f7aff", 0.22), (60, 1260, 160, 100, "#b84fff", 0.16),
                                         (520, 1370, 520, 70, "#7a3cff", 0.12)):
                nebula(s, x, y, rx, ry, c, a, nb)
        stars_top_safe(s, 240, BAND_Y - 12, "#ffffff", 0.06)
        for _ in range(26):
            x, y = s.r.uniform(10, SW - 10), s.r.uniform(TOP_CLEAR, BAND_Y - 6)
            s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(1, 2):.1f}" fill="{s.r.choice(["#ffd6a8", "#a8d0ff", "#ffb8e0"])}" '
                  f'opacity="0.8"/>')
        ringed_planet(s, 922, 420, 58, "#f2d29a", "#b07a4a", "#c98a5a", "#e8d2b0", "#b89870")
        # A small red planet and a far galaxy on the left.
        rp = s.rad([(0, "#f08a5a", 1), (1, "#8a2a1a", 1)], 92, 572, 44)
        s.add(f'<circle cx="104" cy="584" r="32" fill="{rp}"/>')
        for (dx, dy, r_) in ((-8, -6, 6), (10, 8, 4), (-2, 14, 3)):
            s.add(f'<circle cx="{104 + dx}" cy="{584 + dy}" r="{r_}" fill="#6a1a10" opacity="0.4"/>')
        s.add('<ellipse cx="100" cy="556" rx="12" ry="4" fill="#ffffff" opacity="0.8"/>')
        s.add('<circle cx="116" cy="592" r="30" fill="#000000" opacity="0.25"/>')
        g = s.blur(3)
        s.add(f'<g transform="translate(150,350) rotate(-25)"><ellipse cx="0" cy="0" rx="46" ry="14" fill="#c8b8ff" opacity="0.22" filter="{g}"/>'
              f'<path d="M-40,4 Q-10,-16 0,0 Q10,16 40,-4" stroke="#e6dcff" stroke-width="4" fill="none" opacity="0.4" filter="{g}"/>'
              f'<circle cx="0" cy="0" r="5" fill="#ffffff" opacity="0.8"/></g>')

    @staticmethod
    def scene(s):
        f = s.lin([(0, "#141238", 1), (1, "#0b0a26", 1)], 0, 0, 0, 480, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{f}"/>')
        s.add(f'<ellipse cx="300" cy="240" rx="360" ry="70" fill="#7a3cff" opacity="0.12" filter="{area_blur(s, 40)}"/>')
        s.stars(80, 470, "#ffffff", 0.06)
        earth(s, 780, 330, 150)
        anchor(s, "earth", 780, 330, 150)
        # The Moon's ground, curving away.
        horizon = "M-10,500 Q520,418 1042,478"
        far = s.lin([(0, "#b4b2c4", 1), (1, "#8e8ca2", 1)], 0, 430, 0, 560, "userSpaceOnUse")
        s.add(f'<path d="M-10,{SH} L-10,500 Q520,418 1042,478 L1042,{SH}Z" fill="{far}"/>')
        # Low crater rims along the horizon.
        for (x, w, h) in ((90, 160, 14), (300, 120, 10), (560, 200, 12), (930, 180, 16)):
            y = (1 - x / SW) ** 2 * 500 + 2 * (1 - x / SW) * (x / SW) * 418 + (x / SW) ** 2 * 478
            s.add(f'<path d="M{x - w / 2:.1f},{y + 4:.1f} Q{x - w * 0.3:.1f},{y - h:.1f} {x:.1f},{y - h * 0.8:.1f} Q{x + w * 0.3:.1f},{y - h:.1f} '
                  f'{x + w / 2:.1f},{y + 4:.1f}Z" fill="#9e9cb0"/>')
        near = s.lin([(0, "#a6a4b8", 1), (1, "#6c6a80", 1)], 0, 470, 0, SH, "userSpaceOnUse")
        s.add(f'<path d="M-10,{SH} L-10,560 Q300,520 600,548 Q850,570 1042,540 L1042,{SH}Z" fill="{near}"/>')
        s.add(f'<path d="{horizon}" stroke="#e6e4f2" stroke-width="2" fill="none" opacity="0.5"/>')
        for (x, y, rx, ry, a) in ((620, 470, 70, 12, 0.8), (180, 482, 50, 9, 0.8), (880, 500, 60, 10, 0.8), (420, 520, 46, 9, 0.85),
                                  (720, 600, 110, 22, 1), (120, 640, 90, 18, 1), (520, 700, 140, 28, 1), (900, 720, 120, 26, 1),
                                  (320, 790, 90, 20, 1), (980, 610, 50, 11, 1), (40, 780, 60, 14, 1)):
            crater(s, x, y, rx, ry, a)
        # Rocks with long shadows.
        for _ in range(22):
            x, y = s.r.uniform(0, SW), s.r.uniform(560, 850)
            r_ = s.r.uniform(4, 13) * (0.6 + (y - 560) / 400)
            s.add(f'<ellipse cx="{x + r_ * 1.6:.1f}" cy="{y + r_ * 0.4:.1f}" rx="{r_ * 2:.1f}" ry="{r_ * 0.4:.1f}" fill="#3a384a" opacity="0.45"/>')
            s.add(f'<path d="{blob(s.r, x, y - r_ * 0.3, r_, r_ * 0.7, 7, 0.25)}" fill="#8a889c"/>')
            s.add(f'<ellipse cx="{x - r_ * 0.3:.1f}" cy="{y - r_ * 0.6:.1f}" rx="{r_ * 0.45:.1f}" ry="{r_ * 0.25:.1f}" fill="#d6d4e2" opacity="0.6"/>')
        # Footprints leading away from the lander.
        for i in range(14):
            fx = 300 + i * 26 + 8 * math.sin(i * 0.7)
            fy = 662 + i * 7 + (6 if i % 2 else -6)
            s.add(f'<ellipse cx="{fx:.1f}" cy="{fy:.1f}" rx="7" ry="3.2" fill="#55536a" opacity="0.7" transform="rotate(14 {fx:.1f} {fy:.1f})"/>')
            s.add(f'<path d="M{fx - 5:.1f},{fy:.1f} h10" stroke="#3e3c50" stroke-width="1" opacity="0.6"/>')
        lander(s, 230, 650, 1.0)
        anchor(s, "astro", 470, 300, 90)


# ======================= Retro Sunset: a synthwave sun over a neon grid =======================
RETRO_TOP, RETRO_HORIZON = "#4c1166", "#c2346a"


def wire_mountains(s, pts, base, fill, edge, inner, glow):
    """Mountains drawn like an old vector game: dark faces, a glowing ridge line and lines across the faces."""
    d = f"M{pts[0][0]},{base} " + " ".join(f"L{x},{y}" for x, y in pts) + f" L{pts[-1][0]},{base}Z"
    s.add(f'<path d="{d}" fill="{fill}"/>')
    lines = []
    for i, (x, y) in enumerate(pts[1:-1], 1):
        lines.append(f"M{x},{y} L{(pts[i - 1][0] + x) / 2 + (x - pts[i - 1][0]) * 0.1:.1f},{base}")
        lines.append(f"M{x},{y} L{(pts[i + 1][0] + x) / 2 - (pts[i + 1][0] - x) * 0.1:.1f},{base}")
    for k in range(1, 4):
        yy = base - (base - min(y for _, y in pts)) * k / 4
        xs = []
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            if (y0 - yy) * (y1 - yy) < 0:
                xs.append(x0 + (yy - y0) * (x1 - x0) / (y1 - y0))
        for a_, b_ in zip(xs[::2], xs[1::2]):
            lines.append(f"M{a_:.1f},{yy:.1f} L{b_:.1f},{yy:.1f}")
    s.add(f'<path d="{" ".join(lines)}" stroke="{inner}" stroke-width="1.5" fill="none" opacity="0.45"/>')
    ridge = "M" + " L".join(f"{x},{y}" for x, y in pts)
    s.add(f'<path d="{ridge}" stroke="{edge}" stroke-width="7" fill="none" opacity="0.45" stroke-linejoin="round" filter="{glow}"/>')
    s.add(f'<path d="{ridge}" stroke="{edge}" stroke-width="2.5" fill="none" stroke-linejoin="round"/>')


@theme("retro", [(0, "#0d0624"), (0.5, "#1e0940"), (0.8, "#36105a"), (1, RETRO_TOP)])
class Retro:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 140, 1250, "#ffffff", 0.05)
        for _ in range(16):
            p = spot(s.r, TOP_CLEAR, 1300, 14)
            if p:
                s.sparkle(p[0], p[1], s.r.uniform(4, 7), s.r.choice(["#ff8ad8", "#8af0ff"]), 0.8)
        g = area_blur(s, 2)
        for (x0, x1, y, a) in ((0, 250, 1352, 0.5), (40, 300, 1368, 0.35), (760, 1032, 1348, 0.5), (720, 990, 1366, 0.35)):
            s.add(f'<path d="M{x0},{y} H{x1}" stroke="#ff7ad9" stroke-width="2" opacity="{a}" filter="{g}"/>')

    @staticmethod
    def scene(s):
        hy, cx = 430, 516
        sky = s.lin([(0, RETRO_TOP, 1), (1, RETRO_HORIZON, 1)], 0, 0, 0, hy, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{hy + 2}" fill="{sky}"/>')
        anchor(s, "sky", 0, 0, rgb(RETRO_TOP))
        anchor(s, "sky", 0, hy, rgb(RETRO_HORIZON))
        for _ in range(40):
            x, y = s.r.uniform(10, SW - 10), s.r.uniform(8, 260)
            s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(0.8, 1.8):.1f}" fill="#ffffff" opacity="{s.r.uniform(0.3, 0.8):.2f}"/>')
        glow = area_blur(s, 6)
        # The sun: yellow at the top to hot pink, cut off by the horizon.
        sy, sr = 300, 200
        anchor(s, "sun", cx, sy, sr)
        s.glow(cx, sy, sr * 1.7, "#ff5aa8", 0.45)
        sf = s.lin([(0, "#fff36b", 1), (0.45, "#ffb347", 1), (1, "#ff4f8b", 1)], 0, sy - sr, 0, hy, "userSpaceOnUse")
        clip = s.id("sc")
        s.defs.append(f'<clipPath id="{clip}"><rect x="0" y="0" width="{SW}" height="{hy}"/></clipPath>')
        s.add(f'<g clip-path="url(#{clip})"><circle cx="{cx}" cy="{sy}" r="{sr}" fill="{sf}"/></g>')
        # Thin glowing streaks of cloud across the sky.
        for (x0, x1, y, a) in ((0, 270, 64, 0.55), (60, 220, 84, 0.35), (780, 1032, 52, 0.5), (830, 1000, 72, 0.35),
                               (0, 160, 170, 0.35), (900, 1032, 190, 0.35)):
            s.add(f'<path d="M{x0},{y} H{x1}" stroke="#ff8ad8" stroke-width="3" opacity="{a}" stroke-linecap="round" filter="{area_blur(s, 1.5)}"/>')
        # Mountains, left and right.
        wire_mountains(s, [(-10, 330), (60, 262), (122, 318), (196, 236), (262, 330), (290, 372), (322, 404), (348, hy)], hy,
                       "#1a0832", "#ff4fd8", "#3ae7ff", glow)
        wire_mountains(s, [(684, hy), (708, 398), (748, 352), (800, 300), (852, 226), (912, 296), (972, 250), (1042, 300)], hy,
                       "#1a0832", "#3ae7ff", "#ff4fd8", glow)
        # The floor: dark, with glowing lines running to the vanishing point.
        anchor(s, "horizon", cx, hy, 0)
        floor = s.lin([(0, "#2a0846", 1), (1, "#0b0218", 1)], 0, hy, 0, SH, "userSpaceOnUse")
        s.add(f'<rect x="0" y="{hy}" width="{SW}" height="{SH - hy}" fill="{floor}"/>')
        s.add(f'<ellipse cx="{cx}" cy="{hy + 30}" rx="320" ry="60" fill="#ff6aa8" opacity="0.3" filter="{area_blur(s, 20)}"/>')
        start = len(s.body)
        lines = " ".join(f"M{cx},{hy} L{cx + k * 118},{SH + 4}" for k in range(-17, 18))
        s.add(f'<path d="{lines}" stroke="#ff3dd6" stroke-width="7" fill="none" opacity="0.35" filter="{glow}"/>')
        s.add(f'<path d="{lines}" stroke="#ff7ae6" stroke-width="2.4" fill="none"/>')
        grouped(s, start, f'mask="{fade_mask(s, hy, hy + 220, top=hy, bottom=SH + 10)}"')
        # The horizon, glowing.
        s.add(f'<rect x="0" y="{hy - 6}" width="{SW}" height="12" fill="#ff7ad9" opacity="0.5" filter="{area_blur(s, 8)}"/>')
        s.add(f'<rect x="0" y="{hy - 1.5}" width="{SW}" height="3" fill="#ffd6f2"/>')
        # Palms in silhouette standing on the horizon at the far edges, lit along one side (the floor stays clear:
        # the app's moving grid runs over everything below the horizon).
        palm_sil(s, 132, hy + 2, 200, 0.45, "#12031e", "#ff4fa3")
        palm_sil(s, 42, hy + 2, 300, 0.3, "#12031e", "#ff4fa3")
        palm_sil(s, 900, hy + 2, 210, -0.45, "#12031e", "#3ae7ff")
        palm_sil(s, 992, hy + 2, 320, -0.3, "#12031e", "#3ae7ff")
