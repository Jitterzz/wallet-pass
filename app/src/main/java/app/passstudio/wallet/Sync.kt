package app.passstudio.wallet

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.passstudio.data.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Keeps passes in Google Wallet looking the way the app says they should. All calls block: use a background thread. */
object Sync {
    /**
     * Brings one pass in Google Wallet up to date: its theme for today, plus any edits ([force] sends it even when
     * nothing seems to have changed). Returns an error message for the person, or null when all is well.
     */
    fun syncPass(context: Context, passId: String, force: Boolean): String? {
        Store.init(context)
        val d = Store.data.value
        val pass = d.passes.firstOrNull { it.id == passId } ?: return null
        if (!pass.inWallet || pass.walletObjectId.isBlank()) return null
        val theme = Store.themeFor(pass, d)
        if (!force && !pass.dirty && pass.appliedThemeId == theme.id) return null
        val sa = ServiceAccount.load(context)
            ?: return "Add your Google key in Settings first.".also { msg -> Store.updatePass(passId) { it.copy(lastError = msg) } }
        return try {
            WalletApi.update(sa, pass, theme, d.settings)
            Store.updatePass(passId) {
                it.copy(appliedThemeId = theme.id, dirty = false, lastSync = System.currentTimeMillis(), lastError = "")
            }
            null
        } catch (e: WalletNotFound) {
            Store.updatePass(passId) { it.copy(inWallet = false, lastError = e.message ?: "") }
            e.message
        } catch (e: Exception) {
            val msg = e.message ?: "Couldn't reach Google Wallet."
            Store.updatePass(passId) { it.copy(lastError = msg) }
            msg
        }
    }

    /** Updates every pass that's in Google Wallet and due for a change. Returns how many had a problem. */
    fun syncAll(context: Context, force: Boolean = false): Int {
        Store.init(context)
        return Store.data.value.passes.filter { it.inWallet }.count { syncPass(context, it.id, force) != null }
    }

    /**
     * Passes waiting to be saved: once one shows up in Google Wallet, it's marked as in there. Returns the
     * names of the ones found.
     */
    fun checkPending(context: Context): List<String> {
        Store.init(context)
        val waiting = Store.data.value.passes.filter { it.pending && it.walletObjectId.isNotBlank() }
        if (waiting.isEmpty()) return emptyList()
        val sa = ServiceAccount.load(context) ?: return emptyList()
        val found = mutableListOf<String>()
        for (p in waiting) {
            val there = runCatching { WalletApi.exists(sa, p.walletObjectId) }.getOrNull() ?: continue
            if (there) {
                Store.updatePass(p.id) { it.copy(inWallet = true, pending = false, lastError = "", lastSync = System.currentTimeMillis()) }
                found += p.title.ifBlank { "Your pass" }
            }
        }
        // A pass saved on an older day may need today's theme straight away.
        if (found.isNotEmpty()) syncAll(context)
        return found
    }
}

/** Runs [Sync] in the background: a few times a day, and whenever the app opens. */
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Sync.checkPending(applicationContext)
        Sync.syncAll(applicationContext)
        Result.success()
    }

    companion object {
        private val online = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        /** Checks every 6 hours, so a new season or holiday shows up within a few hours of midnight. */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS).setConstraints(online).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork("theme-sync", ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(online).build()
            WorkManager.getInstance(context).enqueueUniqueWork("theme-sync-now", ExistingWorkPolicy.REPLACE, request)
        }
    }
}
