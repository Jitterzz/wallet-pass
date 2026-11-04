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

internal const val ART_W = 1032f
internal const val ART_H = 2240f

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
internal fun hash(a: Int, b: Int): Float {
    var h = a * 374761393 + b * 668265263
    h = (h xor (h ushr 13)) * 1274126177
    h = h xor (h ushr 16)
    return (h and 0x7fffffff) / 2147483648f
}

internal fun frac(v: Float) = v - floor(v)

internal fun rgb(v: Float) = Color(0xFF000000L or v.toLong())

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

internal fun parse(d: String): Path = PathParser().parsePathString(d).toPath()

private val MAPLE: Path by lazy {
    parse("M0,-1 L0.12,-0.62 L0.3,-0.72 L0.27,-0.38 L0.62,-0.55 L0.56,-0.28 L0.95,-0.22 L0.72,-0.02 L0.82,0.12 L0.38,0.12 " +
        "L0.42,0.32 L0.06,0.2 L0.04,0.66 L-0.04,0.66 L-0.06,0.2 L-0.42,0.32 L-0.38,0.12 L-0.82,0.12 L-0.72,-0.02 L-0.95,-0.22 " +
        "L-0.56,-0.28 L-0.62,-0.55 L-0.27,-0.38 L-0.3,-0.72 L-0.12,-0.62Z")
}
private val HEART: Path by lazy {
    parse("M0,0.32 C-0.52,-0.05 -0.52,-0.32 -0.52,-0.32 C-0.52,-0.6 -0.24,-0.68 0,-0.44 C0.24,-0.68 0.52,-0.6 0.52,-0.32 " +
        "C0.52,-0.05 0,0.32 0,0.32Z")
}
internal val SPARKLE: Path by lazy {
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

internal fun DrawScope.glow(c: Offset, r: Float, color: Color, alpha: Float) {
    if (r <= 0f || alpha <= 0f) return
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha.coerceIn(0f, 1f)), Color.Transparent), c, r), r, c)
}

internal fun DrawScope.shape(path: Path, x: Float, y: Float, size: Float, rot: Float, color: Color, alpha: Float, sx: Float = 1f) {
    withTransform({
        translate(x, y)
        rotate(rot, Offset.Zero)
        scale(size * sx, size, Offset.Zero)
    }) { drawPath(path, color, alpha) }
}

// ---------- falling and rising things ----------

/** What falls: snow, leaves, petals, confetti... */
enum class Piece { SNOW, FLAKE, MAPLE, PETAL, CONFETTI, HEART, SHAMROCK, COIN }

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
        Piece.COIN -> withTransform({ translate(x, y); scale(abs(cos(phase * 2.5f)) + 0.08f, 1f, Offset.Zero) }) {
            drawCircle(Color(0xFFE0A800), s / 2f, Offset.Zero, a)
            drawCircle(Color(0xFFFFCF33), s * 0.38f, Offset.Zero, a)
            drawCircle(Color(0xFFFFF3B0), s * 0.12f, Offset(-s * 0.14f, -s * 0.14f), a * 0.9f)
        }
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
        d.glow(m.at(points[i], points[i + 1]), m.d(points[i + 2] * reach * (0.82f + 0.24f * f)), Color(color), strength * (0.45f + 0.55f * f))
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

/**
 * Fireworks: [n] shells at a time going up and bursting, each somewhere new in the given areas: a flash, a ring of
 * trails in one color with an inner ring in another, then glitter as it fades and falls.
 */
private fun fireworks(n: Int, colors: LongArray, seed: Int, areas: Array<FloatArray>, period: Float = 2.4f) = Motion { d, t, m ->
    for (i in 0 until n) {
        val u = (t + i * period / n + hash(seed, i) * 0.5f) / period
        val cycle = floor(u).toInt()
        val p = u - cycle
        val area = areas[(hash(seed, cycle * 7 + i * 3) * areas.size).toInt().coerceIn(0, areas.size - 1)]
        val cx = area[0] + hash(seed, cycle * 11 + i) * (area[2] - area[0])
        val cy = area[1] + hash(seed, cycle * 13 + i) * (area[3] - area[1])
        val color = Color(colors[(hash(seed, cycle * 17 + i) * colors.size).toInt().coerceIn(0, colors.size - 1)])
        val inner = Color(colors[(hash(seed, cycle * 23 + i) * colors.size).toInt().coerceIn(0, colors.size - 1)])
        // A fifth number on an area scales the bursts there (smaller beside the logo, so the text stays clear).
        val radius = (120f + 110f * hash(seed, cycle * 19 + i)) * (if (area.size > 4) area[4] else 1f)
        if (p < 0.16f) {
            val q = p / 0.16f
            val sy = cy + 320f * (1f - q)
            d.drawLine(color, m.at(cx, sy + 70f), m.at(cx, sy), m.d(4f), StrokeCap.Round, alpha = 0.7f)
            d.glow(m.at(cx, sy), m.d(16f), Color.White, 0.9f)
        } else {
            val q = (p - 0.16f) / 0.84f
            val r = radius * (1f - (1f - q).pow(3))
            val a = (1f - q).pow(1.2f)
            val sag = 60f * q * q
            if (q < 0.25f) d.glow(m.at(cx, cy), m.d(radius * 0.9f), Color.White, 0.55f * (1f - q / 0.25f))
            d.glow(m.at(cx, cy), m.d(r * 1.6f + 1f), color, 0.38f * a)
            val rays = 28
            val turn = hash(seed, cycle + 5) * 0.3f
            for (k in 0 until rays) {
                val ang = 2f * PI.toFloat() * k / rays + turn
                val ex = cx + r * cos(ang)
                val ey = cy + r * sin(ang) + sag
                d.drawLine(color, m.at(cx + r * 0.62f * cos(ang), cy + r * 0.62f * sin(ang) + sag * 0.6f), m.at(ex, ey), m.d(4.2f),
                    StrokeCap.Round, alpha = a)
                d.drawCircle(Color.White, m.d(3.6f), m.at(ex, ey), a)
                val ir = r * 0.55f
                d.drawCircle(inner, m.d(3.4f), m.at(cx + ir * cos(ang + 0.11f), cy + ir * sin(ang + 0.11f) + sag * 0.5f), a)
                if (q > 0.45f && hash(seed, cycle * 29 + k + floor(t * 12f).toInt()) > 0.55f) {
                    d.drawCircle(Color(0xFFFFF6D6), m.d(2.6f), m.at(ex + 6f * cos(ang * 3f), ey + 14f), a)
                }
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

/** An eagle (or condor) circling slowly around (cx, cy). */
private fun soaring(cx: Float, cy: Float, color: Long, phase: Float = 0f, size: Float = 100f) = Motion { d, t, m ->
    val a = t * 0.22f + phase
    val x = cx + 150f * cos(a)
    val y = cy + 50f * sin(a)
    val s = m.d(size)
    d.withTransform({ translate(m.x(x), m.y(y)); rotate(-sin(a) * 12f, Offset.Zero); scale(s / 140f, s / 140f, Offset.Zero) }) {
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

// ---------- more moving parts ----------

/** Long soft rays turning slowly around a sun. */
private fun sunRays(points: FloatArray, color: Long, reach: Float = 5.5f, n: Int = 12) = Motion { d, t, m ->
    val path = Path()
    var i = 0
    while (i + 2 < points.size) {
        val cx = points[i]
        val cy = points[i + 1]
        val len = points[i + 2] * reach
        val c = m.at(cx, cy)
        for (k in 0 until n) {
            val a = t * 0.12f + 2f * PI.toFloat() * k / n
            val w = 0.07f + 0.03f * sin(t * 0.9f + k)
            path.reset()
            path.moveTo(c.x, c.y)
            path.lineTo(m.x(cx + len * cos(a - w)), m.y(cy + len * sin(a - w)))
            path.lineTo(m.x(cx + len * cos(a + w)), m.y(cy + len * sin(a + w)))
            path.close()
            val alpha = 0.2f + 0.12f * sin(t * 1.3f + k * 1.7f)
            d.drawPath(path, Brush.radialGradient(listOf(Color(color).copy(alpha = alpha), Color.Transparent), c, m.d(len)))
        }
        i += 3
    }
}

/** Puffy clouds drifting across. */
private fun clouds(n: Int, y0: Float, y1: Float, speed: Float, color: Long, seed: Int, size: ClosedFloatingPointRange<Float> = 90f..150f) =
    Motion { d, t, m ->
        val span = ART_W + 600f
        val c = Color(color)
        for (i in 0 until n) {
            val s = size.start + (size.endInclusive - size.start) * hash(seed, i)
            val x = -300f + frac(hash(seed, i + 10) + t * speed * (0.7f + 0.6f * hash(seed, i + 20)) / span) * span
            val y = y0 + hash(seed, i + 30) * (y1 - y0)
            d.drawCircle(c, m.d(0.35f * s), m.at(x - 0.5f * s, y))
            d.drawCircle(c, m.d(0.5f * s), m.at(x, y - 0.2f * s))
            d.drawCircle(c, m.d(0.33f * s), m.at(x + 0.55f * s, y + 0.02f * s))
            d.drawRoundRect(c, m.at(x - 0.85f * s, y), Size(m.d(1.7f * s), m.d(0.35f * s)), CornerRadius(m.d(0.17f * s)))
        }
    }

/** Hot-air balloons drifting slowly across and bobbing. */
private fun balloons(n: Int, y0: Float, y1: Float, seed: Int) = Motion { d, t, m ->
    val span = ART_W + 400f
    val sets = arrayOf(longArrayOf(0xFFFF5E5B, 0xFFFFD166), longArrayOf(0xFF3FA9F5, 0xFFFFFFFF), longArrayOf(0xFFB07CF2, 0xFFFF8FAB),
        longArrayOf(0xFF7BD389, 0xFFFFD166))
    for (i in 0 until n) {
        val k = 0.7f + 0.5f * hash(seed, i)
        val x = -200f + frac(hash(seed, i + 5) + t * 16f * (0.7f + 0.6f * hash(seed, i + 9)) / span) * span
        val y = y0 + hash(seed, i + 13) * (y1 - y0) + sin(t * 0.6f + i * 2f) * 22f
        val (c1, c2) = sets[i % sets.size].let { Color(it[0]) to Color(it[1]) }
        val w = m.d(110f * k)
        val h = m.d(130f * k)
        val c = m.at(x, y)
        d.drawLine(Color(0xFF6B4A2A), Offset(c.x - w * 0.15f, c.y + h * 0.45f), Offset(c.x - w * 0.1f, c.y + h * 0.7f), m.d(2f))
        d.drawLine(Color(0xFF6B4A2A), Offset(c.x + w * 0.15f, c.y + h * 0.45f), Offset(c.x + w * 0.1f, c.y + h * 0.7f), m.d(2f))
        d.drawRoundRect(Color(0xFFA8743A), Offset(c.x - w * 0.12f, c.y + h * 0.68f), Size(w * 0.24f, h * 0.16f), CornerRadius(m.d(3f)))
        d.drawOval(c1, Offset(c.x - w / 2f, c.y - h / 2f), Size(w, h))
        d.drawOval(c2, Offset(c.x - w * 0.2f, c.y - h / 2f), Size(w * 0.4f, h))
        d.drawOval(Color.White.copy(alpha = 0.3f), Offset(c.x - w * 0.36f, c.y - h * 0.32f), Size(w * 0.14f, h * 0.36f))
    }
}

/** Cars crossing a bridge at night: headlights going one way, tail lights the other. */
private fun traffic(x0: Float, x1: Float, y: Float, seed: Int) = Motion { d, t, m ->
    val span = x1 - x0
    for (lane in 0..1) {
        for (i in 0 until 7) {
            val p = frac(hash(seed, i + lane * 20) + t * (110f + 40f * hash(seed, i + 50)) / span)
            val x = if (lane == 0) x0 + p * span else x1 - p * span
            val yy = y + lane * 8f
            val c = if (lane == 0) Color(0xFFFFF3C4) else Color(0xFFFF5A5A)
            d.glow(m.at(x, yy), m.d(18f), c, 0.6f)
            d.drawRoundRect(c, Offset(m.x(x - 10f), m.y(yy - 2f)), Size(m.d(20f), m.d(4f)), CornerRadius(m.d(2f)))
        }
    }
}

private val GHOST: Path by lazy {
    parse("M-0.45,0.55 L-0.45,-0.05 C-0.45,-0.33 -0.25,-0.55 0,-0.55 C0.25,-0.55 0.45,-0.33 0.45,-0.05 L0.45,0.55 " +
        "Q0.34,0.42 0.22,0.56 Q0.11,0.42 0,0.56 Q-0.11,0.42 -0.22,0.56 Q-0.34,0.42 -0.45,0.55Z")
}

/** Little ghosts floating across, bobbing as they go. */
private fun ghosts(n: Int, y0: Float, y1: Float, seed: Int) = Motion { d, t, m ->
    val span = ART_W + 400f
    for (i in 0 until n) {
        val s = m.d(70f + 50f * hash(seed, i))
        val p = frac(hash(seed, i + 3) + t * 34f * (0.7f + 0.6f * hash(seed, i + 6)) / span)
        val x = if (i % 2 == 0) -200f + p * span else ART_W + 200f - p * span
        val y = y0 + hash(seed, i + 9) * (y1 - y0) + sin(t * 1.6f + i * 2f) * 26f
        val c = m.at(x, y)
        d.glow(c, s * 1.1f, Color(0xFFE8F0FF), 0.25f)
        d.withTransform({ translate(c.x, c.y); rotate(sin(t * 1.6f + i) * 8f, Offset.Zero); scale(s, s, Offset.Zero) }) {
            drawPath(GHOST, Color(0xFFF4F6FF), alpha = 0.92f)
            drawOval(Color(0xFF2A2140), Offset(-0.22f, -0.15f), Size(0.14f, 0.2f))
            drawOval(Color(0xFF2A2140), Offset(0.08f, -0.15f), Size(0.14f, 0.2f))
            drawOval(Color(0xFF2A2140), Offset(-0.07f, 0.07f), Size(0.14f, 0.18f))
        }
    }
}

/** Rows of little waves rolling along the water. */
private fun waves(rows: FloatArray, speed: Float, color: Long, alpha: Float) = Motion { d, t, m ->
    val path = Path()
    rows.forEachIndexed { r, y ->
        for (k in 0 until 14) {
            val dir = if (r % 2 == 0) 1f else -0.7f
            val x = frac(k / 14f + t * speed * dir / ART_W + r * 0.37f) * (ART_W + 80f) - 40f
            val yy = y + 6f * sin(t * 1.4f + k + r)
            path.reset()
            path.moveTo(m.x(x), m.y(yy))
            path.quadraticTo(m.x(x + 18f), m.y(yy - 13f), m.x(x + 36f), m.y(yy))
            d.drawPath(path, Color(color), alpha = alpha, style = Stroke(width = m.d(4.5f), cap = StrokeCap.Round))
        }
    }
}

/** Water pouring down a waterfall, with spray at the bottom. */
private fun waterfall(x: Float, y0: Float, y1: Float, w: Float) = Motion { d, t, m ->
    for (k in 0 until 12) {
        val lx = x - w / 2f + w * hash(91, k)
        val p = frac(t * 1.4f + k / 12f)
        val yy = y0 + p * (y1 - y0)
        d.drawLine(Color.White, m.at(lx, yy), m.at(lx, min(y1, yy + 34f)), m.d(3f), StrokeCap.Round, alpha = 0.85f)
    }
    for (k in 0 until 6) {
        val p = frac(t * 0.8f + k / 6f)
        d.drawCircle(Color.White, m.d(10f + 18f * p), m.at(x + (hash(92, k) - 0.5f) * w * 2.4f, y1 - p * 30f), 0.5f * (1f - p))
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
        val sideSkies = arrayOf(floatArrayOf(40f, 320f, 230f, 520f, 0.6f), floatArrayOf(802f, 320f, 992f, 520f, 0.6f))
        return when (art) {
            "thanksgiving" -> listOf(
                twinkle(26, SKY_TOP, 820f, 0xFFFFE9C9, 11f, 4),
                sunRays(a("sun"), 0xFFFFE8B0),
                pulse(a("sun"), 0xFFFFD27A, 3.6f, 0.45f),
                smoke(a("smoke")),
                flicker(a("window"), WARM, 3f, 0.6f, 0.5f),
                geese(1570f, 0xFF5A2A26, 3, 16f),
                geese(600f, 0xFF2A1430, 4, 23f),
                fall(Piece.MAPLE, 28, 90f, 70f, 24f..44f, LEAVES, 1),
            )
            "halloween" -> listOf(
                twinkle(40, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 14f, 2),
                pulse(floatArrayOf(720f, BAND + 235f, 125f), 0xFFFFF3C4, 1.9f, 0.5f, 1.2f),
                flicker(a("window"), 0xFFFFB84A, 3.4f, 0.7f),
                flicker(a("lantern"), 0xFFFF9A2A, 3f, 0.75f, 1.3f),
                ghosts(3, BAND + 160f, BAND + 520f, 50),
                flyers(Flyer.BAT, 8, BAND + 20f, BAND + 460f, 170f, 70f, 0xFF0A0612, 3),
                flyers(Flyer.BAT, 4, 300f, 580f, 140f, 56f, 0xFF3A2160, 4, leftward = true),
            )
            "dia_de_muertos" -> listOf(
                twinkle(26, SKY_TOP, BAND + 260f, 0xFFFFE9C4, 13f, 5),
                bulbs(a("bulb"), 3.4f, 36f),
                flicker(a("flame"), 0xFFFFB347, 5.5f, 0.85f, 1.4f),
                fall(Piece.PETAL, 60, 80f, 50f, 18f..30f, MARIGOLD, 6),
            )
            "christmas" -> listOf(
                twinkle(34, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 13f, 7),
                pulse(floatArrayOf(800f, BAND + 200f, 122f), 0xFFFFF6D6, 1.9f, 0.45f, 1.1f),
                shootingStars(51, 300f, 900f, 7f),
                smoke(a("smoke")),
                flicker(a("window"), 0xFFFFD27A, 3f, 0.6f, 0.6f),
                bulbs(a("bulb"), 3.4f, 36f),
                bulbs(a("light"), 4.4f, 32f),
                pulse(a("star"), 0xFFFFF3B0, 1.8f, 0.85f, 2.4f),
                fall(Piece.SNOW, 150, 85f, 40f, 6f..14f, longArrayOf(0xFFFFFFFF), 8, 0.9f),
                fall(Piece.FLAKE, 26, 60f, 60f, 26f..44f, longArrayOf(0xFFFFFFFF), 9, 0.95f, 60f),
            )
            "new_year" -> listOf(
                twinkle(30, SKY_TOP, BAND + 380f, 0xFFFFFFFF, 13f, 10),
                fireworks(5, CONFETTI, 11, sideSkies + floatArrayOf(100f, BAND + 80f, 930f, BAND + 380f)),
                shimmer(water(), ART_H, 30, 0xFFFFD27A, 12),
                fall(Piece.CONFETTI, 80, 115f, 50f, 16f..28f, CONFETTI, 13, spin = 260f),
            )
            "valentines" -> listOf(
                twinkle(26, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 15f, 14),
                rise(Piece.HEART, 32, 110f, 50f, 32f..70f, longArrayOf(0xFFFF6B8B, 0xFFFF8FAB, 0xFFE5486E, 0xFFFFFFFF, 0xFFFF4D6D), 15),
                fall(Piece.PETAL, 30, 70f, 50f, 18f..28f, longArrayOf(0xFFE5486E, 0xFFFF6B8B, 0xFFD6234A), 16),
            )
            "st_patricks" -> listOf(
                clouds(3, 300f, 560f, 22f, 0xFFFFFFFF, 52),
                twinkle(40, SKY_TOP, ART_H - 100f, 0xFFFFE28A, 15f, 17),
                pulse(a("glow"), 0xFFFFE066, 1.9f, 0.7f, 2f),
                smoke(a("smoke")),
                fall(Piece.COIN, 22, 110f, 30f, 22f..34f, longArrayOf(0xFFFFCF33), 53),
                fall(Piece.SHAMROCK, 18, 70f, 60f, 30f..48f, longArrayOf(0xFF2F9A4F, 0xFF3CAE5C, 0xFF24804A), 18),
            )
            "easter" -> listOf(
                sunRays(a("sun"), 0xFFFFF6D6),
                pulse(a("sun"), 0xFFFFF3C4, 3.4f, 0.45f),
                clouds(3, 300f, 560f, 22f, 0xFFFFFFFF, 54),
                butterflies(8, 60f, 970f, 300f, BAND + 720f, longArrayOf(0xFFFF9FCE, 0xFFFFD1E8, 0xFF9CC7FF, 0xFFD6E7FF,
                    0xFFFFD166, 0xFFFFF1B8, 0xFFB79CFF, 0xFFE2D6FF), 19, 50f),
                fall(Piece.PETAL, 34, 60f, 60f, 16f..26f, longArrayOf(0xFFFFFFFF, 0xFFFFD1E8, 0xFFFFF3A6), 20),
            )
            "cinco_de_mayo" -> listOf(
                sunRays(a("sun"), 0xFFFFE2A0),
                pulse(a("sun"), 0xFFFFD27A, 3f, 0.45f),
                bulbs(a("bulb"), 3.4f, 36f),
                flicker(a("window"), WARM, 2.6f, 0.6f, 0.6f),
                fall(Piece.CONFETTI, 70, 100f, 50f, 16f..28f, FIESTA, 21, spin = 260f),
            )
            "july_4" -> listOf(
                twinkle(40, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 13f, 22),
                fireworks(5, longArrayOf(0xFFFF4D5E, 0xFFFFFFFF, 0xFF4D8DFF, 0xFFFFD36B), 23,
                    sideSkies + floatArrayOf(100f, BAND + 80f, 930f, BAND + 340f)),
                shimmer(water(), BAND + 720f, 26, 0xFFFFFFFF, 24),
                crackle(a("sparkler"), 25),
            )
            "spring" -> listOf(
                sunRays(a("sun"), 0xFFFFF6D6),
                pulse(a("sun"), 0xFFFFF3C4, 3.4f, 0.45f),
                clouds(3, 300f, 560f, 22f, 0xFFFFFFFF, 55),
                butterflies(7, 60f, 970f, 300f, BAND + 680f, longArrayOf(0xFFFFB0CC, 0xFFFFD6E6, 0xFFFFD166, 0xFFFFF1B8,
                    0xFF9CC7FF, 0xFFD6E7FF), 26, 48f),
                fall(Piece.PETAL, 70, 70f, 80f, 18f..30f, longArrayOf(0xFFFFC2D9, 0xFFFFB0CC, 0xFFFFD6E6, 0xFFFF9CBF), 27),
            )
            "summer" -> listOf(
                sunRays(a("sun"), 0xFFFFE6A8),
                pulse(a("sun"), 0xFFFFE0A0, 3.6f, 0.5f),
                clouds(3, 300f, 560f, 24f, 0xFFFFFFFF, 56),
                waves(floatArrayOf(BAND + 450f, BAND + 500f, BAND + 550f, BAND + 590f), 50f, 0xFFFFFFFF, 0.6f),
                shimmer(water(), BAND + 620f, 40, 0xFFFFFFFF, 28),
                flyers(Flyer.BIRD, 6, BAND + 100f, BAND + 380f, 90f, 50f, 0xFFFFFFFF, 29),
            )
            "fall" -> listOf(
                sunRays(a("sun"), 0xFFFFE6B8),
                pulse(a("sun"), 0xFFFFE0A8, 3.4f, 0.45f),
                flicker(a("lamp"), 0xFFFFD98A, 3.2f, 0.55f, 0.5f),
                fall(Piece.MAPLE, 50, 105f, 70f, 26f..50f, LEAVES, 30),
            )
            "winter" -> listOf(
                smoke(a("smoke")),
                flicker(a("window"), 0xFFFFD27A, 3f, 0.65f, 0.6f),
                fall(Piece.SNOW, 170, 80f, 40f, 6f..15f, longArrayOf(0xFFFFFFFF), 31, 0.95f),
                fall(Piece.FLAKE, 30, 55f, 60f, 26f..46f, longArrayOf(0xFFFFFFFF), 32, 0.95f, 60f),
            )
            "aurora" -> listOf(
                twinkle(50, SKY_TOP, BAND + 420f, 0xFFFFFFFF, 13f, 33),
                auroraWave(a("aurora")),
                shootingStars(34, 300f, 900f, 3.2f),
                shootingStars(57, BAND + 40f, BAND + 300f, 4.1f),
                shimmer(water(), BAND + 640f, 24, 0xFF7FE9D1, 35),
                flicker(a("fire"), 0xFFFF9A3A, 3.4f, 0.8f, 1.4f),
                sparks(a("fire"), 36),
            )
            "ocean" -> listOf(
                pulse(a("sun"), 0xFFFFFFFF, 3.4f, 0.45f),
                clouds(3, 300f, 560f, 24f, 0xFFFFFFFF, 58),
                beam(a("beam")),
                waves(floatArrayOf(BAND + 440f, BAND + 510f, BAND + 590f, BAND + 680f, BAND + 760f), 46f, 0xFFFFFFFF, 0.65f),
                shimmer(water(), ART_H, 40, 0xFFFFFFFF, 37),
                flyers(Flyer.BIRD, 6, BAND + 80f, BAND + 340f, 90f, 50f, 0xFFFFFFFF, 38, leftward = true),
            )
            "desert" -> listOf(
                sunRays(a("sun"), 0xFFFFE2A0),
                pulse(a("sun"), 0xFFFFD9A0, 3.4f, 0.45f),
                balloons(3, BAND + 60f, BAND + 330f, 59),
                flyers(Flyer.BIRD, 4, BAND + 80f, BAND + 320f, 70f, 46f, 0xFF7A4A2A, 39),
                tumbleweed(BAND + 840f),
            )
            "city" -> listOf(
                twinkle(40, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 13f, 40),
                windowsOff(a("citywin")),
                blink(a("antenna")),
                chase(a("ferris"), longArrayOf(0xFFFF7AC0, 0xFFFFD27A, 0xFF63D6FF, 0xFF7EF0A0)),
                traffic(-20f, 640f, BAND + 625f, 60),
                shimmer(water(), BAND + 780f, 30, 0xFFFFCF73, 41),
                flicker(a("lamp"), 0xFFFFD98A, 3f, 0.5f, 0.5f),
            )
            "farm" -> listOf(
                sunRays(a("sun"), 0xFFFFEBB8),
                pulse(a("sun"), 0xFFFFE6B0, 3.4f, 0.45f),
                clouds(3, 300f, 560f, 22f, 0xFFFFFFFF, 61),
                flyers(Flyer.BIRD, 6, BAND + 60f, BAND + 320f, 80f, 44f, 0xFF5A3A22, 42),
                butterflies(5, 60f, 970f, BAND + 400f, BAND + 820f, longArrayOf(0xFFFFFFFF, 0xFFFFE9A8, 0xFFFFB0CC, 0xFFFFD6E6), 43, 40f),
            )
            "mountains" -> listOf(
                clouds(4, 300f, 560f, 22f, 0xFFFFFFFF, 62),
                clouds(2, BAND + 80f, BAND + 220f, 30f, 0xFFFFFFFF, 63, 70f..110f),
                soaring(560f, BAND + 150f, 0xFF2B2B3A),
                waterfall(905f, BAND + 400f, BAND + 560f, 30f),
                shimmer(water(), BAND + 680f, 30, 0xFFFFFFFF, 44),
                flyers(Flyer.BIRD, 5, BAND + 40f, BAND + 280f, 70f, 42f, 0xFF2B3A4A, 45, leftward = true),
            )
            "warehouse" -> listOf(
                flicker(a("lamp"), 0xFFFFF3D6, 4f, 0.6f, 0.35f),
                motes(80, 0xFFFFF3D6, 46),
            )
            "shopping" -> listOf(
                twinkle(44, SKY_TOP, ART_H - 60f, 0xFFFFFFFF, 17f, 47),
                rise(Piece.HEART, 10, 60f, 40f, 24f..40f, longArrayOf(0xFFFFFFFF, 0xFFFFD166, 0xFFFF8FAB), 64),
            )
            "horizon" -> listOf(
                twinkle(30, SKY_TOP, BAND + 300f, 0xFFD6F5DF, 13f, 48),
                pulse(floatArrayOf(640f, BAND + 360f, 90f), 0xFFF4FFE0, 3f, 0.45f, 1f),
                clouds(3, 300f, 560f, 20f, 0xFFE8F8EC, 65, 70f..120f),
                flyers(Flyer.BIRD, 6, BAND + 80f, BAND + 320f, 70f, 42f, 0xFFD6F5DF, 49),
            )
            // ----- places -----
            "golden_gate" -> {
                val shore = a("surf").let { if (it.size >= 3) it[1] else ART_H - 200f }
                listOf(
                    twinkle(14, SKY_TOP, 760f, 0xFFFFFFFF, 11f, 70),
                    sunRays(a("sun"), 0xFFFFE2B8),
                    pulse(a("sun"), 0xFFFFD8A0, 3.6f, 0.5f),
                    clouds(2, 330f, 620f, 12f, 0xB3F6C9CF, 71, 80f..130f),
                    shimmer(water(), shore - 12f, 34, 0xFFFFE0B8, 72),
                    boat(a("lane"), 0, Boat.SHIP, 73, 26f, 870f),
                    boat(a("lane"), 1, Boat.SAIL, 74, 20f, 870f),
                    fog(a("fog"), 0xFFFFF4F0, 75),
                    trafficPath(a("deck"), 76),
                    blink(a("antenna")),
                    surf(a("surf"), 0xFF9FC3D6),
                    flyers(Flyer.BIRD, 6, BAND + 40f, BAND + 300f, 80f, 46f, 0xFFFFFFFF, 77),
                )
            }
            "fuji" -> listOf(
                sunRays(a("sun"), 0xFFFFF1D6),
                pulse(a("sun"), 0xFFFFE6C8, 3.4f, 0.45f),
                clouds(3, 300f, 620f, 18f, 0xE6FFFFFF, 80),
                clouds(2, BAND + 180f, BAND + 330f, 24f, 0xBFFFFFFF, 81, 70f..120f),
                train(a("rail"), 82, size = 1.25f),
                flyers(Flyer.BIRD, 5, BAND + 40f, BAND + 260f, 70f, 40f, 0xFF3A3550, 83),
                fall(Piece.PETAL, 70, 70f, 80f, 18f..30f, longArrayOf(0xFFFFC2D9, 0xFFFFB0CC, 0xFFFFD6E6, 0xFFFF9CBF), 84),
            )
            "grand_canyon" -> {
                val condors = a("condor")
                listOf(
                    twinkle(16, SKY_TOP, 700f, 0xFFFFF1D6, 11f, 90),
                    sunRays(a("sun"), 0xFFFFD9A0),
                    pulse(a("sun"), 0xFFFFC880, 3.6f, 0.5f),
                    clouds(2, 360f, 640f, 12f, 0x99F7B08A, 91, 90f..150f),
                    shadows(a("shade"), 0xFF2A0F2A, 92),
                    glints(a("glint"), 0xFFFFF1D0, 93),
                    flyers(Flyer.BIRD, 4, BAND + 60f, BAND + 300f, 60f, 40f, 0xFF2A1A2A, 94),
                ) + (0 until condors.size / 3).map { soaring(condors[it * 3], condors[it * 3 + 1], 0xFF1E1418, it * 2.6f, 84f) }
            }
            "cappadocia" -> listOf(
                sunRays(a("sun"), 0xFFFFE6C0),
                pulse(a("sun"), 0xFFFFD8A8, 3.6f, 0.5f),
                clouds(2, 330f, 620f, 12f, 0xCCFFFFFF, 100),
                flicker(a("window"), WARM, 2.6f, 0.55f, 0.6f),
                burners(a("burner"), 102),
                balloonsRise(a("rise"), 101, 7, 1.4f),
                flyers(Flyer.BIRD, 4, BAND + 60f, BAND + 300f, 60f, 38f, 0xFF4A3A4A, 103),
            )
            "paris" -> {
                val lane = a("lane").let { if (it.size >= 3) it[1] else ART_H - 120f }
                listOf(
                    twinkle(40, SKY_TOP, BAND + 200f, 0xFFFFFFFF, 13f, 110),
                    beacon(a("beacon")),
                    sparkle(a("sparkle"), 111),
                    flicker(a("window"), WARM, 2.4f, 0.45f, 0.4f),
                    shimmer(water(), lane + 60f, 30, 0xFFFFD27A, 112),
                    boat(a("lane"), 0, Boat.TOUR, 113, 24f),
                    flicker(a("lamp"), 0xFFFFD98A, 3f, 0.55f, 0.5f),
                )
            }
            "london" -> {
                val road = a("road")
                val roadTop = if (road.size >= 6) min(road[1], road[4]) - 6f else ART_H
                val lane = a("lane").let { if (it.size >= 3) it[1] else ART_H - 120f }
                listOf(
                    twinkle(16, SKY_TOP, 700f, 0xFFFFFFFF, 11f, 120),
                    clouds(2, 330f, 620f, 12f, 0x99F4B6C8, 121, 90f..140f),
                    eye(a("wheel"), roadTop),
                    pulse(a("clock"), 0xFFFFF3C4, 2.2f, 0.6f, 1.2f),
                    flicker(a("window"), WARM, 2.4f, 0.45f, 0.4f),
                    vehicles(road, arrayOf(Vehicle.BUS, Vehicle.CAB, Vehicle.BUS, Vehicle.CAB), 122),
                    shimmer(water(), lane + 60f, 30, 0xFFFFD6B0, 123),
                    boat(a("lane"), 0, Boat.TOUR, 124, 22f),
                    flicker(a("lamp"), 0xFFFFD98A, 3f, 0.55f, 0.5f),
                    flyers(Flyer.BIRD, 5, BAND + 40f, BAND + 260f, 80f, 40f, 0xFFFFFFFF, 125),
                )
            }
            "vegas" -> {
                val road = a("road").let { if (it.size >= 3) it[1] else ART_H - 50f }
                listOf(
                    twinkle(20, SKY_TOP, 800f, 0xFFFFFFFF, 11f, 130),
                    searchlights(a("search")),
                    skyBeam(a("beam")),
                    windowsOff(a("citywin")),
                    neon(a("neon"), 131),
                    bulbs(a("bulb"), 6f, 26f),
                    blink(a("antenna")),
                    shimmer(water(), road - 30f, 30, 0xFFFF9BD8, 133),
                    fountains(a("fountain"), 132),
                    trafficPath(a("road"), 134, 7, 0.06f),
                )
            }
            // ----- just for fun -----
            "rainy" -> {
                val walk = a("walk")
                val ground = if (walk.size >= 3) walk[1] else ART_H - 160f
                listOf(
                    lightning(arrayOf(floatArrayOf(40f, 300f, 240f, 760f), floatArrayOf(792f, 300f, 992f, 760f)), 140, 9f, 0.22f),
                    flicker(a("window"), WARM, 1.5f, 0.35f, 0.5f),
                    flicker(a("lamp"), 0xFFFFD98A, 3.2f, 0.6f, 0.6f),
                    neon(a("neon"), 141, 34f),
                    ripples(a("puddle"), 142),
                    walker(walk, 143),
                    splashes(40, ground - 10f, ART_H - 20f, 144),
                    rain(150, 145),
                )
            }
            "reef" -> {
                val sand = a("sand").let { if (it.size >= 3) it[1] else ART_H - 260f }
                listOf(
                    lightRays(7, 0xFFE8FBFF, 150, sand),
                    kelp(a("kelp"), 151),
                    shimmer(sand, ART_H, 30, 0xFFE0FBFF, 152),
                    jellyfish(3, 420f, BAND + 260f, 156),
                    fish(3, BAND + 40f, BAND + 420f, 40f, longArrayOf(0xFFFFD23F, 0xFF4FC3F7, 0xFFFF8A65), 153, 46f, 7),
                    fish(2, 330f, 620f, 34f, longArrayOf(0xFFB3E5FC, 0xFFFFE082), 154, 40f, 6),
                    turtle(BAND + 100f, BAND + 340f, 155),
                    bubbles(a("bubble"), 157),
                )
            }
            "volcano" -> {
                val p = a("plume")
                val px = if (p.size >= 3) p[0] else 520f
                val py = if (p.size >= 3) p[1] else BAND + 300f
                val ps = if (p.size >= 3) p[2] else 120f
                listOf(
                    twinkle(24, SKY_TOP, 900f, 0xFFFFD6C8, 10f, 160),
                    plume(p),
                    lightning(arrayOf(floatArrayOf(px - 160f, py - ps * 4f, px + 200f, py - ps * 1.2f)), 161, 6f, 0f, 0xFFFFC8A0),
                    lavaFlow(a("lava1"), 162),
                    lavaFlow(a("lava2"), 163),
                    lavaFlow(a("lava3"), 164),
                    pulse(a("glow"), 0xFFFFB347, 1.6f, 0.6f, 2.2f),
                    lavaFountain(a("crater"), 165),
                    steam(a("steam"), 168),
                    embers(a("crater"), 166, 46),
                    shimmer(water(), ART_H, 24, 0xFFFF7A4A, 167),
                )
            }
            "space" -> listOf(
                twinkle(70, SKY_TOP, BAND + 300f, 0xFFFFFFFF, 12f, 170),
                shootingStars(171, 300f, 1000f, 4.5f),
                satellites(2, 380f, BAND + 160f, 172),
                halo(a("earth"), 0xFF7FC8FF, 1.35f, 0.45f, 0.6f),
                rocket(173),
                blink(a("antenna")),
                astronaut(a("astro")),
            )
            "retro" -> {
                val horizon = a("horizon").let { if (it.size >= 3) it[1] else BAND + 430f }
                listOf(
                    twinkle(40, SKY_TOP, BAND + 200f, 0xFFFFFFFF, 12f, 180),
                    shootingStars(181, 300f, 900f, 5.5f),
                    sunStripes(a("sun"), a("sky"), horizon),
                    halo(a("sun"), 0xFFFF5AA8, 1.45f, 0.5f, 0.9f),
                    gridScroll(a("horizon"), 0xFFFF3DF0),
                )
            }
            else -> emptyList()
        }
    }
}
