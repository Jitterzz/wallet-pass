# Theme art

The scripts that draw Pass Studio's theme art. Each theme is one full-page picture (1032 x 2240): its sky runs down
the page with decorations kept to the edges, clear of the pass, and its scene sits along the bottom.

- `themes.py`: every theme's sky, decorations and scene
- `props.py`, `base.py`, `scene_class.py.txt`: the things they're drawn from (houses, trees, pumpkins, fireworks...)
- `render.py`: draws the pictures, and notes where the moving parts are (chimneys, flames, lights, water)
- `export_app.py`: puts them in the app: `bg_*.webp` (the page), `art_*.webp` (cards and tiles) and `ThemeAnchors.kt`

Needs Python 3 with Pillow, and `rsvg-convert` (librsvg).

    python render.py /tmp/art
    python export_app.py /tmp/art ..
