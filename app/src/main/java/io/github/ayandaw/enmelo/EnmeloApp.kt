package io.github.ayandaw.enmelo

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// The app shell: owns everything shared between screens (permission, songs, the player
// connection, tabs, mini player, nav bar and the full-screen overlays).
@Composable
fun EnmeloApp(accentKey: String, onAccentChange: (String) -> Unit, modifier: Modifier = Modifier) {
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
                NavTab("You", R.drawable.ic_you),
//                Note: Just Adding one line here can change the number of tabs
//                for example NavTab("Radio", R.drawable.ic_radio)
            )
        }
        var tab by rememberSaveable { mutableStateOf("Library") } //This is set to "Library" for now which will changed by "Home"
        var expanded by rememberSaveable { mutableStateOf(false) }
        var showQueue by rememberSaveable { mutableStateOf(false) }
        var controller by remember { mutableStateOf<MediaController?>(null) }
        var showSettings by rememberSaveable { mutableStateOf(false) }
        val darkTheme = isSystemInDarkTheme()
        val view = LocalView.current
        val prefs = remember { context.getSharedPreferences("enmelo", Context.MODE_PRIVATE) }
        val scope = rememberCoroutineScope()
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkTheme && !expanded
        }


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

        // Remember the current song: save its id whenever the song changes
        DisposableEffect(controller) {
            val c = controller
            val listener = object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    mediaItem?.mediaId?.toLongOrNull()?.let {
                        prefs.edit().putLong("lastSongId", it).apply()
                    }
                }
            }
            c?.addListener(listener)
            onDispose { c?.removeListener(listener) }
        }

        // On launch: load the last song (paused, from the start) so the mini player shows it
        LaunchedEffect(controller, songs) {
            val c = controller ?: return@LaunchedEffect
            if (songs.isEmpty() || c.mediaItemCount > 0) return@LaunchedEffect
            val index = songs.indexOfFirst { it.id == prefs.getLong("lastSongId", -1L) }
            if (index >= 0) {
                c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
                c.prepare()
            }
        }

        // Pull-down on the mini player: pause (fades), then clear the queue so it disappears
        val dismissMini: () -> Unit = {
            controller?.pause()
            scope.launch {
                delay(600)
                controller?.clearMediaItems()
                prefs.edit().remove("lastSongId").apply()
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            when (tab) {
                "Home" -> PlaceholderScreen("Home")
                "Search" -> PlaceholderScreen("Search")
                "You" -> YouScreen { showSettings = true }
                else -> LibraryScreen(
                    songs = songs,
                    onSongClick = { index, song ->
                        controller?.let { c ->
                            if (c.currentMediaItem?.mediaId == song.id.toString()) {
                                if (!c.isPlaying) c.play()
                            } else {
                                c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
                                c.prepare()
                                c.play()
                            }
                        }
                    }
                )
            }
            Column(
                Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MiniPlayer(
                    controller, songs,
                    onExpand = { expanded = true },
                    onQueue = { showQueue = true },
                    onDismiss = dismissMini
                )
                BottomNavBar(tabs, tabs.indexOfFirst { it.label == tab }) { tab = tabs[it].label }
                Spacer(Modifier.height(8.dp))
            }
            controller?.let { c ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(250)),
                    exit = slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
                ) {
                    ExpandedPlayer(c, songs, onClose = { expanded = false }, onQueue = { showQueue = true })
                }
            }
            controller?.let { c ->
                if (showQueue) QueueHost(c, songs, onDismiss = { showQueue = false })
            }
            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically(tween(350, easing = FastOutSlowInEasing)) { it } + fadeIn(tween(250)),
                exit = slideOutVertically(tween(300, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(250))
            ) {
                SettingsScreen(accentKey, onAccentChange) { showSettings = false }
            }
        }
    } else {
        Button(onClick = { launcher.launch(permission) }, modifier = modifier.systemBarsPadding()) {
            Text("Allow music access")
        }
    }
}