package io.github.ayandaw.enmelo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.github.ayandaw.enmelo.ui.theme.EnmeloTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("enmelo", MODE_PRIVATE) }
            var accentKey by remember { mutableStateOf(prefs.getString("accent", "phone") ?: "phone") }
            EnmeloTheme(accent = accentColor(accentKey)) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EnmeloApp(
                        accentKey = accentKey,
                        onAccentChange = {
                            accentKey = it
                            prefs.edit().putString("accent", it).apply()
                        }
                    )
                }
            }
        }
    }
}