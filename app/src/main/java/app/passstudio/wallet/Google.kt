package app.passstudio.wallet

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

/** Something went wrong talking to Google; [message] is written for the person using the app. */
open class WalletException(message: String) : Exception(message)

/** The pass is gone from Google Wallet (or was never saved). */
class WalletNotFound : WalletException("This pass isn't in Google Wallet anymore. Add it again to keep it updated.")

/** A Google Cloud service account key file: the account's email and its private key. */
class ServiceAccount(
    val clientEmail: String,
    private val privateKeyPem: String,
    val keyId: String,
    val tokenUri: String,
) {
    val privateKey: PrivateKey by lazy {
        val body = privateKeyPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace(Regex("\\s"), "")
        KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(Base64.getDecoder().decode(body)))
    }

    companion object {
        /** Reads a key file's contents; throws [WalletException] with a friendly message if it isn't one. */
        fun parse(json: String): ServiceAccount {
            val o = try {
                JSONObject(json)
            } catch (e: Exception) {
                throw WalletException("That file isn't a key file. Pick the .json key you downloaded from Google Cloud.")
            }
            val email = o.optString("client_email")
            val pem = o.optString("private_key")
            if (email.isBlank() || !pem.contains("PRIVATE KEY")) {
                throw WalletException("That file isn't a service account key. Pick the .json key you downloaded from Google Cloud.")
            }
            val sa = ServiceAccount(
                clientEmail = email,
                privateKeyPem = pem,
                keyId = o.optString("private_key_id"),
                tokenUri = o.optString("token_uri").ifBlank { "https://oauth2.googleapis.com/token" },
            )
            try {
                sa.privateKey
            } catch (e: Exception) {
                throw WalletException("The key in that file couldn't be read.")
            }
            return sa
        }

        fun load(context: Context): ServiceAccount? = KeyVault.load(context)?.let { runCatching { parse(it) }.getOrNull() }
    }
}

/** JSON Web Tokens signed with the service account's key (RS256). */
object Jwt {
    private val enc = Base64.getUrlEncoder().withoutPadding()

    fun sign(claims: JSONObject, sa: ServiceAccount): String {
        val header = JSONObject().put("alg", "RS256").put("typ", "JWT")
        if (sa.keyId.isNotBlank()) header.put("kid", sa.keyId)
        val head = enc.encodeToString(header.toString().toByteArray(Charsets.UTF_8))
        val body = enc.encodeToString(claims.toString().toByteArray(Charsets.UTF_8))
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(sa.privateKey)
        signer.update("$head.$body".toByteArray(Charsets.US_ASCII))
        return "$head.$body.${enc.encodeToString(signer.sign())}"
    }
}

/** Plain HTTPS calls (blocking: call them off the main thread). */
object Http {
    class Response(val code: Int, val body: String)

    fun request(
        method: String,
        url: String,
        body: String? = null,
        contentType: String? = null,
        bearer: String? = null,
        headers: Map<String, String> = emptyMap(),
    ): Response {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 15_000
            c.readTimeout = 20_000
            c.setRequestProperty("Accept", "application/json")
            if (bearer != null) c.setRequestProperty("Authorization", "Bearer $bearer")
            headers.forEach { (k, v) -> c.setRequestProperty(k, v) }
            if (body != null) {
                c.doOutput = true
                c.setRequestProperty("Content-Type", contentType ?: "application/json; charset=utf-8")
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            return Response(code, text)
        } catch (e: java.io.IOException) {
            throw WalletException("Couldn't connect. Check your internet connection and try again.")
        } finally {
            c.disconnect()
        }
    }

    /** Google's own error message out of a failed response, when it has one. */
    fun errorOf(r: Response): String {
        val msg = runCatching {
            val o = JSONObject(r.body)
            o.optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
                ?: o.optString("error_description").takeIf { it.isNotBlank() }
                ?: o.optString("error").takeIf { it.isNotBlank() }
        }.getOrNull()
        return when {
            r.code == 401 || r.code == 403 ->
                "Google said no (${r.code}). Check that the key's email is a Developer user in the Google Pay & Wallet Console" +
                    (msg?.let { ": $it" } ?: ".")
            msg != null -> "Google Wallet: $msg (${r.code})"
            else -> "Google Wallet answered with error ${r.code}."
        }
    }
}

/** Short-lived access tokens for the Google Wallet API, made from the service account key. */
object GoogleAuth {
    private const val SCOPE = "https://www.googleapis.com/auth/wallet_object.issuer"
    private var token: String? = null
    private var tokenEmail: String? = null
    private var expiresAt = 0L

    @Synchronized
    fun accessToken(sa: ServiceAccount): String {
        val now = System.currentTimeMillis() / 1000
        val cached = token
        if (cached != null && tokenEmail == sa.clientEmail && now < expiresAt - 60) return cached
        val claims = JSONObject()
            .put("iss", sa.clientEmail)
            .put("scope", SCOPE)
            .put("aud", sa.tokenUri)
            .put("iat", now)
            .put("exp", now + 3600)
        val form = "grant_type=" + URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", "UTF-8") +
            "&assertion=" + URLEncoder.encode(Jwt.sign(claims, sa), "UTF-8")
        val r = Http.request("POST", sa.tokenUri, form, "application/x-www-form-urlencoded")
        if (r.code != 200) throw WalletException(Http.errorOf(r))
        val o = JSONObject(r.body)
        val fresh = o.optString("access_token")
        if (fresh.isBlank()) throw WalletException("Google didn't send back an access token.")
        token = fresh
        tokenEmail = sa.clientEmail
        expiresAt = now + o.optLong("expires_in", 3600)
        return fresh
    }

    @Synchronized
    fun forget() {
        token = null
        tokenEmail = null
        expiresAt = 0
    }
}
