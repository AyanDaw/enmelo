package io.github.ayandaw.enmelo

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.media3.common.Player
import kotlin.math.floor
import kotlin.math.roundToInt

enum class PlayMode(val icon: Int) {
    Plain(R.drawable.ic_linear),
    Shuffle(R.drawable.ic_shuffle),
    RepeatOne(R.drawable.ic_repeat_one)
}

fun playModeOf(shuffle: Boolean, repeat: Int): PlayMode = when {
    repeat == Player.REPEAT_MODE_ONE -> PlayMode.RepeatOne
    shuffle -> PlayMode.Shuffle
    else -> PlayMode.Plain
}

fun Player.applyPlayMode(mode: PlayMode) {
    when (mode) {
        PlayMode.Plain -> { shuffleModeEnabled = false; repeatMode = Player.REPEAT_MODE_OFF }
        PlayMode.Shuffle -> { shuffleModeEnabled = true; repeatMode = Player.REPEAT_MODE_OFF }
        PlayMode.RepeatOne -> { shuffleModeEnabled = false; repeatMode = Player.REPEAT_MODE_ONE }
    }
}

@Composable
fun PlayModeButton(
    current: PlayMode,
    onSelect: (PlayMode) -> Unit,
    onMenuChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = PlayMode.entries
    val itemSize = 44.dp
    val pad = 12.dp
    val density = LocalDensity.current
    val itemPx = with(density) { itemSize.toPx() }
    val padPx = with(density) { pad.toPx() }
    val haptic = LocalHapticFeedback.current
    var open by remember { mutableStateOf(false) }
    var hover by remember { mutableStateOf(current) }
    var menuModes by remember { mutableStateOf<List<PlayMode>>(modes) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var last by remember { mutableStateOf(current) }
    var previous by remember { mutableStateOf<PlayMode?>(null) }
    LaunchedEffect(current) {
        if (current != last) {
            previous = last
            last = current
        }
    }
    val currentMode by rememberUpdatedState(current)
    val select by rememberUpdatedState(onSelect)
    val menuChange by rememberUpdatedState(onMenuChange)

    Box(
        modifier
            .padding(6.dp)
            .size(48.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                        waitForUpOrCancellation()
                    }
                    // quick tap = shortcut: go to the next mode
                    if (up != null) {
                        select(modes[(currentMode.ordinal + 1) % modes.size])
                        return@awaitEachGesture
                    }
                    if (currentEvent.changes.firstOrNull { it.id == down.id }?.pressed != true)
                        return@awaitEachGesture

                    // long press: open the menu and follow the finger
                    // previous on top, current in the middle, next at the bottom
                    val n = modes.size
                    val cycleNext = modes[(currentMode.ordinal + 1) % n]
                    val cyclePrev = modes[(currentMode.ordinal + n - 1) % n]
                    val top = previous?.takeIf { it != currentMode } ?: cyclePrev
                    val bottom = if (cycleNext != top) cycleNext else cyclePrev
                    val items = listOf(top, currentMode, bottom)
                    menuModes = items
                    hover = currentMode
                    dragY = 0f
                    open = true
                    menuChange(true)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    while (true) {
                        val event = awaitPointerEvent()
                        val c = event.changes.firstOrNull { it.id == down.id } ?: break
                        val dy = c.position.y - size.height / 2f
                        dragY = dy.coerceIn(-itemPx, itemPx)
                        val index = (1 + floor((dy + itemPx / 2) / itemPx).toInt())
                            .coerceIn(0, items.size - 1)
                        if (items[index] != hover) {
                            hover = items[index]
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        c.consume()
                        if (!c.pressed) break
                    }
                    select(hover)
                    open = false
                    menuChange(false)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(current.icon), "Play mode", tint = Color.White)

        if (open) {
            val positioner = remember {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect, windowSize: IntSize,
                        layoutDirection: LayoutDirection, popupContentSize: IntSize
                    ): IntOffset {
                        val x = anchorBounds.center.x - popupContentSize.width / 2
                        val bottom = anchorBounds.center.y + (itemPx * 1.5f + padPx).toInt()
                        return IntOffset(x, bottom - popupContentSize.height)
                    }
                }
            }
            Popup(
                popupPositionProvider = positioner,
                properties = PopupProperties(focusable = false, clippingEnabled = false)
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF262626))
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    // the sliding highlight
                    Box(
                        Modifier
                            .offset { IntOffset(0, (itemPx + dragY).roundToInt()) }
                            .size(itemSize)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                    )
                    // the icons on top
                    Column {
                        menuModes.forEach { m ->
                            Box(Modifier.size(itemSize), contentAlignment = Alignment.Center) {
                                Icon(painterResource(m.icon), null, tint = Color.White,
                                    modifier = Modifier.size(30.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}