package app.passstudio.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.passstudio.R
import app.passstudio.data.DetailRow
import app.passstudio.data.LookMode
import app.passstudio.data.Pass
import app.passstudio.data.PassLink
import app.passstudio.data.Seasons
import app.passstudio.data.Store
import app.passstudio.data.LogoException
import app.passstudio.data.Logos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

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

    // Picking a logo: it's fitted to the round spot and kept on the phone.
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
                Field("Card name", draft.title, "Shows at the top, under the logo") { draft = draft.copy(title = it) }
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
                    "A picture for the circle at the top of the pass, like a store's logo. A see-through PNG works best: " +
                        "it's trimmed and centered on white for you. Only use a logo you have the right to use.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (draft.logo.isNotBlank()) RoundLogo(draft, 56.dp, MaterialTheme.colorScheme.onSurface, 22.sp)
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
