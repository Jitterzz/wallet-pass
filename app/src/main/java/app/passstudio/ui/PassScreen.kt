package app.passstudio.ui

import android.animation.ValueAnimator
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.service.quicksettings.TileService
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.passstudio.PassTileService
import app.passstudio.QuickAccess
import app.passstudio.R
import app.passstudio.data.Catalog
import app.passstudio.data.LogoStyle
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
    // The barcode by itself on white, for the scanner (tap the barcode to open it).
    var scanning by remember { mutableStateOf(false) }
    val animatorsOn = remember { ValueAnimator.areAnimatorsEnabled() }
    val code = pass.barcodeValue.trim()

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
        if (!scanning) ThemeMotion(theme.art, Modifier.fillMaxSize())
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // The pass fills the first screen, over the theme's scene; its details are further down, when you scroll.
            val firstScreen = (maxHeight - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)
            val screenWidth = maxWidth
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(Modifier.fillMaxWidth().heightIn(min = firstScreen), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    val (circle, big) = passLogoSize(pass, screenWidth)
                    PassLogo(pass, circle, big, ink, 46.sp * pass.logoSize.coerceIn(0.5f, 1.5f))
                    // Close under a big logo, the way a membership card puts its "EXECUTIVE MEMBER" line, and on a black
                    // band like the card's if it has one.
                    val nameStyle = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, shadow = shadow)
                    val nameMoving = d.settings.motion && animatorsOn && !scanning
                    val bigLogo = pass.logoStyle == LogoStyle.BIG
                    if (pass.titleBand) {
                        Spacer(Modifier.height(if (bigLogo) 6.dp else 16.dp))
                        NameBand(pass.titleColor) {
                            CardName(
                                pass.title.ifBlank { "My pass" }, pass.titleColor, pass.executiveName(), Color.White,
                                nameStyle.copy(shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 2f), 12f)),
                                moving = nameMoving, shineSpeed = pass.shineSpeed,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 5.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier.height(if (bigLogo) 4.dp else 16.dp))
                        CardName(
                            pass.title.ifBlank { "My pass" }, pass.titleColor, pass.executiveName(), ink, nameStyle,
                            moving = nameMoving, shineSpeed = pass.shineSpeed, modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }
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
                        PageBarcode(pass) { scanning = true }
                        if (pass.showNumber) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                code,
                                color = ink,
                                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp, shadow = shadow),
                            )
                        }
                    }

                }

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
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
        AnimatedVisibility(scanning, enter = fadeIn(tween(160)), exit = fadeOut(tween(160))) {
            ScanView(pass) { scanning = false }
        }
    }

    if (pickingTheme) {
        PassThemeSheet(pass, onDismiss = { pickingTheme = false }, onSchedule = {
            pickingTheme = false
            onSchedule()
        })
    }
}

/** The barcode on white, drawn at the exact size it shows at so it scans cleanly. Tap it for the scan view. */
@Composable
private fun PageBarcode(pass: Pass, onOpen: () -> Unit) {
    val square = Barcodes.isSquare(pass.barcodeType)
    Box(
        Modifier.padding(horizontal = 28.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Color.White)
            .clickable(onClickLabel = "Show the barcode bigger for scanning", onClick = onOpen),
    ) {
        BarcodeImage(pass, Modifier.fillMaxWidth().padding(24.dp)) { maxWidth ->
            val h = if (square) minOf(maxWidth, 210.dp) else 118.dp
            if (square) h to h else maxWidth to h
        }
        // A hint that it opens bigger, tucked in the corner.
        Icon(
            painterResource(R.drawable.ic_fullscreen),
            contentDescription = null,
            tint = Color(0xFFA8ADB3),
            modifier = Modifier.align(Alignment.TopEnd).padding(9.dp).size(14.dp),
        )
    }
}

/**
 * The barcode drawn at exactly the size it shows at ([size] gives width and height for the room there is), so every
 * bar is a whole number of pixels wide and it scans cleanly.
 */
@Composable
private fun BarcodeImage(pass: Pass, modifier: Modifier, size: (maxWidth: Dp) -> Pair<Dp, Dp>) {
    val code = pass.barcodeValue.trim()
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val density = LocalDensity.current
        val (widthDp, heightDp) = size(maxWidth)
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

/**
 * For the scanner at the register: the barcode by itself, as big as it goes, on a plain white screen at full
 * brightness with the status and navigation bars out of the way. Tap anywhere (or go back) to return to the pass.
 */
@Composable
private fun ScanView(pass: Pass, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    BrightestScreen()
    val code = pass.barcodeValue.trim()
    val square = Barcodes.isSquare(pass.barcodeType)
    val dark = Color(0xFF202124)
    Column(
        Modifier.fillMaxSize().background(Color.White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Back to the pass",
                onClick = onClose,
            )
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            RoundLogo(pass, 46.dp, dark, 20.sp, Modifier.border(1.dp, Color(0xFFDADCE0), CircleShape))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(pass.title.ifBlank { "My pass" }, color = dark, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                if (pass.header.isNotBlank()) Text(pass.header, color = Color(0xFF5F6368), fontSize = 16.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        // Long barcodes go the full width (wider bars scan from farther away), square ones as big as fits.
        BarcodeImage(pass, Modifier.fillMaxWidth()) { maxWidth ->
            if (square) minOf(maxWidth, 400.dp).let { it to it } else maxWidth to minOf(maxWidth * 0.5f, 240.dp)
        }
        if (pass.showNumber && code.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                code,
                color = dark,
                style = TextStyle(fontSize = 27.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.5.sp, textAlign = TextAlign.Center),
            )
        }
        Spacer(Modifier.weight(1f))
        Text("Tap anywhere to go back", color = Color(0xFF80868B), fontSize = 15.sp)
    }
}

/**
 * The screen as bright as it goes (like the brightness slider all the way up) and kept on, with the status and
 * navigation bars hidden; a swipe in from the edge brings them back for a moment. The bars come back when it closes;
 * the pass page puts the brightness back when you leave it.
 */
@Composable
private fun BrightestScreen() {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val window = activity?.window ?: return@DisposableEffect onDispose { }
        window.attributes = window.attributes.apply { screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val bars = WindowCompat.getInsetsController(window, view)
        val oldStatus = bars.isAppearanceLightStatusBars
        val oldNav = bars.isAppearanceLightNavigationBars
        val oldBehavior = bars.systemBarsBehavior
        bars.isAppearanceLightStatusBars = true
        bars.isAppearanceLightNavigationBars = true
        bars.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        bars.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            bars.show(WindowInsetsCompat.Type.systemBars())
            bars.systemBarsBehavior = oldBehavior
            bars.isAppearanceLightStatusBars = oldStatus
            bars.isAppearanceLightNavigationBars = oldNav
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
