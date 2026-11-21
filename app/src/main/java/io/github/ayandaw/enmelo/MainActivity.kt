package io.github.ayandaw.enmelo

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import io.github.ayandaw.enmelo.ui.theme.EnmeloTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnmeloTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LibraryScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(modifier: Modifier = Modifier) {
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
        var tab by remember { mutableStateOf(1) }
        var expanded by remember { mutableStateOf(false) }
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
                MiniPlayer(controller, songs, onExpand = { expanded = true })
                BottomNavBar(tab) { tab = it }
                Spacer(Modifier.height(8.dp))
            }
            controller?.let { c ->
                if (expanded) ExpandedPlayer(c, songs, onClose = { expanded = false })
            }
        }
    } else {
        Button(onClick = { launcher.launch(permission) }, modifier = modifier) {
            Text("Allow music access")
        }
    }
}

@Composable
fun MiniPlayer(controller: MediaController?, songs: List<Song>, onExpand: () -> Unit) {
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
        Surface(
            modifier = Modifier
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
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(//Progress Bar
                    Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(3.dp))
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
                        onClick = { if (isPlaying) controller.pause() else controller.play() }
                    )
                    PlainIconButton(R.drawable.ic_next, "Next", onClick = { controller.seekToNextMediaItem() })
                    PlainIconButton(R.drawable.ic_queue, "Queue", onClick = onExpand)
                }
            }
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

@Composable
fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "Home" to R.drawable.ic_home,
        "Library" to R.drawable.ic_library,
        "Search" to R.drawable.ic_search
    )
    val lefts = remember { mutableStateMapOf<Int, Float>() }
    val widths = remember { mutableStateMapOf<Int, Float>() }
    val shadeX = remember { Animatable(0f) }
    val shadeW = remember { Animatable(0f) }
    val spec = tween<Float>(durationMillis = 300, easing = EaseInOut)

    LaunchedEffect(selected, lefts[selected], widths[selected]) {
        val x = lefts[selected] ?: return@LaunchedEffect
        val w = widths[selected] ?: return@LaunchedEffect
        if (shadeW.value == 0f) {
            shadeX.snapTo(x)
            shadeW.snapTo(w)
        } else {
            launch { shadeX.animateTo(x, spec) }
            shadeW.animateTo(w, spec)
        }
    }
    Surface(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .height(50.dp)
            .dropShadow(
                RoundedCornerShape(28.dp),
                Shadow(
                    radius = 15.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 0.30f),
                    offset = DpOffset.Zero
                )
            ),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.fillMaxHeight().padding(horizontal = 5.dp, vertical = 5.dp)) {
            if (shadeW.value > 0f) {
                Box(
                    Modifier
                        .offset { IntOffset(shadeX.value.roundToInt(), 0) }
                        .width(with(LocalDensity.current) { shadeW.value.toDp() })
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                )
            }
            Row(Modifier.fillMaxHeight()) {
                items.forEachIndexed { i, (label, icon) ->
                    val tint = if (i == selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                    Column(
                        Modifier
                            .onGloballyPositioned {
                                lefts[i] = it.positionInParent().x
                                widths[i] = it.size.width.toFloat()
                            }
                            .clip(RoundedCornerShape(28.dp))
                            .tapNoRipple { onSelect(i) }
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(painterResource(icon), contentDescription = label, tint = tint)
                        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
                    }
                }
            }
        }
    }
}