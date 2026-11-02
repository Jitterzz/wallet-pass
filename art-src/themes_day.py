"""Pass Studio theme art: famous places by day and at sunset. Laid out like themes.py: each theme has a page sky (top of the page down to the
scene), decorations in that sky (page coordinates, kept to the edges and off the pass), and a scene along the bottom
(band coordinates, 0..SW x 0..SH)."""
from props import *  # noqa: F401,F403
from themes import theme, scatter  # noqa: F401

Front = None  # set by render.py: `with Front(s):` draws in front of the scene band


def faded_rays(s, x, y, r, color, n=12, a=0.16, spread=0.09, fade=170):
    """The sun's rays (base.rays) fading out toward the top of the scene band, so they never stop at a hard line
    where the band meets the page's sky."""
    mid = s.id("rmask")
    s.defs.append(f'<linearGradient id="{mid}g" x1="0" y1="0" x2="0" y2="{fade}" gradientUnits="userSpaceOnUse">'
                  f'<stop offset="0" stop-color="#ffffff" stop-opacity="0"/><stop offset="1" stop-color="#ffffff" stop-opacity="1"/>'
                  f'</linearGradient>')
    s.defs.append(f'<mask id="{mid}" maskUnits="userSpaceOnUse" x="-50" y="-50" width="{SW + 100}" height="{SH + 100}">'
                  f'<rect x="-50" y="-50" width="{SW + 100}" height="{SH + 100}" fill="url(#{mid}g)"/></mask>')
    s.add(f'<g mask="url(#{mid})">')
    rays(s, x, y, r, color, n, a, spread)
    s.add('</g>')


def poly(pts):
    return " ".join(f"{x:.1f},{y:.1f}" for x, y in pts)


def path_through(pts, close=True):
    d = "M" + " L".join(f"{x:.1f},{y:.1f}" for x, y in pts)
    return d + ("Z" if close else "")


# ======================= Golden Gate: the bridge from Baker Beach at sunset, fog rolling in =======================
GG_LIT, GG_SHADE, GG_DEEP, GG_HI = "#c4402b", "#8f2a20", "#6e1f18", "#ef7148"

# A simple camera for the bridge. t runs along it: 0 at the near (south) tower, 1 at the far (north) one, below 0
# toward the city, above 1 toward Marin. Heights are metres above the water.
GG_XN, GG_XF, GG_R = 790.0, 320.0, 0.62      # where the towers stand, and how big the far one looks next to the near one
GG_YH, GG_K, GG_EYE = 472.0, 1.8855, 14.85   # the horizon, pixels per metre at the near tower, eye height (metres)
GG_T_CITY, GG_T_MARIN = -0.268, 1.27         # where the side spans end
GG_DECK, GG_TRUSS = 67.0, 59.4               # road and the bottom of the truss under it


def gg_s(t):
    """How big things look at t, next to the near tower."""
    return 1.0 / (1.0 + t * (1.0 / GG_R - 1.0))


def gg_x(t, side=0.0):
    """Screen x at t; side moves across the bridge (near-tower pixels; negative is the near side, on the left)."""
    xn, xf = GG_XN - 516, GG_XF - 516
    return 516 + (xn + t * (xf / GG_R - xn) + side) * gg_s(t)


def gg_y(t, height):
    return GG_YH + (GG_EYE - height) * GG_K * gg_s(t)


def gg_cable_h(t):
    """The main cable's height: over the saddles at 222 m, down to 72 m at mid-span and to the anchorages at the ends."""
    if 0 <= t <= 1:
        return 72 + (222 - 72) * (2 * t - 1) ** 2
    u = (t / GG_T_CITY) if t < 0 else ((t - 1) / (GG_T_MARIN - 1))
    return 222 - (222 - 70) * u - 16 * math.sin(math.pi * u)


GG_LEG = [(0, 98, 22), (98, 150, 20), (150, 192, 18), (192, 227, 16)]   # leg sections: from, to (m), width (px)
GG_STRUTS = [(203, 216), (165, 178), (127, 141), (89, 104)]


def gg_leg_w(h):
    for h0, h1, w in GG_LEG:
        if h0 <= h <= h1:
            return w
    return GG_LEG[-1][2]


def gg_leg(s, t, side):
    """One tower leg: sunlit face, shaded side, grooves and setbacks, a cap on top."""
    sc = gg_s(t)
    xc = gg_x(t, side)
    for h0, h1, w in GG_LEG:
        ytop, ybot = gg_y(t, h1), gg_y(t, h0) + (2 if h0 == 0 else 0.6)
        ww = w * sc
        x0 = xc - ww / 2
        s.add(f'<rect x="{x0:.1f}" y="{ytop:.1f}" width="{ww * 0.64:.1f}" height="{ybot - ytop:.1f}" fill="{GG_LIT}"/>')
        s.add(f'<rect x="{x0 + ww * 0.64:.1f}" y="{ytop:.1f}" width="{ww * 0.36:.1f}" height="{ybot - ytop:.1f}" fill="{GG_SHADE}"/>')
        s.add(f'<rect x="{x0:.1f}" y="{ytop:.1f}" width="{max(1.0, 1.6 * sc):.1f}" height="{ybot - ytop:.1f}" fill="{GG_HI}" opacity="0.75"/>')
        for f in (0.26, 0.46):
            s.add(f'<rect x="{x0 + ww * f:.1f}" y="{ytop + 3 * sc:.1f}" width="{max(0.8, 1.1 * sc):.1f}" '
                  f'height="{ybot - ytop - 6 * sc:.1f}" fill="{GG_DEEP}" opacity="0.45"/>')
        s.add(f'<rect x="{x0 - 1.2 * sc:.1f}" y="{ytop:.1f}" width="{ww + 2.4 * sc:.1f}" height="{max(1.2, 2.4 * sc):.1f}" '
              f'fill="{GG_DEEP}" opacity="0.55"/>')
    ytop = gg_y(t, 227)
    for k, (dw, dh) in enumerate(((10, 5), (5, 4))):
        ww = (16 - dw) * sc
        s.add(f'<rect x="{xc - ww / 2:.1f}" y="{ytop - (k + 1) * dh * sc:.1f}" width="{ww:.1f}" height="{dh * sc + 0.5:.1f}" '
              f'fill="{GG_SHADE}"/>')


def gg_tower_back(s, t):
    """The far leg of a tower, the portal struts above the deck and the bracing below it."""
    sc = gg_s(t)
    gg_leg(s, t, 27)
    xl, xr = gg_x(t, -27), gg_x(t, 27)
    for h0, h1 in GG_STRUTS:
        w = gg_leg_w((h0 + h1) / 2) * sc / 2
        x0, x1 = xl + w, xr - w
        y0, y1 = gg_y(t, h1), gg_y(t, h0)
        s.add(f'<rect x="{x0:.1f}" y="{y0:.1f}" width="{x1 - x0:.1f}" height="{y1 - y0:.1f}" fill="{GG_LIT}"/>')
        s.add(f'<rect x="{x0:.1f}" y="{y1 - (y1 - y0) * 0.3:.1f}" width="{x1 - x0:.1f}" height="{(y1 - y0) * 0.3:.1f}" fill="{GG_SHADE}"/>')
        for k in range(1, 4):
            xx = x0 + (x1 - x0) * k / 4
            s.add(f'<rect x="{xx - 0.6 * sc:.1f}" y="{y0 + 1.5 * sc:.1f}" width="{max(0.8, 1.2 * sc):.1f}" '
                  f'height="{(y1 - y0) * 0.62:.1f}" fill="{GG_DEEP}" opacity="0.5"/>')
    # Below the deck: an X between the legs and a strut under the road.
    w = gg_leg_w(30) * sc / 2
    x0, x1 = xl + w, xr - w
    ya, yb = gg_y(t, 54), gg_y(t, 8)
    sw = max(1.6, 3.4 * sc)
    s.add(f'<path d="M{x0:.1f},{ya:.1f} L{x1:.1f},{yb:.1f} M{x1:.1f},{ya:.1f} L{x0:.1f},{yb:.1f}" stroke="{GG_SHADE}" '
          f'stroke-width="{sw:.1f}"/>')
    s.add(f'<rect x="{x0:.1f}" y="{gg_y(t, 58):.1f}" width="{x1 - x0:.1f}" height="{max(2, 5 * sc):.1f}" fill="{GG_LIT}"/>')


def gg_base(s, t):
    """The concrete pier a tower stands on, and its fender in the water."""
    sc = gg_s(t)
    xc, y = gg_x(t), gg_y(t, 0)
    w = 118 * sc
    s.add(f'<path d="M{xc - w / 2:.1f},{y + 3 * sc:.1f} L{xc + w / 2:.1f},{y + 3 * sc:.1f} L{xc + w * 0.47:.1f},{y - 9 * sc:.1f} '
          f'L{xc - w * 0.47:.1f},{y - 9 * sc:.1f}Z" fill="#c7a7a0"/>')
    s.add(f'<path d="M{xc + w * 0.12:.1f},{y + 3 * sc:.1f} L{xc + w / 2:.1f},{y + 3 * sc:.1f} L{xc + w * 0.47:.1f},{y - 9 * sc:.1f} '
          f'L{xc + w * 0.12:.1f},{y - 9 * sc:.1f}Z" fill="#98767a"/>')
    s.add(f'<rect x="{xc - w / 2:.1f}" y="{y + 2 * sc:.1f}" width="{w:.1f}" height="{2.5 * sc:.1f}" fill="#f4d2c4" opacity="0.5"/>')


def gg_cable(s, side, color, width, a=1.0):
    """A main cable from the city anchorage over both towers to Marin, with its suspenders."""
    n = 160
    ts = [GG_T_CITY + (GG_T_MARIN - GG_T_CITY) * i / n for i in range(n + 1)]
    pts = [(gg_x(t, side), gg_y(t, gg_cable_h(t))) for t in ts]
    # Suspenders, every few metres along the spans.
    hang = []
    k = 0
    for t in [i / 64 for i in range(1, 64)] + [GG_T_CITY * i / 18 for i in range(1, 18)] + \
             [1 + (GG_T_MARIN - 1) * i / 18 for i in range(1, 18)]:
        x = gg_x(t, side)
        hang.append(f"M{x:.1f},{gg_y(t, gg_cable_h(t)):.1f} V{gg_y(t, GG_DECK):.1f}")
        k += 1
    s.add(f'<path d="{" ".join(hang)}" stroke="{color}" stroke-width="{max(0.7, width * 0.3):.2f}" opacity="{0.75 * a:.2f}"/>')
    for (x0, y0), (x1, y1), t in zip(pts, pts[1:], ts):
        s.add(f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1:.1f}" y2="{y1:.1f}" stroke="{color}" '
              f'stroke-width="{max(1.0, width * gg_s(t)):.2f}" stroke-linecap="round" opacity="{a}"/>')


def gg_deck(s):
    """The deck: road, railing and the truss under it, thinning into the distance."""
    n = 110
    ts = [GG_T_CITY + (GG_T_MARIN - GG_T_CITY) * i / n for i in range(n + 1)]
    top = [(gg_x(t), gg_y(t, GG_DECK)) for t in ts]
    bot = [(gg_x(t), gg_y(t, GG_TRUSS)) for t in ts]
    under = [(gg_x(t), gg_y(t, GG_TRUSS) + 2.2 * gg_s(t)) for t in ts]
    s.add(f'<polygon points="{poly(bot + under[::-1])}" fill="{GG_DEEP}"/>')
    s.add(f'<polygon points="{poly(top + bot[::-1])}" fill="{GG_LIT}"/>')
    lines = []
    for i, t in enumerate(ts[:-1]):
        (xa, ya), (xb, yb) = top[i], bot[i]
        (xc, yc), (xd, yd) = top[i + 1], bot[i + 1]
        lines.append(f"M{xa:.1f},{ya:.1f} L{xb:.1f},{yb:.1f}")
        lines.append(f"M{xa:.1f},{ya:.1f} L{xd:.1f},{yd:.1f}" if i % 2 == 0 else f"M{xb:.1f},{yb:.1f} L{xc:.1f},{yc:.1f}")
    s.add(f'<path d="{" ".join(lines)}" stroke="{GG_DEEP}" stroke-width="1.1" opacity="0.6"/>')
    s.add(f'<polyline points="{poly(bot)}" fill="none" stroke="{GG_DEEP}" stroke-width="1.6"/>')
    rail = [(x, y - 2.6 * gg_s(t)) for (x, y), t in zip(top, ts)]
    s.add(f'<polyline points="{poly(rail)}" fill="none" stroke="{GG_HI}" stroke-width="1.4"/>')
    s.add(f'<polyline points="{poly(top)}" fill="none" stroke="{GG_HI}" stroke-width="1.2" opacity="0.8"/>')
    for i in range(0, n, 4):
        x, y = top[i]
        s.add(f'<circle cx="{x:.1f}" cy="{y - 4.5 * gg_s(ts[i]):.1f}" r="{max(0.9, 1.6 * gg_s(ts[i])):.1f}" fill="#ffe2a8" '
              f'opacity="0.85"/>')


def gg_cypress(s, x, base, h, c1="#2c4436", c2="#3e5c44", trunk="#3a2a26"):
    """A windswept Monterey cypress: a bent trunk and flat, layered tufts leaning with the wind."""
    s.add(f'<path d="M{x - 4},{base} Q{x + 2},{base - h * 0.4} {x + h * 0.18},{base - h * 0.62} L{x + h * 0.2},{base - h * 0.56} '
          f'Q{x + 8},{base - h * 0.36} {x + 4},{base}Z" fill="{trunk}"/>')
    for (dx, dy, rx, ry) in ((-0.1, 0.62, 0.42, 0.13), (0.2, 0.78, 0.5, 0.14), (0.42, 0.66, 0.36, 0.12), (0.05, 0.9, 0.34, 0.1)):
        cx, cy = x + dx * h, base - dy * h
        s.add(f'<ellipse cx="{cx:.1f}" cy="{cy:.1f}" rx="{rx * h:.1f}" ry="{ry * h:.1f}" fill="{c1}"/>')
        s.add(f'<ellipse cx="{cx - rx * h * 0.15:.1f}" cy="{cy - ry * h * 0.3:.1f}" rx="{rx * h * 0.75:.1f}" ry="{ry * h * 0.6:.1f}" '
              f'fill="{c2}"/>')


def gg_people(s, x, base, color="#3a2838"):
    """Two people on a plaid blanket watching the sunset, a dog sitting beside them (seen from behind)."""
    s.add(f'<ellipse cx="{x + 20}" cy="{base + 16}" rx="112" ry="13" fill="#7a4a3a" opacity="0.35"/>')
    s.add(f'<path d="M{x - 66},{base - 2} L{x + 82},{base - 2} L{x + 104},{base + 20} L{x - 54},{base + 20}Z" fill="#c8484a"/>')
    s.add(f'<path d="M{x - 30},{base - 2} L{x - 14},{base + 20} M{x + 12},{base - 2} L{x + 28},{base + 20} '
          f'M{x + 54},{base - 2} L{x + 70},{base + 20} M{x - 60},{base + 6} L{x + 90},{base + 6} M{x - 57},{base + 13} L{x + 97},{base + 13}" '
          f'stroke="#f2d6c8" stroke-width="2.5" opacity="0.55"/>')
    # The one on the left, then the one leaning on them.
    s.add(f'<path d="M{x - 40},{base} Q{x - 44},{base - 30} {x - 30},{base - 42} Q{x - 18},{base - 50} {x - 6},{base - 42} '
          f'Q{x + 6},{base - 30} {x + 2},{base}Z" fill="{color}"/>')
    s.add(f'<circle cx="{x - 19}" cy="{base - 57}" r="14" fill="{color}"/>')
    s.add(f'<path d="M{x + 4},{base} Q{x + 2},{base - 28} {x + 14},{base - 40} Q{x + 26},{base - 48} {x + 38},{base - 40} '
          f'Q{x + 50},{base - 28} {x + 46},{base}Z" fill="{color}"/>')
    s.add(f'<circle cx="{x + 22}" cy="{base - 54}" r="13" fill="{color}"/>')
    s.add(f'<path d="M{x + 12},{base - 38} Q{x - 4},{base - 46} {x - 18},{base - 38}" stroke="{color}" stroke-width="7" '
          f'fill="none" stroke-linecap="round"/>')
    s.add(f'<path d="M{x - 30},{base - 60} Q{x - 19},{base - 75} {x - 7},{base - 62}" stroke="#ffb98a" stroke-width="2" '
          f'fill="none" opacity="0.7"/>')
    s.add(f'<path d="M{x + 11},{base - 58} Q{x + 22},{base - 71} {x + 33},{base - 58}" stroke="#ffb98a" stroke-width="2" '
          f'fill="none" opacity="0.7"/>')
    # The dog, sitting, head turned toward the sunset: snout, a floppy ear, a collar, a tail on the sand.
    d = x + 74
    s.add(f'<ellipse cx="{d + 2}" cy="{base - 15}" rx="15" ry="19" fill="{color}"/>')
    s.add(f'<path d="M{d - 9},{base - 26} Q{d - 12},{base - 40} {d - 6},{base - 46} L{d + 8},{base - 42} Q{d + 10},{base - 30} {d + 12},{base - 22}Z" '
          f'fill="{color}"/>')
    s.add(f'<ellipse cx="{d - 3}" cy="{base - 50}" rx="11" ry="10" fill="{color}"/>')
    s.add(f'<path d="M{d - 10},{base - 54} Q{d - 22},{base - 52} {d - 25},{base - 46} Q{d - 22},{base - 41} {d - 8},{base - 43}Z" fill="{color}"/>')
    s.add(f'<circle cx="{d - 25}" cy="{base - 47}" r="2.4" fill="#1e141c"/>')
    s.add(f'<path d="M{d + 2},{base - 58} Q{d + 12},{base - 56} {d + 11},{base - 44} Q{d + 6},{base - 46} {d + 2},{base - 52}Z" fill="#2a1c28"/>')
    s.add(f'<path d="M{d - 9},{base - 41} Q{d},{base - 37} {d + 9},{base - 41}" stroke="#d8483a" stroke-width="3" fill="none"/>')
    s.add(f'<path d="M{d + 14},{base - 3} Q{d + 32},{base - 1} {d + 38},{base - 10}" stroke="{color}" stroke-width="5" fill="none" '
          f'stroke-linecap="round"/>')
    s.add(f'<path d="M{d - 16},{base - 54} Q{d - 4},{base - 63} {d + 6},{base - 57}" stroke="#ffb98a" stroke-width="2" fill="none" opacity="0.6"/>')


def gg_gull(s, x, y, k, a=1.0):
    """A gull standing on the sand."""
    s.add(f'<g transform="translate({x},{y}) scale({k})" opacity="{a}"><ellipse cx="0" cy="-9" rx="11" ry="6.5" fill="#f6efe9"/>'
          f'<path d="M-9,-11 Q2,-16 12,-9 L4,-6 Q-4,-8 -9,-11Z" fill="#9aa0ac"/><circle cx="-9" cy="-16" r="5" fill="#f6efe9"/>'
          f'<path d="M-14,-16 L-18,-15 L-14,-14Z" fill="#f2b33a"/><path d="M-2,-3 V4 M2,-3 V4" stroke="#e0a07a" stroke-width="1.6"/></g>')


@theme("golden_gate", [(0, "#1c3563"), (0.45, "#3a4886"), (0.78, "#7c5288"), (1, "#c26a83")])
class GoldenGate:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 26, 720, "#ffffff", 0.05)
        s.moon(902, 430, 24, crescent=True, color="#fff1d8", halo="#ffe9c9", ha=0.22)
        long_cloud(s, 110, 1235, 330, 44, "#e7929f", None, 0.5)
        long_cloud(s, 960, 1120, 300, 40, "#d98a9e", None, 0.45)
        long_cloud(s, 60, 1010, 220, 30, "#c9819c", None, 0.35)
        for (x, y, sc) in ((120, 610, 1.3), (160, 632, 1.0), (930, 820, 1.2), (968, 842, 0.9)):
            s.bird(x, y, sc, "#ffffff", 0.85)

    @staticmethod
    def scene(s):
        sky = s.lin([(0, "#c26a83", 1), (0.45, "#ec8f72", 1), (0.8, "#fbb47e", 1), (1, "#ffd193", 1)], 0, 0, 0, GG_YH,
                    "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{sky}"/>')
        s.sun(190, 330, 42, "#fff8e2", "#ffd27f", "#ffc27a", 0.6)
        faded_rays(s, 190, 330, 420, "#ffd6a0", 14, 0.12, 0.05)
        long_cloud(s, 560, 205, 360, 30, "#f7a888", None, 0.6)
        long_cloud(s, 300, 150, 260, 22, "#e88f8f", None, 0.45)
        long_cloud(s, 900, 260, 240, 22, "#ffbf94", None, 0.55)
        # Far hills across the bay, behind the bridge.
        s.add(f'<path d="M470,{GG_YH} L470,462 Q540,446 610,452 Q690,440 760,450 Q860,438 930,452 Q990,446 {SW},450 '
              f'L{SW},{GG_YH}Z" fill="#b98aa1"/>')
        s.add(f'<path d="M600,{GG_YH} Q680,458 760,462 Q840,456 900,{GG_YH}Z" fill="#a67893"/>')
        # The bay.
        anchor(s, "water", 0, GG_YH, 0)
        water = s.lin([(0, "#e3a59c", 1), (0.3, "#a888a6", 1), (1, "#4a5d8e", 1)], 0, GG_YH, 0, 620, "userSpaceOnUse")
        s.add(f'<rect x="0" y="{GG_YH}" width="{SW}" height="{SH - GG_YH}" fill="{water}"/>')
        for _ in range(70):
            y = s.r.uniform(GG_YH + 6, 612)
            w = s.r.uniform(14, 60) * (0.4 + (y - GG_YH) / 160)
            x = s.r.uniform(-20, SW)
            c = "#ffffff" if s.r.random() < 0.5 else "#3c4a7a"
            s.add(f'<rect x="{x:.0f}" y="{y:.0f}" width="{w:.0f}" height="{1.2 + (y - GG_YH) / 70:.1f}" rx="1" fill="{c}" '
                  f'opacity="{s.r.uniform(0.1, 0.25):.2f}"/>')
        # The sun's path on the water: a soft glow and broken gold streaks, wider toward us.
        s.add(f'<ellipse cx="190" cy="{GG_YH + 50}" rx="70" ry="60" fill="{s.rad([(0, "#ffd48a", 0.55), (1, "#ffd48a", 0)], 190, GG_YH + 50, 70)}"/>')
        for k in range(40):
            y = GG_YH + 3 + k * 3.4 + s.r.uniform(-1, 1)
            spread = 10 + k * 2.2
            for _ in range(2 if k > 8 else 1):
                w = s.r.uniform(6, 16 + k * 1.1)
                x = 190 + s.r.uniform(-spread, spread) - w / 2
                s.add(f'<rect x="{x:.0f}" y="{y:.1f}" width="{w:.0f}" height="{1.4 + k * 0.05:.1f}" rx="1" '
                      f'fill="{s.r.choice(("#ffd47e", "#ffe7b0", "#ffbf6a"))}" opacity="{0.9 - k * 0.016:.2f}"/>')
        # The Marin Headlands, a far ridge and the near one, rim-lit by the sun behind them.
        s.add(f'<path d="M0,{GG_YH + 4} L0,372 Q50,352 110,358 Q170,366 230,380 Q300,398 360,430 Q420,458 470,{GG_YH + 4}Z" fill="#9a6f94"/>')
        ridge = [(0, 352), (40, 336), (80, 328), (125, 334), (160, 346), (200, 354), (245, 344), (285, 346), (330, 360),
                 (375, 386), (415, 414), (450, 442), (490, 474)]
        hl = s.lin([(0, "#6c4b70", 1), (1, "#4e3657", 1)], 0, 330, 0, GG_YH, "userSpaceOnUse")
        s.add(f'<path d="{path_through(ridge + [(490, GG_YH + 6), (0, GG_YH + 6)])}" fill="{hl}"/>')
        s.add(f'<path d="{path_through(ridge, False)}" fill="none" stroke="#ffb98a" stroke-width="2.4" opacity="0.6"/>')
        for (x0, y0, x1, y1) in ((60, 360, 120, 420), (180, 372, 250, 440), (300, 380, 360, 450)):
            s.add(f'<path d="M{x0},{y0} Q{(x0 + x1) / 2 + 14},{(y0 + y1) / 2} {x1},{y1}" stroke="#5a3d60" stroke-width="5" fill="none" '
                  f'opacity="0.5"/>')
        s.add(f'<path d="M{gg_x(GG_T_MARIN):.1f},{gg_y(GG_T_MARIN, GG_DECK):.1f} Q215,420 170,433" stroke="#8a5e7a" stroke-width="3" '
              f'fill="none" opacity="0.8"/>')
        # A still fog bank low over the water on the west side (the app adds fog drifting through).
        f = s.blur(12)
        for (x, y, rx, ry, a) in ((150, 470, 250, 22, 0.55), (400, 482, 180, 15, 0.42), (70, 448, 140, 16, 0.35),
                                  (40, 340, 110, 18, 0.3), (230, 488, 210, 12, 0.35)):
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#fff4f0" opacity="{a}" filter="{f}"/>')
        anchor(s, "fog", 0, 478, 44)
        # The bridge: far legs, struts and the far cable behind the deck; near legs and cable in front.
        gg_cable(s, 27, "#9a2f24", 2.4, 0.9)
        for t in (1.0, 0.0):
            gg_tower_back(s, t)
        gg_deck(s)
        for t in (1.0, 0.0):
            gg_leg(s, t, -27)
            gg_base(s, t)
        gg_cable(s, -27, "#b9392a", 3.2)
        for t in (0.0, 1.0):
            x, y = gg_x(t), gg_y(t, 227) - 11 * gg_s(t)
            anchor(s, "antenna", x, y, 0)
            s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{2.2 * gg_s(t):.1f}" fill="#5a1a14"/>')
        # A little fog curling around the far tower's foot.
        f2 = s.blur(8)
        s.add(f'<ellipse cx="300" cy="487" rx="120" ry="9" fill="#fff4f0" opacity="0.4" filter="{f2}"/>')
        # The near tower in the water.
        for k in range(14):
            y = 508 + k * 7
            w = s.r.uniform(30, 70) * (1 - k * 0.04)
            s.add(f'<rect x="{790 - w / 2 + s.r.uniform(-10, 10):.0f}" y="{y}" width="{w:.0f}" height="2.6" rx="1.3" fill="#d6502f" '
                  f'opacity="{0.42 - k * 0.025:.2f}"/>')
        # Traffic and the city side: where the road runs (cars are the app's).
        anchor(s, "deck", SW, gg_y(GG_T_CITY + 0.004, GG_DECK) - 1, 1.0)
        anchor(s, "deck", gg_x(GG_T_MARIN), gg_y(GG_T_MARIN, GG_DECK) - 1, round(gg_s(GG_T_MARIN) / gg_s(GG_T_CITY), 2))
        # The Presidio bluff under the city end, with cypresses on top and the anchorage pier.
        bluff = [(866, 640), (874, 560), (896, 500), (930, 452), (968, 426), (1004, 414), (SW, 410), (SW, 640)]
        s.add(f'<path d="{path_through(bluff)}" fill="#6a4a56"/>')
        s.add(f'<path d="M874,560 L896,500 L930,452 L968,426 L1004,414 L{SW},410" stroke="#d98a76" stroke-width="3" fill="none" opacity="0.6"/>')
        s.add(f'<path d="M900,640 Q910,560 950,500 Q975,470 {SW},450 L{SW},640Z" fill="#58404c"/>')
        xa, ya = gg_x(-0.245), gg_y(-0.245, GG_TRUSS)
        s.add(f'<path d="M{xa - 13:.1f},{ya:.1f} L{xa + 13:.1f},{ya:.1f} L{xa + 19:.1f},420 L{xa - 19:.1f},420Z" fill="#b8968f"/>')
        s.add(f'<path d="M{xa + 4:.1f},{ya:.1f} L{xa + 13:.1f},{ya:.1f} L{xa + 19:.1f},420 L{xa + 6:.1f},420Z" fill="#8c6f73"/>')
        s.add(f'<rect x="{xa - 16:.1f}" y="{ya:.1f}" width="32" height="4" fill="#d9b8ae"/>')
        for (x0, y0, x1, y1) in ((892, 520, 1032, 498), (884, 566, 1032, 548), (900, 470, 1032, 452)):
            s.add(f'<path d="M{x0},{y0} Q{(x0 + x1) / 2},{(y0 + y1) / 2 + 8} {x1},{y1}" stroke="#4a3440" stroke-width="3" fill="none" opacity="0.55"/>')
        for (x, y, r) in ((912, 488, 14), (940, 470, 12), (980, 452, 16), (1016, 444, 12), (896, 536, 10), (1010, 520, 14), (960, 590, 12)):
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{r * 1.4}" ry="{r * 0.7}" fill="#4f5a3e"/>')
            s.add(f'<ellipse cx="{x - r * 0.3}" cy="{y - r * 0.25}" rx="{r * 0.8}" ry="{r * 0.4}" fill="#6a7448"/>')
        gg_cypress(s, 948, 440, 70)
        gg_cypress(s, 1000, 420, 58)
        # Baker Beach: wet sand at the water's edge, then dry sand.
        shore = [(x, 616 + 5 * math.sin(x / 70) + 3 * math.sin(x / 23)) for x in range(-10, SW + 20, 20)]
        wet = s.lin([(0, "#a98f98", 1), (1, "#c9a48f", 1)], 0, 610, 0, 650, "userSpaceOnUse")
        s.add(f'<path d="{path_through(shore + [(SW + 10, SH), (-10, SH)])}" fill="{wet}"/>')
        dry_edge = [(x, y + 26 + 4 * math.sin(x / 41)) for x, y in shore]
        sand = s.lin([(0, "#e7bf98", 1), (0.5, "#dfb08a", 1), (1, "#cf9a78", 1)], 0, 630, 0, SH, "userSpaceOnUse")
        s.add(f'<path d="{path_through(dry_edge + [(SW + 10, SH), (-10, SH)])}" fill="{sand}"/>')
        for k in range(16):
            x = 140 + k * 26 + s.r.uniform(-4, 4)
            s.add(f'<rect x="{x:.0f}" y="{620 + k % 3:.0f}" width="{s.r.uniform(18, 40):.0f}" height="2" rx="1" fill="#ffd8a8" opacity="0.45"/>')
        anchor(s, "surf", 0, 618, 30)
        # Sand ripples, footprints, rocks, driftwood and grass.
        for _ in range(60):
            x, y = s.r.uniform(0, SW), s.r.uniform(670, 850)
            w = s.r.uniform(20, 60)
            s.add(f'<path d="M{x:.0f},{y:.0f} q{w / 2:.0f},-4 {w:.0f},0" stroke="#c48c6a" stroke-width="2" fill="none" opacity="0.4"/>')
        for k in range(12):
            x = 330 + k * 19 + (6 if k % 2 else -6)
            y = 860 - k * 11
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{5 - k * 0.15:.1f}" ry="{8 - k * 0.3:.1f}" fill="#b07c5e" opacity="0.5" '
                  f'transform="rotate(-25 {x} {y})"/>')
        for (x, y, rx, ry) in ((936, 712, 64, 34), (1006, 722, 56, 40), (872, 728, 34, 18), (978, 760, 48, 20), (846, 704, 22, 11)):
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#4d3a44"/>')
            s.add(f'<ellipse cx="{x - rx * 0.2:.0f}" cy="{y - ry * 0.35:.0f}" rx="{rx * 0.62:.0f}" ry="{ry * 0.42:.0f}" fill="#7a5a5e"/>')
            s.add(f'<path d="M{x - rx * 0.8:.0f},{y - ry * 0.3:.0f} Q{x - rx * 0.2:.0f},{y - ry * 1.05:.0f} {x + rx * 0.5:.0f},{y - ry * 0.6:.0f}" '
                  f'stroke="#e9a185" stroke-width="2.5" fill="none" opacity="0.6"/>')
        s.add('<path d="M168,792 Q240,780 330,786 L334,798 Q240,794 170,804Z" fill="#8a7468"/>'
              '<path d="M176,794 Q240,786 326,790" stroke="#c9b0a0" stroke-width="2" fill="none"/>'
              '<path d="M280,788 L300,766 L306,770 L290,790Z" fill="#8a7468"/><ellipse cx="250" cy="806" rx="90" ry="7" fill="#a0705a" opacity="0.35"/>')
        tufts(s, 850, 6, 0.6, 1.0, "#8a7a4a", 26, (16, 34), 0, 0, 170, 0.9)
        tufts(s, 850, 6, 0.6, 2.0, "#8a7a4a", 22, (14, 30), 0, 870, SW, 0.9)
        gg_people(s, 560, 742)
        for (x, y, k) in ((402, 694, 1.0), (440, 702, 0.9), (726, 700, 1.1)):
            gg_gull(s, x, y, k)
        for (x, y, sc) in ((600, 140, 1.3), (636, 160, 1.0), (664, 132, 0.9), (420, 250, 1.0)):
            s.bird(x, y, sc, "#ffffff", 0.85)
        anchor(s, "lane", 0, 532, 0.8)
        anchor(s, "lane", 0, 592, 1.0)


# ======================= Mount Fuji: dawn from the Chureito Pagoda, cherry blossoms in bloom =======================
SAKURA = ("#d98ba3", "#f2a9c0", "#f9c6d5", "#fde4ec")   # shade, base, light, highlight
FJ_RAIL = 628          # top of the track on the viaduct (band y)
FJ_RAIL_X0 = 424       # the track is clear of everything in front from here to the right edge


def fj_silhouette(x):
    """Fuji's outline: a small flat top and long slopes, steep near the top and flattening out."""
    d = x - 560
    if abs(d) <= 38:
        return 152 + 3 * math.sin(d / 6)
    span = 520 if d < 0 else 470
    u = min(1.4, (abs(d) - 38) / span)
    return 152 + 372 * u ** 0.78


def sakura_canopy(s, cx, cy, rx, ry, n=26):
    """A cloud of cherry blossom: overlapping clumps in four pinks, lit from the upper right, with single flowers."""
    shade, base, light, hi = SAKURA
    blobs = []
    for _ in range(n):
        a = s.r.uniform(0, 2 * math.pi)
        d = s.r.uniform(0, 1) ** 0.6
        blobs.append((cx + math.cos(a) * rx * d, cy + math.sin(a) * ry * d, s.r.uniform(0.22, 0.38) * (rx + ry) / 2))
    for bx, by, br in blobs:
        s.add(f'<circle cx="{bx:.1f}" cy="{by + br * 0.15:.1f}" r="{br:.1f}" fill="{shade}"/>')
    for bx, by, br in blobs:
        s.add(f'<circle cx="{bx + br * 0.1:.1f}" cy="{by - br * 0.08:.1f}" r="{br * 0.84:.1f}" fill="{base}"/>')
    for bx, by, br in sorted(blobs, key=lambda b: b[1] - b[0] * 0.5)[:n // 2]:
        s.add(f'<circle cx="{bx + br * 0.25:.1f}" cy="{by - br * 0.3:.1f}" r="{br * 0.5:.1f}" fill="{light}"/>')
    for _ in range(n):
        x, y = cx + s.r.uniform(-rx, rx), cy + s.r.uniform(-ry, ry)
        if ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2 < 0.9:
            s.blossom(x, y, s.r.uniform(3.5, 6), hi, "#e0779a")


def sakura(s, x, base, h, w, lean=0.0):
    """A cherry tree in full bloom: a dark, crooked trunk and branches holding a wide canopy."""
    trunk = "#4a3438"
    top = base - h * 0.55
    s.add(f'<path d="M{x - h * 0.05},{base} C{x - h * 0.04},{base - h * 0.25} {x + lean - h * 0.07},{base - h * 0.4} '
          f'{x + lean - h * 0.2},{top} L{x + lean - h * 0.15},{top - 4} C{x + lean - h * 0.02},{base - h * 0.42} {x + lean + h * 0.02},{base - h * 0.42} '
          f'{x + lean + h * 0.18},{top - 6} L{x + lean + h * 0.22},{top} C{x + lean + h * 0.06},{base - h * 0.38} {x + h * 0.05},{base - h * 0.25} '
          f'{x + h * 0.05},{base}Z" fill="{trunk}"/>')
    sakura_canopy(s, x + lean, base - h * 0.68, w / 2, h * 0.3, int(18 + w / 12))


def fj_roof(s, x, y, rw, depth, rise):
    """A pagoda roof: dark tiles sweeping up at the corners, a red bracketed underside."""
    l, r = x - rw / 2, x + rw / 2
    s.add(f'<path d="M{l + rw * 0.12:.1f},{y:.1f} Q{x:.1f},{y + depth * 0.5:.1f} {r - rw * 0.12:.1f},{y:.1f} L{r - rw * 0.16:.1f},{y + depth * 0.9:.1f} '
          f'Q{x:.1f},{y + depth * 1.3:.1f} {l + rw * 0.16:.1f},{y + depth * 0.9:.1f}Z" fill="#8e2c24"/>')
    s.add(f'<path d="M{l:.1f},{y - rise:.1f} Q{x - rw * 0.3:.1f},{y + depth * 0.15:.1f} {x:.1f},{y + depth * 0.2:.1f} '
          f'Q{x + rw * 0.3:.1f},{y + depth * 0.15:.1f} {r:.1f},{y - rise:.1f} Q{x + rw * 0.36:.1f},{y - depth * 0.6:.1f} {x + rw * 0.18:.1f},{y - depth * 1.5:.1f} '
          f'L{x - rw * 0.18:.1f},{y - depth * 1.5:.1f} Q{x - rw * 0.36:.1f},{y - depth * 0.6:.1f} {l:.1f},{y - rise:.1f}Z" fill="#46424d"/>')
    s.add(f'<path d="M{l:.1f},{y - rise:.1f} Q{x - rw * 0.3:.1f},{y + depth * 0.15:.1f} {x:.1f},{y + depth * 0.2:.1f} '
          f'Q{x + rw * 0.3:.1f},{y + depth * 0.15:.1f} {r:.1f},{y - rise:.1f}" stroke="#2a2730" stroke-width="{max(2.5, depth * 0.35):.1f}" '
          f'fill="none" stroke-linecap="round"/>')
    s.add(f'<path d="M{x + rw * 0.02:.1f},{y - depth * 1.45:.1f} L{x + rw * 0.18:.1f},{y - depth * 1.5:.1f} Q{x + rw * 0.36:.1f},{y - depth * 0.6:.1f} '
          f'{r:.1f},{y - rise:.1f} Q{x + rw * 0.3:.1f},{y + depth * 0.1:.1f} {x + rw * 0.05:.1f},{y + depth * 0.1:.1f}Z" fill="#5d5866" opacity="0.85"/>')


def pagoda(s, x, base, scale=1.0):
    """The Chureito Pagoda: five vermilion storeys under dark curved roofs, a bronze spire on top. Returns its top."""
    s.add(f'<rect x="{x - 84 * scale:.1f}" y="{base - 12 * scale:.1f}" width="{168 * scale:.1f}" height="{14 * scale:.1f}" fill="#a7a3ad"/>')
    s.add(f'<rect x="{x - 84 * scale:.1f}" y="{base - 12 * scale:.1f}" width="{168 * scale:.1f}" height="{3 * scale:.1f}" fill="#d1ccd4"/>')
    y = base - 12 * scale
    for i in range(5):
        bw = 108 * scale * 0.88 ** i
        bh = 42 * scale * 0.9 ** i
        rw = (bw + 92 * scale * 0.9 ** i)
        s.add(f'<rect x="{x - bw / 2:.1f}" y="{y - bh:.1f}" width="{bw:.1f}" height="{bh:.1f}" fill="#c93c2f"/>')
        s.add(f'<rect x="{x + bw * 0.12:.1f}" y="{y - bh:.1f}" width="{bw * 0.38:.1f}" height="{bh:.1f}" fill="#a02e25"/>')
        for f in (-0.36, -0.12, 0.12, 0.36):
            s.add(f'<rect x="{x + bw * f - 1.5 * scale:.1f}" y="{y - bh:.1f}" width="{3 * scale:.1f}" height="{bh:.1f}" fill="#7d231c" opacity="0.7"/>')
        s.add(f'<rect x="{x - bw * 0.1:.1f}" y="{y - bh * 0.78:.1f}" width="{bw * 0.2:.1f}" height="{bh * 0.78:.1f}" fill="#f1e6d2" opacity="0.92"/>')
        s.add(f'<rect x="{x - bw * 0.5:.1f}" y="{y - bh * 0.18:.1f}" width="{bw:.1f}" height="{max(2.0, 3 * scale):.1f}" fill="#7d231c"/>')
        if i > 0:
            s.add(f'<path d="M{x - bw * 0.55:.1f},{y - 2:.1f} H{x + bw * 0.55:.1f}" stroke="#e8c9a0" stroke-width="{2.2 * scale:.1f}"/>')
        y -= bh
        depth = 13 * scale * 0.94 ** i
        fj_roof(s, x, y + depth * 0.3, rw, depth, 12 * scale * 0.9 ** i)
        y -= depth * 1.3
    # The spire: a little cap, nine rings, a flame at the top.
    s.add(f'<path d="M{x - 18 * scale:.1f},{y + 4:.1f} Q{x:.1f},{y - 10 * scale:.1f} {x + 18 * scale:.1f},{y + 4:.1f}Z" fill="#46424d"/>')
    top = y - 104 * scale
    s.add(f'<rect x="{x - 2.2 * scale:.1f}" y="{top:.1f}" width="{4.4 * scale:.1f}" height="{y - top:.1f}" fill="#8a7450"/>')
    for k in range(9):
        ry = y - 18 * scale - k * 7.5 * scale
        rr = (8 - k * 0.3) * scale
        s.add(f'<ellipse cx="{x}" cy="{ry:.1f}" rx="{rr:.1f}" ry="{2.2 * scale:.1f}" fill="#b39a5e"/>')
    s.add(f'<path d="M{x},{top - 14 * scale:.1f} Q{x + 7 * scale:.1f},{top - 2:.1f} {x},{top + 4:.1f} Q{x - 7 * scale:.1f},{top - 2:.1f} {x},{top - 14 * scale:.1f}Z" '
          f'fill="#c9ad66"/>')
    return top


def fj_town(s, y0, y1, n, haze):
    """Rows of little houses and blocks in the valley, hazier further back."""
    for _ in range(n):
        y = s.r.uniform(y0, y1)
        t = (y - y0) / max(1, y1 - y0)
        k = 0.55 + 0.6 * t
        x = s.r.uniform(-10, SW + 10)
        w, h = s.r.uniform(14, 30) * k, s.r.uniform(8, 16) * k
        wall = s.r.choice(("#eef0f6", "#e2e4ee", "#d9dce8", "#f4efe9"))
        roof = s.r.choice(("#6f7ea6", "#8a96b8", "#a8656e", "#5d6c92", "#7a8fa0"))
        a = 0.55 + 0.4 * t
        s.add(f'<g opacity="{a:.2f}"><rect x="{x:.1f}" y="{y - h:.1f}" width="{w:.1f}" height="{h:.1f}" fill="{wall}"/>'
              f'<path d="M{x - 2:.1f},{y - h + 1:.1f} L{x + w * 0.5:.1f},{y - h - h * 0.45:.1f} L{x + w + 2:.1f},{y - h + 1:.1f}Z" fill="{roof}"/>'
              f'<rect x="{x + w * 0.2:.1f}" y="{y - h * 0.65:.1f}" width="{w * 0.18:.1f}" height="{h * 0.3:.1f}" fill="#8a96b0"/></g>')
    s.add(f'<rect x="0" y="{y0 - 20}" width="{SW}" height="{y1 - y0 + 20}" fill="{haze}" opacity="0.18"/>')


@theme("fuji", [(0, "#3a5fa8"), (0.5, "#5b7cc2"), (0.82, "#7f93cf"), (1, "#9aa6d8")])
class Fuji:
    @staticmethod
    def deco(s):
        # Cherry branches reaching in from the edges.
        def branch(x0, y0, pts, w):
            d = f"M{x0},{y0} " + " ".join(f"Q{a},{b} {c},{e}" for a, b, c, e in pts)
            s.add(f'<path d="{d}" stroke="#3f2c33" stroke-width="{w}" fill="none" stroke-linecap="round"/>')
        branch(-20, 470, [(60, 420, 120, 410), (170, 400, 236, 352)], 13)
        branch(110, 412, [(130, 380, 150, 352)], 6)
        branch(176, 396, [(210, 410, 250, 404)], 5)
        branch(40, 440, [(70, 470, 100, 482)], 5)
        for (cx, cy, rx, ry) in ((236, 350, 46, 30), (150, 350, 40, 28), (248, 404, 34, 22), (100, 486, 34, 24), (60, 420, 36, 26),
                                 (190, 402, 30, 20)):
            sakura_canopy(s, cx, cy, rx, ry, 16)
        branch(1052, 560, [(980, 520, 920, 520), (860, 520, 812, 476)], 13)
        branch(940, 520, [(930, 490, 950, 462)], 6)
        branch(870, 516, [(850, 540, 820, 548)], 5)
        for (cx, cy, rx, ry) in ((812, 474, 44, 30), (950, 458, 40, 28), (874, 514, 36, 24), (822, 548, 30, 20), (1010, 530, 40, 28)):
            sakura_canopy(s, cx, cy, rx, ry, 16)
        for _ in range(16):
            p = spot(s.r, TOP_CLEAR + 40, BAND_Y + 40, 20, 0.04)
            if p:
                s.petal(p[0], p[1], s.r.uniform(1.4, 2.2), s.r.choice(SAKURA[1:]), s.r.uniform(0, 180))
        s.cloud(30, 1230, 110, "#f4f0ff", 0.85)
        s.cloud(1012, 1010, 96, "#f4f0ff", 0.85)
        for (x, y, sc) in ((950, 700, 1.2), (985, 722, 0.9)):
            s.bird(x, y, sc, "#2e3a66", 0.75)

    @staticmethod
    def scene(s):
        sky = s.lin([(0, "#9aa6d8", 1), (0.55, "#cdbfe0", 1), (1, "#f6d3d3", 1)], 0, 0, 0, 500, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{sky}"/>')
        s.sun(868, 214, 38, "#fffbf3", "#ffe8c6", "#ffeedd", 0.55)
        s.cloud(170, 214, 100, "#ffffff", 0.75)
        s.cloud(980, 330, 80, "#ffffff", 0.6)
        # Far ridges either side of the mountain's foot.
        s.add(f'<path d="M0,{SH} L0,476 Q120,452 240,470 Q330,458 420,486 L620,500 Q760,470 880,480 Q960,462 {SW},476 L{SW},{SH}Z" fill="#a9a6d2"/>')
        # The mountain: slopes, the shaded west side, gullies, the snowcap with its streaks.
        pts = [(x, fj_silhouette(x)) for x in range(-20, SW + 24, 4)]
        body = s.lin([(0, "#6874b6", 1), (1, "#9ca3d3", 1)], 0, 150, 0, 540, "userSpaceOnUse")
        s.add(f'<path d="M-20,{SH} {" ".join(f"L{x},{y:.1f}" for x, y in pts)} L{SW + 24},{SH}Z" fill="{body}"/>')
        shade = [(x, y) for x, y in pts if x <= 560]
        s.add(f'<path d="M-20,{SH} {" ".join(f"L{x},{y:.1f}" for x, y in shade)} Q520,330 470,{SH}Z" fill="#3e4890" opacity="0.28"/>')
        for k in range(26):
            x0 = 560 + (k - 13) * 26 + s.r.uniform(-6, 6)
            y0 = fj_silhouette(x0) + 30
            s.add(f'<path d="M{x0:.0f},{y0:.0f} q{(x0 - 560) * 0.2:.0f},{60 + s.r.uniform(0, 50):.0f} {(x0 - 560) * 0.45:.0f},{110 + s.r.uniform(0, 60):.0f}" '
                  f'stroke="#56609e" stroke-width="2" fill="none" opacity="0.22"/>')

        fingers = []
        x = 300
        while x < 830:
            fingers.append((x, s.r.uniform(4, 11), s.r.uniform(10, 70) * (1.2 if abs(x - 560) < 160 else 0.8)))
            x += s.r.uniform(9, 22)

        def snowline(x):
            d = abs(x - 560)
            y = 296 + d * 0.07 + 10 * math.sin(x / 23) + 5 * math.sin(x / 8.1)
            for fx, fw, fl in fingers:
                u = abs(x - fx) / fw
                if u < 1:
                    y += fl * (1 - u) ** 1.6
            return y

        top = [(x, y) for x, y in pts if y < snowline(x)]
        bottom = [(x, snowline(x)) for x, _ in top][::-1]
        snow_pts = top + bottom
        s.add(f'<path d="{path_through(snow_pts)}" fill="#f9fbff"/>')
        cid = s.id("fjsnow")
        s.defs.append(f'<clipPath id="{cid}"><path d="{path_through(snow_pts)}"/></clipPath>')
        s.add(f'<g clip-path="url(#{cid})"><path d="M300,600 L548,150 L566,150 Q540,300 520,600Z" fill="#c3cdee"/>'
              + "".join(f'<path d="M{560 + (k - 9) * 9},{158} L{560 + (k - 9) * 34},{360}" stroke="#bcc7ea" stroke-width="3" '
                        f'opacity="0.8"/>' for k in range(19)) +
              '<path d="M520,150 L760,420 L700,420Z" fill="#ffffff" opacity="0.5"/></g>')
        rim = [(x, y) for x, y in pts if 476 < x < 652]
        s.add(f'<path d="{path_through(rim, False)}" stroke="#ffffff" stroke-width="2" fill="none" opacity="0.7"/>')
        f = s.blur(10)
        s.add(f'<ellipse cx="560" cy="392" rx="430" ry="13" fill="#ffffff" opacity="0.45" filter="{f}"/>')
        s.add(f'<ellipse cx="380" cy="404" rx="200" ry="9" fill="#ffffff" opacity="0.4" filter="{f}"/>')
        # The wooded foot of the mountain and the town in the valley.
        s.add(f'<path d="M0,{SH} L0,520 Q90,500 180,512 Q300,496 420,516 Q540,500 660,514 Q800,498 920,512 Q980,504 {SW},510 L{SW},{SH}Z" fill="#5f7aa0"/>')
        for _ in range(70):
            x = s.r.uniform(0, SW)
            y = 512 + s.r.uniform(-4, 10)
            r_ = s.r.uniform(6, 12)
            s.add(f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{r_:.0f}" fill="{s.r.choice(("#56709a", "#6a84a8", "#4f6890"))}"/>')
        valley = s.lin([(0, "#c9c9df", 1), (1, "#a9b6c9", 1)], 0, 528, 0, 730, "userSpaceOnUse")
        s.add(f'<rect x="0" y="528" width="{SW}" height="{SH - 528}" fill="{valley}"/>')
        for _ in range(26):
            x, y = s.r.uniform(0, SW), s.r.uniform(560, 720)
            s.add(f'<rect x="{x:.0f}" y="{y:.0f}" width="{s.r.uniform(50, 130):.0f}" height="{s.r.uniform(6, 14):.0f}" '
                  f'fill="{s.r.choice(("#b5c4b8", "#c4c9b0", "#aebfc0"))}" opacity="0.55"/>')
        for _ in range(60):
            x, y = s.r.uniform(0, SW), s.r.uniform(545, 720)
            r_ = s.r.uniform(4, 9) * (0.6 + (y - 545) / 300)
            s.add(f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{r_:.1f}" fill="{s.r.choice(("#7f98a6", "#8aa39a", "#94a8b6"))}" opacity="0.8"/>')
        fj_town(s, 548, 626, 150, "#e8e4f2")
        # The bullet train's viaduct across the valley (the train is the app's).
        s.add(f'<rect x="0" y="{FJ_RAIL}" width="{SW}" height="16" fill="#e3e2ea"/>')
        s.add(f'<rect x="0" y="{FJ_RAIL + 12}" width="{SW}" height="4" fill="#a9a8b8"/>')
        s.add(f'<rect x="0" y="{FJ_RAIL - 3}" width="{SW}" height="3" fill="#7d7f94"/>')
        for x in range(10, SW, 66):
            s.add(f'<path d="M{x},{FJ_RAIL + 16} L{x + 12},{FJ_RAIL + 16} L{x + 10},{FJ_RAIL + 70} L{x + 2},{FJ_RAIL + 70}Z" fill="#c9c8d6"/>')
            s.add(f'<rect x="{x + 7}" y="{FJ_RAIL + 16}" width="4" height="54" fill="#a9a8b8"/>')
        for x in range(30, SW, 92):
            s.add(f'<path d="M{x},{FJ_RAIL - 3} V{FJ_RAIL - 34} M{x - 8},{FJ_RAIL - 30} H{x + 10}" stroke="#8a8ca2" stroke-width="2"/>')
        s.add(f'<path d="M0,{FJ_RAIL - 30} H{SW}" stroke="#8a8ca2" stroke-width="1" opacity="0.7"/>')
        anchor(s, "rail", FJ_RAIL_X0, FJ_RAIL - 3, 1.0)
        anchor(s, "rail", SW, FJ_RAIL - 3, 1.0)
        fj_town(s, 690, 720, 40, "#e8e4f2")
        # The hillside, the pagoda and the cherry trees around it.
        s.add(f'<path d="M0,{SH} L0,620 Q120,640 220,700 Q300,716 380,752 Q470,792 540,{SH}Z" fill="#4f7a4e"/>')
        s.add(f'<path d="M0,630 Q120,650 220,708 Q300,722 380,758" stroke="#7aa86a" stroke-width="4" fill="none" opacity="0.6"/>')
        sakura(s, 70, 690, 200, 200)
        sakura(s, 150, 640, 150, 150, -10)
        pagoda(s, 236, 722, 1.0)
        # Stone steps up the hill, and a lantern.
        for k in range(14):
            y = 860 - k * 10
            x = 70 + k * 9
            s.add(f'<rect x="{x}" y="{y}" width="{70 - k * 1.5:.0f}" height="6" fill="#b9b4bf"/><rect x="{x}" y="{y + 6}" width="{70 - k * 1.5:.0f}" '
                  f'height="3" fill="#8a8592"/>')
        s.add('<g fill="#9a95a2"><rect x="214" y="818" width="10" height="34"/><path d="M200,818 L238,818 L230,808 L208,808Z"/>'
              '<rect x="206" y="796" width="26" height="12" fill="#b1acb8"/><path d="M196,796 L242,796 L219,780Z"/><rect x="204" y="852" width="30" height="8"/></g>'
              '<rect x="211" y="799" width="16" height="6" fill="#ffe2a8"/>')
        sakura(s, 330, 840, 210, 230, 10)
        sakura(s, 470, 880, 170, 190)
        # The right foreground: grass and more cherry trees, kept below the track.
        s.add(f'<path d="M520,{SH} Q620,770 760,748 Q900,730 {SW},740 L{SW},{SH}Z" fill="#5a8a55"/>')
        sakura(s, 760, 900, 190, 260)
        sakura(s, 950, 880, 200, 240, -14)
        sakura(s, 620, 920, 150, 200)
        for _ in range(40):
            x, y = s.r.uniform(0, SW), s.r.uniform(800, 860)
            s.petal(x, y, s.r.uniform(0.7, 1.1), s.r.choice(SAKURA[:3]), s.r.uniform(0, 180), 0.85)


# ======================= Grand Canyon: sunset from the South Rim =======================
def gc_layer(s, y0, var, lit, shade, cream, band, seed, mesa=(70, 190), haze=None, x0=-30, x1=SW + 30, depth=170,
             haze_clip=None):
    """One range of canyon walls across the view: flat mesa tops stepping up and down. Cliffs facing the low sun
    (west, left) catch the light; those facing away fall into shadow. Rock layers band across it all."""
    r = random.Random(seed)
    x, y = x0, y0 + r.uniform(-var, var) * 0.5
    pts = [(x, y)]
    drops, rises, tops = [], [], []
    while x < x1:
        length = r.uniform(*mesa)
        tops.append((x, x + length, y))
        x += length
        pts.append((x, y))
        target = y0 + r.uniform(-var, var)
        d = target - y
        if abs(d) < 10:
            d = 14 if r.random() < 0.5 else -14
        cliff, talus = r.uniform(4, 10), r.uniform(14, 30)
        if d > 0:
            pts += [(x + cliff, y + d * 0.68), (x + cliff + talus, y + d)]
            drops.append((x, y, x + cliff + talus, y + d))
        else:
            pts += [(x + talus, y + d * 0.32), (x + talus + cliff, y + d)]
            rises.append((x, y, x + talus + cliff, y + d))
        x += cliff + talus
        y += d
    shape = pts + [(x1, SH), (x0, SH)]
    s.add(f'<path d="{path_through(shape)}" fill="{lit}"/>')
    cid = s.id("gcl")
    s.defs.append(f'<clipPath id="{cid}"><path d="{path_through(shape)}"/></clipPath>')
    g = ""
    for a, b, c, o in ((4, 12, cream, 0.55), (20, 26, band, 0.35), (32, 36, cream, 0.22), (44, 70, band, 0.24),
                       (80, 86, "#ffffff", 0.1), (96, 104, band, 0.28), (120, 128, band, 0.2)):
        g += f'<rect x="{x0}" y="{y0 - var + a:.1f}" width="{x1 - x0}" height="{b - a}" fill="{c}" opacity="{o}"/>'
    for (xt, yt, xb, yb) in drops:
        g += (f'<path d="M{xt:.1f},{yt:.1f} L{xb:.1f},{yb:.1f} L{xb + 6:.1f},{yt + depth:.1f} L{xt - 34:.1f},{yt + depth:.1f} '
              f'L{xt - 14:.1f},{yt + 34:.1f}Z" fill="{shade}"/>')
    for _ in range(int((x1 - x0) / 34)):
        gx = r.uniform(x0, x1)
        g += (f'<path d="M{gx:.1f},{y0 + var * 0.4:.1f} q{r.uniform(-6, 6):.1f},{depth * 0.25:.1f} {r.uniform(-14, 14):.1f},{depth * 0.55:.1f}" '
              f'stroke="{shade}" stroke-width="2.4" fill="none" opacity="0.35"/>')
    s.add(f'<g clip-path="url(#{cid})">{g}</g>')
    for (xa, xb, yt) in tops:
        s.add(f'<path d="M{xa:.1f},{yt:.1f} H{xb:.1f}" stroke="#ffd9a8" stroke-width="2" opacity="0.6"/>')
    for (xt, yt, xb, yb) in rises:
        s.add(f'<path d="M{xt:.1f},{yt:.1f} L{xb:.1f},{yb:.1f}" stroke="#ffcf9a" stroke-width="2.5" opacity="0.5"/>')
    if haze:
        # Haze over this range and everything behind it, kept below the skyline (haze_clip: the farthest range's
        # outline) so the sky itself stays the page's color.
        s.add(f'<g clip-path="url(#{haze_clip or cid})"><rect x="0" y="0" width="{SW}" height="{SH}" fill="{haze[0]}" '
              f'opacity="{haze[1]}"/></g>')
    return cid


def gc_rock(s, pts, lit, shade, bands, top):
    """A piece of canyon wall: filled, with rock bands across it at heights below its top, and gullies."""
    s.add(f'<path d="{path_through(pts)}" fill="{lit}"/>')
    cid = s.id("gcr")
    s.defs.append(f'<clipPath id="{cid}"><path d="{path_through(pts)}"/></clipPath>')
    x0, x1 = min(p[0] for p in pts), max(p[0] for p in pts)
    g = "".join(f'<rect x="{x0}" y="{top + a}" width="{x1 - x0}" height="{b - a}" fill="{c}" opacity="{o}"/>' for a, b, c, o in bands)
    for _ in range(int((x1 - x0) / 30)):
        gx = s.r.uniform(x0, x1)
        g += (f'<path d="M{gx:.1f},{top + 12:.1f} q{s.r.uniform(-6, 6):.1f},{60:.1f} {s.r.uniform(-14, 14):.1f},{150:.1f}" '
              f'stroke="{shade}" stroke-width="2.6" fill="none" opacity="0.32"/>')
    s.add(f'<g clip-path="url(#{cid})">{g}</g>')


def gc_stairs(s, x0, y0, x1, y1, n, faces, ledge, talus, seed):
    """A gorge wall stepping down and inward in n steps, from its top at (x0, y0) to the river at (x1, y1)
    (x1 > x0 for the wall on the left, x1 < x0 for the one on the right): each step a lit ledge, a cliff in shadow
    and a talus slope at its foot, the rock getting a little lighter on the way down, banded with layers."""
    r = random.Random(seed)
    side = 1 if x1 > x0 else -1
    dx, dy = abs(x1 - x0) / n, (y1 - y0) / n
    x, y = x0, y0
    outline = [(x0 - side * 60, SH), (x0 - side * 60, y0), (x0, y0)]
    steps = []
    for k in range(n):
        ledge_len = dx * r.uniform(0.34, 0.5)
        cliff_dx = dx * r.uniform(0.05, 0.1)
        cliff_h = dy * r.uniform(0.62, 0.74)
        xa = x + side * ledge_len
        xb, yb = xa + side * cliff_dx, y + cliff_h
        xc, yc = x + side * dx, y + dy
        outline += [(xa, y), (xa + side * cliff_dx * 0.4 + side * r.uniform(-1.5, 1.5), y + cliff_h * 0.5), (xb, yb), (xc, yc)]
        steps.append((x, y, xa, xb, yb, xc, yc))
        x, y = xc, yc
    outline.append((x, SH))
    d = path_through(outline)
    s.add(f'<path d="{d}" fill="{faces[0]}"/>')
    cid = s.id("gcs")
    s.defs.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath>')
    lo, hi = min(x0, x1) - 80, max(x0, x1) + 80
    g = ""
    for k, (sx, sy, xa, xb, yb, xc, yc) in enumerate(steps):
        g += f'<rect x="{lo}" y="{sy:.1f}" width="{hi - lo}" height="{SH - sy:.1f}" fill="{faces[min(k, len(faces) - 1)]}"/>'
        for f_, c, o in ((0.3, "#ffffff", 0.08), (0.55, "#4a1c3a", 0.16), (0.82, "#ffffff", 0.06)):
            g += f'<rect x="{lo}" y="{sy + (yc - sy) * f_:.1f}" width="{hi - lo}" height="2.2" fill="{c}" opacity="{o}"/>'
        g += (f'<path d="M{xa:.1f},{sy:.1f} L{xb:.1f},{yb:.1f} L{xb - side * 13:.1f},{yb + 5:.1f} L{xa - side * 11:.1f},{sy + 7:.1f}Z" '
              f'fill="#3e1632" opacity="0.3"/>')
    for _ in range(int(abs(x1 - x0) / 14) + 2):
        gx = r.uniform(lo + 60, hi - 60)
        g += (f'<path d="M{gx:.1f},{y0 + 5:.1f} q{r.uniform(-4, 4):.1f},{(y1 - y0) * 0.35:.1f} {r.uniform(-7, 7):.1f},{(y1 - y0) * 0.8:.1f}" '
              f'stroke="#4a1c3a" stroke-width="1.4" fill="none" opacity="0.22"/>')
    s.add(f'<g clip-path="url(#{cid})">{g}</g>')
    for (sx, sy, xa, xb, yb, xc, yc) in steps:
        s.add(f'<path d="M{xb:.1f},{yb:.1f} L{xc:.1f},{yc:.1f} L{xb - side * 3:.1f},{yc:.1f}Z" fill="{talus}"/>')
        s.add(f'<path d="M{sx:.1f},{sy:.1f} L{xa:.1f},{sy:.1f} L{xa - side * 2:.1f},{sy + 3.5:.1f} L{sx:.1f},{sy + 3.5:.1f}Z" fill="{ledge}" opacity="0.85"/>')
        s.add(f'<path d="M{sx:.1f},{sy:.1f} L{xa:.1f},{sy:.1f}" stroke="#ffd6a8" stroke-width="1.3" opacity="0.75"/>')


def smooth(pts, k=6):
    """A Catmull-Rom curve through the points, k samples per span."""
    out = []
    p = [pts[0]] + list(pts) + [pts[-1]]
    for i in range(1, len(p) - 2):
        (x0, y0), (x1, y1), (x2, y2), (x3, y3) = p[i - 1], p[i], p[i + 1], p[i + 2]
        for j in range(k):
            t = j / k
            t2, t3 = t * t, t * t * t
            out.append((0.5 * (2 * x1 + (-x0 + x2) * t + (2 * x0 - 5 * x1 + 4 * x2 - x3) * t2 + (-x0 + 3 * x1 - 3 * x2 + x3) * t3),
                        0.5 * (2 * y1 + (-y0 + y2) * t + (2 * y0 - 5 * y1 + 4 * y2 - y3) * t2 + (-y0 + 3 * y1 - 3 * y2 + y3) * t3)))
    out.append(pts[-1])
    return out


def gc_reach(s, top, left, right, bottom, n, river, w, faces_l, faces_r, near, ledge, talus, seed, glints):
    """One reach of the inner gorge: both walls stepping down and inward to the river, the river winding along
    the bottom (its ends running under the walls), and below it the near side of the reach, which hides the river
    where it bends toward us. Reaches are drawn far to near, each nearer one hiding the lower part of the last."""
    (xl, yl), (xr, yr) = left, right
    (bl, br) = bottom
    river = smooth(river, 6)
    yb = max(y for _, y in river)
    # The river and its banks.
    s.add(f'<path d="{path_through([(x, y - w * 0.95) for x, y in river] + [(x, y + w * 1.25) for x, y in river[::-1]])}" fill="#ab7c78"/>')
    s.add(f'<path d="{path_through([(x, y - w / 2) for x, y in river] + [(x, y + w / 2) for x, y in river[::-1]])}" fill="#3f8f8a"/>')
    s.add(f'<path d="{path_through([(x, y - w * 0.15) for x, y in river], False)}" stroke="#9fe0d2" stroke-width="{max(1.2, w * 0.25):.1f}" '
          f'fill="none" opacity="0.7"/>')
    for i in range(1, len(river), 2):
        x, y = river[i]
        if bl[0] + 4 < x < br[0] - 4:
            glints.append((x, y, w))
    gc_stairs(s, xl, yl, bl[0], bl[1], n, faces_l, ledge, talus, seed)
    gc_stairs(s, xr, yr, br[0], br[1], n, faces_r, ledge, talus, seed + 1)
    # The near side, seen from above: ledges stepping back up toward us, a shadowed lip right at the water.
    lip = [(x, y + w * 1.25) for x, y in river]
    poly_pts = [(xl - 60, yb + w * 1.4), (river[0][0], yb + w * 1.4)] + lip + [(river[-1][0], yb + w * 1.4), (xr + 60, yb + w * 1.4),
                                                                                (xr + 60, SH), (xl - 60, SH)]
    s.add(f'<path d="{path_through(poly_pts)}" fill="{near}"/>')
    s.add(f'<path d="{path_through(lip, False)}" stroke="#3e1632" stroke-width="3" fill="none" opacity="0.45"/>')
    r = random.Random(seed + 7)
    for k in (1, 2):
        y = yb + w * 1.4 + k * 12 + k * k * 4
        if y < SH - 4:
            x = xl - 40
            while x < xr + 40:
                run = r.uniform(30, 90)
                if r.random() < 0.7:
                    y0 = y + r.uniform(-2, 2)
                    s.add(f'<path d="M{x:.0f},{y0:.1f} q{run / 2:.0f},{r.uniform(-3, 3):.1f} {run:.0f},{r.uniform(-2, 2):.1f}" stroke="{ledge}" '
                          f'stroke-width="2" fill="none" opacity="0.5"/>')
                    s.add(f'<path d="M{x:.0f},{y0 + 3:.1f} h{run:.0f}" stroke="#3e1632" stroke-width="2" opacity="0.2"/>')
                x += run + r.uniform(8, 30)


def gc_gorge(s):
    """The inner gorge from the rim: three reaches of it one in front of the other, each with walls stepping
    down and inward on both sides, the river winding along the bottom and slipping out of sight at the bends."""
    glints = []
    end = [(470, 566), (560, 560), (640, 560), (730, 566), (730, 660), (470, 660)]
    s.add(f'<path d="{path_through(end)}" fill="#bc8492"/>')
    for (y, c, o) in ((576, "#f4b48c", 0.6), (592, "#9a5a6c", 0.35), (606, "#ffffff", 0.12), (620, "#9a5a6c", 0.3)):
        s.add(f'<path d="M470,{y} Q600,{y - 5} 730,{y}" stroke="{c}" stroke-width="2.5" fill="none" opacity="{o}"/>')
    gc_reach(s, 572, (470, 572), (730, 572), ((586, 634), (626, 632)), 3,
             [(560, 632), (584, 638), (604, 642), (622, 636), (646, 632), (664, 636)], 3.6,
             ("#ad6672", "#b4707a", "#bc7a82"), ("#b4686a", "#bc7272", "#c47c7a"), "#b87882", "#f6b08a", "#c88a86", 71, glints)
    gc_reach(s, 596, (440, 596), (764, 596), ((560, 708), (664, 706)), 3,
             [(530, 708), (560, 716), (592, 720), (624, 712), (650, 706), (676, 712), (700, 718)], 7.0,
             ("#94485a", "#9e5264", "#a85c6c"), ("#a04c4e", "#aa5658", "#b46062"), "#a46070", "#f2a078", "#b46a6c", 73, glints)
    gc_reach(s, 640, (446, 640), (774, 640), ((588, 812), (692, 810)), 4,
             [(556, 812), (590, 822), (626, 828), (662, 820), (690, 812), (722, 818), (748, 826)], 11.5,
             ("#7c3852", "#86425c", "#904c64", "#9a566c"), ("#8e3e46", "#984850", "#a2525a", "#ac5c62"), "#94505e", "#ec9068", "#a65c64", 75, glints)
    for (x, y, w) in glints:
        anchor(s, "glint", round(x, 1), round(y, 1), round(w, 1))


def juniper(s, x, base, h, lean=1.0):
    """A gnarled juniper on the rim: a twisted pale trunk and dark, tufted foliage."""
    s.add(f'<path d="M{x - 5},{base} C{x - 8},{base - h * 0.3} {x + lean * h * 0.15},{base - h * 0.4} {x + lean * h * 0.05},{base - h * 0.62} '
          f'L{x + lean * h * 0.12},{base - h * 0.62} C{x + lean * h * 0.25},{base - h * 0.42} {x + 6},{base - h * 0.3} {x + 6},{base}Z" fill="#7a5a4a"/>')
    s.add(f'<path d="M{x},{base - h * 0.35} Q{x - lean * h * 0.2},{base - h * 0.45} {x - lean * h * 0.3},{base - h * 0.6}" stroke="#7a5a4a" '
          f'stroke-width="{h * 0.04:.1f}" fill="none"/>')
    for (dx, dy, r) in ((0.05, 0.66, 0.2), (-0.22, 0.6, 0.16), (0.22, 0.56, 0.15), (-0.06, 0.8, 0.15), (0.16, 0.76, 0.13), (-0.3, 0.7, 0.11)):
        cx, cy = x + lean * dx * h, base - dy * h
        s.add(f'<ellipse cx="{cx:.1f}" cy="{cy:.1f}" rx="{r * h * 1.2:.1f}" ry="{r * h * 0.85:.1f}" fill="#3a4d34"/>')
        s.add(f'<ellipse cx="{cx - r * h * 0.3:.1f}" cy="{cy - r * h * 0.25:.1f}" rx="{r * h * 0.7:.1f}" ry="{r * h * 0.45:.1f}" fill="#5f7a48"/>')


@theme("grand_canyon", [(0, "#2a2457"), (0.5, "#55306f"), (0.8, "#a0436a"), (1, "#d65a52")])
class GrandCanyon:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 22, 640, "#ffe9d6", 0.05)
        s.moon(898, 416, 23, crescent=True, color="#ffeccc", halo="#ffe0c0", ha=0.22)
        long_cloud(s, 96, 1160, 270, 30, "#f0886a", None, 0.55)
        long_cloud(s, 960, 1268, 290, 34, "#ff9a6a", None, 0.6)
        long_cloud(s, 40, 990, 190, 22, "#c8607a", None, 0.42)
        long_cloud(s, 1000, 1050, 160, 20, "#d86c74", None, 0.4)
        for (x, y, sc) in ((940, 700, 1.2), (972, 716, 0.9)):
            s.bird(x, y, sc, "#2a1a30", 0.75)

    @staticmethod
    def scene(s):
        sky = s.lin([(0, "#d65a52", 1), (0.5, "#f08a55", 1), (1, "#ffc77a", 1)], 0, 0, 0, 350, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{sky}"/>')
        s.sun(360, 316, 44, "#fff6dc", "#ffd27a", "#ffbd6a", 0.65)
        faded_rays(s, 360, 316, 460, "#ffd7a0", 14, 0.11, 0.05)
        long_cloud(s, 640, 190, 380, 26, "#f7a070", None, 0.6)
        long_cloud(s, 180, 140, 300, 20, "#e6806a", None, 0.5)
        long_cloud(s, 900, 250, 240, 22, "#ffb878", None, 0.55)
        # Range after range of canyon walls, each nearer one deeper in color, with haze between.
        far = gc_layer(s, 338, 4, "#b9758e", "#a06684", "#f0b29a", "#9a5a7a", 11, (160, 340), ("#ffc8a0", 0.12))
        f = s.blur(14)
        s.add(f'<ellipse cx="380" cy="344" rx="440" ry="18" fill="#ffd0a0" opacity="0.45" filter="{f}"/>')
        gc_layer(s, 362, 20, "#c47282", "#985a7c", "#f2b49c", "#9c4c62", 12, (60, 170), ("#ffc0a0", 0.08), haze_clip=far)
        gc_layer(s, 396, 26, "#d06e5e", "#8e4866", "#f6bf9c", "#a2443e", 13, (60, 160), ("#ffb898", 0.05), haze_clip=far)
        gc_layer(s, 436, 30, "#dc6e4a", "#7d3c58", "#f8c79a", "#ac4636", 14, (70, 170))
        gc_layer(s, 482, 28, "#e4733f", "#6e3052", "#fac99a", "#b24632", 15, (80, 200))
        # The inner gorge, seen from above: a bench at its lip, then walls stepping down and inward from both sides
        # as spurs, one behind the other, and the river winding along the bottom, slipping behind them at each bend.
        bench = [(300, 600), (300, 556), (380, 552), (470, 548), (560, 546), (650, 548), (740, 550), (820, 554), (900, 560), (900, 600)]
        s.add(f'<path d="{path_through(bench)}" fill="#b47c74"/>')
        s.add('<path d="M300,556 L380,552 L470,548 L560,546 L650,548 L740,550 L820,554 L900,560" stroke="#f4b48c" stroke-width="2.5" fill="none" opacity="0.75"/>')
        for x in range(310, 900, 22):
            s.add(f'<path d="M{x},{552 + s.r.uniform(2, 6):.0f} l{s.r.uniform(-4, 4):.0f},{s.r.uniform(8, 16):.0f}" stroke="#9a6466" stroke-width="2" opacity="0.5"/>')
        gc_gorge(s)
        # The near rims either side, framing the view down into the gorge.
        left = [(-10, SH), (-10, 548), (90, 544), (110, 536), (250, 538), (270, 552), (380, 556), (420, 600), (444, 660), (470, 760), (484, SH)]
        gc_rock(s, left, "#e8763e", "#6a2c4e", [(10, 20, "#fac99a", 0.5), (36, 44, "#b24632", 0.35), (60, 70, "#fac99a", 0.22),
                                               (90, 120, "#b24632", 0.25), (150, 160, "#fac99a", 0.18), (190, 200, "#b24632", 0.25)], 536)
        right = [(SW + 10, SH), (SW + 10, 512), (980, 514), (960, 522), (860, 530), (812, 556), (784, 612), (768, 680), (752, 760), (744, SH)]
        gc_rock(s, right, "#ea7a40", "#6a2c4e", [(10, 20, "#fac99a", 0.5), (36, 44, "#b24632", 0.35), (60, 70, "#fac99a", 0.22),
                                                (90, 120, "#b24632", 0.25), (150, 160, "#fac99a", 0.18), (190, 200, "#b24632", 0.25)], 512)
        s.add('<path d="M812,556 L784,612 L768,680 L752,760 L744,860 L790,860 L800,740 L820,640 L850,580 L860,530Z" fill="#ffb070" opacity="0.3"/>')
        anchor(s, "shade", 0, 340, 440)
        # The foreground rim: limestone ledges, a lookout railing, junipers and rocks.
        rim = [(-10, SH), (-10, 650), (60, 640), (150, 652), (240, 662), (320, 690), (380, 740), (420, 800), (440, SH)]
        s.add(f'<path d="{path_through(rim)}" fill="#cfa983"/>')
        s.add('<path d="M240,662 L320,690 L380,740 L420,800 L440,860 L360,860 L330,760 L280,700Z" fill="#9a7466"/>')
        for (x0, y0, x1, y1) in ((0, 690, 300, 700), (0, 740, 340, 752), (0, 800, 390, 812)):
            s.add(f'<path d="M{x0},{y0} Q{(x0 + x1) / 2},{y0 - 6} {x1},{y1}" stroke="#a88066" stroke-width="3" fill="none" opacity="0.6"/>')
        s.add('<path d="M-10,650 L60,640 L150,652 L240,662 L320,690" stroke="#f6d8b0" stroke-width="3" fill="none" opacity="0.8"/>')
        for x in range(10, 330, 44):
            y = 648 + (x / 330) * 30
            s.add(f'<rect x="{x}" y="{y - 46:.0f}" width="5" height="48" fill="#4a3c40"/>')
        s.add('<path d="M0,606 Q160,612 330,632 M0,624 Q160,630 330,650" stroke="#4a3c40" stroke-width="4" fill="none"/>')
        juniper(s, 120, 700, 150, 1)
        juniper(s, 380, 860, 120, -1)
        for (x, y, rx, ry) in ((250, 820, 50, 24), (60, 846, 70, 22), (330, 786, 30, 14)):
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{rx}" ry="{ry}" fill="#a88066"/>')
            s.add(f'<ellipse cx="{x - rx * 0.2:.0f}" cy="{y - ry * 0.35:.0f}" rx="{rx * 0.6:.0f}" ry="{ry * 0.4:.0f}" fill="#e2c09a"/>')
        s.add('<g transform="translate(236,600)" fill="#1e1620"><ellipse cx="0" cy="0" rx="13" ry="8"/><circle cx="-11" cy="-6" r="6"/>'
              '<path d="M-17,-6 L-24,-4 L-17,-3Z"/><path d="M10,-2 L26,4 L10,4Z"/><path d="M-2,7 V14 M3,7 V14" stroke="#1e1620" stroke-width="2"/></g>')
        # A rocky outcrop with a pinyon on the right.
        s.add(f'<path d="M820,{SH} Q860,760 930,730 Q990,710 {SW},716 L{SW},{SH}Z" fill="#c9966e"/>')
        s.add(f'<path d="M860,780 Q900,740 960,728 Q1000,722 {SW},726" stroke="#f0cda8" stroke-width="3" fill="none" opacity="0.7"/>')
        juniper(s, 960, 760, 170, -1)
        anchor(s, "condor", 720, 200, 0)
        anchor(s, "condor", 250, 252, 0)


# ======================= Cappadocia: hot-air balloons over the fairy chimneys at sunrise =======================
CP_SETS = [("#ff5e5b", "#ffd166", "#ffffff"), ("#3fa9f5", "#ffffff", "#ffd166"), ("#7bd389", "#ffffff", "#ff8fab"),
           ("#b07cf2", "#ffd166", "#ffffff"), ("#ff8c42", "#ffe1a8", "#e63946"), ("#e63946", "#ffffff"), ("#ffd166", "#3a86ff"),
           ("#ff70a6", "#ffffff", "#70d6ff"), ("#2ec4b6", "#ffbf69"), ("#f7b801", "#f35b04", "#ffffff")]


def cp_balloon(s, x, y, k, colors, a=1.0):
    """A balloon (props' hot_air_balloon) with a burner anchor just under its envelope; faded when far away."""
    if a < 1.0:
        s.add(f'<g opacity="{a:.2f}">')
    hot_air_balloon(s, x, y, k, colors)
    if a < 1.0:
        s.add('</g>')
    anchor(s, "burner", x, y + 75 * k, round(10 * k, 2))


def chimney(s, x, base, h, w, body, shade, cap, cap_lit, doors=(), lit=(), win="#ffcf7a", capped=True):
    """A fairy chimney: a cone of soft tuff, wide at the foot and narrowing to a neck, with a dark stone cap
    (or worn to a point without one); lit from the east (right), worn with gullies; doors and windows carved in it
    (lit ones are flickered by the app)."""
    top = base - h
    neck = base - h * (0.84 if capped else 1.0)
    nw = w * (0.11 if capped else 0.02)
    d = (f"M{x - w / 2:.1f},{base:.1f} C{x - w * 0.4:.1f},{base - h * 0.32:.1f} {x - w * 0.16:.1f},{base - h * 0.55:.1f} {x - nw:.1f},{neck:.1f} "
         f"Q{x:.1f},{neck - h * 0.03:.1f} {x + nw:.1f},{neck:.1f} C{x + w * 0.16:.1f},{base - h * 0.55:.1f} {x + w * 0.4:.1f},{base - h * 0.32:.1f} "
         f"{x + w / 2:.1f},{base:.1f}Z")
    s.add(f'<path d="{d}" fill="{body}"/>')
    cid = s.id("cpc")
    s.defs.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath>')
    g = (f'<path d="M{x - w * 0.6:.1f},{base + 2:.1f} L{x - w * 0.6:.1f},{top - 4:.1f} L{x - w * 0.01:.1f},{top - 4:.1f} '
         f'Q{x - w * 0.1:.1f},{base - h * 0.45:.1f} {x - w * 0.06:.1f},{base + 2:.1f}Z" fill="{shade}"/>')
    g += f'<rect x="{x - w}" y="{base - h * 0.42:.1f}" width="{2 * w}" height="{h * 0.05:.1f}" fill="#ffffff" opacity="0.14"/>'
    g += f'<rect x="{x - w}" y="{base - h * 0.2:.1f}" width="{2 * w}" height="{h * 0.04:.1f}" fill="#b0705a" opacity="0.18"/>'
    for k in range(5):
        gx = x - w * 0.36 + w * 0.72 * (k + 0.5) / 5
        g += (f'<path d="M{gx * 0.7 + x * 0.3:.1f},{base - h * 0.7:.1f} q{(gx - x) * 0.3:.1f},{h * 0.35:.1f} {(gx - x) * 0.55:.1f},{h * 0.7:.1f}" '
              f'stroke="{shade}" stroke-width="{max(1.0, w * 0.025):.1f}" fill="none" opacity="0.5"/>')
    s.add(f'<g clip-path="url(#{cid})">{g}</g>')
    if capped:
        cw = w * 0.25
        cb = base - h * 0.82
        s.add(f'<path d="M{x - cw:.1f},{cb:.1f} Q{x - cw * 0.7:.1f},{top + h * 0.03:.1f} {x + cw * 0.05:.1f},{top:.1f} '
              f'Q{x + cw * 0.75:.1f},{top + h * 0.03:.1f} {x + cw:.1f},{cb:.1f} Q{x:.1f},{cb + h * 0.025:.1f} {x - cw:.1f},{cb:.1f}Z" fill="{cap}"/>')
        s.add(f'<path d="M{x + cw * 0.08:.1f},{top + 1:.1f} Q{x + cw * 0.72:.1f},{top + h * 0.035:.1f} {x + cw:.1f},{cb:.1f} '
              f'Q{x + cw * 0.5:.1f},{cb + h * 0.02:.1f} {x + cw * 0.2:.1f},{cb + h * 0.02:.1f}Z" fill="{cap_lit}"/>')
    for (fx, fy, fw) in doors:
        ww, hh = w * fw, w * fw * 1.5
        cx, cy = x + w * fx, base - h * fy
        s.add(f'<path d="M{cx - ww / 2:.1f},{cy + hh / 2:.1f} V{cy - hh * 0.15:.1f} Q{cx:.1f},{cy - hh * 0.7:.1f} {cx + ww / 2:.1f},{cy - hh * 0.15:.1f} '
              f'V{cy + hh / 2:.1f}Z" fill="#6a4638"/>')
    for (fx, fy, fw) in lit:
        ww, hh = w * fw, w * fw * 1.3
        cx, cy = x + w * fx, base - h * fy
        s.glow(cx, cy, ww * 2.2, win, 0.45)
        s.add(f'<path d="M{cx - ww / 2:.1f},{cy + hh / 2:.1f} V{cy - hh * 0.1:.1f} Q{cx:.1f},{cy - hh * 0.65:.1f} {cx + ww / 2:.1f},{cy - hh * 0.1:.1f} '
              f'V{cy + hh / 2:.1f}Z" fill="{win}"/>')
        anchor(s, "window", round(cx, 1), round(cy, 1), round(ww, 1))


def tuff_ridges(s, y0, amp, n, c1, c2, groove, seed, light="#ffe6cc"):
    """A row of soft, rounded ridges of tuff, the kind that fill Rose Valley: domes with sharp gullies between
    them, grooved down their sides, the eastern faces catching the light."""
    r = random.Random(seed)
    pts = [(-20, SH), (-20, y0)]
    domes = []
    x = -20
    while x < SW + 20:
        w = r.uniform(60, 130)
        h = r.uniform(0.5, 1.0) * amp
        domes.append((x, x + w, y0 - h))
        pts.append((x + w * 0.5, y0 - h))
        pts.append((x + w, y0 - h * r.uniform(0.0, 0.25)))
        x += w
    pts.append((SW + 20, SH))
    d = f"M{pts[0][0]},{pts[0][1]} L{pts[1][0]},{pts[1][1]}"
    for (xa, xb, ty) in domes:
        d += f" C{xa + (xb - xa) * 0.1:.1f},{ty:.1f} {xa + (xb - xa) * 0.3:.1f},{ty - 8:.1f} {(xa + xb) / 2:.1f},{ty - 8:.1f}"
        d += f" C{xa + (xb - xa) * 0.7:.1f},{ty - 8:.1f} {xa + (xb - xa) * 0.9:.1f},{ty:.1f} {xb:.1f},{y0 - (y0 - ty) * 0.15:.1f}"
    d += f" L{SW + 20},{SH}Z"
    fill = s.lin([(0, c1, 1), (1, c2, 1)], 0, y0 - amp, 0, y0 + 80, "userSpaceOnUse")
    s.add(f'<path d="{d}" fill="{fill}"/>')
    cid = s.id("tuf")
    s.defs.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath>')
    g = ""
    for (xa, xb, ty) in domes:
        cx = (xa + xb) / 2
        g += f'<path d="M{xa:.1f},{y0 + 60:.1f} Q{xa + (xb - xa) * 0.2:.1f},{ty + 10:.1f} {cx:.1f},{ty - 6:.1f} L{cx:.1f},{y0 + 60:.1f}Z" fill="{groove}" opacity="0.35"/>'
        for k in range(6):
            gx = xa + (xb - xa) * (k + 0.5) / 6
            g += f'<path d="M{cx + (gx - cx) * 0.3:.1f},{ty:.1f} Q{gx:.1f},{ty + 20:.1f} {gx + (gx - cx) * 0.2:.1f},{y0 + 50:.1f}" stroke="{groove}" stroke-width="1.6" fill="none" opacity="0.45"/>'
        g += f'<path d="M{cx:.1f},{ty - 6:.1f} Q{xa + (xb - xa) * 0.8:.1f},{ty:.1f} {xb:.1f},{y0:.1f}" stroke="{light}" stroke-width="2.5" fill="none" opacity="0.6"/>'
    g += f'<rect x="0" y="{y0 - amp * 0.4:.1f}" width="{SW}" height="6" fill="#ffffff" opacity="0.12"/>'
    s.add(f'<g clip-path="url(#{cid})">{g}</g>')


def castle_rock(s, x, base, w, h):
    """A great rock riddled with cave rooms, like Uçhisar's castle: a rounded crag, doors and windows all over it,
    some lit, a flag of light on its eastern side."""
    top = base - h
    d = (f"M{x - w / 2:.1f},{base:.1f} C{x - w * 0.5:.1f},{base - h * 0.5:.1f} {x - w * 0.32:.1f},{top + h * 0.12:.1f} {x - w * 0.12:.1f},{top + h * 0.04:.1f} "
         f"Q{x - w * 0.05:.1f},{top - h * 0.03:.1f} {x + w * 0.04:.1f},{top:.1f} Q{x + w * 0.2:.1f},{top + h * 0.02:.1f} {x + w * 0.26:.1f},{top + h * 0.16:.1f} "
         f"C{x + w * 0.42:.1f},{top + h * 0.3:.1f} {x + w * 0.52:.1f},{base - h * 0.35:.1f} {x + w / 2:.1f},{base:.1f}Z")
    s.add(f'<path d="{d}" fill="#e9bf9c"/>')
    cid = s.id("cas")
    s.defs.append(f'<clipPath id="{cid}"><path d="{d}"/></clipPath>')
    g = f'<path d="M{x - w * 0.6:.1f},{base:.1f} L{x - w * 0.6:.1f},{top - 6:.1f} L{x - w * 0.02:.1f},{top - 6:.1f} Q{x - w * 0.15:.1f},{base - h * 0.5:.1f} {x - w * 0.1:.1f},{base:.1f}Z" fill="#c38a6e"/>'
    for k in range(7):
        y = top + h * (0.18 + k * 0.11)
        g += f'<path d="M{x - w * 0.6:.1f},{y:.1f} Q{x:.1f},{y - 6:.1f} {x + w * 0.6:.1f},{y:.1f}" stroke="#b07a62" stroke-width="2" fill="none" opacity="0.4"/>'
    s.add(f'<g clip-path="url(#{cid})">{g}</g>')
    holes = []
    for k in range(40):
        hx = x + s.r.uniform(-0.38, 0.38) * w
        hy = top + h * s.r.uniform(0.12, 0.9)
        rel = (hx - x) / w
        span = 0.45 * (1 - ((hy - top) / h - 0.55) ** 2 * 0.6)
        if abs(rel) < span * 0.8 and all(abs(hx - a) > 13 or abs(hy - b) > 15 for a, b in holes):
            holes.append((hx, hy))
    for i, (hx, hy) in enumerate(holes):
        ww = s.r.uniform(7, 11)
        hh = ww * s.r.uniform(1.3, 1.9)
        arch = (f"M{hx - ww / 2:.1f},{hy + hh / 2:.1f} V{hy - hh * 0.1:.1f} Q{hx:.1f},{hy - hh * 0.75:.1f} {hx + ww / 2:.1f},{hy - hh * 0.1:.1f} "
                f"V{hy + hh / 2:.1f}Z")
        s.add(f'<path d="M{hx - ww / 2 - 2:.1f},{hy + hh / 2 + 2:.1f} H{hx + ww / 2 + 2:.1f}" stroke="#f6d6b6" stroke-width="2"/>')
        if i % 6 == 2:
            s.glow(hx, hy, ww * 2.4, "#ffcf7a", 0.45)
            s.add(f'<path d="{arch}" fill="#ffcf7a"/>')
            anchor(s, "window", round(hx, 1), round(hy, 1), round(ww, 1))
        else:
            s.add(f'<path d="{arch}" fill="#6a4638"/>')


def lantern(s, x, base, k=1.0):
    """A metal lantern standing on the parapet, lit."""
    s.glow(x, base - 22 * k, 46 * k, "#ffcf7a", 0.5)
    s.add(f'<path d="M{x - 10 * k},{base} H{x + 10 * k} L{x + 8 * k},{base - 6 * k} H{x - 8 * k}Z" fill="#3a2a2a"/>'
          f'<rect x="{x - 8 * k}" y="{base - 34 * k}" width="{16 * k}" height="{28 * k}" fill="#ffcf7a"/>'
          f'<path d="M{x - 8 * k},{base - 34 * k} V{base - 6 * k} M{x},{base - 34 * k} V{base - 6 * k} M{x + 8 * k},{base - 34 * k} V{base - 6 * k}" '
          f'stroke="#3a2a2a" stroke-width="{1.6 * k:.1f}"/>'
          f'<path d="M{x - 11 * k},{base - 34 * k} L{x},{base - 46 * k} L{x + 11 * k},{base - 34 * k}Z" fill="#3a2a2a"/>'
          f'<circle cx="{x}" cy="{base - 49 * k}" r="{3 * k}" fill="none" stroke="#3a2a2a" stroke-width="{1.5 * k:.1f}"/>')
    anchor(s, "window", x, base - 20 * k, 16 * k)


@theme("cappadocia", [(0, "#3d6fb0"), (0.5, "#6a93cf"), (0.82, "#9aa8d6"), (1, "#c2a8c8")])
class Cappadocia:
    @staticmethod
    def deco(s):
        for (x, y, k, c) in ((112, 414, 0.95, 0), (928, 356, 0.7, 1), (44, 612, 0.46, 2), (994, 548, 0.44, 3), (40, 1176, 0.4, 4),
                             (995, 1010, 0.42, 7), (150, 1342, 0.2, 5), (884, 1318, 0.22, 6), (62, 1296, 0.16, 8), (968, 1220, 0.17, 9)):
            cp_balloon(s, x, y, k, CP_SETS[c], 1.0 if k > 0.3 else 0.85)

    @staticmethod
    def scene(s):
        sky = s.lin([(0, "#c2a8c8", 1), (0.5, "#eab4b0", 1), (1, "#ffd6a2", 1)], 0, 0, 0, 430, "userSpaceOnUse")
        s.add(f'<rect width="{SW}" height="{SH}" fill="{sky}"/>')
        s.sun(806, 392, 50, "#fff8e8", "#ffdb98", "#ffd0a0", 0.65)
        faded_rays(s, 806, 392, 470, "#ffe2b0", 14, 0.11, 0.05)
        # Far balloons in the dawn sky.
        for (x, y, k, c) in ((470, 120, 0.24, 1), (560, 210, 0.17, 3), (640, 150, 0.2, 0), (360, 260, 0.15, 8), (900, 210, 0.22, 4),
                             (980, 300, 0.14, 2), (700, 300, 0.13, 6), (150, 330, 0.13, 9), (420, 360, 0.11, 5), (760, 120, 0.15, 7),
                             (300, 180, 0.12, 2), (840, 330, 0.1, 9)):
            cp_balloon(s, x, y, k, CP_SETS[c], 0.8)
        # Mount Erciyes far off, then the far plateau.
        s.add('<path d="M560,430 L640,404 Q690,370 720,362 Q745,370 780,400 L860,430Z" fill="#cfa6c0"/>'
              '<path d="M704,368 Q720,360 736,368 L744,378 Q722,372 700,380Z" fill="#f7f0f8" opacity="0.85"/>')
        s.add('<path d="M0,440 L0,404 Q80,398 160,406 Q240,396 300,404 L330,412 Q420,404 520,414 Q640,406 760,418 Q880,410 1032,416 L1032,440Z" fill="#c9a0b8"/>')
        f = s.blur(12)
        s.add(f'<ellipse cx="780" cy="420" rx="320" ry="16" fill="#ffd8b0" opacity="0.5" filter="{f}"/>')
        # Rose Valley: rows of soft grooved ridges, nearer ones warmer.
        tuff_ridges(s, 452, 30, 0, "#e2b4b4", "#d8a8a8", "#b98a92", 41)
        for k in range(22):
            x = 10 + k * 48 + s.r.uniform(-12, 12)
            chimney(s, x, 470 + s.r.uniform(-4, 6), s.r.uniform(36, 70), s.r.uniform(20, 32), "#e8bfb0", "#cf9f9c", "#a8888e", "#bfa0a4",
                    capped=s.r.random() < 0.6)
        tuff_ridges(s, 506, 44, 0, "#efc2ac", "#e2ad98", "#c48c82", 42)
        castle_rock(s, 150, 600, 250, 230)
        # Chimneys in clusters through the middle of the valley, doors carved in some.
        clusters = [(330, 590, [(-40, 130, 54), (0, 176, 66), (44, 120, 50), (82, 90, 40)]),
                    (560, 584, [(-50, 112, 50), (-6, 160, 62), (40, 136, 56)]),
                    (760, 592, [(-36, 150, 60), (10, 104, 46), (52, 170, 64), (96, 96, 42)])]
        i = 0
        for (cx, base, members) in clusters:
            for (dx, h, w) in members:
                doors = [(-0.04, 0.26, 0.16)] if i % 2 == 0 else [(0.08, 0.4, 0.12)]
                chimney(s, cx + dx, base + s.r.uniform(-4, 4), h, w, "#f0c6a2", "#cc9677", "#86625a", "#a67e70", doors=doors,
                        lit=[(0.1, 0.2, 0.14)] if i in (1, 6, 9) else (), capped=i % 3 != 2)
                i += 1
        # The valley floor: poplars, apricot trees, vines and a dirt road.
        s.add(f'<path d="M0,{SH} L0,606 Q180,592 360,602 Q540,594 720,604 Q880,596 {SW},602 L{SW},{SH}Z" fill="#e3b892"/>')
        s.add('<path d="M470,860 Q480,760 540,700 Q600,650 700,622" stroke="#efd2ae" stroke-width="16" fill="none" stroke-linecap="round" opacity="0.85"/>')
        for k in range(8):
            y = 640 + k * 9
            s.add(f'<path d="M{620 + k * 6},{y} H{880 - k * 4}" stroke="#7d9a58" stroke-width="3.5" stroke-dasharray="7 5" opacity="0.75"/>')
            s.add(f'<path d="M{120 + k * 4},{y + 20} H{360 - k * 8}" stroke="#7d9a58" stroke-width="3.5" stroke-dasharray="7 5" opacity="0.7"/>')
        for _ in range(18):
            x, y = s.r.uniform(0, SW), s.r.uniform(600, 690)
            if s.r.random() < 0.5:
                s.add(f'<ellipse cx="{x:.0f}" cy="{y - 24:.0f}" rx="7" ry="26" fill="#6f8a55"/><ellipse cx="{x + 2:.0f}" cy="{y - 28:.0f}" rx="4" ry="18" fill="#8aa868"/>')
            else:
                s.add(f'<circle cx="{x:.0f}" cy="{y - 12:.0f}" r="13" fill="#76935a"/><circle cx="{x + 3:.0f}" cy="{y - 16:.0f}" r="8" fill="#93b070"/>'
                      f'<rect x="{x - 1.5:.0f}" y="{y - 4:.0f}" width="3" height="8" fill="#6a4a3a"/>')
        # Balloons low over the valley, at different distances.
        for (x, y, k, c) in ((262, 456, 0.3, 2), (470, 486, 0.26, 7), (668, 452, 0.36, 0), (902, 498, 0.28, 3)):
            cp_balloon(s, x, y, k, CP_SETS[c])
        # Big chimneys close by on either side, lived in, with lit windows.
        chimney(s, 70, 770, 300, 150, "#f3c99f", "#c98c66", "#6e4e46", "#8a6658",
                doors=[(-0.1, 0.24, 0.16), (0.14, 0.42, 0.1)], lit=[(0.12, 0.2, 0.13), (-0.06, 0.5, 0.1)])
        chimney(s, 196, 780, 200, 112, "#f1c49a", "#c58964", "#6e4e46", "#8a6658", doors=[(0.0, 0.3, 0.16)], lit=[(0.15, 0.5, 0.12)],
                capped=False)
        chimney(s, 960, 776, 340, 160, "#f3c99f", "#c98c66", "#6e4e46", "#8a6658",
                doors=[(0.12, 0.28, 0.15)], lit=[(-0.06, 0.18, 0.13), (0.1, 0.46, 0.1), (-0.1, 0.6, 0.08)])
        chimney(s, 836, 780, 210, 112, "#f1c49a", "#c58964", "#6e4e46", "#8a6658", doors=[(-0.05, 0.32, 0.15)], lit=[(0.12, 0.2, 0.12)])
        # A close balloon drifting up past it all.
        cp_balloon(s, 560, 250, 0.72, CP_SETS[4])
        # The terrace in front: a stone parapet with a kilim over it, lanterns and geraniums.
        s.add(f'<rect x="0" y="770" width="{SW}" height="{SH - 770}" fill="#d9b48c"/>')
        s.add(f'<rect x="0" y="762" width="{SW}" height="16" fill="#e8c9a2"/>')
        s.add(f'<rect x="0" y="776" width="{SW}" height="4" fill="#b8916c"/>')
        for x in range(0, SW, 46):
            s.add(f'<path d="M{x},{780} V{SH}" stroke="#c7a07a" stroke-width="2" opacity="0.6"/>')
        for y in range(800, SH, 26):
            s.add(f'<path d="M0,{y} H{SW}" stroke="#c7a07a" stroke-width="2" opacity="0.5"/>')
        s.add('<path d="M380,762 H650 V842 H380Z" fill="#b8322a"/>')
        for k in range(5):
            y = 776 + k * 13
            s.add(f'<path d="M392,{y} ' + " ".join(f"L{392 + j * 16},{y + (6 if j % 2 else -2)}" for j in range(1, 16)) +
                  f'" stroke="{("#ffd166", "#1d3557", "#ffffff", "#f4a261", "#2a9d8f")[k]}" stroke-width="3" fill="none"/>')
        for j in range(17):
            s.add(f'<path d="M{384 + j * 16},842 v10" stroke="#e9c46a" stroke-width="2"/>')
        lantern(s, 300, 762, 1.1)
        lantern(s, 730, 762, 1.0)
        for (x, sc) in ((120, 1.0), (880, 1.1), (190, 0.8)):
            s.add(f'<path d="M{x - 22 * sc},{762} L{x + 22 * sc},{762} L{x + 16 * sc},{762 + 34 * sc} L{x - 16 * sc},{762 + 34 * sc}Z" fill="#b5603a" '
                  f'transform="translate(0,{-34 * sc:.1f})"/>')
            for _ in range(7):
                fx, fy = x + s.r.uniform(-22, 22) * sc, 762 - 34 * sc - s.r.uniform(4, 26) * sc
                s.add(f'<circle cx="{fx:.1f}" cy="{fy:.1f}" r="{7 * sc:.1f}" fill="#3f7a3a"/>')
            for _ in range(5):
                fx, fy = x + s.r.uniform(-18, 18) * sc, 762 - 34 * sc - s.r.uniform(14, 34) * sc
                s.blossom(fx, fy, 6 * sc, "#e63946", "#ffd166")
        for (x, y, k) in ((300, 640, 0.42), (560, 650, 0.4), (760, 620, 0.36), (440, 610, 0.32)):
            anchor(s, "rise", x, y, k)
