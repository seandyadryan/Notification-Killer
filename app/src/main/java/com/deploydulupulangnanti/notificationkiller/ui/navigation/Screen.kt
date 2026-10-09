package com.deploydulupulangnanti.notificationkiller.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.annotation.StringRes
import com.deploydulupulangnanti.notificationkiller.R

sealed class Screen(val route: String, @StringRes val title: Int, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", R.string.screen_dashboard, Icons.Default.Dashboard)
    object AppFilters : Screen("app_filters", R.string.screen_apps, Icons.Default.Apps)
    object KeywordRules : Screen("keyword_rules", R.string.screen_rules, Icons.Default.FilterList)
    object History : Screen("history", R.string.screen_history, Icons.Default.History)
    object Settings : Screen("settings", R.string.screen_settings, Icons.Default.Settings)
}
