package io.github.ayandaw.enmelo

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LibraryScreen(modifier: Modifier = Modifier.Companion) {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_AUDIO
    else
        Manifest.permission.READ_EXTERNAL_STORAGE

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    if (granted) {
        var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
//        val tabs = remember { listOf<NavTab>() } // Uncomment this line to make the bottom nav bar go away
        val tabs = remember {
            listOf(
                NavTab("Home", R.drawable.ic_home),
                NavTab("Library", R.drawable.ic_library),
                NavTab("Search", R.drawable.ic_search),
//                Note: Just Adding one line here can change the number of tabs
//                for example NavTab("Radio", R.drawable.ic_radio)
            )
        }
        var tab by remember { mutableStateOf("Library") } //This is set to "Library" for now which will changed by "Home"
        var expanded by remember { mutableStateOf(false) }
        var showQueue by remember { mutableStateOf(false) }
        var controller by remember { mutableStateOf<MediaController?>(null) }


        LaunchedEffect(Unit) {
            songs = withContext(Dispatchers.IO) {
                debugFolders(context)
                loadSongs(context)
            }
        }

        DisposableEffect(context) {
            val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            val future = MediaController.Builder(context, token).buildAsync()
            future.addListener(
                { controller = future.get() },
                ContextCompat.getMainExecutor(context)
            )
            onDispose {
                MediaController.releaseFuture(future)
                controller = null
            }
        }

        Box(modifier = modifier) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 160.dp)
            ) {
                itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                    SongRow(song, onClick = {
                        controller?.let { c ->
                            if (c.currentMediaItem?.mediaId == song.id.toString()) {
                                if (!c.isPlaying) c.play()
                            } else {
                                c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
                                c.prepare()
                                c.play()
                            }
                        }
                    })
                }
            }
            Column(
                Modifier.align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MiniPlayer(controller, songs, onExpand = { expanded = true }, onQueue = { showQueue = true })
                BottomNavBar(tabs, tabs.indexOfFirst { it.label == tab }) { tab = tabs[it].label }
                Spacer(Modifier.height(8.dp))
            }
            controller?.let { c ->
                if (expanded) ExpandedPlayer(c, songs, onClose = { expanded = false }, onQueue = { showQueue = true })
            }
            controller?.let { c ->
                if (showQueue) QueueHost(c, songs, onDismiss = { showQueue = false })
            }
        }
    } else {
        Button(onClick = { launcher.launch(permission) }, modifier = modifier) {
            Text("Allow music access")
        }
    }
}

@Composable
fun SongRow(song: Song, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlbumArt(song, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    song.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 72.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}