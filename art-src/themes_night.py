"""Pass Studio theme art: cities at night. Laid out like themes.py: each theme has a page sky (top of the page down to the
scene), decorations in that sky (page coordinates, kept to the edges and off the pass), and a scene along the bottom
(band coordinates, 0..SW x 0..SH)."""
from props import *  # noqa: F401,F403
from themes import theme, scatter  # noqa: F401

Front = None  # set by render.py: `with Front(s):` draws in front of the scene band


def _pts(points):
    return " L".join(f"{x:.1f},{y:.1f}" for x, y in points)


# ======================= Paris: the Eiffel Tower lit gold over the Seine =======================
def _eiffel_outer(h):
    """The tower's half-width in meters, h meters up: it curves in fast near the ground, then runs nearly straight."""
    if h <= 115:
        return 62.5 * math.exp(-h / 89.8)
    if h <= 276:
        return 17.4 + (8.0 - 17.4) * (h - 115) / 161
    if h <= 300:
        return 8.0 - 5.0 * (h - 276) / 24
    return 1.0


def _eiffel_inner(h):
    """Half the open space between the legs, in meters, h meters up (the legs meet above the second floor)."""
    if h <= 57:
        return 37.5 + (14 - 37.5) * (h / 57) ** 0.85
    if h <= 115:
        return 14 + (2 - 14) * (h - 57) / 58
    if h <= 170:
        return 2 * (1 - (h - 115) / 55)
    return 0.0


def eiffel(s, cx, base, top):
    """The Eiffel Tower lit up at night: four legs curving in, the arch between them, three floors, the latticework
    and the beacon at the top. Records 'sparkle' points all over the ironwork and the 'beacon'."""
    ppm = (base - top) / 330.0

    def ys(h):
        return base - h * ppm

    hs = [i * 2.5 for i in range(0, 121)]
    left = [(cx - _eiffel_outer(h) * ppm, ys(h)) for h in hs]
    right = [(cx + _eiffel_outer(h) * ppm, ys(h)) for h in reversed(hs)]
    gh = [i * 2.5 for i in range(0, 69)]
    gl = [(cx - _eiffel_inner(h) * ppm, ys(h)) for h in gh]
    gr = [(cx + _eiffel_inner(h) * ppm, ys(h)) for h in reversed(gh)]
    sil = f"M{_pts(left + right)}Z M{_pts(gl + gr)}Z"
    w0 = _eiffel_outer(0) * ppm
    s.glow(cx, ys(140), 380, "#ffc861", 0.17)
    s.glow(cx, ys(40), 230, "#ffb347", 0.2)
    gold = s.lin([(0, "#ffe7a6", 1), (0.45, "#f8c763", 1), (1, "#d48f2c", 1)], cx - w0, 0, cx + w0, 0, "userSpaceOnUse")
    s.add(f'<path d="{sil}" fill="{gold}" fill-rule="evenodd"/>')
    cid = s.id("ef")
    s.defs.append(f'<clipPath id="{cid}"><path d="{sil}" clip-rule="evenodd"/></clipPath>')
    lat = []
    run = 700
    k_ = run / math.tan(math.radians(63))
    for k in range(-44, 45):
        x0 = cx + k * 10.5
        lat.append(f"M{x0:.1f},{base} L{x0 + k_:.1f},{base - run}")
        lat.append(f"M{x0:.1f},{base} L{x0 - k_:.1f},{base - run}")
    bands = "".join(f"M{cx - w0 - 10:.1f},{y:.1f} H{cx + w0 + 10:.1f} " for y in [base - 4 - i * 15 for i in range(42)])
    s.add(f'<g clip-path="url(#{cid})"><path d="{" ".join(lat)}" stroke="#8f5716" stroke-width="1.25" opacity="0.55" fill="none"/>'
          f'<path d="{bands}" stroke="#a8691c" stroke-width="1.6" opacity="0.5" fill="none"/>'
          f'<rect x="{cx}" y="{top}" width="{w0 + 10:.1f}" height="{base - top}" fill="#6a3a10" opacity="0.14"/></g>')
    # Bright edges on the lit sides.
    s.add(f'<path d="M{_pts(left)}" stroke="#fff1c2" stroke-width="2" fill="none" opacity="0.9"/>')
    s.add(f'<path d="M{_pts(list(reversed(right)))}" stroke="#ffd27a" stroke-width="1.6" fill="none" opacity="0.8"/>')
    s.add(f'<path d="M{_pts(gl)}" stroke="#ffe3a0" stroke-width="1.4" fill="none" opacity="0.8"/>')
    s.add(f'<path d="M{_pts(list(reversed(gr)))}" stroke="#ffe3a0" stroke-width="1.4" fill="none" opacity="0.8"/>')
    # The arch between the legs.
    g8 = _eiffel_inner(8) * ppm
    ya, yt = ys(8), ys(44)
    arch = (f"M{cx - g8:.1f},{ya:.1f} C{cx - g8:.1f},{ys(30):.1f} {cx - g8 * 0.55:.1f},{yt:.1f} {cx:.1f},{yt:.1f} "
            f"C{cx + g8 * 0.55:.1f},{yt:.1f} {cx + g8:.1f},{ys(30):.1f} {cx + g8:.1f},{ya:.1f}")
    s.add(f'<path d="{arch}" stroke="#f2b84a" stroke-width="7" fill="none"/>')
    s.add(f'<path d="{arch}" stroke="#fff0bd" stroke-width="1.6" fill="none" transform="translate(0,-3)"/>')
    s.add(f'<path d="{arch}" stroke="#a8691c" stroke-width="1.2" fill="none" transform="translate(0,4)" opacity="0.7"/>')
    # The three floors and the top.
    for h, extra, th, fill in ((57, 7, 12, "#f6c35a"), (115, 4.5, 9, "#f8c967"), (276, 3, 9, "#fff0c4")):
        hw = (_eiffel_outer(h) + extra) * ppm
        y = ys(h)
        s.add(f'<rect x="{cx - hw:.1f}" y="{y - th * 0.7:.1f}" width="{2 * hw:.1f}" height="{th}" fill="{fill}"/>')
        s.add(f'<rect x="{cx - hw:.1f}" y="{y - th * 0.7:.1f}" width="{2 * hw:.1f}" height="1.8" fill="#fff6d8"/>')
        ticks = "".join(f'<rect x="{cx - hw + 3 + i * 6.5:.1f}" y="{y - th * 0.35:.1f}" width="3" height="{th * 0.45:.1f}" '
                        f'fill="#8f5716" opacity="0.55"/>' for i in range(int((2 * hw - 6) / 6.5)))
        s.add(ticks)
        s.glow(cx, y, hw * 1.6, "#ffd98a", 0.25)
    yl = ys(300)
    s.add(f'<path d="M{cx - 5:.1f},{yl + 2:.1f} Q{cx - 5:.1f},{yl - 8:.1f} {cx:.1f},{yl - 9:.1f} Q{cx + 5:.1f},{yl - 8:.1f} {cx + 5:.1f},{yl + 2:.1f}Z" fill="#f8d27a"/>')
    s.add(f'<path d="M{cx:.1f},{yl - 8:.1f} V{top:.1f}" stroke="#e6b04c" stroke-width="2.4"/>')
    for k in range(3):
        y = yl - 18 - k * 13
        s.add(f'<path d="M{cx - 3.5:.1f},{y:.1f} H{cx + 3.5:.1f}" stroke="#e6b04c" stroke-width="1.6"/>')
    anchor(s, "beacon", cx, yl - 4, 700)
    s.glow(cx, yl - 4, 22, "#fffbe6", 0.85)
    s.add(f'<circle cx="{cx}" cy="{yl - 4:.1f}" r="3.2" fill="#ffffff"/>')

    # Sparkles all over the ironwork: some spread by area (more on the wide legs), some by height.
    def inside(x, y):
        h = (base - y) / ppm
        if h < 0 or h > 330:
            return False
        d = abs(x - cx)
        if h > 300:
            return d < 2.5
        return _eiffel_inner(h) * ppm + 1.5 < d < _eiffel_outer(h) * ppm - 1.5

    n = 0
    tries = 0
    while n < 160 and tries < 20000:
        tries += 1
        if s.r.random() < 0.5:
            x, y = s.r.uniform(cx - w0, cx + w0), s.r.uniform(top + 4, base - 4)
        else:
            y = s.r.uniform(top + 4, base - 4)
            ow = _eiffel_outer((base - y) / ppm) * ppm
            x = s.r.uniform(cx - ow, cx + ow)
        if inside(x, y):
            anchor(s, "sparkle", x, y, 0)
            n += 1


def haussmann(s, x0, x1, base, top, wall="#6b6a90", shade="#5a587e", roof="#262944", lit="#ffcf7a",
              unlit="#2c2f50", lit_p=0.5, rec=0.3, floors=5, shop="#ffb35a"):
    """A Paris apartment block at night: a stone front with tall windows in rows, iron balconies on the 2nd and 5th
    floors, cornices, shopfronts below, and a dark zinc mansard roof with dormers and chimney stacks. Some lit
    windows (about [rec] of them) are recorded as 'window'."""
    r = s.r
    w = x1 - x0
    rh = 44
    for f in (0.18, 0.5, 0.82):
        cxh = x0 + w * f
        s.add(f'<rect x="{cxh - 11:.1f}" y="{top - rh - 24}" width="22" height="34" fill="#34365a"/>')
        s.add(f'<rect x="{cxh - 13:.1f}" y="{top - rh - 26}" width="26" height="5" fill="#45476c"/>')
        for k in range(3):
            s.add(f'<rect x="{cxh - 9 + k * 7:.1f}" y="{top - rh - 34}" width="4" height="9" fill="#6a4f58"/>')
    s.add(f'<path d="M{x0 - 3},{top} L{x0 + 14},{top - rh} L{x1 - 14},{top - rh} L{x1 + 3},{top}Z" fill="{roof}"/>')
    s.add(f'<path d="M{x0 + 14},{top - rh} L{x1 - 14},{top - rh}" stroke="#4a4e72" stroke-width="3"/>')
    nd = max(2, int((w - 30) / 42))
    for i in range(nd):
        dx = x0 + 22 + i * (w - 44) / max(1, nd - 1) if nd > 1 else x0 + w / 2
        dl = r.random() < lit_p * 0.6
        s.add(f'<rect x="{dx - 8:.1f}" y="{top - rh + 12}" width="16" height="22" fill="#3a3d60"/>')
        s.add(f'<path d="M{dx - 10:.1f},{top - rh + 13} L{dx:.1f},{top - rh + 4} L{dx + 10:.1f},{top - rh + 13}Z" fill="#3a3d60"/>')
        s.add(f'<rect x="{dx - 5:.1f}" y="{top - rh + 16}" width="10" height="15" fill="{lit if dl else unlit}" '
              f'opacity="{0.9 if dl else 1}"/>')
    s.add(f'<rect x="{x0}" y="{top}" width="{w}" height="{base - top}" fill="{wall}"/>')
    s.add(f'<rect x="{x0 + w * 0.62:.1f}" y="{top}" width="{w * 0.38:.1f}" height="{base - top}" fill="{shade}" opacity="0.55"/>')
    s.add(f'<rect x="{x0 - 4}" y="{top - 3}" width="{w + 8}" height="7" fill="#8c8ab0"/>')
    gh = (base - top) * 0.22
    fh = (base - top - gh - 8) / floors
    cols = max(2, int(w / 30))
    sp = w / cols
    for f in range(floors):
        fy = top + 8 + f * fh
        if f in (0, floors - 3):
            s.add(f'<rect x="{x0 + 2}" y="{fy + fh * 0.72:.1f}" width="{w - 4}" height="3" fill="#1c1e36"/>')
            ticks = "".join(f"M{x0 + 4 + t * 4.5:.1f},{fy + fh * 0.72:.1f} v{fh * 0.13:.1f} " for t in range(int((w - 8) / 4.5)))
            s.add(f'<path d="{ticks}" stroke="#1c1e36" stroke-width="1.2"/>')
            s.add(f'<rect x="{x0 + 2}" y="{fy + fh * 0.85:.1f}" width="{w - 4}" height="2" fill="#1c1e36"/>')
        for c in range(cols):
            wx = x0 + sp * (c + 0.5) - 7
            ww, wh = 14, fh * 0.66
            on = r.random() < lit_p
            if on:
                s.glow(wx + ww / 2, fy + wh / 2, 20, lit, 0.22)
            s.add(f'<rect x="{wx:.1f}" y="{fy:.1f}" width="{ww}" height="{wh:.1f}" fill="{lit if on else unlit}" '
                  f'opacity="{r.uniform(0.72, 1) if on else 1:.2f}"/>')
            s.add(f'<path d="M{wx + ww / 2:.1f},{fy:.1f} V{fy + wh:.1f} M{wx:.1f},{fy + wh * 0.38:.1f} H{wx + ww:.1f}" '
                  f'stroke="{"#c98f3a" if on else "#3c4064"}" stroke-width="1.2"/>')
            s.add(f'<rect x="{wx - 2:.1f}" y="{fy - 3:.1f}" width="{ww + 4}" height="2.5" fill="#86849f"/>')
            if on and r.random() < rec:
                anchor(s, "window", wx + ww / 2, fy + wh / 2, ww)
    gy = base - gh
    s.add(f'<rect x="{x0 - 2}" y="{gy - 4:.1f}" width="{w + 4}" height="5" fill="#8c8ab0"/>')
    for c in range(max(1, int(w / 60))):
        n = max(1, int(w / 60))
        bx = x0 + (c + 0.5) * w / n
        on = r.random() < 0.6
        s.add(f'<path d="M{bx - 20:.1f},{base} V{gy + 12:.1f} Q{bx:.1f},{gy + 2:.1f} {bx + 20:.1f},{gy + 12:.1f} V{base}Z" '
              f'fill="{shop if on else "#24264a"}" opacity="{0.85 if on else 1}"/>')
        if on:
            s.glow(bx, gy + gh * 0.6, 46, shop, 0.3)


def quay_trees(s, x0, x1, base, top, colors=("#14262e", "#1b3338", "#28463f"), lamp="#ffcf7a"):
    """A row of trees along a quay at night: dark round crowns, warm where the street lamps light them."""
    r = s.r
    x = x0
    while x < x1:
        rr = r.uniform(30, 46)
        cy = r.uniform(top + rr * 0.6, top + rr * 1.1)
        s.add(f'<rect x="{x - 3:.1f}" y="{cy:.1f}" width="6" height="{base - cy:.1f}" fill="#0f1a20"/>')
        for k in range(5):
            bx, by, br = x + r.uniform(-rr * 0.7, rr * 0.7), cy + r.uniform(-rr * 0.5, rr * 0.3), rr * r.uniform(0.5, 0.75)
            s.add(f'<circle cx="{bx:.1f}" cy="{by:.1f}" r="{br:.1f}" fill="{colors[0]}"/>')
        for k in range(4):
            bx, by, br = x + r.uniform(-rr * 0.6, rr * 0.4), cy + r.uniform(-rr * 0.6, rr * 0.1), rr * r.uniform(0.32, 0.5)
            s.add(f'<circle cx="{bx:.1f}" cy="{by:.1f}" r="{br:.1f}" fill="{colors[1]}"/>')
        bx = x + r.uniform(-rr * 0.3, rr * 0.3)
        s.add(f'<circle cx="{bx:.1f}" cy="{cy + rr * 0.45:.1f}" r="{rr * 0.4:.1f}" fill="{lamp}" opacity="0.07"/>')
        s.add(f'<circle cx="{bx - rr * 0.2:.1f}" cy="{cy - rr * 0.35:.1f}" r="{rr * 0.3:.1f}" fill="{colors[2]}" opacity="0.55"/>')
        x += rr * r.uniform(1.2, 1.6)


def railing(s, x0, x1, top, base, color="#14152b", gap=13):
    """An iron railing: top and bottom rails with thin bars between."""
    s.add(f'<rect x="{x0}" y="{top}" width="{x1 - x0}" height="4" fill="{color}"/>')
    s.add(f'<rect x="{x0}" y="{base - 4}" width="{x1 - x0}" height="3" fill="{color}"/>')
    bars = "".join(f"M{x:.1f},{top + 4} V{base - 2} " for x in [x0 + 4 + i * gap for i in range(int((x1 - x0) / gap))])
    s.add(f'<path d="{bars}" stroke="{color}" stroke-width="2"/>')
    posts = "".join(f'<rect x="{x - 3:.1f}" y="{top - 4}" width="6" height="{base - top + 4}" fill="{color}"/>'
                    f'<circle cx="{x:.1f}" cy="{top - 5}" r="4" fill="{color}"/>' for x in [x0 + 20 + i * 130 for i in range(int((x1 - x0) / 130) + 1)])
    s.add(posts)


@theme("paris", [(0, "#070b22"), (0.5, "#0c1230"), (0.82, "#161d48"), (1, "#232a5c")])
class Paris:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 150, BAND_Y - 40, "#ffffff", 0.07)
        s.moon(880, 460, 50, color="#fff4d6", halo="#fff1c9", ha=0.3)

    @staticmethod
    def scene(s):
        s.sky("#232a5c", "#30336a", "#4a4078")
        s.stars(34, 320)
        f = s.blur(40)
        s.add(f'<ellipse cx="516" cy="640" rx="620" ry="150" fill="#ff9e6a" opacity="0.2" filter="{f}"/>')
        # The far bank: apartment blocks on both sides, trees along the quay, and the tower between them.
        haussmann(s, -20, 120, 664, 486, lit_p=0.5)
        haussmann(s, 124, 262, 664, 456, wall="#706e94", lit_p=0.55)
        haussmann(s, 266, 412, 664, 500, wall="#67658c", lit_p=0.45)
        haussmann(s, 712, 858, 664, 494, wall="#6d6b92", lit_p=0.5)
        haussmann(s, 862, 1052, 664, 462, lit_p=0.55)
        eiffel(s, 560, 690, 70)
        quay_trees(s, -10, 430, 664, 584)
        quay_trees(s, 690, 1050, 664, 590)
        # The Seine behind and under the bridge.
        s.add(f'<rect x="0" y="664" width="{SW}" height="{SH - 664}" fill="#141a3a"/>')
        # Pont d'Iéna: a stone bridge with five arches, lamps along its parapet.
        stone = s.lin([(0, "#7a7898", 1), (1, "#52507a", 1)], 0, 662, 0, 745, "userSpaceOnUse")
        piers = [-30 + i * 218 for i in range(6)]
        arches = []
        for a_, b_ in zip(piers, piers[1:]):
            l, r_ = a_ + 18, b_ - 18
            arches.append(f"M{l},745 V720 Q{(l + r_) / 2},676 {r_},720 V745Z")
        s.add(f'<path d="M0,670 H{SW} V745 H0Z {" ".join(arches)}" fill="{stone}" fill-rule="evenodd"/>')
        for a_, b_ in zip(piers, piers[1:]):
            l, r_ = a_ + 18, b_ - 18
            s.add(f'<path d="M{l},720 Q{(l + r_) / 2},676 {r_},720" stroke="#9a96b8" stroke-width="3" fill="none"/>')
            for k in range(7):
                t = (k + 0.5) / 7
                mx = l + (r_ - l) * t
                my = (1 - t) ** 2 * 720 + 2 * (1 - t) * t * 676 + t * t * 720
                s.add(f'<path d="M{mx:.1f},{my:.1f} l0,-7" stroke="#8884a8" stroke-width="1.5"/>')
            gw = s.lin([(0, "#ffd27a", 0.0), (1, "#ffd27a", 0.35)], 0, 700, 0, 745, "userSpaceOnUse")
            s.add(f'<rect x="{l + 20}" y="700" width="{r_ - l - 40}" height="45" fill="{gw}"/>')
        for px in piers[1:-1]:
            s.add(f'<rect x="{px - 22}" y="676" width="44" height="69" fill="#625f88"/>')
            s.add(f'<circle cx="{px}" cy="700" r="10" fill="#3c3a60"/><path d="M{px - 8},{702} q8,-12 16,0" stroke="#8884a8" stroke-width="2" fill="none"/>')
        s.add(f'<rect x="0" y="660" width="{SW}" height="12" fill="#8a87ab"/>')
        s.add(f'<rect x="0" y="658" width="{SW}" height="3" fill="#b5b2cf"/>')
        balusters = "".join(f"M{x},{662} v9 " for x in range(4, SW, 9))
        s.add(f'<path d="{balusters}" stroke="#5a5880" stroke-width="3"/>')
        for x in range(90, SW, 170):
            lamp_post(s, x, 660, 46, "#ffd98a", "#2a2a46")
        # The river in front, catching the lights.
        anchor(s, "water", 0, 745, 0)
        anchor(s, "lane", 0, 796, 1.0)
        water = s.lin([(0, "#1b2452", 1), (1, "#10163a", 1)], 0, 745, 0, 832, "userSpaceOnUse")
        s.add(f'<rect x="0" y="745" width="{SW}" height="87" fill="{water}"/>')
        s.add(f'<rect x="0" y="745" width="{SW}" height="8" fill="#0a0f28" opacity="0.7"/>')
        for _ in range(46):
            x = s.r.gauss(560, 70)
            y = s.r.uniform(756, 826)
            w = s.r.uniform(14, 50) * (1 - abs(x - 560) / 300)
            if w > 4:
                s.add(f'<rect x="{x - w / 2:.1f}" y="{y:.1f}" width="{w:.1f}" height="2.6" rx="1.3" fill="#ffd27a" opacity="{s.r.uniform(0.35, 0.8):.2f}"/>')
        for x in range(90, SW, 170):
            for k in range(5):
                y = 760 + k * 13
                w = 18 - k * 3
                s.add(f'<rect x="{x - w / 2}" y="{y}" width="{w}" height="2.4" rx="1.2" fill="#ffd98a" opacity="{0.55 - k * 0.1:.2f}"/>')
        for _ in range(18):
            s.add(f'<rect x="{s.r.uniform(0, SW):.1f}" y="{s.r.uniform(758, 826):.1f}" width="{s.r.uniform(20, 70):.1f}" height="2" '
                  f'rx="1" fill="#8f9ad8" opacity="{s.r.uniform(0.15, 0.35):.2f}"/>')
        # The lower quay in front: a stone edge, an iron railing, lamp posts and a bench.
        s.add(f'<rect x="0" y="832" width="{SW}" height="{SH - 832}" fill="#22234a"/>')
        s.add(f'<rect x="0" y="830" width="{SW}" height="8" fill="#59577e"/>')
        s.add(f'<rect x="0" y="828" width="{SW}" height="3" fill="#7d7aa2"/>')
        for x in range(0, SW, 46):
            s.add(f'<rect x="{x}" y="846" width="44" height="2" fill="#2e3058"/>')
        railing(s, 0, SW, 810, 830)
        bench(s, 300, 856, 92, "#4a3a3a", "#141528")
        bench(s, 760, 856, 92, "#4a3a3a", "#141528")
        s.add(f'<rect x="0" y="850" width="{SW}" height="10" fill="#1a1b3c"/>')


# ======================= London: Big Ben, Parliament and the London Eye at dusk =======================
STONE_LIT, STONE_SHADE, STONE_LINE = "#d9b26e", "#b08850", "#80623a"
SLATE = "#4a3f5e"


def _pinnacle(s, x, y, h, w=4, color=STONE_LIT):
    s.add(f'<path d="M{x - w},{y} L{x - w * 0.6},{y - h * 0.55} L{x},{y - h} L{x + w * 0.6},{y - h * 0.55} L{x + w},{y}Z" fill="{color}"/>')


def _gothic_window(s, x, y, w, h, on, win="#fff0b8", off="#5e4c60", rec=0.0):
    s.add(f'<path d="M{x - w / 2:.1f},{y + h:.1f} V{y + w * 0.5:.1f} Q{x:.1f},{y - w * 0.3:.1f} {x + w / 2:.1f},{y + w * 0.5:.1f} '
          f'V{y + h:.1f}Z" fill="{win if on else off}" opacity="{s.r.uniform(0.75, 1) if on else 1:.2f}"/>')
    if on and s.r.random() < rec:
        anchor(s, "window", x, y + h / 2, w)


def parliament(s, x0, x1, base, roof_y, rec=0.12):
    """The Houses of Parliament's river front: floodlit honey stone, bays of tall pointed windows between buttresses
    topped with pinnacles, and the slate roofs behind with their gilded cresting."""
    r = s.r
    # Roofs behind.
    x = x0
    while x < x1 - 50:
        w = min(r.uniform(70, 120), x1 - x)
        h = r.uniform(26, 40)
        s.add(f'<path d="M{x:.1f},{roof_y + 2} L{x + w * 0.2:.1f},{roof_y - h:.1f} H{x + w * 0.8:.1f} L{x + w:.1f},{roof_y + 2}Z" fill="{SLATE}"/>')
        s.add(f'<path d="M{x + w * 0.2:.1f},{roof_y - h:.1f} H{x + w * 0.8:.1f}" stroke="{STONE_LIT}" stroke-width="2" stroke-dasharray="3 3"/>')
        x += w + r.uniform(4, 16)
    s.add(f'<rect x="{x0}" y="{roof_y}" width="{x1 - x0}" height="{base - roof_y}" fill="{STONE_LIT}"/>')
    shade = s.lin([(0, STONE_SHADE, 0.0), (1, STONE_SHADE, 0.55)], 0, roof_y, 0, base, "userSpaceOnUse")
    s.add(f'<rect x="{x0}" y="{roof_y}" width="{x1 - x0}" height="{base - roof_y}" fill="{shade}"/>')
    bay = 26
    n = int((x1 - x0) / bay)
    for i in range(n + 1):
        bx = x0 + i * bay
        s.add(f'<rect x="{bx - 2.5:.1f}" y="{roof_y - 4}" width="5" height="{base - roof_y + 4}" fill="#ecc98a"/>')
        s.add(f'<rect x="{bx + 1:.1f}" y="{roof_y - 4}" width="2" height="{base - roof_y + 4}" fill="{STONE_LINE}" opacity="0.5"/>')
        _pinnacle(s, bx, roof_y - 4, 18, 3.4, "#ecc98a")
        if i < n:
            cx = bx + bay / 2
            for row, (wy, wh) in enumerate(((roof_y + 14, 34), (roof_y + 62, 34), (roof_y + 106, 16))):
                if wy + wh > base - 6:
                    continue
                _gothic_window(s, cx, wy, 11, wh, r.random() < 0.62, rec=rec)
    for y in (roof_y + 8, roof_y + 56, roof_y + 100):
        if y < base:
            s.add(f'<rect x="{x0}" y="{y}" width="{x1 - x0}" height="3" fill="{STONE_LINE}" opacity="0.55"/>')
    s.add(f'<rect x="{x0}" y="{roof_y - 2}" width="{x1 - x0}" height="4" fill="#f0d090"/>')


def victoria_tower(s, x, base, top, w=104):
    """Victoria Tower: a great square tower with tall window bays and corner turrets."""
    l, r_ = x - w / 2, x + w / 2
    s.add(f'<rect x="{l}" y="{top}" width="{w}" height="{base - top}" fill="{STONE_LIT}"/>')
    s.add(f'<rect x="{x + w * 0.1}" y="{top}" width="{w * 0.4}" height="{base - top}" fill="{STONE_SHADE}" opacity="0.5"/>')
    for k in range(4):
        bx = l + w * (k + 0.5) / 4
        for y in range(int(top + 40), int(base - 60), 64):
            _gothic_window(s, bx, y, 12, 44, s.r.random() < 0.5)
        s.add(f'<rect x="{bx - 1:.1f}" y="{top + 20}" width="2" height="{base - top - 40}" fill="{STONE_LINE}" opacity="0.45"/>')
    for tx in (l + 6, r_ - 6):
        s.add(f'<rect x="{tx - 9}" y="{top - 34}" width="18" height="{base - top + 34}" fill="#e6c27e"/>')
        s.add(f'<rect x="{tx + 2}" y="{top - 34}" width="7" height="{base - top + 34}" fill="{STONE_SHADE}" opacity="0.6"/>')
        s.add(f'<path d="M{tx - 10},{top - 34} L{tx},{top - 66} L{tx + 10},{top - 34}Z" fill="{SLATE}"/>')
        _pinnacle(s, tx, top - 62, 14, 2.5, "#e6c27e")
    for k in range(1, 7):
        _pinnacle(s, l + w * k / 7, top, 16, 3, "#e6c27e")
    s.add(f'<rect x="{l - 3}" y="{top - 3}" width="{w + 6}" height="6" fill="#f0d090"/>')
    s.add(f'<path d="M{x},{top - 4} V{top - 78}" stroke="#3a3048" stroke-width="2.5"/>')


def elizabeth_tower(s, x, base, top, face="#fff3c4"):
    """Elizabeth Tower (Big Ben): the tall shaft, the gilded clock stage with its lit face, the belfry, and the steep
    spire with its corner pinnacles. Records the clock face as 'clock' (x, y, r)."""
    ppm = (base - top) / 96.0

    def ys(m):
        return base - m * ppm

    hw = 6.0 * ppm
    y50, y62, y72, y92 = ys(50), ys(62), ys(72), ys(92)
    s.add(f'<rect x="{x - hw:.1f}" y="{y50:.1f}" width="{2 * hw:.1f}" height="{base - y50:.1f}" fill="{STONE_LIT}"/>')
    s.add(f'<rect x="{x + hw * 0.15:.1f}" y="{y50:.1f}" width="{hw * 0.85:.1f}" height="{base - y50:.1f}" fill="{STONE_SHADE}" opacity="0.55"/>')
    for k in range(-3, 4):
        px = x + k * hw / 3.5
        s.add(f'<rect x="{px - 0.8:.1f}" y="{y50 + 6:.1f}" width="1.6" height="{base - y50 - 10:.1f}" fill="{STONE_LINE}" opacity="0.5"/>')
    for y in range(int(y50 + 30), int(base - 40), 46):
        for dx in (-hw * 0.45, hw * 0.45):
            _gothic_window(s, x + dx, y, 7, 22, s.r.random() < 0.45)
        s.add(f'<rect x="{x - hw:.1f}" y="{y - 8}" width="{2 * hw:.1f}" height="2.5" fill="{STONE_LINE}" opacity="0.5"/>')
    s.add(f'<rect x="{x - hw - 2:.1f}" y="{y50 - 4:.1f}" width="{2 * hw + 4:.1f}" height="6" fill="#f0d090"/>')
    # Clock stage.
    cw = hw + 3
    s.add(f'<rect x="{x - cw:.1f}" y="{y62:.1f}" width="{2 * cw:.1f}" height="{y50 - y62:.1f}" fill="#e6c27e"/>')
    s.add(f'<rect x="{x + cw * 0.2:.1f}" y="{y62:.1f}" width="{cw * 0.8:.1f}" height="{y50 - y62:.1f}" fill="{STONE_SHADE}" opacity="0.45"/>')
    cy = (y50 + y62) / 2
    rr = 3.7 * ppm
    s.add(f'<rect x="{x - rr - 6:.1f}" y="{cy - rr - 6:.1f}" width="{2 * rr + 12:.1f}" height="{2 * rr + 12:.1f}" fill="#7a5a2e"/>')
    s.add(f'<rect x="{x - rr - 6:.1f}" y="{cy - rr - 6:.1f}" width="{2 * rr + 12:.1f}" height="{2 * rr + 12:.1f}" fill="none" stroke="#f2cf7a" stroke-width="2"/>')
    for (dx, dy) in ((-1, -1), (1, -1), (-1, 1), (1, 1)):
        s.add(f'<circle cx="{x + dx * (rr + 1):.1f}" cy="{cy + dy * (rr + 1):.1f}" r="3" fill="#f2cf7a"/>')
    anchor(s, "clock", x, cy, rr)
    s.glow(x, cy, rr * 3.2, face, 0.45)
    s.add(f'<circle cx="{x}" cy="{cy:.1f}" r="{rr:.1f}" fill="{face}"/>')
    s.add(f'<circle cx="{x}" cy="{cy:.1f}" r="{rr:.1f}" fill="none" stroke="#2a2010" stroke-width="2"/>')
    for i in range(12):
        a = 2 * math.pi * i / 12
        s.add(f'<path d="M{x + rr * 0.78 * math.cos(a):.1f},{cy + rr * 0.78 * math.sin(a):.1f} L{x + rr * 0.92 * math.cos(a):.1f},'
              f'{cy + rr * 0.92 * math.sin(a):.1f}" stroke="#2a2010" stroke-width="1.6"/>')
    s.add(f'<path d="M{x},{cy:.1f} L{x + rr * 0.32:.1f},{cy + rr * 0.45:.1f} M{x},{cy:.1f} L{x - rr * 0.1:.1f},{cy - rr * 0.72:.1f}" '
          f'stroke="#2a2010" stroke-width="2.2" stroke-linecap="round"/>')
    # Belfry.
    bw = hw + 1
    s.add(f'<rect x="{x - bw:.1f}" y="{y72:.1f}" width="{2 * bw:.1f}" height="{y62 - y72:.1f}" fill="{STONE_LIT}"/>')
    for dx in (-bw * 0.55, 0, bw * 0.55):
        s.add(f'<path d="M{x + dx - 6:.1f},{y62 - 4:.1f} V{y72 + 16:.1f} Q{x + dx:.1f},{y72 + 5:.1f} {x + dx + 6:.1f},{y72 + 16:.1f} V{y62 - 4:.1f}Z" '
              f'fill="#ffcf7a" opacity="0.6"/>')
    s.add(f'<rect x="{x - bw - 3:.1f}" y="{y72 - 3:.1f}" width="{2 * bw + 6:.1f}" height="5" fill="#f0d090"/>')
    # Spire with corner pinnacles and little gabled windows.
    s.add(f'<path d="M{x - bw - 1:.1f},{y72:.1f} L{x:.1f},{y92:.1f} L{x + bw + 1:.1f},{y72:.1f}Z" fill="{SLATE}"/>')
    s.add(f'<path d="M{x:.1f},{y92:.1f} L{x + bw + 1:.1f},{y72:.1f} L{x + bw * 0.3:.1f},{y72:.1f}Z" fill="#3a3150"/>')
    for k in range(1, 4):
        t = k / 4
        yy = y72 + (y92 - y72) * t
        ww = (bw + 1) * (1 - t)
        s.add(f'<path d="M{x - ww:.1f},{yy:.1f} H{x + ww:.1f}" stroke="#d9b26e" stroke-width="1.6"/>')
    for dx in (-bw * 0.45, bw * 0.45):
        s.add(f'<path d="M{x + dx - 5:.1f},{y72 - 1:.1f} V{y72 - 9:.1f} L{x + dx:.1f},{y72 - 16:.1f} L{x + dx + 5:.1f},{y72 - 9:.1f} V{y72 - 1:.1f}Z" fill="#e6c27e"/>')
    for dx in (-bw - 1, bw + 1):
        _pinnacle(s, x + dx, y72, 24, 3, "#e6c27e")
    s.add(f'<path d="M{x:.1f},{y92 + 2:.1f} V{top:.1f}" stroke="#d9b26e" stroke-width="2.6"/>')
    s.add(f'<circle cx="{x}" cy="{top + 6:.1f}" r="3" fill="#f2cf7a"/>')


def london_eye(s, cx, cy, r):
    """The London Eye with no spokes or capsules (the app turns those): the white rim with its truss, the hub, and
    the A-frame legs down to the bank. Records 'wheel' (cx, cy, r)."""
    anchor(s, "wheel", cx, cy, r)
    s.glow(cx, cy, r * 1.25, "#bcd8ff", 0.12)
    bank = cy + r * 1.24
    for lx, c in ((cx - r * 0.62, "#c9cfdc"), (cx - r * 0.3, "#e4e8f0")):
        s.add(f'<path d="M{lx:.1f},{bank:.1f} L{cx:.1f},{cy:.1f}" stroke="{c}" stroke-width="9" stroke-linecap="round"/>')
    s.add(f'<path d="M{cx - r * 0.5:.1f},{cy + r * 0.85:.1f} L{cx - r * 0.25:.1f},{cy + r * 0.88:.1f}" stroke="#c9cfdc" stroke-width="5"/>')
    s.add(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="#9fd2ff" stroke-width="10" opacity="0.18"/>')
    s.add(f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="none" stroke="#f2f5fa" stroke-width="3.2"/>')
    s.add(f'<circle cx="{cx}" cy="{cy}" r="{r - 11}" fill="none" stroke="#e6eaf2" stroke-width="2.2"/>')
    zig = []
    n = 72
    for i in range(n + 1):
        a = 2 * math.pi * i / n
        rr = r if i % 2 == 0 else r - 11
        zig.append(f"{cx + rr * math.cos(a):.1f},{cy + rr * math.sin(a):.1f}")
    s.add(f'<path d="M{" L".join(zig)}" fill="none" stroke="#dfe4ee" stroke-width="1.2" opacity="0.85"/>')
    s.add(f'<circle cx="{cx}" cy="{cy}" r="17" fill="#e8ecf4"/><circle cx="{cx}" cy="{cy}" r="7" fill="#9aa2b8"/>')
    s.add(f'<rect x="{cx - r * 0.7:.1f}" y="{bank - 4:.1f}" width="{r * 0.55:.1f}" height="10" fill="#8a7e96"/>')


def phone_box(s, x, base, h, red="#c8202a", glass="#ffe2a0"):
    """A red telephone box, lit inside."""
    w = h * 0.36
    l = x - w / 2
    s.add(f'<rect x="{l:.1f}" y="{base - h:.1f}" width="{w:.1f}" height="{h:.1f}" fill="{red}"/>')
    s.add(f'<path d="M{l - 2:.1f},{base - h:.1f} Q{x:.1f},{base - h - h * 0.12:.1f} {l + w + 2:.1f},{base - h:.1f}Z" fill="{red}"/>')
    s.add(f'<rect x="{l + 3:.1f}" y="{base - h * 0.92:.1f}" width="{w - 6:.1f}" height="{h * 0.07:.1f}" fill="#f4eee0"/>')
    gx0, gy0, gw, gh = l + 5, base - h * 0.8, w - 10, h * 0.62
    s.glow(x, gy0 + gh / 2, h * 0.6, glass, 0.3)
    s.add(f'<rect x="{gx0:.1f}" y="{gy0:.1f}" width="{gw:.1f}" height="{gh:.1f}" fill="{glass}" opacity="0.9"/>')
    bars = "".join(f"M{gx0 + gw * k / 3:.1f},{gy0:.1f} v{gh:.1f} " for k in (1, 2))
    bars += "".join(f"M{gx0:.1f},{gy0 + gh * k / 8:.1f} h{gw:.1f} " for k in range(1, 8))
    s.add(f'<path d="{bars}" stroke="{red}" stroke-width="1.6"/>')
    s.add(f'<rect x="{l + w * 0.1:.1f}" y="{base - 6:.1f}" width="{w * 0.8:.1f}" height="6" fill="#8a1218"/>')


def ornate_lamp(s, x, base, h, light="#ffe2a0", color="#1a2a2a"):
    """A Victorian embankment lamp: a fluted post on a heavy base, a crossbar and a round glass lantern."""
    anchor(s, "lamp", x, base - h * 0.92, h * 0.1)
    s.glow(x, base - h * 0.92, h * 0.36, light, 0.45)
    s.add(f'<path d="M{x - h * 0.06:.1f},{base} L{x - h * 0.045:.1f},{base - h * 0.16:.1f} H{x + h * 0.045:.1f} L{x + h * 0.06:.1f},{base}Z" fill="{color}"/>')
    s.add(f'<rect x="{x - h * 0.018:.1f}" y="{base - h * 0.84:.1f}" width="{h * 0.036:.1f}" height="{h * 0.7:.1f}" fill="{color}"/>')
    s.add(f'<ellipse cx="{x}" cy="{base - h * 0.4:.1f}" rx="{h * 0.03:.1f}" ry="{h * 0.05:.1f}" fill="{color}"/>')
    s.add(f'<path d="M{x - h * 0.08:.1f},{base - h * 0.82:.1f} H{x + h * 0.08:.1f}" stroke="{color}" stroke-width="3"/>')
    s.add(f'<circle cx="{x}" cy="{base - h * 0.92:.1f}" r="{h * 0.07:.1f}" fill="{light}"/>')
    s.add(f'<path d="M{x - h * 0.07:.1f},{base - h * 0.95:.1f} Q{x:.1f},{base - h * 1.06:.1f} {x + h * 0.07:.1f},{base - h * 0.95:.1f}Z" fill="{color}"/>')
    s.add(f'<path d="M{x:.1f},{base - h * 1.04:.1f} V{base - h * 1.1:.1f}" stroke="{color}" stroke-width="2.5"/>')


@theme("london", [(0, "#1b2350"), (0.45, "#2b2f68"), (0.75, "#4c3f80"), (0.92, "#8a5a8c"), (1, "#b46a8a")])
class London:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 46, 980, "#ffffff", 0.05)
        long_cloud(s, 130, 1185, 260, 34, "#9a5a8c", None, 0.5)
        long_cloud(s, 925, 1290, 240, 30, "#b8668a", None, 0.55)
        long_cloud(s, 90, 1340, 170, 22, "#c27088", None, 0.5)
        for _ in range(5):
            p = spot(s.r, 640, 1320, 30)
            if p:
                s.bird(p[0], p[1], s.r.uniform(1.0, 1.4), "#f2e6f0", 0.85)

    @staticmethod
    def scene(s):
        s.sky("#b46a8a", "#e08a88", "#f8b890")
        f = s.blur(30)
        s.add(f'<ellipse cx="840" cy="560" rx="420" ry="120" fill="#ffd9a6" opacity="0.5" filter="{f}"/>')
        long_cloud(s, 250, 370, 300, 30, "#f2a690", None, 0.45)
        long_cloud(s, 700, 250, 240, 24, "#e8949a", None, 0.4)
        # The city far off, in the haze.
        x = 380
        while x < SW:
            w = s.r.uniform(30, 70)
            h = s.r.uniform(30, 110)
            s.add(f'<rect x="{x:.1f}" y="{600 - h:.1f}" width="{w:.1f}" height="{h:.1f}" fill="#b0708e" opacity="0.55"/>')
            x += w + s.r.uniform(-6, 10)
        s.add('<path d="M640,600 L652,420 L664,600Z" fill="#a8688a" opacity="0.6"/>')
        london_eye(s, 800, 330, 220)
        # Parliament along the river, Victoria Tower at the far end, Big Ben at this end.
        parliament(s, 60, 520, 604, 470, rec=0.7)
        s.add('<path d="M290,470 V420 L300,404 L310,420 V470Z" fill="#e6c27e"/>')
        s.add('<path d="M288,420 L300,330 L312,420Z" fill="#d9b26e"/><path d="M300,330 L312,420 L302,420Z" fill="#b08850"/>')
        for dx in (-10, 10):
            _pinnacle(s, 300 + dx, 420, 18, 2.5, "#e6c27e")
        victoria_tower(s, 62, 604, 214)
        elizabeth_tower(s, 470, 604, 100)
        # The Thames.
        anchor(s, "water", 0, 612, 0)
        anchor(s, "lane", 0, 714, 1.0)
        river = s.lin([(0, "#d08ca0", 1), (0.3, "#8a6292", 1), (1, "#3b3567", 1)], 0, 604, 0, 790, "userSpaceOnUse")
        s.add(f'<rect x="0" y="604" width="{SW}" height="{SH - 604}" fill="{river}"/>')
        s.add(f'<rect x="0" y="600" width="520" height="12" fill="#8a6a50"/>')
        s.add(f'<rect x="0" y="598" width="520" height="3" fill="#c9a46a"/>')
        for _ in range(60):
            x = s.r.uniform(0, 520)
            y = s.r.uniform(620, 770)
            w = s.r.uniform(10, 36)
            s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="2.6" rx="1.3" fill="#ffd98a" opacity="{s.r.uniform(0.25, 0.6):.2f}"/>')
        for k in range(8):
            y = 624 + k * 14
            w = 26 - k * 2.5
            s.add(f'<rect x="{470 - w / 2:.1f}" y="{y}" width="{w:.1f}" height="3" rx="1.5" fill="#fff3c4" opacity="{0.7 - k * 0.07:.2f}"/>')
        for _ in range(30):
            x = s.r.uniform(600, 1032)
            y = s.r.uniform(630, 770)
            s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{s.r.uniform(16, 50):.1f}" height="2.2" rx="1.1" fill="#ffe0d0" opacity="{s.r.uniform(0.2, 0.45):.2f}"/>')
        # Westminster Bridge: green iron arches on stone piers, lamps along it.
        piers = [496, 636, 776, 916, 1056]
        for a_, b_ in zip(piers, piers[1:]):
            l, r_ = a_ + 13, b_ - 13
            s.add(f'<path d="M{l},614 V600 Q{(l + r_) / 2},572 {r_},600 V614" fill="#2a2a50" opacity="0.55"/>')
            s.add(f'<path d="M{l},602 Q{(l + r_) / 2},574 {r_},602" stroke="#3f7a56" stroke-width="5" fill="none"/>')
            for k in range(1, 10):
                t = k / 10
                mx = l + (r_ - l) * t
                my = (1 - t) ** 2 * 602 + 2 * (1 - t) * t * 574 + t * t * 602
                s.add(f'<path d="M{mx:.1f},{my:.1f} V576" stroke="#3f7a56" stroke-width="2"/>')
        s.add(f'<rect x="490" y="556" width="{SW - 490}" height="20" fill="#3f7a56"/>')
        s.add(f'<rect x="490" y="563" width="{SW - 490}" height="2.5" fill="#d9c47a"/>')
        s.add(f'<rect x="490" y="552" width="{SW - 490}" height="5" fill="#2c5a40"/>')
        for px in piers:
            s.add(f'<rect x="{px - 13}" y="574" width="26" height="40" fill="#a8949c"/>')
            s.add(f'<path d="M{px - 13},614 L{px},604 L{px + 13},614Z" fill="#8a7a86"/>')
            s.add(f'<rect x="{px - 15}" y="572" width="30" height="5" fill="#c9b4b8"/>')
        for x in range(560, SW, 120):
            ornate_lamp(s, x, 553, 44, "#fff0c0", "#203a2c")
        anchor(s, "road", 505, 552, 1.0)
        anchor(s, "road", SW, 552, 1.0)
        # The Embankment in front: the river wall, lamps, a phone box and a bench.
        s.add(f'<rect x="0" y="780" width="{SW}" height="{SH - 780}" fill="#3a3150"/>')
        s.add(f'<rect x="0" y="776" width="{SW}" height="22" fill="#6a5a76"/>')
        s.add(f'<rect x="0" y="774" width="{SW}" height="4" fill="#9a88a6"/>')
        for x in range(10, SW, 34):
            s.add(f'<rect x="{x}" y="782" width="16" height="12" rx="3" fill="#4e4260"/>')
        for x in range(0, SW, 52):
            s.add(f'<rect x="{x}" y="822" width="50" height="2" fill="#2e2742"/>')
        bench(s, 760, 852, 96, "#5a3a2a", "#1a2a2a")
        phone_box(s, 300, 856, 112)
        for x in (90, 560, 960):
            ornate_lamp(s, x, 856, 104, "#ffe6b0", "#1a2a2a")
        s.add(f'<rect x="0" y="852" width="{SW}" height="8" fill="#2a2240"/>')


# ======================= Las Vegas: the Strip at night =======================
PINK, CYAN, GOLD, PURPLE = "#ff4fa3", "#3ae7ff", "#ffcf4a", "#b56bff"


def neon(s, d, color, width=3.0, core="#ffffff", glow=0.7):
    """A neon tube along a path: a soft glow, the colored tube and a bright core."""
    f = s.blur(width * 1.6)
    s.add(f'<path d="{d}" stroke="{color}" stroke-width="{width * 3.2:.1f}" fill="none" opacity="{glow}" filter="{f}" '
          f'stroke-linecap="round" stroke-linejoin="round"/>')
    s.add(f'<path d="{d}" stroke="{color}" stroke-width="{width:.1f}" fill="none" stroke-linecap="round" stroke-linejoin="round"/>')
    s.add(f'<path d="{d}" stroke="{core}" stroke-width="{width * 0.35:.1f}" fill="none" opacity="0.8" stroke-linecap="round" '
          f'stroke-linejoin="round"/>')


def neon_text(s, x, y, text, size, color, rec=0, spacing=0.0):
    """Glowing neon letters; [rec] points along them are recorded as 'neon' (with their color)."""
    f = s.blur(size * 0.12)
    common = f'x="{x}" y="{y}" font-family="Inter" font-weight="700" font-size="{size}" text-anchor="middle" letter-spacing="{spacing}"'
    s.add(f'<text {common} fill="none" stroke="{color}" stroke-width="{size * 0.16:.1f}" opacity="0.75" filter="{f}">{text}</text>')
    s.add(f'<text {common} fill="none" stroke="{color}" stroke-width="{size * 0.07:.1f}">{text}</text>')
    s.add(f'<text {common} fill="none" stroke="#ffffff" stroke-width="{size * 0.025:.1f}" opacity="0.85">{text}</text>')
    w = size * 0.62 * len(text) + spacing * (len(text) - 1)
    for i in range(rec):
        t = (i + 0.5) / rec
        anchor(s, "neon", x - w / 2 + w * t, y - size * 0.36, rgb(color))


def windows(s, x0, x1, y0, y1, dx, dy, ww, wh, lit, p, a=(0.5, 1.0)):
    """A grid of small windows, some lit."""
    out = []
    y = y0
    while y < y1:
        x = x0
        while x < x1:
            if s.r.random() < p:
                out.append(f'<rect x="{x:.1f}" y="{y:.1f}" width="{ww}" height="{wh}" fill="{lit}" opacity="{s.r.uniform(*a):.2f}"/>')
            x += dx
        y += dy
    s.add("".join(out))


def vegas_sign(s, cx, cy, w, h):
    """A diamond marquee sign on two posts: a ring of bulbs (recorded as 'bulb'), VEGAS in big red letters, a star
    on top."""
    pole_base = cy + h * 1.55
    for dx in (-w * 0.12, w * 0.12):
        s.add(f'<rect x="{cx + dx - 5:.1f}" y="{cy:.1f}" width="10" height="{pole_base - cy:.1f}" fill="#c9c2d6"/>')
        s.add(f'<rect x="{cx + dx + 1:.1f}" y="{cy:.1f}" width="4" height="{pole_base - cy:.1f}" fill="#8a82a0"/>')
    pts = [(cx - w / 2, cy), (cx, cy - h / 2), (cx + w / 2, cy), (cx, cy + h / 2)]
    d = "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in pts) + "Z"
    s.glow(cx, cy, w * 0.75, "#fff1c4", 0.25)
    s.add(f'<path d="{d}" fill="#f6f1e6" stroke="#d62839" stroke-width="7" stroke-linejoin="round"/>')
    inner = [(cx - w / 2 + 22, cy), (cx, cy - h / 2 + 12), (cx + w / 2 - 22, cy), (cx, cy + h / 2 - 12)]
    s.add(f'<path d="M{_pts(inner)}Z" fill="none" stroke="#2a5ad6" stroke-width="2.5" stroke-linejoin="round"/>')
    s.add(f'<text x="{cx}" y="{cy - h * 0.12:.1f}" font-family="Inter" font-weight="700" font-size="{h * 0.17:.1f}" fill="#2a5ad6" '
          f'text-anchor="middle" letter-spacing="3">LAS</text>')
    s.add(f'<text x="{cx}" y="{cy + h * 0.2:.1f}" font-family="Inter" font-weight="700" font-size="{h * 0.36:.1f}" fill="#d62839" '
          f'text-anchor="middle" letter-spacing="1">VEGAS</text>')
    n = 28
    per = []
    for (ax, ay), (bx, by) in zip(pts, pts[1:] + pts[:1]):
        per.append(((ax, ay), (bx, by), math.hypot(bx - ax, by - ay)))
    total = sum(p[2] for p in per)
    for i in range(n):
        dist = total * i / n
        for (ax, ay), (bx, by), ln in per:
            if dist <= ln:
                t = dist / ln
                x, y = ax + (bx - ax) * t, ay + (by - ay) * t
                break
            dist -= ln
        c = GOLD if i % 2 == 0 else "#fff6d6"
        anchor(s, "bulb", x, y, rgb(c))
        s.glow(x, y, 12, c, 0.6)
        s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4" fill="{c}"/>')
    sy = cy - h / 2 - 16
    star = []
    for k in range(16):
        a = -math.pi / 2 + k * math.pi / 8
        rr = 17 if k % 2 == 0 else 7
        star.append(f"{cx + rr * math.cos(a):.1f},{sy + rr * math.sin(a):.1f}")
    s.glow(cx, sy, 34, "#ff6b6b", 0.5)
    s.add(f'<polygon points="{" ".join(star)}" fill="#e63946"/>')


@theme("vegas", [(0, "#0a0620"), (0.5, "#120a30"), (0.85, "#221046"), (1, "#341462")])
class Vegas:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 70, BAND_Y - 120, "#ffffff", 0.05)

    @staticmethod
    def scene(s):
        s.sky("#341462", "#4a1a78", "#6a2a8a")
        f = s.blur(40)
        s.add(f'<ellipse cx="516" cy="560" rx="620" ry="170" fill="#ff4fa3" opacity="0.18" filter="{f}"/>')
        s.stars(16, 200)
        s.skyline(600, "#2a1650", "#ffcf7a", 0.2, (120, 330))
        # The needle tower with its pod.
        nx = 80
        s.add(f'<path d="M{nx - 26},600 L{nx - 9},170 H{nx + 9} L{nx + 26},600Z" fill="#cfc6e6"/>')
        s.add(f'<path d="M{nx},600 V170 H{nx + 9} L{nx + 26},600Z" fill="#9a90bc"/>')
        s.add(f'<path d="M{nx - 40},600 L{nx - 14},470 M{nx + 40},600 L{nx + 14},470" stroke="#b9b0d6" stroke-width="9"/>')
        for y in range(200, 590, 34):
            s.add(f'<rect x="{nx - 6}" y="{y}" width="3" height="10" fill="#fff3c4" opacity="0.7"/>')
        s.add(f'<ellipse cx="{nx}" cy="150" rx="46" ry="9" fill="#3a2a5a"/>')
        s.add(f'<rect x="{nx - 40}" y="118" width="80" height="30" rx="6" fill="#7ff3ff"/>')
        s.add(''.join(f'<rect x="{nx - 40 + k * 10}" y="118" width="2.5" height="30" fill="#2a5a7a"/>' for k in range(1, 8)))
        s.add(f'<ellipse cx="{nx}" cy="118" rx="42" ry="8" fill="#e6e0f4"/><ellipse cx="{nx}" cy="104" rx="26" ry="7" fill="#d6cfe8"/>')
        neon(s, f"M{nx - 46},150 Q{nx},162 {nx + 46},150", PINK, 3)
        s.add(f'<path d="M{nx},100 V52" stroke="#e6e0f4" stroke-width="4"/>')
        anchor(s, "antenna", nx, 50, 0)
        s.add(f'<circle cx="{nx}" cy="50" r="4" fill="#ff4d4d"/>')
        anchor(s, "neon", nx, 154, rgb(PINK))
        # The curved bronze tower.
        cur = s.lin([(0, "#d39a52", 1), (1, "#5a3a20", 1)], 0, 160, 0, 600, "userSpaceOnUse")
        s.add(f'<path d="M150,600 V196 Q220,140 292,176 V600Z" fill="{cur}"/>')
        s.add(f'<path d="M236,600 V160 Q266,162 292,176 V600Z" fill="#3a2410" opacity="0.35"/>')
        s.add("".join(f'<path d="M152,{y} H290" stroke="#3a2210" stroke-width="1.4" opacity="0.45"/>' for y in range(205, 600, 9)))
        windows(s, 156, 286, 210, 590, 10, 9, 5, 4, "#ffe2a0", 0.22)
        neon(s, "M150,196 Q220,140 292,176", GOLD, 3.2)
        for x, y in ((165, 186), (220, 166), (276, 170)):
            anchor(s, "neon", x, y, rgb(GOLD))
        anchor(s, "search", 220, 162, 0)
        # The white tower with its pink crown.
        s.add('<rect x="300" y="262" width="18" height="338" fill="#cfc9e2"/><rect x="432" y="262" width="18" height="338" fill="#bdb6d4"/>')
        s.add('<rect x="315" y="160" width="120" height="440" fill="#ebe6f6"/><rect x="380" y="160" width="55" height="440" fill="#d2cbe6"/>')
        windows(s, 320, 430, 172, 590, 11, 11, 6, 6, "#8fd3ff", 0.45, (0.6, 1.0))
        s.add('<path d="M330,160 V142 H345 V126 H405 V142 H420 V160Z" fill="#ebe6f6"/>')
        neon(s, "M315,160 H330 V142 H345 V126 H405 V142 H420 V160 H435", PINK, 3)
        s.add('<path d="M375,126 V92" stroke="#ebe6f6" stroke-width="4"/>')
        anchor(s, "antenna", 375, 90, 0)
        s.add('<circle cx="375" cy="90" r="4" fill="#ff4d4d"/>')
        for x, y in ((322, 160), (345, 134), (390, 126), (428, 150)):
            anchor(s, "neon", x, y, rgb(PINK))
        # The blue glass twin towers.
        for (x0, x1, top) in ((458, 530, 206), (544, 616, 236)):
            s.add(f'<rect x="{x0}" y="{top}" width="{x1 - x0}" height="{600 - top}" fill="#1d3a6a"/>')
            s.add(f'<rect x="{(x0 + x1) / 2}" y="{top}" width="{(x1 - x0) / 2}" height="{600 - top}" fill="#142a52"/>')
            grid = "".join(f"M{x},{top} V600 " for x in range(x0 + 8, x1, 9)) + "".join(f"M{x0},{y} H{x1} " for y in range(top + 10, 600, 11))
            s.add(f'<path d="{grid}" stroke="{CYAN}" stroke-width="0.9" opacity="0.32"/>')
            windows(s, x0 + 4, x1 - 6, top + 14, 590, 9, 11, 5, 6, "#bff6ff", 0.18)
            neon(s, f"M{x0},{top} H{x1}", CYAN, 2.6)
            anchor(s, "neon", (x0 + x1) / 2, top, rgb(CYAN))
        anchor(s, "search", 494, 204, 0)
        s.add('<path d="M600,236 V214" stroke="#9ab0d0" stroke-width="3"/><circle cx="600" cy="212" r="4" fill="#ff4d4d"/>')
        anchor(s, "antenna", 600, 212, 0)
        # The castle with its colorful turrets and a CASINO sign.
        s.add('<rect x="626" y="470" width="128" height="130" fill="#efe9f2"/><rect x="690" y="470" width="64" height="130" fill="#d6cfe0"/>')
        s.add("".join(f'<rect x="{x}" y="460" width="9" height="12" fill="#efe9f2"/>' for x in range(628, 752, 16)))
        for (tx, top, w, c) in ((636, 400, 26, "#e63946"), (668, 370, 30, "#3a86ff"), (712, 384, 28, GOLD), (744, 410, 22, "#e63946")):
            s.add(f'<rect x="{tx - w / 2}" y="{top}" width="{w}" height="{600 - top}" fill="#f4eff6"/>')
            s.add(f'<rect x="{tx}" y="{top}" width="{w / 2}" height="{600 - top}" fill="#d6cfe0"/>')
            s.add(f'<path d="M{tx - w / 2 - 4},{top} L{tx},{top - w * 1.6} L{tx + w / 2 + 4},{top}Z" fill="{c}"/>')
            s.add(f'<rect x="{tx - 3}" y="{top + 18}" width="6" height="10" rx="3" fill="#ffcf7a"/>')
        s.add('<rect x="630" y="520" width="120" height="38" rx="6" fill="#241038"/>')
        s.add('<rect x="630" y="520" width="120" height="38" rx="6" fill="none" stroke="#5a2a7a" stroke-width="2"/>')
        neon_text(s, 690, 548, "CASINO", 26, PINK, rec=5, spacing=2)
        anchor(s, "search", 668, 312, 0)
        # The pyramid, off to one side, with a faint beam up from its tip.
        px, py = 886, 330
        beam = s.lin([(0, "#fff6d6", 0.0), (0.7, "#fff6d6", 0.16), (1, "#fff6d6", 0.4)], 0, 0, 0, py, "userSpaceOnUse")
        s.add(f'<rect x="{px - 4}" y="0" width="8" height="{py}" fill="{beam}"/>')
        anchor(s, "beam", px, py, 0)
        s.add(f'<path d="M760,600 L{px},{py} L862,600Z" fill="#1d1836"/>')
        s.add(f'<path d="M862,600 L{px},{py} L1012,600Z" fill="#0e0a20"/>')
        for k in range(1, 12):
            t = k / 12
            y = py + (600 - py) * t
            s.add(f'<path d="M{px - (px - 760) * t:.1f},{y:.1f} L{px - (px - 862) * t:.1f},{y:.1f} L{px + (1012 - px) * t:.1f},{y:.1f}" '
                  f'stroke="#3a3260" stroke-width="1" fill="none" opacity="0.7"/>')
        for d in (f"M760,600 L{px},{py}", f"M862,600 L{px},{py}", f"M1012,600 L{px},{py}"):
            s.add(f'<path d="{d}" stroke="#fff3c4" stroke-width="2.2" opacity="0.9"/>')
        s.glow(px, py, 40, "#fff6d6", 0.8)
        s.add(f'<circle cx="{px}" cy="{py}" r="4" fill="#ffffff"/>')
        # The far shore: a promenade of lights, palms lit from below.
        s.add(f'<rect x="0" y="600" width="{SW}" height="34" fill="#1a1032"/>')
        s.add("".join(f'<circle cx="{x}" cy="610" r="2" fill="#fff3c4" opacity="0.8"/>' for x in range(8, SW, 22)))
        for x, h, lean in ((296, 158, 0.5), (566, 150, -0.5), (942, 154, 0.5)):
            s.glow(x, 628, 52, CYAN, 0.4)
            palm(s, x, 632, h, lean, "#6a4a5a", "#2a7a66", "#3a9a80")
        # The lake, reflecting the neon, with the fountains' nozzles in a row.
        anchor(s, "water", 0, 634, 0)
        lake = s.lin([(0, "#160f3a", 1), (1, "#0a0722", 1)], 0, 634, 0, 772, "userSpaceOnUse")
        s.add(f'<rect x="0" y="634" width="{SW}" height="138" fill="{lake}"/>')
        for (x, c) in ((80, PINK), (220, GOLD), (375, PINK), (494, CYAN), (580, CYAN), (690, PINK), (886, "#fff3c4"), (820, "#fff3c4"), (960, "#fff3c4")):
            for k in range(7):
                y = 642 + k * 17
                w = s.r.uniform(14, 34) * (1 - k * 0.08)
                s.add(f'<rect x="{x - w / 2 + s.r.uniform(-6, 6):.1f}" y="{y:.1f}" width="{w:.1f}" height="3" rx="1.5" fill="{c}" '
                      f'opacity="{0.6 - k * 0.06:.2f}"/>')
        for _ in range(30):
            s.add(f'<rect x="{s.r.uniform(0, SW):.1f}" y="{s.r.uniform(640, 766):.1f}" width="{s.r.uniform(10, 40):.1f}" height="2" '
                  f'rx="1" fill="#c9b8ff" opacity="{s.r.uniform(0.1, 0.3):.2f}"/>')
        for i in range(9):
            x = 340 + i * 67.5
            h = 140 + 80 * (1 - abs(i - 4) / 4)
            anchor(s, "fountain", x, 712, h)
            s.add(f'<ellipse cx="{x}" cy="712" rx="16" ry="4" fill="none" stroke="#ffffff" stroke-width="1.5" opacity="0.35"/>')
            s.add(f'<ellipse cx="{x}" cy="712" rx="5" ry="2" fill="#ffffff" opacity="0.5"/>')
        # The balustrade along the lake, the sidewalk and the Strip.
        s.add(f'<rect x="0" y="772" width="{SW}" height="22" fill="#a89cc0"/>')
        s.add(f'<rect x="0" y="770" width="{SW}" height="6" fill="#d8cfe6"/>')
        s.add("".join(f'<rect x="{x}" y="778" width="6" height="12" rx="3" fill="#7a6e94"/>' for x in range(4, SW, 12)))
        s.add(f'<rect x="0" y="794" width="{SW}" height="10" fill="#2a2044"/>')
        s.add(f'<rect x="0" y="804" width="{SW}" height="{SH - 804}" fill="#141226"/>')
        s.add("".join(f'<rect x="{x}" y="831" width="34" height="3" fill="#e8e2f0" opacity="0.6"/>' for x in range(10, SW, 70)))
        s.add(f'<rect x="0" y="804" width="{SW}" height="3" fill="#3a3058"/>')
        anchor(s, "road", 0, 836, 1.0)
        anchor(s, "road", SW, 836, 1.0)
        vegas_sign(s, 170, 652, 250, 128)


# ======================= Rainy Night: a cozy street in the rain =======================
def storm_cloud(s, x, y, w, h, dark="#161c29", mid="#1f2636", rim="#2f3a50", a=0.96):
    """A heavy rain cloud: soft lumps layered over a flat dark base, with a faint light rim along the top."""
    f = s.blur(max(6, h * 0.07))
    r = s.r
    lumps = []
    for i in range(8):
        t = i / 7
        lr = h * (0.3 + 0.36 * math.sin(math.pi * (0.1 + 0.8 * t))) * r.uniform(0.85, 1.15)
        lumps.append((x - w / 2 + w * t + r.uniform(-w * 0.03, w * 0.03), y - lr * 0.45, lr))
    rim_ = "".join(f'<circle cx="{lx:.1f}" cy="{ly - 4:.1f}" r="{lr:.1f}" fill="{rim}"/>' for lx, ly, lr in lumps)
    body = "".join(f'<circle cx="{lx:.1f}" cy="{ly:.1f}" r="{lr * 0.98:.1f}" fill="{mid}"/>' for lx, ly, lr in lumps)
    under = "".join(f'<circle cx="{lx:.1f}" cy="{ly + lr * 0.35:.1f}" r="{lr * 0.75:.1f}" fill="{dark}"/>' for lx, ly, lr in lumps[1:-1])
    s.add(f'<g filter="{f}" opacity="{a}">{rim_}{body}{under}'
          f'<rect x="{x - w / 2:.1f}" y="{y - h * 0.12:.1f}" width="{w:.1f}" height="{h * 0.3:.1f}" rx="{h * 0.15:.1f}" fill="{dark}"/></g>')


def bricks(s, x0, x1, y0, y1, color="#000000", a=0.16, row=7, brick=18):
    d = []
    y, k = y0 + row, 0
    while y < y1:
        d.append(f"M{x0:.1f},{y:.1f} H{x1:.1f}")
        off = 0 if k % 2 == 0 else brick / 2
        x = x0 + off
        while x < x1:
            d.append(f"M{x:.1f},{y:.1f} v-{row}")
            x += brick
        y += row
        k += 1
    s.add(f'<path d="{" ".join(d)}" stroke="{color}" stroke-width="0.9" opacity="{a}" fill="none"/>')


def cat(s, x, base, k, color="#16121a"):
    """A cat sitting, seen from behind, its tail curled round."""
    s.add(f'<g transform="translate({x:.1f},{base:.1f}) scale({k:.2f})" fill="{color}">'
          f'<ellipse cx="0" cy="-11" rx="9" ry="11"/><circle cx="0" cy="-25" r="6.5"/>'
          f'<path d="M-6,-28 L-5,-36 L-1,-30Z M6,-28 L5,-36 L1,-30Z"/>'
          f'<path d="M7,-3 Q18,-2 15,-14" stroke="{color}" stroke-width="3.2" fill="none" stroke-linecap="round"/></g>')


def apt_window(s, x, y, w, h, on, frame="#c9bfae", lit="#ffcf7a", off="#1d2331", curtain=None, has_cat=False):
    """A sash window: lit warm (maybe with curtains, maybe a cat on the sill) or dark with a glint of the street."""
    s.add(f'<rect x="{x - 3:.1f}" y="{y - 3:.1f}" width="{w + 6:.1f}" height="{h + 6:.1f}" fill="{frame}" opacity="0.85"/>')
    if on:
        s.glow(x + w / 2, y + h / 2, w * 1.5, lit, 0.28)
        g = s.lin([(0, "#ffe2a0", 1), (1, lit, 1)], 0, y, 0, y + h, "userSpaceOnUse")
        s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" fill="{g}"/>')
        if curtain:
            s.add(f'<path d="M{x:.1f},{y:.1f} H{x + w * 0.3:.1f} Q{x + w * 0.12:.1f},{y + h * 0.5:.1f} {x + w * 0.22:.1f},{y + h:.1f} H{x:.1f}Z" fill="{curtain}"/>')
            s.add(f'<path d="M{x + w:.1f},{y:.1f} H{x + w * 0.7:.1f} Q{x + w * 0.88:.1f},{y + h * 0.5:.1f} {x + w * 0.78:.1f},{y + h:.1f} H{x + w:.1f}Z" fill="{curtain}"/>')
        if has_cat:
            cat(s, x + w * 0.42, y + h - 1, w / 34)
        anchor(s, "window", x + w / 2, y + h / 2, w)
    else:
        s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{w:.1f}" height="{h:.1f}" fill="{off}"/>')
        s.add(f'<path d="M{x + w * 0.15:.1f},{y + h * 0.85:.1f} L{x + w * 0.55:.1f},{y + h * 0.15:.1f}" stroke="#56607a" stroke-width="2" opacity="0.5"/>')
    s.add(f'<path d="M{x:.1f},{y + h * 0.5:.1f} H{x + w:.1f} M{x + w / 2:.1f},{y:.1f} V{y + h:.1f}" stroke="{frame}" stroke-width="2"/>')
    s.add(f'<rect x="{x - 5:.1f}" y="{y + h + 2:.1f}" width="{w + 10:.1f}" height="4" fill="{frame}"/>')


def street_lamp(s, x, base, h, light="#ffd98a", color="#10141d"):
    """A black street lamp with a glass lantern, its light haloed in the rain."""
    ly = base - h * 0.9
    anchor(s, "lamp", x, ly, h * 0.1)
    s.glow(x, ly, h * 0.55, light, 0.33)
    s.glow(x, ly, h * 0.2, "#fff3d0", 0.5)
    s.add(f'<path d="M{x - 10:.1f},{base} L{x - 6:.1f},{base - 26:.1f} H{x + 6:.1f} L{x + 10:.1f},{base}Z" fill="{color}"/>')
    s.add(f'<rect x="{x - 3:.1f}" y="{base - h * 0.84:.1f}" width="6" height="{h * 0.84 - 20:.1f}" fill="{color}"/>')
    s.add(f'<path d="M{x - 9:.1f},{base - h * 0.84:.1f} H{x + 9:.1f}" stroke="{color}" stroke-width="4"/>')
    s.add(f'<path d="M{x - 11:.1f},{ly - 12:.1f} H{x + 11:.1f} L{x + 7:.1f},{ly + 14:.1f} H{x - 7:.1f}Z" fill="{light}"/>')
    s.add(f'<path d="M{x:.1f},{ly - 12:.1f} V{ly + 14:.1f}" stroke="{color}" stroke-width="1.5"/>')
    s.add(f'<path d="M{x - 14:.1f},{ly - 12:.1f} L{x:.1f},{ly - 24:.1f} L{x + 14:.1f},{ly - 12:.1f}Z" fill="{color}"/>')
    s.add(f'<circle cx="{x:.1f}" cy="{ly - 27:.1f}" r="3" fill="{color}"/>')
    s.add(f'<rect x="{x - 8:.1f}" y="{ly + 14:.1f}" width="16" height="4" fill="{color}"/>')


def bicycle(s, x, base, k, color="#1a2030"):
    r = 19 * k
    a, b = (x - 26 * k, base - r), (x + 26 * k, base - r)
    s.add(f'<circle cx="{a[0]:.1f}" cy="{a[1]:.1f}" r="{r:.1f}" fill="none" stroke="{color}" stroke-width="3"/>')
    s.add(f'<circle cx="{b[0]:.1f}" cy="{b[1]:.1f}" r="{r:.1f}" fill="none" stroke="{color}" stroke-width="3"/>')
    seat, bar, crank = (x - 8 * k, base - r - 30 * k), (x + 18 * k, base - r - 34 * k), (x - 2 * k, base - r)
    s.add(f'<path d="M{a[0]:.1f},{a[1]:.1f} L{crank[0]:.1f},{crank[1]:.1f} L{seat[0]:.1f},{seat[1]:.1f} Z M{crank[0]:.1f},{crank[1]:.1f} '
          f'L{bar[0] - 4 * k:.1f},{bar[1] + 6 * k:.1f} L{seat[0]:.1f},{seat[1] + 4 * k:.1f} M{bar[0]:.1f},{bar[1]:.1f} L{b[0]:.1f},{b[1]:.1f} '
          f'M{bar[0] - 6 * k:.1f},{bar[1] - 2 * k:.1f} h{10 * k:.1f}" stroke="{color}" stroke-width="3" fill="none" stroke-linejoin="round"/>')
    s.add(f'<rect x="{seat[0] - 7 * k:.1f}" y="{seat[1] - 3 * k:.1f}" width="{14 * k:.1f}" height="{4 * k:.1f}" rx="2" fill="{color}"/>')
    s.add(f'<circle cx="{a[0] - r * 0.2:.1f}" cy="{a[1] - r * 1.05:.1f}" r="2.5" fill="#ff4d4d"/>')


def awning(s, x0, x1, top, bottom, c1="#2e6b4f", c2="#e9e0c8", n=12):
    """A striped shop awning sloping out over the window, with a scalloped edge."""
    cid = s.id("aw")
    d = f"M{x0},{top} H{x1} L{x1 + 10},{bottom} H{x0 - 10}Z"
    s.defs.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath>')
    w = (x1 - x0 + 20) / n
    stripes = "".join(f'<rect x="{x0 - 10 + i * w:.1f}" y="{top}" width="{w + 0.5:.1f}" height="{bottom - top}" fill="{c1 if i % 2 else c2}"/>'
                      for i in range(n))
    s.add(f'<g clip-path="url(#{cid})">{stripes}<rect x="{x0 - 10}" y="{top}" width="{x1 - x0 + 20}" height="{bottom - top}" '
          f'fill="#000000" opacity="0.18"/></g>')
    sc = "".join(f'<path d="M{x0 - 10 + i * w:.1f},{bottom} q{w / 2:.1f},{w * 0.55:.1f} {w:.1f},0Z" fill="{c1 if i % 2 else c2}"/>' for i in range(n))
    s.add(sc)
    s.add(f'<rect x="{x0 - 10}" y="{bottom - 2}" width="{x1 - x0 + 20}" height="3" fill="#000000" opacity="0.2"/>')


@theme("rainy", [(0, "#0e1420"), (0.5, "#141b29"), (0.85, "#1d2535"), (1, "#263042")])
class Rainy:
    @staticmethod
    def deco(s):
        for (x, y, w, h) in ((70, 500, 460, 190), (975, 590, 470, 200), (10, 1020, 380, 160), (1020, 1130, 400, 170)):
            storm_cloud(s, x, y, w, h)
        for (x, y, w, h) in ((150, 420, 260, 100), (930, 500, 280, 110)):
            storm_cloud(s, x, y, w, h, "#1b2230", "#252d3e", "#36425c", 0.9)
        # The low clouds run across the top of the street scene, so they're drawn in front of it.
        with Front(s):
            storm_cloud(s, 120, 1330, 440, 130)
            storm_cloud(s, 920, 1318, 460, 120)
            storm_cloud(s, 180, 1452, 420, 90, "#20283a", "#2a3346", "#3a465e", 0.9)

    @staticmethod
    def scene(s):
        s.sky("#263042", "#2c364a", "#343e54")
        for _ in range(48):  # (the low clouds used to be drawn here; this keeps the rest of the street as it was)
            s.r.random()
        # Building A: old brick with a fire escape and a flower shop.
        s.add('<rect x="-10" y="140" width="212" height="570" fill="#5e3530"/>')
        bricks(s, -10, 202, 150, 560)
        s.add('<rect x="-12" y="130" width="216" height="14" fill="#7a4a40"/><rect x="-12" y="126" width="216" height="5" fill="#8f5c50"/>')
        for f in range(5):
            for c in range(3):
                apt_window(s, 18 + c * 62, 168 + f * 78, 34, 50, s.r.random() < 0.55,
                           curtain=s.r.choice([None, "#a84a3a", "#c98a4a", None]))
        for f in range(4):
            y = 226 + f * 78
            s.add(f'<rect x="76" y="{y}" width="96" height="4" fill="#12141c"/>')
            s.add(f'<path d="M76,{y} v-16 h96 v16 M84,{y - 16} v16 M100,{y - 16} v16 M116,{y - 16} v16 M132,{y - 16} v16 M148,{y - 16} v16 M164,{y - 16} v16" '
                  f'stroke="#12141c" stroke-width="2" fill="none"/>')
            if f < 3:
                s.add(f'<path d="M90,{y + 2} L160,{y + 76}" stroke="#12141c" stroke-width="5"/>')
                s.add(f'<path d="M90,{y + 2} L160,{y + 76}" stroke="#12141c" stroke-width="1.5" stroke-dasharray="2 6" transform="translate(0,-6)"/>')
        s.add('<rect x="-10" y="560" width="212" height="8" fill="#7a4a40"/>')
        s.glow(85, 640, 90, "#ffcf7a", 0.25)
        s.add('<rect x="14" y="584" width="134" height="104" fill="#f0b070"/>')
        for k in range(14):
            fx, fy = 26 + k * 9 + s.r.uniform(-3, 3), 660 + s.r.uniform(-14, 4)
            s.add(f'<circle cx="{fx:.1f}" cy="{fy:.1f}" r="{s.r.uniform(4, 7):.1f}" fill="{s.r.choice(["#e0457b", "#ffd166", "#ff8c42", "#c77dff", "#ffffff"])}"/>')
        s.add('<rect x="20" y="668" width="122" height="20" fill="#7a5a3a"/>')
        s.add('<path d="M14,636 H148 M81,584 V688" stroke="#3a2a24" stroke-width="3"/>')
        s.add('<rect x="156" y="600" width="38" height="106" fill="#2a1c1a"/><rect x="160" y="606" width="30" height="40" fill="#ffcf7a" opacity="0.7"/>')
        # Building B: painted blue-gray, a bookshop below.
        s.add('<rect x="202" y="214" width="232" height="496" fill="#3b5562"/>')
        s.add('<rect x="198" y="200" width="240" height="16" fill="#5a7682"/><rect x="194" y="196" width="248" height="6" fill="#6f8c98"/>')
        for x in range(206, 434, 16):
            s.add(f'<rect x="{x}" y="216" width="8" height="8" fill="#5a7682"/>')
        for f in range(4):
            for c in range(3):
                apt_window(s, 226 + c * 70, 246 + f * 76, 40, 52, s.r.random() < 0.6 or (f == 2 and c == 1), frame="#d8d2c4",
                           curtain=s.r.choice([None, "#6a8a5a", "#c9a46a"]), has_cat=(f == 2 and c == 1))
        s.add('<rect x="202" y="548" width="232" height="30" fill="#22402f"/>')
        s.add('<text x="300" y="571" font-family="Inter" font-weight="700" font-size="22" fill="#efe6cf" text-anchor="middle" letter-spacing="4">BOOKS</text>')
        s.glow(290, 640, 100, "#ffcf7a", 0.22)
        s.add('<rect x="214" y="586" width="156" height="104" fill="#e8b070"/>')
        for row, y in enumerate((598, 632, 666)):
            s.add(f'<rect x="216" y="{y + 22}" width="152" height="4" fill="#6a4426"/>')
            x = 220
            while x < 362:
                bw = s.r.uniform(5, 9)
                bh = s.r.uniform(15, 22)
                s.add(f'<rect x="{x:.1f}" y="{y + 22 - bh:.1f}" width="{bw:.1f}" height="{bh:.1f}" '
                      f'fill="{s.r.choice(["#8a3a3a", "#3a5a8a", "#c9a24a", "#4a7a4a", "#7a4a7a", "#d8cfb8"])}"/>')
                x += bw + 1
        s.add('<path d="M214,638 H370 M292,586 V690" stroke="#22402f" stroke-width="3"/>')
        s.add('<rect x="380" y="596" width="42" height="110" fill="#22402f"/><rect x="386" y="604" width="30" height="46" fill="#ffcf7a" opacity="0.6"/>')
        # Building C: cream paint, the café on the ground floor with its awning and neon sign.
        s.add('<rect x="434" y="168" width="330" height="542" fill="#7a6d55"/>')
        s.add('<rect x="434" y="168" width="330" height="542" fill="#000000" opacity="0.08"/>')
        s.add('<path d="M428,170 L599,120 L770,170Z" fill="#8a7c62"/><path d="M450,166 L599,128 L748,166Z" fill="#6a5e48"/>')
        s.add('<rect x="426" y="166" width="346" height="8" fill="#9a8c70"/>')
        for f in range(3):
            for c in range(4):
                apt_window(s, 462 + c * 78, 206 + f * 92, 42, 60, s.r.random() < 0.6 or (f == 1 and c == 3), frame="#e6dccb",
                           curtain=s.r.choice([None, "#b8604a", "#e0c080"]), has_cat=(f == 1 and c == 3))
        neon_text(s, 599, 506, "CAFÉ", 44, PINK, rec=8, spacing=4)
        awning(s, 452, 746, 520, 562)
        s.glow(580, 630, 190, "#ffb85a", 0.3)
        cafe = s.lin([(0, "#ffe0a0", 1), (1, "#f0a050", 1)], 0, 576, 0, 696, "userSpaceOnUse")
        s.add(f'<rect x="452" y="576" width="246" height="120" fill="{cafe}"/>')
        for lx in (500, 576, 652):
            s.add(f'<path d="M{lx},576 V600" stroke="#5a3a20" stroke-width="1.5"/>')
            s.glow(lx, 606, 26, "#fff3d0", 0.8)
            s.add(f'<path d="M{lx - 9},606 Q{lx},592 {lx + 9},606Z" fill="#5a3a20"/>')
        s.add('<rect x="452" y="662" width="246" height="6" fill="#8a5a30"/>')
        for (px, k) in ((520, 1.0), (628, 0.95)):
            s.add(f'<g fill="#9a5a2e" opacity="0.75" transform="translate({px},662) scale({k})">'
                  f'<circle cx="0" cy="-40" r="9"/><path d="M-12,0 V-22 Q0,-34 12,-22 V0Z"/></g>')
        s.add('<rect x="566" y="640" width="30" height="22" fill="#7a4a24" opacity="0.7"/><circle cx="574" cy="637" r="4" fill="#ffffff" opacity="0.8"/>')
        s.add('<path d="M452,576 H698 M534,576 V696 M616,576 V696" stroke="#3a2a1c" stroke-width="4" fill="none"/>')
        s.add('<rect x="706" y="582" width="46" height="124" fill="#3a2a1c"/><rect x="712" y="590" width="34" height="56" fill="#ffd58a" opacity="0.85"/>')
        neon_text(s, 729, 624, "OPEN", 12, "#ff5a5a", rec=2, spacing=1)
        # Building D: dark brick, a water tower on the roof, a laundromat below.
        s.add('<rect x="764" y="150" width="280" height="560" fill="#4a3332"/>')
        bricks(s, 764, 1044, 160, 560, a=0.18)
        s.add('<rect x="760" y="140" width="290" height="12" fill="#5e4442"/>')
        s.add('<path d="M880,140 V112 M940,140 V112 M872,112 H948" stroke="#20181a" stroke-width="4"/>')
        s.add('<rect x="876" y="64" width="68" height="48" fill="#5a3e30"/><path d="M870,64 L910,36 L950,64Z" fill="#3e2a22"/>')
        s.add(''.join(f'<path d="M876,{y} H944" stroke="#2a1c16" stroke-width="2"/>' for y in (76, 90, 104)))
        for f in range(5):
            for c in range(3):
                apt_window(s, 798 + c * 76, 178 + f * 76, 38, 48, s.r.random() < 0.5, frame="#b9ad9e",
                           curtain=s.r.choice([None, None, "#5a7a9a"]))
        s.add('<rect x="764" y="560" width="280" height="8" fill="#5e4442"/>')
        s.glow(900, 640, 120, "#cfe8ff", 0.25)
        s.add('<rect x="786" y="584" width="222" height="104" fill="#d6ecff"/>')
        for k in range(4):
            wx = 818 + k * 54
            s.add(f'<rect x="{wx - 22}" y="640" width="44" height="48" fill="#eef6ff" stroke="#9ab4cc" stroke-width="2"/>')
            s.add(f'<circle cx="{wx}" cy="664" r="14" fill="#7aa0c4"/><circle cx="{wx}" cy="664" r="9" fill="#a8c8e6"/>')
        s.add('<path d="M786,630 H1008" stroke="#9ab4cc" stroke-width="2"/>')
        neon_text(s, 897, 554, "LAUNDRY", 22, CYAN, rec=3, spacing=2)
        # The sidewalk, wet and shining, with lamps, a bench and a bicycle along the buildings.
        s.add(f'<rect x="0" y="706" width="{SW}" height="44" fill="#3a404f"/>')
        s.add("".join(f'<path d="M{x},706 L{x - 14},748" stroke="#2e3341" stroke-width="1.5"/>' for x in range(30, SW + 40, 64)))
        s.add(f'<rect x="0" y="726" width="{SW}" height="1.5" fill="#2e3341"/>')
        fb = s.blur(9)
        for (x, c, w) in ((80, "#ffcf7a", 120), (292, "#ffcf7a", 140), (575, "#ffb85a", 220), (897, "#cfe8ff", 200), (729, "#ffd58a", 40),
                          (190, "#ffd98a", 40), (432, "#ffd98a", 40), (1000, "#ffd98a", 40)):
            s.add(f'<ellipse cx="{x}" cy="718" rx="{w / 2}" ry="12" fill="{c}" opacity="0.28" filter="{fb}"/>')
        s.add(f'<rect x="0" y="744" width="{SW}" height="7" fill="#596273"/><rect x="0" y="744" width="{SW}" height="2" fill="#7a8496"/>')
        bench(s, 290, 706, 84, "#5a4030", "#151820")
        for x in (190, 432, 1000):
            street_lamp(s, x, 706, 200)
        bicycle(s, 800, 706, 1.0)
        anchor(s, "walk", 0, 738, 1.0)
        anchor(s, "walk", SW, 738, 1.0)
        # The street: wet asphalt with long reflections of every light, and puddles.
        s.add(f'<rect x="0" y="751" width="{SW}" height="{SH - 751}" fill="#191d27"/>')
        for (x, c, n_, w0) in ((190, "#ffd98a", 8, 20), (432, "#ffd98a", 8, 20), (1000, "#ffd98a", 8, 20), (599, PINK, 7, 60),
                               (575, "#ffb85a", 6, 120), (897, "#cfe8ff", 6, 110), (85, "#ffcf7a", 5, 70), (292, "#ffcf7a", 5, 80)):
            for k in range(n_):
                y = 760 + k * 13
                w = w0 * (1 - k * 0.07) * s.r.uniform(0.7, 1.1)
                s.add(f'<rect x="{x - w / 2 + s.r.uniform(-5, 5):.1f}" y="{y:.1f}" width="{w:.1f}" height="3" rx="1.5" fill="{c}" '
                      f'opacity="{0.42 - k * 0.04:.2f}"/>')
        for (x, y, w) in ((160, 800, 120), (430, 836, 150), (700, 792, 130), (910, 834, 140), (560, 730, 70), (330, 734, 56)):
            anchor(s, "puddle", x, y, w)
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{w / 2}" ry="{w / 8:.1f}" fill="#11141c"/>')
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{w / 2}" ry="{w / 8:.1f}" fill="none" stroke="#4a5266" stroke-width="1.5" opacity="0.7"/>')
            s.add(f'<ellipse cx="{x - w * 0.1:.1f}" cy="{y - w / 40:.1f}" rx="{w * 0.25:.1f}" ry="{w / 30:.1f}" fill="#ffd98a" opacity="0.35"/>')
        s.add(f'<rect x="0" y="804" width="{SW}" height="3" fill="#e8e2c8" opacity="0.08"/>')
        s.add("".join(f'<rect x="{x}" y="806" width="50" height="4" fill="#d8d0b0" opacity="0.35"/>' for x in range(20, SW, 110)))
