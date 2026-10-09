package com.deploydulupulangnanti.notificationkiller.ui.dashboard

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.data.local.entity.HistoryEntity
import com.deploydulupulangnanti.notificationkiller.service.NotificationAccessHelper
import com.deploydulupulangnanti.notificationkiller.service.ListenerRuntime
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isAccessGranted: Boolean = false,
    val isAutoCleanEnabled: Boolean = false,
    val todayCleanedCount: Int = 0,
    val todayDetectedCount: Int = 0,
    val filteredAppsCount: Int = 0,
    val activeRulesCount: Int = 0,
    val recentActivities: List<HistoryEntity> = emptyList()
)

class DashboardViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    private val _access = MutableStateFlow(false)

    private val baseState = combine(_access, repo.isAutoCleanEnabled, repo.getTodayCleanedCount()) { access, autoClean, cleaned ->
        Triple(access, autoClean, cleaned)
    }
    val uiState: StateFlow<DashboardUiState> = combine(
        baseState, repo.getTodayDetectedCount(), repo.getAppFilters(), repo.getRules(), repo.getHistory()
    ) { base, detected, apps, rules, history ->
        DashboardUiState(base.first, base.second, base.third, detected, apps.count { it.isBlocked }, rules.count { it.isEnabled }, history.take(5))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun refresh() { _access.value = NotificationAccessHelper.isNotificationAccessGranted(getApplication()) && ListenerRuntime.isConnected }
    fun toggleAutoClean(enabled: Boolean) = viewModelScope.launch { repo.setAutoClean(enabled) }
}

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = if (state.isAccessGranted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (state.isAccessGranted) "Notification Access: Connected" else "Notification Access: Not Connected", style = MaterialTheme.typography.titleMedium)
                    Text("Akses ini memungkinkan aplikasi membaca judul dan teks notifikasi sementara untuk mengevaluasi aturan. Isi tidak disimpan atau dikirim; riwayat hanya menyimpan metadata aktivitas.", style = MaterialTheme.typography.bodyMedium)
                    if (!state.isAccessGranted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { context.startActivity(NotificationAccessHelper.createNotificationAccessSettingsIntent()) }) {
                            Text("Buka Pengaturan Akses")
                        }
                    }
                }
            }
        }
        item {
            Card {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Auto-Clean", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = state.isAutoCleanEnabled, onCheckedChange = { viewModel.toggleAutoClean(it) })
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f)) { Column(modifier = Modifier.padding(12.dp)) { Text("${state.todayDetectedCount}", style = MaterialTheme.typography.headlineMedium); Text("Terdeteksi Hari Ini") } }
                Card(modifier = Modifier.weight(1f)) { Column(modifier = Modifier.padding(12.dp)) { Text("${state.todayCleanedCount}", style = MaterialTheme.typography.headlineMedium); Text("Permintaan Hapus") } }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f)) { Column(modifier = Modifier.padding(12.dp)) { Text("${state.filteredAppsCount}", style = MaterialTheme.typography.headlineMedium); Text("Aplikasi difilter") } }
                Card(modifier = Modifier.weight(1f)) { Column(modifier = Modifier.padding(12.dp)) { Text("${state.activeRulesCount}", style = MaterialTheme.typography.headlineMedium); Text("Aturan aktif") } }
            }
        }
        item { Text("Aktivitas Terbaru", style = MaterialTheme.typography.titleMedium) }
        items(state.recentActivities) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.appName, style = MaterialTheme.typography.titleSmall)
                    Text("${item.actionTaken} • ${item.result}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
