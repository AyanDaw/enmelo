package io.github.ayandaw.enmelo

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val FallbackAccent = Color(0xFF1F63F2)

data class AccentOption(val key: String, val label: String, val color: Color?)

val accentOptions = listOf(
    AccentOption("phone", "Phone colour", null),
    AccentOption("blue", "Blue", Color(0xFF1F63F2)),
    AccentOption("green", "Green", Color(0xFF2E9E5B)),
    AccentOption("pink", "Pink", Color(0xFFE0457B)),
    AccentOption("orange", "Orange", Color(0xFFF08A24))
)

fun accentColor(key: String): Color? = accentOptions.firstOrNull { it.key == key }?.color

@Composable
fun phoneAccent(): Color? {
    if (Build.VERSION.SDK_INT < 31) return null
    val ctx = LocalContext.current
    return if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx).primary
    else dynamicLightColorScheme(ctx).primary
}

@Composable
fun SettingsScreen(accentKey: String, onAccentChange: (String) -> Unit, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val phone = phoneAccent() ?: FallbackAccent
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.systemBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PlainIconButton(R.drawable.ic_back, "Back", onClose)
                Text("Settings", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                "Accent colour", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                accentOptions.forEach { opt ->
                    val selected = opt.key == accentKey
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .background(opt.color ?: phone)
                            .then(
                                if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                else Modifier
                            )
                            .tapNoRipple { onAccentChange(opt.key) }
                    )
                }
            }
            Text(
                accentOptions.first { it.key == accentKey }.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }
    }
}