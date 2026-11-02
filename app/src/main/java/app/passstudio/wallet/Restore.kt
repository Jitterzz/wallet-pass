package app.passstudio.wallet

import android.content.Context
import android.net.Uri
import app.passstudio.data.DetailRow
import app.passstudio.data.LookMode
import app.passstudio.data.Pass
import app.passstudio.data.PassLink
import app.passstudio.data.Store
import org.json.JSONObject

/**
 * Bringing back passes that are already in Google Wallet, after reinstalling Pass Studio or moving to a new phone:
 * each one comes back with its name, barcode, details, links and logos, still linked to the same Google Wallet
 * pass, so its theme keeps changing there. Blocking: call it off the main thread.
 */
object Restore {
    private fun text(o: JSONObject, key: String): String =
        o.optJSONObject(key)?.optJSONObject("defaultValue")?.optString("value").orEmpty()

    private fun imageUrl(o: JSONObject, key: String): String =
        o.optJSONObject(key)?.optJSONObject("sourceUri")?.optString("uri").orEmpty()

    /** The pass a Google Wallet pass object describes, or null if it isn't one of Pass Studio's (or isn't active). */
    fun toPass(o: JSONObject, issuerId: String): Pass? {
        val objectId = o.optString("id")
        val prefix = "$issuerId.ps_"
        if (!objectId.startsWith(prefix)) return null
        val state = o.optString("state").uppercase()
        if (state.isNotEmpty() && state != "ACTIVE" && state != "STATE_UNSPECIFIED") return null
        if (!o.optBoolean("hasUsers", true)) return null

        // Object IDs are "<issuer>.ps_<pass id>_<when it was made>".
        val localId = objectId.removePrefix(prefix).substringBeforeLast('_').ifBlank { Store.newId() }
        val title = text(o, "cardTitle")

        val barcode = o.optJSONObject("barcode")
        val code = barcode?.optString("value").orEmpty()

        var showId = false
        var idLabel = "Member ID"
        val rows = mutableListOf<DetailRow>()
        o.optJSONArray("textModulesData")?.let { a ->
            for (i in 0 until a.length()) {
                val m = a.getJSONObject(i)
                if (m.optString("id") == "member_id") {
                    showId = true
                    idLabel = m.optString("header").ifBlank { "Member ID" }
                } else {
                    rows += DetailRow(m.optString("header"), m.optString("body"))
                }
            }
        }

        var nearby = false
        var nearbyQuery = ""
        val links = mutableListOf<PassLink>()
        o.optJSONObject("linksModuleData")?.optJSONArray("uris")?.let { a ->
            for (i in 0 until a.length()) {
                val u = a.getJSONObject(i)
                if (u.optString("id") == "nearby") {
                    nearby = true
                    val q = runCatching { Uri.parse(u.optString("uri")).getQueryParameter("q") }.getOrNull().orEmpty()
                    nearbyQuery = if (q.equals(title.trim(), ignoreCase = true)) "" else q
                } else {
                    links += PassLink(u.optString("description"), u.optString("uri"))
                }
            }
        }

        return Pass(
            id = localId,
            title = title,
            subheader = text(o, "subheader"),
            header = text(o, "header"),
            barcodeValue = code,
            barcodeType = barcode?.optString("type").orEmpty().ifBlank { "CODE_128" },
            showNumber = barcode?.has("alternateText") == true,
            showIdRow = showId,
            idRowLabel = idLabel,
            rows = rows.take(2),
            nearby = nearby,
            nearbyQuery = nearbyQuery,
            links = links.take(3),
            wideLogoUrl = imageUrl(o, "wideLogo"),
            logoUrl = imageUrl(o, "logo"),
            mode = LookMode.AUTO,
            walletObjectId = objectId,
            walletClassId = o.optString("classId"),
            inWallet = true,
            pending = false,
            // Blank so the next update puts today's theme on it.
            appliedThemeId = "",
        )
    }

    /** Brings back every Pass Studio pass in Google Wallet that isn't in the app. Returns how many came back. */
    fun run(context: Context): Int {
        Store.init(context)
        val d = Store.data.value
        val issuer = d.settings.issuerId.trim()
        val sa = ServiceAccount.load(context) ?: throw WalletException("Add your Google key in Settings first.")
        val found = WalletApi.listSaved(sa, issuer).mapNotNull { o -> toPass(o, issuer)?.let { it to o.optString("id") } }
        // A pass added again ("Add a new copy") has more than one: keep the newest.
        val newest = found.groupBy { it.first.id }.values.map { copies ->
            copies.maxBy { (_, objectId) -> objectId.substringAfterLast('_').toLongOrNull() ?: 0L }.first
        }
        val have = d.passes.flatMap { listOf(it.id, it.walletObjectId) }.filter { it.isNotBlank() }.toSet()
        val fresh = newest.filter { it.id !in have && it.walletObjectId !in have }
        if (fresh.isNotEmpty()) {
            Store.update { data -> data.copy(passes = data.passes + fresh) }
            Sync.syncAll(context)
        }
        return fresh.size
    }
}
