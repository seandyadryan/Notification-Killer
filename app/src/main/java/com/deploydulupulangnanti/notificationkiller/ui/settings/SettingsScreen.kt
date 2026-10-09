package com.deploydulupulangnanti.notificationkiller.ui.settings

import android.app.Application
import androidx.compose.foundation.layout.*
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

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    val retention = repo.retentionDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    fun setRetention(days: Int) = viewModelScope.launch {
        repo.setRetentionDays(days)
        repo.purgeOldHistory(days)
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val retention by viewModel.retention.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings & privacy", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("On-device processing", style = MaterialTheme.typography.titleMedium)
                Text("Notification text is read only in memory to evaluate your rules. History stores the app, time, matched rule and action, never notification text. No notification data is sent to a server.")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("History retention: $retention days", style = MaterialTheme.typography.titleMedium)
                Slider(value = retention.toFloat(), onValueChange = { viewModel.setRetention(it.toInt().coerceIn(1, 30)) }, valueRange = 1f..30f, steps = 28)
                Text("Older activity metadata is removed when the setting changes and when the app starts.", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Android limits", style = MaterialTheme.typography.titleMedium)
                Text("Notification Access must be enabled by you in Android settings. Android decides which notifications can be dismissed; a dismissal request is not a guarantee that every notification type can be removed. Quiet Hours is not enabled because it would require changing the device’s global Do Not Disturb policy.")
            }
        }
    }
}
