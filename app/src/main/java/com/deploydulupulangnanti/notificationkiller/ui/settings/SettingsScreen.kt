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
    val quiet = repo.isQuietHoursEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val retention = repo.retentionDays.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    fun setQuiet(q: Boolean) = viewModelScope.launch { repo.setQuietHours(q) }
    fun setRet(d: Int) = viewModelScope.launch { repo.setRetentionDays(d) }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val quiet by viewModel.quiet.collectAsState()
    val ret by viewModel.retention.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Pengaturan & Privasi", style = MaterialTheme.typography.titleLarge)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Quiet Hours", style = MaterialTheme.typography.titleMedium)
                Switch(checked = quiet, onCheckedChange = { viewModel.setQuiet(it) })
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Retensi Riwayat: $ret Hari", style = MaterialTheme.typography.titleMedium)
                Slider(value = ret.toFloat(), onValueChange = { viewModel.setRet(it.toInt()) }, valueRange = 1f..30f)
            }
        }
    }
}
