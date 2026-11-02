package app.passstudio.data

import org.json.JSONArray
import org.json.JSONObject

/** A line in the pass's details, like "Member ID". */
data class DetailRow(val label: String = "", val value: String = "")

/** A link in the pass's details, like a website. */
data class PassLink(val label: String = "", val url: String = "")

/** How a pass picks its theme: on its own with the seasons and holidays, or one theme that stays. */
enum class LookMode { AUTO, FIXED }

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
    // Logos, by public https link (Google loads them from there).
    val wideLogoUrl: String = "",
    val logoUrl: String = "",
    // Look.
    val mode: LookMode = LookMode.AUTO,
    val fixedThemeId: String = "aurora",
    // Google Wallet.
    val walletObjectId: String = "",
    val walletClassId: String = "",
    val inWallet: Boolean = false,
    val pending: Boolean = false,
    val appliedThemeId: String = "",
    val dirty: Boolean = false,
    val lastSync: Long = 0L,
    val lastError: String = "",
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
    val issuerId: String = "",
    val artBase: String = Catalog.DEFAULT_ART_BASE,
    /** Where picked logos get uploaded ("owner/repo"), so Google Wallet can load them from a public link. */
    val githubRepo: String = Catalog.DEFAULT_GITHUB_REPO,
    val githubBranch: String = "main",
    /** The pass the quick settings tile opens (the starred one); the first pass when none is starred. */
    val quickPassId: String = "",
    /** Opening Pass Studio from its icon (or the side button) goes straight to that pass. */
    val openToPass: Boolean = true,
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
        o.put("version", 1)
        o.put("passes", JSONArray().apply { d.passes.forEach { put(pass(it)) } })
        o.put("customThemes", JSONArray().apply { d.customThemes.forEach { put(theme(it)) } })
        o.put("schedule", JSONObject().apply {
            d.schedule.forEach { (k, v) -> put(k.name, JSONObject().put("themeId", v.themeId).put("on", v.on)) }
        })
        o.put("settings", JSONObject().put("issuerId", d.settings.issuerId).put("artBase", d.settings.artBase)
            .put("githubRepo", d.settings.githubRepo).put("githubBranch", d.settings.githubBranch)
            .put("quickPassId", d.settings.quickPassId).put("openToPass", d.settings.openToPass))
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
            issuerId = st?.optString("issuerId", "") ?: "",
            artBase = st?.optString("artBase", Catalog.DEFAULT_ART_BASE)?.ifBlank { Catalog.DEFAULT_ART_BASE }
                ?: Catalog.DEFAULT_ART_BASE,
            githubRepo = st?.optString("githubRepo", Catalog.DEFAULT_GITHUB_REPO)?.ifBlank { Catalog.DEFAULT_GITHUB_REPO }
                ?: Catalog.DEFAULT_GITHUB_REPO,
            githubBranch = st?.optString("githubBranch", "main")?.ifBlank { "main" } ?: "main",
            quickPassId = st?.optString("quickPassId", "") ?: "",
            openToPass = st?.optBoolean("openToPass", true) ?: true,
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
        .put("wideLogoUrl", p.wideLogoUrl).put("logoUrl", p.logoUrl)
        .put("mode", p.mode.name).put("fixedThemeId", p.fixedThemeId)
        .put("walletObjectId", p.walletObjectId).put("walletClassId", p.walletClassId)
        .put("inWallet", p.inWallet).put("pending", p.pending).put("appliedThemeId", p.appliedThemeId)
        .put("dirty", p.dirty).put("lastSync", p.lastSync).put("lastError", p.lastError)

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
            wideLogoUrl = o.optString("wideLogoUrl"),
            logoUrl = o.optString("logoUrl"),
            mode = runCatching { LookMode.valueOf(o.optString("mode", "AUTO")) }.getOrDefault(LookMode.AUTO),
            fixedThemeId = o.optString("fixedThemeId", "aurora"),
            walletObjectId = o.optString("walletObjectId"),
            walletClassId = o.optString("walletClassId"),
            inWallet = o.optBoolean("inWallet", false),
            pending = o.optBoolean("pending", false),
            appliedThemeId = o.optString("appliedThemeId"),
            dirty = o.optBoolean("dirty", false),
            lastSync = o.optLong("lastSync", 0L),
            lastError = o.optString("lastError"),
        )
    }

    private fun theme(t: PassTheme): JSONObject =
        JSONObject().put("id", t.id).put("name", t.name).put("color", t.color).put("art", t.art)

    private fun theme(o: JSONObject): PassTheme =
        PassTheme(o.getString("id"), o.optString("name", "My theme"), o.optString("color", "#1a1a1a"), o.optString("art", "aurora"))
}
