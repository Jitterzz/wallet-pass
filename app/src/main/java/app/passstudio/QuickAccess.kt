package app.passstudio

import android.app.PendingIntent
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import app.passstudio.data.Pass
import app.passstudio.data.Store

/** Ways to get to a pass fast: a quick settings tile and a home-screen shortcut. */
object QuickAccess {
    /** Opens Pass Studio straight to [passId] (or to the list when it's null). */
    fun showPassIntent(context: Context, passId: String?): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (passId != null) putExtra(MainActivity.EXTRA_SHOW_PASS, passId)
        }

    /** Asks the launcher to put a shortcut to [pass] on the home screen. False if the launcher can't. */
    fun pinShortcut(context: Context, pass: Pass): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) return false
        val info = ShortcutInfoCompat.Builder(context, "pass_${pass.id}")
            .setShortLabel(pass.title.ifBlank { "My pass" }.take(24))
            .setLongLabel(pass.title.ifBlank { "My pass" })
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(showPassIntent(context, pass.id))
            .build()
        return ShortcutManagerCompat.requestPinShortcut(context, info, null)
    }

    /**
     * Asks Android to add the quick settings tile (Android 13 and newer show a prompt). [onResult] gets a line for
     * the person.
     */
    fun requestTile(context: Context, onResult: (String) -> Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val bar = context.getSystemService(StatusBarManager::class.java)
            if (bar == null) {
                onResult(MANUAL_TILE)
                return
            }
            bar.requestAddTileService(
                ComponentName(context, PassTileService::class.java),
                context.getString(R.string.tile_label),
                Icon.createWithResource(context, R.drawable.ic_tile),
                context.mainExecutor,
                java.util.function.Consumer<Int> { result ->
                    onResult(
                        when (result) {
                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> "Tile added. Swipe down twice to find it."
                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> "The tile's already in your quick settings."
                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> "Tile not added."
                            else -> MANUAL_TILE
                        }
                    )
                },
            )
        } else {
            onResult(MANUAL_TILE)
        }
    }

    private const val MANUAL_TILE =
        "Swipe down twice, tap the pencil to edit your quick settings, and drag in the Pass Studio tile."
}

/** The quick settings tile: one tap opens the starred pass, full screen. */
class PassTileService : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        Store.init(this)
        val pass = Store.quickPass()
        qsTile?.let { tile ->
            tile.label = pass?.title?.ifBlank { null } ?: getString(R.string.tile_label)
            if (Build.VERSION.SDK_INT >= 29) tile.subtitle = if (pass != null) getString(R.string.tile_label) else null
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        Store.init(this)
        val intent = QuickAccess.showPassIntent(this, Store.quickPass()?.id)
        val open = Runnable {
            if (Build.VERSION.SDK_INT >= 34) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                )
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }
        if (isLocked) unlockAndRun(open) else open.run()
    }
}
