"""Every theme's page art: its sky (top of the page down to the scene), the decorations in that sky, and the scene
along the bottom. Scenes draw in band coordinates (0..SW, 0..SH); decorations in page coordinates."""
from props import *  # noqa: F401,F403

Front = None  # set by render.py: `with Front(s):` draws in front of the scene band

THEMES = {}


def theme(key, sky):
    def wrap(cls):
        THEMES[key] = {"sky": sky, "scene": cls.scene, "deco": cls.deco}
        return cls
    return wrap


AUTUMN = [("#8f2a1c", "#c8462a", "#ea6a45"), ("#b9531f", "#e07b2a", "#f6a94a"), ("#a9741f", "#dba236", "#f5cd68")]
LEAVES = ["#c8462a", "#e07b2a", "#b5341f", "#f0a43a", "#d9622a", "#a8321c"]


# ======================= Thanksgiving: a harvest at golden hour =======================
@theme("thanksgiving", [(0, "#24132f"), (0.5, "#4a1e3c"), (0.82, "#82303f"), (1, "#a8443d")])
class Thanksgiving:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 36, 760, "#ffe6c9", 0.06)
        for _ in range(34):
            p = spot(s.r, TOP_CLEAR + 30, BAND_Y + 60, 26)
            if p:
                maple(s, p[0], p[1], s.r.uniform(13, 26), s.r.choice(LEAVES), s.r.uniform(0, 360), s.r.uniform(0.75, 1))
        geese(s, 858, 560, 5, 0.95, "#1d0f22", 0.75, 30)
        long_cloud(s, 120, 1180, 300, 50, "#b5485a", None, 0.55)
        long_cloud(s, 930, 1290, 280, 44, "#c4584f", None, 0.6)
        with Front(s):
            for _ in range(8):
                x, y = edge(s.r, BAND_Y - 40, BAND_Y + 180, 0.0)
                maple(s, x, y, s.r.uniform(16, 26), s.r.choice(LEAVES), s.r.uniform(0, 360))

    @staticmethod
    def scene(s):
        s.sky("#a8443d", "#cf5f3a", "#ee8c3e", "#ffc76a", "#ffd98c")
        s.sun(690, 478, 74, "#fff6d6", "#ffd27a", "#ffc46b", 0.75)
        rays(s, 690, 478, 560, "#ffe7b0", 14, 0.22, 0.05)
        long_cloud(s, 250, 250, 420, 60, "#f39a6a", "#c9566a", 0.75)
        long_cloud(s, 800, 330, 380, 46, "#ffb877", "#e0745e", 0.7)
        long_cloud(s, 480, 140, 300, 40, "#e2766a", "#b54c64", 0.55)
        geese(s, 300, 240, 9, 1.15, "#3a1620", 0.8, 34)
        # far hills, hazy in the evening light
        hill(s, 505, 22, 0.9, 0.4, "#a4505e", "#8a3f55")
        for x in range(30, SW, 26):
            y = s.ridge_y(505, 22, 0.9, 0.4, x) + 6
            if s.r.random() < 0.55:
                s.add(f'<ellipse cx="{x:.0f}" cy="{y - 9:.0f}" rx="{s.r.uniform(8, 13):.0f}" ry="{s.r.uniform(10, 16):.0f}" '
                      f'fill="#7d3550" opacity="0.8"/>')
        hill(s, 560, 26, 0.7, 2.2, "#a4462f", "#7a3120")
        # the farmhouse, home for dinner
        hx = 770
        hb = s.ridge_y(560, 26, 0.7, 2.2, hx) + 14
        house(s, hx, hb, 150, 92, "#efdcbc", "#6a2a2a", "#ffcf6a", True, "#6a3a24", True, True, False, "#fff5e0",
              shade="#8a5a3a")
        leafy_tree(s, 905, hb + 6, 190, AUTUMN[0], "#4a2a1a")
        leafy_tree(s, 640, s.ridge_y(560, 26, 0.7, 2.2, 640) + 14, 130, AUTUMN[2], "#4a2a1a")
        leafy_tree(s, 330, s.ridge_y(560, 26, 0.7, 2.2, 330) + 14, 150, AUTUMN[1], "#4a2a1a")
        leafy_tree(s, 130, s.ridge_y(560, 26, 0.7, 2.2, 130) + 18, 230, AUTUMN[0], "#4a2a1a", -6)
        leafy_tree(s, 230, s.ridge_y(560, 26, 0.7, 2.2, 230) + 18, 170, AUTUMN[2], "#4a2a1a")
        # field rows and the fence
        hill(s, 640, 14, 0.5, 1.0, "#8a3e1f", "#6b2c15")
        for k in range(7):
            y = 660 + k * 22
            s.add(f'<path d="M-20,{y} Q{SW / 2},{y - 30 + k * 2} {SW + 20},{y}" fill="none" stroke="#a24f26" '
                  f'stroke-width="5" opacity="0.5"/>')
        fence(s, -10, SW + 10, 676, "#5a3018", 50, 44, "#3a1d0e")
        # the harvest out front
        hill(s, 748, 12, 0.6, 3.1, "#6e3218", "#4a200f")
        tufts(s, 748, 12, 0.6, 3.1, "#a85f2a", 70, (10, 20))
        corn_shock(s, 92, 840, 300)
        corn_shock(s, 960, 846, 260)
        hay_bale(s, 175, 840, 62)
        hay_bale(s, 800, 846, 52)
        cornucopia(s, 548, 846, 272)
        pumpkin2(s, 275, 852, 46)
        pumpkin2(s, 345, 858, 30, "#d8cdb2", "#f1e8d4", "#6b6b3a")
        gourd(s, 700, 852, 20, "#5d7f3a", "#7fa04a", "#e8e0b0")
        pumpkin2(s, 880, 856, 38, "#c9561d", "#ea8a34")
        gourd(s, 930, 858, 16, "#e8b23a", "#f5d06a", None, 1.4)
        for _ in range(14):
            maple(s, s.r.uniform(20, SW - 20), s.r.uniform(60, 470), s.r.uniform(10, 18), s.r.choice(LEAVES),
                  s.r.uniform(0, 360), 0.9)
        for _ in range(20):
            maple(s, s.r.uniform(0, SW), s.r.uniform(800, 860), s.r.uniform(9, 14), s.r.choice(LEAVES), s.r.uniform(0, 360))


def scatter(s, n, y0, y1, pad, draw, center=0.08):
    """Calls draw(x, y) at up to n spots in the page's sky, near the edges and clear of the pass. Ones that would be cut
    off by the top of the scene are left out (still drawn, into nothing, so the rest stay where they were)."""
    for _ in range(n):
        p = spot(s.r, y0, y1, pad, center)
        if p and p[1] > BAND_Y - pad * 1.2:
            kept = s.body
            s.body = []
            draw(*p)
            s.body = kept
        elif p:
            draw(*p)


# ======================= Halloween: a haunted hill under a full moon =======================
@theme("halloween", [(0, "#0d0820"), (0.6, "#1b1033"), (1, "#2a1546")])
class Halloween:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 150, BAND_Y - 20, "#ffffff", 0.08)
        spider_web(s, 0, 300, 170, "#cfc6ea", 0.4, 0, 90)
        spider(s, 118, 470, 12, 360)
        spider_web(s, SW, 300, 120, "#cfc6ea", 0.35, 90, 90)
        scatter(s, 12, TOP_CLEAR + 40, BAND_Y + 40, 40, lambda x, y: s.bat(x, y, s.r.uniform(1.0, 1.9), "#2e1a4a", s.r.uniform(-12, 12)))

    @staticmethod
    def scene(s):
        s.sky("#2a1546", "#4b2366", "#8a3b6a", "#e2733a")
        s.stars(50, 300)
        s.moon(720, 235, 125, color="#f8ecc9")
        witch(s, 640, 190, 1.1, "#140a22")
        for (x, y, sc, rot) in ((520, 120, 2.0, -10), (590, 70, 1.3, 8), (880, 120, 1.6, 6), (840, 330, 1.1, -6), (420, 230, 1.2, 12)):
            s.bat(x, y, sc, "#140b22", rot)
        hill(s, 478, 24, 0.9, 0.6, "#3a1d4f", "#2a1540")
        haunted_house(s, 770, s.ridge_y(478, 24, 0.9, 0.6, 770) + 12, 0.82, "#160b24")
        s.mist(520, 0.16)
        hill(s, 575, 24, 0.7, 2.4, "#26143a", "#1c0f2c")
        base = lambda x: s.ridge_y(575, 24, 0.7, 2.4, x) + 10
        s.bare_tree(150, base(150), 420, "#120a1c", depth=6)
        s.bare_tree(985, base(985), 300, "#120a1c", depth=5)
        spider_web(s, 186, 330, 64, "#d9d2ef", 0.5, 0, 360)
        spider(s, 960, 470, 11, 330)
        for (x, w, h, kind) in ((300, 46, 74, 0), (372, 40, 92, 1), (440, 52, 70, 2), (520, 44, 86, 0), (600, 50, 78, 1),
                                (676, 42, 68, 0)):
            tombstone(s, x, base(x) + 22, w, h, "#5c5470", "#433c56", kind)
        iron_fence(s, 240, 730, 690, 64, "#120a1c")
        s.mist(668, 0.2)
        hill(s, 728, 14, 0.6, 4.0, "#170b25", "#0d0616")
        tufts(s, 728, 14, 0.6, 4.0, "#2e1a40", 60, (10, 18))
        ghost(s, 880, 600, 110, 0.9, 8)
        ghost(s, 70, 640, 64, 0.75, -10)
        pumpkin2(s, 180, 852, 44)
        pumpkin2(s, 375, 848, 56, face=True)
        pumpkin2(s, 492, 856, 38, face=True)
        pumpkin2(s, 608, 850, 50, face=True)
        pumpkin2(s, 860, 852, 40)
        pumpkin2(s, 945, 858, 28, "#c9561d", "#ea8a34")


# ======================= Día de los Muertos: an ofrenda under papel picado =======================
@theme("dia_de_muertos", [(0, "#1a0828"), (0.6, "#2a0f3d"), (1, "#3d1250")])
class DiaDeMuertos:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 70, BAND_Y - 20, "#ffe9c4", 0.06)

        def bloom(x, y):
            if s.r.random() < 0.35:
                marigold(s, x, y, s.r.uniform(9, 15))
            else:
                s.petal(x, y, s.r.uniform(0.8, 1.4), s.r.choice(["#ffb21e", "#ff8c1a", "#ffd34d"]), s.r.uniform(0, 180))
        scatter(s, 70, TOP_CLEAR + 20, BAND_Y + 40, 18, bloom)
        string_lights(s, -10, 330, 300, 30, ["#ffb02e", "#f25f8a", "#55c1f0", "#7bd389"], 7, 0.35)
        string_lights(s, 700, SW + 10, 300, 30, ["#7bd389", "#ffb02e", "#f25f8a", "#55c1f0"], 7, 0.35)

    @staticmethod
    def scene(s):
        s.sky("#3d1250", "#6b1f5c", "#b23c6c", "#f08a4b")
        s.stars(40, 260, "#ffe9c4")
        s.papel(40, SW - 40, 46, ["#f25f8a", "#ffb02e", "#7bd389", "#55c1f0", "#b07cf2", "#ff7a45", "#f25f8a", "#ffb02e"], h=84)
        hill(s, 450, 18, 1.0, 1.0, "#5a1f4f", "#4a1842")
        cx, cb = 860, s.ridge_y(450, 18, 1.0, 1.0, 860) + 6
        s.add(f'<path d="M{cx - 70},{cb} V{cb - 70} H{cx + 40} V{cb}Z M{cx + 40},{cb} V{cb - 130} H{cx + 80} V{cb}Z '
              f'M{cx - 50},{cb - 70} Q{cx - 15},{cb - 125} {cx + 20},{cb - 70}Z M{cx + 56},{cb - 130} V{cb - 152} '
              f'M{cx + 48},{cb - 144} H{cx + 64}" fill="#3a1236" stroke="#3a1236" stroke-width="4"/>')
        for (wx, wy) in ((cx - 50, cb - 40), (cx - 20, cb - 40), (cx + 52, cb - 100), (cx + 52, cb - 60)):
            s.glow(wx + 6, wy + 8, 16, "#ffcf6a", 0.5)
            s.add(f'<rect x="{wx}" y="{wy}" width="12" height="16" rx="5" fill="#ffcf6a"/>')
        for _ in range(26):
            x = s.r.uniform(20, SW - 20)
            y = s.ridge_y(450, 18, 1.0, 1.0, x) + s.r.uniform(8, 50)
            s.add(f'<circle cx="{x:.0f}" cy="{y:.0f}" r="2.4" fill="#ffcf6a" opacity="0.8"/>')
        hill(s, 540, 18, 0.7, 3.0, "#3a1236", "#2a0c28")
        for _ in range(90):
            t = s.r.uniform(0, 1)
            x = 516 + (s.r.uniform(-1, 1) * (60 + 200 * t))
            y = 860 - 260 * (1 - t) ** 0.5 * 0 - (1 - t) * 0 + s.r.uniform(-8, 8) + 600 + 250 * t
            s.petal(x, y, s.r.uniform(0.8, 1.3), s.r.choice(["#ffb21e", "#ff8c1a", "#ffd34d"]), s.r.uniform(0, 180))
        ofrenda(s, 516, 812, 560)
        for (x, y) in ((70, 790), (130, 820), (40, 840), (200, 846), (100, 860)):
            marigold(s, x, y, 26)
        for (x, y) in ((962, 790), (902, 822), (990, 838), (836, 848), (930, 860)):
            marigold(s, x, y, 26)
        for (x, h) in ((210, 70), (250, 96), (790, 84), (830, 60)):
            s.candle(x, 846, h, 20)


# ======================= Christmas: a snowy village on Christmas Eve =======================
@theme("christmas", [(0, "#050c1f"), (0.6, "#0a1834"), (1, "#0c1b3a")])
class Christmas:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 120, BAND_Y - 20, "#ffffff", 0.06)
        scatter(s, 22, TOP_CLEAR + 20, BAND_Y + 40, 24, lambda x, y: snowflake(s, x, y, s.r.uniform(9, 20), "#ffffff",
                                                                                s.r.uniform(0.6, 0.95), s.r.uniform(0, 60)))
        scatter(s, 140, TOP_CLEAR, BAND_Y + 40, 4, lambda x, y: s.add(
            f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(1.5, 4):.1f}" fill="#ffffff" opacity="{s.r.uniform(0.4, 0.9):.2f}"/>'), 0.25)

    @staticmethod
    def scene(s):
        s.sky("#0c1b3a", "#1d3566", "#3b5d93", "#5d7fb3")
        s.stars(70, 340)
        s.moon(800, 200, 122, color="#fbf3dc")
        sleigh(s, 868, 196, 0.62, "#0a1430")
        hill(s, 430, 24, 0.8, 1.5, "#a9c0de", "#93acd0")
        for x in range(10, SW, 30):
            if s.r.random() < 0.7:
                pine2(s, x + s.r.uniform(-8, 8), s.ridge_y(430, 24, 0.8, 1.5, x) + 14, s.r.uniform(50, 80), "#5f7aa3", "#7390b8")
        hill(s, 520, 20, 0.6, 3.3, "#dbe7f6", "#c3d5ec")
        gb = lambda x: s.ridge_y(520, 20, 0.6, 3.3, x) + 30
        lights = ["#ff4d5e", "#ffd36b", "#4cd17a", "#63d6ff"]
        for (x, w, h, wall, roof) in ((170, 180, 112, "#8a3b2e", "#3a2a3a"), (395, 140, 92, "#2f5a7a", "#2a2a3a"),
                                      (880, 190, 118, "#4f7a3a", "#3a2a2a")):
            house(s, x, gb(x), w, h, wall, roof, "#ffd27a", True, "#3a2014", True, True, True, "#f7f1e3", shade="#000000")
            string_lights(s, x - w * 0.55, x, gb(x) - h - h * 0.6 + 8, -4, lights, 5, 0.4)
            string_lights(s, x, x + w * 0.55, gb(x) - h - h * 0.6 + 8, -4, lights[::-1], 5, 0.4)
        for x in (40, 280, 520, 760, 1000):
            pine2(s, x, gb(x) + 40, s.r.uniform(120, 170), "#1f4a3a", "#2c6a50", "#ffffff")
        hill(s, 700, 12, 0.5, 0.5, "#f4f8fd", "#e2ecf7")
        hill(s, 820, 10, 0.5, 2.0, "#ffffff", "#eef4fb")
        xmas_tree(s, 620, 800, 360)
        for (dx, w, h, c, rb) in ((-120, 70, 52, "#e63946", "#ffd36b"), (-50, 58, 70, "#3fa9f5", "#ffffff"), (40, 76, 56, "#2c8a57", "#ff4d5e"),
                                  (115, 54, 44, "#b07cf2", "#ffd36b"), (-170, 46, 38, "#ffd36b", "#e63946")):
            present(s, 620 + dx, 836, w, h, c, rb, "#000000")
        snowman(s, 160, 830, 230)
        s.add('<rect x="928" y="560" width="12" height="270" fill="#1d2433"/>')
        s.glow(934, 548, 70, "#ffd27a", 0.55)
        s.add('<path d="M914,560 H954 L948,520 H920Z" fill="#1d2433"/><rect x="922" y="526" width="24" height="30" fill="#ffd27a"/>')
        s.add('<circle cx="934" cy="640" r="34" fill="none" stroke="#1f6b45" stroke-width="16"/>')
        for i in range(10):
            a = 2 * math.pi * i / 10
            s.add(f'<circle cx="{934 + 34 * math.cos(a):.1f}" cy="{640 + 34 * math.sin(a):.1f}" r="4" fill="#ff4d5e"/>')
        s.add('<path d="M934,672 l-16,18 l8,2 l8,-14 l8,14 l8,-2Z" fill="#e63946"/>')
        s.snow(150, 0, SH, (0.5, 0.95), (1.5, 4.5))
        for _ in range(12):
            snowflake(s, s.r.uniform(20, SW - 20), s.r.uniform(20, 500), s.r.uniform(7, 13), "#ffffff", 0.8, s.r.uniform(0, 60))


# ======================= New Year's: fireworks over the river at midnight =======================
@theme("new_year", [(0, "#03050f"), (0.6, "#070b1f"), (1, "#0f1433")])
class NewYear:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 110, BAND_Y - 20, "#ffffff", 0.06)
        conf = ["#ffd36b", "#ff7ac0", "#63d6ff", "#ffffff", "#b88cff"]
        scatter(s, 110, TOP_CLEAR + 10, BAND_Y + 60, 10, lambda x, y: s.add(
            f'<rect x="{x:.1f}" y="{y:.1f}" width="{s.r.uniform(6, 13):.1f}" height="{s.r.uniform(3, 6):.1f}" '
            f'fill="{s.r.choice(conf)}" opacity="0.9" transform="rotate({s.r.uniform(0, 180):.0f} {x:.1f} {y:.1f})"/>'), 0.2)
        burst_at = [(110, 420, 78, "#ffd36b", "#fff3c4"), (930, 460, 70, "#ff7ac0", "#ffd1ea")]
        for (x, y, r, c1, c2) in burst_at:
            s.burst(x, y, r, c1, c2, 20, 0.9)
        streamer(s, 20, 640, 150, "#ffd36b")
        streamer(s, 880, 1200, 130, "#63d6ff")
        streamer(s, 900, 700, 120, "#ff7ac0")
        scatter(s, 10, TOP_CLEAR + 30, BAND_Y, 30, lambda x, y: s.sparkle(x, y, s.r.uniform(7, 12), "#fff3c4", 0.9))

    @staticmethod
    def scene(s):
        s.sky("#0f1433", "#1b1f4f", "#2c2a66", "#3a2f72")
        s.stars(50, 420)
        s.burst(210, 180, 125, "#ffd36b", "#fff3c4", 28)
        willow(s, 520, 150, 135, "#ffb3d9", 22)
        ring_burst(s, 830, 200, 105, "#63d6ff")
        s.burst(390, 330, 72, "#b88cff", "#eadcff", 18)
        s.burst(950, 380, 62, "#ffd36b", "#fff3c4", 16)
        s.burst(690, 360, 54, "#4cd17a", "#d6ffe6", 16)
        s.skyline(650, "#1a1d44", "#ffd27a", 0.22, (150, 330))
        clock_tower(s, 300, 650, 74, 360, "#0a0d24")
        s.skyline(660, "#0a0d24", "#ffd98a", 0.32, (90, 230))
        s.water(660, "#141a46", "#0a0d26", "#ffd27a")
        for (x, c) in ((210, "#ffd36b"), (520, "#ffb3d9"), (830, "#63d6ff")):
            s.glow(x, 720, 110, c, 0.22)
            for k in range(6):
                s.add(f'<rect x="{x - 50 + s.r.uniform(0, 40):.1f}" y="{676 + k * 26}" width="{s.r.uniform(30, 80):.1f}" '
                      f'height="3" rx="1.5" fill="{c}" opacity="0.45"/>')
        for (x, y, r, c1, c2) in ((95, 560, 50, "#c9a227", "#ffe9a3"), (170, 520, 56, "#9aa3b5", "#f2f5fb"),
                                  (240, 575, 46, "#1c1c2e", "#5a5a7a"), (150, 620, 42, "#c9a227", "#ffe9a3")):
            balloon(s, x, y, r, c1, c2, (170, 860))
        flute(s, 760, 850, 250, -14)
        flute(s, 880, 850, 250, 14)
        s.glow(822, 610, 60, "#fff3c4", 0.6)
        for _ in range(12):
            a = s.r.uniform(-math.pi, 0)
            d = s.r.uniform(20, 60)
            s.add(f'<circle cx="{822 + d * math.cos(a):.1f}" cy="{608 + d * math.sin(a):.1f}" r="{s.r.uniform(2, 4.5):.1f}" fill="#fff3c4"/>')
        conf = ["#ffd36b", "#ff7ac0", "#63d6ff", "#ffffff", "#b88cff"]
        for _ in range(70):
            x, y = s.r.uniform(0, SW), s.r.uniform(30, 640)
            s.add(f'<rect x="{x:.1f}" y="{y:.1f}" width="{s.r.uniform(5, 10):.1f}" height="{s.r.uniform(2, 5):.1f}" '
                  f'fill="{s.r.choice(conf)}" opacity="0.9" transform="rotate({s.r.uniform(0, 180):.0f} {x:.1f} {y:.1f})"/>')


# ======================= Valentine's Day: hearts, roses and love letters =======================
@theme("valentines", [(0, "#fff0f5"), (0.6, "#ffe2ec"), (1, "#ffd6e2")])
class Valentines:
    @staticmethod
    def deco(s):
        scatter(s, 34, TOP_CLEAR + 10, BAND_Y + 40, 24, lambda x, y: s.heart(x, y, s.r.uniform(16, 34), s.r.choice(
            ["#ff8fab", "#ffb3c6", "#ff6b8b", "#ffffff"]), s.r.uniform(0.5, 0.9), s.r.uniform(-25, 25)))
        envelope(s, 110, 420, 92, -14)
        envelope(s, 940, 1160, 80, 12, a=0.95)
        arrow_heart(s, 900, 420, 46, -18)
        scatter(s, 14, TOP_CLEAR + 30, BAND_Y, 30, lambda x, y: s.sparkle(x, y, s.r.uniform(6, 11), "#ffffff", 0.9))

    @staticmethod
    def scene(s):
        s.sky("#ffd6e2", "#f9b8cb", "#f7a3bf", "#e9789d")
        s.glow(516, 360, 420, "#ffffff", 0.3)
        heart_garland(s, -30, SW + 30, 40, 36, ["#e5486e", "#ff8fab", "#ffffff", "#ff4d6d"], 12)
        for (x, y, sz, c1, c2) in ((300, 330, 140, "#ff6b8b", "#d6335c"), (520, 260, 180, "#ff4d6d", "#c9184a"),
                                   (745, 350, 125, "#ff8fab", "#e5486e"), (440, 450, 84, "#ffb3c6", "#ff6b8b")):
            s.balloon_heart(x, y, sz, c1, c2, (x + s.r.uniform(-30, 30), y + sz * 1.6))
        envelope(s, 140, 300, 110, -12)
        envelope(s, 900, 230, 90, 14)
        for _ in range(16):
            s.heart(s.r.uniform(30, SW - 30), s.r.uniform(110, 560), s.r.uniform(14, 28), "#ffffff", s.r.uniform(0.35, 0.8),
                    s.r.uniform(-25, 25))
        s.cloud(170, 640, 170, "#ffffff", 0.9)
        s.cloud(880, 650, 190, "#ffffff", 0.9)
        s.cloud(520, 690, 240, "#fff4f7", 0.95)
        s.cloud(240, 770, 230, "#fff4f7", 1)
        s.cloud(800, 780, 250, "#fff4f7", 1)
        s.add(f'<rect x="0" y="745" width="{SW}" height="130" fill="#fff4f7"/>')
        bouquet(s, 790, 850, 330)
        for _ in range(40):
            s.petal(s.r.uniform(0, SW), s.r.uniform(700, 860), s.r.uniform(0.8, 1.4), s.r.choice(["#e5486e", "#ff6b8b", "#d6234a"]),
                    s.r.uniform(0, 180))
        rose(s, 210, 812, 30)
        rose(s, 262, 832, 24, "#ff6b8b", "#ff9fb3", "#d6335c")
        rose(s, 160, 842, 22)


# ======================= St. Patrick's Day: a rainbow over the green fields =======================
@theme("st_patricks", [(0, "#7cc8ff"), (0.6, "#9fd8ff"), (1, "#bfe6ff")])
class StPatricks:
    @staticmethod
    def deco(s):
        s.cloud(150, 400, 120, "#ffffff", 0.95)
        s.cloud(900, 600, 100, "#ffffff", 0.95)
        scatter(s, 16, TOP_CLEAR + 40, BAND_Y + 40, 34, lambda x, y: shamrock(s, x, y, s.r.uniform(20, 34), s.r.choice(
            ["#2f9a4f", "#3cae5c", "#24804a"]), s.r.uniform(0, 120), False))
        scatter(s, 10, TOP_CLEAR + 40, BAND_Y + 40, 24, lambda x, y: coin(s, x, y, s.r.uniform(10, 16), s.r.uniform(-40, 40)))
        scatter(s, 14, TOP_CLEAR + 30, BAND_Y, 20, lambda x, y: s.sparkle(x, y, s.r.uniform(6, 11), "#ffe28a", 0.95))

    @staticmethod
    def scene(s):
        s.sky("#bfe6ff", "#d8f1ff", "#eefaff")
        rainbow(s, 470, 640, 470, 26)
        s.cloud(70, 600, 150, "#ffffff", 0.95)
        hill(s, 470, 24, 0.9, 0.6, "#a8dc8f", "#8fd17a")
        hill(s, 525, 26, 0.7, 2.4, "#78c867", "#62b85a")
        for k in range(5):
            x0 = 80 + k * 210
            s.add(f'<path d="M{x0},{s.ridge_y(525, 26, 0.7, 2.4, x0) + 6:.0f} Q{x0 + 30},{600} {x0 - 20},{690}" stroke="#9aa4a0" '
                  f'stroke-width="6" stroke-dasharray="10 5" fill="none" opacity="0.8"/>')
        cottage(s, 270, s.ridge_y(525, 26, 0.7, 2.4, 270) + 30, 170, 92)
        for (x, k) in ((520, 46), (590, 40), (650, 44), (430, 36)):
            sheep(s, x, s.ridge_y(525, 26, 0.7, 2.4, x) + 40 + k * 0.3, k)
        hill(s, 640, 18, 0.6, 4.2, "#4caa58", "#3a9a4f")
        stone_wall(s, -20, 600, 700, 46)
        hill(s, 735, 12, 0.6, 1.0, "#2f8a47", "#257a3d")
        tufts(s, 735, 12, 0.6, 1.0, "#5cc46c", 70, (10, 20))
        pot_of_gold(s, 820, 770, 130)
        for _ in range(8):
            coin(s, s.r.uniform(680, 990), s.r.uniform(790, 850), s.r.uniform(12, 18), s.r.uniform(-40, 40))
        for (x, y, sz) in ((120, 800, 50), (250, 840, 40), (470, 820, 46), (560, 850, 34), (980, 830, 40)):
            shamrock(s, x, y, sz, "#1f7a3a", s.r.uniform(-20, 20), True, "#3cae5c")
        for _ in range(20):
            s.blossom(s.r.uniform(20, SW - 20), s.r.uniform(760, 860), s.r.uniform(5, 8), "#ffffff", "#ffd34d")


# ======================= Easter: a spring meadow with eggs to find =======================
@theme("easter", [(0, "#cfe8ff"), (0.6, "#e3f0ff"), (1, "#f0e8ff")])
class Easter:
    @staticmethod
    def deco(s):
        s.cloud(140, 410, 110, "#ffffff", 0.95)
        s.cloud(905, 600, 96, "#ffffff", 0.95)
        cols = [("#ff9fce", "#ffd1e8"), ("#9cc7ff", "#d6e7ff"), ("#ffd166", "#fff1b8"), ("#b79cff", "#e2d6ff")]
        scatter(s, 9, TOP_CLEAR + 60, BAND_Y + 20, 40, lambda x, y: butterfly(s, x, y, s.r.uniform(30, 44), *s.r.choice(cols),
                                                                              s.r.uniform(-25, 25)))
        scatter(s, 22, TOP_CLEAR + 40, BAND_Y + 40, 16, lambda x, y: s.blossom(x, y, s.r.uniform(6, 10), s.r.choice(
            ["#ffffff", "#ffd1e8", "#fff3a6", "#d9c7ff"]), "#ffcf4a"))

    @staticmethod
    def scene(s):
        s.sky("#f0e8ff", "#f6eefc", "#fdf6ea")
        s.sun(840, 170, 60, "#fffbe6", "#ffe9a3", "#fff3c4", 0.6)
        s.cloud(250, 200, 120, "#ffffff", 0.95)
        s.cloud(620, 130, 90, "#ffffff", 0.9)
        hill(s, 450, 22, 0.9, 0.2, "#cfeec0", "#bfe6ae")
        for (x, h, c) in ((120, 170, ("#f7a8c8", "#ffc2d9", "#ffe0ec")), (300, 130, ("#ffffff", "#fff4f8", "#ffffff")),
                          (760, 150, ("#f7a8c8", "#ffc2d9", "#ffe0ec")), (930, 190, ("#ffffff", "#fff0f6", "#ffffff"))):
            leafy_tree(s, x, s.ridge_y(450, 22, 0.9, 0.2, x) + 16, h, c, "#8a6a52")
        hill(s, 520, 26, 0.6, 2.6, "#b2e39f", "#a2d98d")
        fence(s, -10, SW + 10, 590, "#ffffff", 46, 34, "#dfe6ee")
        hill(s, 620, 16, 0.5, 4.0, "#8fd17a", "#7cc368")
        tufts(s, 620, 16, 0.5, 4.0, "#5fae55", 80, (10, 22))
        for (x, h) in ((60, 150), (110, 120), (950, 160), (1000, 130)):
            daffodil(s, x, 860, h)
        for (x, h, c) in ((170, 170, "#ff8fb8"), (215, 140, "#ffd166"), (880, 180, "#c49bff"), (830, 150, "#ff8fb8")):
            s.tulip(x, 860, h, c, "#4f9a4a")
        for (x, y, sz, c1, c2, rot) in ((300, 760, 40, "#8ec5ff", "#ffffff", -12), (380, 800, 34, "#ffb3d9", "#ffffff", 8),
                                        (690, 790, 38, "#ffe27a", "#ff9fce", -4), (760, 830, 32, "#9ce6c1", "#ffffff", 10)):
            s.egg(x, y, sz, c1, c2, rot)
        basket(s, 520, 850, 210, [("#ff9fce", "#ffffff"), ("#8ec5ff", "#fff3a6"), ("#ffe27a", "#ff9fce"), ("#c9a7ff", "#ffffff")])
        bunny(s, 650, 860, 150)
        chick(s, 300, 856, 56)
        chick(s, 360, 860, 44, "#ffe14a")
        butterfly(s, 640, 520, 46, "#ff9fce", "#ffd1e8", -15)
        butterfly(s, 420, 610, 38, "#9cc7ff", "#d6e7ff", 12)
        for _ in range(26):
            x = s.r.uniform(20, SW - 20)
            s.blossom(x, s.r.uniform(640, 860), s.r.uniform(5, 9), s.r.choice(["#ffffff", "#ffd1e8", "#fff3a6", "#d9c7ff"]), "#ffcf4a")


# ======================= Cinco de Mayo: a fiesta in the desert at sunset =======================
@theme("cinco_de_mayo", [(0, "#0f3d48"), (0.6, "#155a66"), (1, "#1d6f7a")])
class CincoDeMayo:
    @staticmethod
    def deco(s):
        conf = ["#e63946", "#f4a261", "#2a9d8f", "#e9c46a", "#ff70a6", "#3a86ff", "#ffffff"]
        scatter(s, 90, TOP_CLEAR + 10, BAND_Y + 40, 10, lambda x, y: s.add(
            f'<rect x="{x:.1f}" y="{y:.1f}" width="{s.r.uniform(6, 13):.1f}" height="{s.r.uniform(3, 6):.1f}" fill="{s.r.choice(conf)}" '
            f'opacity="0.85" transform="rotate({s.r.uniform(0, 180):.0f} {x:.1f} {y:.1f})"/>'), 0.15)
        string_lights(s, -10, 340, 300, 30, ["#ffd166", "#ff70a6", "#3a86ff", "#06d6a0"], 7, 0.35)
        string_lights(s, 700, SW + 10, 300, 30, ["#06d6a0", "#ffd166", "#ff70a6", "#3a86ff"], 7, 0.35)
        s.add('<path d="M922,330 V380" stroke="#f7f1e3" stroke-width="2"/>')
        pinata(s, 922, 450, 70)

    @staticmethod
    def scene(s):
        s.sky("#1d6f7a", "#3fa3a0", "#f2b45a", "#f07a3a")
        s.sun(400, 470, 100, "#fff1c9", "#ffc75a", "#ffd27a", 0.55)
        s.bunting(-20, SW + 20, 34, ["#e63946", "#f4a261", "#2a9d8f", "#e9c46a", "#e76f51", "#8ac926", "#ff70a6",
                                     "#3a86ff", "#e63946", "#f4a261", "#2a9d8f", "#e9c46a", "#e76f51"], 52, 44)
        s.peaks(500, [(0, 445), (70, 390), (250, 390), (300, 445), (560, 445), (610, 360), (840, 360), (900, 430),
                      (SW, 430)], "#b5562b")
        s.peaks(500, [(0, 470), (120, 455), (400, 470), (700, 462), (SW, 470)], "#9c4423")
        hill(s, 560, 12, 0.5, 1.0, "#8c3f21", "#7a3520")
        mission(s, 720, s.ridge_y(560, 12, 0.5, 1.0, 720) + 18, 0.95)
        string_lights(s, 380, 610, 470, 26, ["#ffd166", "#ff70a6", "#3a86ff", "#06d6a0"], 8, 0.4)
        s.add('<rect x="376" y="470" width="6" height="140" fill="#5a2a1a"/>')
        hill(s, 650, 10, 0.5, 2.0, "#6b2e18", "#5a2614")
        agave(s, 230, 700, 110)
        prickly_pear(s, 120, 770, 120)
        prickly_pear(s, 940, 790, 110)
        agave(s, 830, 760, 90, "#4a7f62", "#68a383")
        hill(s, 780, 8, 0.5, 3.0, "#5a2614", "#4a1f10")
        for (x, y, r, c1, c2) in ((360, 800, 34, "#ff3d7f", "#ff8fb3"), (440, 830, 28, "#ffd166", "#fff1b8"), (520, 805, 36, "#ff8c42", "#ffc08a"),
                                  (600, 835, 26, "#b07cf2", "#d9c2ff"), (680, 815, 32, "#ff3d7f", "#ff8fb3")):
            dahlia(s, x, y, r, c1, c2)
        cactus(s, 1000, 860, 230, "#2f6b3d")


# ======================= 4th of July: fireworks over the lake =======================
@theme("july_4", [(0, "#020614"), (0.6, "#050b20"), (1, "#060d26")])
class July4:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 140, BAND_Y - 20, "#ffffff", 0.08)
        s.burst(110, 430, 74, "#ff4d5e", "#ffd0d4", 20, 0.9)
        s.burst(930, 470, 68, "#4d8dff", "#d4e4ff", 20, 0.9)
        scatter(s, 12, TOP_CLEAR + 30, BAND_Y, 30, lambda x, y: s.sparkle(x, y, s.r.uniform(7, 12), "#ffffff", 0.85))

    @staticmethod
    def scene(s):
        s.sky("#060d26", "#13235a", "#25387a")
        s.stars(60, 340)
        s.burst(220, 170, 125, "#ff4d5e", "#ffd0d4", 28)
        ring_burst(s, 520, 130, 92, "#ffffff")
        s.burst(810, 190, 128, "#4d8dff", "#d4e4ff", 28)
        willow(s, 390, 330, 92, "#ffd36b", 18)
        s.burst(930, 360, 62, "#ffffff", "#ffffff", 16)
        s.burst(650, 330, 70, "#ff4d5e", "#ffd0d4", 18)
        s.peaks(500, [(0, 470), (140, 430), (300, 465), (480, 422), (690, 462), (870, 428), (SW, 460)], "#0c163d")
        s.skyline(505, "#101a44", "#ffd27a", 0.2, (30, 80))
        s.water(505, "#14275e", "#0a1438")
        for (x, c) in ((220, "#ff4d5e"), (520, "#ffffff"), (810, "#4d8dff"), (390, "#ffd36b")):
            s.glow(x, 590, 100, c, 0.22)
            for k in range(6):
                s.add(f'<rect x="{x - 50 + s.r.uniform(0, 40):.1f}" y="{530 + k * 24}" width="{s.r.uniform(30, 80):.1f}" '
                      f'height="3" rx="1.5" fill="{c}" opacity="0.45"/>')
        s.add(f'<rect x="0" y="720" width="{SW}" height="140" fill="#2a1d14"/>')
        s.add(f'<rect x="0" y="720" width="{SW}" height="10" fill="#3d2a1c"/>')
        for x in range(0, SW, 60):
            s.add(f'<rect x="{x}" y="730" width="3" height="130" fill="#1f150e"/>')
        s.add(f'<rect x="0" y="640" width="{SW}" height="12" fill="#4a3424"/>')
        for x in range(20, SW, 92):
            s.add(f'<rect x="{x}" y="640" width="12" height="90" fill="#4a3424"/>')
        for x in (150, 330, 510, 690, 870):
            fan_bunting(s, x, 652, 140)
        us_flag(s, 66, 360, 170, 720)
        for (x, y) in ((420, 600), (600, 590), (760, 610)):
            anchor(s, "sparkler", x, y, 0)
            s.glow(x, y, 26, "#fff3c4", 0.6)
            for k in range(10):
                a = s.r.uniform(0, 2 * math.pi)
                d = s.r.uniform(6, 24)
                s.add(f'<path d="M{x},{y} l{d * math.cos(a):.1f},{d * math.sin(a):.1f}" stroke="#fff3c4" stroke-width="2"/>')


# ======================= Spring: cherry blossoms, tulip fields and a pond =======================
@theme("spring", [(0, "#a6d9ff"), (0.6, "#bfe6ff"), (1, "#d4eeff")])
class Spring:
    @staticmethod
    def deco(s):
        s.cloud(150, 400, 110, "#ffffff", 0.95)
        s.cloud(900, 590, 96, "#ffffff", 0.9)
        cols = [("#ffb0cc", "#ffd6e6"), ("#ffd166", "#fff1b8"), ("#9cc7ff", "#d6e7ff")]
        scatter(s, 6, TOP_CLEAR + 60, BAND_Y, 40, lambda x, y: butterfly(s, x, y, s.r.uniform(28, 40), *s.r.choice(cols), s.r.uniform(-25, 25)))
        scatter(s, 60, TOP_CLEAR + 20, BAND_Y + 40, 12, lambda x, y: s.petal(x, y, s.r.uniform(0.9, 1.6), s.r.choice(
            ["#ffc2d9", "#ffb0cc", "#ffd6e6"]), s.r.uniform(0, 180), 0.9))
        for (x, y, sc) in ((870, 470, 1.2), (900, 450, 1.0), (930, 480, 1.1)):
            s.bird(x, y, sc, "#4a6a8a", 0.8)

    @staticmethod
    def scene(s):
        s.sky("#d4eeff", "#e8f4ff", "#ffe4f0")
        s.sun(860, 200, 56, "#fffbe6", "#ffe9a3", "#fff3c4", 0.55)
        s.cloud(600, 170, 110, "#ffffff", 0.9)
        hill(s, 430, 22, 0.9, 1.2, "#cdeab8", "#bfe3a6")
        rows = ["#ff5e7e", "#7cc368", "#ffd166", "#6cbf5c", "#ff8fb8", "#62b452", "#b07cf2", "#5aac4c", "#ff8c42"]
        for i, c in enumerate(rows):
            hill(s, 470 + i * 16, 10 + i, 0.6, 2.0 + i * 0.05, c)
        hill(s, 620, 18, 0.7, 3.2, "#93cf7d", "#7cbe66")
        px, py = 790, 700
        anchor(s, "pond", px, py, 200)
        s.add(f'<ellipse cx="{px}" cy="{py}" rx="220" ry="62" fill="#5aa9d6"/><ellipse cx="{px}" cy="{py - 6}" rx="200" ry="50" fill="#79c2e8"/>')
        for k in range(5):
            s.add(f'<rect x="{px - 140 + k * 60}" y="{py - 20 + (k % 2) * 16}" width="{s.r.uniform(30, 60):.0f}" height="3" rx="1.5" fill="#ffffff" opacity="0.6"/>')
        for (lx, ly, lr) in ((650, 715, 22), (720, 735, 18), (910, 718, 24), (860, 740, 16)):
            s.add(f'<path d="M{lx},{ly} m-{lr},0 a{lr},{lr * 0.45} 0 1,0 {lr * 2},0 a{lr},{lr * 0.45} 0 1,0 -{lr * 2},0 M{lx},{ly} l{lr},-{lr * 0.2:.1f}" '
                  f'fill="#4f9a4a" stroke="#79c2e8" stroke-width="3"/>')
        s.blossom(910, 708, 9, "#ffd1e8", "#ffcf4a")
        duck(s, 760, 690, 70)
        duck(s, 840, 702, 36, "#ffe14a")
        duck(s, 875, 708, 32, "#ffe14a")
        tx, tb = 210, 720
        s.add(f'<path d="M{tx - 16},{tb + 40} C{tx - 10},{tb - 60} {tx - 30},{tb - 120} {tx - 80},{tb - 190} M{tx},{tb - 70} '
              f'C{tx + 30},{tb - 140} {tx + 90},{tb - 170} {tx + 150},{tb - 200} M{tx - 20},{tb - 110} C{tx - 10},{tb - 180} '
              f'{tx + 20},{tb - 230} {tx + 40},{tb - 270}" stroke="#6b4636" stroke-width="18" fill="none" stroke-linecap="round"/>')
        s.add(f'<path d="M{tx - 22},{tb + 44} C{tx - 14},{tb - 40} {tx + 12},{tb - 40} {tx + 16},{tb + 44}Z" fill="#6b4636"/>')
        for _ in range(190):
            ang = s.r.uniform(0, 2 * math.pi)
            rr = s.r.uniform(0, 1) ** 0.6
            x = tx + 20 + math.cos(ang) * 220 * rr
            y = tb - 210 + math.sin(ang) * 125 * rr
            s.add(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(10, 24):.1f}" '
                  f'fill="{s.r.choice(["#ffc2d9", "#ffb0cc", "#ffd6e6", "#ff9cbf"])}" opacity="{s.r.uniform(0.75, 1):.2f}"/>')
        birdhouse(s, 470, 800, 230)
        hill(s, 790, 10, 0.5, 4.4, "#79bd66", "#6aaf58")
        tufts(s, 790, 10, 0.5, 4.4, "#4f9a4a", 80, (10, 20))
        for (x, h, c) in ((340, 130, "#ff5e7e"), (380, 110, "#ffd166"), (600, 140, "#ff8fb8"), (640, 115, "#b07cf2"),
                          (980, 150, "#ff5e7e"), (40, 120, "#ffd166")):
            s.tulip(x, 862, h, c, "#4f9a4a")
        for _ in range(30):
            s.blossom(s.r.uniform(20, SW - 20), s.r.uniform(800, 860), s.r.uniform(6, 10), s.r.choice(["#ffffff", "#fff3a6", "#ffd1e8"]), "#ffcf4a")
        butterfly(s, 540, 560, 40, "#ffb0cc", "#ffd6e6", 15)
        for _ in range(30):
            s.petal(s.r.uniform(0, SW), s.r.uniform(80, 700), s.r.uniform(0.8, 1.5), "#ffc2d9", s.r.uniform(0, 180), 0.9)


# ======================= Summer: a beach day =======================
@theme("summer", [(0, "#1d69c3"), (0.6, "#2a78cf"), (1, "#3d8bd9")])
class Summer:
    @staticmethod
    def deco(s):
        s.cloud(150, 400, 120, "#ffffff", 0.95)
        s.cloud(900, 600, 104, "#ffffff", 0.95)
        for (x, y, sc) in ((860, 430, 1.6), (905, 455, 1.2), (950, 420, 1.4), (110, 640, 1.3)):
            s.bird(x, y, sc, "#ffffff", 0.95)
        scatter(s, 10, TOP_CLEAR + 30, BAND_Y, 30, lambda x, y: s.sparkle(x, y, s.r.uniform(6, 10), "#ffffff", 0.8))

    @staticmethod
    def scene(s):
        s.sky("#3d8bd9", "#7cc2f0", "#ffe0a6", "#ffb37a")
        s.sun(640, 410, 84, "#fff6d8", "#ffd27a", "#ffe0a0", 0.7)
        s.cloud(300, 220, 120, "#ffffff", 0.9)
        s.cloud(820, 160, 90, "#ffffff", 0.85)
        s.add('<path d="M860,420 Q900,380 950,404 Q990,388 1040,420Z" fill="#3a7a6a"/><path d="M700,422 Q725,404 760,422Z" fill="#4a8a7a"/>')
        s.water(420, "#2a8bc9", "#0e4f86", "#ffffff")
        for k, (w, a) in enumerate(((150, 0.8), (110, 0.6), (80, 0.45), (50, 0.35))):
            s.add(f'<rect x="{640 - w / 2}" y="{428 + k * 20}" width="{w}" height="6" rx="3" fill="#fff3c4" opacity="{a}"/>')
        sailboat(s, 450, 470, 0.7, "#ffffff", "#ffffff", "#ff8a5a")
        dolphin(s, 880, 520, 0.7, "#5b8fb9", -25)
        s.add(f'<path d="M0,{SH} L0,610 Q220,570 450,625 Q650,668 {SW},612 L{SW},{SH}Z" fill="#f4d29c"/>')
        s.add(f'<path d="M0,610 Q220,570 450,625 Q650,668 {SW},612" fill="none" stroke="#ffffff" stroke-width="10" opacity="0.7"/>')
        s.add(f'<path d="M0,628 Q220,590 450,645 Q650,688 {SW},632" fill="none" stroke="#e6bf85" stroke-width="12" opacity="0.6"/>')
        palm(s, 120, 830, 520, 1.0)
        palm(s, 230, 800, 380, 0.8, "#7a4e2c", "#3a9a52", "#4fb066")
        s.add('<path d="M560,830 L760,800 L800,840 L600,860Z" fill="#3fa9f5"/><path d="M580,834 L770,806" stroke="#ffffff" stroke-width="8"/>'
              '<path d="M590,846 L785,818" stroke="#ffffff" stroke-width="8"/>')
        umbrella(s, 680, 820, 250, ("#ff5e5b", "#ffffff"), -8)
        beach_ball(s, 470, 800, 34)
        sandcastle(s, 860, 846, 110)
        surfboard(s, 990, 840, 260, "#ffd166", "#ff5e5b", 8)
        starfish(s, 380, 840, 22, "#ff8a5a", 20)
        shell(s, 520, 848, 16, "#ffd1c2", -10)
        shell(s, 330, 812, 12, "#ffe3d6", 15)
        starfish(s, 940, 862, 16, "#ff6f61", -10)
        for (x, y, sc) in ((420, 250, 1.4), (460, 270, 1.1), (760, 300, 1.2)):
            s.bird(x, y, sc, "#ffffff", 0.95)


# ======================= Fall: an afternoon in the park =======================
@theme("fall", [(0, "#ffe6bd"), (0.6, "#ffdcab"), (1, "#ffd59a")])
class Fall:
    @staticmethod
    def deco(s):
        scatter(s, 46, TOP_CLEAR + 20, BAND_Y + 60, 22, lambda x, y: maple(s, x, y, s.r.uniform(13, 26), s.r.choice(LEAVES),
                                                                           s.r.uniform(0, 360), s.r.uniform(0.8, 1)))
        for (x, y, sc) in ((880, 450, 1.3), (910, 470, 1.0), (940, 445, 1.1)):
            s.bird(x, y, sc, "#7a4a2a", 0.7)

    @staticmethod
    def scene(s):
        s.sky("#ffd59a", "#f6b06a", "#ec8f52")
        s.sun(800, 290, 64, "#fff2cf", "#ffd27a", "#ffe0a8", 0.5)
        hill(s, 400, 18, 0.9, 0.5, "#e0955a", "#d88a4e")
        for x in range(10, SW, 34):
            leafy_tree(s, x + s.r.uniform(-8, 8), s.ridge_y(400, 18, 0.9, 0.5, x) + 26, s.r.uniform(70, 100), s.r.choice(AUTUMN), "#6a3a22")
        s.mist(440, 0.22)
        hill(s, 480, 20, 0.7, 2.2, "#c9733a", "#b9652f")
        hill(s, 560, 14, 0.6, 1.4, "#b5652f", "#9a5226")
        s.add(f'<path d="M420,{SH} C470,760 640,700 560,640 C510,605 560,575 600,560 L640,562 C610,580 590,610 640,640 '
              f'C740,700 640,780 700,{SH}Z" fill="#e8c38a"/>')
        s.add(f'<path d="M430,{SH} C480,770 630,705 565,642" stroke="#d4a868" stroke-width="5" fill="none" opacity="0.7"/>')
        lamp_post(s, 380, 700, 230)
        lamp_post(s, 760, 690, 230)
        bench(s, 270, 760, 170)
        leafy_tree(s, 110, 830, 480, AUTUMN[0], "#5a3020")
        leafy_tree(s, 950, 820, 440, AUTUMN[2], "#5a3020")
        leafy_tree(s, 840, 700, 260, AUTUMN[1], "#5a3020")
        leaf_pile(s, 230, 836, 180, LEAVES)
        leaf_pile(s, 860, 846, 160, LEAVES)
        for (x, h, c) in ((560, 60, "#d9452f"), (600, 44, "#e8a23a"), (980, 50, "#d9452f")):
            mushroom(s, x, 856, h, c)
        pumpkin2(s, 760, 852, 34)
        for _ in range(24):
            maple(s, s.r.uniform(20, SW - 20), s.r.uniform(40, 560), s.r.uniform(10, 18), s.r.choice(LEAVES), s.r.uniform(0, 360), 0.9)
        for _ in range(30):
            maple(s, s.r.uniform(0, SW), s.r.uniform(780, 860), s.r.uniform(8, 13), s.r.choice(LEAVES), s.r.uniform(0, 360))


# ======================= Winter: a cabin in the snowy woods =======================
@theme("winter", [(0, "#a0c6e9"), (0.6, "#b0d0ee"), (1, "#b9d8f2")])
class Winter:
    @staticmethod
    def deco(s):
        scatter(s, 24, TOP_CLEAR + 20, BAND_Y + 40, 24, lambda x, y: snowflake(s, x, y, s.r.uniform(9, 20), "#ffffff",
                                                                                s.r.uniform(0.7, 1), s.r.uniform(0, 60)))
        scatter(s, 160, TOP_CLEAR, BAND_Y + 40, 4, lambda x, y: s.add(
            f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s.r.uniform(1.5, 4.5):.1f}" fill="#ffffff" opacity="{s.r.uniform(0.5, 0.95):.2f}"/>'), 0.25)
        s.cloud(150, 400, 110, "#ffffff", 0.7)
        s.cloud(900, 590, 96, "#ffffff", 0.7)

    @staticmethod
    def scene(s):
        s.sky("#b9d8f2", "#d4e6f6", "#e8f3fb")
        s.sun(800, 210, 50, "#ffffff", "#fdf6e3", "#ffffff", 0.7)
        s.peaks(470, [(0, 360), (125, 260), (240, 345), (380, 210), (540, 360), (650, 280), (790, 200), (930, 335),
                      (SW, 275)], "#8fb3d6", "#ffffff")
        s.peaks(510, [(0, 450), (175, 360), (345, 430), (485, 350), (650, 440), (810, 360), (SW, 430)], "#b7cfe6", "#ffffff")
        hill(s, 545, 16, 0.6, 1.4, "#eef5fc", "#e3edf8")
        for x in range(0, SW, 36):
            pine2(s, x + s.r.uniform(-8, 8), s.ridge_y(545, 16, 0.6, 1.4, x) + 30, s.r.uniform(90, 150), "#2e5a52", "#3f7468", "#ffffff")
        hill(s, 640, 14, 0.5, 3.0, "#f7fbff", "#eaf2fa")
        log_cabin(s, 800, 720, 220, 130)
        s.add('<ellipse cx="420" cy="740" rx="230" ry="44" fill="#bcd8ee"/><ellipse cx="420" cy="734" rx="214" ry="34" fill="#d6e9f7"/>')
        for k in range(4):
            s.add(f'<path d="M{300 + k * 60},{722 + (k % 2) * 16} l{s.r.uniform(30, 60):.0f},-6" stroke="#ffffff" stroke-width="3" opacity="0.8"/>')
        deer(s, 150, 700, 1.1, "#5a4636")
        snowman(s, 610, 820, 190, "#2f6fb0", "#2b2b3a")
        sled(s, 930, 830, 150)
        for k in range(10):
            s.add(f'<ellipse cx="{560 - k * 34 + (8 if k % 2 else -8)}" cy="{850 - k * 10}" rx="7" ry="4" fill="#c9dcef"/>')
        hill(s, 800, 8, 0.5, 2.0, "#ffffff", "#f2f7fc")
        s.snow(140, 0, SH, (0.6, 1.0), (2, 5))
        for _ in range(10):
            snowflake(s, s.r.uniform(20, SW - 20), s.r.uniform(20, 500), s.r.uniform(7, 12), "#ffffff", 0.9, s.r.uniform(0, 60))


# ======================= Aurora: camping under the northern lights =======================
@theme("aurora", [(0, "#020812"), (0.5, "#03101f"), (1, "#04101f")])
class Aurora:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 190, BAND_Y - 20, "#ffffff", 0.08)
        s.aurora(["#3df2b0", "#2fd1ff", "#a77bff"], 300, 0.5)
        shooting_star(s, 930, 700, 140)

    @staticmethod
    def scene(s):
        s.sky("#04101f", "#0b2a45", "#123d5c")
        s.stars(90, 420)
        s.aurora(["#3df2b0", "#2fd1ff", "#a77bff"], 100, 0.75)
        s.peaks(470, [(0, 400), (160, 300), (300, 380), (450, 270), (600, 370), (760, 290), (920, 380), (SW, 330)],
                "#0a1e33", "#cfe6ff")
        s.water(470, "#0f3350", "#071a2c", "#7fe9d1")
        f = s.blur(16)
        for i, c in enumerate(["#3df2b0", "#2fd1ff"]):
            s.add(f'<ellipse cx="{360 + i * 300}" cy="{540 + i * 20}" rx="260" ry="26" fill="{c}" opacity="0.18" filter="{f}"/>')
        hill(s, 640, 10, 0.4, 2.0, "#0a1a2a", "#06121f")
        for x in (40, 95, 140, 900, 955, 1005):
            pine2(s, x, 650, s.r.uniform(150, 220), "#04101c", "#071828")
        tent(s, 560, 700, 170, 130, "#e8743b", "#ffcf6a")
        campfire(s, 380, 720, 0.9)
        canoe(s, 780, 640, 200, "#c8462a")
        hill(s, 760, 8, 0.5, 3.0, "#06121f", "#030a12")
        for x in (60, 980):
            pine2(s, x, 860, 300, "#030a12", "#061422")


# ======================= Ocean: a lighthouse by the sea =======================
@theme("ocean", [(0, "#3a95de"), (0.6, "#46a0e4"), (1, "#4fa7e8")])
class Ocean:
    @staticmethod
    def deco(s):
        s.cloud(150, 400, 120, "#ffffff", 0.95)
        s.cloud(900, 600, 104, "#ffffff", 0.95)
        for (x, y, sc) in ((860, 430, 1.5), (900, 455, 1.1), (110, 640, 1.3), (150, 660, 1.0)):
            s.bird(x, y, sc, "#ffffff", 0.95)

    @staticmethod
    def scene(s):
        s.sky("#4fa7e8", "#7cc2f0", "#a8dcff")
        s.sun(820, 200, 54, "#ffffff", "#fff3c4", "#ffffff", 0.6)
        s.cloud(300, 210, 125)
        s.cloud(620, 150, 85, "#ffffff", 0.85)
        s.add('<path d="M560,390 Q620,350 690,388 Q730,372 780,392Z" fill="#5a9a8a"/>')
        anchor(s, "water", 0, 400, 0)
        s.add(f'<rect x="0" y="390" width="{SW}" height="{SH}" fill="#1f7fc4"/>')
        for i, (y, c) in enumerate(((410, "#2a92d4"), (480, "#1b74b5"), (560, "#155f9a"), (650, "#0f4b7d"))):
            d = [f"M0,{SH}", f"L0,{y}"]
            for k in range(0, 41):
                x = SW * k / 40
                d.append(f"L{x:.1f},{y + 14 * math.sin(k * 0.8 + i):.1f}")
            d.append(f"L{SW},{SH}Z")
            s.add(f'<path d="{" ".join(d)}" fill="{c}"/>')
            for k in range(0, 40, 3):
                x = SW * k / 40
                s.add(f'<path d="M{x:.0f},{y + 14 * math.sin(k * 0.8 + i):.0f} q16,-12 32,0" stroke="#ffffff" stroke-width="4" fill="none" '
                      f'opacity="0.55" stroke-linecap="round"/>')
            if i == 0:
                sailboat(s, 640, 420, 0.9)
                whale_tail(s, 460, 470, 0.8)
            if i == 1:
                dolphin(s, 840, 500, 0.9, "#5b8fb9", -25)
                dolphin(s, 930, 520, 0.7, "#6a9cc4", -35)
        s.add('<path d="M0,860 L0,520 Q60,480 120,500 Q190,470 250,520 Q300,560 330,640 Q360,700 420,760 L460,860Z" fill="#5a5f66"/>')
        s.add('<path d="M0,560 Q60,520 120,540 Q190,512 240,556 Q280,590 300,640" stroke="#7a8088" stroke-width="10" fill="none"/>')
        lighthouse(s, 150, 520, 300)
        s.add('<rect x="70" y="500" width="160" height="22" rx="6" fill="#7a8088"/>')
        for (x, y, r) in ((360, 780, 40), (430, 820, 30), (300, 830, 50)):
            s.add(f'<ellipse cx="{x}" cy="{y}" rx="{r * 1.4}" ry="{r}" fill="#4a4f56"/>')
            s.add(f'<path d="M{x - r * 1.4},{y} q{r * 0.7:.0f},-{r * 0.3:.0f} {r * 1.4:.0f},-{r * 0.1:.0f}" stroke="#ffffff" stroke-width="5" fill="none" opacity="0.7"/>')
        s.add('<rect x="700" y="690" width="16" height="70" fill="#e63946"/><path d="M692,690 h32 l-6,-18 h-20Z" fill="#e63946"/>'
              '<rect x="700" y="712" width="16" height="12" fill="#ffffff"/>')
        for (x, y, sc) in ((380, 300, 1.4), (420, 320, 1.1), (900, 320, 1.2)):
            s.bird(x, y, sc, "#ffffff", 0.95)


# ======================= Desert: a highway through the mesas =======================
@theme("desert", [(0, "#ffb067"), (0.6, "#ffc48a"), (1, "#ffd5a3")])
class Desert:
    @staticmethod
    def deco(s):
        hot_air_balloon(s, 130, 440, 0.9, ("#ff5e5b", "#ffd166", "#ff8c42", "#ffffff"))
        hot_air_balloon(s, 905, 470, 0.62, ("#3fa9f5", "#ffffff", "#7bd389", "#ffd166"))
        for (x, y, sc) in ((860, 610, 1.3), (900, 630, 1.0)):
            s.bird(x, y, sc, "#7a4a2a", 0.7)

    @staticmethod
    def scene(s):
        s.sky("#ffd5a3", "#ffe3bd", "#fff1d6")
        s.sun(300, 330, 88, "#fff6e0", "#ffcf7a", "#ffd9a0", 0.6)
        hot_air_balloon(s, 700, 190, 0.55, ("#b07cf2", "#ffffff", "#ff8fab", "#ffd166"))
        hot_air_balloon(s, 860, 300, 0.36, ("#ff5e5b", "#ffffff"))
        s.peaks(470, [(0, 430), (100, 380), (270, 380), (330, 440), (680, 440), (730, 350), (940, 350), (985, 410),
                      (SW, 410)], "#e2895a")
        s.peaks(470, [(0, 455), (500, 448), (560, 405), (620, 405), (650, 452), (SW, 455)], "#d27a4c")
        s.add(f'<path d="M0,{SH} L0,500 Q250,445 520,510 T{SW},495 L{SW},{SH}Z" fill="#f2b277"/>')
        s.add(f'<path d="M0,{SH} L0,580 Q300,525 580,600 T{SW},565 L{SW},{SH}Z" fill="#e89a5c"/>')
        s.add(f'<path d="M500,500 L540,500 L760,{SH} L280,{SH}Z" fill="#5a5560"/>')
        for k in range(7):
            y0 = 505 + k * k * 8
            y1 = y0 + 6 + k * 4
            s.add(f'<path d="M{519 + (y0 - 500) * 0.06:.1f},{y0} L{521 + (y1 - 500) * 0.06:.1f},{y1}" stroke="#ffe08a" stroke-width="{2 + k * 1.6:.1f}"/>')
        s.add(f'<path d="M0,{SH} L0,660 Q280,615 560,668 T{SW},640 L{SW},{SH}Z" fill="#d9824a" opacity="0.9"/>')
        s.add(f'<path d="M280,{SH} L760,{SH} L700,760 L340,760Z" fill="#5a5560" opacity="0.6"/>')
        cactus(s, 840, 640, 240, "#3d7a4a", "#5a9a62")
        cactus(s, 180, 690, 170, "#3d7a4a", "#5a9a62")
        cactus(s, 640, 520, 70, "#4f8a5a", "#6aa874")
        rocks(s, 960, 760, 1.2)
        rocks(s, 70, 800, 1.0)
        prickly_pear(s, 300, 830, 80, "#4f8a4a", "#6aa864")
        agave(s, 930, 850, 80, "#5a8a6a", "#77a888")
        tumbleweed(s, 760, 850, 34)
        for (x, y, sc) in ((420, 200, 1.3), (460, 220, 1.0)):
            s.bird(x, y, sc, "#7a4a2a", 0.7)


# ======================= City Nights: the river, the bridge and the lights =======================
@theme("city", [(0, "#060918"), (0.6, "#0a0e26"), (1, "#0d1230")])
class City:
    @staticmethod
    def deco(s):
        stars_top_safe(s, 130, BAND_Y - 20, "#ffffff", 0.06)
        s.moon(890, 440, 54, crescent=True, color="#fff1c9")

    @staticmethod
    def scene(s):
        s.sky("#0d1230", "#26265c", "#6b3f7a")
        s.stars(40, 300)
        s.skyline(560, "#2a2858", "#ffcf73", 0.22, (130, 330))
        s.skyline(640, "#151536", "#ffd98a", 0.34, (90, 260))
        for x in (140, 330, 610, 790):
            ay = 640 - s.r.uniform(260, 300)
            anchor(s, "antenna", x, ay, 0)
            s.glow(x, ay, 14, "#ff4d4d", 0.6)
        s.water(640, "#1d1f4a", "#0e0f2a", "#ffcf73")
        for _ in range(16):
            x = s.r.uniform(0, SW)
            s.add(f'<rect x="{x:.0f}" y="{s.r.uniform(650, 760):.0f}" width="{s.r.uniform(3, 6):.0f}" height="{s.r.uniform(20, 60):.0f}" '
                  f'fill="#ffcf73" opacity="{s.r.uniform(0.15, 0.4):.2f}"/>')
        ferris_wheel(s, 830, 470, 130, "#9a8ad8")
        bridge(s, -20, 640, 620, [150, 470], "#5a5f9a", "#ffd27a", 220)
        s.add(f'<rect x="0" y="790" width="{SW}" height="70" fill="#0b0c22"/>')
        s.add(f'<rect x="0" y="770" width="{SW}" height="6" fill="#3a3d6a"/>')
        for x in range(0, SW, 40):
            s.add(f'<rect x="{x}" y="770" width="4" height="22" fill="#3a3d6a"/>')
        for x in (90, 520, 960):
            lamp_post(s, x, 800, 150, "#ffd98a", "#2a2d55")


# ======================= Farm: a red barn and the fields =======================
@theme("farm", [(0, "#ffcf8a"), (0.6, "#ffdca5"), (1, "#ffe7c2")])
class Farm:
    @staticmethod
    def deco(s):
        s.cloud(150, 410, 110, "#ffffff", 0.95)
        s.cloud(905, 600, 96, "#ffffff", 0.95)
        for (x, y, sc) in ((860, 440, 1.3), (895, 460, 1.0), (930, 435, 1.1)):
            s.bird(x, y, sc, "#5a3a22", 0.75)

    @staticmethod
    def scene(s):
        s.sky("#ffe7c2", "#f3efd6", "#cde9ff")
        s.sun(220, 300, 70, "#fff8e1", "#ffd98a", "#ffe6b0", 0.6)
        s.cloud(600, 170, 110, "#ffffff", 0.9)
        hill(s, 430, 20, 0.8, 1.0, "#a9d48a", "#9fcf7d")
        rows = ["#e9c46a", "#8cc46a", "#d9b44a", "#7bb85a", "#e9c46a"]
        for i, c in enumerate(rows):
            hill(s, 455 + i * 14, 14 + i * 2, 0.7, 2.6 + i * 0.03, c)
        hill(s, 545, 16, 0.6, 2.6, "#79b65c", "#6aa850")
        base = lambda x: s.ridge_y(545, 16, 0.6, 2.6, x) + 26
        leafy_tree(s, 90, base(90), 190, ("#3f8a3f", "#4f9a4a", "#7bc062"), "#5a3a24")
        leafy_tree(s, 180, base(180), 140, ("#4f9a4a", "#5fae55", "#8fd06a"), "#5a3a24")
        house(s, 330, base(330), 150, 92, "#f4efe3", "#5a6a7a", "#ffe9a8", False, "#7a4a2a", True, False, False, "#ffffff", shade="#000000")
        barn(s, 650, base(650), 1.0)
        silo(s, 800, base(800), 1.0)
        windmill(s, 930, base(930), 260)
        hill(s, 640, 10, 0.5, 1.4, "#e9c46a", "#e0b85a")
        for k in range(9):
            y = 655 + k * 13
            s.add(f'<path d="M0,{y} Q{SW / 2},{y - 44 + k * 3} {SW},{y}" stroke="#d4a64a" stroke-width="5" fill="none"/>')
        tractor(s, 480, 712, 0.9)
        hay_bale(s, 720, 720, 40)
        hay_bale(s, 610, 735, 34)
        cow(s, 150, 700, 1.0, 1)
        cow(s, 900, 712, 0.8, -1)
        hill(s, 770, 8, 0.5, 2.0, "#7cb85a", "#6aa850")
        fence(s, -10, SW + 10, 806, "#f5efe0", 56, 40, "#d9d2c0")
        for x in (40, 110, 180, 860, 930, 1000):
            sunflower(s, x, 860, s.r.uniform(150, 200))


# ======================= Mountains: a lake below the peaks =======================
@theme("mountains", [(0, "#4f9ae6"), (0.6, "#62a8ee"), (1, "#6fb5f2")])
class Mountains:
    @staticmethod
    def deco(s):
        s.cloud(150, 410, 110, "#ffffff", 0.95)
        s.cloud(905, 600, 96, "#ffffff", 0.9)
        eagle(s, 880, 450, 0.9, "#2b2b3a")
        for (x, y, sc) in ((110, 640, 1.2), (140, 660, 1.0)):
            s.bird(x, y, sc, "#2b3a4a", 0.7)

    @staticmethod
    def scene(s):
        s.sky("#6fb5f2", "#a8d4f8", "#cfe9ff")
        s.cloud(300, 160, 110)
        s.peaks(420, [(0, 310), (160, 192), (300, 290), (460, 152), (620, 280), (780, 182), (940, 290), (SW, 230)],
                "#5d7fa8", "#ffffff")
        s.peaks(480, [(0, 382), (200, 292), (380, 372), (540, 302), (710, 382), (870, 312), (SW, 372)], "#3f6b8f", "#e8f2fb")
        s.mist(492, 0.3)
        s.add('<path d="M840,520 L880,400 L960,380 L1040,420 L1040,560Z" fill="#4a6070"/>')
        waterfall(s, 905, 400, 560, 30)
        hill(s, 532, 18, 0.6, 2.0, "#3d7a5a", "#346c4f")
        for x in range(10, 840, 38):
            pine2(s, x + s.r.uniform(-8, 8), s.ridge_y(532, 18, 0.6, 2.0, x) + 36, s.r.uniform(90, 140), "#24553f", "#2f6a4f")
        s.water(600, "#4f8fbf", "#2f6a96", "#ffffff")
        for (x0, w) in ((150, 140), (420, 120), (640, 160)):
            s.add(f'<path d="M{x0},{612} l{w / 2},50 l{w / 2},-50Z" fill="#1f4a3a" opacity="0.25"/>')
        s.add('<rect x="120" y="680" width="260" height="14" fill="#8a6a4a"/>')
        for x in (130, 200, 270, 340):
            s.add(f'<rect x="{x}" y="680" width="10" height="50" fill="#6a4a2a"/>')
        canoe(s, 470, 700, 190, "#e8743b")
        deer(s, 700, 650, 0.8, "#6a4a32", -1)
        hill(s, 760, 10, 0.5, 3.0, "#5a9a4a", "#4a8a3f")
        tufts(s, 760, 10, 0.5, 3.0, "#3f7a35", 70, (10, 20))
        rocks(s, 900, 860, 1.4, "#8a8f96", "#70757c")
        rocks(s, 80, 860, 1.1, "#8a8f96", "#70757c")
        for _ in range(26):
            x = s.r.uniform(150, 850)
            h = s.r.uniform(40, 80)
            c = s.r.choice(["#8e6cf2", "#b07cf2", "#ffd166", "#ffffff"])
            s.add(f'<path d="M{x:.0f},860 V{860 - h:.0f}" stroke="#4f8a3a" stroke-width="3"/>')
            for k in range(5):
                s.add(f'<circle cx="{x:.0f}" cy="{860 - h + k * 7:.0f}" r="{5 - k * 0.6:.1f}" fill="{c}"/>')


# ======================= Simple themes =======================
@theme("warehouse", [(0, "#121212"), (0.6, "#181818"), (1, "#1f1f1f")])
class Warehouse:
    @staticmethod
    def deco(s):
        for x in (130, 900):
            s.add(f'<path d="M{x},{TOP_CLEAR} V{380}" stroke="#3a3a3a" stroke-width="3"/>')
            s.add(f'<path d="M{x - 40},{420} Q{x},{370} {x + 40},{420}Z" fill="#3a3a3a"/>')
            s.add(f'<path d="M{x - 38},{420} L{x - 150},{900} L{x + 150},{900} L{x + 38},{420}Z" fill="#fff3d6" opacity="0.035"/>')
            anchor(s, "lamp", x, 422, 30)
            s.glow(x, 422, 40, "#fff3d6", 0.5)
        scatter(s, 30, TOP_CLEAR, BAND_Y + 40, 6, lambda x, y: s.add(
            f'<circle cx="{x:.0f}" cy="{y:.0f}" r="{s.r.uniform(1.5, 3):.1f}" fill="#fff3d6" opacity="{s.r.uniform(0.15, 0.4):.2f}"/>'), 0.3)

    @staticmethod
    def scene(s):
        s.sky("#1f1f1f", "#262626", "#2e2e2e")
        rack(s, 190, 640, 360, 420)
        rack(s, 842, 640, 360, 420)
        s.add(f'<rect x="0" y="640" width="{SW}" height="{SH - 640}" fill="#3a3a3a"/>')
        s.add(f'<path d="M0,700 H{SW} M0,820 H{SW}" stroke="#f2b31a" stroke-width="8" opacity="0.8"/>')
        for x in range(0, SW, 60):
            s.add(f'<path d="M{x},700 l20,0" stroke="#3a3a3a" stroke-width="8"/>')
        forklift(s, 520, 800, 1.1)
        for (x, w) in ((300, 120), (740, 140)):
            s.add(f'<rect x="{x - w / 2}" y="760" width="{w}" height="16" fill="#9a7a4a"/><rect x="{x - w / 2 + 6}" y="776" width="{w - 12}" height="8" fill="#7a5a3a"/>')
            s.add(f'<rect x="{x - w / 2 + 6}" y="{700}" width="{w - 12}" height="60" fill="#c9a26b"/>')
            s.add(f'<path d="M{x - w / 2 + 6},{730} H{x + w / 2 - 6}" stroke="#a07a48" stroke-width="3"/>')


@theme("shopping", [(0, "#7b35c9"), (0.6, "#8740d6"), (1, "#9147df")])
class Shopping:
    @staticmethod
    def deco(s):
        scatter(s, 16, TOP_CLEAR + 20, BAND_Y + 20, 24, lambda x, y: s.sparkle(x, y, s.r.uniform(7, 13), "#ffffff", 0.9))
        for (x, y, rot, c) in ((120, 430, -18, "#ffd166"), (920, 460, 15, "#7ec8f5")):
            s.add(f'<g transform="rotate({rot} {x} {y})"><path d="M{x - 40},{y - 22} H{x + 22} L{x + 44},{y} L{x + 22},{y + 22} H{x - 40}Z" fill="{c}"/>'
                  f'<circle cx="{x + 18}" cy="{y}" r="6" fill="#8740d6"/><path d="M{x - 30},{y - 6} H{x + 4} M{x - 30},{y + 6} H{x - 6}" '
                  f'stroke="#8740d6" stroke-width="4" stroke-linecap="round"/></g>')

    @staticmethod
    def scene(s):
        s.sky("#9147df", "#9a52e6", "#a35fee")
        n = 9
        for i in range(n):
            x0 = SW * i / n
            c = "#ffffff" if i % 2 else "#ff8fab"
            s.add(f'<path d="M{x0:.1f},0 H{x0 + SW / n:.1f} V70 Q{x0 + SW / n / 2:.1f},104 {x0:.1f},70Z" fill="{c}"/>')
        s.add(f'<rect x="0" y="0" width="{SW}" height="14" fill="#5a2a8a"/>')
        s.glow(516, 560, 360, "#ffffff", 0.18)
        s.add('<circle cx="516" cy="560" r="230" fill="#ffffff" opacity="0.08"/>')
        cart(s, 520, 800, 1.3)
        shopping_bag(s, 180, 820, 130, 150, "#ffd166")
        shopping_bag(s, 290, 830, 110, 125, "#ff8fab")
        shopping_bag(s, 880, 826, 140, 160, "#7ec8f5")
        for _ in range(14):
            s.sparkle(s.r.uniform(40, SW - 40), s.r.uniform(160, 560), s.r.uniform(6, 12), "#ffffff", 0.9)
        s.add(f'<rect x="0" y="828" width="{SW}" height="40" fill="#7b35c9"/>')


@theme("horizon", [(0, "#14693a"), (0.6, "#1a7640"), (1, "#1e7e3b")])
class Horizon:
    @staticmethod
    def deco(s):
        for (x, y, sc) in ((860, 440, 1.3), (895, 460, 1.0), (930, 435, 1.1), (120, 620, 1.2)):
            s.bird(x, y, sc, "#d6f5df", 0.6)
        scatter(s, 12, TOP_CLEAR + 30, BAND_Y, 24, lambda x, y: s.sparkle(x, y, s.r.uniform(6, 10), "#d6f5df", 0.6))

    @staticmethod
    def scene(s):
        s.sky("#1e7e3b", "#2a8f4a", "#5fb56f")
        for (r, a) in ((300, 0.06), (220, 0.08), (150, 0.1)):
            s.add(f'<circle cx="640" cy="360" r="{r}" fill="#ffffff" opacity="{a}"/>')
        s.add('<circle cx="640" cy="360" r="90" fill="#f4ffe0"/>')
        hill(s, 470, 30, 0.8, 0.3, "#8fd19a", "#7cc68a")
        hill(s, 540, 34, 0.6, 2.2, "#5fb56f", "#4fa860")
        s.add(f'<path d="M470,{SH} C520,760 380,700 470,640 C540,590 600,590 640,560 L660,562 C640,600 580,620 540,660 C460,720 620,780 560,{SH}Z" fill="#bfe8f0"/>')
        hill(s, 640, 26, 0.7, 4.0, "#3f9a52", "#348a47")
        for x in range(30, SW, 60):
            if abs(x - 520) > 90:
                y = s.ridge_y(640, 26, 0.7, 4.0, x) + 20
                s.add(f'<rect x="{x - 3}" y="{y - 40}" width="6" height="40" fill="#2a5a3a"/><circle cx="{x}" cy="{y - 50}" r="22" fill="#2f7a42"/>')
        hill(s, 760, 16, 0.5, 1.0, "#2a7a3e", "#216a33")
        for (x, y, sc) in ((300, 220, 1.4), (340, 240, 1.1), (380, 215, 1.2)):
            s.bird(x, y, sc, "#d6f5df", 0.7)


# ======================= More themes, kept in their own files =======================
# Places (landmarks by day and at sunset), city nights, and just-for-fun scenes. THEME_MODULES (comma-separated)
# limits which of these load, for drawing one set at a time.
import os as _os  # noqa: E402

for _m in (_os.environ.get("THEME_MODULES") or "themes_day,themes_night,themes_fun").split(","):
    if _m.strip():
        __import__(_m.strip())
