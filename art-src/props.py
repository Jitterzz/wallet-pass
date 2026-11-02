"""Props for the theme scenes, drawn on an S (see base.py)."""
from base import *  # noqa: F401,F403


# ---------- harvest ----------
def pumpkin2(s, x, base, r, c1="#d9621f", c2="#f2913a", stem="#5b6b2a", face=False, light="#ffd36b"):
    """A ridged pumpkin sitting on [base]; a glowing jack-o'-lantern face if asked."""
    cy = base - r * 0.82
    if face:
        anchor(s, "lantern", x, cy, r)
        s.glow(x, cy, r * 2.8, "#ff9a2a", 0.4)
    s.add(f'<path d="M{x - r * 0.1},{cy - r * 0.78} q-2,-{r * 0.32:.1f} {r * 0.16:.1f},-{r * 0.42:.1f}" stroke="{stem}" '
          f'stroke-width="{r * 0.17:.1f}" fill="none" stroke-linecap="round"/>')
    for dx, rx, c in ((-0.55, 0.55, c1), (0.55, 0.55, c1), (-0.28, 0.5, c2), (0.28, 0.5, c2), (0, 0.42, c2)):
        s.add(f'<ellipse cx="{x + dx * r:.1f}" cy="{cy:.1f}" rx="{rx * r:.1f}" ry="{r * 0.82:.1f}" fill="{c}"/>')
    for dx in (-0.3, 0.3):
        s.add(f'<path d="M{x + dx * r:.1f},{cy - r * 0.74:.1f} Q{x + dx * r * 1.25:.1f},{cy:.1f} {x + dx * r:.1f},{cy + r * 0.74:.1f}" '
              f'stroke="{c1}" stroke-width="{max(1.5, r * 0.06):.1f}" fill="none" opacity="0.7"/>')
    s.add(f'<ellipse cx="{x - r * 0.42:.1f}" cy="{cy - r * 0.38:.1f}" rx="{r * 0.12:.1f}" ry="{r * 0.24:.1f}" '
          f'fill="#ffffff" opacity="0.25" transform="rotate(20 {x - r * 0.42:.1f} {cy - r * 0.38:.1f})"/>')
    s.add(f'<path d="M{x + r * 0.05},{cy - r * 0.86} q{r * 0.3:.1f},-{r * 0.2:.1f} {r * 0.5:.1f},0 q{r * 0.12:.1f},{r * 0.1:.1f} 0,{r * 0.18:.1f}" '
          f'stroke="{stem}" stroke-width="{max(1.2, r * 0.05):.1f}" fill="none"/>')
    if face:
        f = light
        s.add(f'<path d="M{x - r * 0.48},{cy - r * 0.08} l{r * 0.17:.1f},-{r * 0.3:.1f} l{r * 0.17:.1f},{r * 0.3:.1f}Z '
              f'M{x + r * 0.14},{cy - r * 0.08} l{r * 0.17:.1f},-{r * 0.3:.1f} l{r * 0.17:.1f},{r * 0.3:.1f}Z '
              f'M{x - r * 0.08},{cy + r * 0.06} l{r * 0.08:.1f},-{r * 0.14:.1f} l{r * 0.08:.1f},{r * 0.14:.1f}Z '
              f'M{x - r * 0.55},{cy + r * 0.22} Q{x},{cy + r * 0.72} {x + r * 0.55},{cy + r * 0.22} '
              f'L{x + r * 0.38},{cy + r * 0.3} L{x + r * 0.28},{cy + r * 0.2} L{x + r * 0.16},{cy + r * 0.34} '
              f'L{x},{cy + r * 0.24} L{x - r * 0.16},{cy + r * 0.34} L{x - r * 0.28},{cy + r * 0.2} L{x - r * 0.38},{cy + r * 0.3}Z" '
              f'fill="{f}"/>')


def gourd(s, x, base, r, c1, c2, stripe=None, tall=1.25):
    """A squash or gourd: taller than a pumpkin, sometimes striped."""
    cy = base - r * tall
    s.add(f'<ellipse cx="{x:.1f}" cy="{cy:.1f}" rx="{r:.1f}" ry="{r * tall:.1f}" fill="{c1}"/>')
    s.add(f'<ellipse cx="{x - r * 0.18:.1f}" cy="{cy - r * 0.1:.1f}" rx="{r * 0.62:.1f}" ry="{r * tall * 0.85:.1f}" fill="{c2}"/>')
    if stripe:
        for dx in (-0.45, 0, 0.45):
            s.add(f'<path d="M{x + dx * r:.1f},{cy - r * tall * 0.9:.1f} Q{x + dx * r * 1.3:.1f},{cy:.1f} {x + dx * r:.1f},{cy + r * tall * 0.9:.1f}" '
                  f'stroke="{stripe}" stroke-width="{r * 0.12:.1f}" fill="none" opacity="0.8"/>')
    s.add(f'<path d="M{x},{cy - r * tall} q1,-{r * 0.3:.1f} {r * 0.15:.1f},-{r * 0.4:.1f}" stroke="#5b4a2a" '
          f'stroke-width="{r * 0.16:.1f}" fill="none" stroke-linecap="round"/>')


def corn_shock(s, x, base, h, c1="#e3b45a", c2="#b98536", tie="#7a4f22"):
    """A teepee of dried corn stalks tied near the top."""
    top, ty, w = base - h, base - h * 0.66, h * 0.42
    s.add(f'<path d="M{x - w},{base} Q{x - w * 0.4},{ty + h * 0.08} {x - w * 0.12},{ty} L{x - w * 0.03},{top} '
          f'L{x + w * 0.05},{top} L{x + w * 0.12},{ty} Q{x + w * 0.4},{ty + h * 0.08} {x + w},{base}Z" fill="{c2}"/>')
    for i in range(22):
        bx = x - w + 2 * w * i / 21
        tx = x + (bx - x) * 0.1
        top_x = x + s.r.uniform(-w * 0.3, w * 0.3)
        c = c1 if i % 3 else "#f0cd7a"
        s.add(f'<path d="M{bx:.1f},{base} Q{x + (bx - x) * 0.45:.1f},{ty + h * 0.12:.1f} {tx:.1f},{ty:.1f} '
              f'Q{x + (top_x - x) * 0.4:.1f},{ty - h * 0.15:.1f} {top_x:.1f},{top - s.r.uniform(0, h * 0.1):.1f}" '
              f'stroke="{c}" stroke-width="3" fill="none" stroke-linecap="round"/>')
    for k in range(5):
        side = -1 if k % 2 else 1
        ly = ty - h * (0.05 + 0.06 * k)
        s.add(f'<path d="M{x:.1f},{ly:.1f} q{side * w * 0.35:.1f},{h * 0.02:.1f} {side * w * 0.55:.1f},{h * 0.16:.1f} '
              f'q-{side * w * 0.2:.1f},-{h * 0.08:.1f} -{side * w * 0.55:.1f},-{h * 0.12:.1f}Z" fill="{c1}" opacity="0.9"/>')
    s.add(f'<path d="M{x - w * 0.16},{ty - 6} Q{x},{ty + 4} {x + w * 0.16},{ty - 6} L{x + w * 0.17},{ty + 4} Q{x},{ty + 14} '
          f'{x - w * 0.17},{ty + 4}Z" fill="{tie}"/>')


def hay_bale(s, x, base, r, c1="#e5b85c", c2="#c9963e", line="#a8772c"):
    """A round hay bale on its side: the rolled end facing us, its body going back."""
    cy = base - r
    s.add(f'<rect x="{x}" y="{cy - r}" width="{r * 1.3}" height="{r * 2}" fill="{c2}"/>')
    s.add(f'<ellipse cx="{x + r * 1.3}" cy="{cy}" rx="{r * 0.45}" ry="{r}" fill="{c2}"/>')
    for k in range(6):
        yy = cy - r * 0.8 + k * r * 0.32
        s.add(f'<path d="M{x + 6},{yy:.1f} q{r * 0.6:.1f},-4 {r * 1.4:.1f},0" stroke="{line}" stroke-width="2" fill="none" opacity="0.6"/>')
    s.add(f'<ellipse cx="{x}" cy="{cy}" rx="{r * 0.55}" ry="{r}" fill="{c1}"/>')
    d = []
    for i in range(80):
        t = i / 79
        a = t * 5.5 * math.pi
        rr = r * (1 - t) * 0.92
        d.append(f"{'M' if i == 0 else 'L'}{x + rr * 0.55 * math.cos(a):.1f},{cy + rr * math.sin(a):.1f}")
    s.add(f'<path d="{" ".join(d)}" stroke="{line}" stroke-width="2.4" fill="none"/>')


def apple(s, x, y, r, c="#c8312a", hi="#f0675c"):
    s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r:.1f}" fill="{c}"/>')
    s.add(f'<circle cx="{x - r * 0.3:.1f}" cy="{y - r * 0.3:.1f}" r="{r * 0.35:.1f}" fill="{hi}" opacity="0.7"/>')
    s.add(f'<path d="M{x:.1f},{y - r * 0.8:.1f} l1,-{r * 0.4:.1f}" stroke="#4a2a14" stroke-width="2"/>')
    s.add(f'<path d="M{x + 1:.1f},{y - r * 1.1:.1f} q{r * 0.4:.1f},-{r * 0.3:.1f} {r * 0.7:.1f},0 q-{r * 0.35:.1f},{r * 0.25:.1f} -{r * 0.7:.1f},0Z" fill="#5b8a2a"/>')


def grapes(s, x, y, r, c="#6b3a8a", hi="#9b6bc0"):
    for row, n in enumerate((4, 3, 2, 1)):
        for k in range(n):
            gx = x + (k - (n - 1) / 2) * r * 1.6
            gy = y + row * r * 1.35
            s.add(f'<circle cx="{gx:.1f}" cy="{gy:.1f}" r="{r:.1f}" fill="{c}"/>')
            s.add(f'<circle cx="{gx - r * 0.3:.1f}" cy="{gy - r * 0.3:.1f}" r="{r * 0.3:.1f}" fill="{hi}"/>')


def pear(s, x, y, r, c="#c9b23a", hi="#e8d66a"):
    s.add(f'<path d="M{x},{y - r * 1.5} C{x + r * 0.5},{y - r * 1.5} {x + r * 0.4},{y - r * 0.6} {x + r},{y} '
          f'C{x + r * 1.2},{y + r} {x - r * 1.2},{y + r} {x - r},{y} C{x - r * 0.4},{y - r * 0.6} {x - r * 0.5},{y - r * 1.5} {x},{y - r * 1.5}Z" fill="{c}"/>')
    s.add(f'<ellipse cx="{x - r * 0.35}" cy="{y - r * 0.1}" rx="{r * 0.2}" ry="{r * 0.4}" fill="{hi}" opacity="0.7"/>')
    s.add(f'<path d="M{x},{y - r * 1.5} l2,-{r * 0.4:.1f}" stroke="#4a2a14" stroke-width="2"/>')


def cornucopia(s, x, base, size):
    """A woven horn of plenty, open to the left, spilling fruit and little pumpkins."""
    k = size
    mx, my = x - k * 0.55, base - k * 0.42          # middle of the open mouth
    tx, ty = x + k * 0.8, my - k * 0.32            # the curled-up tip
    horn = (f"M{mx},{my - k * 0.37} C{mx + k * 0.35},{my - k * 0.41} {x + k * 0.28},{my - k * 0.3} {x + k * 0.54},{my - k * 0.17} "
            f"C{x + k * 0.67},{my - k * 0.1} {x + k * 0.74},{my - k * 0.2} {tx},{ty} "
            f"C{x + k * 0.88},{my - k * 0.12} {x + k * 0.72},{my + k * 0.12} {x + k * 0.46},{my + k * 0.25} "
            f"C{x + k * 0.2},{my + k * 0.36} {mx + k * 0.22},{my + k * 0.39} {mx},{my + k * 0.37}Z")
    cid = s.id("cc")
    s.defs.append(f'<clipPath id="{cid}"><path d="{horn}"/></clipPath>')
    s.add(f'<path d="{horn}" fill="#b07a3a"/>')
    weave = []
    for i in range(22):
        wx = mx - k * 0.1 + i * k * 0.07
        weave.append(f"M{wx:.1f},{my - k * 0.5:.1f} q{k * 0.12:.1f},{k * 0.5:.1f} {k * 0.02:.1f},{k * 0.95:.1f}")
    for j in range(7):
        wy = my - k * 0.33 + j * k * 0.11
        weave.append(f"M{mx:.1f},{wy:.1f} Q{x + k * 0.2:.1f},{wy - k * 0.04:.1f} {tx:.1f},{ty + (wy - my) * 0.15:.1f}")
    s.add(f'<g clip-path="url(#{cid})"><path d="{" ".join(weave)}" stroke="#7d5223" stroke-width="{k * 0.025:.1f}" fill="none"/>'
          f'<path d="M{mx},{my + k * 0.4} C{x},{my + k * 0.42} {x + k * 0.5},{my + k * 0.3} {x + k * 0.9},{my}" stroke="#6b431b" '
          f'stroke-width="{k * 0.14:.1f}" fill="none" opacity="0.35"/></g>')
    s.add(f'<ellipse cx="{mx}" cy="{my}" rx="{k * 0.13}" ry="{k * 0.37}" fill="#5e3a17"/>')
    s.add(f'<ellipse cx="{mx}" cy="{my}" rx="{k * 0.13}" ry="{k * 0.37}" fill="none" stroke="#c99550" stroke-width="{k * 0.03:.1f}"/>')
    # what spills out
    grapes(s, mx - k * 0.04, my - k * 0.2, k * 0.045)
    pumpkin2(s, mx - k * 0.32, base, k * 0.16)
    apple(s, mx - k * 0.02, base - k * 0.12, k * 0.1)
    pear(s, mx - k * 0.55, base - k * 0.1, k * 0.08)
    gourd(s, mx + k * 0.16, base - 2, k * 0.08, "#e9e2c9", "#f7f2e0", "#8aa65a", 1.1)
    apple(s, mx - k * 0.66, base - k * 0.07, k * 0.075, "#d6532a", "#f08a5a")
    for (dx, dy, c, rot) in ((-0.2, -0.42, "#c8462a", -30), (0.05, -0.5, "#e07b2a", 20), (-0.8, -0.05, "#b5341f", 60)):
        maple(s, mx + dx * k, my + dy * k, k * 0.09, c, rot)


# ---------- Halloween ----------
def tombstone(s, x, base, w, h, color="#5c5470", shade="#433c56", kind=0):
    """A gravestone: rounded, cross-shaped or pointed, with a shaded side and a crack."""
    if kind == 1:
        s.add(f'<path d="M{x - w * 0.16},{base} V{base - h * 0.62} H{x - w * 0.5} V{base - h * 0.8} H{x - w * 0.16} V{base - h} '
              f'H{x + w * 0.16} V{base - h * 0.8} H{x + w * 0.5} V{base - h * 0.62} H{x + w * 0.16} V{base}Z" fill="{color}"/>')
        s.add(f'<rect x="{x + w * 0.04}" y="{base - h}" width="{w * 0.12}" height="{h}" fill="{shade}"/>')
        return
    top = f"A{w / 2},{w / 2} 0 0,1 {x + w / 2},{base - h + w / 2}" if kind == 0 else f"L{x},{base - h} L{x + w / 2},{base - h + w * 0.35}"
    start = base - h + (w / 2 if kind == 0 else w * 0.35)
    s.add(f'<path d="M{x - w / 2},{base} V{start} {top} V{base}Z" fill="{color}"/>')
    s.add(f'<path d="M{x + w * 0.22},{base} V{start + 6} Q{x + w * 0.4},{start - w * 0.1} {x + w / 2},{start} V{base}Z" fill="{shade}"/>')
    s.add(f'<path d="M{x - w * 0.15},{base - h * 0.62} h{w * 0.3:.1f} M{x - w * 0.2},{base - h * 0.48} h{w * 0.4:.1f} '
          f'M{x - w * 0.12},{base - h * 0.36} h{w * 0.24:.1f}" stroke="{shade}" stroke-width="3" stroke-linecap="round"/>')
    s.add(f'<path d="M{x - w * 0.3},{base - h * 0.85} l6,10 l-4,8 l7,12" stroke="{shade}" stroke-width="2" fill="none"/>')


def iron_fence(s, x0, x1, base, h, color="#120a1c", gap=26):
    s.add(f'<rect x="{x0}" y="{base - h * 0.85}" width="{x1 - x0}" height="5" fill="{color}"/>')
    s.add(f'<rect x="{x0}" y="{base - h * 0.25}" width="{x1 - x0}" height="5" fill="{color}"/>')
    x = x0
    while x <= x1:
        s.add(f'<path d="M{x - 2.5},{base} V{base - h} L{x - 6},{base - h} L{x},{base - h - 14} L{x + 6},{base - h} '
              f'L{x + 2.5},{base - h} V{base}Z" fill="{color}"/>')
        x += gap


def haunted_house(s, x, base, k, color="#160b24", win="#ffb84a"):
    """A crooked old mansion on a hill: a tower with a spire, gables, a porch and a few lit windows."""
    s.glow(x, base - 180 * k, 260 * k, win, 0.12)
    parts = [
        f"M{x - 150 * k},{base} V{base - 170 * k} L{x - 95 * k},{base - 245 * k} L{x - 40 * k},{base - 170 * k} V{base}Z",
        f"M{x - 60 * k},{base} V{base - 210 * k} L{x + 30 * k},{base - 300 * k} L{x + 120 * k},{base - 210 * k} V{base}Z",
        f"M{x + 95 * k},{base} V{base - 250 * k} H{x + 165 * k} V{base}Z",
        f"M{x + 85 * k},{base - 250 * k} L{x + 130 * k},{base - 400 * k} L{x + 175 * k},{base - 250 * k}Z",
        f"M{x - 175 * k},{base} V{base - 70 * k} H{x + 190 * k} V{base}Z",
        f"M{x + 10 * k},{base - 300 * k} V{base - 330 * k} H{x + 26 * k} V{base - 290 * k}Z",
    ]
    s.add(f'<path d="{" ".join(parts)}" fill="{color}"/>')
    s.add(f'<path d="M{x + 130 * k},{base - 400 * k} V{base - 430 * k} M{x + 120 * k},{base - 420 * k} H{x + 140 * k}" '
          f'stroke="{color}" stroke-width="{4 * k:.1f}"/>')
    for (wx, wy, ww, wh, lit) in ((-120, 150, 24, 34, True), (-75, 150, 24, 34, False), (-10, 175, 26, 40, True),
                                  (40, 175, 26, 40, True), (115, 210, 30, 40, True), (115, 140, 30, 36, False),
                                  (10, 245, 22, 22, True), (-130, 50, 30, 32, False), (60, 55, 30, 36, True)):
        c = win if lit else "#3a2a4a"
        if lit:
            anchor(s, "window", x + (wx + ww / 2) * k, base - (wy - wh / 2) * k, ww * k)
            s.glow(x + (wx + ww / 2) * k, base - (wy - wh / 2) * k, 40 * k, win, 0.35)
        s.add(f'<rect x="{x + wx * k:.1f}" y="{base - wy * k:.1f}" width="{ww * k:.1f}" height="{wh * k:.1f}" fill="{c}" '
              f'transform="rotate({s.r.uniform(-4, 4):.1f} {x + wx * k:.1f} {base - wy * k:.1f})"/>')
    s.add(f'<path d="M{x - 18 * k},{base} V{base - 50 * k} Q{x},{base - 66 * k} {x + 18 * k},{base - 50 * k} V{base}Z" fill="#3a2a4a"/>')


def ghost(s, x, y, size, a=0.92, tilt=0):
    k = size
    s.glow(x, y, k * 1.2, "#e8f0ff", 0.18 * a)
    s.add(f'<g transform="rotate({tilt} {x} {y})" opacity="{a}"><path d="M{x - k * 0.45},{y + k * 0.55} V{y - k * 0.05} '
          f'A{k * 0.45},{k * 0.5} 0 0,1 {x + k * 0.45},{y - k * 0.05} V{y + k * 0.55} Q{x + k * 0.34},{y + k * 0.42} {x + k * 0.22},{y + k * 0.56} '
          f'Q{x + k * 0.11},{y + k * 0.42} {x},{y + k * 0.56} Q{x - k * 0.11},{y + k * 0.42} {x - k * 0.22},{y + k * 0.56} '
          f'Q{x - k * 0.34},{y + k * 0.42} {x - k * 0.45},{y + k * 0.55}Z" fill="#f4f6ff"/>'
          f'<ellipse cx="{x - k * 0.15}" cy="{y - k * 0.05}" rx="{k * 0.07}" ry="{k * 0.1}" fill="#2a2140"/>'
          f'<ellipse cx="{x + k * 0.15}" cy="{y - k * 0.05}" rx="{k * 0.07}" ry="{k * 0.1}" fill="#2a2140"/>'
          f'<ellipse cx="{x}" cy="{y + k * 0.16}" rx="{k * 0.07}" ry="{k * 0.09}" fill="#2a2140"/></g>')


def spider_web(s, x, y, r, color="#d9d2ef", a=0.55, start=0, sweep=180):
    """A web hung in a corner or a branch fork, spreading over [sweep] degrees from [start]."""
    spokes = 7
    angs = [math.radians(start + sweep * i / (spokes - 1)) for i in range(spokes)]
    d = [f"M{x},{y} L{x + r * math.cos(t):.1f},{y + r * math.sin(t):.1f}" for t in angs]
    for ring in (0.25, 0.45, 0.65, 0.85):
        pts = [(x + r * ring * math.cos(t), y + r * ring * math.sin(t)) for t in angs]
        seg = [f"M{pts[0][0]:.1f},{pts[0][1]:.1f}"]
        for (ax, ay), (bx, by) in zip(pts, pts[1:]):
            mx, my = (ax + bx) / 2, (ay + by) / 2
            seg.append(f"Q{mx + (x - mx) * 0.12:.1f},{my + (y - my) * 0.12:.1f} {bx:.1f},{by:.1f}")
        d.append(" ".join(seg))
    s.add(f'<path d="{" ".join(d)}" stroke="{color}" stroke-width="1.6" fill="none" opacity="{a}"/>')


def spider(s, x, y, size, thread_from, color="#120a1c"):
    s.add(f'<path d="M{x},{thread_from} V{y}" stroke="#d9d2ef" stroke-width="1.2" opacity="0.6"/>')
    legs = []
    for side in (-1, 1):
        for k in range(4):
            a = math.radians(-50 + k * 32)
            legs.append(f"M{x},{y} q{side * size * 0.7 * math.cos(a):.1f},{-size * 0.5 + k * size * 0.3:.1f} "
                        f"{side * size * 1.1:.1f},{-size * 0.2 + k * size * 0.38:.1f}")
    s.add(f'<path d="{" ".join(legs)}" stroke="{color}" stroke-width="2" fill="none"/>')
    s.add(f'<ellipse cx="{x}" cy="{y + size * 0.15}" rx="{size * 0.35}" ry="{size * 0.42}" fill="{color}"/>')


def witch(s, x, y, k, color="#120a1c"):
    """A witch on a broom, flying left, as a silhouette."""
    s.add(f'<g transform="translate({x},{y}) scale({k})" fill="{color}">'
          f'<path d="M-70,8 L60,-6 L62,-1 L-68,13Z"/>'
          f'<path d="M58,-8 Q80,-14 96,-22 Q88,-8 98,0 Q84,0 96,10 Q78,4 60,4Z"/>'
          f'<path d="M-6,4 Q-14,-20 -2,-36 Q12,-46 22,-38 Q30,-18 26,2Z"/>'
          f'<path d="M-4,-2 Q-24,6 -36,22 L-28,24 Q-16,10 4,6Z"/><path d="M14,0 Q20,16 12,30 L18,31 Q28,14 22,0Z"/>'
          f'<circle cx="8" cy="-46" r="9"/><path d="M-10,-50 L26,-52 L22,-47 L-6,-45Z"/>'
          f'<path d="M0,-50 Q8,-74 24,-88 Q20,-70 18,-50Z"/>'
          f'<path d="M14,-40 Q34,-30 40,-14 Q30,-24 14,-28Z"/></g>')


# ---------- Día de los Muertos ----------
def marigold(s, x, y, r, c1="#ff8c1a", c2="#ffb21e", c3="#c24d14"):
    """A ruffled cempasúchil flower: rings of petals around a darker heart."""
    for ring, (rr, n, c) in enumerate(((0.78, 12, c3), (0.62, 11, c1), (0.42, 9, c2), (0.22, 6, c1))):
        for i in range(n):
            a = 2 * math.pi * i / n + ring * 0.4
            s.add(f'<circle cx="{x + r * rr * math.cos(a):.1f}" cy="{y + r * rr * math.sin(a) * 0.9:.1f}" '
                  f'r="{r * (0.32 - ring * 0.04):.1f}" fill="{c}"/>')
    s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r * 0.16:.1f}" fill="{c3}"/>')


def calavera(s, x, y, r, base="#f7efe2", c1="#f25f8a", c2="#55c1f0", c3="#ffb02e", c4="#7bd389"):
    """A sugar skull: a rounded skull with flowers around its eyes, a heart nose and a row of teeth."""
    s.add(f'<path d="M{x - r},{y - r * 0.1} A{r},{r * 1.02} 0 0,1 {x + r},{y - r * 0.1} Q{x + r},{y + r * 0.45} {x + r * 0.62},{y + r * 0.62} '
          f'V{y + r * 0.95} Q{x},{y + r * 1.12} {x - r * 0.62},{y + r * 0.95} V{y + r * 0.62} Q{x - r},{y + r * 0.45} {x - r},{y - r * 0.1}Z" '
          f'fill="{base}"/>')
    for side, c in ((-1, c2), (1, c1)):
        ex = x + side * r * 0.4
        for i in range(8):
            a = 2 * math.pi * i / 8
            s.add(f'<circle cx="{ex + r * 0.3 * math.cos(a):.1f}" cy="{y + r * 0.12 + r * 0.3 * math.sin(a):.1f}" r="{r * 0.1:.1f}" fill="{c}"/>')
        s.add(f'<circle cx="{ex:.1f}" cy="{y + r * 0.12:.1f}" r="{r * 0.23:.1f}" fill="#2a1030"/>')
        s.add(f'<circle cx="{ex - r * 0.06:.1f}" cy="{y + r * 0.06:.1f}" r="{r * 0.06:.1f}" fill="#ffffff" opacity="0.8"/>')
    s.add(f'<path d="{s.heart_path(x, y + r * 0.5, r * 0.26)}" fill="#2a1030" transform="rotate(180 {x} {y + r * 0.45})"/>')
    s.add(f'<path d="M{x - r * 0.42},{y + r * 0.78} H{x + r * 0.42}" stroke="#2a1030" stroke-width="{r * 0.05:.1f}"/>')
    for i in range(7):
        tx = x - r * 0.36 + i * r * 0.12
        s.add(f'<path d="M{tx:.1f},{y + r * 0.7:.1f} V{y + r * 0.86:.1f}" stroke="#2a1030" stroke-width="{r * 0.03:.1f}"/>')
    for i in range(5):
        a = 2 * math.pi * i / 5 - math.pi / 2
        s.add(f'<circle cx="{x + r * 0.15 * math.cos(a):.1f}" cy="{y - r * 0.55 + r * 0.15 * math.sin(a):.1f}" r="{r * 0.11:.1f}" fill="{c3}"/>')
    s.add(f'<circle cx="{x:.1f}" cy="{y - r * 0.55:.1f}" r="{r * 0.08:.1f}" fill="{c4}"/>')
    for dx in (-0.62, -0.75, 0.62, 0.75):
        s.add(f'<circle cx="{x + dx * r:.1f}" cy="{y - r * 0.3 + abs(dx) * r * 0.2:.1f}" r="{r * 0.05:.1f}" fill="{c4}"/>')


def ofrenda(s, x, base, w):
    """An altar for the departed: three steps under bright cloths, with candles, sugar skulls, marigolds,
    framed photos and bread, under an arch of marigolds."""
    steps = [(w, 120, "#c2185b", "#ff6f91"), (w * 0.74, 105, "#6a1b9a", "#b07cf2"), (w * 0.48, 95, "#ef6c00", "#ffb02e")]
    y = base
    tops = []
    for (sw, sh, c, edgec) in steps:
        s.add(f'<rect x="{x - sw / 2:.1f}" y="{y - sh:.1f}" width="{sw:.1f}" height="{sh:.1f}" fill="{c}"/>')
        n = max(6, int(sw / 26))
        zig = " ".join(f"L{x - sw / 2 + sw * (i + 0.5) / n:.1f},{y - sh + 24:.1f} L{x - sw / 2 + sw * (i + 1) / n:.1f},{y - sh + 12:.1f}"
                       for i in range(n))
        s.add(f'<path d="M{x - sw / 2:.1f},{y - sh:.1f} H{x + sw / 2:.1f} V{y - sh + 12:.1f} {zig.replace("L", "L", 1)} '
              f'L{x - sw / 2:.1f},{y - sh + 12:.1f}Z" fill="{edgec}"/>')
        tops.append((y - sh, sw))
        y -= sh
    # the marigold arch
    ax0, ax1, atop = x - w * 0.42, x + w * 0.42, y - 150
    for i in range(34):
        t = i / 33
        ang = math.pi * t
        mx = (ax0 + ax1) / 2 - (ax1 - ax0) / 2 * math.cos(ang)
        my = base - 20 - (base - 20 - atop) * math.sin(ang) ** 0.8
        marigold(s, mx, my, 15 + 3 * math.sin(ang))
    # what sits on each step
    t0, w0 = tops[0]
    for dx in (-0.4, -0.27, 0.27, 0.4):
        s.candle(x + dx * w0, t0, 46, 14)
    calavera(s, x - w0 * 0.12, t0 - 30, 26)
    calavera(s, x + w0 * 0.12, t0 - 30, 26, c1="#7bd389", c2="#ffb02e")
    t1, w1 = tops[1]
    for dx, c in ((-0.32, "#f7e8c8"), (0.32, "#f7e8c8")):
        s.add(f'<rect x="{x + dx * w1 - 30:.1f}" y="{t1 - 84:.1f}" width="60" height="78" fill="#7a4a1f"/>')
        s.add(f'<rect x="{x + dx * w1 - 22:.1f}" y="{t1 - 76:.1f}" width="44" height="62" fill="{c}"/>')
        s.add(f'<circle cx="{x + dx * w1:.1f}" cy="{t1 - 54:.1f}" r="11" fill="#c9a77a"/><path d="M{x + dx * w1 - 18:.1f},{t1 - 14:.1f} '
              f'Q{x + dx * w1:.1f},{t1 - 44:.1f} {x + dx * w1 + 18:.1f},{t1 - 14:.1f}Z" fill="#c9a77a"/>')
    s.add(f'<ellipse cx="{x:.1f}" cy="{t1 - 24:.1f}" rx="44" ry="26" fill="#c98a3e"/>')
    s.add(f'<path d="M{x - 30:.1f},{t1 - 34:.1f} q15,-12 30,0 q15,12 30,0 M{x - 22:.1f},{t1 - 24:.1f} q11,-9 22,0 q11,9 22,0" '
          f'stroke="#a8692a" stroke-width="5" fill="none" stroke-linecap="round"/>')
    t2, w2 = tops[2]
    for dx in (-0.3, 0.3):
        s.add(f'<path d="M{x + dx * w2 - 16:.1f},{t2} L{x + dx * w2 - 20:.1f},{t2 - 40} H{x + dx * w2 + 20:.1f} '
              f'L{x + dx * w2 + 16:.1f},{t2}Z" fill="#3fa9f5"/>')
        for k in range(5):
            marigold(s, x + dx * w2 + (k - 2) * 12, t2 - 52 - (8 if k % 2 else 0), 13)
    s.candle(x, t2, 70, 22)


# ---------- Christmas ----------
def xmas_tree(s, x, base, h, glow=True):
    """A decorated tree: tiers of green, a garland, ornaments, little lights and a star on top."""
    if glow:
        s.glow(x, base - h * 0.45, h * 0.8, "#ffd27a", 0.25)
    s.add(f'<rect x="{x - h * 0.05}" y="{base - h * 0.12}" width="{h * 0.1}" height="{h * 0.14}" fill="#5a3a24"/>')
    tiers = 5
    for k in range(tiers):
        t = k / tiers
        ty = base - h + h * 0.82 * t
        by = ty + h * 0.3
        hw = h * (0.1 + 0.3 * (k + 1) / tiers)
        s.add(f'<path d="M{x},{ty} L{x + hw},{by} Q{x},{by + h * 0.03} {x - hw},{by}Z" fill="#1f6b45"/>')
        s.add(f'<path d="M{x},{ty} L{x - hw},{by} Q{x - hw * 0.5},{by + h * 0.01} {x - hw * 0.1},{by - h * 0.02}Z" fill="#2c8a57"/>')
        s.add(f'<path d="M{x - hw * 0.8},{by - h * 0.06} Q{x},{by + h * 0.02} {x + hw * 0.8},{by - h * 0.1}" stroke="#ffd36b" '
              f'stroke-width="{h * 0.012:.1f}" fill="none" opacity="0.9"/>')
    colors = ["#e63946", "#ffd36b", "#3fa9f5", "#ff8fab", "#ffffff", "#b07cf2"]
    for k in range(26):
        t = s.r.uniform(0.12, 0.95)
        y = base - h + h * 0.9 * t
        hw = h * 0.36 * t
        ox = x + s.r.uniform(-hw, hw) * 0.85
        c = s.r.choice(colors)
        if k % 2:
            anchor(s, "light", ox, y, rgb(c))
            s.glow(ox, y, h * 0.03, c, 0.6)
            s.add(f'<circle cx="{ox:.1f}" cy="{y:.1f}" r="{h * 0.007:.1f}" fill="#fff6d6"/>')
        else:
            s.add(f'<circle cx="{ox:.1f}" cy="{y:.1f}" r="{h * 0.022:.1f}" fill="{c}"/>')
            s.add(f'<circle cx="{ox - h * 0.007:.1f}" cy="{y - h * 0.007:.1f}" r="{h * 0.007:.1f}" fill="#ffffff" opacity="0.7"/>')
    anchor(s, "star", x, base - h, h * 0.16)
    s.glow(x, base - h, h * 0.16, "#fff3b0", 0.7)
    star = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        rr = h * (0.07 if i % 2 == 0 else 0.03)
        star.append(f"{x + rr * math.cos(a):.1f},{base - h + rr * math.sin(a):.1f}")
    s.add(f'<polygon points="{" ".join(star)}" fill="#ffd36b"/>')


def present(s, x, base, w, h, color, ribbon="#ffd36b", shade=None):
    s.add(f'<rect x="{x - w / 2}" y="{base - h}" width="{w}" height="{h}" rx="3" fill="{color}"/>')
    if shade:
        s.add(f'<rect x="{x + w * 0.15}" y="{base - h}" width="{w * 0.35}" height="{h}" fill="{shade}" opacity="0.25"/>')
    s.add(f'<rect x="{x - w * 0.06}" y="{base - h}" width="{w * 0.12}" height="{h}" fill="{ribbon}"/>')
    s.add(f'<rect x="{x - w / 2}" y="{base - h * 0.62}" width="{w}" height="{h * 0.1}" fill="{ribbon}"/>')
    s.add(f'<path d="M{x},{base - h} q-{w * 0.32:.1f},-{h * 0.32:.1f} -{w * 0.28:.1f},-{h * 0.04:.1f} q{w * 0.06:.1f},{h * 0.1:.1f} {w * 0.28:.1f},{h * 0.04:.1f} '
          f'q{w * 0.32:.1f},-{h * 0.32:.1f} {w * 0.28:.1f},-{h * 0.04:.1f} q-{w * 0.06:.1f},{h * 0.1:.1f} -{w * 0.28:.1f},{h * 0.04:.1f}Z" fill="{ribbon}"/>')


def snowman(s, x, base, h, scarf="#e63946", hat="#1b1b2a"):
    s.add(f'<ellipse cx="{x + h * 0.05}" cy="{base}" rx="{h * 0.3}" ry="{h * 0.05}" fill="#7f9cc0" opacity="0.35"/>')
    for (fy, fr) in ((0.2, 0.2), (0.5, 0.15), (0.73, 0.11)):
        cy, r = base - h * fy, h * fr
        s.add(f'<circle cx="{x}" cy="{cy}" r="{r}" fill="#f7fbff"/>')
        s.add(f'<circle cx="{x + r * 0.3}" cy="{cy + r * 0.25}" r="{r * 0.8}" fill="#d7e6f5" opacity="0.5"/>')
    s.add(f'<path d="M{x - h * 0.14},{base - h * 0.5} l-{h * 0.18:.1f},-{h * 0.12:.1f} l-{h * 0.04:.1f},-{h * 0.06:.1f} M{x - h * 0.3},{base - h * 0.62} '
          f'l-{h * 0.05:.1f},{h * 0.02:.1f} M{x + h * 0.14},{base - h * 0.52} l{h * 0.17:.1f},-{h * 0.16:.1f} l{h * 0.06:.1f},-{h * 0.02:.1f}" '
          f'stroke="#5a3a24" stroke-width="{h * 0.02:.1f}" stroke-linecap="round" fill="none"/>')
    hy = base - h * 0.73
    s.add(f'<circle cx="{x - h * 0.04}" cy="{hy - h * 0.02}" r="{h * 0.014}" fill="#1b1b2a"/>'
          f'<circle cx="{x + h * 0.04}" cy="{hy - h * 0.02}" r="{h * 0.014}" fill="#1b1b2a"/>')
    s.add(f'<path d="M{x},{hy + h * 0.01} l{h * 0.1:.1f},{h * 0.02:.1f} l-{h * 0.1:.1f},{h * 0.02:.1f}Z" fill="#f28c38"/>')
    for k in range(3):
        s.add(f'<circle cx="{x}" cy="{base - h * (0.44 + k * 0.06):.1f}" r="{h * 0.012:.1f}" fill="#1b1b2a"/>')
    s.add(f'<path d="M{x - h * 0.11},{hy + h * 0.09} Q{x},{hy + h * 0.13} {x + h * 0.11},{hy + h * 0.09} L{x + h * 0.11},{hy + h * 0.13} '
          f'Q{x},{hy + h * 0.17} {x - h * 0.11},{hy + h * 0.13}Z M{x + h * 0.04},{hy + h * 0.12} l{h * 0.03:.1f},{h * 0.14:.1f} '
          f'l{h * 0.05:.1f},-{h * 0.01:.1f} l-{h * 0.02:.1f},-{h * 0.13:.1f}Z" fill="{scarf}"/>')
    s.add(f'<rect x="{x - h * 0.12}" y="{hy - h * 0.12}" width="{h * 0.24}" height="{h * 0.03}" fill="{hat}"/>'
          f'<rect x="{x - h * 0.08}" y="{hy - h * 0.27}" width="{h * 0.16}" height="{h * 0.16}" fill="{hat}"/>'
          f'<rect x="{x - h * 0.08}" y="{hy - h * 0.16}" width="{h * 0.16}" height="{h * 0.025}" fill="{scarf}"/>')


def sleigh(s, x, y, k, color="#0a1430"):
    """A sleigh pulled by a line of reindeer, flying left, as a silhouette (for crossing the moon)."""
    g = [f'<path d="M0,0 Q4,14 20,16 H62 Q74,16 76,6 L80,-12 Q70,-6 64,-2 H18 Q8,-2 6,-14 Z"/>',
         f'<path d="M-4,22 H84 Q92,22 94,16" stroke="{color}" stroke-width="3" fill="none"/>',
         f'<path d="M36,-2 Q40,-22 52,-24 Q60,-22 58,-2Z"/><circle cx="50" cy="-30" r="7"/>',
         f'<path d="M-6,4 L-60,-6" stroke="{color}" stroke-width="2" fill="none"/>']
    for i in range(3):
        rx = -70 - i * 52
        g.append(f'<path d="M{rx},-10 Q{rx + 18},-16 {rx + 34},-10 Q{rx + 38},-4 {rx + 30},0 Q{rx + 14},2 {rx},-2Z"/>'
                 f'<path d="M{rx + 2},-8 Q{rx - 6},-20 {rx - 12},-26 L{rx - 8},-28 Q{rx},-20 {rx + 6},-12Z"/>'
                 f'<circle cx="{rx - 12}" cy="-28" r="5"/>'
                 f'<path d="M{rx - 12},-32 l-4,-10 m4,10 l4,-11 m-6,4 l-5,-2 m9,1 l5,-3" stroke="{color}" stroke-width="2" fill="none"/>'
                 f'<path d="M{rx + 4},-2 l-6,12 M{rx + 10},-2 l-2,14 M{rx + 26},-2 l6,12 M{rx + 32},-4 l10,8" '
                 f'stroke="{color}" stroke-width="3" fill="none" stroke-linecap="round"/>')
    s.add(f'<g transform="translate({x},{y}) scale({k})" fill="{color}">{"".join(g)}</g>')


# ---------- New Year's ----------
def willow(s, x, y, r, color, n=18, a=0.9):
    """A willow firework: trails that rise and droop."""
    s.glow(x, y, r * 0.9, color, 0.25 * a)
    for i in range(n):
        ang = 2 * math.pi * i / n
        ex, ey = x + r * math.cos(ang), y + r * math.sin(ang) * 0.7
        s.add(f'<path d="M{x},{y} Q{x + r * 0.8 * math.cos(ang):.1f},{y + r * 0.6 * math.sin(ang) - r * 0.3:.1f} '
              f'{ex:.1f},{ey + r * 0.45:.1f}" stroke="{color}" stroke-width="2.4" fill="none" opacity="{a * 0.8:.2f}" stroke-linecap="round"/>')
        s.add(f'<circle cx="{ex:.1f}" cy="{ey + r * 0.45:.1f}" r="2.4" fill="#fff6d6" opacity="{a:.2f}"/>')


def ring_burst(s, x, y, r, color, n=28):
    s.glow(x, y, r * 1.3, color, 0.2)
    for i in range(n):
        ang = 2 * math.pi * i / n
        s.add(f'<circle cx="{x + r * math.cos(ang):.1f}" cy="{y + r * math.sin(ang):.1f}" r="{max(2.5, r * 0.04):.1f}" fill="{color}"/>')
        s.add(f'<circle cx="{x + r * 0.6 * math.cos(ang + 0.1):.1f}" cy="{y + r * 0.6 * math.sin(ang + 0.1):.1f}" r="{max(1.6, r * 0.025):.1f}" '
              f'fill="#ffffff" opacity="0.8"/>')


def clock_tower(s, x, base, w, h, color="#0a0d24", face="#fff3c4"):
    s.add(f'<rect x="{x - w / 2}" y="{base - h}" width="{w}" height="{h}" fill="{color}"/>')
    s.add(f'<path d="M{x - w * 0.62},{base - h} L{x},{base - h - w * 1.5} L{x + w * 0.62},{base - h}Z" fill="{color}"/>')
    s.add(f'<rect x="{x - w * 0.62}" y="{base - h - 8}" width="{w * 1.24}" height="12" fill="{color}"/>')
    cy = base - h + w * 0.7
    s.glow(x, cy, w * 1.1, face, 0.35)
    s.add(f'<circle cx="{x}" cy="{cy}" r="{w * 0.36}" fill="{face}"/><circle cx="{x}" cy="{cy}" r="{w * 0.36}" fill="none" '
          f'stroke="#c9a24a" stroke-width="3"/>')
    for i in range(12):
        a = 2 * math.pi * i / 12
        s.add(f'<circle cx="{x + w * 0.28 * math.cos(a):.1f}" cy="{cy + w * 0.28 * math.sin(a):.1f}" r="1.6" fill="#5a4a2a"/>')
    s.add(f'<path d="M{x},{cy} V{cy - w * 0.26} M{x},{cy} L{x - w * 0.05},{cy - w * 0.19}" stroke="#2a2010" stroke-width="3" '
          f'stroke-linecap="round"/>')
    for k in range(4):
        s.add(f'<rect x="{x - w * 0.2}" y="{base - h + w * 1.3 + k * 46}" width="{w * 0.4}" height="24" rx="10" fill="#ffd27a" opacity="0.75"/>')


def flute(s, x, base, h, tilt, wine="#f6d27a"):
    """A champagne glass, tipped by [tilt] degrees, with bubbles."""
    w = h * 0.26
    bowl = (f"M{x - w / 2},{base - h} L{x + w / 2},{base - h} Q{x + w * 0.52},{base - h * 0.48} {x + w * 0.08},{base - h * 0.42} "
            f"L{x + w * 0.05},{base - h * 0.08} L{x + w * 0.4},{base - h * 0.02} V{base} H{x - w * 0.4} V{base - h * 0.02} "
            f"L{x - w * 0.05},{base - h * 0.08} L{x - w * 0.08},{base - h * 0.42} Q{x - w * 0.52},{base - h * 0.48} {x - w / 2},{base - h}Z")
    wine_p = (f"M{x - w * 0.46},{base - h * 0.85} L{x + w * 0.46},{base - h * 0.85} Q{x + w * 0.45},{base - h * 0.5} {x},{base - h * 0.44} "
              f"Q{x - w * 0.45},{base - h * 0.5} {x - w * 0.46},{base - h * 0.85}Z")
    bub = "".join(f'<circle cx="{x + s.r.uniform(-w * 0.3, w * 0.3):.1f}" cy="{base - h * s.r.uniform(0.5, 0.82):.1f}" '
                  f'r="{s.r.uniform(1.5, 3.2):.1f}" fill="#ffffff" opacity="0.8"/>' for _ in range(9))
    s.add(f'<g transform="rotate({tilt} {x} {base})"><path d="{wine_p}" fill="{wine}"/>{bub}'
          f'<path d="{bowl}" fill="#ffffff" fill-opacity="0.18" stroke="#ffffff" stroke-opacity="0.8" stroke-width="3"/>'
          f'<path d="M{x - w * 0.3},{base - h * 0.95} Q{x - w * 0.36},{base - h * 0.6} {x - w * 0.12},{base - h * 0.5}" '
          f'stroke="#ffffff" stroke-opacity="0.6" stroke-width="3" fill="none"/></g>')


def balloon(s, x, y, r, c1, c2, string_to=None):
    f = s.rad([(0, c2, 1), (1, c1, 1)], x - r * 0.35, y - r * 0.45, r * 1.5)
    if string_to:
        sx, sy = string_to
        s.add(f'<path d="M{x},{y + r * 1.15} C{x - 12},{(y + sy) / 2} {sx + 12},{(y + sy) / 2} {sx},{sy}" stroke="#ffffff" '
              f'stroke-opacity="0.6" stroke-width="2" fill="none"/>')
    s.add(f'<ellipse cx="{x}" cy="{y}" rx="{r}" ry="{r * 1.15}" fill="{f}"/>')
    s.add(f'<path d="M{x - 5},{y + r * 1.15 + 6} L{x},{y + r * 1.12} L{x + 5},{y + r * 1.15 + 6}Z" fill="{c1}"/>')
    s.add(f'<ellipse cx="{x - r * 0.38}" cy="{y - r * 0.45}" rx="{r * 0.16}" ry="{r * 0.28}" fill="#ffffff" opacity="0.55" '
          f'transform="rotate(25 {x - r * 0.38} {y - r * 0.45})"/>')


def streamer(s, x, y, length, color, a=0.9):
    d = [f"M{x},{y}"]
    for i in range(1, 9):
        d.append(f"q{length / 16:.1f},{(-1) ** i * length / 10:.1f} {length / 8:.1f},{length / 24:.1f}")
    s.add(f'<path d="{" ".join(d)}" stroke="{color}" stroke-width="4" fill="none" stroke-linecap="round" opacity="{a}"/>')


# ---------- Valentine's ----------
def rose(s, x, y, r, c1="#d6234a", c2="#ff4d6d", c3="#9b1235"):
    """A rose seen from above and a little to the side: layered petals curling into a spiral."""
    s.add(f'<ellipse cx="{x}" cy="{y + r * 0.1}" rx="{r}" ry="{r * 0.85}" fill="{c3}"/>')
    for i in range(6):
        a = 2 * math.pi * i / 6 + 0.3
        s.add(f'<ellipse cx="{x + r * 0.45 * math.cos(a):.1f}" cy="{y + r * 0.38 * math.sin(a):.1f}" rx="{r * 0.55:.1f}" '
              f'ry="{r * 0.45:.1f}" fill="{c1}" transform="rotate({math.degrees(a):.0f} {x + r * 0.45 * math.cos(a):.1f} '
              f'{y + r * 0.38 * math.sin(a):.1f})"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{r * 0.55}" fill="{c2}"/>')
    s.add(f'<path d="M{x - r * 0.3},{y + r * 0.05} A{r * 0.3},{r * 0.3} 0 1,1 {x + r * 0.22},{y + r * 0.2} '
          f'A{r * 0.2},{r * 0.2} 0 1,1 {x - r * 0.08},{y - r * 0.08}" stroke="{c3}" stroke-width="{r * 0.08:.1f}" fill="none" '
          f'stroke-linecap="round"/>')


def bouquet(s, x, base, h):
    """A bunch of roses in paper, tied with a bow."""
    for i in range(7):
        ang = math.radians(-60 + i * 20)
        ex, ey = x + math.cos(ang - math.pi / 2) * 0 + h * 0.3 * math.sin(ang), base - h * 0.7 - h * 0.12 * math.cos(ang)
        s.add(f'<path d="M{x},{base - h * 0.15} Q{x + (ex - x) * 0.4:.1f},{base - h * 0.45:.1f} {ex:.1f},{ey:.1f}" stroke="#2f7a3a" '
              f'stroke-width="5" fill="none"/>')
    for (dx, dy, rot) in ((-0.32, -0.52, -40), (0.3, -0.5, 40), (-0.1, -0.62, -10), (0.16, -0.66, 20)):
        lx, ly = x + dx * h, base + dy * h
        s.add(f'<ellipse cx="{lx:.1f}" cy="{ly:.1f}" rx="{h * 0.08:.1f}" ry="{h * 0.035:.1f}" fill="#3d8f46" '
              f'transform="rotate({rot} {lx:.1f} {ly:.1f})"/>')
    heads = [(-0.22, -0.72, 0.1), (0.0, -0.8, 0.11), (0.22, -0.72, 0.1), (-0.1, -0.62, 0.095), (0.12, -0.62, 0.095),
             (-0.3, -0.58, 0.08), (0.3, -0.58, 0.08)]
    for i, (dx, dy, rr) in enumerate(sorted(heads, key=lambda t: t[1])):
        if i % 3 == 1:
            rose(s, x + dx * h, base + dy * h, h * rr, "#ff6b8b", "#ff9fb3", "#d6335c")
        else:
            rose(s, x + dx * h, base + dy * h, h * rr)
    s.add(f'<path d="M{x - h * 0.34},{base - h * 0.6} L{x - h * 0.06},{base} H{x + h * 0.06} L{x + h * 0.34},{base - h * 0.6} '
          f'Q{x},{base - h * 0.48} {x - h * 0.34},{base - h * 0.6}Z" fill="#fff4ea"/>')
    s.add(f'<path d="M{x - h * 0.34},{base - h * 0.6} L{x - h * 0.06},{base} H{x} L{x - h * 0.12},{base - h * 0.52} '
          f'Q{x - h * 0.24},{base - h * 0.54} {x - h * 0.34},{base - h * 0.6}Z" fill="#f2dccb"/>')
    by = base - h * 0.22
    s.add(f'<path d="M{x},{by} q-{h * 0.14:.1f},-{h * 0.1:.1f} -{h * 0.14:.1f},0 q0,{h * 0.1:.1f} {h * 0.14:.1f},0 '
          f'q{h * 0.14:.1f},-{h * 0.1:.1f} {h * 0.14:.1f},0 q0,{h * 0.1:.1f} -{h * 0.14:.1f},0Z M{x},{by} l-{h * 0.06:.1f},{h * 0.14:.1f} '
          f'M{x},{by} l{h * 0.07:.1f},{h * 0.13:.1f}" fill="#e5486e" stroke="#e5486e" stroke-width="5" stroke-linecap="round"/>')


def envelope(s, x, y, w, rot, color="#fff6f0", seal="#e5486e", a=1.0):
    h = w * 0.66
    s.add(f'<g transform="rotate({rot} {x} {y})" opacity="{a}"><rect x="{x - w / 2}" y="{y - h / 2}" width="{w}" height="{h}" rx="3" '
          f'fill="{color}"/><path d="M{x - w / 2},{y - h / 2} L{x},{y + h * 0.08} L{x + w / 2},{y - h / 2}" fill="none" '
          f'stroke="#e8c9c9" stroke-width="2.5"/><path d="M{x - w / 2},{y + h / 2} L{x - w * 0.1},{y} M{x + w / 2},{y + h / 2} '
          f'L{x + w * 0.1},{y}" stroke="#f0dada" stroke-width="2"/>'
          f'<path d="{s.heart_path(x, y + h * 0.02, w * 0.22)}" fill="{seal}"/></g>')


def heart_garland(s, x0, x1, y, sag, colors, n):
    s.add(f'<path d="M{x0},{y} Q{(x0 + x1) / 2},{y + sag * 2} {x1},{y}" fill="none" stroke="#ffffff" stroke-width="2.5" '
          f'stroke-opacity="0.85"/>')
    for i in range(n):
        t = (i + 0.5) / n
        hx = x0 + (x1 - x0) * t
        hy = y + 2 * sag * 2 * t * (1 - t) + 4
        s.add(f'<path d="M{hx:.1f},{hy - 4:.1f} V{hy + 6:.1f}" stroke="#ffffff" stroke-width="2" stroke-opacity="0.8"/>')
        s.heart(hx, hy + 24, 38, colors[i % len(colors)], 1, s.r.uniform(-10, 10))


def arrow_heart(s, x, y, size, rot=-20):
    s.add(f'<g transform="rotate({rot} {x} {y})"><path d="M{x - size * 1.1},{y} H{x + size * 1.0}" stroke="#c98a3e" stroke-width="5"/>'
          f'<path d="M{x + size * 1.0},{y - 10} L{x + size * 1.22},{y} L{x + size * 1.0},{y + 10}Z" fill="#9aa0a8"/>'
          f'<path d="M{x - size * 1.1},{y} l-16,-12 h12 l14,12 l-14,12 h-12Z" fill="#ff8fab"/></g>')
    s.add(f'<path d="{s.heart_path(x, y + size * 0.1, size)}" fill="#e5486e"/>')
    s.add(f'<g transform="rotate({rot} {x} {y})"><path d="M{x - size * 0.15},{y} H{x + size * 1.0}" stroke="#c98a3e" stroke-width="5"/></g>')


# ---------- St. Patrick's ----------
def shamrock(s, x, y, size, color="#2f9a4f", rot=0, stem=True, light="#4cbf6a"):
    for k in range(3):
        s.heart(x, y, size, color, 1, rot + k * 120)
    for k in range(3):
        a = math.radians(rot + k * 120 - 90)
        s.add(f'<path d="M{x:.1f},{y:.1f} L{x + size * 0.32 * math.cos(a):.1f},{y + size * 0.32 * math.sin(a):.1f}" '
              f'stroke="{light}" stroke-width="{max(1.2, size * 0.05):.1f}" opacity="0.8"/>')
    if stem:
        s.add(f'<path d="M{x},{y + 2} q{size * 0.15:.1f},{size * 0.45:.1f} -{size * 0.05:.1f},{size * 0.9:.1f}" stroke="{color}" '
              f'stroke-width="{max(2, size * 0.1):.1f}" fill="none" stroke-linecap="round"/>')


def coin(s, x, y, r, rot=0):
    s.add(f'<g transform="rotate({rot} {x} {y})"><ellipse cx="{x}" cy="{y}" rx="{r}" ry="{r * 0.92}" fill="#e0a800"/>'
          f'<ellipse cx="{x}" cy="{y}" rx="{r * 0.78}" ry="{r * 0.72}" fill="#ffcf33"/>'
          f'<ellipse cx="{x - r * 0.25}" cy="{y - r * 0.25}" rx="{r * 0.18}" ry="{r * 0.28}" fill="#fff3b0" opacity="0.8"/></g>')


def pot_of_gold(s, x, base, k):
    anchor(s, "glow", x, base - k * 0.7, k * 1.4)
    s.glow(x, base - k * 0.7, k * 1.4, "#ffe066", 0.5)
    for i in range(16):
        coin(s, x - k * 0.5 + s.r.uniform(0, k), base - k * 0.78 + s.r.uniform(-k * 0.12, k * 0.08), k * 0.13, s.r.uniform(-30, 30))
    s.add(f'<path d="M{x - k * 0.62},{base - k * 0.72} Q{x - k * 0.72},{base - k * 0.1} {x - k * 0.3},{base} H{x + k * 0.3} '
          f'Q{x + k * 0.72},{base - k * 0.1} {x + k * 0.62},{base - k * 0.72}Z" fill="#1d2b2a"/>')
    s.add(f'<path d="M{x - k * 0.5},{base - k * 0.62} Q{x - k * 0.56},{base - k * 0.2} {x - k * 0.26},{base - k * 0.08}" '
          f'stroke="#3d5250" stroke-width="{k * 0.07:.1f}" fill="none" stroke-linecap="round"/>')
    s.add(f'<rect x="{x - k * 0.7}" y="{base - k * 0.82}" width="{k * 1.4}" height="{k * 0.16}" rx="{k * 0.08}" fill="#2c3b3a"/>')
    for (dx, dy) in ((-0.9, -0.05), (0.85, -0.02), (1.05, -0.08)):
        coin(s, x + dx * k, base + dy * k - k * 0.06, k * 0.12, s.r.uniform(-40, 40))


def cottage(s, x, base, w, h, door="#c8302a"):
    """A whitewashed cottage with a thick thatched roof, small windows with flower boxes and a bright door."""
    s.add(f'<rect x="{x - w / 2}" y="{base - h}" width="{w}" height="{h}" fill="#f4f1e8"/>')
    s.add(f'<rect x="{x + w * 0.2}" y="{base - h}" width="{w * 0.3}" height="{h}" fill="#d9d4c4" opacity="0.6"/>')
    s.add(f'<rect x="{x + w * 0.34}" y="{base - h - h * 0.62}" width="{w * 0.1}" height="{h * 0.5}" fill="#9a948a"/>')
    anchor(s, "smoke", x + w * 0.39, base - h - h * 0.62, 1)
    s.add(f'<path d="M{x - w * 0.6},{base - h + 8} Q{x - w * 0.62},{base - h - h * 0.2} {x - w * 0.36},{base - h - h * 0.5} '
          f'Q{x},{base - h - h * 0.72} {x + w * 0.36},{base - h - h * 0.5} Q{x + w * 0.62},{base - h - h * 0.2} {x + w * 0.6},{base - h + 8} '
          f'Q{x},{base - h - 4} {x - w * 0.6},{base - h + 8}Z" fill="#c99a4a"/>')
    for i in range(12):
        tx = x - w * 0.5 + i * w / 11
        s.add(f'<path d="M{tx:.1f},{base - h - h * 0.5 + abs(tx - x) / w * h * 0.5:.1f} q4,{h * 0.2:.1f} 0,{h * 0.4:.1f}" '
              f'stroke="#a87a32" stroke-width="3" fill="none" opacity="0.7"/>')
    for wx in (x - w * 0.32, x + w * 0.18):
        s.add(f'<rect x="{wx:.1f}" y="{base - h * 0.62:.1f}" width="{w * 0.15:.1f}" height="{h * 0.28:.1f}" fill="#4a6a7a"/>')
        s.add(f'<path d="M{wx + w * 0.075:.1f},{base - h * 0.62:.1f} v{h * 0.28:.1f} M{wx:.1f},{base - h * 0.48:.1f} h{w * 0.15:.1f}" '
              f'stroke="#f4f1e8" stroke-width="2.5"/>')
        s.add(f'<rect x="{wx - 3:.1f}" y="{base - h * 0.34:.1f}" width="{w * 0.15 + 6:.1f}" height="8" fill="#7a5a3a"/>')
        for k in range(4):
            s.add(f'<circle cx="{wx + k * w * 0.05:.1f}" cy="{base - h * 0.35:.1f}" r="4" fill="{["#ff5e8a", "#ffd166", "#ff8c42", "#ff5e8a"][k]}"/>')
    s.add(f'<rect x="{x - w * 0.08:.1f}" y="{base - h * 0.58:.1f}" width="{w * 0.16:.1f}" height="{h * 0.58:.1f}" rx="{w * 0.08:.1f}" fill="{door}"/>')


def stone_wall(s, x0, x1, base, h, c1="#9aa4a0", c2="#7d8783", c3="#b6beba"):
    rows = 3
    for r in range(rows):
        y = base - h + r * h / rows
        x = x0 - s.r.uniform(0, 30)
        while x < x1:
            w = s.r.uniform(26, 48)
            s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w - 3:.1f}" height="{h / rows - 3:.1f}" rx="{h / rows * 0.35:.1f}" '
                  f'fill="{s.r.choice((c1, c2, c3))}"/>')
            x += w


def sheep(s, x, base, k, face="#2b2b2b"):
    for (dx, dy) in ((-0.3, -0.6), (0, -0.7), (0.3, -0.6), (-0.4, -0.38), (0.4, -0.38), (-0.15, -0.35), (0.15, -0.35), (0, -0.5)):
        s.add(f'<circle cx="{x + dx * k:.1f}" cy="{base + dy * k:.1f}" r="{k * 0.26:.1f}" fill="#f7f7f2"/>')
    for dx in (-0.25, -0.08, 0.1, 0.27):
        s.add(f'<rect x="{x + dx * k:.1f}" y="{base - k * 0.25:.1f}" width="{k * 0.07:.1f}" height="{k * 0.25:.1f}" fill="{face}"/>')
    s.add(f'<ellipse cx="{x - k * 0.62:.1f}" cy="{base - k * 0.62:.1f}" rx="{k * 0.16:.1f}" ry="{k * 0.2:.1f}" fill="{face}"/>')
    s.add(f'<ellipse cx="{x - k * 0.55:.1f}" cy="{base - k * 0.78:.1f}" rx="{k * 0.1:.1f}" ry="{k * 0.05:.1f}" fill="{face}" '
          f'transform="rotate(-30 {x - k * 0.55:.1f} {base - k * 0.78:.1f})"/>')
    s.add(f'<circle cx="{x - k * 0.66:.1f}" cy="{base - k * 0.64:.1f}" r="{k * 0.025:.1f}" fill="#ffffff"/>')


def rainbow(s, cx, cy, r, width, colors=("#ff5e5b", "#ff9f43", "#ffd93d", "#4cd17a", "#3fa9f5", "#8e6cf2"), a=0.9):
    for i, c in enumerate(colors):
        rr = r - i * width
        s.add(f'<path d="M{cx - rr},{cy} A{rr},{rr} 0 0,1 {cx + rr},{cy}" fill="none" stroke="{c}" stroke-width="{width + 0.5}" opacity="{a}"/>')


# ---------- Easter and spring ----------
def chick(s, x, base, k, c1="#ffd94a", c2="#ffe98a"):
    s.add(f'<ellipse cx="{x}" cy="{base - k * 0.38}" rx="{k * 0.42}" ry="{k * 0.38}" fill="{c1}"/>')
    s.add(f'<circle cx="{x + k * 0.18}" cy="{base - k * 0.78}" r="{k * 0.25}" fill="{c1}"/>')
    s.add(f'<ellipse cx="{x - k * 0.08}" cy="{base - k * 0.42}" rx="{k * 0.2}" ry="{k * 0.14}" fill="{c2}" '
          f'transform="rotate(-20 {x - k * 0.08} {base - k * 0.42})"/>')
    s.add(f'<path d="M{x + k * 0.4},{base - k * 0.8} l{k * 0.14:.1f},{k * 0.04:.1f} l-{k * 0.14:.1f},{k * 0.05:.1f}Z" fill="#ff9a3c"/>')
    s.add(f'<circle cx="{x + k * 0.26}" cy="{base - k * 0.84}" r="{k * 0.04}" fill="#2b2b2b"/>')
    s.add(f'<path d="M{x + k * 0.1},{base - k * 1.02} q{k * 0.05:.1f},-{k * 0.1:.1f} {k * 0.1:.1f},0 q{k * 0.04:.1f},-{k * 0.08:.1f} {k * 0.08:.1f},0" '
          f'stroke="{c1}" stroke-width="{k * 0.04:.1f}" fill="none"/>')
    s.add(f'<path d="M{x - k * 0.08},{base} l-{k * 0.06:.1f},{k * 0.05:.1f} M{x - k * 0.08},{base} l{k * 0.02:.1f},{k * 0.06:.1f} '
          f'M{x + k * 0.1},{base} l-{k * 0.04:.1f},{k * 0.06:.1f} M{x + k * 0.1},{base} l{k * 0.04:.1f},{k * 0.06:.1f}" '
          f'stroke="#ff9a3c" stroke-width="{k * 0.03:.1f}" stroke-linecap="round"/>')


def bunny(s, x, base, k, c1="#f4f1ee", c2="#ddd6d0", inner="#ffc2d4"):
    """A sitting bunny, side on, looking right."""
    s.add(f'<ellipse cx="{x}" cy="{base - k * 0.36}" rx="{k * 0.42}" ry="{k * 0.36}" fill="{c1}"/>')
    s.add(f'<ellipse cx="{x - k * 0.1}" cy="{base - k * 0.22}" rx="{k * 0.26}" ry="{k * 0.2}" fill="{c2}" opacity="0.6"/>')
    s.add(f'<circle cx="{x - k * 0.42}" cy="{base - k * 0.42}" r="{k * 0.1}" fill="#ffffff"/>')
    s.add(f'<circle cx="{x + k * 0.3}" cy="{base - k * 0.72}" r="{k * 0.24}" fill="{c1}"/>')
    for (dx, rot) in ((0.18, -12), (0.34, 10)):
        ex, ey = x + dx * k, base - k * 1.12
        s.add(f'<ellipse cx="{ex:.1f}" cy="{ey:.1f}" rx="{k * 0.08:.1f}" ry="{k * 0.26:.1f}" fill="{c1}" transform="rotate({rot} {ex:.1f} {base - k * 0.9:.1f})"/>')
        s.add(f'<ellipse cx="{ex:.1f}" cy="{ey + k * 0.02:.1f}" rx="{k * 0.04:.1f}" ry="{k * 0.18:.1f}" fill="{inner}" '
              f'transform="rotate({rot} {ex:.1f} {base - k * 0.9:.1f})"/>')
    s.add(f'<circle cx="{x + k * 0.38}" cy="{base - k * 0.76}" r="{k * 0.035}" fill="#2b2b2b"/>')
    s.add(f'<circle cx="{x + k * 0.53}" cy="{base - k * 0.68}" r="{k * 0.03}" fill="#ff8fab"/>')
    s.add(f'<ellipse cx="{x + k * 0.22}" cy="{base - k * 0.04}" rx="{k * 0.16}" ry="{k * 0.06}" fill="{c2}"/>')


def butterfly(s, x, y, k, c1, c2, rot=0, a=0.95):
    s.add(f'<g transform="rotate({rot} {x} {y})" opacity="{a}">'
          f'<ellipse cx="{x - k * 0.32}" cy="{y - k * 0.22}" rx="{k * 0.34}" ry="{k * 0.26}" fill="{c1}" transform="rotate(-25 {x - k * 0.32} {y - k * 0.22})"/>'
          f'<ellipse cx="{x + k * 0.32}" cy="{y - k * 0.22}" rx="{k * 0.34}" ry="{k * 0.26}" fill="{c1}" transform="rotate(25 {x + k * 0.32} {y - k * 0.22})"/>'
          f'<ellipse cx="{x - k * 0.22}" cy="{y + k * 0.2}" rx="{k * 0.2}" ry="{k * 0.16}" fill="{c2}"/>'
          f'<ellipse cx="{x + k * 0.22}" cy="{y + k * 0.2}" rx="{k * 0.2}" ry="{k * 0.16}" fill="{c2}"/>'
          f'<circle cx="{x - k * 0.34}" cy="{y - k * 0.24}" r="{k * 0.08}" fill="#ffffff" opacity="0.7"/>'
          f'<circle cx="{x + k * 0.34}" cy="{y - k * 0.24}" r="{k * 0.08}" fill="#ffffff" opacity="0.7"/>'
          f'<rect x="{x - k * 0.04}" y="{y - k * 0.3}" width="{k * 0.08}" height="{k * 0.6}" rx="{k * 0.04}" fill="#3a2a2a"/>'
          f'<path d="M{x},{y - k * 0.3} q-{k * 0.1:.1f},-{k * 0.2:.1f} -{k * 0.2:.1f},-{k * 0.22:.1f} M{x},{y - k * 0.3} q{k * 0.1:.1f},-{k * 0.2:.1f} {k * 0.2:.1f},-{k * 0.22:.1f}" '
          f'stroke="#3a2a2a" stroke-width="1.6" fill="none"/></g>')


def daffodil(s, x, base, h, petal="#fff3a6", cup="#ffb81c", stem="#5aa04a"):
    s.add(f'<path d="M{x},{base} Q{x - 4},{base - h * 0.5} {x + 2},{base - h * 0.82}" stroke="{stem}" stroke-width="{h * 0.045:.1f}" fill="none"/>')
    s.add(f'<path d="M{x - 2},{base} Q{x - h * 0.2},{base - h * 0.4} {x - h * 0.16},{base - h * 0.7} Q{x - h * 0.08},{base - h * 0.4} {x + 2},{base}Z" fill="{stem}"/>')
    cx, cy = x + 2, base - h * 0.86
    for i in range(6):
        a = 2 * math.pi * i / 6
        s.add(f'<ellipse cx="{cx + h * 0.08 * math.cos(a):.1f}" cy="{cy + h * 0.08 * math.sin(a):.1f}" rx="{h * 0.08:.1f}" '
              f'ry="{h * 0.045:.1f}" fill="{petal}" transform="rotate({math.degrees(a):.0f} {cx + h * 0.08 * math.cos(a):.1f} '
              f'{cy + h * 0.08 * math.sin(a):.1f})"/>')
    s.add(f'<circle cx="{cx}" cy="{cy}" r="{h * 0.055}" fill="{cup}"/><circle cx="{cx}" cy="{cy}" r="{h * 0.03}" fill="#e08a00"/>')


def basket(s, x, base, w, eggs):
    h = w * 0.45
    s.add(f'<path d="M{x - w * 0.42},{base - h} Q{x},{base - h - w * 0.75} {x + w * 0.42},{base - h}" stroke="#a8743a" '
          f'stroke-width="{w * 0.05:.1f}" fill="none"/>')
    s.add(f'<path d="M{x - w * 0.5},{base - h} H{x + w * 0.5} L{x + w * 0.4},{base} H{x - w * 0.4}Z" fill="#c48a48"/>')
    for k in range(4):
        y = base - h + k * h / 4 + 6
        s.add(f'<path d="M{x - w * 0.48 + k * 3},{y} H{x + w * 0.48 - k * 3}" stroke="#a8743a" stroke-width="3"/>')
    for k in range(9):
        xx = x - w * 0.45 + k * w * 0.1125
        s.add(f'<path d="M{xx},{base - h} L{x + (xx - x) * 0.82},{base}" stroke="#a8743a" stroke-width="2.5"/>')
    for k in range(14):
        gx = x - w * 0.48 + k * w * 0.07
        s.add(f'<path d="M{gx:.1f},{base - h + 2} q2,-14 6,-20" stroke="#6cbf5a" stroke-width="3" fill="none"/>')
    for i, (c1, c2) in enumerate(eggs):
        egg_x = x - w * 0.3 + i * w * 0.2
        s.egg(egg_x, base - h - w * 0.04, w * 0.11, c1, c2, s.r.uniform(-15, 15))


def birdhouse(s, x, base, h, color="#5fa8d3", roof="#c8462a"):
    s.add(f'<rect x="{x - h * 0.03}" y="{base - h * 0.62}" width="{h * 0.06}" height="{h * 0.62}" fill="#8a6a4a"/>')
    hb = base - h * 0.6
    s.add(f'<rect x="{x - h * 0.16}" y="{hb - h * 0.3}" width="{h * 0.32}" height="{h * 0.3}" fill="{color}"/>')
    s.add(f'<path d="M{x - h * 0.22},{hb - h * 0.28} L{x},{hb - h * 0.46} L{x + h * 0.22},{hb - h * 0.28}Z" fill="{roof}"/>')
    s.add(f'<circle cx="{x}" cy="{hb - h * 0.16}" r="{h * 0.06}" fill="#3a2a2a"/><rect x="{x - h * 0.015}" y="{hb - h * 0.08}" '
          f'width="{h * 0.03}" height="{h * 0.05}" fill="#8a6a4a"/>')
    bx, by = x + h * 0.06, hb - h * 0.46
    s.add(f'<ellipse cx="{bx}" cy="{by - h * 0.04}" rx="{h * 0.07}" ry="{h * 0.05}" fill="#3f7fd0"/>'
          f'<circle cx="{bx + h * 0.05}" cy="{by - h * 0.08}" r="{h * 0.035}" fill="#3f7fd0"/>'
          f'<path d="M{bx + h * 0.085},{by - h * 0.085} l{h * 0.035:.1f},{h * 0.01:.1f} l-{h * 0.035:.1f},{h * 0.012:.1f}Z" fill="#ffb81c"/>'
          f'<ellipse cx="{bx}" cy="{by - h * 0.02}" rx="{h * 0.045}" ry="{h * 0.025}" fill="#ff8a5a"/>')


def duck(s, x, y, k, body="#ffffff", bill="#ffa630"):
    s.add(f'<ellipse cx="{x}" cy="{y}" rx="{k * 0.5}" ry="{k * 0.24}" fill="{body}"/>')
    s.add(f'<path d="M{x - k * 0.5},{y - k * 0.05} q-{k * 0.12:.1f},-{k * 0.16:.1f} {k * 0.02:.1f},-{k * 0.14:.1f}Z" fill="{body}"/>')
    s.add(f'<circle cx="{x + k * 0.32}" cy="{y - k * 0.28}" r="{k * 0.17}" fill="{body}"/>')
    s.add(f'<path d="M{x + k * 0.46},{y - k * 0.28} l{k * 0.16:.1f},{k * 0.03:.1f} l-{k * 0.15:.1f},{k * 0.06:.1f}Z" fill="{bill}"/>')
    s.add(f'<circle cx="{x + k * 0.36}" cy="{y - k * 0.32}" r="{k * 0.025}" fill="#2b2b2b"/>')
    s.add(f'<ellipse cx="{x - k * 0.05}" cy="{y - k * 0.02}" rx="{k * 0.24}" ry="{k * 0.1}" fill="#000000" opacity="0.06"/>')


# ---------- Cinco de Mayo ----------
def mission(s, x, base, k, wall="#e9a66a", shade="#c9824a", win="#ffcf6a"):
    """An adobe mission at sunset: a bell tower, a curved gable, arches and warm windows."""
    s.add(f'<rect x="{x - 150 * k}" y="{base - 110 * k}" width="{300 * k}" height="{110 * k}" fill="{wall}"/>')
    s.add(f'<path d="M{x - 70 * k},{base - 110 * k} Q{x - 60 * k},{base - 160 * k} {x - 30 * k},{base - 165 * k} Q{x},{base - 210 * k} '
          f'{x + 30 * k},{base - 165 * k} Q{x + 60 * k},{base - 160 * k} {x + 70 * k},{base - 110 * k}Z" fill="{wall}"/>')
    s.add(f'<rect x="{x + 100 * k}" y="{base - 240 * k}" width="{70 * k}" height="{240 * k}" fill="{shade}"/>')
    s.add(f'<path d="M{x + 95 * k},{base - 240 * k} H{x + 175 * k} V{base - 250 * k} H{x + 95 * k}Z M{x + 108 * k},{base - 250 * k} '
          f'Q{x + 135 * k},{base - 300 * k} {x + 162 * k},{base - 250 * k}Z" fill="{shade}"/>')
    s.add(f'<path d="M{x + 135 * k},{base - 296 * k} V{base - 330 * k} M{x + 124 * k},{base - 318 * k} H{x + 146 * k}" stroke="{shade}" '
          f'stroke-width="{5 * k:.1f}"/>')
    s.add(f'<path d="M{x + 118 * k},{base - 175 * k} V{base - 205 * k} Q{x + 135 * k},{base - 228 * k} {x + 152 * k},{base - 205 * k} '
          f'V{base - 175 * k}Z" fill="#5a2a1a"/><circle cx="{x + 135 * k}" cy="{base - 200 * k}" r="{8 * k}" fill="#d9a43a"/>')
    for i in range(5):
        ax = x - 130 * k + i * 52 * k
        s.add(f'<path d="M{ax},{base} V{base - 50 * k} Q{ax + 20 * k},{base - 78 * k} {ax + 40 * k},{base - 50 * k} V{base}Z" fill="{shade}"/>')
        anchor(s, "window", ax + 20 * k, base - 30 * k, 26 * k)
        s.glow(ax + 20 * k, base - 30 * k, 30 * k, win, 0.3)
    s.glow(x, base - 150 * k, 30 * k, win, 0.5)
    s.add(f'<circle cx="{x}" cy="{base - 150 * k}" r="{13 * k}" fill="{win}"/>')
    s.add(f'<rect x="{x - 110 * k}" y="{base - 100 * k}" width="{220 * k}" height="{10 * k}" fill="{shade}"/>')


def pinata(s, x, y, k, colors=("#ff3d7f", "#ffd166", "#3a86ff", "#06d6a0", "#ff8c42")):
    """A star piñata: a round middle with five cones and tassels."""
    s.add(f'<path d="M{x},{y - k * 0.9} V{y - k * 0.4}" stroke="#f7f1e3" stroke-width="2"/>')
    for i in range(5):
        a = -math.pi / 2 + 2 * math.pi * i / 5
        tx, ty = x + k * math.cos(a), y + k * math.sin(a)
        l1, l2 = a - 0.42, a + 0.42
        s.add(f'<path d="M{x + k * 0.36 * math.cos(l1):.1f},{y + k * 0.36 * math.sin(l1):.1f} L{tx:.1f},{ty:.1f} '
              f'L{x + k * 0.36 * math.cos(l2):.1f},{y + k * 0.36 * math.sin(l2):.1f}Z" fill="{colors[i % len(colors)]}"/>')
        for j in range(4):
            s.add(f'<path d="M{tx:.1f},{ty:.1f} q{(j - 1.5) * 4:.1f},{k * 0.2:.1f} {(j - 1.5) * 7:.1f},{k * 0.42:.1f}" '
                  f'stroke="{colors[(i + j) % len(colors)]}" stroke-width="3" fill="none"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{k * 0.42}" fill="#fff3d6"/>')
    for i in range(6):
        s.add(f'<path d="M{x - k * 0.42:.1f},{y - k * 0.3 + i * k * 0.12:.1f} H{x + k * 0.42:.1f}" stroke="{colors[i % len(colors)]}" '
              f'stroke-width="{k * 0.07:.1f}" opacity="0.9"/>')


def agave(s, x, base, k, c1="#4f8a6a", c2="#6fae8a"):
    for i in range(9):
        a = math.radians(-160 + i * 17.5)
        ln = k * (0.7 + 0.3 * math.sin(i))
        tx, ty = x + ln * math.cos(a), base + ln * math.sin(a)
        nx, ny = -math.sin(a) * k * 0.08, math.cos(a) * k * 0.08
        s.add(f'<path d="M{x - nx:.1f},{base - ny:.1f} Q{(x + tx) / 2 - nx:.1f},{(base + ty) / 2 - ny:.1f} {tx:.1f},{ty:.1f} '
              f'Q{(x + tx) / 2 + nx:.1f},{(base + ty) / 2 + ny:.1f} {x + nx:.1f},{base + ny:.1f}Z" fill="{c1 if i % 2 else c2}"/>')


def prickly_pear(s, x, base, k, c1="#4f9a5a", c2="#6dba6f", fruit="#e0457b"):
    pads = [(0, -0.45, 0.32, 0.45, 0), (-0.35, -1.0, 0.26, 0.36, -25), (0.32, -1.05, 0.26, 0.36, 25), (0, -1.45, 0.22, 0.3, 0)]
    for (dx, dy, rx, ry, rot) in pads:
        px, py = x + dx * k, base + dy * k
        s.add(f'<ellipse cx="{px:.1f}" cy="{py:.1f}" rx="{rx * k:.1f}" ry="{ry * k:.1f}" fill="{c1}" transform="rotate({rot} {px:.1f} {py:.1f})"/>')
        s.add(f'<ellipse cx="{px - rx * k * 0.25:.1f}" cy="{py - ry * k * 0.2:.1f}" rx="{rx * k * 0.6:.1f}" ry="{ry * k * 0.65:.1f}" fill="{c2}" '
              f'transform="rotate({rot} {px:.1f} {py:.1f})"/>')
        for _ in range(6):
            s.add(f'<circle cx="{px + s.r.uniform(-rx, rx) * k * 0.7:.1f}" cy="{py + s.r.uniform(-ry, ry) * k * 0.7:.1f}" r="1.6" fill="#e8f5d0"/>')
    for (dx, dy) in ((-0.42, -1.33), (0.18, -1.72), (0.48, -1.36)):
        s.add(f'<ellipse cx="{x + dx * k:.1f}" cy="{base + dy * k:.1f}" rx="{k * 0.07:.1f}" ry="{k * 0.09:.1f}" fill="{fruit}"/>')


def dahlia(s, x, y, r, c1, c2):
    for ring, (rr, n, c) in enumerate(((0.7, 14, c1), (0.48, 11, c2), (0.26, 8, c1))):
        for i in range(n):
            a = 2 * math.pi * i / n + ring * 0.2
            s.add(f'<ellipse cx="{x + r * rr * math.cos(a):.1f}" cy="{y + r * rr * math.sin(a):.1f}" rx="{r * (0.3 - ring * 0.05):.1f}" '
                  f'ry="{r * (0.14 - ring * 0.02):.1f}" fill="{c}" transform="rotate({math.degrees(a):.0f} {x + r * rr * math.cos(a):.1f} '
                  f'{y + r * rr * math.sin(a):.1f})"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{r * 0.12}" fill="#ffd166"/>')


# ---------- 4th of July ----------
def us_flag(s, x, y, w, pole_base):
    """A waving flag on a pole: thirteen stripes and a blue canton of stars."""
    h = w * 0.53
    s.add(f'<rect x="{x - 5}" y="{y - 12}" width="6" height="{pole_base - y + 12}" fill="#c9ccd2"/><circle cx="{x - 2}" cy="{y - 14}" r="6" fill="#e0b84a"/>')
    def wave(t):
        return math.sin(t * 2.4 * math.pi) * h * 0.08 * t
    for i in range(13):
        y0, y1 = y + h * i / 13, y + h * (i + 1) / 13
        pts_top = [(x + w * t, y0 + wave(t)) for t in [k / 20 for k in range(21)]]
        pts_bot = [(x + w * t, y1 + wave(t)) for t in [k / 20 for k in reversed(range(21))]]
        d = "M" + " L".join(f"{px:.1f},{py:.1f}" for px, py in pts_top + pts_bot) + "Z"
        s.add(f'<path d="{d}" fill="{"#c8202f" if i % 2 == 0 else "#ffffff"}"/>')
    cw, ch = w * 0.42, h * 7 / 13
    pts_top = [(x + cw * t, y + wave(t * 0.42)) for t in [k / 10 for k in range(11)]]
    pts_bot = [(x + cw * t, y + ch + wave(t * 0.42)) for t in [k / 10 for k in reversed(range(11))]]
    s.add(f'<path d="M{" L".join(f"{px:.1f},{py:.1f}" for px, py in pts_top + pts_bot)}Z" fill="#1d3a8a"/>')
    for r_ in range(5):
        for c_ in range(6 if r_ % 2 == 0 else 5):
            sx = x + cw * (0.1 + c_ * 0.16 + (0.08 if r_ % 2 else 0))
            sy = y + ch * (0.14 + r_ * 0.18) + wave((sx - x) / w)
            s.add(f'<circle cx="{sx:.1f}" cy="{sy:.1f}" r="{w * 0.012:.1f}" fill="#ffffff"/>')
    s.add(f'<path d="M{x},{y} L{x + w},{y + wave(1)} V{y + h + wave(1)} L{x},{y + h}Z" fill="#000000" opacity="0.06"/>')


def fan_bunting(s, x, y, w):
    """A pleated half-round fan in red, white and blue, hung by its straight edge."""
    r = w / 2
    s.add(f'<path d="M{x - r},{y} A{r},{r} 0 0,0 {x + r},{y}Z" fill="#1d3a8a"/>')
    for k, c in enumerate(("#c8202f", "#ffffff", "#c8202f")):
        rr = r * (0.8 - k * 0.18)
        s.add(f'<path d="M{x - rr},{y} A{rr},{rr} 0 0,0 {x + rr},{y}Z" fill="{c}"/>')
    for i in range(1, 12):
        a = math.pi * i / 12
        s.add(f'<path d="M{x},{y} L{x + r * math.cos(a):.1f},{y + r * math.sin(a):.1f}" stroke="#000000" stroke-opacity="0.12" stroke-width="2"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{r * 0.2}" fill="#1d3a8a"/>')
    star = " ".join(f"{x + (r * 0.13 if i % 2 == 0 else r * 0.055) * math.cos(-math.pi / 2 + i * math.pi / 5):.1f},"
                    f"{y + r * 0.04 + (r * 0.13 if i % 2 == 0 else r * 0.055) * math.sin(-math.pi / 2 + i * math.pi / 5):.1f}" for i in range(10))
    s.add(f'<polygon points="{star}" fill="#ffffff"/>')
    s.add(f'<rect x="{x - r - 4}" y="{y - 5}" width="{2 * r + 8}" height="8" fill="#f7f1e3"/>')


def cactus(s, x, base, h, color="#2f6b3d", light="#4a8a55"):
    """A saguaro: a ribbed trunk with two arms."""
    w = h * 0.17
    s.add(f'<rect x="{x - w / 2}" y="{base - h}" width="{w}" height="{h}" rx="{w / 2}" fill="{color}"/>')
    s.add(f'<path d="M{x - w / 2},{base - h * 0.45} h-{w * 0.8:.1f} a{w * 0.38:.1f},{w * 0.38:.1f} 0 0,1 -{w * 0.38:.1f},-{w * 0.38:.1f} '
          f'v-{h * 0.25:.1f} a{w * 0.38:.1f},{w * 0.38:.1f} 0 0,1 {w * 0.76:.1f},0 v{h * 0.13:.1f} h{w * 0.42:.1f}Z" fill="{color}"/>')
    s.add(f'<path d="M{x + w / 2},{base - h * 0.6} h{w * 0.7:.1f} a{w * 0.38:.1f},{w * 0.38:.1f} 0 0,0 {w * 0.38:.1f},-{w * 0.38:.1f} '
          f'v-{h * 0.2:.1f} a{w * 0.38:.1f},{w * 0.38:.1f} 0 0,0 -{w * 0.76:.1f},0 v{h * 0.08:.1f} h-{w * 0.32:.1f}Z" fill="{color}"/>')
    for dx in (-0.22, 0.05, 0.3):
        s.add(f'<path d="M{x + dx * w:.1f},{base - h + w * 0.4:.1f} V{base - 4:.1f}" stroke="{light}" stroke-width="{max(2, w * 0.08):.1f}" '
              f'opacity="0.7" stroke-linecap="round"/>')
    s.add(f'<rect x="{x - w * 0.42}" y="{base - h + 4}" width="{w * 0.2}" height="{h * 0.9}" rx="{w * 0.1}" fill="{light}" opacity="0.35"/>')


# ---------- Summer and the sea ----------
def palm(s, x, base, h, lean=1.0, trunk="#8a5a32", leaf="#2f8a4a", leaf2="#3fa65a"):
    tx, ty = x + h * 0.3 * lean, base - h
    s.add(f'<path d="M{x},{base} C{x + h * 0.03 * lean},{base - h * 0.4} {x + h * 0.12 * lean},{base - h * 0.75} {tx},{ty}" stroke="{trunk}" '
          f'stroke-width="{h * 0.065:.1f}" fill="none" stroke-linecap="round"/>')
    for k in range(9):
        t = k / 9
        px, py = x + (tx - x) * t ** 1.4, base - h * t
        s.add(f'<path d="M{px - h * 0.03:.1f},{py:.1f} q{h * 0.03:.1f},-{h * 0.025:.1f} {h * 0.06:.1f},0" stroke="#6b4426" stroke-width="3" fill="none"/>')
    for ang, ln, c in ((-165, 0.55, leaf), (-130, 0.5, leaf2), (-95, 0.4, leaf), (-55, 0.5, leaf2), (-15, 0.55, leaf), (150, 0.45, leaf2), (30, 0.45, leaf2)):
        a = math.radians(ang)
        ex, ey = tx + h * ln * math.cos(a), ty + h * ln * math.sin(a) + h * 0.16
        cx_, cy_ = tx + h * ln * 0.5 * math.cos(a), ty + h * ln * 0.5 * math.sin(a) - h * 0.08
        s.add(f'<path d="M{tx:.1f},{ty:.1f} Q{cx_:.1f},{cy_:.1f} {ex:.1f},{ey:.1f} Q{cx_ + h * 0.02:.1f},{cy_ + h * 0.09:.1f} {tx:.1f},{ty:.1f}Z" fill="{c}"/>')
        for f in (0.3, 0.5, 0.7):
            mx = tx + (ex - tx) * f
            my = ty + (ey - ty) * f - h * 0.05 * math.sin(math.pi * f)
            s.add(f'<path d="M{mx:.1f},{my:.1f} l{h * 0.03:.1f},{h * 0.05:.1f}" stroke="{c}" stroke-width="3"/>')
    for dx in (-0.03, 0.02, 0.06):
        s.add(f'<circle cx="{tx + h * dx:.1f}" cy="{ty + h * 0.03:.1f}" r="{h * 0.03:.1f}" fill="#6b4a2a"/>')


def umbrella(s, x, base, h, colors=("#ff5e5b", "#ffffff"), tilt=-8):
    """A beach umbrella: a striped dome with a scalloped edge on a pole."""
    w = h * 0.86
    top, rim = base - h, base - h + h * 0.24
    n = 6
    dome = f"M{x - w / 2},{rim} Q{x - w * 0.42},{top - h * 0.02} {x},{top} Q{x + w * 0.42},{top - h * 0.02} {x + w / 2},{rim}"
    scallops = " ".join(f"Q{x - w / 2 + w * (i + 0.5) / n:.1f},{rim - h * 0.06:.1f} {x - w / 2 + w * (i + 1) / n:.1f},{rim:.1f}"
                        for i in reversed(range(n)))
    cid = s.id("um")
    shape = f"{dome} {scallops.replace('Q', 'Q', 1)}"
    pts = [(x - w / 2 + w * i / n, rim) for i in range(n + 1)]
    back = " ".join(f"Q{x - w / 2 + w * (i + 0.5) / n:.1f},{rim - h * 0.06:.1f} {x - w / 2 + w * i / n:.1f},{rim:.1f}" for i in reversed(range(n)))
    outline = f"{dome} {back}Z"
    s.defs.append(f'<clipPath id="{cid}"><path d="{outline}"/></clipPath>')
    g = [f'<path d="M{x},{base} V{top}" stroke="#d9d4c4" stroke-width="{h * 0.025:.1f}"/>',
         f'<path d="{outline}" fill="{colors[1]}"/>']
    stripes = "".join(f'<path d="M{x},{top - 4} L{pts[i][0] - w * 0.02:.1f},{rim + 10} L{pts[i + 1][0] + w * 0.02:.1f},{rim + 10}Z" fill="{colors[0]}"/>'
                      for i in range(0, n, 2))
    g.append(f'<g clip-path="url(#{cid})">{stripes}<path d="M{x - w / 2},{rim - h * 0.02} H{x + w / 2}" stroke="#000000" stroke-opacity="0.08" stroke-width="{h * 0.04:.1f}"/></g>')
    g.append(f'<circle cx="{x}" cy="{top - 3}" r="{h * 0.022}" fill="#d9d4c4"/>')
    s.add(f'<g transform="rotate({tilt} {x} {base})">{"".join(g)}</g>')


def beach_ball(s, x, y, r):
    s.add(f'<circle cx="{x}" cy="{y}" r="{r}" fill="#ffffff"/>')
    for i, c in enumerate(("#ff5e5b", "#ffd166", "#3fa9f5")):
        a = math.radians(-90 + i * 120)
        s.add(f'<path d="M{x},{y} L{x + r * math.cos(a - 0.5):.1f},{y + r * math.sin(a - 0.5):.1f} A{r},{r} 0 0,1 '
              f'{x + r * math.cos(a + 0.5):.1f},{y + r * math.sin(a + 0.5):.1f}Z" fill="{c}"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{r * 0.16}" fill="#ffffff"/><circle cx="{x - r * 0.35}" cy="{y - r * 0.4}" r="{r * 0.18}" fill="#ffffff" opacity="0.6"/>')


def sandcastle(s, x, base, k, c1="#e8c486", c2="#d4a965"):
    parts = []
    for (dx, w, h) in ((-0.55, 0.32, 0.55), (0.55, 0.32, 0.55), (0, 0.42, 0.85)):
        tx0 = x + (dx - w / 2) * k
        parts.append(f'<rect x="{tx0:.1f}" y="{base - h * k:.1f}" width="{w * k:.1f}" height="{h * k:.1f}" fill="{c1}"/>')
        for j in range(3):
            parts.append(f'<rect x="{tx0 + j * w * k / 2.5:.1f}" y="{base - h * k - 0.08 * k:.1f}" width="{w * k / 5:.1f}" height="{0.09 * k:.1f}" fill="{c1}"/>')
    parts.append(f'<rect x="{x - 0.55 * k:.1f}" y="{base - 0.35 * k:.1f}" width="{1.1 * k:.1f}" height="{0.35 * k:.1f}" fill="{c2}"/>')
    parts.append(f'<path d="M{x - 0.1 * k:.1f},{base} V{base - 0.2 * k:.1f} Q{x:.1f},{base - 0.3 * k:.1f} {x + 0.1 * k:.1f},{base - 0.2 * k:.1f} V{base}Z" fill="#a8803e"/>')
    parts.append(f'<path d="M{x:.1f},{base - 0.95 * k:.1f} V{base - 1.25 * k:.1f}" stroke="#7a5a3a" stroke-width="3"/>'
                 f'<path d="M{x:.1f},{base - 1.25 * k:.1f} l{0.2 * k:.1f},{0.06 * k:.1f} l-{0.2 * k:.1f},{0.06 * k:.1f}Z" fill="#ff5e5b"/>')
    s.add("".join(parts))


def starfish(s, x, y, r, color="#ff8a5a", rot=0):
    pts = " ".join(f"{x + (r if i % 2 == 0 else r * 0.45) * math.cos(math.radians(rot) - math.pi / 2 + i * math.pi / 5):.1f},"
                   f"{y + (r if i % 2 == 0 else r * 0.45) * math.sin(math.radians(rot) - math.pi / 2 + i * math.pi / 5):.1f}" for i in range(10))
    s.add(f'<polygon points="{pts}" fill="{color}" stroke="{color}" stroke-width="{r * 0.25:.1f}" stroke-linejoin="round"/>')
    for i in range(5):
        a = math.radians(rot) - math.pi / 2 + i * 2 * math.pi / 5
        s.add(f'<circle cx="{x + r * 0.45 * math.cos(a):.1f}" cy="{y + r * 0.45 * math.sin(a):.1f}" r="{r * 0.07:.1f}" fill="#ffffff" opacity="0.7"/>')


def shell(s, x, y, r, color="#ffd1c2", rot=0):
    g = [f'<path d="M{x},{y + r * 0.3} L{x - r},{y - r * 0.2} A{r},{r * 0.9} 0 0,1 {x + r},{y - r * 0.2}Z" fill="{color}"/>']
    for i in range(5):
        a = math.pi + math.pi * (i + 0.5) / 5
        g.append(f'<path d="M{x},{y + r * 0.3} L{x + r * 0.95 * math.cos(a):.1f},{y - r * 0.2 + r * 0.85 * math.sin(a):.1f}" stroke="#e8a898" stroke-width="2"/>')
    s.add(f'<g transform="rotate({rot} {x} {y})">{"".join(g)}</g>')


def surfboard(s, x, base, h, color, stripe, tilt):
    w = h * 0.24
    s.add(f'<g transform="rotate({tilt} {x} {base})"><path d="M{x},{base - h} C{x + w * 0.6},{base - h * 0.85} {x + w * 0.6},{base - h * 0.1} '
          f'{x},{base} C{x - w * 0.6},{base - h * 0.1} {x - w * 0.6},{base - h * 0.85} {x},{base - h}Z" fill="{color}"/>'
          f'<path d="M{x},{base - h * 0.95} V{base - h * 0.05}" stroke="{stripe}" stroke-width="{w * 0.12:.1f}"/></g>')


def sailboat(s, x, y, k, hull="#ffffff", sail1="#ffffff", sail2="#ffd27a"):
    s.add(f'<path d="M{x - 60 * k},{y} L{x + 60 * k},{y} L{x + 40 * k},{y + 24 * k} L{x - 40 * k},{y + 24 * k}Z" fill="{hull}"/>')
    s.add(f'<path d="M{x},{y - 4 * k} L{x},{y - 130 * k} L{x + 56 * k},{y - 10 * k}Z" fill="{sail1}"/>')
    s.add(f'<path d="M{x - 6 * k},{y - 10 * k} L{x - 6 * k},{y - 110 * k} L{x - 52 * k},{y - 12 * k}Z" fill="{sail2}"/>')
    s.add(f'<path d="M{x},{y - 130 * k} V{y}" stroke="#7a5a3a" stroke-width="{2.5 * k:.1f}"/>')


def lighthouse(s, x, base, h, beam=True):
    w0, w1 = h * 0.2, h * 0.13
    anchor(s, "beam", x, base - h * 0.92, h)
    if beam:
        s.add(f'<path d="M{x},{base - h * 0.92} L{x - h * 1.6},{base - h * 1.12} L{x - h * 1.6},{base - h * 0.84}Z" fill="#fff6c9" opacity="0.35"/>')
        s.glow(x, base - h * 0.92, h * 0.3, "#fff3b0", 0.7)
    s.add(f'<path d="M{x - w0 / 2},{base} L{x - w1 / 2},{base - h * 0.82} H{x + w1 / 2} L{x + w0 / 2},{base}Z" fill="#ffffff"/>')
    for k in range(3):
        t0, t1 = 0.12 + k * 0.24, 0.24 + k * 0.24
        ww0 = w0 + (w1 - w0) * t0
        ww1 = w0 + (w1 - w0) * t1
        s.add(f'<path d="M{x - ww0 / 2:.1f},{base - h * t0:.1f} L{x - ww1 / 2:.1f},{base - h * t1:.1f} H{x + ww1 / 2:.1f} L{x + ww0 / 2:.1f},{base - h * t0:.1f}Z" fill="#e63946"/>')
    s.add(f'<path d="M{x + w0 * 0.1},{base} L{x + w1 * 0.1},{base - h * 0.82} H{x + w1 / 2} L{x + w0 / 2},{base}Z" fill="#000000" opacity="0.08"/>')
    s.add(f'<rect x="{x - w1 * 0.75}" y="{base - h * 0.84}" width="{w1 * 1.5}" height="{h * 0.03}" fill="#2b3a4a"/>')
    s.add(f'<rect x="{x - w1 * 0.42}" y="{base - h * 0.98}" width="{w1 * 0.84}" height="{h * 0.14}" fill="#fff3b0"/>')
    s.add(f'<path d="M{x - w1 * 0.55},{base - h * 0.98} L{x},{base - h * 1.1} L{x + w1 * 0.55},{base - h * 0.98}Z" fill="#2b3a4a"/>')


def whale_tail(s, x, y, k, color="#2b4f73"):
    s.add(f'<path d="M{x},{y} C{x - 6 * k},{y - 40 * k} {x - 10 * k},{y - 60 * k} {x - 2 * k},{y - 70 * k} C{x - 30 * k},{y - 80 * k} '
          f'{x - 60 * k},{y - 76 * k} {x - 70 * k},{y - 92 * k} C{x - 40 * k},{y - 100 * k} {x - 12 * k},{y - 92 * k} {x + 4 * k},{y - 82 * k} '
          f'C{x + 20 * k},{y - 92 * k} {x + 48 * k},{y - 100 * k} {x + 78 * k},{y - 92 * k} C{x + 66 * k},{y - 76 * k} {x + 36 * k},{y - 80 * k} '
          f'{x + 10 * k},{y - 70 * k} C{x + 16 * k},{y - 60 * k} {x + 14 * k},{y - 40 * k} {x + 10 * k},{y}Z" fill="{color}"/>')
    for i in range(6):
        s.add(f'<circle cx="{x + 5 * k + s.r.uniform(-30, 30) * k:.1f}" cy="{y - s.r.uniform(60, 110) * k:.1f}" r="{s.r.uniform(2, 4) * k:.1f}" fill="#ffffff" opacity="0.8"/>')


def dolphin(s, x, y, k, color="#5b8fb9", rot=-20):
    s.add(f'<g transform="rotate({rot} {x} {y})"><path d="M{x - 60 * k},{y} C{x - 30 * k},{y - 30 * k} {x + 30 * k},{y - 34 * k} {x + 60 * k},{y - 8 * k} '
          f'L{x + 78 * k},{y - 6 * k} L{x + 62 * k},{y + 2 * k} C{x + 30 * k},{y + 12 * k} {x - 20 * k},{y + 12 * k} {x - 52 * k},{y + 6 * k} '
          f'L{x - 70 * k},{y + 20 * k} L{x - 64 * k},{y} L{x - 76 * k},{y - 14 * k}Z" fill="{color}"/>'
          f'<path d="M{x - 6 * k},{y - 24 * k} L{x + 8 * k},{y - 44 * k} L{x + 14 * k},{y - 26 * k}Z" fill="{color}"/>'
          f'<path d="M{x - 40 * k},{y + 4 * k} C{x},{y + 12 * k} {x + 30 * k},{y + 8 * k} {x + 56 * k},{y - 2 * k}" stroke="#cfe2f0" stroke-width="{5 * k:.1f}" fill="none" opacity="0.7"/>'
          f'<circle cx="{x + 44 * k}" cy="{y - 12 * k}" r="{2.5 * k}" fill="#1b2b3a"/></g>')


# ---------- Fall and winter ----------
def lamp_post(s, x, base, h, light="#ffd98a", color="#2b2a33"):
    anchor(s, "lamp", x, base - h * 0.94, h * 0.12)
    s.glow(x, base - h * 0.95, h * 0.35, light, 0.45)
    s.add(f'<rect x="{x - h * 0.02}" y="{base - h * 0.85}" width="{h * 0.04}" height="{h * 0.85}" fill="{color}"/>')
    s.add(f'<rect x="{x - h * 0.045}" y="{base - h * 0.12}" width="{h * 0.09}" height="{h * 0.12}" fill="{color}"/>')
    s.add(f'<path d="M{x - h * 0.07},{base - h * 0.86} H{x + h * 0.07} L{x + h * 0.05},{base - h * 1.02} H{x - h * 0.05}Z" fill="{light}"/>')
    s.add(f'<path d="M{x - h * 0.08},{base - h * 1.02} L{x},{base - h * 1.1} L{x + h * 0.08},{base - h * 1.02}Z" fill="{color}"/>')
    s.add(f'<rect x="{x - h * 0.08}" y="{base - h * 0.87}" width="{h * 0.16}" height="{h * 0.02}" fill="{color}"/>')


def bench(s, x, base, w, wood="#8a5a32", iron="#2b2a33"):
    h = w * 0.42
    for k in range(3):
        s.add(f'<rect x="{x - w / 2}" y="{base - h + k * h * 0.14}" width="{w}" height="{h * 0.1}" rx="3" fill="{wood}"/>')
    s.add(f'<rect x="{x - w / 2 - 4}" y="{base - h * 0.48}" width="{w + 8}" height="{h * 0.12}" rx="3" fill="{wood}"/>')
    for dx in (-0.42, 0.42):
        s.add(f'<path d="M{x + dx * w:.1f},{base} V{base - h * 0.48:.1f} M{x + dx * w:.1f},{base - h * 0.48:.1f} L{x + dx * w * 1.02:.1f},{base - h:.1f}" '
              f'stroke="{iron}" stroke-width="{w * 0.035:.1f}" fill="none"/>')


def mushroom(s, x, base, h, cap="#d9452f", dots=True):
    s.add(f'<path d="M{x - h * 0.12},{base} Q{x - h * 0.1},{base - h * 0.5} {x - h * 0.08},{base - h * 0.62} H{x + h * 0.08} '
          f'Q{x + h * 0.1},{base - h * 0.5} {x + h * 0.12},{base}Z" fill="#f4ead7"/>')
    s.add(f'<path d="M{x - h * 0.42},{base - h * 0.58} Q{x - h * 0.4},{base - h * 1.05} {x},{base - h * 1.05} Q{x + h * 0.4},{base - h * 1.05} '
          f'{x + h * 0.42},{base - h * 0.58}Z" fill="{cap}"/>')
    if dots:
        for (dx, dy, rr) in ((-0.2, -0.8, 0.07), (0.12, -0.9, 0.06), (0.24, -0.72, 0.05), (-0.04, -0.68, 0.045)):
            s.add(f'<circle cx="{x + dx * h:.1f}" cy="{base + dy * h:.1f}" r="{rr * h:.1f}" fill="#ffffff"/>')


def leaf_pile(s, x, base, w, colors):
    s.add(f'<ellipse cx="{x}" cy="{base}" rx="{w / 2}" ry="{w * 0.2}" fill="{colors[0]}"/>')
    for _ in range(int(w / 4)):
        a = s.r.uniform(math.pi, 2 * math.pi)
        rr = s.r.uniform(0, 1) ** 0.5
        maple(s, x + w / 2 * rr * math.cos(a), base + w * 0.2 * rr * math.sin(a), s.r.uniform(7, 12), s.r.choice(colors), s.r.uniform(0, 360))


def log_cabin(s, x, base, w, h, win="#ffd27a"):
    s.add(f'<rect x="{x + w * 0.22}" y="{base - h - h * 0.75}" width="{w * 0.12}" height="{h * 0.6}" fill="#7a6a62"/>')
    anchor(s, "smoke", x + w * 0.28, base - h - h * 0.8, 1)
    for k in range(6):
        s.add(f'<circle cx="{x + w * 0.28 + k * 6:.1f}" cy="{base - h - h * 0.85 - k * 24:.1f}" r="{9 + k * 4}" fill="#ffffff" opacity="{0.4 - k * 0.055:.2f}"/>')
    rows = 7
    for k in range(rows):
        y = base - h + k * h / rows
        s.add(f'<rect x="{x - w / 2}" y="{y:.1f}" width="{w}" height="{h / rows + 1:.1f}" rx="{h / rows / 2:.1f}" fill="{"#7a4a2a" if k % 2 else "#8a5832"}"/>')
    for dx in (-0.5, 0.5):
        for k in range(rows):
            s.add(f'<circle cx="{x + dx * w:.1f}" cy="{base - h + (k + 0.5) * h / rows:.1f}" r="{h / rows / 2:.1f}" fill="#b07a4a"/>')
    s.add(f'<path d="M{x - w * 0.62},{base - h + 4} L{x},{base - h - h * 0.62} L{x + w * 0.62},{base - h + 4}Z" fill="#5a3a2a"/>')
    s.add(f'<path d="M{x - w * 0.64},{base - h + 6} L{x},{base - h - h * 0.66} L{x + w * 0.64},{base - h + 6} L{x + w * 0.56},{base - h - 4} '
          f'Q{x + w * 0.2},{base - h - h * 0.3} {x},{base - h - h * 0.5} Q{x - w * 0.2},{base - h - h * 0.3} {x - w * 0.56},{base - h - 4}Z" fill="#ffffff"/>')
    for wx in (x - w * 0.32, x + w * 0.14):
        anchor(s, "window", wx + w * 0.09, base - h * 0.56, w * 0.18)
        s.glow(wx + w * 0.09, base - h * 0.55, w * 0.25, win, 0.5)
        s.add(f'<rect x="{wx:.1f}" y="{base - h * 0.72:.1f}" width="{w * 0.18:.1f}" height="{h * 0.32:.1f}" fill="{win}" stroke="#4a2a1a" stroke-width="4"/>')
        s.add(f'<path d="M{wx + w * 0.09:.1f},{base - h * 0.72:.1f} v{h * 0.32:.1f} M{wx:.1f},{base - h * 0.56:.1f} h{w * 0.18:.1f}" stroke="#4a2a1a" stroke-width="3"/>')
    s.add(f'<rect x="{x - w * 0.06:.1f}" y="{base - h * 0.62:.1f}" width="{w * 0.14:.1f}" height="{h * 0.62:.1f}" fill="#4a2a1a"/>')


def sled(s, x, base, w, color="#c8302a"):
    h = w * 0.22
    s.add(f'<path d="M{x - w / 2},{base} H{x + w * 0.4} Q{x + w * 0.56},{base} {x + w * 0.55},{base - h * 0.5}" stroke="#7a5a3a" stroke-width="5" fill="none"/>')
    s.add(f'<rect x="{x - w * 0.46}" y="{base - h}" width="{w * 0.86}" height="{h * 0.45}" rx="4" fill="{color}"/>')
    for dx in (-0.3, 0.0, 0.3):
        s.add(f'<rect x="{x + dx * w - 3:.1f}" y="{base - h * 0.6:.1f}" width="6" height="{h * 0.6:.1f}" fill="#7a5a3a"/>')


def deer(s, x, base, k, color="#3a2a22", facing=1):
    f = facing
    s.add(f'<g transform="translate({x},{base}) scale({f * k},{k})" fill="{color}">'
          f'<path d="M-40,-60 Q-10,-70 30,-62 Q42,-60 44,-50 L46,-30 Q40,-26 30,-30 L28,-50 Q0,-44 -36,-46 Q-46,-48 -40,-60Z"/>'
          f'<path d="M30,-62 L46,-96 L58,-98 L62,-90 L52,-88 L42,-58Z"/><path d="M48,-100 l-6,-22 m6,22 l10,-20 m-14,4 l-8,-6 m18,0 l8,-6" '
          f'stroke="{color}" stroke-width="3" fill="none"/>'
          f'<path d="M-36,-50 L-40,0 h6 L-28,-46Z M-22,-48 L-20,0 h6 L-14,-48Z M22,-48 L20,0 h6 L30,-48Z M34,-44 L38,0 h6 L40,-40Z"/>'
          f'<path d="M-40,-58 l-8,-8 l2,10Z"/></g>')


def tent(s, x, base, w, h, color="#e8743b", inner="#ffcf6a"):
    s.glow(x, base - h * 0.4, w * 0.9, inner, 0.35)
    s.add(f'<path d="M{x - w / 2},{base} L{x},{base - h} L{x + w / 2},{base}Z" fill="{color}"/>')
    s.add(f'<path d="M{x},{base - h} L{x + w / 2},{base} H{x + w * 0.1}Z" fill="#000000" opacity="0.15"/>')
    s.add(f'<path d="M{x - w * 0.16},{base} L{x},{base - h * 0.62} L{x + w * 0.16},{base}Z" fill="{inner}"/>')
    s.add(f'<path d="M{x},{base - h} V{base - h - 14}" stroke="#4a3a2a" stroke-width="3"/>')


def campfire(s, x, base, k):
    anchor(s, "fire", x, base - 34 * k, 60 * k)
    s.glow(x, base - 30 * k, 160 * k, "#ff9a3a", 0.45)
    s.add(f'<path d="M{x - 40 * k},{base} L{x + 30 * k},{base - 16 * k} M{x + 40 * k},{base} L{x - 30 * k},{base - 16 * k}" stroke="#5a3a24" '
          f'stroke-width="{12 * k:.1f}" stroke-linecap="round"/>')
    s.add(f'<path d="M{x - 26 * k},{base - 8 * k} Q{x - 30 * k},{base - 50 * k} {x - 6 * k},{base - 80 * k} Q{x - 2 * k},{base - 52 * k} {x + 8 * k},{base - 60 * k} '
          f'Q{x + 30 * k},{base - 40 * k} {x + 24 * k},{base - 8 * k}Z" fill="#ff7a2a"/>')
    s.add(f'<path d="M{x - 14 * k},{base - 8 * k} Q{x - 16 * k},{base - 34 * k} {x},{base - 52 * k} Q{x + 18 * k},{base - 30 * k} {x + 12 * k},{base - 8 * k}Z" fill="#ffd36b"/>')
    for _ in range(8):
        s.add(f'<circle cx="{x + s.r.uniform(-30, 30) * k:.1f}" cy="{base - s.r.uniform(80, 160) * k:.1f}" r="{s.r.uniform(1.5, 3) * k:.1f}" fill="#ffcf6a"/>')


def canoe(s, x, y, w, color="#c8462a"):
    s.add(f'<path d="M{x - w / 2},{y - w * 0.06} Q{x},{y + w * 0.16} {x + w / 2},{y - w * 0.06} Q{x},{y + w * 0.04} {x - w / 2},{y - w * 0.06}Z" fill="{color}"/>')
    s.add(f'<path d="M{x - w / 2},{y - w * 0.06} Q{x},{y + w * 0.04} {x + w / 2},{y - w * 0.06}" stroke="#7a2a1a" stroke-width="3" fill="none"/>')


def shooting_star(s, x, y, length, color="#ffffff"):
    f = s.lin([(0, color, 0), (1, color, 0.9)], x - length, y - length * 0.35, x, y, "userSpaceOnUse")
    s.add(f'<path d="M{x - length},{y - length * 0.35} L{x},{y}" stroke="{f}" stroke-width="3" stroke-linecap="round"/>')
    s.sparkle(x, y, 9, color, 1)


# ---------- Desert ----------
def hot_air_balloon(s, x, y, k, colors=("#ff5e5b", "#ffd166", "#3fa9f5", "#ffffff")):
    """A hot-air balloon: a striped envelope, ropes and a wicker basket."""
    env = (f"M{x},{y - 70 * k} C{x + 62 * k},{y - 70 * k} {x + 66 * k},{y + 10 * k} {x + 22 * k},{y + 52 * k} "
           f"L{x + 14 * k},{y + 64 * k} H{x - 14 * k} L{x - 22 * k},{y + 52 * k} C{x - 66 * k},{y + 10 * k} {x - 62 * k},{y - 70 * k} {x},{y - 70 * k}Z")
    cid = s.id("hb")
    s.defs.append(f'<clipPath id="{cid}"><path d="{env}"/></clipPath>')
    stripes = "".join(f'<rect x="{x - 70 * k + i * 20 * k:.1f}" y="{y - 80 * k:.1f}" width="{20 * k + 0.5:.1f}" height="{160 * k:.1f}" '
                      f'fill="{colors[i % len(colors)]}"/>' for i in range(7))
    s.add(f'<g clip-path="url(#{cid})">{stripes}<ellipse cx="{x + 30 * k}" cy="{y - 10 * k}" rx="{40 * k}" ry="{70 * k}" fill="#000000" opacity="0.12"/></g>')
    s.add(f'<path d="M{x - 14 * k},{y + 64 * k} L{x - 10 * k},{y + 88 * k} M{x + 14 * k},{y + 64 * k} L{x + 10 * k},{y + 88 * k}" stroke="#6b4a2a" stroke-width="{1.5 * k + 0.5:.1f}"/>')
    s.add(f'<rect x="{x - 12 * k}" y="{y + 86 * k}" width="{24 * k}" height="{18 * k}" rx="{3 * k}" fill="#a8743a"/>')
    s.add(f'<path d="M{x - 30 * k},{y - 40 * k} Q{x - 40 * k},{y} {x - 20 * k},{y + 30 * k}" stroke="#ffffff" stroke-opacity="0.4" '
          f'stroke-width="{5 * k:.1f}" fill="none" stroke-linecap="round"/>')


def tumbleweed(s, x, base, r, color="#b58a52"):
    d = []
    for _ in range(26):
        a1, a2 = s.r.uniform(0, 2 * math.pi), s.r.uniform(0, 2 * math.pi)
        rr1, rr2 = r * s.r.uniform(0.3, 1), r * s.r.uniform(0.3, 1)
        d.append(f"M{x + rr1 * math.cos(a1):.1f},{base - r + rr1 * math.sin(a1):.1f} Q{x:.1f},{base - r:.1f} "
                 f"{x + rr2 * math.cos(a2):.1f},{base - r + rr2 * math.sin(a2):.1f}")
    s.add(f'<path d="{" ".join(d)}" stroke="{color}" stroke-width="2.2" fill="none" opacity="0.9"/>')


def rocks(s, x, base, k, c1="#b5653a", c2="#9a5230"):
    s.add(f'<path d="M{x - 60 * k},{base} Q{x - 58 * k},{base - 40 * k} {x - 20 * k},{base - 46 * k} Q{x + 10 * k},{base - 60 * k} {x + 40 * k},{base - 34 * k} '
          f'Q{x + 64 * k},{base - 20 * k} {x + 66 * k},{base}Z" fill="{c1}"/>')
    s.add(f'<path d="M{x + 10 * k},{base} Q{x + 14 * k},{base - 40 * k} {x + 40 * k},{base - 34 * k} Q{x + 64 * k},{base - 20 * k} {x + 66 * k},{base}Z" fill="{c2}"/>')


# ---------- City ----------
def bridge(s, x0, x1, deck, towers, color="#1a1d44", lights="#ffd27a", th=230):
    """A suspension bridge: towers, the main cables hanging between them, hangers and a lit deck with traffic."""
    for tx in towers:
        s.add(f'<rect x="{tx - 10}" y="{deck - th}" width="20" height="{th + 30}" fill="{color}"/>')
        s.add(f'<rect x="{tx - 18}" y="{deck - th * 0.62}" width="36" height="10" fill="{color}"/>')
        anchor(s, "antenna", tx, deck - th - 6, 0)
        s.glow(tx, deck - th - 6, 18, "#ff4d4d", 0.6)
        s.add(f'<circle cx="{tx}" cy="{deck - th - 6}" r="5" fill="#ff4d4d"/>')
    spans = []
    pts = [x0] + list(towers) + [x1]
    for a_, b_ in zip(pts, pts[1:]):
        ya = deck - th if a_ in towers else deck - 8
        yb = deck - th if b_ in towers else deck - 8
        if a_ in towers and b_ in towers:
            cx_, cy_ = (a_ + b_) / 2, 2 * (deck - 24) - (deck - th)
        elif a_ in towers:
            cx_, cy_ = a_ + (b_ - a_) * 0.45, deck - 8
        else:
            cx_, cy_ = a_ + (b_ - a_) * 0.55, deck - 8
        spans.append((a_, ya, cx_, cy_, b_, yb))
    for (ax, ay, cx_, cy_, bx, by) in spans:
        s.add(f'<path d="M{ax},{ay} Q{cx_},{cy_} {bx},{by}" stroke="{color}" stroke-width="4" fill="none"/>')
        for i in range(1, 16):
            t = i / 16
            hx = (1 - t) ** 2 * ax + 2 * (1 - t) * t * cx_ + t ** 2 * bx
            hy = (1 - t) ** 2 * ay + 2 * (1 - t) * t * cy_ + t ** 2 * by
            s.add(f'<path d="M{hx:.1f},{hy:.1f} V{deck}" stroke="{color}" stroke-width="1.5" opacity="0.8"/>')
    s.add(f'<rect x="{x0}" y="{deck}" width="{x1 - x0}" height="14" fill="{color}"/>')
    for x in range(int(x0), int(x1), 22):
        s.add(f'<circle cx="{x + 11}" cy="{deck + 3}" r="2.6" fill="{lights}"/>')
    for i in range(10):
        lx = s.r.uniform(x0, x1 - 60)
        c = "#ff6b6b" if i % 2 else "#fff3c4"
        s.add(f'<rect x="{lx:.1f}" y="{deck + 7}" width="{s.r.uniform(30, 70):.1f}" height="3" rx="1.5" fill="{c}" opacity="0.85"/>')


def ferris_wheel(s, x, y, r, color="#2a2858", lights=("#ff7ac0", "#ffd27a", "#63d6ff", "#7ef0a0")):
    base = y + r * 1.35
    s.add(f'<path d="M{x - r * 0.55},{base} L{x},{y} L{x + r * 0.55},{base}" stroke="{color}" stroke-width="10" fill="none"/>')
    anchor(s, "ferris", x, y, r)
    s.glow(x, y, r * 1.3, "#ff7ac0", 0.15)
    s.add(f'<circle cx="{x}" cy="{y}" r="{r}" fill="none" stroke="{color}" stroke-width="6"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="{r * 0.92}" fill="none" stroke="{color}" stroke-width="2"/>')
    n = 16
    for i in range(n):
        a = 2 * math.pi * i / n
        ex, ey = x + r * math.cos(a), y + r * math.sin(a)
        s.add(f'<path d="M{x},{y} L{ex:.1f},{ey:.1f}" stroke="{color}" stroke-width="2"/>')
        c = lights[i % len(lights)]
        s.add(f'<circle cx="{ex:.1f}" cy="{ey:.1f}" r="4" fill="{c}"/>')
        if i % 2 == 0:
            s.add(f'<rect x="{ex - 9:.1f}" y="{ey + 4:.1f}" width="18" height="15" rx="4" fill="{c}" opacity="0.9"/>')
    s.add(f'<circle cx="{x}" cy="{y}" r="9" fill="{color}"/>')


# ---------- Farm and mountains ----------
def barn(s, x, base, k, wall="#b8322a", roof="#7e1f1b", trim="#ffffff"):
    w, h = 200 * k, 150 * k
    s.add(f'<rect x="{x - w / 2}" y="{base - h}" width="{w}" height="{h}" fill="{wall}"/>')
    s.add(f'<rect x="{x + w * 0.15}" y="{base - h}" width="{w * 0.35}" height="{h}" fill="#000000" opacity="0.1"/>')
    s.add(f'<path d="M{x - w * 0.59},{base - h + 10 * k} L{x - w * 0.35},{base - h - 90 * k} L{x + w * 0.35},{base - h - 90 * k} L{x + w * 0.59},{base - h + 10 * k}Z" fill="{roof}"/>')
    s.add(f'<path d="M{x - w * 0.59},{base - h + 10 * k} L{x - w * 0.35},{base - h - 90 * k} L{x + w * 0.35},{base - h - 90 * k} L{x + w * 0.59},{base - h + 10 * k}" '
          f'fill="none" stroke="{trim}" stroke-width="{7 * k:.1f}"/>')
    s.add(f'<rect x="{x - 40 * k}" y="{base - 90 * k}" width="{80 * k}" height="{90 * k}" fill="{trim}"/>')
    s.add(f'<rect x="{x - 34 * k}" y="{base - 84 * k}" width="{68 * k}" height="{84 * k}" fill="{roof}"/>')
    s.add(f'<path d="M{x - 34 * k},{base - 84 * k} L{x + 34 * k},{base} M{x + 34 * k},{base - 84 * k} L{x - 34 * k},{base}" stroke="{trim}" stroke-width="{6 * k:.1f}"/>')
    s.add(f'<rect x="{x - 22 * k}" y="{base - h - 50 * k}" width="{44 * k}" height="{34 * k}" fill="{trim}"/>')
    s.add(f'<rect x="{x - 17 * k}" y="{base - h - 45 * k}" width="{34 * k}" height="{24 * k}" fill="#3a2a2a"/>')


def silo(s, x, base, k):
    s.add(f'<rect x="{x - 28 * k}" y="{base - 190 * k}" width="{56 * k}" height="{190 * k}" rx="{4 * k}" fill="#c9ccd2"/>')
    s.add(f'<rect x="{x + 6 * k}" y="{base - 190 * k}" width="{22 * k}" height="{190 * k}" fill="#000000" opacity="0.08"/>')
    for i in range(5):
        s.add(f'<path d="M{x - 28 * k},{base - 30 * k - i * 34 * k} H{x + 28 * k}" stroke="#aeb2b9" stroke-width="{3 * k:.1f}"/>')
    s.add(f'<path d="M{x - 28 * k},{base - 190 * k} A{28 * k},{24 * k} 0 0,1 {x + 28 * k},{base - 190 * k}Z" fill="#9aa0a8"/>')


def windmill(s, x, base, h, color="#7a8088", blades="#c9ccd2"):
    """A farm windmill: a lattice tower and a many-bladed wheel with a tail vane."""
    top = base - h
    s.add(f'<path d="M{x - h * 0.16},{base} L{x - h * 0.03},{top + h * 0.08} M{x + h * 0.16},{base} L{x + h * 0.03},{top + h * 0.08}" stroke="{color}" stroke-width="4"/>')
    for i in range(6):
        y0 = base - h * 0.15 * i
        w0 = h * (0.16 - 0.022 * i)
        y1 = base - h * 0.15 * (i + 1)
        w1 = h * (0.16 - 0.022 * (i + 1))
        s.add(f'<path d="M{x - w0:.1f},{y0:.1f} L{x + w1:.1f},{y1:.1f} M{x + w0:.1f},{y0:.1f} L{x - w1:.1f},{y1:.1f} M{x - w1:.1f},{y1:.1f} H{x + w1:.1f}" '
              f'stroke="{color}" stroke-width="2"/>')
    s.add(f'<path d="M{x},{top + h * 0.06} L{x + h * 0.3},{top + h * 0.04} L{x + h * 0.36},{top - h * 0.04} L{x + h * 0.36},{top + h * 0.12} L{x + h * 0.3},{top + h * 0.08}Z" fill="{color}"/>')
    r = h * 0.22
    for i in range(16):
        a = 2 * math.pi * i / 16
        s.add(f'<path d="M{x + r * 0.25 * math.cos(a):.1f},{top + h * 0.06 + r * 0.25 * math.sin(a):.1f} '
              f'L{x + r * math.cos(a - 0.08):.1f},{top + h * 0.06 + r * math.sin(a - 0.08):.1f} '
              f'L{x + r * math.cos(a + 0.08):.1f},{top + h * 0.06 + r * math.sin(a + 0.08):.1f}Z" fill="{blades}"/>')
    s.add(f'<circle cx="{x}" cy="{top + h * 0.06}" r="{r * 0.2}" fill="{color}"/>')


def tractor(s, x, base, k, body="#c8302a", wheel="#2b2b2b"):
    s.add(f'<rect x="{x - 60 * k}" y="{base - 70 * k}" width="{100 * k}" height="{36 * k}" rx="{6 * k}" fill="{body}"/>')
    s.add(f'<rect x="{x - 10 * k}" y="{base - 120 * k}" width="{50 * k}" height="{54 * k}" rx="{4 * k}" fill="{body}"/>')
    s.add(f'<rect x="{x - 2 * k}" y="{base - 112 * k}" width="{34 * k}" height="{28 * k}" fill="#bfe3f5"/>')
    s.add(f'<rect x="{x - 56 * k}" y="{base - 96 * k}" width="{8 * k}" height="{28 * k}" fill="#3a3a3a"/>')
    s.add(f'<circle cx="{x + 22 * k}" cy="{base - 34 * k}" r="{34 * k}" fill="{wheel}"/><circle cx="{x + 22 * k}" cy="{base - 34 * k}" r="{14 * k}" fill="#ffd166"/>')
    s.add(f'<circle cx="{x - 44 * k}" cy="{base - 20 * k}" r="{20 * k}" fill="{wheel}"/><circle cx="{x - 44 * k}" cy="{base - 20 * k}" r="{8 * k}" fill="#ffd166"/>')


def cow(s, x, base, k, facing=1):
    f = facing
    g = [f'<ellipse cx="0" cy="-48" rx="46" ry="26" fill="#ffffff"/>',
         f'<path d="M-30,-60 q14,-8 22,6 q-6,14 -24,6Z M8,-40 q16,-6 22,8 q-10,10 -24,2Z" fill="#2b2b2b"/>',
         f'<rect x="-36" y="-30" width="9" height="30" fill="#ffffff"/><rect x="-18" y="-30" width="9" height="30" fill="#ffffff"/>'
         f'<rect x="12" y="-30" width="9" height="30" fill="#ffffff"/><rect x="28" y="-30" width="9" height="30" fill="#ffffff"/>',
         f'<rect x="-36" y="-4" width="9" height="4" fill="#3a3a3a"/><rect x="-18" y="-4" width="9" height="4" fill="#3a3a3a"/>'
         f'<rect x="12" y="-4" width="9" height="4" fill="#3a3a3a"/><rect x="28" y="-4" width="9" height="4" fill="#3a3a3a"/>',
         f'<ellipse cx="54" cy="-58" rx="16" ry="20" fill="#ffffff"/><ellipse cx="58" cy="-44" rx="12" ry="9" fill="#ffc2c2"/>',
         f'<path d="M44,-74 l-8,-8 M62,-74 l8,-8" stroke="#d9c7a8" stroke-width="4" stroke-linecap="round"/><circle cx="56" cy="-62" r="2.5" fill="#2b2b2b"/>',
         f'<path d="M-46,-52 q-12,10 -8,26" stroke="#2b2b2b" stroke-width="3" fill="none"/>']
    s.add(f'<g transform="translate({x},{base}) scale({f * k},{k})">{"".join(g)}</g>')


def sunflower(s, x, base, h):
    s.add(f'<path d="M{x},{base} V{base - h}" stroke="#4f8a3a" stroke-width="{h * 0.04:.1f}"/>')
    for side in (-1, 1):
        s.add(f'<ellipse cx="{x + side * h * 0.1:.1f}" cy="{base - h * 0.45:.1f}" rx="{h * 0.1:.1f}" ry="{h * 0.045:.1f}" fill="#5a9a42" '
              f'transform="rotate({side * -25} {x + side * h * 0.1:.1f} {base - h * 0.45:.1f})"/>')
    cy = base - h
    for i in range(14):
        a = 2 * math.pi * i / 14
        s.add(f'<ellipse cx="{x + h * 0.12 * math.cos(a):.1f}" cy="{cy + h * 0.12 * math.sin(a):.1f}" rx="{h * 0.07:.1f}" ry="{h * 0.03:.1f}" '
              f'fill="#ffc414" transform="rotate({math.degrees(a):.0f} {x + h * 0.12 * math.cos(a):.1f} {cy + h * 0.12 * math.sin(a):.1f})"/>')
    s.add(f'<circle cx="{x}" cy="{cy}" r="{h * 0.07}" fill="#6b3f1f"/>')


def waterfall(s, x, top, bottom, w):
    s.add(f'<rect x="{x - w / 2}" y="{top}" width="{w}" height="{bottom - top}" fill="#cfeaff"/>')
    for i in range(6):
        lx = x - w / 2 + w * (i + 0.5) / 6
        s.add(f'<path d="M{lx:.1f},{top} V{bottom}" stroke="#ffffff" stroke-width="3" opacity="0.7" stroke-dasharray="{s.r.uniform(20, 40):.0f} 12"/>')
    s.add(f'<ellipse cx="{x}" cy="{bottom}" rx="{w * 1.2}" ry="{w * 0.3}" fill="#ffffff" opacity="0.8"/>')


def eagle(s, x, y, k, color="#3a2a22"):
    s.add(f'<g transform="translate({x},{y}) scale({k})" fill="{color}"><path d="M-70,-6 Q-40,-22 -12,-6 Q0,-12 12,-6 Q40,-22 70,-6 Q46,-8 20,2 '
          f'Q8,6 0,4 Q-8,6 -20,2 Q-46,-8 -70,-6Z"/><path d="M-6,2 L0,16 L6,2Z"/><circle cx="0" cy="-8" r="5" fill="#ffffff"/></g>')


# ---------- simple themes ----------
def rack(s, x, base, w, h, upright="#3a6ea5", beam="#f08a24", box_colors=("#c9a26b", "#b98e57", "#d8b47c")):
    levels = 4
    for k in range(levels):
        y = base - h + k * h / levels
        for b in range(3):
            bw = w / 3 - 18
            bx = x - w / 2 + 8 + b * w / 3
            bh = h / levels - 22
            if s.r.random() < 0.85:
                c = s.r.choice(box_colors)
                s.add(f'<rect x="{bx:.1f}" y="{y + h / levels - bh - 8:.1f}" width="{bw:.1f}" height="{bh:.1f}" fill="{c}"/>')
                s.add(f'<path d="M{bx:.1f},{y + h / levels - bh + 4:.1f} H{bx + bw:.1f}" stroke="#a07a48" stroke-width="3"/>')
                s.add(f'<rect x="{bx + bw * 0.4:.1f}" y="{y + h / levels - bh - 8:.1f}" width="{bw * 0.2:.1f}" height="{bh * 0.3:.1f}" fill="#e8d2a8"/>')
        s.add(f'<rect x="{x - w / 2}" y="{y + h / levels - 8:.1f}" width="{w}" height="10" fill="{beam}"/>')
    for ux in (x - w / 2, x - w / 6, x + w / 6, x + w / 2):
        s.add(f'<rect x="{ux - 5:.1f}" y="{base - h - 10:.1f}" width="10" height="{h + 10}" fill="{upright}"/>')


def forklift(s, x, base, k, body="#f2b31a"):
    s.add(f'<rect x="{x - 50 * k}" y="{base - 60 * k}" width="{90 * k}" height="{40 * k}" rx="{8 * k}" fill="{body}"/>')
    s.add(f'<path d="M{x - 30 * k},{base - 60 * k} V{base - 120 * k} H{x + 20 * k} L{x + 30 * k},{base - 60 * k}" stroke="#2b2b2b" stroke-width="{5 * k:.1f}" fill="none"/>')
    s.add(f'<rect x="{x + 40 * k}" y="{base - 140 * k}" width="{8 * k}" height="{140 * k}" fill="#4a4a4a"/>')
    s.add(f'<rect x="{x + 48 * k}" y="{base - 16 * k}" width="{50 * k}" height="{6 * k}" fill="#4a4a4a"/>')
    s.add(f'<rect x="{x + 50 * k}" y="{base - 60 * k}" width="{46 * k}" height="{44 * k}" fill="#c9a26b"/>')
    for (cx, r) in ((x - 30 * k, 16 * k), (x + 22 * k, 14 * k)):
        s.add(f'<circle cx="{cx}" cy="{base - r}" r="{r}" fill="#2b2b2b"/><circle cx="{cx}" cy="{base - r}" r="{r * 0.4}" fill="#9a9a9a"/>')


def cart(s, x, base, k, color="#ffffff"):
    """A shopping cart full of groceries."""
    s.add(f'<path d="M{x - 140 * k},{base - 230 * k} H{x - 110 * k} L{x - 70 * k},{base - 70 * k} H{x + 110 * k}" stroke="{color}" '
          f'stroke-width="{10 * k:.1f}" fill="none" stroke-linecap="round" stroke-linejoin="round"/>')
    for (bx, by, bw, bh, c) in ((-60, -250, 60, 100, "#ff8fab"), (0, -270, 46, 120, "#7ec8f5"), (50, -235, 56, 85, "#ffd166"),
                                (-90, -200, 50, 50, "#9be38a")):
        s.add(f'<rect x="{x + bx * k}" y="{base + by * k}" width="{bw * k}" height="{bh * k}" rx="{6 * k}" fill="{c}"/>')
    s.add(f'<path d="M{x - 8 * k},{base - 300 * k} l{8 * k},-{30 * k} l{8 * k},{30 * k}" fill="#4caf50"/>')
    s.add(f'<ellipse cx="{x + 2 * k}" cy="{base - 284 * k}" rx="{26 * k}" ry="{18 * k}" fill="#ff6b6b"/>')
    s.add(f'<path d="M{x - 100 * k},{base - 210 * k} H{x + 130 * k} L{x + 110 * k},{base - 90 * k} H{x - 76 * k}Z" fill="{color}" fill-opacity="0.18" '
          f'stroke="{color}" stroke-width="{8 * k:.1f}" stroke-linejoin="round"/>')
    for i in range(1, 6):
        gx = x - 100 * k + i * 38 * k
        s.add(f'<path d="M{gx:.1f},{base - 210 * k:.1f} L{gx - 6 * k:.1f},{base - 90 * k:.1f}" stroke="{color}" stroke-width="{4 * k:.1f}" opacity="0.8"/>')
    for i in range(1, 3):
        gy = base - 210 * k + i * 40 * k
        s.add(f'<path d="M{x - 96 * k + i * 6 * k:.1f},{gy:.1f} H{x + 126 * k - i * 6 * k:.1f}" stroke="{color}" stroke-width="{4 * k:.1f}" opacity="0.8"/>')
    for wx in (x - 50 * k, x + 90 * k):
        s.add(f'<circle cx="{wx}" cy="{base - 30 * k}" r="{24 * k}" fill="#2b2140"/><circle cx="{wx}" cy="{base - 30 * k}" r="{10 * k}" fill="{color}"/>')


def shopping_bag(s, x, base, w, h, color, handle="#ffffff"):
    s.add(f'<path d="M{x - w * 0.25},{base - h} Q{x - w * 0.25},{base - h - w * 0.45} {x},{base - h - w * 0.45} Q{x + w * 0.25},{base - h - w * 0.45} '
          f'{x + w * 0.25},{base - h}" stroke="{handle}" stroke-width="{w * 0.06:.1f}" fill="none"/>')
    s.add(f'<path d="M{x - w / 2},{base - h} H{x + w / 2} L{x + w * 0.46},{base} H{x - w * 0.46}Z" fill="{color}"/>')
    s.add(f'<path d="M{x + w * 0.1},{base - h} H{x + w / 2} L{x + w * 0.46},{base} H{x + w * 0.06}Z" fill="#000000" opacity="0.1"/>')
