package app.passstudio.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.passstudio.R
import app.passstudio.data.Catalog
import app.passstudio.data.LookMode
import app.passstudio.data.Pass
import app.passstudio.data.PassTheme
import app.passstudio.data.Seasons
import app.passstudio.data.Store
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenPass: (String) -> Unit,
    onNewPass: () -> Unit,
    onThemes: () -> Unit,
    onSchedule: () -> Unit,
    onSettings: () -> Unit,
) {
    val d by Store.data.collectAsState()
    val occasion = Seasons.current(LocalDate.now(), d.schedule)
    val todayTheme = Store.theme(d.schedule[occasion]?.themeId ?: occasion.defaultTheme, d)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Pass Studio", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onThemes) { Icon(painterResource(R.drawable.ic_palette), contentDescription = "Themes") }
                    IconButton(onClick = onSchedule) { Icon(Icons.Filled.DateRange, contentDescription = "Seasons and holidays") }
                    IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewPass,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New pass") },
            )
        },
    ) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                TodayCard(occasion.label, occasion.days, todayTheme, onSchedule)
            }
            if (d.passes.isEmpty()) {
                item { EmptyCard(onNewPass) }
            } else {
                item {
                    Text("Your passes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp))
                }
                items(d.passes, key = { it.id }) { p ->
                    PassCard(p, Store.themeFor(p, d), statusOf(p, Store.themeFor(p, d).name)) { onOpenPass(p.id) }
                }
            }
        }
    }
}

fun statusOf(p: Pass, themeName: String): String =
    if (p.mode == LookMode.AUTO) "$themeName · changes with the seasons and holidays" else "$themeName theme"

@Composable
private fun TodayCard(occasion: String, days: String, theme: PassTheme, onSchedule: () -> Unit) {
    ThemedCard(theme, onSchedule, Modifier.heightIn(min = 172.dp)) { ink ->
        Column(Modifier.fillMaxWidth(0.62f).padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("TODAY", color = ink.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(occasion, color = ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("$days · ${theme.name} theme", color = ink.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            Text("Passes on auto switch by themselves", color = ink.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun EmptyCard(onNewPass: () -> Unit) {
    Panel {
        Text("No passes yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Make a pass for any card with a barcode: scan it, add your logo, and pick a look. Show it at the register " +
                "straight from here.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onNewPass) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Make a pass")
        }
    }
}
