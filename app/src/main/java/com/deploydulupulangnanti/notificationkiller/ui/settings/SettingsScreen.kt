package com.deploydulupulangnanti.notificationkiller.ui.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.R
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
        Text(stringResource(R.string.settings_privacy_title), style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.on_device_processing), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.privacy_details))
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.retention_days, retention), style = MaterialTheme.typography.titleMedium)
                Slider(value = retention.toFloat(), onValueChange = { viewModel.setRetention(it.toInt().coerceIn(1, 30)) }, valueRange = 1f..30f, steps = 28)
                Text(stringResource(R.string.retention_details), style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.android_limits), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.android_limits_details))
            }
        }
    }
}
