package io.github.ayandaw.enmelo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.tapNoRipple(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = source, indication = null, onClick = onClick)
}

@Composable
fun PlainIconButton(
    icon: Int, description: String, onClick: () -> Unit,
    size: Dp = 40.dp, tint: Color = LocalContentColor.current
) {
    Box(Modifier.size(size).tapNoRipple(onClick), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), contentDescription = description, tint = tint)
    }
}

@Composable
fun RoundPlayButton(isPlaying: Boolean, onClick: () -> Unit, size: Dp = 72.dp) {
    Icon(
        painterResource(if (isPlaying) R.drawable.ic_pause_circle else R.drawable.ic_play_circle),
        contentDescription = if (isPlaying) "Pause" else "Play",
        tint = Color.White,
        modifier = Modifier.size(size).tapNoRipple(onClick)
    )
}