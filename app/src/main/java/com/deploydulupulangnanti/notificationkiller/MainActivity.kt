package com.deploydulupulangnanti.notificationkiller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.deploydulupulangnanti.notificationkiller.ui.appfilter.*
import com.deploydulupulangnanti.notificationkiller.ui.dashboard.*
import com.deploydulupulangnanti.notificationkiller.ui.history.*
import com.deploydulupulangnanti.notificationkiller.ui.keyword.*
import com.deploydulupulangnanti.notificationkiller.ui.navigation.Screen
import com.deploydulupulangnanti.notificationkiller.ui.settings.*
import com.deploydulupulangnanti.notificationkiller.ui.theme.NotificationKillerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotificationKillerTheme {
                val navController = rememberNavController()
                val screens = listOf(Screen.Dashboard, Screen.AppFilters, Screen.KeywordRules, Screen.History, Screen.Settings)
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, stringResource(screen.title)) },
                                    label = { Text(stringResource(screen.title)) },
                                    selected = currentRoute == screen.route,
                                    onClick = { navController.navigate(screen.route) { launchSingleTop = true; restoreState = true } }
                                )
                            }
                        }
                    }
                ) { inner ->
                    NavHost(navController, Screen.Dashboard.route, modifier = Modifier.padding(inner)) {
                        composable(Screen.Dashboard.route) { DashboardScreen(viewModel()) }
                        composable(Screen.AppFilters.route) { AppFilterScreen(viewModel()) }
                        composable(Screen.KeywordRules.route) { KeywordScreen(viewModel()) }
                        composable(Screen.History.route) { HistoryScreen(viewModel()) }
                        composable(Screen.Settings.route) { SettingsScreen(viewModel()) }
                    }
                }
            }
        }
    }
}
