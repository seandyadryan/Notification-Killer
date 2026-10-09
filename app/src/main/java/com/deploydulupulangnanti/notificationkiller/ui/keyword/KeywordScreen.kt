package com.deploydulupulangnanti.notificationkiller.ui.keyword

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.domain.model.KeywordRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KeywordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    val rules = repo.getRules().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun saveRule(r: KeywordRule) = viewModelScope.launch { repo.saveRule(r) }
    fun deleteRule(r: KeywordRule) = viewModelScope.launch { repo.deleteRule(r) }
    fun toggle(r: KeywordRule, en: Boolean) = viewModelScope.launch { repo.saveRule(r.copy(isEnabled = en)) }
}

@Composable
fun KeywordScreen(viewModel: KeywordViewModel) {
    val list by viewModel.rules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, null) } }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { rule ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.pattern, style = MaterialTheme.typography.titleMedium)
                            Text("${rule.ruleType} • ${rule.scope}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.deleteRule(rule) }) { Icon(Icons.Default.Delete, null) }
                        Switch(checked = rule.isEnabled, onCheckedChange = { viewModel.toggle(rule, it) })
                    }
                }
            }
        }
    }
    if (showDialog) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tambah Aturan") },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Kata Kunci") }) },
            confirmButton = {
                Button(onClick = {
                    if (text.isNotBlank()) viewModel.saveRule(KeywordRule(pattern = text))
                    showDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Batal") } }
        )
    }
}
