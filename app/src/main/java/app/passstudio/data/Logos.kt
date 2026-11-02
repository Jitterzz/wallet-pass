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
 * Logos picked on the phone. Each one is fitted to the round spot at the top of the pass and kept in the app's own
 * storage, so it shows with no signal (in a store, say). Saving blocks: call it off the main thread.
 */
object Logos {
    private const val SIZE = 660
    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val DARK = 0xFF202124.toInt()

    private fun dir(context: Context): File = File(context.applicationContext.filesDir, "logos").apply { mkdirs() }

    /** Where the logo called [name] is kept. */
    fun file(context: Context, name: String): File = File(dir(context), name)

    /** Fits a picked picture to the round spot, keeps it, and returns its name. */
    fun save(context: Context, bytes: ByteArray, passId: String): String {
        val png = fit(bytes)
        val name = "$passId-${System.currentTimeMillis()}.png"
        file(context, name).writeBytes(png)
        return name
    }

    /** Deletes kept logos that no pass uses anymore. */
    fun cleanUp(context: Context, inUse: Collection<String>) {
        val keep = inUse.filter { it.isNotBlank() }.toSet()
        dir(context).listFiles()?.forEach { if (it.name !in keep) it.delete() }
    }

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
    fun fit(bytes: ByteArray): ByteArray {
        val src = decode(bytes) ?: throw LogoException("That file isn't a picture. Pick a PNG.")
        val w = src.width
        val h = src.height
        val px = IntArray(w * h).also { src.getPixels(it, 0, w, 0, 0, w, h) }
        val around = around(px, w, h)
        val out = if (around == Around.Busy) {
            val side = minOf(w, h)
            place(src, Rect((w - side) / 2, (h - side) / 2, (w - side) / 2 + side, (h - side) / 2 + side), 1f, null, null)
        } else {
            val box = contentBox(px, w, h, around) ?: throw LogoException("That picture looks empty.")
            val fill = (around as? Around.Plain)?.color ?: if (isLight(px, w, box)) DARK else WHITE
            // Well inside the circle, so no corner of the logo touches its edge.
            place(src, box, 0.78f, 0.82f, fill)
        }
        val stream = ByteArrayOutputStream()
        out.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
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
}
