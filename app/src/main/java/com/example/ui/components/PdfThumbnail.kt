package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.model.SettingsManager

object PdfThumbnailCache {
    // Cache up to 20MB of thumbnails
    val cache = object : LruCache<String, Bitmap>(20 * 1024 * 1024) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount
        }
    }
}

suspend fun generatePdfThumbnail(context: Context, uri: Uri): Bitmap? {
    val cacheKey = uri.toString()
    PdfThumbnailCache.cache.get(cacheKey)?.let { return it }

    return withContext(Dispatchers.IO) {
        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        var currentPage: PdfRenderer.Page? = null
        try {
            parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            if (parcelFileDescriptor != null) {
                pdfRenderer = PdfRenderer(parcelFileDescriptor)
                if (pdfRenderer.pageCount > 0) {
                    currentPage = pdfRenderer.openPage(0)
                    
                    // Scale down the bitmap to save memory (e.g. 120px width)
                    val width = 120f
                    val scale = width / currentPage.width
                    val height = currentPage.height * scale
                    
                    val bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                    
                    // Fill with white background since PDFs often have transparent backgrounds
                    val canvas = android.graphics.Canvas(bitmap)
                    canvas.drawColor(AndroidColor.WHITE)
                    
                    currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    PdfThumbnailCache.cache.put(cacheKey, bitmap)
                    return@withContext bitmap
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                currentPage?.close()
                pdfRenderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
        return@withContext null
    }
}

@Composable
fun PdfThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier,
    fallbackIcon: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val isPdfPreviewEnabled by settingsManager.isPdfPreviewEnabled.collectAsState()
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(PdfThumbnailCache.cache.get(uri.toString())) }

    LaunchedEffect(uri, isPdfPreviewEnabled) {
        if (isPdfPreviewEnabled && bitmap == null) {
            bitmap = generatePdfThumbnail(context, uri)
        } else if (!isPdfPreviewEnabled) {
            bitmap = null // Clear bitmap if disabled
        }
    }

    if (isPdfPreviewEnabled && bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "PDF Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            fallbackIcon()
        }
    }
}
