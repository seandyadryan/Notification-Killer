package com.deploydulupulangnanti.notificationkiller.ui.appfilter

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
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
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(list, key = { it.packageName }) { app ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(app.appName, style = MaterialTheme.typography.titleMedium)
                        Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { viewModel.toggleWhitelist(app, !app.isWhitelisted) }) {
                        Icon(Icons.Default.Security, contentDescription = "Whitelist", tint = if (app.isWhitelisted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = app.isBlocked, onCheckedChange = { viewModel.toggleBlock(app, it) })
                }
            }
        }
    }
}
