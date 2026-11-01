package app.passstudio.wallet

import app.passstudio.data.Pass
import app.passstudio.data.PassTheme
import app.passstudio.data.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder

/** Google Wallet passes: the "Add to Google Wallet" link, and updating a pass that's already saved. */
object WalletApi {
    private const val OBJECTS = "https://walletobjects.googleapis.com/walletobjects/v1/genericObject/"
    private const val SAVE = "https://pay.google.com/gp/v/save/"

    class SaveLink(val url: String, val objectId: String, val classId: String)

    private fun text(value: String) = JSONObject().put("defaultValue", JSONObject().put("language", "en-US").put("value", value))

    private fun image(url: String, description: String) = JSONObject()
        .put("sourceUri", JSONObject().put("uri", url))
        .put("contentDescription", text(description))

    fun isImageUrl(url: String) = url.trim().startsWith("https://")

    fun isWebUrl(url: String) = url.trim().let { it.startsWith("https://") || it.startsWith("http://") }

    private fun hostOf(url: String): String =
        runCatching { URI(url.trim()).host?.removePrefix("www.") }.getOrNull()?.takeIf { it.isNotBlank() } ?: "Website"

    fun artUrl(settings: Settings, art: String): String {
        val base = settings.artBase.trim().ifBlank { app.passstudio.data.Catalog.DEFAULT_ART_BASE }
        return (if (base.endsWith("/")) base else "$base/") + art + ".png"
    }

    /** The pass as Google Wallet stores it (a Generic pass object). */
    fun passObject(pass: Pass, theme: PassTheme, settings: Settings, objectId: String, classId: String): JSONObject {
        val title = pass.title.trim().ifBlank { "My pass" }
        val o = JSONObject()
            .put("id", objectId)
            .put("classId", classId)
            .put("state", "ACTIVE")
            .put("cardTitle", text(title))
            .put("header", text(pass.header.trim().ifBlank { title }))
            .put("hexBackgroundColor", theme.color.trim().lowercase())
            .put("heroImage", image(artUrl(settings, theme.art), theme.name))
        if (pass.subheader.isNotBlank()) o.put("subheader", text(pass.subheader.trim()))

        val code = pass.barcodeValue.trim()
        if (code.isNotEmpty()) {
            val barcode = JSONObject().put("type", pass.barcodeType).put("value", code)
            if (pass.showNumber) barcode.put("alternateText", code)
            o.put("barcode", barcode)
        }

        val rows = JSONArray()
        if (pass.showIdRow && code.isNotEmpty()) {
            rows.put(JSONObject().put("id", "member_id").put("header", pass.idRowLabel.trim().ifBlank { "Member ID" }).put("body", code))
        }
        pass.rows.filter { it.label.isNotBlank() && it.value.isNotBlank() }.forEachIndexed { i, r ->
            rows.put(JSONObject().put("id", "row_$i").put("header", r.label.trim()).put("body", r.value.trim()))
        }
        if (rows.length() > 0) o.put("textModulesData", rows)

        val links = JSONArray()
        if (pass.nearby) {
            val q = pass.nearbyQuery.trim().ifBlank { pass.title.trim() }
            if (q.isNotEmpty()) {
                links.put(JSONObject().put("id", "nearby")
                    .put("uri", "https://maps.google.com/?q=" + URLEncoder.encode(q, "UTF-8"))
                    .put("description", "Nearby locations"))
            }
        }
        pass.links.filter { isWebUrl(it.url) }.forEachIndexed { i, l ->
            links.put(JSONObject().put("id", "link_$i").put("uri", l.url.trim())
                .put("description", l.label.trim().ifBlank { hostOf(l.url) }))
        }
        if (links.length() > 0) o.put("linksModuleData", JSONObject().put("uris", links))

        if (isImageUrl(pass.wideLogoUrl)) o.put("wideLogo", image(pass.wideLogoUrl.trim(), "$title logo"))
        if (isImageUrl(pass.logoUrl)) o.put("logo", image(pass.logoUrl.trim(), "$title logo"))
        return o
    }

    /**
     * A signed "Add to Google Wallet" link for a brand-new copy of the pass. The pass (and its class) are created
     * when it's saved, under new IDs, so an older copy in Google Wallet stays as it was.
     */
    fun saveLink(pass: Pass, theme: PassTheme, settings: Settings, sa: ServiceAccount): SaveLink {
        val issuer = settings.issuerId.trim()
        if (issuer.isEmpty() || !issuer.all { it.isDigit() }) {
            throw WalletException("Add your Issuer ID in Settings first.")
        }
        val stamp = System.currentTimeMillis()
        val objectId = "$issuer.ps_${pass.id}_$stamp"
        val classId = "$issuer.ps_class_${pass.id}_$stamp"
        val claims = JSONObject()
            .put("iss", sa.clientEmail)
            .put("aud", "google")
            .put("typ", "savetowallet")
            .put("iat", stamp / 1000)
            .put("origins", JSONArray())
            .put("payload", JSONObject()
                .put("genericClasses", JSONArray().put(JSONObject().put("id", classId)))
                .put("genericObjects", JSONArray().put(passObject(pass, theme, settings, objectId, classId))))
        return SaveLink(SAVE + Jwt.sign(claims, sa), objectId, classId)
    }

    /** True if the pass is in Google Wallet, false if it isn't (not saved yet, or deleted). Blocking. */
    fun exists(sa: ServiceAccount, objectId: String): Boolean {
        val r = Http.request("GET", OBJECTS + URLEncoder.encode(objectId, "UTF-8"), bearer = GoogleAuth.accessToken(sa))
        return when (r.code) {
            200 -> true
            404 -> false
            else -> throw WalletException(Http.errorOf(r))
        }
    }

    /** Replaces the saved pass with how it should look now (theme, details, links, logos). Blocking. */
    fun update(sa: ServiceAccount, pass: Pass, theme: PassTheme, settings: Settings) {
        val body = passObject(pass, theme, settings, pass.walletObjectId, pass.walletClassId).toString()
        val r = Http.request("PUT", OBJECTS + URLEncoder.encode(pass.walletObjectId, "UTF-8"), body, bearer = GoogleAuth.accessToken(sa))
        when (r.code) {
            in 200..299 -> Unit
            404 -> throw WalletNotFound()
            else -> throw WalletException(Http.errorOf(r))
        }
    }

    /**
     * Checks the key works and is allowed to make passes for [issuerId]: returns the issuer account's name.
     * Blocking.
     */
    fun check(sa: ServiceAccount, issuerId: String): String {
        GoogleAuth.forget()
        val token = GoogleAuth.accessToken(sa)
        val id = issuerId.trim()
        if (id.isEmpty() || !id.all { it.isDigit() }) throw WalletException("The key works. Now add your Issuer ID (only digits).")
        val r = Http.request("GET", "https://walletobjects.googleapis.com/walletobjects/v1/issuer/$id", bearer = token)
        if (r.code != 200) throw WalletException(Http.errorOf(r))
        return runCatching { JSONObject(r.body).optString("name") }.getOrNull().orEmpty().ifBlank { "your issuer account" }
    }
}
