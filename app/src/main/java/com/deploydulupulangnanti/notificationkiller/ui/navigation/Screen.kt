package com.deploydulupulangnanti.notificationkiller.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object AppFilters : Screen("app_filters", "Apps", Icons.Default.Apps)
    object KeywordRules : Screen("keyword_rules", "Rules", Icons.Default.FilterList)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
