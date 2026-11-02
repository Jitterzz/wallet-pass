"""Puts the rendered page art into the app: python export_app.py OUT_DIR APP_DIR

- res/drawable-nodpi/bg_<key>.webp: the full page (900 x 1953)
- res/drawable-nodpi/art_<key>.webp: its bottom part, for the cards and theme tiles (774 x 609)
- java/app/passstudio/ui/ThemeAnchors.kt: where each theme's moving parts are, for the animations
- prints each theme's sky color (the top of its page), which the catalog uses for the text color
"""
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import themes  # noqa: E402

OUT, APP = sys.argv[1], sys.argv[2]
res = os.path.join(APP, "app/src/main/res/drawable-nodpi")
kt = os.path.join(APP, "app/src/main/java/app/passstudio/ui/ThemeAnchors.kt")

blocks = []
for key in themes.THEMES:
    page = Image.open(os.path.join(OUT, key + ".png")).convert("RGB")
    page.resize((900, 1953), Image.LANCZOS).save(os.path.join(res, f"bg_{key}.webp"), "WEBP", quality=82, method=6)
    page.crop((0, 1428, 1032, 2240)).resize((774, 609), Image.LANCZOS).save(os.path.join(res, f"art_{key}.webp"), "WEBP",
                                                                            quality=84, method=6)
    marks = json.load(open(os.path.join(OUT, key + ".anchors.json")))
    kinds = {}
    for (kind, x, y, v) in marks:
        kinds.setdefault(kind, []).extend([x, y, v])
    entries = ",\n".join(f'            "{k}" to floatArrayOf({", ".join(f"{n:.1f}f" for n in vals)})' for k, vals in sorted(kinds.items()))
    fn = "".join(w.capitalize() for w in key.split("_"))
    blocks.append((key, fn, entries))
    print(key, themes.THEMES[key]["sky"][0][1])

with open(kt, "w") as fh:
    fh.write("package app.passstudio.ui\n\n")
    fh.write("// Made by the art script (art-src/export_app.py); don't edit by hand.\n\n")
    fh.write("/**\n * Where each theme's moving parts are on its page art (1032 x 2240): chimneys, flames, lights, water and so on,\n"
             " * three numbers each (x, y, and a size or color).\n */\n")
    fh.write("object ThemeAnchors {\n    private val empty = FloatArray(0)\n\n")
    fh.write("    private val all: Map<String, Map<String, FloatArray>> by lazy {\n        mapOf(\n")
    fh.write("".join(f'            "{key}" to {fn[0].lower() + fn[1:]}(),\n' for key, fn, _ in blocks))
    fh.write("        )\n    }\n\n")
    fh.write("    fun of(art: String, kind: String): FloatArray = all[art]?.get(kind) ?: empty\n")
    for key, fn, entries in blocks:
        name = fn[0].lower() + fn[1:]
        body = f"mapOf(\n{entries},\n        )" if entries else "emptyMap()"
        fh.write(f"\n    private fun {name}(): Map<String, FloatArray> = {body}\n")
    fh.write("}\n")
print("wrote", kt)
