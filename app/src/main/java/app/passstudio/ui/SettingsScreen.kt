package app.passstudio.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.passstudio.BuildConfig
import app.passstudio.data.Catalog
import app.passstudio.data.Store
import app.passstudio.wallet.GoogleAuth
import app.passstudio.wallet.KeyVault
import app.passstudio.wallet.ServiceAccount
import app.passstudio.wallet.Sync
import app.passstudio.wallet.WalletApi
import app.passstudio.wallet.WalletException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val d by Store.data.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var issuer by remember { mutableStateOf(d.settings.issuerId) }
    var artBase by remember { mutableStateOf(d.settings.artBase) }
    var keyEmail by remember { mutableStateOf(ServiceAccount.load(context)?.clientEmail) }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        val m = message
        if (m != null) {
            snackbar.showSnackbar(m)
            message = null
        }
    }

    fun saveFields() {
        Store.update {
            it.copy(settings = it.settings.copy(
                issuerId = issuer.trim(),
                artBase = artBase.trim().ifBlank { Catalog.DEFAULT_ART_BASE },
            ))
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            }.getOrNull()
            if (text == null) {
                message = "Couldn't open that file."
            } else {
                try {
                    val sa = ServiceAccount.parse(text)
                    KeyVault.save(context, text)
                    GoogleAuth.forget()
                    keyEmail = sa.clientEmail
                    message = "Key added. Tap Check connection to test it."
                } catch (e: WalletException) {
                    message = e.message
                } catch (e: Exception) {
                    message = "Couldn't keep that key on this phone."
                }
            }
        }
    }

    BackHandler {
        saveFields()
        onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackBar("Settings", onBack = {
                saveFields()
                onBack()
            })
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).imePadding().verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 40.dp),
        ) {
            SectionTitle("Google Wallet")
            Panel {
                OutlinedTextField(
                    value = issuer,
                    onValueChange = { v -> issuer = v.filter { it.isDigit() } },
                    label = { Text("Issuer ID") },
                    supportingText = { Text("From the Google Pay & Wallet Console") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Google key", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    keyEmail?.let { "Added: $it" }
                        ?: "Not added yet. Pick the .json key file of your Google Cloud service account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(onClick = { picker.launch(arrayOf("application/json", "text/plain", "application/octet-stream", "*/*")) }) {
                        Text(if (keyEmail == null) "Add key file" else "Replace key")
                    }
                    if (keyEmail != null) {
                        TextButton(onClick = {
                            KeyVault.clear(context)
                            GoogleAuth.forget()
                            keyEmail = null
                            message = "Key removed from this phone."
                        }) { Text("Remove") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(enabled = !busy, onClick = {
                        saveFields()
                        val sa = ServiceAccount.load(context)
                        if (sa == null) {
                            message = "Add the key file first."
                        } else {
                            busy = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) { runCatching { WalletApi.check(sa, issuer) } }
                                busy = false
                                message = result.fold(
                                    onSuccess = { name -> "Connected to $name. You're ready to add passes." },
                                    onFailure = { it.message ?: "Couldn't connect." },
                                )
                            }
                        }
                    }) { Text("Check connection") }
                    Spacer(Modifier.width(12.dp))
                    if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                }
                Text(
                    "The key stays on this phone, locked with Android's keystore and left out of backups. It lets this app " +
                        "make and update passes for your issuer account, so don't share the file.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SectionTitle("Automatic updates")
            Panel {
                Text(
                    "A few times a day, and whenever you open the app, passes in Google Wallet are checked and moved to " +
                        "the theme for the day (seasons and holidays), along with any edits.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                FilledTonalButton(enabled = !busy, onClick = {
                    saveFields()
                    busy = true
                    scope.launch {
                        val problems = withContext(Dispatchers.IO) {
                            Sync.checkPending(context.applicationContext)
                            Sync.syncAll(context.applicationContext, force = true)
                        }
                        busy = false
                        message = if (problems == 0) "Everything's up to date." else "$problems pass(es) couldn't be updated. Open them to see why."
                    }
                }) { Text("Update all passes now") }
            }

            SectionTitle("Theme art")
            Panel {
                OutlinedTextField(
                    value = artBase,
                    onValueChange = { artBase = it },
                    label = { Text("Art link") },
                    supportingText = { Text("Google Wallet loads each theme's picture from here. The repo has to be public.") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { artBase = Catalog.DEFAULT_ART_BASE }) { Text("Reset") }
            }

            SectionTitle("How to set it up")
            Panel {
                listOf(
                    "In the Google Pay & Wallet Console (pay.google.com/business/console), make an issuer account for the " +
                        "Google Wallet API. Copy its Issuer ID into the box above.",
                    "Still there, under Set up test accounts, add the Google account on this phone. (New issuer accounts " +
                        "start in demo mode, where only test accounts can save passes. That's all you need.)",
                    "In Google Cloud (console.cloud.google.com), make a project, turn on the Google Wallet API, and make a " +
                        "service account. Under its Keys, add a JSON key: the file downloads to this phone.",
                    "Back in the Wallet Console, under Users, invite the service account's email as a Developer.",
                    "Tap Add key file above and pick that file, then Check connection.",
                ).forEachIndexed { i, step ->
                    Row {
                        Text("${i + 1}.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(24.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Text(
                "Pass Studio ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 20.dp, start = 4.dp),
            )
        }
    }
}
