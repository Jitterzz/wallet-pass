package app.passstudio.wallet

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Keeps secrets on the phone (the Google service account key, the GitHub token for logo uploads), encrypted with a
 * key from Android's keystore, which never leaves the phone. The files are excluded from backups (see the manifest).
 */
object KeyVault {
    private const val ALIAS = "pass_studio_google_key"
    private const val FILE = "google_key.bin"

    /** The GitHub token used to upload logos. */
    const val GITHUB_TOKEN = "github_token.bin"

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    private fun file(context: Context, name: String) = File(context.applicationContext.filesDir, name)

    fun has(context: Context, name: String = FILE): Boolean = file(context, name).exists()

    fun save(context: Context, text: String, name: String = FILE) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val sealed = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        file(context, name).writeBytes(byteArrayOf(iv.size.toByte()) + iv + sealed)
    }

    fun load(context: Context, name: String = FILE): String? {
        val f = file(context, name)
        if (!f.exists()) return null
        return runCatching {
            val bytes = f.readBytes()
            val ivSize = bytes[0].toInt()
            val iv = bytes.copyOfRange(1, 1 + ivSize)
            val sealed = bytes.copyOfRange(1 + ivSize, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(sealed), Charsets.UTF_8)
        }.getOrNull()
    }

    fun clear(context: Context, name: String = FILE) {
        file(context, name).delete()
    }
}
