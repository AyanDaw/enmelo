package io.github.ayandaw.enmelo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import io.github.ayandaw.enmelo.ui.theme.EnmeloTheme
import kotlinx.coroutines.delay

@Composable
fun MiniPlayerContent(
    song: Song?,
    title: String,
    artist: String,
    isPlaying: Boolean,
    fraction: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onQueue: () -> Unit,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    mediaId: String?,
) {
    Surface(
        modifier = Modifier
            .pullToDismiss(mediaId, onDismiss)
            .fillMaxWidth()
            .padding(horizontal = 25.dp, vertical = 5.dp)
            .height(64.dp) // Actually was 64, 8 added for 1 mm height
            .dropShadow(
                RoundedCornerShape(20.dp),
                Shadow(
                    radius = 6.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 0.30f),
                    offset = DpOffset.Zero
                )//TODO: make it dynamic by themes
            )
            .tapNoRipple(onExpand),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(//Progress Bar
                Modifier
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Row(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArt(song, 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                PlainIconButton(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                    if (isPlaying) "Pause" else "Play",
                    onClick = onPlayPause
                )
                PlainIconButton(
                    R.drawable.ic_next,
                    "Next",
                    onClick = onNext,
                )
                PlainIconButton(R.drawable.ic_queue, "Queue", onClick = onQueue)
            }
        }
    }
}

@Composable
fun MiniPlayer(
    controller: MediaController?, songs: List<Song>,
    onExpand: () -> Unit, onQueue: () -> Unit, onDismiss: () -> Unit
) {
    if (controller == null) return

    var isPlaying by remember { mutableStateOf(controller.isPlaying) }
    var title by remember { mutableStateOf(controller.mediaMetadata.title?.toString() ?: "") }
    var artist by remember { mutableStateOf(controller.mediaMetadata.artist?.toString() ?: "") }
    var position by remember { mutableStateOf(controller.currentPosition) }
    var duration by remember { mutableStateOf(controller.duration) }
    var mediaId by remember { mutableStateOf(controller.currentMediaItem?.mediaId) }
    val song = remember(mediaId, songs) { songs.firstOrNull { it.id.toString() == mediaId } }

    DisposableEffect(controller) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                isPlaying = player.isPlaying
                title = player.mediaMetadata.title?.toString() ?: ""
                artist = player.mediaMetadata.artist?.toString() ?: ""
                duration = player.duration
                position = player.currentPosition
                mediaId = player.currentMediaItem?.mediaId
            }
        }
        controller.addListener(listener)
        onDispose { controller.removeListener(listener) }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            position = controller.currentPosition
            delay(500)
        }
    }

    val fraction = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

    if (mediaId != null) {
        MiniPlayerContent(
            song = song, title = title, artist = artist,
            isPlaying = isPlaying, fraction = fraction,
            onPlayPause = { if (isPlaying) controller.pause() else controller.play() },
            onNext = { controller.seekToNextMediaItem() },
            onQueue = onQueue,
            onExpand = onExpand,
            onDismiss = onDismiss,
            mediaId = mediaId
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun MiniPlayerPreview() {
    EnmeloTheme {
        MiniPlayerContent(
            song = null, title = "Aaj Jyotsna Raate", artist = "Kumar Sanu",
            isPlaying = true, fraction = 0.4f,
            onPlayPause = {}, onNext = {}, onQueue = {}, onExpand = {},
            onDismiss = {}, mediaId = "preview"
        )
    }
}