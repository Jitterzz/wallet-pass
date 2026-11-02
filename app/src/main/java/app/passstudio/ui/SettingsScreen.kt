package app.passstudio.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.passstudio.BuildConfig
import app.passstudio.QuickAccess
import app.passstudio.data.Store

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val d by Store.data.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(message) {
        val m = message
        if (m != null) {
            snackbar.showSnackbar(m)
            message = null
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BackBar("Settings", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
        ) {
            SectionTitle("Quick access")
            Panel {
                SwitchRow(
                    "Open straight to my pass",
                    "Opening Pass Studio (from its icon, the side button or a back tap) shows your starred pass full " +
                        "screen. Star a pass from its page.",
                    d.settings.openToPass,
                ) { on -> Store.update { it.copy(settings = it.settings.copy(openToPass = on)) } }
                FilledTonalButton(onClick = { QuickAccess.requestTile(context) { message = it } }) {
                    Text("Add quick settings tile")
                }
            }

            SectionTitle("Themes")
            Panel {
                SwitchRow(
                    "Moving themes",
                    "Snow falls, leaves drift, fireworks go off, candles flicker... on your pass page. They hold still " +
                        "when this is off or your phone has animations turned off.",
                    d.settings.motion,
                ) { on -> Store.update { it.copy(settings = it.settings.copy(motion = on)) } }
            }

            SectionTitle("Side button")
            Panel {
                listOf(
                    "Open your phone's Settings, then Advanced features, then Side button.",
                    "Turn on Double press, pick Open app, and choose Pass Studio.",
                    "Now pressing the side button twice opens your starred pass.",
                ).forEachIndexed { i, step ->
                    Row {
                        Text("${i + 1}.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(24.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Text(
                    "Rather keep the double press for something else? Samsung's Good Lock app (RegiStar module) can open " +
                        "Pass Studio when you tap the back of the phone or hold the side button.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                "Pass Studio ${BuildConfig.VERSION_NAME}. Your passes and logos are kept on this phone and go along with its backups.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 20.dp, start = 4.dp),
            )
        }
    }
}
