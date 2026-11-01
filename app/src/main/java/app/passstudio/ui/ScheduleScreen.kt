package app.passstudio.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.passstudio.data.LookMode
import app.passstudio.data.Occasion
import app.passstudio.data.ScheduleEntry
import app.passstudio.data.Seasons
import app.passstudio.data.Store
import app.passstudio.wallet.SyncWorker
import java.time.LocalDate

/**
 * Which theme each holiday and season gets, for passes that change on their own. Holidays can be switched off
 * (the season shows on their days instead).
 */
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val d by Store.data.collectAsState()
    val context = LocalContext.current
    var picking by remember { mutableStateOf<Occasion?>(null) }
    val today = Seasons.current(LocalDate.now(), d.schedule)

    fun change(occ: Occasion, entry: ScheduleEntry) {
        Store.update { data ->
            val next = data.copy(schedule = data.schedule + (occ to entry))
            // Passes on auto whose look changes today get it in Google Wallet.
            next.copy(passes = next.passes.map { p ->
                if (p.inWallet && p.mode == LookMode.AUTO && Store.themeFor(p, next).id != p.appliedThemeId) p.copy(dirty = true) else p
            })
        }
        SyncWorker.runNow(context)
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { BackBar("Seasons & holidays", onBack) }) { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "Passes set to \"Seasons & holidays\" switch to the theme below on their own. Today it's ${today.label}.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                )
            }
            item { SectionTitle("Holidays") }
            items(Occasion.entries.filter { it.holiday }, key = { it.name }) { occ ->
                val entry = d.schedule[occ] ?: ScheduleEntry(occ.defaultTheme)
                OccasionRow(occ, entry, today == occ, showSwitch = true,
                    onPick = { picking = occ }, onToggle = { change(occ, entry.copy(on = it)) })
            }
            item { SectionTitle("Seasons") }
            items(Occasion.entries.filter { !it.holiday }, key = { it.name }) { occ ->
                val entry = d.schedule[occ] ?: ScheduleEntry(occ.defaultTheme)
                OccasionRow(occ, entry, today == occ, showSwitch = false, onPick = { picking = occ }, onToggle = {})
            }
        }
    }

    picking?.let { occ ->
        val entry = d.schedule[occ] ?: ScheduleEntry(occ.defaultTheme)
        ThemePickerSheet(
            title = "${occ.label} theme",
            current = entry.themeId,
            onPick = {
                change(occ, entry.copy(themeId = it))
                picking = null
            },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun OccasionRow(
    occ: Occasion,
    entry: ScheduleEntry,
    isToday: Boolean,
    showSwitch: Boolean,
    onPick: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    val theme = Store.theme(entry.themeId)
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).clickable(onClick = onPick).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemeArt(theme, Modifier.width(112.dp).alpha(if (entry.on) 1f else 0.45f), corner = 12)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    occ.label + if (isToday) "  ·  today" else "",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                )
                Text(occ.days, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (entry.on) theme.name else "Off: the season shows instead",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary,
                )
            }
            if (showSwitch) Switch(checked = entry.on, onCheckedChange = onToggle)
        }
    }
}
