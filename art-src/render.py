"""Renders Pass Studio's page art: python render.py OUT_DIR [theme ...]  (no themes = all of them)."""
import json
import os
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from base import *  # noqa: E402,F401,F403
import themes  # noqa: E402


class Front:
    """Drawing inside `with Front(s):` goes in front of the scene band instead of behind it."""

    def __init__(self, s):
        self.s = s

    def __enter__(self):
        self.saved = self.s.body
        self.s.body = self.s.front

    def __exit__(self, *exc):
        self.s.body = self.saved


themes.Front = Front


def render(key, out_dir):
    t = themes.THEMES[key]
    band = S(sum(map(ord, key)) + 7)
    t["scene"](band)
    sky = S(sum(map(ord, key)) * 3 + 1)
    sky.n = 10000                        # keep its gradient ids apart from the band's
    sky.front = []
    t["deco"](sky)
    stops = "".join(f'<stop offset="{o}" stop-color="{c}"/>' for o, c in t["sky"])
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{TALL_W}" height="{TALL_H}" viewBox="0 0 {TALL_W} {TALL_H}">'
           f'<defs><linearGradient id="pagesky" x1="0" y1="0" x2="0" y2="{BAND_Y + 2}" gradientUnits="userSpaceOnUse">'
           f'{stops}</linearGradient><clipPath id="band"><rect width="{SW}" height="{SH}"/></clipPath>'
           f'{"".join(sky.defs)}{"".join(band.defs)}</defs>'
           f'<rect width="{TALL_W}" height="{BAND_Y + 2}" fill="url(#pagesky)"/>'
           f'<g>{"".join(sky.body)}</g>'
           f'<g transform="translate(0,{BAND_Y})"><g clip-path="url(#band)">{"".join(band.body)}</g></g>'
           f'<g>{"".join(sky.front)}</g></svg>')
    p = os.path.join(out_dir, key)
    with open(p + ".svg", "w") as fh:
        fh.write(svg)
    subprocess.run(["rsvg-convert", "-w", str(TALL_W), "-h", str(TALL_H), p + ".svg", "-o", p + ".png"], check=True)
    os.remove(p + ".svg")
    # Where the moving parts are, on the page: the band's are moved down to where the band sits.
    marks = [(k, x, y + BAND_Y, v) for (k, x, y, v) in getattr(band, "anchors", [])]
    marks += list(getattr(sky, "anchors", []))
    with open(p + ".anchors.json", "w") as fh:
        json.dump(marks, fh)


if __name__ == "__main__":
    out = sys.argv[1]
    os.makedirs(out, exist_ok=True)
    keys = sys.argv[2:] or list(themes.THEMES)
    for k in keys:
        render(k, out)
        print("rendered", k)
