package com.deploydulupulangnanti.notificationkiller.ui.keyword

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.domain.engine.RegexSafetyHelper
import com.deploydulupulangnanti.notificationkiller.domain.model.ActionType
import com.deploydulupulangnanti.notificationkiller.domain.model.KeywordRule
import com.deploydulupulangnanti.notificationkiller.domain.model.RuleScope
import com.deploydulupulangnanti.notificationkiller.domain.model.RuleType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KeywordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as NotificationKillerApp).repository
    val rules = repo.getRules().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun saveRule(rule: KeywordRule) = viewModelScope.launch { repo.saveRule(rule) }
    fun deleteRule(rule: KeywordRule) = viewModelScope.launch { repo.deleteRule(rule) }
    fun toggle(rule: KeywordRule, enabled: Boolean) = viewModelScope.launch { repo.saveRule(rule.copy(isEnabled = enabled)) }
}

@Composable
fun KeywordScreen(viewModel: KeywordViewModel) {
    val rules by viewModel.rules.collectAsState()
    var editing by remember { mutableStateOf<KeywordRule?>(null) }
    var createNew by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { createNew = true }) { Icon(Icons.Default.Add, "Add rule") } }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Keyword rules", style = MaterialTheme.typography.headlineSmall)
                Text("Rules run only when Auto-Clean is enabled. Protected apps always take priority.", style = MaterialTheme.typography.bodyMedium)
            }
            if (rules.isEmpty()) item { Text("Belum ada aturan. Tambahkan aturan untuk memeriksa judul atau teks notifikasi.", modifier = Modifier.padding(12.dp)) }
            items(rules, key = { it.id }) { rule ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.pattern, style = MaterialTheme.typography.titleMedium)
                            Text("${rule.ruleType.label} • ${rule.scope.label}", style = MaterialTheme.typography.bodySmall)
                            Text(if (rule.actionType == ActionType.AUTO_DISMISS) "Auto-dismiss" else "Mark for review", style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { editing = rule }) { Icon(Icons.Default.Edit, "Edit rule") }
                        IconButton(onClick = { viewModel.deleteRule(rule) }) { Icon(Icons.Default.Delete, "Delete rule") }
                        Switch(checked = rule.isEnabled, onCheckedChange = { viewModel.toggle(rule, it) })
                    }
                }
            }
        }
    }
    if (createNew || editing != null) {
        RuleEditorDialog(
            initial = editing,
            onDismiss = { createNew = false; editing = null },
            onSave = { rule -> viewModel.saveRule(rule); createNew = false; editing = null }
        )
    }
}

@Composable
private fun RuleEditorDialog(initial: KeywordRule?, onDismiss: () -> Unit, onSave: (KeywordRule) -> Unit) {
    var pattern by remember(initial) { mutableStateOf(initial?.pattern.orEmpty()) }
    var type by remember(initial) { mutableStateOf(initial?.ruleType ?: RuleType.CONTAINS) }
    var scope by remember(initial) { mutableStateOf(initial?.scope ?: RuleScope.TITLE_OR_TEXT) }
    var action by remember(initial) { mutableStateOf(initial?.actionType ?: ActionType.AUTO_DISMISS) }
    var caseSensitive by remember(initial) { mutableStateOf(initial?.isCaseSensitive ?: false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add keyword rule" else "Edit keyword rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(pattern, { pattern = it; error = null }, label = { Text("Text or regular expression") }, singleLine = true)
                EnumDropdown("Match", type.label, RuleType.entries.map { it.label }) { selected -> type = RuleType.entries.first { it.label == selected }; error = null }
                EnumDropdown("Apply to", scope.label, RuleScope.entries.map { it.label }) { selected -> scope = RuleScope.entries.first { it.label == selected } }
                EnumDropdown("Action", action.label, ActionType.entries.map { it.label }) { selected -> action = ActionType.entries.first { it.label == selected } }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = caseSensitive, onCheckedChange = { caseSensitive = it })
                    Text("Case-sensitive")
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pattern.isBlank()) error = "Masukkan teks atau pola yang tidak kosong."
                else if (type == RuleType.REGEX && RegexSafetyHelper.compileSafeRegex(pattern, caseSensitive) == null) error = "Regex tidak valid atau terlalu kompleks. Gunakan pola sederhana (maksimal 100 karakter)."
                else onSave(KeywordRule(initial?.id ?: 0, pattern.trim(), type, scope, caseSensitive, action, initial?.isEnabled ?: true))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun EnumDropdown(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, label = { Text(label) }, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth())
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

private val RuleType.label: String get() = when (this) {
    RuleType.CONTAINS -> "Contains"
    RuleType.EXACT_MATCH -> "Exact match"
    RuleType.STARTS_WITH -> "Starts with"
    RuleType.REGEX -> "Regular expression"
}
private val RuleScope.label: String get() = when (this) {
    RuleScope.TITLE_ONLY -> "Title"
    RuleScope.TEXT_ONLY -> "Text"
    RuleScope.TITLE_OR_TEXT -> "Title or text"
}
private val ActionType.label: String get() = when (this) {
    ActionType.AUTO_DISMISS -> "Auto-dismiss"
    ActionType.MARK_FOR_REVIEW -> "Mark for review"
}
