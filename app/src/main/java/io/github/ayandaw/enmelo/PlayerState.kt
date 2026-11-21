package io.github.ayandaw.enmelo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import kotlinx.coroutines.delay

class PlayerState(player: Player) {
    var isPlaying by mutableStateOf(player.isPlaying)
    var title by mutableStateOf(player.mediaMetadata.title?.toString() ?: "")
    var artist by mutableStateOf(player.mediaMetadata.artist?.toString() ?: "")
    var mediaId by mutableStateOf(player.currentMediaItem?.mediaId)
    var position by mutableStateOf(player.currentPosition)
    var duration by mutableStateOf(player.duration)
    var index by mutableStateOf(player.currentMediaItemIndex)
    var count by mutableStateOf(player.mediaItemCount)

    fun sync(player: Player) {
        isPlaying = player.isPlaying
        title = player.mediaMetadata.title?.toString() ?: ""
        artist = player.mediaMetadata.artist?.toString() ?: ""
        mediaId = player.currentMediaItem?.mediaId
        position = player.currentPosition
        duration = player.duration
        index = player.currentMediaItemIndex
        count = player.mediaItemCount
    }
}

@Composable
fun rememberPlayerState(controller: MediaController): PlayerState {
    val state = remember(controller) { PlayerState(controller) }
    DisposableEffect(controller) {
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = state.sync(player)
        }
        controller.addListener(listener)
        onDispose { controller.removeListener(listener) }
    }
    LaunchedEffect(state.isPlaying) {
        while (state.isPlaying) {
            state.position = controller.currentPosition
            delay(500)
        }
    }
    return state
}