package com.deploydulupulangnanti.notificationkiller.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color

@Composable
fun NotificationKillerTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(primary = Color(0xFFB8C4FF), secondary = Color(0xFF8FC7FF))
        else lightColorScheme(primary = Color(0xFF3F51B5), secondary = Color(0xFF2196F3)),
        content = content
    )
}
