package app.passstudio.ui

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Moving parts for the places and just-for-fun themes: fog rolling in, waves washing up a beach, ships, cars, buses
 * and a bullet train, the Eiffel Tower's sparkle and beacon, the London Eye turning, fountains and searchlights,
 * rain, lightning and puddle ripples, fish, a turtle, jellyfish, bubbles and kelp, lava, embers and ash, a rocket,
 * an astronaut and satellites, and a synthwave grid and sun. Same rules as ThemeMotion.kt: worked out in the page
 * art's pixels, and each frame is just a function of the time.
 */

private const val TAU = 2f * PI.toFloat()
private const val PAGE_BAND = 1380f

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

/** 1 inside [start, end] of a 0..1 cycle and 0 outside, easing in and out over [fade]. */
private fun window(u: Float, start: Float, end: Float, fade: Float): Float = (min(u - start, end - u) / fade).coerceIn(0f, 1f)

// Anchor lists are (x, y, v) triples.
private val FloatArray.count get() = size / 3
private fun FloatArray.px(i: Int) = this[i * 3]
private fun FloatArray.py(i: Int) = this[i * 3 + 1]
private fun FloatArray.pv(i: Int) = this[i * 3 + 2]

// ---------- sea and shore ----------

/** Fog banks rolling slowly in from the sea on the left, at each fog anchor (0, y, thickness). */
internal fun fog(points: FloatArray, color: Long, seed: Int, n: Int = 12, speed: Float = 16f) = Motion { d, t, m ->
    val span = ART_W + 1000f
    for (f in 0 until points.count) {
        val yc = points.py(f)
        val h = max(points.pv(f), 20f)
        for (k in 0 until n) {
            val j = k + f * 50
            val x = -500f + frac(hash(seed, j) + t * speed * (0.7f + 0.6f * hash(seed, j + 20)) / span) * span
            val y = yc + (hash(seed, j + 40) - 0.5f) * h * 0.8f + sin(t * 0.3f + k) * h * 0.08f
            val r = h * (0.9f + 0.8f * hash(seed, j + 60))
            val a = 0.3f + 0.2f * hash(seed, j + 80)
            d.withTransform({ translate(m.x(x), m.y(y)); scale(3f, 1f, Offset.Zero) }) {
                glow(Offset.Zero, m.d(r), Color(color), a)
            }
        }
    }
}

/** Waves washing up the beach and sliding back, from the shoreline anchor (0, y, how far up the sand they reach). */
internal fun surf(points: FloatArray, water: Long) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val y = points.py(0)
    val reach = points.pv(0)
    val sheet = Path()
    val foam = Path()
    for (w in 0..1) {
        val p = frac(t / 6.5f + w * 0.5f)
        val wash = if (p < 0.3f) sin(p / 0.3f * PI.toFloat() / 2f) else 0.5f + 0.5f * cos((p - 0.3f) / 0.7f * PI.toFloat())
        val r = reach * (if (w == 0) 1f else 0.55f) * wash
        val a = if (p < 0.3f) 1f else 1f - (p - 0.3f) / 0.7f
        sheet.reset()
        foam.reset()
        sheet.moveTo(m.x(0f), m.y(y - 4f))
        for (k in 0..40) {
            val x = ART_W * k / 40f
            val e = y + r + 5f * sin(x * 0.017f + w * 2.1f + t * 0.35f) + 3f * sin(x * 0.047f - t * 0.6f + w)
            sheet.lineTo(m.x(x), m.y(e))
            if (k == 0) foam.moveTo(m.x(x), m.y(e)) else foam.lineTo(m.x(x), m.y(e))
        }
        sheet.lineTo(m.x(ART_W), m.y(y - 4f))
        sheet.close()
        d.drawPath(sheet, Color(water), alpha = 0.38f * a)
        d.drawPath(foam, Color.White, alpha = 0.3f * a, style = Stroke(m.d(12f), cap = StrokeCap.Round, join = StrokeJoin.Round))
        d.drawPath(foam, Color.White, alpha = 0.85f * a, style = Stroke(m.d(4.5f), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Light glinting at points on water (x, y, width), like a river catching the sun. */
internal fun glints(points: FloatArray, color: Long, seed: Int) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        val a = max(0f, sin(t * (1.3f + hash(seed, k)) + k * 2.1f)).pow(3)
        if (a > 0.02f) {
            val w = m.d(max(8f, points.pv(k) * 0.55f))
            val h = m.d(2.6f)
            d.drawRoundRect(Color(color), Offset(m.x(points.px(k)) - w / 2f, m.y(points.py(k)) - h / 2f), Size(w, h), CornerRadius(h / 2f),
                alpha = 0.85f * a)
        }
    }
}

/** What crosses the water. */
enum class Boat { SHIP, SAIL, TOUR }

/**
 * A boat crossing along water lane [lane] (lanes are (0, y, size), y at the waterline): a container ship, a sailboat
 * or a lit tour boat, one way and then back the other, at [speed] pixels a second. It goes out of sight past
 * [clipRight] (behind a cliff, say).
 */
internal fun boat(points: FloatArray, lane: Int, kind: Boat, seed: Int, speed: Float, clipRight: Float = ART_W) = Motion { d, t, m ->
    if (points.count <= lane) return@Motion
    val y = points.py(lane)
    val s = if (points.pv(lane) > 0f) points.pv(lane) else 1f
    val span = ART_W + 700f
    val u = hash(seed, 3) + t * speed / span
    val lap = floor(u).toInt()
    val p = u - lap
    val leftward = lap % 2 == 0
    val x = if (leftward) ART_W + 350f - p * span else -350f + p * span
    val bob = sin(t * 1.3f + seed) * 1.5f
    // Drawn facing left; flipped when heading right.
    d.clipRect(right = m.x(clipRight)) {
        withTransform({ translate(m.x(x), m.y(y + bob)); scale(m.d(s) * (if (leftward) 1f else -1f), m.d(s), Offset.Zero) }) {
            when (kind) {
                Boat.SHIP -> ship()
                Boat.SAIL -> sailboat(t)
                Boat.TOUR -> tourBoat(t)
            }
        }
    }
}

private val CONTAINERS = longArrayOf(0xFFD9622A, 0xFF2F6FB0, 0xFF3A9A8A, 0xFFB8362F, 0xFF8A8F98, 0xFFE0A030)

private fun DrawScope.ship() {
    drawRect(Color.Black, Offset(-112f, 0f), Size(224f, 6f), alpha = 0.18f)
    val hull = Path().apply {
        moveTo(-128f, -24f); lineTo(118f, -24f); lineTo(116f, -6f); quadraticTo(112f, 0f, 104f, 0f); lineTo(-104f, 0f)
        quadraticTo(-120f, -8f, -128f, -24f); close()
    }
    drawPath(hull, Color(0xFF2B3140))
    drawRect(Color(0xFF8E2B2B), Offset(-110f, -6f), Size(214f, 4f))
    for (row in 0..1) {
        for (c in 0 until 9) {
            if (row == 1 && (c == 0 || c == 8)) continue
            drawRect(Color(CONTAINERS[(c * 3 + row * 5) % CONTAINERS.size]), Offset(-98f + c * 19f, -24f - 11f * (row + 1)), Size(18f, 10f))
        }
    }
    drawRect(Color(0xFFF2F2EE), Offset(84f, -58f), Size(24f, 34f))
    drawRect(Color(0xFF3A4A5A), Offset(84f, -54f), Size(24f, 4f))
    drawRect(Color(0xFF2B3140), Offset(95f, -70f), Size(8f, 12f))
    glow(Offset(84f, -54f), 12f, Color(0xFFFFF3C4), 0.6f)
    glow(Offset(-126f, -26f), 7f, Color(0xFFFFFFFF), 0.7f)
}

private fun DrawScope.sailboat(t: Float) {
    withTransform({ rotate(sin(t * 0.9f) * 2.5f, Offset(0f, 0f)) }) {
        val hull = Path().apply {
            moveTo(-34f, -8f); lineTo(36f, -8f); quadraticTo(30f, 4f, 18f, 5f); lineTo(-22f, 5f); quadraticTo(-30f, 2f, -34f, -8f); close()
        }
        drawPath(hull, Color(0xFFF7F4EE))
        drawRect(Color(0xFF2B3A55), Offset(-30f, -8f), Size(64f, 3f))
        drawLine(Color(0xFF5A5A5A), Offset(4f, -8f), Offset(4f, -104f), 2f)
        drawPath(Path().apply { moveTo(6f, -100f); lineTo(6f, -12f); lineTo(42f, -12f); close() }, Color.White)
        drawPath(Path().apply { moveTo(2f, -94f); lineTo(2f, -12f); lineTo(-30f, -12f); close() }, Color(0xFFFFF1D6))
    }
    drawRect(Color.Black, Offset(-24f, 5f), Size(52f, 4f), alpha = 0.15f)
}

private fun DrawScope.tourBoat(t: Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFFFFD27A).copy(alpha = 0.35f), Color.Transparent), 2f, 34f), Offset(-96f, 2f), Size(192f, 32f))
    val hull = Path().apply { moveTo(-108f, -10f); lineTo(100f, -10f); lineTo(98f, 0f); lineTo(-96f, 0f); quadraticTo(-104f, -2f, -108f, -10f); close() }
    drawPath(hull, Color(0xFF1C2238))
    drawRoundRect(Color(0xFF2A3350), Offset(-88f, -30f), Size(176f, 20f), CornerRadius(6f))
    for (w in 0 until 14) {
        drawRect(Color(0xFFFFE2A0), Offset(-84f + w * 12.2f, -27f), Size(9.5f, 13f), alpha = 0.82f + 0.18f * sin(t * 3f + w))
    }
    drawRect(Color(0xFFE8ECF4), Offset(-90f, -32f), Size(180f, 3f))
    glow(Offset(0f, -18f), 110f, Color(0xFFFFD27A), 0.2f)
    glow(Offset(-104f, -14f), 9f, Color.White, 0.85f)
}

// ---------- roads and rails ----------

/** Cars on a road between two anchors (x, y, size): tail lights heading off one way, headlights coming back. */
internal fun trafficPath(points: FloatArray, seed: Int, n: Int = 6, speed: Float = 0.085f) = Motion { d, t, m ->
    if (points.count < 2) return@Motion
    for (lane in 0..1) {
        val c = if (lane == 0) Color(0xFFFF4A4A) else Color(0xFFFFF3C4)
        for (i in 0 until n) {
            val q = frac(hash(seed, i + lane * 20) + t * speed * (0.8f + 0.4f * hash(seed, i + 40 + lane * 20)))
            val p = if (lane == 0) q else 1f - q
            val s = lerp(points.pv(0), points.pv(1), p)
            val x = lerp(points.px(0), points.px(1), p)
            val y = lerp(points.py(0), points.py(1), p) + (if (lane == 0) -2.2f else 2.2f) * s
            d.glow(m.at(x, y), m.d(15f * s), c, 0.5f)
            d.drawRoundRect(c, Offset(m.x(x - 6f * s), m.y(y - 1.6f * s)), Size(m.d(12f * s), m.d(3.2f * s)), CornerRadius(m.d(1.6f * s)))
        }
    }
}

/**
 * A bullet train speeding along the track between two anchors (x, y, size; left end first), one way and then the
 * other. It's clipped to that stretch, so it slides out from behind whatever stands at either end.
 */
internal fun train(points: FloatArray, seed: Int, period: Float = 15f, cars: Int = 9, size: Float = 1f) = Motion { d, t, m ->
    if (points.count < 2) return@Motion
    val x0 = points.px(0)
    val x1 = points.px(1)
    val s = (if (points.pv(0) > 0f) points.pv(0) else 1f) * size
    val len = 74f * s * cars + 40f * s
    val u = t / period + hash(seed, 1)
    val lap = floor(u).toInt()
    val p = u - lap
    if (p > 0.42f) return@Motion
    val q = p / 0.42f
    val rightward = lap % 2 == 1
    val travel = (x1 - x0) + len + 80f
    val head = if (rightward) x0 - 40f + q * travel else x1 + 40f - q * travel
    val y = lerp(points.py(0), points.py(1), ((head - x0) / (x1 - x0)).coerceIn(0f, 1f))
    d.clipRect(left = m.x(x0), right = m.x(x1)) {
        withTransform({ translate(m.x(head), m.y(y)); scale(m.d(s) * (if (rightward) 1f else -1f), m.d(s), Offset.Zero) }) {
            // The lead car's nose points the way it's going (local +x).
            val nose = Path().apply { moveTo(-2f, -20f); quadraticTo(26f, -19f, 42f, -3f); lineTo(42f, 0f); lineTo(-2f, 0f); close() }
            drawPath(nose, Color(0xFFF4F6F8))
            drawPath(Path().apply { moveTo(6f, -18f); quadraticTo(18f, -17.5f, 24f, -12f); lineTo(6f, -12f); close() }, Color(0xFF34405A))
            drawRect(Color(0xFF1F4AA8), Offset(-2f, -7f), Size(36f, 2.6f))
            glow(Offset(40f, -4f), 9f, Color(0xFFFFF6D6), 0.8f)
            for (c in 0 until cars) {
                val l = -(c + 1) * 74f
                drawRoundRect(Color(0xFFF4F6F8), Offset(l + 1f, -20f), Size(72f, 20f), CornerRadius(3f))
                drawRect(Color(0xFF1F4AA8), Offset(l + 1f, -7f), Size(72f, 2.6f))
                drawRect(Color(0xFF3B4A66), Offset(l + 6f, -16f), Size(62f, 4.5f), alpha = 0.85f)
                drawRect(Color(0xFF2A2F3A), Offset(l + 4f, -1.6f), Size(66f, 2f))
            }
        }
    }
}

/** What drives across a bridge. */
enum class Vehicle { BUS, CAB }

/**
 * Buses and cabs crossing the road between two anchors (x, y, size; left end first), each now and then, both ways,
 * clipped to that stretch of road.
 */
internal fun vehicles(points: FloatArray, kinds: Array<Vehicle>, seed: Int, speed: Float = 62f) = Motion { d, t, m ->
    if (points.count < 2) return@Motion
    val x0 = points.px(0)
    val x1 = points.px(1)
    val span = (x1 - x0) + 400f
    d.clipRect(left = m.x(x0), right = m.x(x1)) {
        kinds.forEachIndexed { i, kind ->
            val u = hash(seed, i + 10) + t * speed * (0.8f + 0.4f * hash(seed, i)) / (span * 1.5f)
            val lap = floor(u).toInt()
            val p = (u - lap) * 1.5f
            if (p > 1f) return@forEachIndexed
            val rightward = (lap + i) % 2 == 0
            val x = if (rightward) x0 - 200f + p * span else x1 + 200f - p * span
            val f = ((x - x0) / (x1 - x0)).coerceIn(0f, 1f)
            val y = lerp(points.py(0), points.py(1), f)
            val s = lerp(points.pv(0), points.pv(1), f).let { if (it > 0f) it else 1f }
            withTransform({ translate(m.x(x), m.y(y)); scale(m.d(s) * (if (rightward) 1f else -1f), m.d(s), Offset.Zero) }) {
                when (kind) {
                    Vehicle.BUS -> bus()
                    Vehicle.CAB -> cab()
                }
            }
        }
    }
}

/** A red double-decker, facing +x, standing on y = 0. */
private fun DrawScope.bus() {
    drawRoundRect(Color(0xFFD7262E), Offset(-42f, -44f), Size(84f, 40f), CornerRadius(5f))
    drawRect(Color(0xFFF0E6C8), Offset(-42f, -24f), Size(84f, 2.5f))
    for (w in 0 until 6) {
        drawRect(Color(0xFFFFE9B0), Offset(-38f + w * 13.2f, -39f), Size(10f, 9f), alpha = 0.9f)
        drawRect(Color(0xFFFFE9B0), Offset(-38f + w * 13.2f, -19f), Size(10f, 8f), alpha = 0.9f)
    }
    drawRect(Color(0xFF2A2A30), Offset(-42f, -6f), Size(84f, 2.5f))
    for (wx in floatArrayOf(-26f, 27f)) {
        drawCircle(Color(0xFF1E1E22), 5.5f, Offset(wx, -3f))
        drawCircle(Color(0xFF8A8A92), 2f, Offset(wx, -3f))
    }
    glow(Offset(43f, -10f), 16f, Color(0xFFFFF3C4), 0.7f)
    glow(Offset(-43f, -10f), 9f, Color(0xFFFF4A4A), 0.6f)
}

/** A black cab, facing +x, standing on y = 0. */
private fun DrawScope.cab() {
    val body = Path().apply {
        moveTo(-22f, -5f); lineTo(-22f, -14f); quadraticTo(-20f, -17f, -14f, -17f); lineTo(-10f, -25f); lineTo(10f, -25f)
        lineTo(15f, -17f); lineTo(20f, -16f); quadraticTo(23f, -14f, 23f, -9f); lineTo(23f, -5f); close()
    }
    drawPath(body, Color(0xFF18181C))
    drawRect(Color(0xFF9FB4C8), Offset(-8.5f, -23f), Size(7.5f, 5.5f), alpha = 0.8f)
    drawRect(Color(0xFF9FB4C8), Offset(1f, -23f), Size(8f, 5.5f), alpha = 0.8f)
    drawRoundRect(Color(0xFFFFC94A), Offset(-4f, -28.5f), Size(8f, 3.5f), CornerRadius(1f))
    glow(Offset(0f, -27f), 8f, Color(0xFFFFC94A), 0.6f)
    drawCircle(Color(0xFF0E0E10), 4.2f, Offset(-13f, -4f))
    drawCircle(Color(0xFF0E0E10), 4.2f, Offset(14f, -4f))
    glow(Offset(23f, -10f), 12f, Color(0xFFFFF3C4), 0.6f)
}

/** Someone under an umbrella walking along the sidewalk between two anchors (feet level: x, y, size), now and then. */
internal fun walker(points: FloatArray, seed: Int, speed: Float = 40f) = Motion { d, t, m ->
    if (points.count < 2) return@Motion
    val x0 = points.px(0)
    val x1 = points.px(1)
    val span = (x1 - x0) + 240f
    val u = hash(seed, 1) + t * speed / (span * 1.4f)
    val lap = floor(u).toInt()
    val p = (u - lap) * 1.4f
    if (p > 1f) return@Motion
    val rightward = lap % 2 == 0
    val x = if (rightward) x0 - 120f + p * span else x1 + 120f - p * span
    val f = ((x - x0) / (x1 - x0)).coerceIn(0f, 1f)
    val y = lerp(points.py(0), points.py(1), f)
    val s = lerp(points.pv(0), points.pv(1), f).let { if (it > 0f) it else 1f }
    val step = sin(t * 7f)
    val umbrella = Color(longArrayOf(0xFFD7262E, 0xFFF2B33D, 0xFF2F6FB0)[lap % 3])
    val coat = Color(0xFF232733)
    d.drawOval(Color.Black, Offset(m.x(x - 16f * s), m.y(y - 2f * s)), Size(m.d(32f * s), m.d(5f * s)), alpha = 0.22f)
    d.withTransform({ translate(m.x(x), m.y(y)); scale(m.d(s) * (if (rightward) 1f else -1f), m.d(s), Offset.Zero) }) {
        drawLine(coat, Offset(-2f, -34f), Offset(-2f + 9f * step, 0f), 5f, StrokeCap.Round)
        drawLine(coat, Offset(2f, -34f), Offset(2f - 9f * step, 0f), 5f, StrokeCap.Round)
        drawRoundRect(coat, Offset(-9f, -72f), Size(18f, 42f), CornerRadius(6f))
        drawCircle(Color(0xFFE0B48A), 7f, Offset(1f, -79f))
        drawLine(coat, Offset(4f, -64f), Offset(8f, -84f), 4f, StrokeCap.Round)
        drawLine(Color(0xFF2A2A2A), Offset(8f, -82f), Offset(8f, -108f), 2f)
        val dome = Path().apply {
            moveTo(-24f, -102f); quadraticTo(8f, -144f, 40f, -102f)
            for (k in 0 until 8) quadraticTo(36f - k * 8f, -107f, 32f - k * 8f, -102f)
            close()
        }
        drawPath(dome, umbrella)
        drawPath(Path().apply { moveTo(-10f, -114f); quadraticTo(4f, -132f, 22f, -126f) }, Color.White, alpha = 0.35f,
            style = Stroke(3f, cap = StrokeCap.Round))
    }
}

// ---------- lights ----------

/** The Eiffel Tower's sparkle: lights flashing all over the ironwork, in a burst every so often and softly between. */
internal fun sparkle(points: FloatArray, seed: Int) = Motion { d, t, m ->
    val show = 0.3f + 0.7f * window(frac(t / 14f), 0.05f, 0.65f, 0.06f)
    for (k in 0 until points.count) {
        val f = max(0f, sin(t * (4f + 5f * hash(seed, k)) + hash(seed, k + 500) * TAU)).pow(14)
        val a = f * show
        if (a > 0.04f) {
            val c = m.at(points.px(k), points.py(k))
            d.glow(c, m.d(18f), Color.White, 0.55f * a)
            d.shape(SPARKLE, c.x, c.y, m.d(10f) * (0.5f + 0.5f * f), 0f, Color.White, a)
        }
    }
}

/** A beacon on top of a tower (x, y, beam length): two beams turning, flaring as one faces you. */
internal fun beacon(points: FloatArray) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val x = points.px(0)
    val y = points.py(0)
    val len = max(points.pv(0), 200f)
    for (b in 0..1) {
        val th = t * 0.9f + b * PI.toFloat()
        val dx = cos(th) * len
        val toward = max(0f, sin(th))
        if (abs(dx) < 8f) continue
        val a = 0.3f + 0.25f * toward
        val spread = 0.035f + 0.05f * toward
        val path = Path().apply {
            moveTo(m.x(x), m.y(y))
            lineTo(m.x(x + dx), m.y(y - abs(dx) * spread - 6f))
            lineTo(m.x(x + dx), m.y(y + abs(dx) * spread + 6f))
            close()
        }
        d.drawPath(path, Brush.linearGradient(listOf(Color(0xFFFFF6D6).copy(alpha = a), Color.Transparent), m.at(x, y), m.at(x + dx, y)))
    }
    val flash = max(0f, sin(t * 0.9f)).pow(8) + max(0f, sin(t * 0.9f + PI.toFloat())).pow(8)
    d.glow(m.at(x, y), m.d(22f + 60f * flash), Color(0xFFFFF3C4), 0.6f + 0.35f * flash)
}

/** A beam of light straight up the sky from (x, y), shimmering. */
internal fun skyBeam(points: FloatArray) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val x = points.px(0)
    val y = points.py(0)
    val f = 0.78f + 0.22f * sin(t * 2.2f) * sin(t * 0.7f)
    val top = m.y(m.top)
    val bottom = m.y(y)
    for ((w, a) in arrayOf(30f to 0.12f, 10f to 0.32f, 3.5f to 0.8f)) {
        d.drawRect(Brush.verticalGradient(listOf(Color(0xFFEAF6FF).copy(alpha = 0f), Color(0xFFEAF6FF).copy(alpha = a * f)), top, bottom),
            Offset(m.x(x - w / 2f), top), Size(m.d(w), bottom - top))
    }
    d.glow(m.at(x, y), m.d(44f), Color(0xFFEAF6FF), 0.8f * f)
}

/** Searchlights sweeping the sky from rooftop bases (x, y). */
internal fun searchlights(points: FloatArray) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        val bx = points.px(k)
        val by = points.py(k)
        val ang = sin(t * 0.35f + k * 2.1f) * 0.45f
        val len = 1500f
        val ex = bx + sin(ang) * len
        val ey = by - cos(ang) * len
        val px = cos(ang) * 70f
        val py = sin(ang) * 70f
        val path = Path().apply {
            moveTo(m.x(bx - 4f), m.y(by)); lineTo(m.x(ex - px), m.y(ey - py)); lineTo(m.x(ex + px), m.y(ey + py)); lineTo(m.x(bx + 4f), m.y(by)); close()
        }
        d.drawPath(path, Brush.linearGradient(listOf(Color(0xFFDDEBFF).copy(alpha = 0.26f), Color.Transparent), m.at(bx, by), m.at(ex, ey)))
        d.glow(m.at(bx, by), m.d(18f), Color(0xFFEAF2FF), 0.7f)
    }
}

/** Neon signs and trims glowing (x, y, color), buzzing off and on now and then. */
internal fun neon(points: FloatArray, seed: Int, reach: Float = 30f) = Motion { d, t, m ->
    val frame = floor(t * 18f).toInt()
    for (k in 0 until points.count) {
        val group = k / 4
        val buzzing = frac(t / 7f + hash(seed, group) * 0.9f) < 0.06f
        val on = if (buzzing) (if (hash(seed, frame * 7 + group) > 0.45f) 1f else 0.1f) else 0.85f + 0.15f * sin(t * 11f + k)
        d.glow(m.at(points.px(k), points.py(k)), m.d(reach), rgb(points.pv(k)), 0.6f * on)
    }
}

/** Burner flames firing now and then under balloons drawn in the art (x, y, size). */
internal fun burners(points: FloatArray, seed: Int) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        if (frac(t / 3.6f + hash(seed, k)) > 0.25f) continue
        val f = 0.75f + 0.25f * sin(t * 30f + k)
        val s = max(points.pv(k), 3f)
        val c = m.at(points.px(k), points.py(k))
        d.glow(c, m.d(s * 2.6f), Color(0xFFFFA43A), 0.85f * f)
        d.glow(c, m.d(s * 0.9f), Color(0xFFFFF3B0), 0.9f * f)
    }
}

/** A soft ring of light around each anchor's circle (x, y, r), swelling slowly: an atmosphere, a sun's halo. */
internal fun halo(points: FloatArray, color: Long, reach: Float, strength: Float, speed: Float = 0.8f) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        val r = points.pv(k)
        if (r <= 0f) continue
        val f = 0.5f + 0.5f * sin(t * speed + k)
        val outer = r * reach * (0.95f + 0.1f * f)
        val inner = r / outer
        val c = Color(color)
        val center = m.at(points.px(k), points.py(k))
        val brush = Brush.radialGradient(
            0f to Color.Transparent, inner * 0.92f to Color.Transparent, inner to c.copy(alpha = strength * (0.6f + 0.4f * f)),
            1f to Color.Transparent, center = center, radius = m.d(outer),
        )
        d.drawCircle(brush, m.d(outer), center)
    }
}

// ---------- wheels, fountains, balloons ----------

/** The London Eye turning slowly: spokes and capsules around the rim anchor (x, y, r), clipped above [clipBottom]. */
internal fun eye(points: FloatArray, clipBottom: Float) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val cx = points.px(0)
    val cy = points.py(0)
    val r = points.pv(0)
    val turn = t * 0.06f
    d.clipRect(bottom = m.y(clipBottom)) {
        val hub = m.at(cx, cy)
        for (k in 0 until 32) {
            val a = turn + TAU * k / 32f
            drawLine(Color(0xFFE9EEF5), hub, m.at(cx + r * cos(a), cy + r * sin(a)), m.d(1.3f), alpha = 0.55f)
        }
        val w = m.d(15f)
        val h = m.d(9f)
        for (k in 0 until 32) {
            val a = turn + TAU * k / 32f + TAU / 64f
            val c = m.at(cx + (r + 9f) * cos(a), cy + (r + 9f) * sin(a))
            drawOval(Color(0xFFDDE8F2), Offset(c.x - w / 2f, c.y - h / 2f), Size(w, h))
            drawOval(Color(0xFFFFE6A8), Offset(c.x - w * 0.35f, c.y - h * 0.3f), Size(w * 0.7f, h * 0.6f), alpha = 0.8f)
        }
    }
}

/** Fountains dancing in a row (x, y, max height): a wave running along them, and now and then a big shot. */
internal fun fountains(points: FloatArray, seed: Int) = Motion { d, t, m ->
    val burst = window(frac(t / 12f), 0.62f, 0.8f, 0.04f)
    for (k in 0 until points.count) {
        val x = points.px(k)
        val y = points.py(k)
        val maxH = max(points.pv(k), 40f)
        val wave = max(0f, sin(t * 1.7f - k * 0.55f))
        val h = maxH * (0.22f + 0.6f * wave + 0.45f * burst)
        val sway = sin(t * 0.8f + k) * 0.06f * h
        val top = Offset(m.x(x + sway), m.y(y - h))
        val base = m.at(x, y)
        val w = m.d(9f)
        val jet = Path().apply {
            moveTo(base.x - w / 2f, base.y)
            quadraticTo(base.x - w * 0.3f, (base.y + top.y) / 2f, top.x - w * 0.25f, top.y)
            lineTo(top.x + w * 0.25f, top.y)
            quadraticTo(base.x + w * 0.3f, (base.y + top.y) / 2f, base.x + w / 2f, base.y)
            close()
        }
        d.drawPath(jet, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.85f)), top.y, base.y))
        for (j in 0 until 8) {
            val p = frac(t * 1.2f + hash(seed, k * 13 + j))
            val dx = (hash(seed, k * 31 + j) - 0.5f) * 2f * (20f + h * 0.12f) * p
            val dy = -h + h * 0.35f * p * p
            d.drawCircle(Color.White, m.d(2.2f), m.at(x + sway + dx, y + dy), alpha = 0.7f * (1f - p))
        }
        d.glow(top, m.d(26f + h * 0.12f), Color(0xFFE8F6FF), 0.35f)
        d.glow(base, m.d(22f), Color(0xFFE8F6FF), 0.35f)
    }
}

private val BALLOON_COLORS = arrayOf(
    longArrayOf(0xFFE63946, 0xFFFFD166), longArrayOf(0xFF3FA9F5, 0xFFFFFFFF), longArrayOf(0xFFB07CF2, 0xFFFF8FAB),
    longArrayOf(0xFF2A9D8F, 0xFFE9C46A), longArrayOf(0xFFFF8C42, 0xFF6A4C93), longArrayOf(0xFFFF70A6, 0xFF70D6FF),
)

/** Hot-air balloons rising slowly from the valley anchors (x, y, size) up the page, drifting, burners firing. */
internal fun balloonsRise(points: FloatArray, seed: Int, n: Int = 6, size: Float = 1f) = Motion { d, t, m ->
    if (points.count == 0) return@Motion
    for (i in 0 until n) {
        val from = i % points.count
        val sx = points.px(from)
        val sy = points.py(from)
        val k = (if (points.pv(from) > 0f) points.pv(from) else 1f) * (0.7f + 0.5f * hash(seed, i)) * size
        val rise = sy - (m.top - 220f)
        val p = frac(hash(seed, i + 3) + t * (14f + 8f * hash(seed, i + 7)) / rise)
        val y = sy - p * rise
        val x = sx + (hash(seed, i + 11) - 0.5f) * 160f + p * (hash(seed, i + 13) - 0.5f) * 520f + sin(t * 0.4f + i) * 14f
        val colors = BALLOON_COLORS[i % BALLOON_COLORS.size]
        val flame = frac(t / 3.2f + hash(seed, i + 17)) < 0.28f
        d.withTransform({ translate(m.x(x), m.y(y)); scale(m.d(1f), m.d(1f), Offset.Zero) }) {
            balloon(k, Color(colors[0]), Color(colors[1]), min(1f, p * 8f), flame)
        }
    }
}

private fun DrawScope.balloon(k: Float, c1: Color, c2: Color, alpha: Float, flame: Boolean) {
    val env = Path().apply {
        moveTo(0f, -70f * k)
        cubicTo(62f * k, -70f * k, 66f * k, 10f * k, 22f * k, 52f * k)
        lineTo(14f * k, 64f * k); lineTo(-14f * k, 64f * k); lineTo(-22f * k, 52f * k)
        cubicTo(-66f * k, 10f * k, -62f * k, -70f * k, 0f, -70f * k)
        close()
    }
    clipPath(env) {
        drawRect(c1, Offset(-70f * k, -80f * k), Size(140f * k, 150f * k), alpha = alpha)
        for (g in 0 until 3) drawRect(c2, Offset((-50f + g * 36f) * k, -80f * k), Size(14f * k, 150f * k), alpha = alpha)
        drawOval(Color.Black, Offset(10f * k, -80f * k), Size(70f * k, 150f * k), alpha = 0.12f * alpha)
        drawOval(Color.White, Offset(-40f * k, -52f * k), Size(14f * k, 50f * k), alpha = 0.3f * alpha)
    }
    drawLine(Color(0xFF6B4A2A), Offset(-14f * k, 64f * k), Offset(-10f * k, 86f * k), 1.6f * k + 0.5f, alpha = alpha)
    drawLine(Color(0xFF6B4A2A), Offset(14f * k, 64f * k), Offset(10f * k, 86f * k), 1.6f * k + 0.5f, alpha = alpha)
    drawRoundRect(Color(0xFFA8743A), Offset(-12f * k, 86f * k), Size(24f * k, 17f * k), CornerRadius(3f * k), alpha = alpha)
    if (flame) {
        glow(Offset(0f, 70f * k), 24f * k, Color(0xFFFFB347), 0.8f * alpha)
        drawOval(Color(0xFFFFE08A), Offset(-4f * k, 62f * k), Size(8f * k, 16f * k), alpha = alpha)
    }
}

// ---------- weather ----------

/** Cloud shadows drifting over the land, inside the shade anchor (0, top, height). */
internal fun shadows(points: FloatArray, color: Long, seed: Int, n: Int = 4) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val y0 = points.py(0)
    val h = points.pv(0)
    val span = ART_W + 1000f
    for (k in 0 until n) {
        val x = -500f + frac(hash(seed, k) + t * (9f + 5f * hash(seed, k + 10)) / span) * span
        val y = y0 + h * (0.15f + 0.7f * hash(seed, k + 20))
        val r = 120f + 90f * hash(seed, k + 30)
        d.withTransform({ translate(m.x(x), m.y(y)); scale(2.4f, 0.8f, Offset.Zero) }) {
            glow(Offset.Zero, m.d(r), Color(color), 0.3f)
        }
    }
}

/** Rain over the whole page, slanting a little in the wind. */
internal fun rain(n: Int, seed: Int, slant: Float = -0.18f, speed: Float = 1100f, color: Long = 0xFFBFD4F0) = Motion { d, t, m ->
    val span = ART_H - m.top + 100f
    val extra = abs(slant) * span
    for (i in 0 until n) {
        val y = m.top - 60f + frac(hash(seed, i + 1000) + t * speed * (0.8f + 0.4f * hash(seed, i)) / span) * span
        val len = 26f + 30f * hash(seed, i + 2000)
        val x = -60f + hash(seed, i + 3000) * (ART_W + 120f + extra) - (if (slant > 0f) extra else 0f) + slant * (y - m.top)
        d.drawLine(Color(color), m.at(x, y), m.at(x - slant * len, y - len), m.d(1.8f), StrokeCap.Round, alpha = 0.28f + 0.3f * hash(seed, i + 4000))
    }
}

/** Little splashes where the rain hits the ground, between y0 and y1. */
internal fun splashes(n: Int, y0: Float, y1: Float, seed: Int) = Motion { d, t, m ->
    for (i in 0 until n) {
        val u = t / (0.6f + 0.5f * hash(seed, i)) + hash(seed, i + 7)
        val cycle = floor(u).toInt()
        val p = u - cycle
        if (p > 0.35f) continue
        val q = p / 0.35f
        val x = hash(seed, cycle * 13 + i) * ART_W
        val y = y0 + hash(seed, cycle * 29 + i) * (y1 - y0)
        val r = 3f + 9f * q
        val a = 0.6f * (1f - q)
        d.drawLine(Color(0xFFDDE8F8), m.at(x - r * 0.4f, y - r * 0.2f), m.at(x - r, y - r * 0.9f), m.d(1.4f), StrokeCap.Round, alpha = a)
        d.drawLine(Color(0xFFDDE8F8), m.at(x + r * 0.4f, y - r * 0.2f), m.at(x + r, y - r * 0.9f), m.d(1.4f), StrokeCap.Round, alpha = a)
    }
}

/** Rings spreading on puddles (x, y, width) as the rain hits them. */
internal fun ripples(points: FloatArray, seed: Int) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        val w = max(points.pv(k), 30f)
        for (j in 0 until 3) {
            val u = t / (0.9f + 0.4f * hash(seed, k * 7 + j)) + hash(seed, k * 11 + j)
            val cycle = floor(u).toInt()
            val p = u - cycle
            val x = points.px(k) + (hash(seed, cycle * 17 + k * 5 + j) - 0.5f) * w * 0.6f
            val y = points.py(k) + (hash(seed, cycle * 23 + k * 5 + j) - 0.5f) * w * 0.1f
            val r = w * 0.22f * p
            d.drawOval(Color(0xFFDDE8F8), Offset(m.x(x - r), m.y(y - r * 0.32f)), Size(m.d(2f * r), m.d(0.64f * r)), alpha = 0.7f * (1f - p),
                style = Stroke(m.d(1.6f)))
        }
    }
}

/**
 * Lightning now and then somewhere in [areas] (x0, y0, x1, y1): a jagged bolt with a branch, flickering twice. With
 * [flash] above 0 the whole screen lights up with it.
 */
internal fun lightning(areas: Array<FloatArray>, seed: Int, period: Float, flash: Float, tint: Long = 0xFFB8C8FF) = Motion { d, t, m ->
    val u = t / period
    val cycle = floor(u).toInt()
    val dt = (u - cycle) * period - hash(seed, cycle) * (period - 1f)
    if (dt < 0f || dt > 0.5f) return@Motion
    val on = when {
        dt < 0.1f -> 1f
        dt < 0.17f -> 0.1f
        dt < 0.24f -> 0.9f
        else -> 1f - (dt - 0.24f) / 0.26f
    }
    if (flash > 0f) d.drawRect(Color(0xFFDDE6FF), alpha = flash * on)
    val area = areas[(hash(seed, cycle + 3) * areas.size).toInt().coerceIn(0, areas.size - 1)]
    val bolt = Path()
    val branch = Path()
    var x = area[0] + hash(seed, cycle + 7) * (area[2] - area[0])
    var y = area[1]
    val startX = x
    bolt.moveTo(m.x(x), m.y(y))
    val dy = (area[3] - area[1]) / 10f
    var bx = x
    var by = y
    for (s in 1..10) {
        x += (hash(seed, cycle * 31 + s) - 0.5f) * 60f
        y += dy
        bolt.lineTo(m.x(x), m.y(y))
        if (s == 4) {
            bx = x
            by = y
        }
    }
    branch.moveTo(m.x(bx), m.y(by))
    for (s in 1..4) {
        bx += 18f + (hash(seed, cycle * 17 + s) - 0.3f) * 40f
        by += dy * 0.7f
        branch.lineTo(m.x(bx), m.y(by))
    }
    d.glow(m.at(startX, area[1] + (area[3] - area[1]) * 0.4f), m.d((area[3] - area[1]) * 0.6f), Color(tint), 0.35f * on)
    for ((path, width) in arrayOf(bolt to 3.4f, branch to 2.2f)) {
        d.drawPath(path, Color(tint), alpha = 0.35f * on, style = Stroke(m.d(12f), cap = StrokeCap.Round, join = StrokeJoin.Round))
        d.drawPath(path, Color.White, alpha = on, style = Stroke(m.d(width), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ---------- under the sea ----------

/** Soft rays of sunlight slanting down from the surface, swaying, down to [yEnd]. */
internal fun lightRays(n: Int, color: Long, seed: Int, yEnd: Float) = Motion { d, t, m ->
    val top = m.top
    for (i in 0 until n) {
        val xt = ART_W * (i + 0.5f) / n + (hash(seed, i) - 0.5f) * 120f + sin(t * 0.18f + i * 1.9f) * 50f
        val xb = xt + 160f + 60f * hash(seed, i + 10)
        val w1 = 18f + 30f * hash(seed, i + 20)
        val w2 = w1 * 3f
        val a = 0.1f + 0.08f * sin(t * 0.5f + i * 2.3f)
        val path = Path().apply {
            moveTo(m.x(xt - w1), m.y(top)); lineTo(m.x(xt + w1), m.y(top)); lineTo(m.x(xb + w2), m.y(yEnd)); lineTo(m.x(xb - w2), m.y(yEnd)); close()
        }
        d.drawPath(path, Brush.verticalGradient(listOf(Color(color).copy(alpha = a), Color.Transparent), m.y(top), m.y(yEnd)))
    }
}

private val FISH: Path by lazy { parse("M0.5,0 C0.3,-0.3 -0.1,-0.3 -0.32,-0.04 L-0.32,0.04 C-0.1,0.3 0.3,0.3 0.5,0Z") }

/** Schools of fish swimming across between y0 and y1, every fish wagging its tail. */
internal fun fish(schools: Int, y0: Float, y1: Float, size: Float, colors: LongArray, seed: Int, speed: Float = 46f, perSchool: Int = 7) =
    Motion { d, t, m ->
        val span = ART_W + 700f
        val tail = Path()
        for (sc in 0 until schools) {
            val p = frac(hash(seed, sc + 10) + t * speed * (0.7f + 0.6f * hash(seed, sc)) / span)
            val leftward = sc % 2 == 1
            val cx = if (leftward) ART_W + 350f - p * span else -350f + p * span
            val cy = y0 + hash(seed, sc + 20) * (y1 - y0) + sin(t * 0.35f + sc * 2f) * 30f
            val color = Color(colors[sc % colors.size])
            for (j in 0 until perSchool) {
                val fx = cx + (hash(seed, sc * 40 + j) - 0.5f) * 170f + sin(t * 0.9f + j) * 6f
                val fy = cy + (hash(seed, sc * 40 + j + 20) - 0.5f) * 80f + sin(t * 1.3f + j * 1.7f) * 5f
                val s = m.d(size * (0.75f + 0.5f * hash(seed, sc * 40 + j + 30)))
                val wag = sin(t * 9f + j * 1.3f + sc)
                d.withTransform({ translate(m.x(fx), m.y(fy)); scale(if (leftward) -s else s, s, Offset.Zero) }) {
                    tail.reset()
                    tail.moveTo(-0.3f, 0f)
                    tail.lineTo(-0.58f, -0.2f + 0.08f * wag)
                    tail.lineTo(-0.52f, 0f)
                    tail.lineTo(-0.58f, 0.2f + 0.08f * wag)
                    tail.close()
                    drawPath(tail, color)
                    drawPath(FISH, color)
                    drawOval(Color.White, Offset(-0.1f, -0.02f), Size(0.42f, 0.1f), alpha = 0.25f)
                    drawCircle(Color.White, 0.06f, Offset(0.3f, -0.05f))
                    drawCircle(Color(0xFF1A1A1A), 0.035f, Offset(0.31f, -0.05f))
                }
            }
        }
    }

/** A sea turtle gliding across now and then between y0 and y1, flippers rowing. */
internal fun turtle(y0: Float, y1: Float, seed: Int, speed: Float = 34f) = Motion { d, t, m ->
    val span = ART_W + 600f
    val u = hash(seed, 1) + t * speed / span
    val lap = floor(u).toInt()
    val p = u - lap
    val leftward = lap % 2 == 0
    val x = if (leftward) ART_W + 300f - p * span else -300f + p * span
    val y = y0 + hash(seed, lap + 5) * (y1 - y0) + sin(t * 0.5f) * 20f
    val s = m.d(1f)
    val row = sin(t * 1.6f)
    d.withTransform({
        translate(m.x(x), m.y(y))
        scale(if (leftward) -s else s, s, Offset.Zero)
        rotate(sin(t * 0.5f) * 4f, Offset.Zero)
    }) {
        drawOval(Color(0xFF6E8F4E), Offset(-58f, 8f + 4f * row), Size(26f, 12f))
        drawOval(Color(0xFF6E8F4E), Offset(-58f, -20f - 4f * row), Size(26f, 12f))
        rotate(30f + 25f * row, Offset(14f, 14f)) { drawOval(Color(0xFF7FA05A), Offset(4f, 10f), Size(58f, 18f)) }
        rotate(-30f - 25f * row, Offset(14f, -14f)) { drawOval(Color(0xFF7FA05A), Offset(4f, -28f), Size(58f, 18f)) }
        drawOval(Color(0xFF8AAE62), Offset(36f, -11f), Size(32f, 22f))
        drawCircle(Color(0xFF1A1A1A), 2.6f, Offset(58f, -4f))
        drawOval(Color(0xFF4E6232), Offset(-50f, -32f), Size(96f, 64f))
        drawOval(Color(0xFF7A8E4A), Offset(-45f, -27f), Size(86f, 54f))
        for ((px, py) in arrayOf(-20f to -11f, 4f to -12f, -20f to 11f, 4f to 12f, -8f to 0f, 20f to 0f, -34f to 0f)) {
            drawCircle(Color(0xFF9AAE5A), 8f, Offset(px, py), alpha = 0.85f)
        }
    }
}

/** Jellyfish drifting up between y0 and y1, bells pulsing and tentacles trailing. */
internal fun jellyfish(n: Int, y0: Float, y1: Float, seed: Int) = Motion { d, t, m ->
    val span = y1 - y0 + 300f
    val path = Path()
    for (i in 0 until n) {
        val p = frac(hash(seed, i) + t * (12f + 8f * hash(seed, i + 5)) / span)
        val y = y1 + 150f - p * span
        val x = 80f + hash(seed, i + 9) * (ART_W - 160f) + sin(t * 0.3f + i * 2f) * 30f
        val pulse = 0.5f + 0.5f * sin(t * 2.4f + i * 1.7f)
        val w = 54f * (0.85f + 0.3f * hash(seed, i + 13)) * (0.9f + 0.15f * pulse)
        val h = w * (0.62f - 0.12f * pulse)
        val c = Color(if (i % 2 == 0) 0xFFF7A8D8 else 0xFFC8A8FF)
        val a = 0.75f * min(1f, min(p * 6f, (1f - p) * 6f))
        for (k in 0 until 5) {
            val tx = x - w * 0.36f + k * w * 0.18f
            path.reset()
            path.moveTo(m.x(tx), m.y(y))
            for (s in 1..6) path.lineTo(m.x(tx + sin(t * 2f + k + s * 0.9f) * 6f), m.y(y + s * 14f * (0.9f + 0.2f * pulse)))
            d.drawPath(path, c, alpha = a * 0.6f, style = Stroke(m.d(2f), cap = StrokeCap.Round))
        }
        d.glow(m.at(x, y - h * 0.4f), m.d(w), c, 0.25f * a)
        val bell = Path().apply {
            moveTo(m.x(x - w / 2f), m.y(y))
            cubicTo(m.x(x - w / 2f), m.y(y - h * 1.3f), m.x(x + w / 2f), m.y(y - h * 1.3f), m.x(x + w / 2f), m.y(y))
            quadraticTo(m.x(x), m.y(y - h * 0.2f), m.x(x - w / 2f), m.y(y))
            close()
        }
        d.drawPath(bell, c, alpha = a * 0.8f)
        d.drawOval(Color.White, Offset(m.x(x - w * 0.25f), m.y(y - h * 0.85f)), Size(m.d(w * 0.2f), m.d(h * 0.3f)), alpha = a * 0.5f)
    }
}

/** Bubbles rising from each source (x, y) up the page, wobbling and growing. */
internal fun bubbles(points: FloatArray, seed: Int, perSource: Int = 7) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        val sx = points.px(k)
        val sy = points.py(k)
        val rise = sy - m.top + 100f
        for (j in 0 until perSource) {
            val p = frac(hash(seed, k * 31 + j) + t * (70f + 50f * hash(seed, k * 17 + j)) / rise)
            val y = sy - p * rise
            val x = sx + sin(t * 2.2f + j * 1.7f + k) * (6f + 18f * p) + (hash(seed, k * 7 + j) - 0.5f) * 30f
            val r = m.d(4f + 7f * hash(seed, k * 11 + j) + 4f * p)
            val a = 0.75f * (1f - p * 0.6f) * min(1f, p * 12f)
            d.drawCircle(Color.White, r, m.at(x, y), alpha = a, style = Stroke(m.d(1.6f)))
            d.drawCircle(Color.White, r * 0.28f, Offset(m.x(x) - r * 0.35f, m.y(y) - r * 0.35f), alpha = a)
        }
    }
}

/** Kelp swaying from each base (x, y, height): a stem with leaves, bending more toward the tip. */
internal fun kelp(points: FloatArray, seed: Int) = Motion { d, t, m ->
    val stem = Path()
    for (k in 0 until points.count) {
        val x0 = points.px(k)
        val y0 = points.py(k)
        val h = max(points.pv(k), 60f)
        val ph = hash(seed, k) * TAU
        val color = Color(if (k % 2 == 0) 0xFF2F7A3A else 0xFF4F8A2A)
        val leaf = Color(if (k % 2 == 0) 0xFF3F9A4A else 0xFF6AA23A)
        stem.reset()
        for (s in 0..14) {
            val u = s / 14f
            val x = x0 + sin(t * 0.9f + ph + u * 2.4f) * 26f * u.pow(1.3f) * (h / 300f)
            val y = y0 - u * h
            if (s == 0) stem.moveTo(m.x(x), m.y(y)) else stem.lineTo(m.x(x), m.y(y))
            if (s in 2..13 && s % 2 == 0) {
                val side = if ((s / 2) % 2 == 0) 1f else -1f
                val ang = side * (55f + 15f * sin(t * 1.3f + s + ph))
                val lw = m.d(30f * (1f - u * 0.4f))
                val lh = m.d(11f)
                d.withTransform({ translate(m.x(x), m.y(y)); rotate(-90f + ang, Offset.Zero) }) {
                    drawOval(leaf, Offset(0f, -lh / 2f), Size(lw, lh))
                }
            }
        }
        d.drawPath(stem, color, style = Stroke(m.d(5f), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

// ---------- fire ----------

/** Lava thrown up from the crater (x, y, mouth width) in glowing arcs that fall back and cool. */
internal fun lavaFountain(points: FloatArray, seed: Int, n: Int = 46) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val cx = points.px(0)
    val cy = points.py(0)
    val mouth = max(points.pv(0), 30f)
    val g = 520f
    for (i in 0 until n) {
        val hgt = 90f + 260f * hash(seed, i).pow(1.5f)
        val vy = sqrt(2f * g * hgt)
        val life = vy / g + sqrt(2f * (hgt + 120f) / g)
        val p = frac(hash(seed, i + 100) + t / life)
        val tt = p * life
        val x = cx + (hash(seed, i + 300) - 0.5f) * mouth * 0.6f + (hash(seed, i + 200) - 0.5f) * 260f * tt
        val y = cy - vy * tt + 0.5f * g * tt * tt
        val heat = 1f - p
        val color = when {
            heat > 0.7f -> Color(0xFFFFF1B0)
            heat > 0.4f -> Color(0xFFFFB02E)
            else -> Color(0xFFE0461F)
        }
        val r = m.d(2.2f + 3.2f * hash(seed, i + 400))
        d.glow(m.at(x, y), r * 4f, Color(0xFFFF8A2A), 0.45f * heat)
        d.drawCircle(color, r, m.at(x, y), alpha = min(1f, 0.4f + heat))
    }
    val f = 0.8f + 0.2f * sin(t * 7f) * sin(t * 3.1f)
    d.glow(m.at(cx, cy), m.d(mouth * 1.4f), Color(0xFFFFB347), 0.6f * f)
}

/** Glowing embers drifting up the page from around (x, y), spreading out and fading as they climb. */
internal fun embers(points: FloatArray, seed: Int, n: Int = 40) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val sx = points.px(0)
    val sy = points.py(0)
    val span = max(200f, sy - m.top)
    for (i in 0 until n) {
        val p = frac(hash(seed, i + 50) + t * (40f + 50f * hash(seed, i)) / span)
        val y = sy - p * span
        val x = sx + (hash(seed, i + 100) - 0.5f) * (80f + 700f * p) + sin(t * 1.1f + i) * 20f * p
        val a = (1f - p) * min(1f, p * 10f) * (0.6f + 0.4f * sin(t * 6f + i))
        val r = m.d(1.6f + 2f * hash(seed, i + 150))
        d.glow(m.at(x, y), r * 4f, Color(0xFFFF7A2A), 0.5f * a)
        d.drawCircle(Color(0xFFFFC46A), r, m.at(x, y), alpha = max(0f, a))
    }
}

/** Brighter pulses of lava creeping down a channel (anchors in order from the top: x, y, width). */
internal fun lavaFlow(points: FloatArray, seed: Int, speed: Float = 28f) = Motion { d, t, m ->
    val n = points.count
    if (n < 2) return@Motion
    val lens = FloatArray(n)
    for (i in 1 until n) lens[i] = lens[i - 1] + hypot(points.px(i) - points.px(i - 1), points.py(i) - points.py(i - 1))
    val total = lens[n - 1]
    if (total <= 0f) return@Motion
    val pulses = max(2, (total / 70f).toInt())
    for (k in 0 until pulses) {
        val dist = frac(k / pulses.toFloat() + t * speed / total + hash(seed, k) * 0.1f) * total
        var s = 0
        while (s < n - 2 && lens[s + 1] < dist) s++
        val f = ((dist - lens[s]) / max(0.001f, lens[s + 1] - lens[s])).coerceIn(0f, 1f)
        val w = max(lerp(points.pv(s), points.pv(s + 1), f), 6f)
        d.glow(m.at(lerp(points.px(s), points.px(s + 1), f), lerp(points.py(s), points.py(s + 1), f)), m.d(w * 1.3f), Color(0xFFFFE08A),
            0.55f + 0.25f * sin(t * 3f + k))
    }
}

/** Ash billowing up from the vent (x, y, size): dark puffs swelling as they rise, lit from below near the bottom. */
internal fun plume(points: FloatArray, n: Int = 9) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val bx = points.px(0)
    val by = points.py(0)
    val size = max(points.pv(0), 40f)
    for (k in 0 until n) {
        val p = frac(t / 9f + k / n.toFloat())
        val y = by - p * size * 4.5f
        val x = bx + sin(k * 2.3f + t * 0.2f) * size * 0.3f * p + p * size * 0.6f
        val r = size * (0.35f + 0.9f * p)
        d.glow(m.at(x, y), m.d(r), Color(0xFF2A1A1C), 0.5f * min(1f, p * 5f) * (1f - p))
        if (p < 0.35f) d.glow(m.at(x, y + r * 0.3f), m.d(r * 0.8f), Color(0xFFFF6A2A), 0.25f * (1f - p / 0.35f))
    }
}

/** Steam rising where lava meets the sea (x, y): soft white puffs swelling and drifting as they climb. */
internal fun steam(points: FloatArray, seed: Int, n: Int = 8) = Motion { d, t, m ->
    for (k in 0 until points.count) {
        for (j in 0 until n) {
            val p = frac(t / 4.5f + j / n.toFloat() + hash(seed, k) * 0.5f)
            val x = points.px(k) + sin(t * 0.6f + j * 1.7f + k) * 10f * p + p * 40f
            val y = points.py(k) - p * 260f
            d.glow(m.at(x, y), m.d(16f + 60f * p), Color(0xFFF2E6E0), 0.5f * min(1f, p * 6f) * (1f - p))
        }
    }
}

// ---------- space ----------

/** A rocket flying across the lower sky now and then, flame flickering, leaving a trail of smoke. */
internal fun rocket(seed: Int, period: Float = 17f) = Motion { d, t, m ->
    val u = t / period + hash(seed, 1)
    val lap = floor(u).toInt()
    val p = u - lap
    if (p > 0.5f) return@Motion
    val q = p / 0.5f
    val fromLeft = lap % 2 == 0
    val sx = if (fromLeft) -90f else ART_W + 90f
    val ex = if (fromLeft) ART_W + 120f else -120f
    val sy = PAGE_BAND + 420f
    val ey = PAGE_BAND + 70f
    fun pos(v: Float) = Offset(lerp(sx, ex, v), lerp(sy, ey, v) - sin(v * PI.toFloat()) * 120f)
    for (j in 1..14) {
        val v = q - j * 0.018f
        if (v < 0f) break
        val c = pos(v)
        d.drawCircle(Color.White, m.d(8f + j * 3f), m.at(c.x, c.y), alpha = 0.3f * (1f - j / 15f))
    }
    val c = pos(q)
    val ahead = pos(min(1f, q + 0.01f))
    val ang = atan2(ahead.y - c.y, ahead.x - c.x) * 180f / PI.toFloat()
    d.withTransform({ translate(m.x(c.x), m.y(c.y)); rotate(ang + 90f, Offset.Zero); scale(m.d(1.4f), m.d(1.4f), Offset.Zero) }) {
        val f = 0.8f + 0.2f * sin(t * 40f)
        glow(Offset(0f, 34f), 26f * f, Color(0xFFFFA43A), 0.8f)
        drawOval(Color(0xFFFFE08A), Offset(-6f, 22f), Size(12f, 26f * f))
        drawPath(Path().apply { moveTo(0f, -40f); cubicTo(14f, -26f, 14f, 6f, 10f, 24f); lineTo(-10f, 24f); cubicTo(-14f, 6f, -14f, -26f, 0f, -40f); close() },
            Color(0xFFF4F5F8))
        drawPath(Path().apply { moveTo(0f, -40f); cubicTo(8f, -32f, 10f, -24f, 11f, -20f); lineTo(-11f, -20f); cubicTo(-10f, -24f, -8f, -32f, 0f, -40f); close() },
            Color(0xFFE63946))
        drawPath(Path().apply { moveTo(-10f, 8f); lineTo(-22f, 28f); lineTo(-9f, 24f); close() }, Color(0xFFE63946))
        drawPath(Path().apply { moveTo(10f, 8f); lineTo(22f, 28f); lineTo(9f, 24f); close() }, Color(0xFFE63946))
        drawCircle(Color(0xFF3A6EA5), 6f, Offset(0f, -8f))
        drawCircle(Color(0xFFBFE3FF), 4f, Offset(0f, -8f))
    }
}

/** An astronaut floating at (x, y, size), drifting and turning slowly, one arm waving. */
internal fun astronaut(points: FloatArray) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val x = points.px(0) + sin(t * 0.4f) * 22f
    val y = points.py(0) + sin(t * 0.6f) * 14f
    val k = max(points.pv(0), 40f) / 90f
    val wave = sin(t * 2.2f)
    d.withTransform({ translate(m.x(x), m.y(y)); rotate(sin(t * 0.25f) * 25f, Offset.Zero); scale(m.d(k), m.d(k), Offset.Zero) }) {
        val suit = Color(0xFFF4F5F8)
        val shade = Color(0xFFC9CDD8)
        drawRoundRect(shade, Offset(-20f, -22f), Size(40f, 40f), CornerRadius(8f))
        rotate(-35f + 10f * wave, Offset(-14f, -10f)) { drawRoundRect(suit, Offset(-20f, -12f), Size(12f, 34f), CornerRadius(6f)) }
        rotate(-140f + 30f * wave, Offset(14f, -10f)) { drawRoundRect(suit, Offset(8f, -12f), Size(12f, 34f), CornerRadius(6f)) }
        drawRoundRect(suit, Offset(-13f, 12f), Size(11f, 30f), CornerRadius(5f))
        drawRoundRect(suit, Offset(2f, 12f), Size(11f, 30f), CornerRadius(5f))
        drawRoundRect(shade, Offset(-13f, 36f), Size(11f, 7f), CornerRadius(3f))
        drawRoundRect(shade, Offset(2f, 36f), Size(11f, 7f), CornerRadius(3f))
        drawRoundRect(suit, Offset(-16f, -18f), Size(32f, 36f), CornerRadius(10f))
        drawRect(Color(0xFF3A6EA5), Offset(-7f, -4f), Size(14f, 9f))
        drawCircle(Color(0xFFE63946), 2f, Offset(-3f, 0f))
        drawCircle(suit, 17f, Offset(0f, -30f))
        drawOval(Brush.linearGradient(listOf(Color(0xFF1B2A4A), Color(0xFF3E5C8A), Color(0xFFD9B44A)), Offset(-11f, -40f), Offset(11f, -20f)),
            Offset(-12f, -40f), Size(24f, 18f))
        drawOval(Color.White, Offset(-8f, -37f), Size(6f, 4f), alpha = 0.7f)
    }
}

/** Satellites drifting across between y0 and y1, solar panels glinting, a light blinking. */
internal fun satellites(n: Int, y0: Float, y1: Float, seed: Int) = Motion { d, t, m ->
    val span = ART_W + 300f
    for (i in 0 until n) {
        val p = frac(hash(seed, i) + t * (18f + 10f * hash(seed, i + 5)) / span)
        val x = if (i % 2 == 1) ART_W + 150f - p * span else -150f + p * span
        val y = y0 + hash(seed, i + 9) * (y1 - y0)
        val s = m.d(0.9f + 0.4f * hash(seed, i + 13))
        d.withTransform({ translate(m.x(x), m.y(y)); rotate(15f * sin(t * 0.2f + i) - 10f, Offset.Zero); scale(s, s, Offset.Zero) }) {
            for (px in floatArrayOf(-44f, 14f)) {
                drawRect(Color(0xFF2D4F8A), Offset(px, -7f), Size(30f, 14f))
                for (g in 1..2) drawLine(Color(0xFF7FA8E0), Offset(px + g * 10f, -7f), Offset(px + g * 10f, 7f), 0.8f)
            }
            drawLine(Color(0xFFB0B4BC), Offset(-14f, 0f), Offset(14f, 0f), 1.6f)
            drawRoundRect(Color(0xFFD9B44A), Offset(-9f, -9f), Size(18f, 18f), CornerRadius(2f))
            drawLine(Color(0xFFB0B4BC), Offset(0f, -9f), Offset(0f, -18f), 1.2f)
            val on = frac(t / 1.4f + i * 0.3f) < 0.2f
            glow(Offset(0f, -19f), if (on) 9f else 4f, Color(0xFFFF4D4D), if (on) 0.95f else 0.3f)
            val glint = max(0f, sin(t * 0.7f + i * 2f)).pow(12)
            if (glint > 0.05f) glow(Offset(-29f, 0f), 30f, Color.White, 0.7f * glint)
        }
    }
}

// ---------- synthwave ----------

/** The synthwave floor's cross lines rolling toward you, from the horizon anchor (vanishing x, horizon y) down. */
internal fun gridScroll(points: FloatArray, color: Long, speed: Float = 0.6f) = Motion { d, t, m ->
    if (points.count < 1) return@Motion
    val hy = points.py(0)
    val depth = ART_H - hy
    val shift = frac(t * speed)
    for (i in 0 until 28) {
        val z = 1f + (i - shift) * 0.55f
        if (z < 1f) continue
        val y = hy + depth / z
        if (y < hy + 2f) continue
        val near = 1f / z
        val a = 0.25f + 0.75f * near
        d.drawLine(Color(color), m.at(0f, y), m.at(ART_W, y), m.d(1.5f + 4f * near), alpha = 0.3f * a)
        d.drawLine(Color(color), m.at(0f, y), m.at(ART_W, y), m.d(0.8f + 1.6f * near), alpha = 0.9f * a)
    }
}

/**
 * The retro sun's stripes sliding down: bars of the sky's color (the two "sky" anchors are its gradient: (0, y,
 * color) at the top and at the horizon) across the lower part of the sun (x, y, r), thicker toward the horizon.
 */
internal fun sunStripes(sun: FloatArray, sky: FloatArray, horizon: Float, speed: Float = 0.05f) = Motion { d, t, m ->
    if (sun.count < 1 || sky.count < 2) return@Motion
    val cx = sun.px(0)
    val cy = sun.py(0)
    val r = sun.pv(0)
    val y0 = cy - r * 0.05f
    val y1 = min(horizon, cy + r)
    if (y1 <= y0) return@Motion
    val brush = Brush.verticalGradient(listOf(rgb(sky.pv(0)), rgb(sky.pv(1))), m.y(sky.py(0)), m.y(sky.py(1)))
    val disc = Path().apply { addOval(androidx.compose.ui.geometry.Rect(m.x(cx - r), m.y(cy - r), m.x(cx + r), m.y(cy + r))) }
    d.clipPath(disc) {
        val n = 7
        for (j in 0 until n) {
            val u = frac(j / n.toFloat() + t * speed)
            val y = y0 + (y1 - y0) * u
            val h = 2f + 16f * u
            drawRect(brush, Offset(m.x(cx - r - 2f), m.y(y)), Size(m.d(2f * r + 4f), m.d(h)))
        }
    }
}
