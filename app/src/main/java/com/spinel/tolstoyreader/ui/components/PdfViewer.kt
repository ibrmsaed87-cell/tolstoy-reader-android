package com.spinel.tolstoyreader.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun PdfViewer(
    file: File,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val pdfRenderer = remember(file) {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        PdfRenderer(descriptor)
    }

    DisposableEffect(pdfRenderer) {
        onDispose {
            pdfRenderer.close()
        }
    }
    
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialPage)
    
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { page ->
                onPageChanged(page)
            }
    }

    LazyColumn(modifier = modifier, state = listState) {
        items(pdfRenderer.pageCount) { index ->
            val bitmap = remember(index) {
                val page = pdfRenderer.openPage(index)
                val width = context.resources.displayMetrics.densityDpi * page.width / 72
                val height = context.resources.displayMetrics.densityDpi * page.height / 72
                val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                b
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Page $index",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
    }
}
