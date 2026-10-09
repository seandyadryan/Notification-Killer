package com.deploydulupulangnanti.notificationkiller.ui.appfilter

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.R
import com.deploydulupulangnanti.notificationkiller.domain.model.AppFilter
import com.deploydulupulangnanti.notificationkiller.util.AppInstalledHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppFilterViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    private val _installed = MutableStateFlow<List<AppFilter>>(emptyList())
    val apps = combine(_installed, repo.getAppFilters()) { inst, saved ->
        val map = saved.associateBy { it.packageName }
        inst.map { map[it.packageName] ?: it }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _installed.value = AppInstalledHelper.getInstalledLaunchableApps(getApplication()).map {
                AppFilter(it.packageName, it.appName)
            }
        }
    }
    fun toggleBlock(app: AppFilter, b: Boolean) = viewModelScope.launch { repo.saveAppFilter(app.copy(isBlocked = b, isWhitelisted = if (b) false else app.isWhitelisted)) }
    fun toggleWhitelist(app: AppFilter, w: Boolean) = viewModelScope.launch { repo.saveAppFilter(app.copy(isWhitelisted = w, isBlocked = if (w) false else app.isBlocked)) }
}

@Composable
fun AppFilterScreen(viewModel: AppFilterViewModel) {
    val list by viewModel.apps.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedOnly by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val visible = list.filter { it.packageName != context.packageName }
        .filter { !selectedOnly || it.isBlocked || it.isWhitelisted }
        .filter { query.isBlank() || it.appName.contains(query, true) || it.packageName.contains(query, true) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(stringResource(R.string.app_filters_title), style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search_apps)) }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
            FilterChip(selected = selectedOnly, onClick = { selectedOnly = !selectedOnly }, label = { Text(stringResource(R.string.selected_only)) })
        }
        items(visible, key = { it.packageName }) { app ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val icon = remember(app.packageName) {
                        runCatching { context.packageManager.getApplicationIcon(app.packageName).toBitmap().asImageBitmap() }.getOrNull()
                    }
                    if (icon != null) Image(icon, contentDescription = "${app.appName} icon", modifier = Modifier.size(40.dp).padding(end = 8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.appName, style = MaterialTheme.typography.titleMedium)
                        Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                        Text(stringResource(R.string.dismiss_count, app.cleanedCount), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { viewModel.toggleWhitelist(app, !app.isWhitelisted) }) {
                            Icon(Icons.Default.Security, contentDescription = stringResource(if (app.isWhitelisted) R.string.unprotect_app else R.string.protect_app, app.appName), tint = if (app.isWhitelisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        }
                        Text(stringResource(R.string.protect), style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(checked = app.isBlocked, onCheckedChange = { viewModel.toggleBlock(app, it) }, enabled = !app.isWhitelisted)
                        Text(stringResource(R.string.filter), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        if (visible.isEmpty()) item { Text(stringResource(R.string.no_apps_found), modifier = Modifier.padding(16.dp)) }
    }
}
