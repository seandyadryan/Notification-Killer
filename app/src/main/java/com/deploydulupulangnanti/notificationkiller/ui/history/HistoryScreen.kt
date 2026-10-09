package com.deploydulupulangnanti.notificationkiller.ui.history

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    val history = repo.getHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun clear() = viewModelScope.launch { repo.clearHistory() }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val list by viewModel.history.collectAsState()
    Scaffold(topBar = {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Riwayat Pembersihan", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { viewModel.clear() }) { Icon(Icons.Default.DeleteSweep, null) }
        }
    }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.appName, style = MaterialTheme.typography.titleSmall)
                        Text(item.titlePreview, style = MaterialTheme.typography.bodyMedium)
                        Text("${item.actionTaken} • ${item.matchedRule}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
