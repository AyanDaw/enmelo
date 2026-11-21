package io.github.ayandaw.enmelo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.tapNoRipple(onClick: () -> Unit): Modifier {
    val source = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = source, indication = null, onClick = onClick)
}

@Composable
fun PlainIconButton(icon: Int, description: String, onClick: () -> Unit, size: Dp = 40.dp) {
    Box(Modifier.size(size).tapNoRipple(onClick), contentAlignment = Alignment.Center) {
        Icon(painterResource(icon), contentDescription = description)
    }
}