package app.passstudio.wallet

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.passstudio.data.Settings
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Logos picked on the phone: sized for Google Wallet and uploaded to the logos/ folder of the public GitHub repo,
 * because Google Wallet only loads pictures from a public link. All calls block: use a background thread.
 */
object LogoUpload {
    /** The two logo spots on a pass, with the most Google Wallet needs for each. */
    enum class Kind(val maxW: Int, val maxH: Int, val label: String) {
        WIDE(1280, 400, "wide"),
        ROUND(660, 660, "round"),
    }

    private val githubHeaders = mapOf("Accept" to "application/vnd.github+json", "X-GitHub-Api-Version" to "2022-11-28")

    fun hasToken(context: Context) = KeyVault.has(context, KeyVault.GITHUB_TOKEN)

    /** Shrinks the picture to fit its spot (never enlarges it) and keeps its transparency, as a PNG. */
    fun prepare(bytes: ByteArray, kind: Kind): ByteArray {
        val src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw WalletException("That file isn't a picture. Pick a PNG.")
        val scale = minOf(1f, kind.maxW.toFloat() / src.width, kind.maxH.toFloat() / src.height)
        val bmp = if (scale < 1f) {
            Bitmap.createScaledBitmap(src, (src.width * scale).toInt().coerceAtLeast(1), (src.height * scale).toInt().coerceAtLeast(1), true)
        } else {
            src
        }
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }

    private fun repoOf(settings: Settings): String {
        val repo = settings.githubRepo.trim().trim('/')
        if (!Regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$").matches(repo)) {
            throw WalletException("Check the repo in Settings. It looks like Jitterzz/wallet-pass.")
        }
        return repo
    }

    private fun token(context: Context): String = KeyVault.load(context, KeyVault.GITHUB_TOKEN)?.trim().orEmpty()
        .ifEmpty { throw WalletException("Add your GitHub token in Settings to upload logos.") }

    /** Uploads [png] as logos/[name] and returns its public https link. */
    fun upload(context: Context, settings: Settings, png: ByteArray, name: String): String {
        val token = token(context)
        val repo = repoOf(settings)
        val branch = settings.githubBranch.trim().ifBlank { "main" }
        val path = "logos/$name"
        val body = JSONObject()
            .put("message", "Add logo $name (uploaded from Pass Studio)")
            .put("content", Base64.getEncoder().encodeToString(png))
            .put("branch", branch)
            .toString()
        val r = Http.request("PUT", "https://api.github.com/repos/$repo/contents/$path", body, bearer = token, headers = githubHeaders)
        if (r.code !in 200..299) throw WalletException(githubError(r))
        val link = runCatching { JSONObject(r.body).getJSONObject("content").optString("download_url") }.getOrNull()
        return link?.takeIf { it.startsWith("https://") } ?: "https://raw.githubusercontent.com/$repo/$branch/$path"
    }

    /** Checks the token reaches the repo. Says so when the repo is private (Google Wallet couldn't load the logos). */
    fun check(context: Context, settings: Settings): String {
        val token = token(context)
        val repo = repoOf(settings)
        val r = Http.request("GET", "https://api.github.com/repos/$repo", bearer = token, headers = githubHeaders)
        if (r.code != 200) throw WalletException(githubError(r))
        val private = runCatching { JSONObject(r.body).optBoolean("private", false) }.getOrDefault(false)
        return if (private) "The token works, but $repo is private. Make it public so Google Wallet can load the pictures."
        else "Connected to $repo. Logo uploads are ready."
    }

    private fun githubError(r: Http.Response): String {
        val msg = runCatching { JSONObject(r.body).optString("message") }.getOrNull().orEmpty()
        val detail = if (msg.isNotBlank()) ": $msg" else "."
        return when (r.code) {
            401 -> "GitHub didn't accept the token. Check it in Settings."
            403 -> "The token can't write to that repo. Give it Contents: Read and write$detail"
            404 -> "GitHub can't find that repo with this token. Check the repo name and the token's access."
            422 -> "GitHub turned the upload down$detail"
            else -> "GitHub error ${r.code}$detail"
        }
    }
}
