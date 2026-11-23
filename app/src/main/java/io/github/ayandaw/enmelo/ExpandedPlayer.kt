package io.github.ayandaw.enmelo

import android.content.Context
import android.media.AudioManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlin.math.roundToInt

fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPlayer(controller: MediaController, songs: List<Song>, onClose: () -> Unit, onQueue: () -> Unit) {
    BackHandler(onBack = onClose)
    val state = rememberPlayerState(controller)
    val song = remember(state.mediaId, songs) { songs.firstOrNull { it.id.toString() == state.mediaId } }
    var dragging by remember { mutableStateOf<Float?>(null) }
    val fraction = if (state.duration > 0)
        (state.position.toFloat() / state.duration).coerceIn(0f, 1f) else 0f



    Surface(Modifier.fillMaxSize(), color = Color.Black, contentColor = Color.White) {
        BlurredBackdrop(song)
        LazyColumn(
            Modifier.fillMaxSize().systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth().padding(8.dp)) {
                    PlainIconButton(R.drawable.ic_down, "Close", onClose, size = 48.dp)
                }
            }
            item {
                val context = LocalContext.current
                val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
                var volumeShown by remember { mutableStateOf<Float?>(null) }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 50.dp)
                ) {
                    AlbumArt(
                        song, 340.dp,
                        modifier = Modifier
                            .pointerInput(Unit) {
                                var total = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = { total = 0f },
                                    onDragCancel = { total = 0f },
                                    onDragEnd = {
                                        if (total > 120f) controller.seekToPreviousMediaItem()
                                        else if (total < -120f) controller.seekToNextMediaItem()
                                    },
                                    onHorizontalDrag = { _, dx -> total += dx }
                                )
                            }
                            .pointerInput(Unit) {
                                val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                var startVolume = 0
                                var dragged = 0f
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        startVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        dragged = 0f
                                        volumeShown = startVolume / max.toFloat()
                                    },
                                    onDragEnd = { volumeShown = null },
                                    onDragCancel = { volumeShown = null },
                                    onVerticalDrag = { _, dy ->
                                        dragged += dy
                                        val level = (startVolume - dragged / size.height * max)
                                            .roundToInt().coerceIn(0, max)
                                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0)
                                        volumeShown = level / max.toFloat()
                                    }
                                )
                            },
                        shape = RoundedCornerShape(10.dp)
                    )
                    volumeShown?.let { v ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.55f))
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("${(v * 100).roundToInt()}%", color = Color.White)
                        }
                    }
                }
            }
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
                    Text(
                        state.title, style = MaterialTheme.typography.titleLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        state.artist, style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            item {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    val seekSource = remember { MutableInteractionSource() }
                    val isDragged by seekSource.collectIsDraggedAsState()
                    val isPressed by seekSource.collectIsPressedAsState()
                    val active = isDragged || isPressed
                    val trackHeight by animateDpAsState(
                        targetValue = if (active) 14.dp else 3.dp,
                        animationSpec = tween(150), label = "trackHeight"
                    )
                    val dotSize by animateDpAsState(
                        targetValue = if (active) 6.dp else 16.dp,
                        animationSpec = tween(150), label = "dotSize"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (active) Color.Black else Color.White,
                        animationSpec = tween(150), label = "dotColor"
                    )
                    val seekColors = SliderDefaults.colors(
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Slider(
                        value = dragging ?: fraction,
                        onValueChange = { dragging = it },
                        onValueChangeFinished = {
                            dragging?.let { controller.seekTo((it * state.duration).toLong()) }
                            dragging = null
                        },
                        enabled = state.duration > 0,
                        interactionSource = seekSource,
                        colors = seekColors,
                        thumb = { Spacer(Modifier.size(16.dp)) },
                        track = { sliderState ->
                            Canvas(Modifier.fillMaxWidth().height(trackHeight)) {
                                val h = size.height
                                val corner = CornerRadius(h / 2, h / 2)
                                val thumbHalf = 8.dp.toPx()
                                val center = thumbHalf + sliderState.value * (size.width - 2 * thumbHalf)
                                val fillEnd = (center + if (active) h / 2 else 0f).coerceAtMost(size.width)
                                drawRoundRect(Color.White.copy(alpha = 0.3f), size = size, cornerRadius = corner)
                                drawRoundRect(Color.White, size = Size(fillEnd, h), cornerRadius = corner)
                                drawCircle(dotColor, radius = dotSize.toPx() / 2, center = Offset(center, h / 2))
                            }
                        }
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatTime(dragging?.let { (it * state.duration).toLong() } ?: state.position),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(formatTime(state.duration), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val plain = !state.shuffle && state.repeat != Player.REPEAT_MODE_ONE
                    PlainIconButton(
                        when {
                            state.repeat == Player.REPEAT_MODE_ONE -> R.drawable.ic_repeat_one
                            state.shuffle -> R.drawable.ic_shuffle
                            else -> R.drawable.ic_linear
                        },
                        "Play mode",
                        {
                            when {
                                // repeat one -> plain
                                state.repeat == Player.REPEAT_MODE_ONE -> {
                                    controller.repeatMode = Player.REPEAT_MODE_OFF
                                    controller.shuffleModeEnabled = false
                                }
                                // shuffle -> repeat one
                                state.shuffle -> {
                                    controller.shuffleModeEnabled = false
                                    controller.repeatMode = Player.REPEAT_MODE_ONE
                                }
                                // plain -> shuffle
                                else -> controller.shuffleModeEnabled = true
                            }
                        },
                        size = 44.dp,
                        tint = if (plain) Color.White.copy(alpha = 0.5f) else Color.White
                    )
                    PlainIconButton(R.drawable.ic_previous, "Previous",
                        { controller.seekToPreviousMediaItem() }, size = 56.dp)
                    RoundPlayButton(
                        state.isPlaying,
                        { if (state.isPlaying) controller.pause() else controller.play() },
                        size = 88.dp
                    )
                    PlainIconButton(R.drawable.ic_next, "Next",
                        { controller.seekToNextMediaItem() }, size = 56.dp)
                    PlainIconButton(R.drawable.ic_queue, "Queue", onQueue, size = 44.dp)
                }
            }
        }
    }
}