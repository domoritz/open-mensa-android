package de.uni_potsdam.hpi.openmensa.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.fragment.app.FragmentActivity
import de.uni_potsdam.hpi.openmensa.BuildConfig
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.ui.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : FragmentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = SettingsUtils.with(this)
        val database = AppDatabase.with(this)
        val snackbarHostState = SnackbarHostState()

        suspend fun open(uri: String) {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            } catch (ex: ActivityNotFoundException) {
                snackbarHostState.showSnackbar(getString(R.string.settings_toast_no_app_found))
            }
        }

        enableEdgeToEdge()

        setContent {
            val scope = rememberCoroutineScope()
            val currentServerUrl by settings.settingsFlow.map { it.sourceUrl }.collectAsState(null)
            var editUrlDialogContent by rememberSaveable { mutableStateOf(null as String?) }

            Theme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(stringResource(R.string.menu_preferences))
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { finish() }
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        stringResource(R.string.settings_back)
                                    )
                                }
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    content = { insets ->
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(insets)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card {
                                Column (
                                    Modifier.fillMaxWidth().padding(8.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.app_name),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(stringResource(
                                        R.string.settings_version,
                                        BuildConfig.VERSION_NAME
                                    ))
                                }
                            }

                            Card (
                                Modifier.clickable {
                                    editUrlDialogContent = currentServerUrl ?: ""
                                }
                            ) {
                                Row (
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column (Modifier.weight(1f)) {
                                        Text(
                                            stringResource(R.string.source_url_title),
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            stringResource(R.string.source_url_desc)
                                        )

                                        currentServerUrl?.let {
                                            if (it.isNotBlank()) {
                                                Text(it)
                                            }
                                        }
                                    }

                                    Icon(
                                        Icons.Default.Edit,
                                        stringResource(R.string.settings_icon_edit)
                                    )
                                }
                            }

                            Card (
                                Modifier.clickable {
                                    scope.launch {
                                        open("https://openmensa.org/")
                                    }
                                }
                            ) {
                                Row (
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column (Modifier.weight(1f)) {
                                        Text(
                                            stringResource(R.string.powered_by_title),
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            stringResource(R.string.powered_by_desc)
                                        )
                                    }

                                    Icon(
                                        Icons.Default.OpenInBrowser,
                                        stringResource(R.string.settings_icon_open_in_browser)
                                    )
                                }
                            }

                            Card (
                                Modifier.clickable {
                                    scope.launch {
                                        open("https://github.com/domoritz/open-mensa-android")
                                    }
                                }
                            ) {
                                Row (
                                    Modifier.fillMaxWidth().padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column (Modifier.weight(1f)) {
                                        Text(
                                            stringResource(R.string.settings_authors_and_license),
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Text(
                                            stringResource(R.string.license)
                                        )

                                        Text(
                                            stringResource(R.string.author_desc)
                                        )
                                    }

                                    Icon(
                                        Icons.Default.OpenInBrowser,
                                        stringResource(R.string.settings_icon_open_in_browser)
                                    )
                                }
                            }
                        }
                    }
                )

                if (editUrlDialogContent != null) AlertDialog(
                    title = { Text(stringResource(R.string.source_url_title)) },
                    text = {
                        TextField(
                            value = editUrlDialogContent ?: "",
                            onValueChange = { editUrlDialogContent = it }
                        )
                    },
                    onDismissRequest = { editUrlDialogContent = null },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val url = editUrlDialogContent
                                val doneMessage = getString(R.string.settings_server_updated_toast)

                                if (url != null) {
                                    editUrlDialogContent = null

                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            database.canteen.deleteAllItems()

                                            settings.sourceUrl = url
                                            settings.lastCanteenListUpdate = 0

                                            snackbarHostState.showSnackbar(doneMessage)
                                        }
                                    }
                                }
                            }
                        ) {
                            Text(stringResource(R.string.settings_save_btn))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { editUrlDialogContent = null }
                        ) {
                            Text(stringResource(R.string.settings_cancel_btn))
                        }
                    }
                )
            }
        }
    }
}