package app.passstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import app.passstudio.data.PassTheme
import app.passstudio.data.Store
import app.passstudio.ui.HomeScreen
import app.passstudio.ui.PassEditor
import app.passstudio.ui.PassStudioTheme
import app.passstudio.ui.ScheduleScreen
import app.passstudio.ui.SettingsScreen
import app.passstudio.ui.ThemeEditor
import app.passstudio.ui.ThemesScreen
import app.passstudio.wallet.SyncWorker

/** The screens, as a stack: the last one shows, and Back takes it off. */
sealed interface Screen {
    data object Home : Screen
    data class EditPass(val id: String) : Screen
    data object Themes : Screen
    data class EditTheme(val id: String?, val from: PassTheme? = null) : Screen
    data object Schedule : Screen
    data object Settings : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Store.init(this)
        SyncWorker.schedule(this)
        setContent { PassStudioTheme { App() } }
    }

    override fun onResume() {
        super.onResume()
        // Coming back from Google Wallet: see if a pass got saved, and catch up on today's theme.
        SyncWorker.runNow(this)
    }
}

@Composable
private fun App() {
    val stack = remember { mutableStateListOf<Screen>(Screen.Home) }
    fun go(s: Screen) {
        stack.add(s)
    }
    fun back() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
    // The editors handle Back themselves (to save first); this covers the rest.
    BackHandler(enabled = stack.size > 1) { back() }

    AnimatedContent(targetState = stack.last(), transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "screen") { screen ->
        when (screen) {
            Screen.Home -> HomeScreen(
                onOpenPass = { go(Screen.EditPass(it)) },
                onNewPass = { go(Screen.EditPass(Store.newId())) },
                onThemes = { go(Screen.Themes) },
                onSchedule = { go(Screen.Schedule) },
                onSettings = { go(Screen.Settings) },
            )
            is Screen.EditPass -> PassEditor(
                passId = screen.id,
                onBack = { back() },
                onSettings = { go(Screen.Settings) },
                onSchedule = { go(Screen.Schedule) },
            )
            Screen.Themes -> ThemesScreen(
                onBack = { back() },
                onEdit = { go(Screen.EditTheme(it)) },
                onCopy = { go(Screen.EditTheme(null, it)) },
            )
            is Screen.EditTheme -> ThemeEditor(themeId = screen.id, startFrom = screen.from, onBack = { back() })
            Screen.Schedule -> ScheduleScreen(onBack = { back() })
            Screen.Settings -> SettingsScreen(onBack = { back() })
        }
    }
}
