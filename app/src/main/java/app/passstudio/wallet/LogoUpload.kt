package app.passstudio.wallet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import app.passstudio.data.Settings
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Base64
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Logos picked on the phone: sized for Google Wallet and uploaded to the logos/ folder of the public GitHub repo,
 * because Google Wallet only loads pictures from a public link. All calls block: use a background thread.
 */
object LogoUpload {
    /** The two logo spots on a pass, and the size Google Wallet wants for each. */
    enum class Kind(val w: Int, val h: Int, val label: String) {
        WIDE(1280, 400, "wide"),
        ROUND(660, 660, "round"),
    }

    private val githubHeaders = mapOf("Accept" to "application/vnd.github+json", "X-GitHub-Api-Version" to "2022-11-28")

    fun hasToken(context: Context) = KeyVault.has(context, KeyVault.GITHUB_TOKEN)

    // ---------- Fitting a picture to its spot ----------

    private const val WHITE = 0xFFFFFFFF.toInt()
    private const val DARK = 0xFF202124.toInt()

    /** What's around the logo in a picture: nothing (see-through), one plain color, or a photo to the edges. */
    private sealed interface Around {
        data object Clear : Around
        class Plain(val color: Int) : Around
        data object Busy : Around
    }

    /**
     * Makes a picture fit its spot the way Google Wallet's guidelines ask, so nothing gets cut off:
     * - Round: a 660 × 660 square with a solid background and clear space around the logo, because Google Wallet
     *   cuts it into a circle. A see-through logo goes on white (on dark gray if the logo itself is light).
     * - Wide: a see-through 1280 × 400 picture with the logo centered.
     * Empty space around the logo in the file is trimmed first. A photo (no plain background) is cropped to the
     * spot's shape instead.
     */
    fun prepare(bytes: ByteArray, kind: Kind): ByteArray {
        val src = decode(bytes) ?: throw WalletException("That file isn't a picture. Pick a PNG.")
        val w = src.width
        val h = src.height
        val px = IntArray(w * h).also { src.getPixels(it, 0, w, 0, 0, w, h) }
        val around = around(px, w, h)
        val out = if (around == Around.Busy) {
            place(src, middle(w, h, kind.w.toFloat() / kind.h), kind, 1f, 1f, null, null)
        } else {
            val box = contentBox(px, w, h, around) ?: throw WalletException("That picture looks empty.")
            when (kind) {
                Kind.ROUND -> {
                    val fill = (around as? Around.Plain)?.color ?: if (isLight(px, w, box)) DARK else WHITE
                    // Well inside the circle Google Wallet cuts it into, so no corner of the logo touches the edge.
                    place(src, box, kind, 0.78f, 0.78f, 0.82f, fill)
                }
                Kind.WIDE -> {
                    val logo = if (around is Around.Plain) clearBackground(src, around.color) else src
                    place(logo, box, kind, 0.92f, 0.80f, null, null)
                }
            }
        }
        val stream = ByteArrayOutputStream()
        out.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /** True if a picture already fits its spot as Google Wallet shows it (a logo made by [prepare] always does). */
    fun fits(bitmap: Bitmap, kind: Kind): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return true
        val want = kind.w.toFloat() / kind.h
        if (abs(w.toFloat() / h - want) / want > 0.04f) return false
        if (kind == Kind.WIDE) return true
        // The round spot needs a solid background: see-through edges show up as a cut-off or empty ring.
        val border = edgePixels(w, h) { x, y -> bitmap.getPixel(x, y) }
        return border.count { Color.alpha(it) < 200 } * 2 < border.size
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
    private fun edgePixels(w: Int, h: Int, at: (Int, Int) -> Int): IntArray {
        val out = ArrayList<Int>(2 * (w + h))
        for (x in 0 until w) { out += at(x, 0); out += at(x, h - 1) }
        for (y in 1 until h - 1) { out += at(0, y); out += at(w - 1, y) }
        return out.toIntArray()
    }

    private fun distance(a: Int, b: Int): Float {
        val dr = (Color.red(a) - Color.red(b)).toFloat()
        val dg = (Color.green(a) - Color.green(b)).toFloat()
        val db = (Color.blue(a) - Color.blue(b)).toFloat()
        return sqrt(dr * dr + dg * dg + db * db)
    }

    private fun around(px: IntArray, w: Int, h: Int): Around {
        val edge = edgePixels(w, h) { x, y -> px[y * w + x] }
        if (edge.count { Color.alpha(it) < 24 } * 2 >= edge.size) return Around.Clear
        // The most common edge color (in steps of 8), then how much of the edge is close to it.
        val counts = HashMap<Int, Int>()
        for (c in edge) if (Color.alpha(c) >= 200) {
            val key = ((Color.red(c) shr 3) shl 10) or ((Color.green(c) shr 3) shl 5) or (Color.blue(c) shr 3)
            counts[key] = (counts[key] ?: 0) + 1
        }
        val top = counts.maxByOrNull { it.value }?.key ?: return Around.Busy
        val bucket = edge.filter { c ->
            Color.alpha(c) >= 200 &&
                (((Color.red(c) shr 3) shl 10) or ((Color.green(c) shr 3) shl 5) or (Color.blue(c) shr 3)) == top
        }
        val color = Color.rgb(bucket.map { Color.red(it) }.average().roundToInt(),
            bucket.map { Color.green(it) }.average().roundToInt(), bucket.map { Color.blue(it) }.average().roundToInt())
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
            val logo = Color.alpha(c) >= 24 && (plain == null || distance(c, plain) > 40f)
            if (logo) {
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

    /** The middle of a w × h picture with the given width-to-height shape. */
    private fun middle(w: Int, h: Int, ratio: Float): Rect {
        return if (w.toFloat() / h > ratio) {
            val cw = (h * ratio).roundToInt().coerceIn(1, w)
            Rect((w - cw) / 2, 0, (w - cw) / 2 + cw, h)
        } else {
            val ch = (w / ratio).roundToInt().coerceIn(1, h)
            Rect(0, (h - ch) / 2, w, (h - ch) / 2 + ch)
        }
    }

    /** Makes a plain background see-through, taking its tint out of the logo's soft edges. */
    private fun clearBackground(src: Bitmap, bg: Int): Bitmap {
        val w = src.width
        val h = src.height
        val px = IntArray(w * h).also { src.getPixels(it, 0, w, 0, 0, w, h) }
        for (i in px.indices) {
            val c = px[i]
            val a = ((distance(c, bg) - 16f) / 80f).coerceIn(0f, 1f) * (Color.alpha(c) / 255f)
            px[i] = when {
                a <= 0f -> 0
                a >= 1f -> c
                else -> {
                    fun un(v: Int, b: Int) = ((v - (1 - a) * b) / a).roundToInt().coerceIn(0, 255)
                    Color.argb((a * 255).roundToInt(), un(Color.red(c), Color.red(bg)), un(Color.green(c), Color.green(bg)),
                        un(Color.blue(c), Color.blue(bg)))
                }
            }
        }
        return Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
    }

    /**
     * Draws the [box] part of [src] centered on a new picture the size of [kind]'s spot, as big as fits in
     * [fitW] × [fitH] of it (and in a circle [diagonal] across, if given), over [fill] (see-through if null).
     */
    private fun place(src: Bitmap, box: Rect, kind: Kind, fitW: Float, fitH: Float, diagonal: Float?, fill: Int?): Bitmap {
        val out = Bitmap.createBitmap(kind.w, kind.h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        if (fill != null) canvas.drawColor(fill)
        val bw = box.width().toFloat()
        val bh = box.height().toFloat()
        var scale = minOf(kind.w * fitW / bw, kind.h * fitH / bh)
        if (diagonal != null) scale = minOf(scale, minOf(kind.w, kind.h) * diagonal / hypot(bw, bh))
        val dw = (bw * scale).roundToInt().coerceIn(1, kind.w)
        val dh = (bh * scale).roundToInt().coerceIn(1, kind.h)
        var piece = Bitmap.createBitmap(src, box.left, box.top, box.width(), box.height())
        // Shrinking in halves first keeps a big picture smooth.
        while (piece.width / 2 >= dw && piece.height / 2 >= dh) {
            piece = Bitmap.createScaledBitmap(piece, piece.width / 2, piece.height / 2, true)
        }
        if (piece.width != dw || piece.height != dh) piece = Bitmap.createScaledBitmap(piece, dw, dh, true)
        canvas.drawBitmap(piece, ((kind.w - dw) / 2).toFloat(), ((kind.h - dh) / 2).toFloat(), Paint(Paint.FILTER_BITMAP_FLAG))
        return out
    }

    // ---------- Pictures already on a pass ----------

    /** Where a link's picture is kept on the phone, so logos show with no signal (in a store, say). */
    fun cacheFile(context: Context, link: String): File {
        val name = MessageDigest.getInstance("SHA-1").digest(link.trim().toByteArray()).joinToString("") { "%02x".format(it) }
        return File(File(context.applicationContext.filesDir, "logo_cache").apply { mkdirs() }, name)
    }

    /** The picture at [link]: the copy on the phone if there is one, else downloaded. */
    fun fetch(context: Context, link: String): ByteArray {
        val file = cacheFile(context, link)
        if (file.exists()) return file.readBytes()
        val c = URL(link.trim()).openConnection() as HttpURLConnection
        c.connectTimeout = 10_000
        c.readTimeout = 20_000
        try {
            if (c.responseCode !in 200..299) throw WalletException("Couldn't load the logo (error ${c.responseCode}).")
            return c.inputStream.use { it.readBytes() }
        } finally {
            c.disconnect()
        }
    }

    /** Fits a logo that's already on a pass to its spot and uploads the fitted copy. Returns its link. */
    fun refit(context: Context, settings: Settings, link: String, kind: Kind, passId: String): String {
        val png = prepare(fetch(context, link), kind)
        return upload(context, settings, png, "$passId-${kind.label}-${System.currentTimeMillis()}.png")
    }

    private fun repoOf(settings: Settings): String {
        val repo = settings.githubRepo.trim().trim('/')
        if (!Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$").matches(repo)) {
            throw WalletException("Check the repo in Settings. It looks like Jitterzz/wallet-pass.")
        }
        return repo
    }

    private fun token(context: Context): String = KeyVault.load(context, KeyVault.GITHUB_TOKEN)?.trim().orEmpty()
        .ifEmpty { throw WalletException("Add your GitHub token in Settings to upload logos.") }

    /** Uploads [png] as logos/[name] and returns its public https link. */
    fun upload(context: Context, settings: Settings, png: ByteArray, name: String): String {
        val token = token(context)
        val repo = repoOf(settings)
        val branch = settings.githubBranch.trim().ifBlank { "main" }
        val path = "logos/$name"
        val body = JSONObject()
            .put("message", "Add logo $name (uploaded from Pass Studio)")
            .put("content", Base64.getEncoder().encodeToString(png))
            .put("branch", branch)
            .toString()
        val r = Http.request("PUT", "https://api.github.com/repos/$repo/contents/$path", body, bearer = token, headers = githubHeaders)
        if (r.code !in 200..299) throw WalletException(githubError(r))
        val link = runCatching { JSONObject(r.body).getJSONObject("content").optString("download_url") }.getOrNull()
            ?.takeIf { it.startsWith("https://") } ?: "https://raw.githubusercontent.com/$repo/$branch/$path"
        // Keep a copy on the phone, so the new logo shows right away.
        runCatching { cacheFile(context, link).writeBytes(png) }
        return link
    }

    /** Checks the token reaches the repo. Says so when the repo is private (Google Wallet couldn't load the logos). */
    fun check(context: Context, settings: Settings): String {
        val token = token(context)
        val repo = repoOf(settings)
        val r = Http.request("GET", "https://api.github.com/repos/$repo", bearer = token, headers = githubHeaders)
        if (r.code != 200) throw WalletException(githubError(r))
        val private = runCatching { JSONObject(r.body).optBoolean("private", false) }.getOrDefault(false)
        return if (private) "The token works, but $repo is private. Make it public so Google Wallet can load the pictures."
        else "Connected to $repo. Logo uploads are ready."
    }

    private fun githubError(r: Http.Response): String {
        val msg = runCatching { JSONObject(r.body).optString("message") }.getOrNull().orEmpty()
        val detail = if (msg.isNotBlank()) ": $msg" else "."
        return when (r.code) {
            401 -> "GitHub didn't accept the token. Check it in Settings."
            403 -> "The token can't write to that repo. Give it Contents: Read and write$detail"
            404 -> "GitHub can't find that repo with this token. Check the repo name and the token's access."
            422 -> "GitHub turned the upload down$detail"
            else -> "GitHub error ${r.code}$detail"
        }
    }
}
