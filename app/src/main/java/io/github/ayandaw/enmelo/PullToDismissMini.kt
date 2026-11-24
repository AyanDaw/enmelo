package io.github.ayandaw.enmelo

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun Modifier.pullToDismiss(resetKey: Any?, onDismiss: () -> Unit): Modifier {
    val threshold = with(LocalDensity.current) { 56.dp.toPx() }
    val offset = remember(resetKey) { Animatable(0f) }
    var gone by remember(resetKey) { mutableStateOf(false) }
    val goneAlpha by animateFloatAsState(if (gone) 0f else 1f, tween(200), label = "miniGone")
    val scope = rememberCoroutineScope()
    val currentDismiss by rememberUpdatedState(onDismiss)
    return this
        .graphicsLayer {
            translationY = offset.value
            alpha = goneAlpha * (1f - offset.value / (threshold * 3f)).coerceIn(0.3f, 1f)
        }
        .pointerInput(resetKey) {
            detectVerticalDragGestures(
                onVerticalDrag = { change, dy ->
                    change.consume()
                    scope.launch { offset.snapTo((offset.value + dy).coerceAtLeast(0f)) }
                },
                onDragEnd = {
                    if (offset.value > threshold) { gone = true; currentDismiss() }
                    else scope.launch { offset.animateTo(0f) }
                },
                onDragCancel = { scope.launch { offset.animateTo(0f) } }
            )
        }
}