package com.deploydulupulangnanti.notificationkiller.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun NotificationKillerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF3F51B5),
            secondary = Color(0xFF2196F3)
        ),
        content = content
    )
}
