package app.passstudio.ui

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import app.passstudio.R
import app.passstudio.data.Catalog
import app.passstudio.data.LogoStyle
import app.passstudio.data.Logos
import app.passstudio.data.Pass
import app.passstudio.data.PassTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ---------- The app's own look ----------

private val LightColors = lightColorScheme(
    primary = Color(0xFF3B2F7A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE6E0FF),
    onPrimaryContainer = Color(0xFF221B3F),
    secondary = Color(0xFFD9692F),
    onSecondary = Color.White,
    background = Color(0xFFF6F4EF),
    onBackground = Color(0xFF1C1B22),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1B22),
    surfaceVariant = Color(0xFFEDEAE3),
    onSurfaceVariant = Color(0xFF5F5B68),
    outline = Color(0xFFCFCAC0),
    outlineVariant = Color(0xFFE2DED6),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCBC2FF),
    onPrimary = Color(0xFF221B3F),
    primaryContainer = Color(0xFF3B2F7A),
    onPrimaryContainer = Color(0xFFE6E0FF),
    secondary = Color(0xFFF2A065),
    onSecondary = Color(0xFF2B1608),
    background = Color(0xFF121216),
    onBackground = Color(0xFFE8E6EE),
    surface = Color(0xFF1C1C22),
    onSurface = Color(0xFFE8E6EE),
    surfaceVariant = Color(0xFF27272F),
    onSurfaceVariant = Color(0xFFBAB6C6),
    outline = Color(0xFF45444F),
    outlineVariant = Color(0xFF33323B),
)

@Composable
fun PassStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}

// ---------- Colors ----------

fun parseHex(hex: String): Color? = runCatching { Color(android.graphics.Color.parseColor(hex.trim())) }.getOrNull()

/** White or near-black text, whichever reads better on [background]. */
fun inkOn(background: Color): Color = if (background.luminance() > 0.45f) Color(0xFF1A1A1A) else Color.White

fun isHexColor(s: String) = Regex("^#[0-9a-fA-F]{6}$").matches(s.trim())

// ---------- Pieces used on several screens ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 18.dp, bottom = 8.dp, start = 4.dp),
    )
}

/** A white (or dark) rounded block that holds a group of settings. */
@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
fun SwitchRow(title: String, subtitle: String? = null, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** A theme's art on its own color, like a tiny pass. */
@Composable
fun ThemeArt(theme: PassTheme, modifier: Modifier = Modifier, selected: Boolean = false, corner: Int = 16) {
    val bg = parseHex(theme.color) ?: Color(0xFF1A1A1A)
    val shape = RoundedCornerShape(corner.dp)
    Box(
        modifier
            .clip(shape)
            .background(bg)
            .then(if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, shape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Catalog.artRes(theme.art)),
            contentDescription = theme.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().aspectRatio(1032f / 812f),
        )
        if (selected) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(8.dp).size(26.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Picked", tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** A theme tile with its name underneath, for grids and rows of themes. */
@Composable
fun ThemeTile(theme: PassTheme, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(4.dp)) {
        ThemeArt(theme, Modifier.fillMaxWidth(), selected)
        Text(
            theme.name,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
        )
    }
}

// ---------- Logos ----------

private val imageCache = LruCache<String, ImageBitmap>(24)

/** A pass's logo (by the name it's kept under on the phone), or null while it loads or if there's none. */
@Composable
fun rememberLogo(name: String): ImageBitmap? {
    val context = LocalContext.current
    val key = name.trim()
    val cached: ImageBitmap? = if (key.isEmpty()) null else imageCache.get(key)
    val image by produceState(cached, key) {
        // A new logo starts from its own picture (or none), not the one the last logo showed.
        value = cached
        if (value == null && key.isNotEmpty()) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    val file = Logos.file(context, key)
                    if (file.exists()) BitmapFactory.decodeFile(file.path)?.asImageBitmap() else null
                }.getOrNull()
            }
            if (loaded != null) imageCache.put(key, loaded)
            value = loaded
        }
    }
    return image
}

/**
 * How big a pass's logo shows on a pass page [pageWidth] across: its circle's size, and the box a big logo fits in.
 * The usual sizes are for a phone 411 wide ([scale] shrinks them for a smaller picture of the page), times the pass's
 * own logo size; a big logo never goes wider than the page.
 */
fun passLogoSize(pass: Pass, pageWidth: Dp, scale: Float = 1f): Pair<Dp, DpSize> {
    val s = pass.logoSize.coerceIn(0.5f, 1.5f)
    val width = minOf((pageWidth - 40.dp * scale) * s, pageWidth - 24.dp * scale)
    return (104.dp * s * scale) to DpSize(width, 140.dp * s * scale)
}

/**
 * The logo at the top of a pass, the way the pass shows it: in its white circle ([circle] across), or big on its own,
 * as large as fits in [big]. A logo kept before there were big versions shows in its circle.
 */
@Composable
fun PassLogo(pass: Pass, circle: Dp, big: DpSize, ink: Color, letterSize: TextUnit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // The big picture's name and its width over height, if this pass shows its logo big and the picture is there.
    val bigLogo = remember(pass.logo, pass.logoStyle, pass.logoOutline) {
        if (pass.logoStyle != LogoStyle.BIG || !Logos.hasBig(context, pass.logo)) null
        else Logos.bigName(pass.logo, pass.logoOutline).let { name ->
            Logos.size(context, name)?.let { (w, h) -> Triple(name, w.toFloat() / h, Logos.bodyBottom(context, name)) }
        }
    }
    if (bigLogo == null) {
        RoundLogo(pass, circle, ink, letterSize, modifier)
        return
    }
    val (name, ratio, bottom) = bigLogo
    val width = if (ratio >= big.width / big.height) big.width else big.height * ratio
    val height = width / ratio
    val image = rememberLogo(name)
    // It takes up room only down to the logo itself; its shadow hangs below, so the text under it sits close.
    Box(modifier.size(width, height * bottom)) {
        if (image != null) {
            Image(image, contentDescription = "Logo", contentScale = ContentScale.Fit,
                modifier = Modifier.wrapContentHeight(Alignment.Top, unbounded = true).size(width, height))
        }
    }
}

// ---------- The card name ----------

/** Colors to pick from for the card name: the usual (white or dark to suit the theme), shiny gold and silver, and plain ones. */
val CARD_NAME_COLORS = listOf("", "gold", "silver", "#E31837", "#1F6FD0", "#FFFFFF", "#1A1A1A")

/** What each card name color is called, for the color picker. */
fun cardNameColorName(choice: String): String = when (choice) {
    "" -> "Usual"
    "gold" -> "Gold"
    "silver" -> "Silver"
    "#E31837" -> "Red"
    "#1F6FD0" -> "Blue"
    "#FFFFFF" -> "White"
    "#1A1A1A" -> "Black"
    else -> choice
}

/**
 * A membership card's lettering, like the "EXECUTIVE MEMBER" on Costco's card: Copperplate CC, a free revival of
 * Copperplate Gothic by indestructible type* (SIL Open Font License; the license is in the app's assets).
 */
val ExecutiveFont = FontFamily(Font(R.font.copperplate_cc_bold, FontWeight.Bold))

/** Whether the card name shows in a membership card's lettering: as picked, or by itself when it's gold or silver. */
fun Pass.executiveName(): Boolean = when (titleFont) {
    "executive" -> true
    "plain" -> false
    else -> metalOf(titleColor) != null
}

/**
 * [text] in small capitals, the way a membership card letters it: capitals stay full size and the other letters become
 * smaller capitals, so "Executive Member" shows like the card's "EXECUTIVE MEMBER". Spaces are narrower, like the card's.
 */
fun smallCaps(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        if (text[i] == ' ') {
            withStyle(SpanStyle(fontSize = 0.6.em)) { append(' ') }
            i++
            continue
        }
        val lower = text[i].isLowerCase()
        var j = i + 1
        while (j < text.length && text[j] != ' ' && text[j].isLowerCase() == lower) j++
        val run = text.substring(i, j)
        if (lower) withStyle(SpanStyle(fontSize = 0.83.em)) { append(run.uppercase()) } else append(run)
        i = j
    }
}

/** Polished metal down the letters, and the deeper metal of their outlines. */
private class Metal(val face: List<Color>, val edge: List<Color>)

private val GOLD = Metal(
    listOf(Color(0xFFFFF7D6), Color(0xFFFAEA99), Color(0xFFE6C677), Color(0xFFF4DE96), Color(0xFFFFF1C4)),
    listOf(Color(0xFFC9A04A), Color(0xFF9C7428), Color(0xFFC29A48)),
)
private val SILVER = Metal(
    listOf(Color(0xFFFFFFFF), Color(0xFFEDF0F3), Color(0xFFC3CAD2), Color(0xFFE2E6EA), Color(0xFFF9FAFB)),
    listOf(Color(0xFFA2ABB5), Color(0xFF6F7883), Color(0xFFA0A9B3)),
)

private fun metalOf(choice: String): Metal? = when (choice) {
    "gold" -> GOLD
    "silver" -> SILVER
    else -> null
}

/** A swatch's paint for a card name color. */
fun cardNameSwatch(choice: String): Brush = metalOf(choice)?.let { Brush.verticalGradient(it.face) }
    ?: parseHex(choice)?.let { SolidColor(it) }
    ?: Brush.horizontalGradient(0f to Color.White, 0.5f to Color.White, 0.5f to Color(0xFF1A1A1A), 1f to Color(0xFF1A1A1A))

/** Whether a card name color is a shiny one (gold or silver). */
fun isShiny(choice: String) = metalOf(choice) != null

/** The metal lines along a card name band: gold, or silver for silver lettering. */
private val GOLD_RULE = listOf(Color(0xFF9C7428), Color(0xFFF4DE96), Color(0xFFFFF7D6), Color(0xFFF4DE96), Color(0xFF9C7428))
private val SILVER_RULE = listOf(Color(0xFF6F7883), Color(0xFFDCE2E8), Color(0xFFFFFFFF), Color(0xFFDCE2E8), Color(0xFF6F7883))

/**
 * A black band across the pass with a thin metal line along each edge, like the Executive card, for the card name to sit
 * on so it stands out on any theme.
 */
@Composable
fun NameBand(color: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val rule = Brush.horizontalGradient(if (color == "silver") SILVER_RULE else GOLD_RULE)
    Column(
        modifier.fillMaxWidth().shadow(6.dp, RectangleShape).background(Color(0xFF0B0B0D)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(rule))
        content()
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(rule))
    }
}

/** How long the shine takes to glide across, and how long until the next one starts, at each speed (milliseconds). */
private fun shineTiming(speed: String): Pair<Int, Int> = when (speed) {
    "slow" -> 2600 to 3200
    "fast" -> 1300 to 1600
    else -> 1900 to 2300
}

/** The light of a glint [at] (0 to 1) of the way across a line of text [size] big. */
private fun glint(at: Float, size: Size, alpha: Float): Brush {
    val band = size.height * 2.2f
    val x = -band + at * (size.width + band * 2f)
    val light = Color(0xFFFFFCEB)
    return Brush.linearGradient(
        0.3f to light.copy(alpha = 0f), 0.5f to light.copy(alpha = alpha), 0.7f to light.copy(alpha = 0f),
        start = Offset(x - band, 0f), end = Offset(x + band, size.height * 0.7f),
    )
}

/** How a card name line got laid out: on one line, made smaller if it didn't fit. */
private class NameLayout {
    var text: TextLayoutResult? = null
    var fontPx = 0f
}

/**
 * The card name line, in its color. In a membership card's lettering ([executive]) it's in small capitals on one line,
 * stretched to fill the width so it stands out (or made smaller if it doesn't fit). Gold and silver shine like polished
 * metal: while [moving], a shine glides steadily across the letters and their outlines, one after another, at
 * [shineSpeed] ("slow", "medium" or "fast").
 */
@Composable
fun CardName(
    text: String,
    color: String,
    executive: Boolean,
    ink: Color,
    style: TextStyle,
    moving: Boolean,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    shineSpeed: String = "medium",
) {
    val metal = metalOf(color)
    if (!executive && metal == null) {
        Text(text, color = parseHex(color) ?: ink, style = style, maxLines = maxLines, overflow = TextOverflow.Ellipsis,
            modifier = modifier)
        return
    }
    val size = if (style.fontSize.isSpecified) style.fontSize else 16.sp
    // Sized, spaced and widened to match the card's lettering.
    val look = if (executive) {
        style.copy(fontFamily = ExecutiveFont, fontWeight = FontWeight.Bold, fontSize = size * 1.25f, letterSpacing = 0.18.em,
            textGeometricTransform = TextGeometricTransform(scaleX = 1.1f), shadow = null)
    } else {
        style.copy(fontWeight = FontWeight.SemiBold, fontSize = size, letterSpacing = 0.03.em, shadow = null)
    }
    val label = remember(text, executive) { if (executive) smallCaps(text) else AnnotatedString(text) }
    // Where the shine is, from 0 to 1 across the line, gliding at a steady pace; below 0 in the moment before the next.
    val shine = if (metal != null && moving) {
        key(shineSpeed) {
            val (across, round) = shineTiming(shineSpeed)
            val clock = rememberInfiniteTransition(label = "shine")
                .animateFloat(0f, round.toFloat(), infiniteRepeatable(tween(round, easing = LinearEasing)), label = "shine")
            remember(clock) { derivedStateOf { if (clock.value < across) clock.value / across else -1f } }
        }
    } else {
        null
    }
    val measurer = rememberTextMeasurer()
    val laid = remember { NameLayout() }
    val shadow = style.shadow
    Spacer(
        modifier
            .semantics { contentDescription = text }
            .layout { measurable, constraints ->
                var fontSize = look.fontSize
                var result = measurer.measure(label, look, softWrap = false, maxLines = 1)
                if (constraints.hasBoundedWidth) {
                    // Too wide: smaller, to fit. A membership card's lettering also grows to stretch across, so it stands out.
                    val room = constraints.maxWidth.toFloat()
                    val width = result.size.width.toFloat()
                    val scale = when {
                        width > room -> room / width * 0.98f
                        executive && width > 0f -> (room * 0.95f / width).coerceIn(1f, 1.6f)
                        else -> 1f
                    }
                    if (scale != 1f) {
                        fontSize = look.fontSize * scale
                        result = measurer.measure(label, look.copy(fontSize = fontSize), softWrap = false, maxLines = 1)
                    }
                }
                laid.text = result
                laid.fontPx = fontSize.toPx()
                val placeable = measurable.measure(Constraints.fixed(result.size.width, result.size.height))
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .drawBehind {
                val result = laid.text ?: return@drawBehind
                // A fine outline: it gives the letters the weight of the card's printing, and catches the glint.
                val outline = Stroke(laid.fontPx * 0.05f, join = StrokeJoin.Round)
                if (metal == null) {
                    val c = parseHex(color) ?: ink
                    drawText(result, color = c, shadow = shadow)
                    drawText(result, color = c, drawStyle = outline)
                    return@drawBehind
                }
                // The metal's shading runs down the capitals, from their tops to the line they sit on.
                val bottom = result.firstBaseline
                val top = bottom - laid.fontPx * 0.7f
                val at = shine?.value ?: -1f
                drawText(result, brush = Brush.verticalGradient(metal.face, top, bottom),
                    shadow = Shadow(Color.Black.copy(alpha = 0.55f), Offset(0f, 1.5.dp.toPx()), 5.dp.toPx()))
                if (at >= 0f) drawText(result, brush = glint(at, this.size, 0.8f))
                drawText(result, brush = Brush.verticalGradient(metal.edge, top, bottom), drawStyle = outline)
                if (at >= 0f) drawText(result, brush = glint(at, this.size, 1f), drawStyle = outline)
            },
    )
}

/** A logo in its white circle, or the card name's first letter when there's no logo. */
@Composable
fun RoundLogo(pass: Pass, size: Dp, ink: Color, letterSize: TextUnit, modifier: Modifier = Modifier) {
    val image = rememberLogo(pass.logo)
    if (image != null) {
        Image(image, contentDescription = "Logo", contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape).background(Color.White))
    } else {
        Box(modifier.size(size).clip(CircleShape).background(ink.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Text(pass.title.trim().take(1).uppercase().ifBlank { "P" }, color = ink, fontSize = letterSize, fontWeight = FontWeight.SemiBold)
        }
    }
}
