package io.github.ayandaw.enmelo

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController

fun formatTime(ms: Long): String {
    if (ms < 0) return "0:00"
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
fun ExpandedPlayer(controller: MediaController, songs: List<Song>, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val state = rememberPlayerState(controller)
    val song = remember(state.mediaId, songs) { songs.firstOrNull { it.id.toString() == state.mediaId } }
    var dragging by remember { mutableStateOf<Float?>(null) }
    val fraction = if (state.duration > 0)
        (state.position.toFloat() / state.duration).coerceIn(0f, 1f) else 0f

    val upNext = remember(state.index, state.count, songs) {
        val byId = songs.associateBy { it.id.toString() }
        ((state.index + 1) until state.count).take(20).mapNotNull { qi ->
            byId[controller.getMediaItemAt(qi).mediaId]?.let { qi to it }
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Box(Modifier.fillMaxWidth().padding(8.dp)) {
                    PlainIconButton(R.drawable.ic_down, "Close", onClose, size = 48.dp)
                }
            }
            item { AlbumArt(song, 300.dp, shape = RoundedCornerShape(16.dp)) }
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
                    Slider(
                        value = dragging ?: fraction,
                        onValueChange = { dragging = it },
                        onValueChangeFinished = {
                            dragging?.let { controller.seekTo((it * state.duration).toLong()) }
                            dragging = null
                        },
                        enabled = state.duration > 0
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatTime(state.position), style = MaterialTheme.typography.labelSmall)
                        Text(formatTime(state.duration), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                Row(
                    Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlainIconButton(R.drawable.ic_previous, "Previous",
                        { controller.seekToPreviousMediaItem() }, size = 56.dp)
                    PlainIconButton(
                        if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                        if (state.isPlaying) "Pause" else "Play",
                        { if (state.isPlaying) controller.pause() else controller.play() },
                        size = 56.dp
                    )
                    PlainIconButton(R.drawable.ic_next, "Next",
                        { controller.seekToNextMediaItem() }, size = 56.dp)
                }
            }
            item {
                Text(
                    "Up next in Queue",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                )
            }
            itemsIndexed(upNext, key = { _, p -> p.second.id }) { i, (queueIndex, queued) ->
                Box(Modifier.blur(if (i == 0) 0.dp else 3.dp)) {
                    SongRow(queued, onClick = {
                        controller.seekToDefaultPosition(queueIndex)
                        controller.play()
                    })
                }
            }
        }
    }
}