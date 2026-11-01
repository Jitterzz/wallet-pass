package app.passstudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.passstudio.data.Catalog
import app.passstudio.data.Pass
import app.passstudio.data.PassTheme
import app.passstudio.data.Store
import app.passstudio.wallet.SyncWorker

/** Every theme: the built-in ones by group, then the person's own. Tap one of yours to edit it. */
@Composable
fun ThemesScreen(onBack: () -> Unit, onEdit: (String?) -> Unit, onCopy: (PassTheme) -> Unit) {
    val d by Store.data.collectAsState()
    var picked by remember { mutableStateOf<PassTheme?>(null) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackBar("Themes", onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEdit(null) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New theme") },
            )
        },
    ) { pad ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 104.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (d.customThemes.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { SectionTitle("Your themes") }
                items(d.customThemes, key = { it.id }) { t -> ThemeTile(t, false, onClick = { onEdit(t.id) }) }
            }
            Catalog.groups.forEach { group ->
                item(span = { GridItemSpan(maxLineSpan) }, key = "group-" + group.name) { SectionTitle(group.name) }
                items(group.ids.map { Store.theme(it, d) }, key = { "builtin-" + it.id }) { t ->
                    ThemeTile(t, false, onClick = { picked = t })
                }
            }
        }
    }
    picked?.let { t ->
        AlertDialog(
            onDismissRequest = { picked = null },
            title = { Text(t.name) },
            text = { Text("Built-in themes stay as they are. Make your own copy to change its color or art.") },
            confirmButton = {
                TextButton(onClick = {
                    picked = null
                    onCopy(t)
                }) { Text("Make a copy") }
            },
            dismissButton = { TextButton(onClick = { picked = null }) { Text("Close") } },
        )
    }
}

/** Making or changing one of the person's own themes: a name, a color and a piece of art. */
@Composable
fun ThemeEditor(themeId: String?, startFrom: PassTheme?, onBack: () -> Unit) {
    val d by Store.data.collectAsState()
    val context = LocalContext.current
    val existing = themeId?.let { id -> d.customThemes.firstOrNull { it.id == id } }
    var theme by remember(themeId) {
        mutableStateOf(
            existing ?: PassTheme(
                id = "custom_" + Store.newId(),
                name = startFrom?.let { "My ${it.name}" } ?: "My theme",
                color = startFrom?.color ?: "#1e3a5f",
                art = startFrom?.art ?: "aurora",
            )
        )
    }
    var hex by remember(themeId) { mutableStateOf(theme.color) }
    var confirmDelete by remember { mutableStateOf(false) }

    fun save() {
        val name = theme.name.trim().ifBlank { "My theme" }
        Store.putTheme(theme.copy(name = name))
        // Passes showing this theme get the new look in Google Wallet.
        Store.update { data ->
            data.copy(passes = data.passes.map { p ->
                if (p.inWallet && Store.themeFor(p, data).id == theme.id) p.copy(dirty = true) else p
            })
        }
        SyncWorker.runNow(context)
        onBack()
    }

    BackHandler { save() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackBar(if (existing == null) "New theme" else "Edit theme", onBack = { save() }) {
                if (existing != null) {
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete theme") }
                }
                IconButton(onClick = { save() }) { Icon(Icons.Filled.Check, contentDescription = "Save theme") }
            }
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).imePadding().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
        ) {
            PassFace(Pass(id = "preview", title = "Card name", header = "Your name", barcodeValue = "012345678905"), theme)
            SectionTitle("Name")
            OutlinedTextField(
                value = theme.name,
                onValueChange = { theme = theme.copy(name = it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            SectionTitle("Color")
            ColorGrid(theme.color) {
                theme = theme.copy(color = it)
                hex = it
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = hex,
                onValueChange = { v ->
                    hex = v
                    val c = if (v.startsWith("#")) v else "#$v"
                    if (isHexColor(c)) theme = theme.copy(color = c.lowercase())
                },
                label = { Text("Or type a color, like #1e3a5f") },
                isError = !isHexColor(if (hex.startsWith("#")) hex else "#$hex"),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            SectionTitle("Art")
            ArtGrid(theme.art, theme.color) { theme = theme.copy(art = it) }
        }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this theme?") },
            text = { Text("Passes and holidays using it go back to their usual theme.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    Store.deleteTheme(existing.id)
                    onBack()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun ColorGrid(selected: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Catalog.swatches.chunked(8).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                row.forEach { hex ->
                    val c = parseHex(hex) ?: Color.Black
                    val on = hex.equals(selected, ignoreCase = true)
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(c)
                            .border(if (on) 3.dp else 1.dp, if (on) MaterialTheme.colorScheme.primary else Color(0x33000000), CircleShape)
                            .clickable { onPick(hex) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (on) Icon(Icons.Filled.Check, contentDescription = null, tint = inkOn(c), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtGrid(selected: String, color: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Catalog.arts.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { art ->
                    ThemeTile(
                        PassTheme("art-$art", Catalog.artName(art), color, art),
                        selected = art == selected,
                        onClick = { onPick(art) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** A sheet of every theme to pick one from (used by the seasons and holidays). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerSheet(title: String, current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val d by Store.data.collectAsState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().height(560.dp),
        ) {
            items(Store.allThemes(d), key = { it.id }) { t ->
                ThemeTile(t, t.id == current, onClick = { onPick(t.id) })
            }
        }
    }
}
