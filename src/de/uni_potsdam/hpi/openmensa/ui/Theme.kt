package de.uni_potsdam.hpi.openmensa.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext as LocalContext1

@Composable
fun Theme(content: @Composable () -> Unit) {
    val context = LocalContext1.current

    val useDarkTheme = isSystemInDarkTheme()

    val colors = when {
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
            if (useDarkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> MaterialTheme.colorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}