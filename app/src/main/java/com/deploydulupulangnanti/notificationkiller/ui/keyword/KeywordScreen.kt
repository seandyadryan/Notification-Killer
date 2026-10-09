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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.R
import com.deploydulupulangnanti.notificationkiller.service.ListenerRuntime
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
    fun saveRule(rule: KeywordRule) = viewModelScope.launch {
        repo.saveRule(rule)
        ListenerRuntime.recheckActiveNotifications()
    }
    fun deleteRule(rule: KeywordRule) = viewModelScope.launch {
        repo.deleteRule(rule)
        ListenerRuntime.recheckActiveNotifications()
    }
    fun toggle(rule: KeywordRule, enabled: Boolean) = viewModelScope.launch {
        repo.saveRule(rule.copy(isEnabled = enabled))
        ListenerRuntime.recheckActiveNotifications()
    }
}

@Composable
fun KeywordScreen(viewModel: KeywordViewModel) {
    val rules by viewModel.rules.collectAsState()
    var editing by remember { mutableStateOf<KeywordRule?>(null) }
    var createNew by remember { mutableStateOf(false) }
    Scaffold(floatingActionButton = { FloatingActionButton(onClick = { createNew = true }) { Icon(Icons.Default.Add, stringResource(R.string.add_rule)) } }) { pad ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text(stringResource(R.string.keyword_rules_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.keyword_rules_help), style = MaterialTheme.typography.bodyMedium)
            }
            if (rules.isEmpty()) item { Text(stringResource(R.string.no_rules), modifier = Modifier.padding(12.dp)) }
            items(rules, key = { it.id }) { rule ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.pattern, style = MaterialTheme.typography.titleMedium)
                            Text("${stringResource(rule.ruleType.resource)} • ${stringResource(rule.scope.resource)}", style = MaterialTheme.typography.bodySmall)
                            Text(stringResource(rule.actionType.resource), style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = { editing = rule }) { Icon(Icons.Default.Edit, stringResource(R.string.edit_rule)) }
                        IconButton(onClick = { viewModel.deleteRule(rule) }) { Icon(Icons.Default.Delete, stringResource(R.string.delete)) }
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
    val typeOptions = listOf(R.string.match_contains, R.string.match_exact, R.string.match_starts, R.string.match_regex).map { stringResource(it) }
    val scopeOptions = listOf(R.string.scope_title, R.string.scope_text, R.string.scope_both).map { stringResource(it) }
    val actionOptions = listOf(R.string.auto_dismiss, R.string.mark_for_review).map { stringResource(it) }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_rule else R.string.edit_rule)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(pattern, { pattern = it; error = null }, label = { Text(stringResource(R.string.rule_pattern_hint)) }, singleLine = true)
                EnumDropdown(stringResource(R.string.match_type), stringResource(type.resource), typeOptions) { selected -> type = RuleType.entries[typeOptions.indexOf(selected).coerceAtLeast(0)]; error = null }
                EnumDropdown(stringResource(R.string.rule_scope), stringResource(scope.resource), scopeOptions) { selected -> scope = RuleScope.entries[scopeOptions.indexOf(selected).coerceAtLeast(0)] }
                EnumDropdown(stringResource(R.string.rule_action), stringResource(action.resource), actionOptions) { selected -> action = ActionType.entries[actionOptions.indexOf(selected).coerceAtLeast(0)] }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = caseSensitive, onCheckedChange = { caseSensitive = it })
                    Text(stringResource(R.string.case_sensitive))
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (pattern.isBlank()) error = context.getString(R.string.empty_pattern_error)
                else if (type == RuleType.REGEX && RegexSafetyHelper.compileSafeRegex(pattern, caseSensitive) == null) error = context.getString(R.string.unsafe_regex_error)
                else onSave(KeywordRule(initial?.id ?: 0, pattern.trim(), type, scope, caseSensitive, action, initial?.isEnabled ?: true))
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
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

private val RuleType.resource: Int get() = when (this) {
    RuleType.CONTAINS -> R.string.match_contains
    RuleType.EXACT_MATCH -> R.string.match_exact
    RuleType.STARTS_WITH -> R.string.match_starts
    RuleType.REGEX -> R.string.match_regex
}
private val RuleScope.resource: Int get() = when (this) {
    RuleScope.TITLE_ONLY -> R.string.scope_title
    RuleScope.TEXT_ONLY -> R.string.scope_text
    RuleScope.TITLE_OR_TEXT -> R.string.scope_both
}
private val ActionType.resource: Int get() = when (this) {
    ActionType.AUTO_DISMISS -> R.string.auto_dismiss
    ActionType.MARK_FOR_REVIEW -> R.string.mark_for_review
}
