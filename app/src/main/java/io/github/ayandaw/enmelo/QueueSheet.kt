package io.github.ayandaw.enmelo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.session.MediaController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    current: Song?,
    upNext: List<Song>,
    onPick: (Song) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(Modifier.fillMaxHeight(0.65f)) {
            Text(
                "Queue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )
            if (current != null) QueueRow(current, highlight = true)
            Text(
                "Next up",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp)
            )
            LazyColumn(Modifier.weight(1f)) {
                items(upNext, key = { it.id }) { song ->
                    QueueRow(song, onClick = { onPick(song) })
                }
            }
        }
    }
}

@Composable
fun QueueRow(song: Song, highlight: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(64.dp)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(song, 48.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title, maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = if (highlight) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
            Text(
                song.artist, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun QueueHost(controller: MediaController, songs: List<Song>, onDismiss: () -> Unit) {
    val byId = remember(songs) { songs.associateBy { it.id.toString() } }
    val current = remember { controller.currentMediaItem?.mediaId?.let { byId[it] } }
    val upNext = remember {
        ((controller.currentMediaItemIndex + 1) until controller.mediaItemCount)
            .mapNotNull { byId[controller.getMediaItemAt(it).mediaId] }
    }
    QueueSheet(
        current = current,
        upNext = upNext,
        onPick = { song ->
            val target = (0 until controller.mediaItemCount)
                .firstOrNull { controller.getMediaItemAt(it).mediaId == song.id.toString() }
            if (target != null) {
                controller.seekToDefaultPosition(target)
                controller.play()
            }
            onDismiss()
        },
        onDismiss = onDismiss
    )
}