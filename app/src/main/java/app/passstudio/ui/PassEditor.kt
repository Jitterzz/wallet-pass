package app.passstudio.ui

import android.animation.ValueAnimator
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.passstudio.R
import app.passstudio.data.Catalog
import app.passstudio.data.DetailRow
import app.passstudio.data.LogoException
import app.passstudio.data.LogoStyle
import app.passstudio.data.Logos
import app.passstudio.data.LookMode
import app.passstudio.data.Pass
import app.passstudio.data.PassLink
import app.passstudio.data.PassTheme
import app.passstudio.data.Seasons
import app.passstudio.data.Store
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassEditor(passId: String, onBack: () -> Unit, onSchedule: () -> Unit) {
    val d by Store.data.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val stored = d.passes.firstOrNull { it.id == passId }
    var draft by remember(passId) { mutableStateOf(stored ?: Pass(id = passId)) }
    var message by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var savingLogo by remember { mutableStateOf(false) }
    val animatorsOn = remember { ValueAnimator.areAnimatorsEnabled() }

    // Picking a logo: it's kept on the phone, fitted to the circle and cut out for showing big.
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            savingLogo = true
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw LogoException("Couldn't open that file.")
                        Logos.save(context.applicationContext, bytes, passId)
                    }
                }
                savingLogo = false
                result.onSuccess { name -> draft = draft.copy(logo = name) }
                    .onFailure { message = (it as? LogoException)?.message ?: "Couldn't use that picture." }
            }
        }
    }

    LaunchedEffect(message) {
        val m = message
        if (m != null) {
            snackbar.showSnackbar(m)
            message = null
        }
    }

    /** Saves the edits (a new pass with nothing in it isn't kept). */
    fun commit() {
        val base = Store.data.value.passes.firstOrNull { it.id == passId }
        val isEmpty = draft.title.isBlank() && draft.header.isBlank() && draft.barcodeValue.isBlank()
        if (!(base == null && isEmpty) && base != draft) Store.putPass(draft)
        // Logos no pass uses anymore (changed or removed) come off the phone.
        Logos.cleanUp(context.applicationContext, Store.data.value.passes.map { it.logo })
    }

    fun leave() {
        commit()
        onBack()
    }

    BackHandler { leave() }

    val theme = Store.themeFor(draft, d)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackBar(if (stored == null) "New pass" else draft.title.ifBlank { "Pass" }, onBack = { leave() }) {
                if (stored != null) {
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete pass") }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).imePadding().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
        ) {
            PassFace(draft, theme)

            SectionTitle("Card")
            Panel {
                Field("Card name", draft.title, "Shows under the logo, like \"Executive Member\"") { draft = draft.copy(title = it) }
                CardNameLook(draft) { draft = it }
                Field("Small line", draft.subheader, "Above your name, like \"Member\"") { draft = draft.copy(subheader = it) }
                Field("Big line", draft.header, "Usually your name") { draft = draft.copy(header = it) }
            }

            SectionTitle("Barcode")
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilledTonalButton(onClick = {
                        Barcodes.scan(context,
                            onResult = { value, type -> draft = draft.copy(barcodeValue = value, barcodeType = type) },
                            onError = { message = it })
                    }) {
                        Icon(painterResource(R.drawable.ic_scan), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Scan card")
                    }
                    BarcodeTypeMenu(draft.barcodeType) { draft = draft.copy(barcodeType = it) }
                }
                Field("Number", draft.barcodeValue, "What the barcode holds", KeyboardType.Ascii) {
                    draft = draft.copy(barcodeValue = it.trim())
                }
                SwitchRow("Show the number under the barcode", checked = draft.showNumber) { draft = draft.copy(showNumber = it) }
            }

            SectionTitle("Details")
            Panel {
                SwitchRow("Member ID line", "Shows the number in the pass's details", draft.showIdRow) {
                    draft = draft.copy(showIdRow = it)
                }
                if (draft.showIdRow) {
                    Field("Its label", draft.idRowLabel) { draft = draft.copy(idRowLabel = it) }
                }
                draft.rows.forEachIndexed { i, row ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Field("Label", row.label) { v -> draft = draft.copy(rows = draft.rows.mapIndexed { j, r -> if (j == i) r.copy(label = v) else r }) }
                            Field("Text", row.value) { v -> draft = draft.copy(rows = draft.rows.mapIndexed { j, r -> if (j == i) r.copy(value = v) else r }) }
                        }
                        IconButton(onClick = { draft = draft.copy(rows = draft.rows.filterIndexed { j, _ -> j != i }) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove line")
                        }
                    }
                }
                if (draft.rows.size < 2) {
                    TextButton(onClick = { draft = draft.copy(rows = draft.rows + DetailRow()) }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add a line")
                    }
                }
            }

            SectionTitle("Links")
            Panel {
                SwitchRow("Nearby locations", "Opens Google Maps to the closest ones", draft.nearby) { draft = draft.copy(nearby = it) }
                if (draft.nearby) {
                    Field("Search Maps for", draft.nearbyQuery, "Leave empty to use the card name") { draft = draft.copy(nearbyQuery = it) }
                }
                draft.links.forEachIndexed { i, link ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Field("Link name", link.label, "Like \"Website\"") { v ->
                                draft = draft.copy(links = draft.links.mapIndexed { j, l -> if (j == i) l.copy(label = v) else l })
                            }
                            Field("Link", link.url, "https://…", KeyboardType.Uri) { v ->
                                draft = draft.copy(links = draft.links.mapIndexed { j, l -> if (j == i) l.copy(url = v.trim()) else l })
                            }
                        }
                        IconButton(onClick = { draft = draft.copy(links = draft.links.filterIndexed { j, _ -> j != i }) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove link")
                        }
                    }
                }
                if (draft.links.size < 3) {
                    TextButton(onClick = { draft = draft.copy(links = draft.links + PassLink("Website", "https://")) }) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add a link")
                    }
                }
            }

            SectionTitle("Logo")
            Panel {
                Text(
                    "A picture for the top of the pass, like a store's logo: in a white circle, or big on its own with its " +
                        "background cut away. A see-through PNG works best. Only use a logo you have the right to use.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // The top of the pass with the logo the size it shows, so the changes below show as they're made.
                if (draft.logo.isNotBlank()) LogoPreview(draft, theme, moving = d.settings.motion && animatorsOn)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilledTonalButton(onClick = { logoPicker.launch(arrayOf("image/*")) }, enabled = !savingLogo) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (draft.logo.isBlank()) "Pick a picture" else "Change")
                    }
                    if (savingLogo) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else if (draft.logo.isNotBlank()) {
                        TextButton(onClick = { draft = draft.copy(logo = "") }) { Text("Remove") }
                    }
                }
                if (draft.logo.isNotBlank()) {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = draft.logoStyle == LogoStyle.CIRCLE,
                            onClick = { draft = draft.copy(logoStyle = LogoStyle.CIRCLE) },
                            shape = SegmentedButtonDefaults.itemShape(0, 2),
                        ) { Text("In a circle") }
                        SegmentedButton(
                            selected = draft.logoStyle == LogoStyle.BIG,
                            onClick = { draft = draft.copy(logoStyle = LogoStyle.BIG) },
                            shape = SegmentedButtonDefaults.itemShape(1, 2),
                        ) { Text("Big") }
                    }
                    if (draft.logoStyle == LogoStyle.BIG) {
                        val hasBig = remember(draft.logo) { Logos.hasBig(context, draft.logo) }
                        if (hasBig) {
                            SwitchRow("White line", "A thin white line around the logo, so it stands out on any theme.", draft.logoOutline) {
                                draft = draft.copy(logoOutline = it)
                            }
                        } else {
                            Text(
                                "Pick the picture again to show this logo big. Logos picked before this update were only kept " +
                                    "for the circle.",
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                    // As big as it goes: a circle up to one and a half times; a big logo until it's as wide as the screen.
                    val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
                    val maxSize = remember(draft.logo, draft.logoStyle, draft.logoOutline, screenWidth) {
                        val ratio = if (draft.logoStyle != LogoStyle.BIG) null
                        else Logos.size(context, Logos.bigName(draft.logo, draft.logoOutline))?.let { (w, h) -> w.toFloat() / h }
                        if (ratio == null) 1.5f
                        else ((screenWidth - 24f) / minOf(screenWidth - 40f, 140f * ratio)).coerceIn(1f, 1.5f)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Size", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text("${(draft.logoSize.coerceIn(0.5f, maxSize) * 100).roundToInt()}%", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (draft.logoSize != 1f) {
                            TextButton(onClick = { draft = draft.copy(logoSize = 1f) }) { Text("Reset") }
                        }
                    }
                    Slider(
                        value = draft.logoSize.coerceIn(0.5f, maxSize),
                        onValueChange = { draft = draft.copy(logoSize = ((it * 20).roundToInt() / 20f).coerceIn(0.5f, maxSize)) },
                        valueRange = 0.5f..maxSize,
                    )
                }
            }

            SectionTitle("Look")
            Panel {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = draft.mode == LookMode.AUTO,
                        onClick = { draft = draft.copy(mode = LookMode.AUTO) },
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                    ) { Text("Seasons & holidays") }
                    SegmentedButton(
                        selected = draft.mode == LookMode.FIXED,
                        onClick = { draft = draft.copy(mode = LookMode.FIXED) },
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                    ) { Text("One theme") }
                }
                if (draft.mode == LookMode.AUTO) {
                    val occ = Seasons.current(LocalDate.now(), d.schedule)
                    Text(
                        "Changes on its own: ${occ.label} now (${theme.name}). Pick each holiday's and season's theme in " +
                            "Seasons & holidays.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = { commit(); onSchedule() }) { Text("Seasons & holidays") }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Store.allThemes(d), key = { it.id }) { t ->
                            ThemeTile(t, selected = t.id == draft.fixedThemeId, onClick = { draft = draft.copy(fixedThemeId = t.id) },
                                modifier = Modifier.width(150.dp))
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this pass?") },
            text = { Text("It comes off Pass Studio for good.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    Store.deletePass(passId)
                    Logos.cleanUp(context.applicationContext, Store.data.value.passes.map { it.logo })
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

/**
 * The top of the pass with its logo and card name, the way the pass page shows them (scaled to fit here), over the
 * theme's art: for seeing the logo's style and size, and the name's color, as they change.
 */
@Composable
private fun LogoPreview(pass: Pass, theme: PassTheme, moving: Boolean) {
    val sky = parseHex(Catalog.skyColor(theme.art)) ?: Color(0xFF1A1A1A)
    val ink = inkOn(sky)
    BoxWithConstraints(
        Modifier.fillMaxWidth().heightIn(min = 200.dp).clip(RoundedCornerShape(18.dp)).background(sky),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Catalog.pageRes(theme.art)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.matchParentSize(),
        )
        val scale = maxWidth / 411.dp
        val (circle, big) = passLogoSize(pass, maxWidth, scale)
        Column(Modifier.padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PassLogo(pass, circle, big, ink, 46.sp * pass.logoSize.coerceIn(0.5f, 1.5f) * scale)
            Spacer(Modifier.height((if (pass.logoStyle == LogoStyle.BIG) 4.dp else 16.dp) * scale))
            CardName(
                pass.title.ifBlank { "My pass" },
                pass.titleColor,
                pass.executiveName(),
                ink,
                TextStyle(
                    fontSize = 19.sp * scale, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center,
                    shadow = Shadow(if (ink == Color.White) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
                        Offset(0f, 2f), 12f),
                ),
                moving = moving,
                modifier = Modifier.padding(horizontal = 24.dp * scale),
            )
        }
    }
}

/** The card name's color (gold and silver shine) and its lettering. */
@Composable
private fun CardNameLook(draft: Pass, onChange: (Pass) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Card name color", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(cardNameColorName(draft.titleColor), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (c in CARD_NAME_COLORS) {
                val picked = draft.titleColor == c
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .selectable(selected = picked, role = Role.RadioButton) { onChange(draft.copy(titleColor = c)) }
                        .semantics { contentDescription = cardNameColorName(c) }
                        .border(2.5.dp, if (picked) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                        .padding(5.dp)
                        .clip(CircleShape)
                        .background(cardNameSwatch(c))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                )
            }
        }
        Text("Font", style = MaterialTheme.typography.bodyLarge)
        val executive = draft.executiveName()
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !executive,
                onClick = { onChange(draft.copy(titleFont = "plain")) },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
            ) { Text("Plain") }
            SegmentedButton(
                selected = executive,
                onClick = { onChange(draft.copy(titleFont = "executive")) },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
            ) { Text(smallCaps("Executive"), fontFamily = ExecutiveFont, letterSpacing = 0.1.em) }
        }
        Text(
            "Executive is a membership card's lettering, in small capitals: type \"Executive Member\" and it shows like " +
                "the card's. Gold and silver use it unless you pick Plain.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    hint: String? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        supportingText = if (hint != null) ({ Text(hint) }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboard,
            capitalization = if (keyboard == KeyboardType.Text) KeyboardCapitalization.Words else KeyboardCapitalization.None,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun BarcodeTypeMenu(type: String, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { open = true }) {
            Text(Barcodes.label(type))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Barcodes.types.forEach { t ->
                DropdownMenuItem(text = { Text(Barcodes.label(t)) }, onClick = {
                    onPick(t)
                    open = false
                })
            }
        }
    }
}
