package app.passstudio.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.LocalDate
import java.util.UUID

/** Everything the app keeps (passes, themes, the schedule, settings), saved to one file on the phone. */
object Store {
    private lateinit var file: File
    private val state = MutableStateFlow(AppData())
    val data: StateFlow<AppData> get() = state

    @Volatile
    private var ready = false

    fun init(context: Context) {
        if (ready) return
        synchronized(this) {
            if (ready) return
            file = File(context.applicationContext.filesDir, "passes.json")
            state.value = if (file.exists()) {
                runCatching { DataJson.decode(file.readText()) }.getOrDefault(AppData())
            } else {
                AppData()
            }
            ready = true
        }
    }

    fun update(change: (AppData) -> AppData) {
        synchronized(this) {
            val next = change(state.value)
            state.value = next
            runCatching {
                val tmp = File(file.parentFile, "passes.json.tmp")
                tmp.writeText(DataJson.encode(next))
                if (!tmp.renameTo(file)) {
                    file.writeText(DataJson.encode(next))
                    tmp.delete()
                }
            }
        }
    }

    fun updatePass(id: String, change: (Pass) -> Pass) =
        update { d -> d.copy(passes = d.passes.map { if (it.id == id) change(it) else it }) }

    fun putPass(pass: Pass) = update { d ->
        if (d.passes.any { it.id == pass.id }) d.copy(passes = d.passes.map { if (it.id == pass.id) pass else it })
        else d.copy(passes = d.passes + pass)
    }

    fun deletePass(id: String) = update { d -> d.copy(passes = d.passes.filterNot { it.id == id }) }

    fun putTheme(theme: PassTheme) = update { d ->
        if (d.customThemes.any { it.id == theme.id }) d.copy(customThemes = d.customThemes.map { if (it.id == theme.id) theme else it })
        else d.copy(customThemes = d.customThemes + theme)
    }

    /** Deletes a custom theme, moving anything that used it back to its default. */
    fun deleteTheme(id: String) = update { d ->
        d.copy(
            customThemes = d.customThemes.filterNot { it.id == id },
            schedule = d.schedule.mapValues { (occ, e) -> if (e.themeId == id) e.copy(themeId = occ.defaultTheme) else e },
            passes = d.passes.map { if (it.fixedThemeId == id) it.copy(fixedThemeId = "aurora", dirty = true) else it },
        )
    }

    fun allThemes(d: AppData = data.value): List<PassTheme> = Catalog.themes + d.customThemes

    fun theme(id: String, d: AppData = data.value): PassTheme =
        d.customThemes.firstOrNull { it.id == id } ?: Catalog.themes.firstOrNull { it.id == id } ?: Catalog.themes.first()

    /** The theme [pass] shows today. */
    fun themeFor(pass: Pass, d: AppData = data.value, date: LocalDate = LocalDate.now()): PassTheme =
        if (pass.mode == LookMode.AUTO) {
            val occ = Seasons.current(date, d.schedule)
            theme(d.schedule[occ]?.themeId ?: occ.defaultTheme, d)
        } else {
            theme(pass.fixedThemeId, d)
        }

    fun newId(): String = UUID.randomUUID().toString().replace("-", "").take(12)
}
