package io.github.ayandaw.enmelo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val artCache = object : LruCache<String, Bitmap>(24 * 1024 * 1024) {
    override fun sizeOf(key: String, value: Bitmap) = value.byteCount
}

fun loadArt(context: Context, song: Song, sizePx: Int): Bitmap? {
    val key = "${song.id}_$sizePx"
    artCache.get(key)?.let { return it }
    val bitmap = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(song.uri, Size(sizePx, sizePx), null)
        } else {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, song.uri)
                retriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            } finally {
                retriever.release()
            }
        }
    } catch (e: Exception) {
        null
    }
    if (bitmap != null) artCache.put(key, bitmap)
    return bitmap
}

@Composable
fun AlbumArt(
    song: Song?,
    size: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(10.dp)
) {
    val context = LocalContext.current
    val px = with(LocalDensity.current) { size.roundToPx() }
    val bitmap by produceState<Bitmap?>(null, song?.id, px) {
        value = if (song == null) null
        else withContext(Dispatchers.IO) { loadArt(context, song, px) }
    }
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                it.asImageBitmap(), contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}