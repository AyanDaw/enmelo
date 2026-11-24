package io.github.ayandaw.enmelo.ui.theme

import android.app.Activity

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import io.github.ayandaw.enmelo.FallbackAccent
import io.github.ayandaw.enmelo.phoneAccent

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EnmeloTheme(accent: Color? = null, content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val primary = accent ?: phoneAccent() ?: FallbackAccent
    val scheme = if (dark)
        darkColorScheme(primary = primary, background = Color.Black, surface = Color.Black)
    else
        lightColorScheme(primary = primary, background = Color.White, surface = Color.White)
    MaterialTheme(colorScheme = scheme, content = content)
}