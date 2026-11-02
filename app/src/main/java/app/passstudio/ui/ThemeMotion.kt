package app.passstudio.ui

import android.animation.ValueAnimator
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import app.passstudio.data.Store
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/*
 * The moving parts of each theme, drawn over its page art: snow and leaves falling, fireworks, flickering candles
 * and windows, chimney smoke, bats and birds flying by, twinkling lights, the aurora rippling, water glinting.
 *
 * Everything is worked out in the page art's own pixels (1032 x 2240) and placed the way the art is on screen, so it
 * lines up with the picture. Each frame is just a function of the time, so nothing is kept between frames.
 */

private const val ART_W = 1032f
private const val ART_H = 2240f

/** Where the page art lands on the screen: scaled to cover it, its bottom on the bottom (as the picture behind is). */
class ArtMap(w: Float, h: Float) {
    val scale = max(w / ART_W, h / ART_H)
    private val ox = (w - ART_W * scale) / 2f
    private val oy = h - ART_H * scale

    /** The art's y at the top of the screen. */
    val top = -oy / scale
    fun x(ax: Float) = ox + ax * scale
    fun y(ay: Float) = oy + ay * scale
    fun d(v: Float) = v * scale
    fun at(ax: Float, ay: Float) = Offset(x(ax), y(ay))
}

/** One moving thing on a page, drawn as it is [t] seconds in. */
fun interface Motion {
    fun draw(d: DrawScope, t: Float, m: ArtMap)
}

/**
 * The theme's moving parts, over its page art. They play while the page is up, unless they're turned off in
 * Settings or the phone has animations off; then they hold still.
 */
@Composable
fun ThemeMotion(art: String, modifier: Modifier = Modifier) {
    val d by Store.data.collectAsState()
    val motions = remember(art) { Motions.of(art) }
    if (motions.isEmpty()) return
    val animatorsOn = remember { ValueAnimator.areAnimatorsEnabled() }
    val playing = d.settings.motion && animatorsOn
    val time = remember { mutableFloatStateOf(0f) }
    if (playing) {
        LaunchedEffect(art) {
            var start = -1L
            while (true) {
                withFrameNanos { now ->
                    if (start < 0) start = now
                    time.floatValue = (now - start) / 1_000_000_000f
                }
            }
        }
    }
    Canvas(modifier) {
        val m = ArtMap(size.width, size.height)
        val t = time.floatValue
        motions.forEach { it.draw(this, t, m) }
    }
}

// ---------- helpers ----------

/** A steady pseudo-random number in [0, 1) for (a, b): the same every frame, so things keep their place. */
private fun hash(a: Int, b: Int): Float {
    var h = a * 374761393 + b * 668265263
    h = (h xor (h ushr 13)) * 1274126177
    h = h xor (h ushr 16)
    return (h and 0x7fffffff) / 2147483648f
}

private fun frac(v: Float) = v - floor(v)

private fun rgb(v: Float) = Color(0xFF000000L or v.toLong())

/** Where the pass sits on the page (logo, card name, name, barcode, number), so twinkles stay off it. */
private val PASS = arrayOf(
    floatArrayOf(380f, 255f, 655f, 532f), floatArrayOf(280f, 548f, 752f, 632f), floatArrayOf(56f, 638f, 976f, 880f),
    floatArrayOf(60f, 884f, 972f, 1332f), floatArrayOf(320f, 1336f, 712f, 1412f),
)

private fun clearOfPass(x: Float, y: Float, pad: Float) =
    PASS.none { b -> x > b[0] - pad && x < b[2] + pad && y > b[1] - pad && y < b[3] + pad }

/** Up to [n] steady spots between [y0] and [y1] that stay off the pass. */
private fun spots(n: Int, y0: Float, y1: Float, seed: Int, pad: Float = 24f): List<Offset> {
    val out = ArrayList<Offset>(n)
    var i = 0
    while (out.size < n && i < n * 40) {
        val x = 16f + hash(seed, i * 2) * (ART_W - 32f)
        val y = y0 + hash(seed, i * 2 + 1) * (y1 - y0)
        if (clearOfPass(x, y, pad)) out += Offset(x, y)
        i++
    }
    return out
}

private fun parse(d: String): Path = PathParser().parsePathString(d).toPath()

private val MAPLE: Path by lazy {
    parse("M0,-1 L0.12,-0.62 L0.3,-0.72 L0.27,-0.38 L0.62,-0.55 L0.56,-0.28 L0.95,-0.22 L0.72,-0.02 L0.82,0.12 L0.38,0.12 " +
        "L0.42,0.32 L0.06,0.2 L0.04,0.66 L-0.04,0.66 L-0.06,0.2 L-0.42,0.32 L-0.38,0.12 L-0.82,0.12 L-0.72,-0.02 L-0.95,-0.22 " +
        "L-0.56,-0.28 L-0.62,-0.55 L-0.27,-0.38 L-0.3,-0.72 L-0.12,-0.62Z")
}
private val HEART: Path by lazy {
    parse("M0,0.32 C-0.52,-0.05 -0.52,-0.32 -0.52,-0.32 C-0.52,-0.6 -0.24,-0.68 0,-0.44 C0.24,-0.68 0.52,-0.6 0.52,-0.32 " +
        "C0.52,-0.05 0,0.32 0,0.32Z")
}
private val SPARKLE: Path by lazy {
    parse("M0,-1 Q0.22,-0.22 1,0 Q0.22,0.22 0,1 Q-0.22,0.22 -1,0 Q-0.22,-0.22 0,-1Z")
}
private val BAT: Path by lazy {
    parse("M0,-2 C-5,-8 -13,-9 -22,-5 C-19,-3 -18,1 -20,5 C-15,3 -11,4 -8,7 C-6,4 -3,3 -1.5,4.5 L0,3 L1.5,4.5 C3,3 6,4 8,7 " +
        "C11,4 15,3 20,5 C18,1 19,-3 22,-5 C13,-9 5,-8 0,-2Z")
}
private val EAGLE: Path by lazy {
    parse("M-70,-6 Q-40,-22 -12,-6 Q0,-12 12,-6 Q40,-22 70,-6 Q46,-8 20,2 Q8,6 0,4 Q-8,6 -20,2 Q-46,-8 -70,-6Z M-6,2 L0,16 L6,2Z")
}
private val TUMBLEWEED: Path by lazy {
    val sb = StringBuilder()
    for (i in 0 until 24) {
        val a1 = hash(77, i) * 6.2832f
        val a2 = hash(78, i) * 6.2832f
        val r1 = 0.3f + 0.7f * hash(79, i)
        val r2 = 0.3f + 0.7f * hash(80, i)
        sb.append("M${r1 * cos(a1)},${r1 * sin(a1)} Q0,0 ${r2 * cos(a2)},${r2 * sin(a2)} ")
    }
    parse(sb.toString())
}

private fun DrawScope.glow(c: Offset, r: Float, color: Color, alpha: Float) {
    if (r <= 0f || alpha <= 0f) return
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha.coerceIn(0f, 1f)), Color.Transparent), c, r), r, c)
}

private fun DrawScope.shape(path: Path, x: Float, y: Float, size: Float, rot: Float, color: Color, alpha: Float, sx: Float = 1f) {
    withTransform({
        translate(x, y)
        rotate(rot, Offset.Zero)
        scale(size * sx, size, Offset.Zero)
    }) { drawPath(path, color, alpha) }
}

// ---------- falling and rising things ----------

/** What falls: snow, leaves, petals, confetti... */
enum class Piece { SNOW, FLAKE, MAPLE, PETAL, CONFETTI, HEART, SHAMROCK }

private fun DrawScope.piece(p: Piece, x: Float, y: Float, s: Float, rot: Float, phase: Float, c: Color, a: Float) {
    when (p) {
        Piece.SNOW -> drawCircle(c, s / 2f, Offset(x, y), a)
        Piece.FLAKE -> withTransform({ translate(x, y); rotate(rot, Offset.Zero) }) {
            for (k in 0 until 6) {
                val ang = k * PI.toFloat() / 3f
                val ex = cos(ang) * s / 2f
                val ey = sin(ang) * s / 2f
                drawLine(c, Offset.Zero, Offset(ex, ey), s * 0.09f, StrokeCap.Round, alpha = a)
                drawLine(c, Offset(ex * 0.6f, ey * 0.6f), Offset(ex * 0.6f + cos(ang + 0.7f) * s * 0.15f, ey * 0.6f + sin(ang + 0.7f) * s * 0.15f),
                    s * 0.07f, StrokeCap.Round, alpha = a)
                drawLine(c, Offset(ex * 0.6f, ey * 0.6f), Offset(ex * 0.6f + cos(ang - 0.7f) * s * 0.15f, ey * 0.6f + sin(ang - 0.7f) * s * 0.15f),
                    s * 0.07f, StrokeCap.Round, alpha = a)
            }
        }
        Piece.MAPLE -> shape(MAPLE, x, y, s / 2f, rot, c, a, sx = 0.35f + 0.65f * abs(cos(phase * 1.3f)))
        Piece.PETAL -> withTransform({ translate(x, y); rotate(rot, Offset.Zero); scale(0.4f + 0.6f * abs(cos(phase * 1.7f)), 1f, Offset.Zero) }) {
            drawOval(c, Offset(-s / 2f, -s * 0.3f), Size(s, s * 0.6f), a)
        }
        Piece.CONFETTI -> withTransform({ translate(x, y); rotate(rot, Offset.Zero); scale(abs(cos(phase * 3f)) + 0.05f, 1f, Offset.Zero) }) {
            drawRect(c, Offset(-s / 2f, -s / 4f), Size(s, s / 2f), a)
        }
        Piece.HEART -> shape(HEART, x, y, s, rot, c, a)
        Piece.SHAMROCK -> for (k in 0 until 3) shape(HEART, x, y, s * 0.8f, rot + k * 120f, c, a)
    }
}

/** [n] pieces falling down the page, swaying, each at its own speed. */
private fun fall(p: Piece, n: Int, speed: Float, sway: Float, size: ClosedFloatingPointRange<Float>, colors: LongArray, seed: Int,
                 alpha: Float = 1f, spin: Float = 120f) = Motion { d, t, m ->
    val y0 = m.top - 80f
    val span = ART_H + 160f - m.top
    for (i in 0 until n) {
        val sp = speed * (0.6f + 0.8f * hash(seed, i * 9))
        val y = y0 + frac(hash(seed, i * 9 + 1) + t * sp / span) * span
        val ph = hash(seed, i * 9 + 2) * 6.2832f
        val x = hash(seed, i * 9 + 3) * ART_W + sway * sin(t * (0.4f + 0.8f * hash(seed, i * 9 + 4)) + ph)
        val s = size.start + (size.endInclusive - size.start) * hash(seed, i * 9 + 5)
        val rot = hash(seed, i * 9 + 6) * 360f + t * spin * (hash(seed, i * 9 + 7) - 0.5f) * 2f
        d.piece(p, m.x(x), m.y(y), m.d(s), rot, t + ph, Color(colors[i % colors.size]), alpha)
    }
}

/** Pieces floating up the page (hearts), fading out near the top. */
private fun rise(p: Piece, n: Int, speed: Float, sway: Float, size: ClosedFloatingPointRange<Float>, colors: LongArray, seed: Int) =
    Motion { d, t, m ->
        val span = ART_H + 160f - m.top
        for (i in 0 until n) {
            val sp = speed * (0.6f + 0.8f * hash(seed, i * 7))
            val y = ART_H + 80f - frac(hash(seed, i * 7 + 1) + t * sp / span) * span
            val ph = hash(seed, i * 7 + 2) * 6.2832f
            val x = hash(seed, i * 7 + 3) * ART_W + sway * sin(t * 0.9f + ph)
            val s = size.start + (size.endInclusive - size.start) * hash(seed, i * 7 + 4)
            val a = ((y - (m.top + 60f)) / 500f).coerceIn(0f, 1f) * 0.85f
            d.piece(p, m.x(x), m.y(y), m.d(s), sin(t * 1.3f + ph) * 15f, ph, Color(colors[i % colors.size]), a)
        }
    }

// ---------- lights ----------

/** Stars or sparkles fading in and out, off the pass. */
private fun twinkle(n: Int, y0: Float, y1: Float, color: Long, size: Float, seed: Int, star: Boolean = true): Motion {
    val pts = spots(n, y0, y1, seed)
    return Motion { d, t, m ->
        pts.forEachIndexed { i, p ->
            val a = (0.5f + 0.5f * sin(t * (1.1f + 2f * hash(seed, i + 500)) + i * 1.9f)).pow(2)
            if (a > 0.03f) {
                val r = m.d(size * (0.6f + 0.6f * hash(seed, i + 900)))
                if (star) d.shape(SPARKLE, m.x(p.x), m.y(p.y), r * (0.7f + 0.3f * a), 0f, Color(color), a)
                else d.drawCircle(Color(color), r * 0.3f, m.at(p.x, p.y), a)
            }
        }
    }
}

/** Warm glows that flicker, at each of the anchors (candles, windows, lanterns, lamps). */
private fun flicker(points: FloatArray, color: Long, reach: Float, strength: Float, speed: Float = 1f) = Motion { d, t, m ->
    var i = 0
    while (i + 2 < points.size) {
        val k = i / 3
        val f = 0.72f + 0.28f * sin(t * 9f * speed + k * 1.7f) * sin(t * 13.3f * speed + k * 2.3f)
        d.glow(m.at(points[i], points[i + 1]), m.d(max(points[i + 2], 8f) * reach * (0.88f + 0.12f * f)), Color(color), strength * f)
        i += 3
    }
}

/** A slow, soft swell of light (the sun, the pot of gold, the star on the tree). */
private fun pulse(points: FloatArray, color: Long, reach: Float, strength: Float, speed: Float = 0.8f) = Motion { d, t, m ->
    var i = 0
    while (i + 2 < points.size) {
        val f = 0.5f + 0.5f * sin(t * speed + i)
        d.glow(m.at(points[i], points[i + 1]), m.d(points[i + 2] * reach * (0.92f + 0.08f * f)), Color(color), strength * (0.55f + 0.45f * f))
        i += 3
    }
}

/** String lights and tree lights: each bulb's color (stored with it) brightening and dimming in turn. */
private fun bulbs(points: FloatArray, speed: Float = 2.6f, reach: Float = 24f) = Motion { d, t, m ->
    var i = 0
    var k = 0
    while (i + 2 < points.size) {
        val f = 0.5f + 0.5f * sin(t * speed + k * 1.1f)
        d.glow(m.at(points[i], points[i + 1]), m.d(reach * (0.6f + 0.4f * f)), rgb(points[i + 2]), 0.75f * f)
        i += 3
        k++
    }
}

/** Red lights on towers, blinking. */
private fun blink(points: FloatArray) = Motion { d, t, m ->
    var i = 0
    var k = 0
    while (i + 2 < points.size) {
        val on = frac(t / 1.8f + k * 0.37f) < 0.45f
        d.glow(m.at(points[i], points[i + 1]), m.d(if (on) 28f else 12f), Color(0xFFFF4D4D), if (on) 0.9f else 0.3f)
        i += 3
        k++
    }
}

/** Lights chasing around a Ferris wheel's rim. */
private fun chase(points: FloatArray, colors: LongArray) = Motion { d, t, m ->
    if (points.size < 3) return@Motion
    val x = points[0]
    val y = points[1]
    val r = points[2]
    for (k in 0 until 16) {
        val a = 2f * PI.toFloat() * k / 16f
        val f = max(0f, cos(a - t * 1.6f)).pow(6)
        if (f > 0.02f) d.glow(m.at(x + r * cos(a), y + r * sin(a)), m.d(24f), Color(colors[k % colors.size]), 0.9f * f)
    }
}

/** A few lit windows in town going dark now and then (painted over in their building's color). */
private fun windowsOff(points: FloatArray) = Motion { d, t, m ->
    var i = 0
    var k = 0
    while (i + 2 < points.size) {
        if (sin(t * 0.45f + k * 2.3f) > 0.55f) {
            d.drawRect(rgb(points[i + 2]), Offset(m.x(points[i] - 4f), m.y(points[i + 1] - 5f)), Size(m.d(8f), m.d(10f)))
        }
        i += 3
        k++
    }
}

// ---------- smoke, fire and sparks ----------

/** Puffs of smoke rising from chimneys. */
private fun smoke(points: FloatArray, color: Color = Color.White) = Motion { d, t, m ->
    var i = 0
    while (i + 2 < points.size) {
        for (k in 0 until 7) {
            val p = frac(t / 5f + k / 7f + i * 0.13f)
            val px = points[i] + p * 44f + sin(t * 0.9f + k) * 6f * p
            val py = points[i + 1] - p * 210f
            val a = 0.42f * (1f - p) * min(1f, p * 5f)
            d.drawCircle(color, m.d(8f + p * 28f), m.at(px, py), a)
        }
        i += 3
    }
}

/** Sparks drifting up from a campfire. */
private fun sparks(points: FloatArray, seed: Int) = Motion { d, t, m ->
    var i = 0
    while (i + 2 < points.size) {
        for (k in 0 until 10) {
            val p = frac(t / 2.4f + hash(seed, k))
            val px = points[i] + (hash(seed, k + 20) - 0.5f) * 50f + sin(t * 2f + k) * 10f * p
            val py = points[i + 1] - 20f - p * 220f
            d.drawCircle(Color(0xFFFFCF6A), m.d(2.6f * (1f - p) + 0.8f), m.at(px, py), (1f - p) * 0.9f)
        }
        i += 3
    }
}

/** Sparklers crackling: short bright lines around each, changing many times a second. */
private fun crackle(points: FloatArray, seed: Int) = Motion { d, t, m ->
    val frame = floor(t * 14f).toInt()
    var i = 0
    while (i + 2 < points.size) {
        val c = m.at(points[i], points[i + 1])
        d.glow(c, m.d(30f), Color(0xFFFFF3C4), 0.6f + 0.3f * hash(seed, frame + i))
        for (k in 0 until 9) {
            val a = hash(seed, frame * 31 + k + i * 7) * 2f * PI.toFloat()
            val len = 8f + 22f * hash(seed, frame * 17 + k + i)
            d.drawLine(Color(0xFFFFF3C4), c, Offset(c.x + m.d(len) * cos(a), c.y + m.d(len) * sin(a)), m.d(2f), StrokeCap.Round)
        }
        i += 3
    }
}

// ---------- things in the sky ----------

/** Fireworks: [n] shells at a time going up and bursting, each somewhere new, in the given areas. */
private fun fireworks(n: Int, colors: LongArray, seed: Int, areas: Array<FloatArray>, period: Float = 2.8f) = Motion { d, t, m ->
    for (i in 0 until n) {
        val u = (t + i * period / n + hash(seed, i) * 0.5f) / period
        val cycle = floor(u).toInt()
        val p = u - cycle
        val area = areas[(hash(seed, cycle * 7 + i * 3) * areas.size).toInt().coerceIn(0, areas.size - 1)]
        val cx = area[0] + hash(seed, cycle * 11 + i) * (area[2] - area[0])
        val cy = area[1] + hash(seed, cycle * 13 + i) * (area[3] - area[1])
        val color = Color(colors[(hash(seed, cycle * 17 + i) * colors.size).toInt().coerceIn(0, colors.size - 1)])
        val radius = 70f + 80f * hash(seed, cycle * 19 + i)
        if (p < 0.2f) {
            val q = p / 0.2f
            val sy = cy + 260f * (1f - q)
            d.drawLine(color, m.at(cx, sy + 46f), m.at(cx, sy), m.d(3f), StrokeCap.Round, alpha = 0.6f)
            d.drawCircle(Color.White, m.d(3.2f), m.at(cx, sy), 0.95f)
        } else {
            val q = (p - 0.2f) / 0.8f
            val r = radius * (1f - (1f - q).pow(3))
            val a = (1f - q).pow(1.4f)
            val sag = 46f * q * q
            d.glow(m.at(cx, cy), m.d(r * 1.5f + 1f), color, 0.3f * a)
            val rays = 22
            val turn = hash(seed, cycle + 5) * 0.3f
            for (k in 0 until rays) {
                val ang = 2f * PI.toFloat() * k / rays + turn
                val ex = cx + r * cos(ang)
                val ey = cy + r * sin(ang) + sag
                d.drawLine(color, m.at(cx + r * 0.7f * cos(ang), cy + r * 0.7f * sin(ang) + sag * 0.6f), m.at(ex, ey), m.d(2.8f),
                    StrokeCap.Round, alpha = a)
                d.drawCircle(Color.White, m.d(2.4f), m.at(ex, ey), a)
            }
        }
    }
}

/** A shooting star now and then. */
private fun shootingStars(seed: Int, y0: Float, y1: Float, period: Float = 6.5f) = Motion { d, t, m ->
    val u = t / period
    val cycle = floor(u).toInt()
    val p = u - cycle
    if (p > 0.16f) return@Motion
    val q = p / 0.16f
    val sx = 260f + hash(seed, cycle) * 700f
    val sy = y0 + hash(seed, cycle + 50) * (y1 - y0)
    val hx = sx - q * 300f
    val hy = sy + q * 120f
    val a = sin(q * PI.toFloat())
    val tail = m.at(hx + 150f, hy - 60f)
    d.drawLine(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = a)), tail, m.at(hx, hy)), tail, m.at(hx, hy),
        m.d(3f), StrokeCap.Round)
    d.drawCircle(Color.White, m.d(3.5f), m.at(hx, hy), a)
}

/** Northern lights rippling over the drawn ones, at each aurora anchor's height. */
private fun auroraWave(points: FloatArray) = Motion { d, t, m ->
    val colors = longArrayOf(0xFF3DF2B0, 0xFF2FD1FF, 0xFFA77BFF)
    val path = Path()
    var i = 0
    while (i + 2 < points.size) {
        val y = points[i + 1]
        colors.forEachIndexed { c, col ->
            path.reset()
            for (k in 0..24) {
                val x = ART_W * k / 24f
                val yy = y + c * 26f + 46f * sin(k * 0.45f + c * 1.3f + t * 0.4f) + 10f * sin(k * 1.3f - t * 0.9f)
                if (k == 0) path.moveTo(m.x(x), m.y(yy)) else path.lineTo(m.x(x), m.y(yy))
            }
            for (k in 24 downTo 0) {
                val x = ART_W * k / 24f
                val yy = y + c * 26f + 46f * sin(k * 0.45f + c * 1.3f + t * 0.4f) + 10f * sin(k * 1.3f - t * 0.9f) + 120f
                path.lineTo(m.x(x), m.y(yy))
            }
            path.close()
            val a = 0.22f + 0.1f * sin(t * 0.7f + c * 2f)
            d.drawPath(path, Brush.verticalGradient(listOf(Color.Transparent, Color(col).copy(alpha = a), Color.Transparent),
                m.y(y - 50f), m.y(y + 220f)))
        }
        i += 3
    }
}

/** Birds, bats or gulls crossing the page now and then, flapping. */
enum class Flyer { BIRD, BAT, EAGLE }

private fun flyers(kind: Flyer, n: Int, y0: Float, y1: Float, speed: Float, size: Float, color: Long, seed: Int, leftward: Boolean = false) =
    Motion { d, t, m ->
        val span = ART_W + 500f
        val wing = Path()
        for (i in 0 until n) {
            val sp = speed * (0.7f + 0.6f * hash(seed, i))
            val p = frac(hash(seed, i + 40) + t * sp / span)
            val x = if (leftward) ART_W + 250f - p * span else -250f + p * span
            val y = y0 + hash(seed, i + 80) * (y1 - y0) + sin(t * 0.8f + i * 2f) * 18f
            val flap = sin(t * (if (kind == Flyer.BAT) 15f else 7f) + i * 1.3f)
            val c = Color(color)
            val s = m.d(size * (0.75f + 0.5f * hash(seed, i + 120)))
            when (kind) {
                Flyer.BIRD -> {
                    val f = 0.15f + 0.9f * (0.5f + 0.5f * flap)
                    wing.reset()
                    wing.moveTo(-0.5f, 0f)
                    wing.quadraticTo(-0.25f, -0.3f * f, 0f, 0f)
                    wing.quadraticTo(0.25f, -0.3f * f, 0.5f, 0f)
                    d.withTransform({ translate(m.x(x), m.y(y)); scale(s, s, Offset.Zero) }) {
                        drawPath(wing, c, style = Stroke(width = 0.1f, cap = StrokeCap.Round))
                    }
                }
                Flyer.BAT -> d.withTransform({
                    translate(m.x(x), m.y(y))
                    scale((if (leftward) -1f else 1f) * s / 44f, s / 44f * (0.5f + 0.5f * abs(flap)), Offset.Zero)
                }) { drawPath(BAT, c) }
                Flyer.EAGLE -> d.withTransform({
                    translate(m.x(x), m.y(y))
                    rotate(sin(t * 0.5f + i) * 6f, Offset.Zero)
                    scale(s / 140f, s / 140f * (0.85f + 0.15f * flap), Offset.Zero)
                }) { drawPath(EAGLE, c) }
            }
        }
    }

/** A V of geese flying across, now and then. */
private fun geese(y: Float, color: Long, seed: Int, period: Float = 34f) = Motion { d, t, m ->
    val p = frac(t / period + hash(seed, 1))
    if (p > 0.75f) return@Motion
    val lead = ART_W + 120f - p / 0.75f * (ART_W + 560f)
    val wing = Path()
    for (k in 0 until 7) {
        val rank = (k + 1) / 2
        val side = if (k % 2 == 1) 1 else -1
        val gx = lead + rank * 42f
        val gy = y + side * rank * 18f + sin(t * 0.7f) * 8f
        val f = 0.15f + 0.9f * (0.5f + 0.5f * sin(t * 6f + k * 0.8f))
        wing.reset()
        wing.moveTo(-0.5f, 0f)
        wing.quadraticTo(-0.25f, -0.3f * f, 0f, 0f)
        wing.quadraticTo(0.25f, -0.3f * f, 0.5f, 0f)
        val s = m.d(30f)
        d.withTransform({ translate(m.x(gx), m.y(gy)); scale(s, s, Offset.Zero) }) {
            drawPath(wing, Color(color), alpha = 0.85f, style = Stroke(width = 0.11f, cap = StrokeCap.Round))
        }
    }
}

/** Butterflies wandering around an area, wings beating. */
private fun butterflies(n: Int, x0: Float, x1: Float, y0: Float, y1: Float, colors: LongArray, seed: Int, size: Float = 34f) =
    Motion { d, t, m ->
        for (i in 0 until n) {
            val a = hash(seed, i) * 6.28f
            val b = hash(seed, i + 10) * 6.28f
            val sx = 0.13f + 0.08f * hash(seed, i + 20)
            val sy = 0.21f + 0.1f * hash(seed, i + 30)
            val x = (x0 + x1) / 2f + (x1 - x0) / 2f * sin(t * sx + a)
            val y = (y0 + y1) / 2f + (y1 - y0) / 2f * sin(t * sy + b) + sin(t * 3f + i) * 6f
            val flap = 0.2f + 0.8f * abs(sin(t * 11f + i * 2f))
            val c1 = Color(colors[(i * 2) % colors.size])
            val c2 = Color(colors[(i * 2 + 1) % colors.size])
            val s = m.d(size)
            d.withTransform({ translate(m.x(x), m.y(y)); rotate(cos(t * sx + a) * 20f, Offset.Zero) }) {
                scale(flap, 1f, Offset.Zero) {
                    drawOval(c1, Offset(-0.66f * s, -0.48f * s), Size(0.62f * s, 0.5f * s))
                    drawOval(c1, Offset(0.04f * s, -0.48f * s), Size(0.62f * s, 0.5f * s))
                    drawOval(c2, Offset(-0.44f * s, -0.04f * s), Size(0.4f * s, 0.32f * s))
                    drawOval(c2, Offset(0.04f * s, -0.04f * s), Size(0.4f * s, 0.32f * s))
                }
                drawRoundRect(Color(0xFF3A2A2A), Offset(-0.04f * s, -0.3f * s), Size(0.08f * s, 0.6f * s), CornerRadius(0.04f * s))
            }
        }
    }

/** An eagle circling slowly. */
private fun soaring(cx: Float, cy: Float, color: Long) = Motion { d, t, m ->
    val x = cx + 150f * cos(t * 0.22f)
    val y = cy + 50f * sin(t * 0.22f)
    val s = m.d(100f)
    d.withTransform({ translate(m.x(x), m.y(y)); rotate(-sin(t * 0.22f) * 12f, Offset.Zero); scale(s / 140f, s / 140f, Offset.Zero) }) {
        drawPath(EAGLE, Color(color))
    }
}

// ---------- water and ground ----------

/** Sunlight glinting on water, from [y0] down to [y1]. */
private fun shimmer(y0: Float, y1: Float, n: Int, color: Long, seed: Int) = Motion { d, t, m ->
    for (i in 0 until n) {
        val x = hash(seed, i) * ART_W
        val y = y0 + 10f + hash(seed, i + 100) * max(1f, y1 - y0 - 20f)
        val a = max(0f, sin(t * (1.2f + hash(seed, i + 200)) + i * 2.4f)).pow(3)
        if (a > 0.02f) {
            val w = m.d(20f + 50f * hash(seed, i + 300))
            val h = m.d(3f)
            d.drawRoundRect(Color(color), Offset(m.x(x) - w / 2f, m.y(y) - h / 2f), Size(w, h), CornerRadius(h / 2f), alpha = 0.7f * a)
        }
    }
}

/** A lighthouse's beam turning: sweeping across, then flashing as it faces us. */
private fun beam(points: FloatArray) = Motion { d, t, m ->
    if (points.size < 3) return@Motion
    val x = points[0]
    val y = points[1]
    val h = points[2]
    val c = cos(t * 1.1f)
    val a = abs(c)
    val dir = if (c < 0f) -1f else 1f
    val len = h * 2f
    val spread = 0.08f + 0.06f * (1f - a)
    val path = Path().apply {
        moveTo(m.x(x), m.y(y))
        lineTo(m.x(x + dir * len), m.y(y - len * spread))
        lineTo(m.x(x + dir * len), m.y(y + len * spread))
        close()
    }
    d.drawPath(path, Brush.horizontalGradient(listOf(Color(0xFFFFF6C9).copy(alpha = 0.5f * a), Color.Transparent), m.x(x), m.x(x + dir * len)))
    val flash = (1f - a).pow(4)
    d.glow(m.at(x, y), m.d(h * 0.14f + h * 0.3f * flash), Color(0xFFFFF3B0), 0.5f + 0.45f * flash)
}

/** A tumbleweed rolling and bouncing across the ground now and then. */
private fun tumbleweed(ground: Float) = Motion { d, t, m ->
    val p = frac(t / 15f)
    if (p > 0.7f) return@Motion
    val q = p / 0.7f
    val x = -120f + q * (ART_W + 240f)
    val y = ground - 34f - abs(sin(q * 16f)) * 34f
    val s = m.d(34f)
    d.withTransform({ translate(m.x(x), m.y(y)); rotate(q * 1500f, Offset.Zero); scale(s, s, Offset.Zero) }) {
        drawPath(TUMBLEWEED, Color(0xFFB58A52), style = Stroke(width = 0.06f))
    }
}

/** Dust drifting slowly up through the light. */
private fun motes(n: Int, color: Long, seed: Int) = Motion { d, t, m ->
    val span = ART_H - m.top
    for (i in 0 until n) {
        val y = ART_H - frac(hash(seed, i) + t * (6f + 8f * hash(seed, i + 50)) / span) * span
        val x = hash(seed, i + 100) * ART_W + 30f * sin(t * 0.25f + i)
        val a = 0.15f + 0.25f * (0.5f + 0.5f * sin(t * 0.8f + i * 1.3f))
        d.drawCircle(Color(color), m.d(1.6f + 2f * hash(seed, i + 200)), m.at(x, y), a)
    }
}

// ---------- each theme's moving parts ----------

private val LEAVES = longArrayOf(0xFFC8462A, 0xFFE07B2A, 0xFFB5341F, 0xFFF0A43A, 0xFFD9622A, 0xFFA8321C)
private val MARIGOLD = longArrayOf(0xFFFFB21E, 0xFFFF8C1A, 0xFFFFD34D)
private val CONFETTI = longArrayOf(0xFFFFD36B, 0xFFFF7AC0, 0xFF63D6FF, 0xFFFFFFFF, 0xFFB88CFF, 0xFF4CD17A)
private val FIESTA = longArrayOf(0xFFE63946, 0xFFF4A261, 0xFF2A9D8F, 0xFFE9C46A, 0xFFFF70A6, 0xFF3A86FF, 0xFFFFFFFF)
private val WARM = 0xFFFFCF6A
private const val SKY_TOP = 270f
private const val BAND = 1380f

object Motions {
    fun of(art: String): List<Motion> {
        fun a(kind: String) = ThemeAnchors.of(art, kind)
        fun water(): Float = a("water").let { if (it.size >= 3) it[1] else ART_H }
        return when (art) {
            "thanksgiving" -> listOf(
                pulse(a("sun"), 0xFFFFD27A, 3.2f, 0.32f),
                smoke(a("smoke")),
                flicker(a("window"), WARM, 2.4f, 0.4f, 0.4f),
                geese(1610f, 0xFF3A1620, 3),
                fall(Piece.MAPLE, 22, 70f, 40f, 18f..34f, LEAVES, 1),
            )
            "halloween" -> listOf(
                twinkle(26, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 9f, 2),
                flicker(a("window"), 0xFFFFB84A, 2.4f, 0.45f),
                flicker(a("lantern"), 0xFFFF9A2A, 2.4f, 0.5f),
                flyers(Flyer.BAT, 4, BAND + 40f, BAND + 420f, 120f, 46f, 0xFF0A0612, 3),
                flyers(Flyer.BAT, 2, 330f, 560f, 90f, 34f, 0xFF2E1A4A, 4, leftward = true),
            )
            "dia_de_muertos" -> listOf(
                twinkle(16, SKY_TOP, BAND + 260f, 0xFFFFE9C4, 8f, 5),
                bulbs(a("bulb")),
                flicker(a("flame"), 0xFFFFB347, 4f, 0.55f, 1.2f),
                fall(Piece.PETAL, 28, 55f, 30f, 12f..18f, MARIGOLD, 6),
            )
            "christmas" -> listOf(
                twinkle(20, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 8f, 7),
                smoke(a("smoke")),
                flicker(a("window"), 0xFFFFD27A, 2.2f, 0.4f, 0.5f),
                bulbs(a("bulb")),
                bulbs(a("light"), 3.4f, 20f),
                pulse(a("star"), 0xFFFFF3B0, 1.4f, 0.6f, 2f),
                fall(Piece.SNOW, 70, 60f, 26f, 4f..9f, longArrayOf(0xFFFFFFFF), 8, 0.85f),
                fall(Piece.FLAKE, 10, 45f, 40f, 16f..26f, longArrayOf(0xFFFFFFFF), 9, 0.9f, 40f),
            )
            "new_year" -> listOf(
                twinkle(20, SKY_TOP, BAND + 380f, 0xFFFFFFFF, 8f, 10),
                fireworks(3, CONFETTI, 11, arrayOf(floatArrayOf(60f, 300f, 300f, 520f), floatArrayOf(720f, 300f, 980f, 520f),
                    floatArrayOf(100f, BAND + 80f, 930f, BAND + 380f))),
                shimmer(water(), ART_H, 18, 0xFFFFD27A, 12),
                fall(Piece.CONFETTI, 34, 80f, 30f, 10f..18f, CONFETTI, 13, spin = 200f),
            )
            "valentines" -> listOf(
                twinkle(16, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 10f, 14),
                rise(Piece.HEART, 16, 70f, 30f, 22f..44f, longArrayOf(0xFFFF6B8B, 0xFFFF8FAB, 0xFFE5486E, 0xFFFFFFFF), 15),
                fall(Piece.PETAL, 12, 45f, 30f, 12f..18f, longArrayOf(0xFFE5486E, 0xFFFF6B8B, 0xFFD6234A), 16),
            )
            "st_patricks" -> listOf(
                twinkle(26, SKY_TOP, ART_H - 100f, 0xFFFFE28A, 10f, 17),
                pulse(a("glow"), 0xFFFFE066, 1.6f, 0.45f, 1.6f),
                smoke(a("smoke")),
                fall(Piece.SHAMROCK, 8, 40f, 40f, 22f..34f, longArrayOf(0xFF2F9A4F, 0xFF3CAE5C, 0xFF24804A), 18),
            )
            "easter" -> listOf(
                pulse(a("sun"), 0xFFFFF3C4, 3f, 0.3f),
                butterflies(4, 120f, 910f, BAND + 300f, BAND + 700f, longArrayOf(0xFFFF9FCE, 0xFFFFD1E8, 0xFF9CC7FF, 0xFFD6E7FF,
                    0xFFFFD166, 0xFFFFF1B8, 0xFFB79CFF, 0xFFE2D6FF), 19),
                fall(Piece.PETAL, 14, 40f, 40f, 10f..16f, longArrayOf(0xFFFFFFFF, 0xFFFFD1E8, 0xFFFFF3A6), 20),
            )
            "cinco_de_mayo" -> listOf(
                pulse(a("sun"), 0xFFFFD27A, 2.6f, 0.3f),
                bulbs(a("bulb")),
                flicker(a("window"), WARM, 2f, 0.4f, 0.5f),
                fall(Piece.CONFETTI, 30, 70f, 30f, 10f..18f, FIESTA, 21, spin = 200f),
            )
            "july_4" -> listOf(
                twinkle(26, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 8f, 22),
                fireworks(3, longArrayOf(0xFFFF4D5E, 0xFFFFFFFF, 0xFF4D8DFF, 0xFFFFD36B), 23, arrayOf(
                    floatArrayOf(60f, 300f, 300f, 520f), floatArrayOf(720f, 300f, 980f, 520f), floatArrayOf(100f, BAND + 80f, 930f, BAND + 340f))),
                shimmer(water(), BAND + 720f, 16, 0xFFFFFFFF, 24),
                crackle(a("sparkler"), 25),
            )
            "spring" -> listOf(
                pulse(a("sun"), 0xFFFFF3C4, 3f, 0.3f),
                butterflies(3, 120f, 910f, BAND + 250f, BAND + 650f, longArrayOf(0xFFFFB0CC, 0xFFFFD6E6, 0xFFFFD166, 0xFFFFF1B8,
                    0xFF9CC7FF, 0xFFD6E7FF), 26),
                fall(Piece.PETAL, 30, 45f, 50f, 12f..18f, longArrayOf(0xFFFFC2D9, 0xFFFFB0CC, 0xFFFFD6E6), 27),
            )
            "summer" -> listOf(
                pulse(a("sun"), 0xFFFFE0A0, 3f, 0.35f),
                shimmer(water(), BAND + 620f, 26, 0xFFFFFFFF, 28),
                flyers(Flyer.BIRD, 3, BAND + 120f, BAND + 360f, 70f, 34f, 0xFFFFFFFF, 29),
            )
            "fall" -> listOf(
                pulse(a("sun"), 0xFFFFE0A8, 3f, 0.3f),
                flicker(a("lamp"), 0xFFFFD98A, 2.6f, 0.35f, 0.4f),
                fall(Piece.MAPLE, 26, 70f, 40f, 18f..32f, LEAVES, 30),
            )
            "winter" -> listOf(
                smoke(a("smoke")),
                flicker(a("window"), 0xFFFFD27A, 2.2f, 0.45f, 0.5f),
                fall(Piece.SNOW, 80, 55f, 26f, 4f..10f, longArrayOf(0xFFFFFFFF), 31, 0.9f),
                fall(Piece.FLAKE, 12, 40f, 40f, 16f..28f, longArrayOf(0xFFFFFFFF), 32, 0.9f, 40f),
            )
            "aurora" -> listOf(
                twinkle(34, SKY_TOP, BAND + 420f, 0xFFFFFFFF, 8f, 33),
                auroraWave(a("aurora")),
                shootingStars(34, 300f, 900f),
                shimmer(water(), BAND + 640f, 14, 0xFF7FE9D1, 35),
                flicker(a("fire"), 0xFFFF9A3A, 2.6f, 0.55f, 1.3f),
                sparks(a("fire"), 36),
            )
            "ocean" -> listOf(
                pulse(a("sun"), 0xFFFFFFFF, 3f, 0.3f),
                beam(a("beam")),
                shimmer(water(), ART_H, 26, 0xFFFFFFFF, 37),
                flyers(Flyer.BIRD, 3, BAND + 100f, BAND + 340f, 70f, 34f, 0xFFFFFFFF, 38, leftward = true),
            )
            "desert" -> listOf(
                pulse(a("sun"), 0xFFFFD9A0, 3f, 0.3f),
                flyers(Flyer.BIRD, 2, BAND + 120f, BAND + 300f, 50f, 30f, 0xFF7A4A2A, 39),
                tumbleweed(BAND + 840f),
            )
            "city" -> listOf(
                twinkle(30, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 8f, 40),
                windowsOff(a("citywin")),
                blink(a("antenna")),
                chase(a("ferris"), longArrayOf(0xFFFF7AC0, 0xFFFFD27A, 0xFF63D6FF, 0xFF7EF0A0)),
                shimmer(water(), BAND + 780f, 20, 0xFFFFCF73, 41),
                flicker(a("lamp"), 0xFFFFD98A, 2.4f, 0.35f, 0.4f),
            )
            "farm" -> listOf(
                pulse(a("sun"), 0xFFFFE6B0, 3f, 0.3f),
                flyers(Flyer.BIRD, 3, BAND + 80f, BAND + 300f, 60f, 30f, 0xFF5A3A22, 42),
                butterflies(2, 200f, 840f, BAND + 600f, BAND + 820f, longArrayOf(0xFFFFFFFF, 0xFFFFE9A8, 0xFFFFB0CC, 0xFFFFD6E6), 43, 26f),
            )
            "mountains" -> listOf(
                soaring(560f, BAND + 150f, 0xFF2B2B3A),
                shimmer(water(), BAND + 680f, 18, 0xFFFFFFFF, 44),
                flyers(Flyer.BIRD, 2, BAND + 60f, BAND + 260f, 50f, 28f, 0xFF2B3A4A, 45, leftward = true),
            )
            "warehouse" -> listOf(
                flicker(a("lamp"), 0xFFFFF3D6, 3f, 0.4f, 0.25f),
                motes(36, 0xFFFFF3D6, 46),
            )
            "shopping" -> listOf(twinkle(24, SKY_TOP, ART_H - 60f, 0xFFFFFFFF, 11f, 47))
            "horizon" -> listOf(
                twinkle(14, SKY_TOP, BAND + 300f, 0xFFD6F5DF, 8f, 48),
                flyers(Flyer.BIRD, 3, BAND + 100f, BAND + 300f, 50f, 28f, 0xFFD6F5DF, 49),
            )
            else -> emptyList()
        }
    }
}
