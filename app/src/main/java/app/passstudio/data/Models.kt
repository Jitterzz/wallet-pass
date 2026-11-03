package app.passstudio.data

import org.json.JSONArray
import org.json.JSONObject

/** A line in the pass's details, like "Member ID". */
data class DetailRow(val label: String = "", val value: String = "")

/** A link in the pass's details, like a website. */
data class PassLink(val label: String = "", val url: String = "")

/** How a pass picks its theme: on its own with the seasons and holidays, or one theme that stays. */
enum class LookMode { AUTO, FIXED }

/** How the logo shows at the top of the pass: in a white circle, or big on its own, cut out of its background. */
enum class LogoStyle { CIRCLE, BIG }

data class Pass(
    val id: String,
    val title: String = "",
    val subheader: String = "Member",
    val header: String = "",
    val barcodeValue: String = "",
    val barcodeType: String = "CODE_128",
    val showNumber: Boolean = true,
    // Details.
    val showIdRow: Boolean = true,
    val idRowLabel: String = "Member ID",
    val rows: List<DetailRow> = emptyList(),
    // Links.
    val nearby: Boolean = true,
    val nearbyQuery: String = "",
    val links: List<PassLink> = emptyList(),
    /** The logo at the top of the pass: the name of its picture in the app's own storage (see [Logos]). */
    val logo: String = "",
    val logoStyle: LogoStyle = LogoStyle.CIRCLE,
    /** A thin white line around a big logo, so it stands out on any theme. */
    val logoOutline: Boolean = true,
    /** How big the logo shows, from half to one and a half times its usual size. */
    val logoSize: Float = 1f,
    /** The card name's color: "" for the usual (white or dark to suit the theme), "gold" or "silver" (shiny), or #rrggbb. */
    val titleColor: String = "",
    /** The card name's font: "executive" (a membership card's lettering), "plain", or "" for the first with gold or silver. */
    val titleFont: String = "",
    /** Whether the card name sits on a black band with gold lines, like the Executive card, so it stands out. */
    val titleBand: Boolean = false,
    /** How fast the shine goes across gold or silver lettering: "slow", "medium" or "fast". */
    val shineSpeed: String = "medium",
    // Look.
    val mode: LookMode = LookMode.AUTO,
    val fixedThemeId: String = "aurora",
)

data class PassTheme(
    val id: String,
    val name: String,
    val color: String,
    val art: String,
    val builtIn: Boolean = false,
)

data class ScheduleEntry(val themeId: String, val on: Boolean = true)

data class Settings(
    /** The pass the quick settings tile opens (the starred one); the first pass when none is starred. */
    val quickPassId: String = "",
    /** Opening Pass Studio from its icon (or the side button) goes straight to that pass. */
    val openToPass: Boolean = true,
    /** Themes move on the pass page (snow falling, fireworks...). */
    val motion: Boolean = true,
)

data class AppData(
    val passes: List<Pass> = emptyList(),
    val customThemes: List<PassTheme> = emptyList(),
    val schedule: Map<Occasion, ScheduleEntry> = defaultSchedule(),
    val settings: Settings = Settings(),
)

fun defaultSchedule(): Map<Occasion, ScheduleEntry> = Occasion.entries.associateWith { ScheduleEntry(it.defaultTheme) }

/** Saving and loading [AppData] as JSON. */
object DataJson {
    fun encode(d: AppData): String {
        val o = JSONObject()
        o.put("version", 2)
        o.put("passes", JSONArray().apply { d.passes.forEach { put(pass(it)) } })
        o.put("customThemes", JSONArray().apply { d.customThemes.forEach { put(theme(it)) } })
        o.put("schedule", JSONObject().apply {
            d.schedule.forEach { (k, v) -> put(k.name, JSONObject().put("themeId", v.themeId).put("on", v.on)) }
        })
        o.put("settings", JSONObject().put("quickPassId", d.settings.quickPassId).put("openToPass", d.settings.openToPass)
            .put("motion", d.settings.motion))
        return o.toString()
    }

    fun decode(text: String): AppData {
        val o = JSONObject(text)
        val passes = o.optJSONArray("passes")?.let { a -> (0 until a.length()).map { pass(a.getJSONObject(it)) } } ?: emptyList()
        val themes = o.optJSONArray("customThemes")?.let { a -> (0 until a.length()).map { theme(a.getJSONObject(it)) } }
            ?: emptyList()
        val schedule = defaultSchedule().toMutableMap()
        o.optJSONObject("schedule")?.let { s ->
            Occasion.entries.forEach { occ ->
                s.optJSONObject(occ.name)?.let {
                    schedule[occ] = ScheduleEntry(it.optString("themeId", occ.defaultTheme), it.optBoolean("on", true))
                }
            }
        }
        val st = o.optJSONObject("settings")
        val settings = Settings(
            quickPassId = st?.optString("quickPassId", "") ?: "",
            openToPass = st?.optBoolean("openToPass", true) ?: true,
            motion = st?.optBoolean("motion", true) ?: true,
        )
        return AppData(passes, themes, schedule, settings)
    }

    private fun pass(p: Pass): JSONObject = JSONObject()
        .put("id", p.id).put("title", p.title).put("subheader", p.subheader).put("header", p.header)
        .put("barcodeValue", p.barcodeValue).put("barcodeType", p.barcodeType).put("showNumber", p.showNumber)
        .put("showIdRow", p.showIdRow).put("idRowLabel", p.idRowLabel)
        .put("rows", JSONArray().apply { p.rows.forEach { put(JSONObject().put("label", it.label).put("value", it.value)) } })
        .put("nearby", p.nearby).put("nearbyQuery", p.nearbyQuery)
        .put("links", JSONArray().apply { p.links.forEach { put(JSONObject().put("label", it.label).put("url", it.url)) } })
        .put("logo", p.logo).put("logoStyle", p.logoStyle.name).put("logoOutline", p.logoOutline)
        .put("logoSize", p.logoSize.toDouble()).put("titleColor", p.titleColor).put("titleFont", p.titleFont)
        .put("titleBand", p.titleBand).put("shineSpeed", p.shineSpeed)
        .put("mode", p.mode.name).put("fixedThemeId", p.fixedThemeId)

    private fun pass(o: JSONObject): Pass {
        val rows = o.optJSONArray("rows")?.let { a ->
            (0 until a.length()).map { val r = a.getJSONObject(it); DetailRow(r.optString("label"), r.optString("value")) }
        } ?: emptyList()
        val links = o.optJSONArray("links")?.let { a ->
            (0 until a.length()).map { val l = a.getJSONObject(it); PassLink(l.optString("label"), l.optString("url")) }
        } ?: emptyList()
        return Pass(
            id = o.getString("id"),
            title = o.optString("title"),
            subheader = o.optString("subheader", "Member"),
            header = o.optString("header"),
            barcodeValue = o.optString("barcodeValue"),
            barcodeType = o.optString("barcodeType", "CODE_128"),
            showNumber = o.optBoolean("showNumber", true),
            showIdRow = o.optBoolean("showIdRow", true),
            idRowLabel = o.optString("idRowLabel", "Member ID"),
            rows = rows,
            nearby = o.optBoolean("nearby", true),
            nearbyQuery = o.optString("nearbyQuery"),
            links = links,
            logo = o.optString("logo"),
            logoStyle = runCatching { LogoStyle.valueOf(o.optString("logoStyle", "CIRCLE")) }.getOrDefault(LogoStyle.CIRCLE),
            logoOutline = o.optBoolean("logoOutline", true),
            logoSize = o.optDouble("logoSize", 1.0).toFloat().coerceIn(0.5f, 1.5f),
            titleColor = o.optString("titleColor", ""),
            titleFont = o.optString("titleFont", ""),
            titleBand = o.optBoolean("titleBand", false),
            shineSpeed = o.optString("shineSpeed", "medium"),
            mode = runCatching { LookMode.valueOf(o.optString("mode", "AUTO")) }.getOrDefault(LookMode.AUTO),
            fixedThemeId = o.optString("fixedThemeId", "aurora"),
        )
    }

    private fun theme(t: PassTheme): JSONObject =
        JSONObject().put("id", t.id).put("name", t.name).put("color", t.color).put("art", t.art)

    private fun theme(o: JSONObject): PassTheme =
        PassTheme(o.getString("id"), o.optString("name", "My theme"), o.optString("color", "#1a1a1a"), o.optString("art", "aurora"))
}
