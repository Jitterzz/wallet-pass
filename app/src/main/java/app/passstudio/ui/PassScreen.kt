package app.passstudio.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.service.quicksettings.TileService
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import app.passstudio.PassTileService
import app.passstudio.QuickAccess
import app.passstudio.R
import app.passstudio.data.Catalog
import app.passstudio.data.Pass
import app.passstudio.data.Store
import java.net.URLEncoder

/**
 * The pass: round logo, card name, big name and barcode, then its details, with the theme's art covering the whole
 * page. Made for showing at the register: the screen goes to full brightness and
 * stays on while it's open, and it works with no signal (the art is in the app and the logo is kept on the phone).
 */
@Composable
fun PassScreen(passId: String, onBack: () -> Unit, onEdit: () -> Unit, onSchedule: () -> Unit) {
    val d by Store.data.collectAsState()
    val pass = d.passes.firstOrNull { it.id == passId }
    if (pass == null) {
        // Deleted while open (from its editor): back to the list.
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val context = LocalContext.current
    val theme = Store.themeFor(pass, d)
    val sky = parseHex(Catalog.skyColor(theme.art)) ?: Color(0xFF1A1A1A)
    val ink = inkOn(sky)
    val darkArt = ink == Color.White
    val shadow = Shadow(
        color = if (darkArt) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
        offset = Offset(0f, 2f),
        blurRadius = 12f,
    )
    val snackbar = remember { SnackbarHostState() }
    var message by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(message) {
        val m = message
        if (m != null) {
            snackbar.showSnackbar(m)
            message = null
        }
    }
    ShowAtRegister(lightArt = !darkArt)

    val starred = d.settings.quickPassId == pass.id
    var pickingTheme by remember { mutableStateOf(false) }
    val code = pass.barcodeValue.trim()
    val screenHeight = LocalConfiguration.current.screenHeightDp

    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { message = "Couldn't open that link." }
    }

    Box(Modifier.fillMaxSize().background(sky)) {
        Image(
            painter = painterResource(Catalog.pageRes(theme.art)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize(),
        )
        ThemeMotion(theme.art, Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ink)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    Store.update { it.copy(settings = it.settings.copy(quickPassId = if (starred) "" else pass.id)) }
                    runCatching { TileService.requestListeningState(context, ComponentName(context, PassTileService::class.java)) }
                    message = if (starred) "Unstarred." else "Starred. The quick settings tile and opening the app now go to this pass."
                }) {
                    Icon(
                        painterResource(if (starred) R.drawable.ic_star else R.drawable.ic_star_outline),
                        contentDescription = if (starred) "Starred pass" else "Star this pass",
                        tint = ink,
                    )
                }
                IconButton(onClick = { pickingTheme = true }) {
                    Icon(painterResource(R.drawable.ic_palette), contentDescription = "Change theme", tint = ink)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit pass", tint = ink) }
            }

            // Sized so everything fits above the scene at the bottom of the page.
            Spacer(Modifier.height(8.dp))
            RoundLogo(pass, 104.dp, ink, 46.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                pass.title.ifBlank { "My pass" },
                color = ink,
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, shadow = shadow),
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                pass.header.ifBlank { pass.title.ifBlank { "My pass" } },
                color = ink,
                style = TextStyle(fontSize = 42.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                    shadow = shadow),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            if (pass.subheader.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    pass.subheader,
                    color = ink,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, shadow = shadow),
                )
            }
            if (code.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                PageBarcode(pass)
                if (pass.showNumber) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        code,
                        color = ink,
                        style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, shadow = shadow),
                    )
                }
            }

            // Room for the scene at the bottom of the page; the details come up when you scroll.
            Spacer(Modifier.height((screenHeight * 0.34f).dp))

            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val lines = buildList {
                    if (pass.showIdRow && code.isNotEmpty()) add(pass.idRowLabel.ifBlank { "Member ID" } to code)
                    pass.rows.filter { it.label.isNotBlank() && it.value.isNotBlank() }.forEach { add(it.label to it.value) }
                }
                lines.forEach { (label, value) ->
                    Glass(darkArt) {
                        Column(Modifier.padding(horizontal = 22.dp, vertical = 18.dp)) {
                            Text(label, color = ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                            Text(value, color = ink, fontSize = 17.sp)
                        }
                    }
                }

                // Nearby locations (Google Maps) first, then website links: (is it the map link, label, link).
                val links = buildList {
                    if (pass.nearby) {
                        val q = pass.nearbyQuery.trim().ifBlank { pass.title.trim() }
                        if (q.isNotEmpty()) add(Triple(true, "Nearby locations", "https://maps.google.com/?q=" + URLEncoder.encode(q, "UTF-8")))
                    }
                    pass.links.filter { it.url.trim().startsWith("http") }
                        .forEach { add(Triple(false, it.label.ifBlank { "Website" }, it.url.trim())) }
                }
                if (links.isNotEmpty()) {
                    Glass(darkArt) {
                        links.forEachIndexed { i, (isMap, label, url) ->
                            if (i > 0) Line(ink)
                            PageRow(
                                if (isMap) rememberVectorPainter(Icons.Filled.Place) else painterResource(R.drawable.ic_globe),
                                label, ink,
                            ) { open(url) }
                        }
                    }
                }

                Glass(darkArt) {
                    PageRow(painterResource(R.drawable.ic_palette), "Change theme", ink) { pickingTheme = true }
                    Line(ink)
                    PageRow(rememberVectorPainter(Icons.Filled.Edit), "Edit pass", ink, onEdit)
                }

                Glass(darkArt) {
                    PageRow(painterResource(R.drawable.ic_tile), "Add quick settings tile", ink) {
                        QuickAccess.requestTile(context) { message = it }
                    }
                    Line(ink)
                    PageRow(rememberVectorPainter(Icons.Filled.Home), "Add to home screen", ink) {
                        if (!QuickAccess.pinShortcut(context, pass)) message = "Your home screen doesn't take shortcuts."
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }

    if (pickingTheme) {
        PassThemeSheet(pass, onDismiss = { pickingTheme = false }, onSchedule = {
            pickingTheme = false
            onSchedule()
        })
    }
}

/** The barcode on white, drawn at the exact size it shows at so it scans cleanly. */
@Composable
private fun PageBarcode(pass: Pass) {
    val code = pass.barcodeValue.trim()
    val square = Barcodes.isSquare(pass.barcodeType)
    Box(
        Modifier.padding(horizontal = 28.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val density = LocalDensity.current
            val heightDp = if (square) minOf(maxWidth, 210.dp) else 118.dp
            val widthDp = if (square) heightDp else maxWidth
            val wPx = with(density) { widthDp.roundToPx() }
            val hPx = with(density) { heightDp.roundToPx() }
            val bitmap = remember(code, pass.barcodeType, wPx, hPx) { Barcodes.render(code, pass.barcodeType, wPx, hPx) }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Barcode",
                    contentScale = ContentScale.FillBounds,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(widthDp, heightDp),
                )
            } else {
                Text("This number doesn't fit a ${Barcodes.label(pass.barcodeType)} barcode", color = Color(0xFFB3261E), fontSize = 14.sp)
            }
        }
    }
}

/** A see-through panel for the details, dark over dark art and light over light art. */
@Composable
private fun Glass(darkArt: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp))
            .background(if (darkArt) Color.Black.copy(alpha = 0.44f) else Color.White.copy(alpha = 0.68f)),
        content = content,
    )
}

@Composable
private fun PageRow(icon: Painter, label: String, ink: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 22.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(18.dp))
        Text(label, color = ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun Line(ink: Color) {
    Box(Modifier.fillMaxWidth().padding(horizontal = 22.dp).height(1.dp).background(ink.copy(alpha = 0.14f)))
}

/**
 * While a pass is up: full brightness (so the barcode scans), the screen stays on, and the
 * status bar icons are light or dark to suit the art. All put back when the pass closes.
 */
@Composable
private fun ShowAtRegister(lightArt: Boolean) {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity, lightArt) {
        val window = activity?.window ?: return@DisposableEffect onDispose { }
        val oldBrightness = window.attributes.screenBrightness
        window.attributes = window.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val bars = WindowCompat.getInsetsController(window, view)
        val oldStatus = bars.isAppearanceLightStatusBars
        val oldNav = bars.isAppearanceLightNavigationBars
        bars.isAppearanceLightStatusBars = lightArt
        bars.isAppearanceLightNavigationBars = lightArt
        onDispose {
            window.attributes = window.attributes.apply { screenBrightness = oldBrightness }
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            bars.isAppearanceLightStatusBars = oldStatus
            bars.isAppearanceLightNavigationBars = oldNav
        }
    }
}

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
