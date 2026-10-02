package com.tekewin.mp3offline.playback

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads cover art embedded in an MP3 (ID3 APIC frame), downsampled and cached in memory. */
object Artwork {

    private val cache = LruCache<String, ImageBitmap>(16)
    private val noArt = LruCache<String, Boolean>(256)

    suspend fun load(context: Context, uri: Uri, maxPx: Int = 720): ImageBitmap? {
        val key = uri.toString()
        val sizedKey = "$key@$maxPx"
        cache.get(sizedKey)?.let { return it }
        if (noArt.get(key) == true) return null

        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context.applicationContext, uri)
                val bytes = retriever.embeddedPicture
                if (bytes == null) {
                    noArt.put(key, true)
                    return@withContext null
                }
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= maxPx && bounds.outHeight / (sample * 2) >= maxPx) {
                    sample *= 2
                }
                val bitmap = BitmapFactory.decodeByteArray(
                    bytes, 0, bytes.size,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                ) ?: return@withContext null
                bitmap.asImageBitmap().also { cache.put(sizedKey, it) }
            } catch (e: Exception) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }
    }
}
