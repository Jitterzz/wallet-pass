package app.passstudio

import android.content.Intent
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import app.passstudio.data.PassTheme
import app.passstudio.data.Store
import app.passstudio.ui.HomeScreen
import app.passstudio.ui.PassEditor
import app.passstudio.ui.PassScreen
import app.passstudio.ui.PassStudioTheme
import app.passstudio.ui.ScheduleScreen
import app.passstudio.ui.SettingsScreen
import app.passstudio.ui.ThemeEditor
import app.passstudio.ui.ThemesScreen
import app.passstudio.wallet.SyncWorker

/** The screens, as a stack: the last one shows, and Back takes it off. */
sealed interface Screen {
    data object Home : Screen
    data class ShowPass(val id: String) : Screen
    data class EditPass(val id: String) : Screen
    data object Themes : Screen
    data class EditTheme(val id: String?, val from: PassTheme? = null) : Screen
    data object Schedule : Screen
    data object Settings : Screen
}

class MainActivity : ComponentActivity() {
    /** A pass to open full screen (from the quick settings tile, a home-screen shortcut, or opening the app). */
    private val openPass = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Store.init(this)
        SyncWorker.schedule(this)
        if (savedInstanceState == null) openPass.value = passToOpen(intent, launched = true)
        setContent { PassStudioTheme { App(openPass) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        passToOpen(intent, launched = false)?.let { openPass.value = it }
    }

    override fun onResume() {
        super.onResume()
        // Coming back from Google Wallet: see if a pass got saved, and catch up on today's theme.
        SyncWorker.runNow(this)
    }

    /**
     * The pass an intent asks for. Opening the app from its icon (or a side-button or back-tap shortcut) goes
     * straight to the starred pass, unless that's turned off in Settings.
     */
    private fun passToOpen(intent: Intent?, launched: Boolean): String? {
        intent?.getStringExtra(EXTRA_SHOW_PASS)?.let { return it }
        val fromLauncher = intent?.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
        val d = Store.data.value
        return if (launched && fromLauncher && d.settings.openToPass) Store.quickPass(d)?.id else null
    }

    companion object {
        const val EXTRA_SHOW_PASS = "app.passstudio.SHOW_PASS"
    }
}

@Composable
private fun App(openPass: MutableState<String?>) {
    val stack = remember { mutableStateListOf<Screen>(Screen.Home) }
    fun go(s: Screen) {
        stack.add(s)
    }
    fun back() {
        if (stack.size > 1) stack.removeAt(stack.lastIndex)
    }
    // A pass asked for from outside the app opens on top of the list.
    LaunchedEffect(openPass.value) {
        val id = openPass.value ?: return@LaunchedEffect
        if (Store.data.value.passes.any { it.id == id }) {
            while (stack.size > 1) stack.removeAt(stack.lastIndex)
            stack.add(Screen.ShowPass(id))
        }
        openPass.value = null
    }
    // The editors handle Back themselves (to save first); this covers the rest.
    BackHandler(enabled = stack.size > 1) { back() }

    AnimatedContent(targetState = stack.last(), transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "screen") { screen ->
        when (screen) {
            Screen.Home -> HomeScreen(
                onOpenPass = { go(Screen.ShowPass(it)) },
                onNewPass = { go(Screen.EditPass(Store.newId())) },
                onThemes = { go(Screen.Themes) },
                onSchedule = { go(Screen.Schedule) },
                onSettings = { go(Screen.Settings) },
            )
            is Screen.ShowPass -> PassScreen(
                passId = screen.id,
                onBack = { back() },
                onEdit = { go(Screen.EditPass(screen.id)) },
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
