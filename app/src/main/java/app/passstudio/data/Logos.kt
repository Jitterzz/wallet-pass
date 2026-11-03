package app.passstudio.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** A picture that couldn't be used as a logo, with a message to show. */
class LogoException(message: String) : Exception(message)

/**
 * Logos picked on the phone. Each one is kept in the app's own storage, so it shows with no signal (in a store, say),
 * in three versions: fitted to the round spot at the top of the pass, and big on its own (cut out of its background,
 * with a soft shadow), plain and with a thin white line around it. Saving blocks: call it off the main thread.
 */
object Logos {
    private const val SIZE = 660
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val DARK = 0xFF202124.toInt()

    private fun dir(context: Context): File = File(context.applicationContext.filesDir, "logos").apply { mkdirs() }

    /** Where the logo called [name] is kept. */
    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Keeps a picked picture as a logo (fitted to the round spot, and big), and returns its name. */
    fun save(context: Context, bytes: ByteArray, passId: String): String {
        val src = decode(bytes) ?: throw LogoException("That file isn't a picture. Pick a PNG.")
        val name = "$passId-${System.currentTimeMillis()}.png"
        file(context, name).writeBytes(encode(fit(src)))
        // The big versions. If one can't be made, the logo still works in its circle.
        runCatching {
            val cut = cutOut(src)
            file(context, bigName(name, outline = false)).writeBytes(encode(dressed(cut, outline = false)))
            file(context, bigName(name, outline = true)).writeBytes(encode(dressed(cut, outline = true)))
        }
        return name
    }

    /** The name of the big version of the logo called [name]: with a thin white line around it, or without. */
    fun bigName(name: String, outline: Boolean): String = name.removeSuffix(".png") + if (outline) "-edge.png" else "-big.png"

    /** True if the logo called [name] has its big versions (logos kept before there were any don't). */
    fun hasBig(context: Context, name: String): Boolean = name.isNotBlank() && file(context, bigName(name, true)).exists()

    /** The width and height of a kept logo picture, without loading it. */
    fun size(context: Context, name: String): Pair<Int, Int>? {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file(context, name).path, opts)
        return if (opts.outWidth > 0 && opts.outHeight > 0) opts.outWidth to opts.outHeight else null
    }

    private val bottoms = HashMap<String, Float>()

    /**
     * How far down the big logo picture called [name] the logo itself goes, from 0 to 1 (below that there's only its
     * soft shadow), so the text under it can sit close to the logo.
     */
    fun bodyBottom(context: Context, name: String): Float = synchronized(bottoms) {
        bottoms.getOrPut(name) {
            val bitmap = BitmapFactory.decodeFile(file(context, name).path) ?: return@getOrPut 1f
            val w = bitmap.width
            val h = bitmap.height
            val px = IntArray(w * h).also { bitmap.getPixels(it, 0, w, 0, 0, w, h) }
            var last = h - 1
            run {
                for (y in h - 1 downTo 0) for (x in 0 until w) if (px[y * w + x] ushr 24 >= 200) {
                    last = y
                    return@run
                }
            }
            (last + 1f) / h
        }
    }

    /** Deletes kept logos that no pass uses anymore. */
    fun cleanUp(context: Context, inUse: Collection<String>) {
        val keep = inUse.filter { it.isNotBlank() }.flatMap { listOf(it, bigName(it, true), bigName(it, false)) }.toSet()
        dir(context).listFiles()?.forEach { if (it.name !in keep) it.delete() }
    }

    private fun encode(bitmap: Bitmap): ByteArray =
        ByteArrayOutputStream().also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }.toByteArray()

    // ---------- Fitting a picture into the circle ----------

    /** What's around the logo in a picture: nothing (see-through), one plain color, or a photo to the edges. */
    private sealed interface Around {
        data object Clear : Around
        class Plain(val color: Int) : Around
        data object Busy : Around
    }

    /**
     * A 660 × 660 square for the round spot: the logo, trimmed of any empty space around it, centered well inside the
     * circle on a solid background (white for a see-through logo, dark gray if the logo itself is light, like white
     * letters; or the picture's own background color). A photo with no plain background is cropped to its middle.
     */
    fun fit(bytes: ByteArray): ByteArray =
        encode(fit(decode(bytes) ?: throw LogoException("That file isn't a picture. Pick a PNG.")))

    private fun fit(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val px = IntArray(w * h).also { src.getPixels(it, 0, w, 0, 0, w, h) }
        val around = around(px, w, h)
        return if (around == Around.Busy) {
            val side = minOf(w, h)
            place(src, Rect((w - side) / 2, (h - side) / 2, (w - side) / 2 + side, (h - side) / 2 + side), 1f, null, null)
        } else {
            val box = contentBox(px, w, h, around) ?: throw LogoException("That picture looks empty.")
            val fill = (around as? Around.Plain)?.color ?: if (isLight(px, w, box)) DARK else WHITE
            // Well inside the circle, so no corner of the logo touches its edge.
            place(src, box, 0.78f, 0.82f, fill)
        }
    }

    private fun decode(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1200) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }

    /** The pixels along the picture's four edges. */
    private fun edgePixels(px: IntArray, w: Int, h: Int): IntArray {
        val out = ArrayList<Int>(2 * (w + h))
        for (x in 0 until w) {
            out += px[x]
            out += px[(h - 1) * w + x]
        }
        for (y in 1 until h - 1) {
            out += px[y * w]
            out += px[y * w + w - 1]
        }
        return out.toIntArray()
    }

    private fun distance(a: Int, b: Int): Float {
        val dr = (Color.red(a) - Color.red(b)).toFloat()
        val dg = (Color.green(a) - Color.green(b)).toFloat()
        val db = (Color.blue(a) - Color.blue(b)).toFloat()
        return sqrt(dr * dr + dg * dg + db * db)
    }

    private fun bucket(c: Int) = ((Color.red(c) shr 3) shl 10) or ((Color.green(c) shr 3) shl 5) or (Color.blue(c) shr 3)

    private fun around(px: IntArray, w: Int, h: Int): Around {
        val edge = edgePixels(px, w, h)
        if (edge.count { Color.alpha(it) < 24 } * 2 >= edge.size) return Around.Clear
        // The most common edge color (in steps of 8), then how much of the edge is close to it.
        val counts = HashMap<Int, Int>()
        for (c in edge) if (Color.alpha(c) >= 200) counts[bucket(c)] = (counts[bucket(c)] ?: 0) + 1
        val top = counts.maxByOrNull { it.value }?.key ?: return Around.Busy
        val same = edge.filter { Color.alpha(it) >= 200 && bucket(it) == top }
        val color = Color.rgb(same.map { Color.red(it) }.average().roundToInt(),
            same.map { Color.green(it) }.average().roundToInt(), same.map { Color.blue(it) }.average().roundToInt())
        val near = edge.count { Color.alpha(it) >= 200 && distance(it, color) <= 28f }
        return if (near >= edge.size * 0.9f) Around.Plain(color) else Around.Busy
    }

    /** The smallest box holding everything that isn't background. */
    private fun contentBox(px: IntArray, w: Int, h: Int, around: Around): Rect? {
        var l = w
        var t = h
        var r = -1
        var b = -1
        val plain = (around as? Around.Plain)?.color
        for (y in 0 until h) for (x in 0 until w) {
            val c = px[y * w + x]
            if (Color.alpha(c) >= 24 && (plain == null || distance(c, plain) > 40f)) {
                if (x < l) l = x
                if (x > r) r = x
                if (y < t) t = y
                if (y > b) b = y
            }
        }
        return if (r < 0) null else Rect(l, t, r + 1, b + 1)
    }

    /** True if the logo is mostly light (white letters, say), so it needs a dark background. */
    private fun isLight(px: IntArray, w: Int, box: Rect): Boolean {
        var sum = 0.0
        var weight = 0.0
        for (y in box.top until box.bottom) for (x in box.left until box.right) {
            val c = px[y * w + x]
            val a = Color.alpha(c) / 255.0
            if (a < 0.1) continue
            sum += a * (0.299 * Color.red(c) + 0.587 * Color.green(c) + 0.114 * Color.blue(c)) / 255.0
            weight += a
        }
        return weight > 0 && sum / weight > 0.75
    }

    /**
     * Draws the [box] part of [src] centered on a new 660 × 660 picture over [fill] (see-through if null), as big as
     * fits in [fit] of its width and height and, if given, in a circle [diagonal] of its width across.
     */
    private fun place(src: Bitmap, box: Rect, fit: Float, diagonal: Float?, fill: Int?): Bitmap {
        val out = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        if (fill != null) canvas.drawColor(fill)
        val bw = box.width().toFloat()
        val bh = box.height().toFloat()
        var scale = minOf(SIZE * fit / bw, SIZE * fit / bh)
        if (diagonal != null) scale = minOf(scale, SIZE * diagonal / hypot(bw, bh))
        val dw = (bw * scale).roundToInt().coerceIn(1, SIZE)
        val dh = (bh * scale).roundToInt().coerceIn(1, SIZE)
        var piece = Bitmap.createBitmap(src, box.left, box.top, box.width(), box.height())
        // Shrinking in halves first keeps a big picture smooth.
        while (piece.width / 2 >= dw && piece.height / 2 >= dh) {
            piece = Bitmap.createScaledBitmap(piece, piece.width / 2, piece.height / 2, true)
        }
        if (piece.width != dw || piece.height != dh) piece = Bitmap.createScaledBitmap(piece, dw, dh, true)
        canvas.drawBitmap(piece, ((SIZE - dw) / 2).toFloat(), ((SIZE - dh) / 2).toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        return out
    }

    // ---------- The big logo, cut out of its background ----------

    /**
     * The logo on its own: a plain background around it (white, say) taken away, trimmed to what's left, and sized
     * so its longer side is about 900 pixels. A photo with no plain background is kept whole, with rounded corners.
     */
    private fun cutOut(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val px = IntArray(w * h).also { src.getPixels(it, 0, w, 0, 0, w, h) }
        when (val around = around(px, w, h)) {
            Around.Clear -> stripWhiteRim(px, w, h)
            is Around.Plain -> removeBackground(px, w, h, around.color)
            Around.Busy -> roundCorners(px, w, h)
        }
        var l = w
        var t = h
        var r = -1
        var b = -1
        for (y in 0 until h) for (x in 0 until w) {
            if (Color.alpha(px[y * w + x]) >= 24) {
                if (x < l) l = x
                if (x > r) r = x
                if (y < t) t = y
                if (y > b) b = y
            }
        }
        if (r < 0) throw LogoException("That picture looks empty.")
        val cw = r - l + 1
        val ch = b - t + 1
        var cut = Bitmap.createBitmap(cw, ch, Bitmap.Config.ARGB_8888)
        cut.setPixels(px, t * w + l, w, 0, 0, cw, ch)
        // About 900 pixels on its longer side: shrunk in halves to stay smooth, or grown a little if it's small.
        val target = 900
        while (maxOf(cut.width, cut.height) / 2 >= target) cut = Bitmap.createScaledBitmap(cut, cut.width / 2, cut.height / 2, true)
        val scale = target.toFloat() / maxOf(cut.width, cut.height)
        if (scale < 1f || scale > 1.5f) {
            cut = Bitmap.createScaledBitmap(cut, (cut.width * scale).roundToInt().coerceAtLeast(1),
                (cut.height * scale).roundToInt().coerceAtLeast(1), true)
        }
        return cut
    }

    /**
     * Takes away the plain [bg] color around the logo: everything close to it that reaches the picture's edge (or, if
     * [fromHoles], any see-through spot, like the middle of an O). The logo's own edge pixels, part logo and part
     * background, keep just their logo part, so there's no halo.
     */
    private fun removeBackground(px: IntArray, w: Int, h: Int, bg: Int, fromHoles: Boolean = false) {
        val near = 48f
        val far = 150f
        val isBg = BooleanArray(w * h)
        val queue = IntArray(w * h)
        var head = 0
        var tail = 0
        fun visit(i: Int) {
            if (!isBg[i] && (Color.alpha(px[i]) < 24 || distance(px[i], bg) <= near)) {
                isBg[i] = true
                queue[tail++] = i
            }
        }
        for (x in 0 until w) {
            visit(x)
            visit((h - 1) * w + x)
        }
        for (y in 0 until h) {
            visit(y * w)
            visit(y * w + w - 1)
        }
        if (fromHoles) for (i in 0 until w * h) if (Color.alpha(px[i]) < 24) visit(i)
        while (head < tail) {
            val i = queue[head++]
            val x = i % w
            val y = i / w
            if (x > 0) visit(i - 1)
            if (x < w - 1) visit(i + 1)
            if (y > 0) visit(i - w)
            if (y < h - 1) visit(i + w)
        }
        val br = Color.red(bg)
        val bgG = Color.green(bg)
        val bb = Color.blue(bg)
        val edge = ArrayList<Int>()
        for (y in 0 until h) for (x in 0 until w) {
            val i = y * w + x
            if (isBg[i]) continue
            var touches = false
            for (dy in -2..2) for (dx in -2..2) {
                val xx = x + dx
                val yy = y + dy
                if (xx in 0 until w && yy in 0 until h && isBg[yy * w + xx]) touches = true
            }
            if (touches) edge += i
        }
        for (i in edge) {
            val c = px[i]
            val a = ((distance(c, bg) - near) / (far - near)).coerceIn(0f, 1f)
            if (a >= 1f) continue
            if (a <= 0.02f) {
                px[i] = 0
                continue
            }
            // c = a * logo + (1 - a) * bg, so the logo's own color is (c - (1 - a) * bg) / a.
            fun part(v: Int, b: Int) = ((v - (1f - a) * b) / a).roundToInt().coerceIn(0, 255)
            px[i] = Color.argb((a * Color.alpha(c)).roundToInt(), part(Color.red(c), br), part(Color.green(c), bgG), part(Color.blue(c), bb))
        }
        for (i in 0 until w * h) if (isBg[i]) px[i] = 0
    }

    /**
     * Takes off a white border drawn around a see-through logo (a sticker-style edge), so a big logo shows just its
     * own shape. A logo that's mostly white itself (white letters, say) is left as it is.
     */
    private fun stripWhiteRim(px: IntArray, w: Int, h: Int) {
        val before = px.count { Color.alpha(it) >= 128 }
        val stripped = px.copyOf()
        removeBackground(stripped, w, h, WHITE, fromHoles = true)
        val after = stripped.count { Color.alpha(it) >= 128 }
        if (before > 0 && after >= before * 0.35f) stripped.copyInto(px)
    }

    /** Rounds a photo's corners (smoothly), for a big logo that has no plain background to take away. */
    private fun roundCorners(px: IntArray, w: Int, h: Int) {
        val r = minOf(w, h) * 0.1f
        for (y in 0 until h) for (x in 0 until w) {
            val dx = maxOf(r - x - 0.5f, x + 0.5f - (w - r), 0f)
            val dy = maxOf(r - y - 0.5f, y + 0.5f - (h - r), 0f)
            if (dx == 0f && dy == 0f) continue
            val cover = (r - hypot(dx, dy) + 0.5f).coerceIn(0f, 1f)
            val i = y * w + x
            px[i] = Color.argb((Color.alpha(px[i]) * cover).roundToInt(), Color.red(px[i]), Color.green(px[i]), Color.blue(px[i]))
        }
    }

    /**
     * The cut-out logo ready to show big: with a thin white line around its shape if [outline], and a soft shadow under
     * it so it lifts off the theme's art. Room is left around it for both.
     */
    private fun dressed(cut: Bitmap, outline: Boolean): Bitmap {
        val m = maxOf(cut.width, cut.height)
        val edge = if (outline) maxOf(3, (m * 0.008f).roundToInt()) else 0
        val soft = maxOf(4, (m * 0.03f).roundToInt())
        val drop = maxOf(2, (m * 0.018f).roundToInt())
        val pad = edge + soft * 2 + drop
        val w = cut.width + pad * 2
        val h = cut.height + pad * 2
        val alpha = IntArray(w * h)
        val logo = IntArray(cut.width * cut.height).also { cut.getPixels(it, 0, cut.width, 0, 0, cut.width, cut.height) }
        for (y in 0 until cut.height) for (x in 0 until cut.width) alpha[(y + pad) * w + x + pad] = Color.alpha(logo[y * cut.width + x])
        val shape = if (outline) grow(alpha, w, h, edge) else alpha
        val shadow = blur(shape, w, h, maxOf(2, soft / 2))
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(Bitmap.createBitmap(IntArray(w * h) { (shadow[it] * 0.42f).roundToInt() shl 24 }, w, h, Bitmap.Config.ARGB_8888),
            0f, drop.toFloat(), paint)
        if (outline) {
            canvas.drawBitmap(Bitmap.createBitmap(IntArray(w * h) { (shape[it] shl 24) or 0xFFFFFF }, w, h, Bitmap.Config.ARGB_8888),
                0f, 0f, paint)
        }
        canvas.drawBitmap(cut, pad.toFloat(), pad.toFloat(), paint)
        // Trimmed to what shows, so the logo sits as close to the text under it as it looks.
        val px = IntArray(w * h).also { out.getPixels(it, 0, w, 0, 0, w, h) }
        var l = w
        var t = h
        var r = -1
        var b = -1
        for (y in 0 until h) for (x in 0 until w) {
            if (px[y * w + x] ushr 24 >= 4) {
                if (x < l) l = x
                if (x > r) r = x
                if (y < t) t = y
                if (y > b) b = y
            }
        }
        return if (r < 0) out else Bitmap.createBitmap(out, l, t, r - l + 1, b - t + 1)
    }

    /**
     * The shape of [alpha] grown by [r] pixels all around, with a smooth edge: a distance from the logo worked out in
     * two passes over the picture (steps of 5 across, 7 diagonally and 11 for a knight's move, so it's nearly round).
     */
    private fun grow(alpha: IntArray, w: Int, h: Int, r: Int): IntArray {
        val far = Int.MAX_VALUE / 4
        val d = IntArray(w * h) { if (alpha[it] >= 128) 0 else far }
        for (y in 0 until h) for (x in 0 until w) {
            val i = y * w + x
            var v = d[i]
            if (v == 0) continue
            if (x > 0) v = minOf(v, d[i - 1] + 5)
            if (y > 0) {
                v = minOf(v, d[i - w] + 5)
                if (x > 0) v = minOf(v, d[i - w - 1] + 7)
                if (x < w - 1) v = minOf(v, d[i - w + 1] + 7)
                if (x > 1) v = minOf(v, d[i - w - 2] + 11)
                if (x < w - 2) v = minOf(v, d[i - w + 2] + 11)
                if (y > 1) {
                    if (x > 0) v = minOf(v, d[i - 2 * w - 1] + 11)
                    if (x < w - 1) v = minOf(v, d[i - 2 * w + 1] + 11)
                }
            }
            d[i] = v
        }
        for (y in h - 1 downTo 0) for (x in w - 1 downTo 0) {
            val i = y * w + x
            var v = d[i]
            if (v == 0) continue
            if (x < w - 1) v = minOf(v, d[i + 1] + 5)
            if (y < h - 1) {
                v = minOf(v, d[i + w] + 5)
                if (x < w - 1) v = minOf(v, d[i + w + 1] + 7)
                if (x > 0) v = minOf(v, d[i + w - 1] + 7)
                if (x < w - 2) v = minOf(v, d[i + w + 2] + 11)
                if (x > 1) v = minOf(v, d[i + w - 2] + 11)
                if (y < h - 2) {
                    if (x < w - 1) v = minOf(v, d[i + 2 * w + 1] + 11)
                    if (x > 0) v = minOf(v, d[i + 2 * w - 1] + 11)
                }
            }
            d[i] = v
        }
        return IntArray(w * h) { i -> maxOf(alpha[i], ((r + 0.5f - d[i] / 5f).coerceIn(0f, 1f) * 255f).roundToInt()) }
    }

    /** A soft blur of [a] (three box blurs of radius [r] one after another, across and down), for the shadow. */
    private fun blur(a: IntArray, w: Int, h: Int, r: Int): IntArray {
        val cur = a.copyOf()
        val tmp = IntArray(w * h)
        val n = 2 * r + 1
        repeat(3) {
            for (y in 0 until h) {
                val row = y * w
                var sum = 0
                for (k in -r..r) sum += cur[row + k.coerceIn(0, w - 1)]
                for (x in 0 until w) {
                    tmp[row + x] = sum / n
                    sum += cur[row + minOf(x + r + 1, w - 1)] - cur[row + maxOf(x - r, 0)]
                }
            }
            for (x in 0 until w) {
                var sum = 0
                for (k in -r..r) sum += tmp[k.coerceIn(0, h - 1) * w + x]
                for (y in 0 until h) {
                    cur[y * w + x] = sum / n
                    sum += tmp[minOf(y + r + 1, h - 1) * w + x] - tmp[maxOf(y - r, 0) * w + x]
                }
            }
        }
        return cur
    }
}
