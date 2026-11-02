package app.passstudio.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.passstudio.R
import app.passstudio.data.DetailRow
import app.passstudio.data.LookMode
import app.passstudio.data.Pass
import app.passstudio.data.PassLink
import app.passstudio.data.Seasons
import app.passstudio.data.Store
import app.passstudio.wallet.LogoUpload
import app.passstudio.wallet.ServiceAccount
import app.passstudio.wallet.Sync
import app.passstudio.wallet.WalletApi
import app.passstudio.wallet.WalletException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** The pass without its Google Wallet bookkeeping, to tell whether anything the person sees has changed. */
private fun Pass.content(): Pass = copy(
    walletObjectId = "", walletClassId = "", inWallet = false, pending = false, appliedThemeId = "",
    dirty = false, lastSync = 0L, lastError = "",
)

/** [draft]'s edits with the Google Wallet bookkeeping from [stored] (which the background updates may have changed). */
private fun withWallet(draft: Pass, stored: Pass?): Pass = if (stored == null) draft else draft.copy(
    walletObjectId = stored.walletObjectId, walletClassId = stored.walletClassId, inWallet = stored.inWallet,
    pending = stored.pending, appliedThemeId = stored.appliedThemeId, dirty = stored.dirty, lastSync = stored.lastSync,
    lastError = stored.lastError,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassEditor(passId: String, onBack: () -> Unit, onSettings: () -> Unit, onSchedule: () -> Unit) {
    val d by Store.data.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val stored = d.passes.firstOrNull { it.id == passId }
    var draft by remember(passId) { mutableStateOf(stored ?: Pass(id = passId)) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var uploading by remember { mutableStateOf<LogoUpload.Kind?>(null) }
    var pickFor by remember { mutableStateOf<LogoUpload.Kind?>(null) }

    // Picking a logo: it's sized for Google Wallet, uploaded to the repo's logos/ folder, and its link filled in.
    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val kind = pickFor
        pickFor = null
        if (uri != null && kind != null) {
            uploading = kind
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    runCatching {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw WalletException("Couldn't open that file.")
                        val png = LogoUpload.prepare(bytes, kind)
                        val name = "${passId}-${kind.label}-${System.currentTimeMillis()}.png"
                        LogoUpload.upload(context.applicationContext, Store.data.value.settings, png, name)
                    }
                }
                uploading = null
                result.onSuccess { url ->
                    draft = if (kind == LogoUpload.Kind.WIDE) draft.copy(wideLogoUrl = url) else draft.copy(logoUrl = url)
                    message = "Logo uploaded."
                }.onFailure { message = it.message ?: "Couldn't upload the logo." }
            }
        }
    }

    // Logos that don't fit their spot (uploaded before Pass Studio sized them, or pasted as a link) get cut off in
    // Google Wallet: one tap makes a fitted copy.
    var fitting by remember { mutableStateOf(false) }
    val wideImage = rememberUrlImage(draft.wideLogoUrl)
    val roundImage = rememberUrlImage(draft.logoUrl)
    val misfits = remember(wideImage, roundImage) {
        buildList {
            if (wideImage != null && !LogoUpload.fits(wideImage.asAndroidBitmap(), LogoUpload.Kind.WIDE)) add(LogoUpload.Kind.WIDE)
            if (roundImage != null && !LogoUpload.fits(roundImage.asAndroidBitmap(), LogoUpload.Kind.ROUND)) add(LogoUpload.Kind.ROUND)
        }
    }

    fun pickLogo(kind: LogoUpload.Kind) {
        if (!LogoUpload.hasToken(context)) {
            message = "Add your GitHub token in Settings to upload logos."
            return
        }
        pickFor = kind
        logoPicker.launch(arrayOf("image/png"))
    }

    LaunchedEffect(message) {
        val m = message
        if (m != null) {
            snackbar.showSnackbar(m)
            message = null
        }
    }

    /** Saves the edits. A pass already in Google Wallet gets them there too. */
    fun commit(): Pass {
        val base = Store.data.value.passes.firstOrNull { it.id == passId }
        val merged = withWallet(draft, base)
        val isEmpty = merged.title.isBlank() && merged.header.isBlank() && merged.barcodeValue.isBlank()
        if (base == null && isEmpty) return merged
        val changed = base == null || base.content() != merged.content()
        if (!changed) return merged
        val final = if (merged.inWallet) merged.copy(dirty = true) else merged
        Store.putPass(final)
        if (final.inWallet) {
            scope.launch(Dispatchers.IO) { Sync.syncPass(context.applicationContext, passId, force = true) }
        }
        return final
    }

    fun leave() {
        commit()
        onBack()
    }

    fun fitLogos() {
        if (!LogoUpload.hasToken(context)) {
            message = "Add your GitHub token in Settings to fix logos."
            return
        }
        val todo = misfits.associateWith { if (it == LogoUpload.Kind.WIDE) draft.wideLogoUrl else draft.logoUrl }
        fitting = true
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    todo.mapValues { (kind, link) ->
                        LogoUpload.refit(context.applicationContext, Store.data.value.settings, link, kind, passId)
                    }
                }
            }
            fitting = false
            result.onSuccess { links ->
                links[LogoUpload.Kind.WIDE]?.let { draft = draft.copy(wideLogoUrl = it) }
                links[LogoUpload.Kind.ROUND]?.let { draft = draft.copy(logoUrl = it) }
                val p = commit()
                message = if (p.inWallet) "Logo fixed. It updates in Google Wallet in a moment." else "Logo fixed."
            }.onFailure { message = it.message ?: "Couldn't fix the logo." }
        }
    }

    fun addToWallet() {
        if (draft.title.isBlank()) {
            message = "Give the card a name first."
            return
        }
        val p = commit()
        Store.putPass(p)
        val settings = Store.data.value.settings
        val sa = ServiceAccount.load(context)
        if (settings.issuerId.isBlank() || sa == null) {
            message = "Connect Google Wallet in Settings first."
            return
        }
        busy = true
        scope.launch {
            val theme = Store.themeFor(p)
            val result = withContext(Dispatchers.Default) { runCatching { WalletApi.saveLink(p, theme, settings, sa) } }
            busy = false
            result.onSuccess { link ->
                Store.updatePass(passId) {
                    it.copy(walletObjectId = link.objectId, walletClassId = link.classId, pending = true, inWallet = false,
                        appliedThemeId = theme.id, dirty = false, lastError = "")
                }
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url))) }
                    .onFailure { message = "Couldn't open Google Wallet." }
            }.onFailure { message = it.message ?: "Couldn't make the pass." }
        }
    }

    fun checkSaved() {
        busy = true
        scope.launch {
            val found = withContext(Dispatchers.IO) { Sync.checkPending(context.applicationContext) }
            busy = false
            message = if (found.isNotEmpty()) "It's in your Google Wallet." else "Not in Google Wallet yet. Save it there, then check again."
        }
    }

    fun updateNow() {
        commit()
        busy = true
        scope.launch {
            val err = withContext(Dispatchers.IO) { Sync.syncPass(context.applicationContext, passId, force = true) }
            busy = false
            message = err ?: "Updated in Google Wallet."
        }
    }

    BackHandler { leave() }

    val theme = Store.themeFor(draft, d)
    // Logo previews sit on the pass's own color, so a white logo shows up.
    val passColor = parseHex(theme.color) ?: Color(0xFF1A1A1A)
    val wallet = stored ?: draft

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
            Spacer(Modifier.height(14.dp))
            WalletPanel(wallet, busy, onAdd = { addToWallet() }, onCheck = { checkSaved() }, onUpdate = { updateNow() },
                onSettings = onSettings)

            SectionTitle("Card")
            Panel {
                Field("Card name", draft.title, "Shows at the top, next to the logo") { draft = draft.copy(title = it) }
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
                    "A transparent PNG at the top of the pass. Pick one from your phone and it's uploaded to your GitHub " +
                        "repo's logos folder (Google Wallet only loads pictures from a public link), or paste a link. Only " +
                        "use a logo you have the right to use.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (misfits.isNotEmpty()) {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.errorContainer)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            when {
                                misfits.size == 2 -> "Your logos don't fit Google Wallet's logo spots, so they show up cut off."
                                misfits[0] == LogoUpload.Kind.ROUND -> "Your round logo isn't square, so Google Wallet cuts it off in the circle."
                                else -> "Your wide logo isn't the shape Google Wallet uses, so it shows up small or cut off."
                            },
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { fitLogos() }, enabled = !fitting && uploading == null) {
                                Text(if (misfits.size == 2) "Fix my logos" else "Fix my logo")
                            }
                            if (fitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                LogoField("Top logo (wide)", draft.wideLogoUrl, "Any PNG: it's fitted to 1280 × 400. Light logos show best on dark colors",
                    uploading = uploading == LogoUpload.Kind.WIDE, background = passColor, round = false,
                    onPick = { pickLogo(LogoUpload.Kind.WIDE) }) {
                    draft = draft.copy(wideLogoUrl = it.trim())
                }
                LogoField("Round logo", draft.logoUrl, "Any PNG: it's centered in a 660 × 660 circle with room to spare",
                    uploading = uploading == LogoUpload.Kind.ROUND, background = passColor, round = true,
                    onPick = { pickLogo(LogoUpload.Kind.ROUND) }) {
                    draft = draft.copy(logoUrl = it.trim())
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
            text = { Text("It comes off this app. If it's in Google Wallet, it stays there until you remove it in Google Wallet.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    Store.deletePass(passId)
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun WalletPanel(p: Pass, busy: Boolean, onAdd: () -> Unit, onCheck: () -> Unit, onUpdate: () -> Unit, onSettings: () -> Unit) {
    val context = LocalContext.current
    val d by Store.data.collectAsState()
    val ready = d.settings.issuerId.isNotBlank() && app.passstudio.wallet.KeyVault.has(context)
    Panel {
        when {
            !ready -> {
                Text("Connect Google Wallet to add this pass.", style = MaterialTheme.typography.bodyMedium)
                Button(onClick = onSettings) { Text("Set up Google Wallet") }
            }
            p.inWallet -> {
                Text("In your Google Wallet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (p.lastError.isNotBlank()) p.lastError else "Changes here go to Google Wallet on their own.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (p.lastError.isNotBlank()) Color(0xFFB3261E) else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = onUpdate, enabled = !busy) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Update now")
                    }
                    TextButton(onClick = onAdd, enabled = !busy) { Text("Add a new copy") }
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
            p.pending -> {
                Text("Save it in Google Wallet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Once it's saved there, it'll show here. It's checked whenever the app opens.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = onCheck, enabled = !busy) { Text("I saved it") }
                    TextButton(onClick = onAdd, enabled = !busy) { Text("Open it again") }
                    if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            }
            else -> {
                Button(onClick = onAdd, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    if (busy) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(painterResource(R.drawable.ic_wallet), contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Add to Google Wallet", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
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
private fun LogoField(
    label: String,
    value: String,
    hint: String,
    uploading: Boolean,
    background: Color,
    round: Boolean,
    onPick: () -> Unit,
    onChange: (String) -> Unit,
) {
    val image = rememberUrlImage(value)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilledTonalButton(onClick = onPick, enabled = !uploading) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (value.isBlank()) "Pick PNG" else "Change PNG")
            }
            if (uploading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Text("Uploading…", style = MaterialTheme.typography.bodySmall)
            } else if (value.isNotBlank()) {
                TextButton(onClick = { onChange("") }) { Text("Remove") }
            }
        }
        when {
            value.isBlank() -> Unit
            !value.trim().startsWith("https://") -> Text("Use an https:// link.", color = Color(0xFFB3261E),
                style = MaterialTheme.typography.bodySmall)
            image != null -> Box(
                Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp))
                    .background(background).padding(8.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (round) {
                    // As Google Wallet shows it: cut into a circle.
                    Image(bitmap = image, contentDescription = label, contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color.White))
                } else {
                    Image(bitmap = image, contentDescription = label, contentScale = ContentScale.Fit, modifier = Modifier.height(56.dp))
                }
            }
            else -> Text("Loading the picture… (if it never shows, check the link)",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Field("Or paste a link", value, hint, KeyboardType.Uri, onChange)
    }
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
