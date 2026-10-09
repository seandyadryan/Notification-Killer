package com.deploydulupulangnanti.notificationkiller.ui.history

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    val history = repo.getHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun clear() = viewModelScope.launch { repo.clearHistory() }
}

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val list by viewModel.history.collectAsState()
    var query by remember { mutableStateOf("") }
    var appFilter by remember { mutableStateOf("") }
    var clearConfirmation by remember { mutableStateOf(false) }
    val apps = list.map { it.appName }.distinct().sorted()
    val visible = list.filter { row ->
        (appFilter.isBlank() || row.appName == appFilter) &&
            (query.isBlank() || row.appName.contains(query, true) || row.packageName.contains(query, true) || row.matchedRule.contains(query, true) || row.actionTaken.contains(query, true))
    }
    Scaffold(topBar = {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Notification history", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { clearConfirmation = true }, enabled = list.isNotEmpty()) { Icon(Icons.Default.DeleteSweep, "Hapus semua riwayat") }
        }
    }) { pad ->
        Column(modifier = Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp)) {
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Cari aplikasi atau aturan") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
            if (apps.isNotEmpty()) {
                PrimaryScrollableTabRow(selectedTabIndex = (listOf("") + apps).indexOf(appFilter).coerceAtLeast(0), edgePadding = 0.dp) {
                    Tab(selected = appFilter.isBlank(), onClick = { appFilter = "" }, text = { Text("All") })
                    apps.forEach { name -> Tab(selected = appFilter == name, onClick = { appFilter = name }, text = { Text(name, maxLines = 1) }) }
                }
            }
            if (visible.isEmpty()) {
                Text("Belum ada aktivitas untuk filter ini. Isi notifikasi tidak disimpan.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visible, key = { it.eventId }) { item -> HistoryRow(item) }
                }
            }
        }
    }
    if (clearConfirmation) AlertDialog(
        onDismissRequest = { clearConfirmation = false },
        title = { Text("Hapus riwayat lokal?") },
        text = { Text("Metadata aktivitas yang tersimpan di perangkat akan dihapus. Notifikasi yang sudah ditutup tidak dapat dipulihkan dari sini.") },
        confirmButton = { TextButton(onClick = { viewModel.clear(); clearConfirmation = false }) { Text("Hapus") } },
        dismissButton = { TextButton(onClick = { clearConfirmation = false }) { Text("Batal") } }
    )
}

@Composable
private fun HistoryRow(item: HistoryEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(item.appName, style = MaterialTheme.typography.titleSmall)
            Text(item.packageName, style = MaterialTheme.typography.labelSmall)
            Text(if (item.matchedRule.isBlank()) item.actionTaken else "${item.actionTaken} • ${item.matchedRule}", style = MaterialTheme.typography.bodyMedium)
            Text("${item.result} • ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(item.timestamp))}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
