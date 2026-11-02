package app.passstudio.ui

import android.graphics.BitmapFactory
import androidx.compose.ui.platform.LocalContext
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.passstudio.data.Catalog
import app.passstudio.data.PassTheme
import app.passstudio.wallet.LogoUpload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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

/** White or near-black text, whichever reads better on [background] (Google Wallet picks the same way). */
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

// ---------- Images from a link (logos) ----------

private val imageCache = LruCache<String, ImageBitmap>(24)

/**
 * Loads a picture from an https link: from memory, else from the copy kept on the phone, else from the internet
 * (keeping a copy). Null while loading or if it can't be loaded.
 */
@Composable
fun rememberUrlImage(url: String): ImageBitmap? {
    val context = LocalContext.current
    val link = url.trim()
    val cached: ImageBitmap? = if (link.isEmpty()) null else imageCache.get(link)
    val image by produceState(cached, link) {
        // A new link starts from its own picture (or none), not the one the last link showed.
        value = cached
        if (value == null && link.startsWith("https://")) {
            val loaded = withContext(Dispatchers.IO) {
                val file = LogoUpload.cacheFile(context, link)
                val fromPhone = runCatching { if (file.exists()) BitmapFactory.decodeFile(file.path) else null }.getOrNull()
                fromPhone?.asImageBitmap() ?: runCatching {
                    val c = URL(link).openConnection() as HttpURLConnection
                    c.connectTimeout = 10_000
                    c.readTimeout = 15_000
                    try {
                        val bytes = c.inputStream.use { it.readBytes() }
                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        if (bmp != null) runCatching { file.writeBytes(bytes) }
                        bmp?.asImageBitmap()
                    } finally {
                        c.disconnect()
                    }
                }.getOrNull()
            }
            if (loaded != null) imageCache.put(link, loaded)
            value = loaded
        }
    }
    return image
}

/**
 * A round logo in its circle. One that fits the round spot fills it, as in Google Wallet; one that doesn't (an older
 * upload, or a wide picture) is shown whole inside a white circle instead of being cut off.
 */
@Composable
fun RoundLogo(image: ImageBitmap, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val fits = remember(image) { runCatching { LogoUpload.fits(image.asAndroidBitmap(), LogoUpload.Kind.ROUND) }.getOrDefault(true) }
    Box(modifier.size(size).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
        if (fits) {
            Image(image, contentDescription = "Logo", contentScale = ContentScale.Crop, modifier = Modifier.size(size))
        } else {
            Image(image, contentDescription = "Logo", contentScale = ContentScale.Fit, modifier = Modifier.size(size * 0.72f))
        }
    }
}
