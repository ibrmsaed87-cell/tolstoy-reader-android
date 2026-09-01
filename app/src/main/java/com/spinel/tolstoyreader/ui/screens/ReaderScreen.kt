package com.spinel.tolstoyreader.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.displayCutout

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spinel.tolstoyreader.R
import com.spinel.tolstoyreader.data.model.Book
import com.spinel.tolstoyreader.ui.viewmodel.BookViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val book by viewModel.selectedBook.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    val fontSize by viewModel.fontSize.collectAsStateWithLifecycle()
    val lineSpacing by viewModel.lineSpacing.collectAsStateWithLifecycle()
    val readingTheme by viewModel.readingTheme.collectAsStateWithLifecycle()
    val autoScrollSpeed by viewModel.autoScrollSpeed.collectAsStateWithLifecycle()

    var currentChapterIndex by remember { mutableIntStateOf(0) }
    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var currentPdfPage by remember { mutableIntStateOf(0) }
    var pdfFileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var tempPdfFile by remember { mutableStateOf<File?>(null) }

    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState()
    var showSettings by remember { mutableStateOf(false) }
    var showBookmarkMenu by remember { mutableStateOf(false) }

    var ttsManager by remember { mutableStateOf<TtsManager?>(null) }
    var isTtsPlaying by remember { mutableStateOf(false) }

    // Search and AutoScroll state
    var isSearchActive by remember { mutableStateOf(false) }
    var bookSearchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var currentSearchResultIndex by remember { mutableStateOf(-1) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    var isAutoScrolling by remember { mutableStateOf(false) }
    val isDragged by scrollState.interactionSource.collectIsDraggedAsState()

    // Global Progress Slider
    val chapterLengths = remember(book) { book?.chapters?.map { it.content.length } ?: emptyList() }
    val totalLength = remember(chapterLengths) { chapterLengths.sum().coerceAtLeast(1) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDraggingSlider by remember { mutableStateOf(false) }
    var pendingScrollFraction by remember { mutableStateOf<Float?>(null) }
    var isFullscreen by remember { mutableStateOf(false) }
    val view = LocalView.current
    val window = (context as? Activity)?.window
    val insetsController = remember(window, view) {
        window?.let { WindowCompat.getInsetsController(it, view) }
    }
    
    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            insetsController?.hide(WindowInsetsCompat.Type.systemBars())
            insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    
    BackHandler(enabled = isFullscreen) {
        isFullscreen = false
    }


    DisposableEffect(Unit) {
        ttsManager = TtsManager(context) { }
        onDispose {
            ttsManager?.shutdown()
        }
    }

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    LaunchedEffect(book) {
        book?.let {
            if (it.format == "pdf") {
                val file = viewModel.getTemporaryPdf(it, context)
                if (file != null) {
                    tempPdfFile = file
                    pdfFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    pdfRenderer = PdfRenderer(pdfFileDescriptor!!)
                    currentPdfPage = it.currentPdfPage
                } else {
                    Toast.makeText(context, R.string.needs_internet, Toast.LENGTH_SHORT).show()
                }
            } else {
                currentChapterIndex = it.currentChapterIndex
                coroutineScope.launch {
                    delay(100)
                    scrollState.scrollTo(it.scrollPosition)
                }
            }
        }
    }

    val currentChapterState = rememberUpdatedState(currentChapterIndex)
    val currentScrollState = rememberUpdatedState(scrollState.value)
    val currentPdfPageState = rememberUpdatedState(currentPdfPage)

    DisposableEffect(bookId) {
        onDispose {
            viewModel.saveProgress(bookId, currentChapterState.value, currentScrollState.value)
            viewModel.savePdfProgress(bookId, currentPdfPageState.value)
            pdfRenderer?.close()
            pdfFileDescriptor?.close()
            tempPdfFile?.delete()
        }
    }

    // Auto Scroll logic
    LaunchedEffect(isDragged) {
        if (isDragged) isAutoScrolling = false
    }

    LaunchedEffect(isAutoScrolling, autoScrollSpeed, currentChapterIndex) {
        if (isAutoScrolling && book?.format != "pdf") {
            while (isActive) {
                if (scrollState.value < scrollState.maxValue) {
                    val pixels = 1.5f * autoScrollSpeed
                    scrollState.scrollBy(pixels)
                    delay(16)
                } else {
                    if (currentChapterIndex < (book?.chapters?.size ?: 0) - 1) {
                        currentChapterIndex++
                        delay(500) // Wait for layout
                    } else {
                        isAutoScrolling = false
                        break
                    }
                }
            }
        }
    }

    // Global Progress Update
    val globalProgress by remember(currentChapterIndex, scrollState.value, scrollState.maxValue) {
        derivedStateOf {
            if (chapterLengths.isEmpty() || scrollState.maxValue == 0) return@derivedStateOf 0f
            val chapterStartOffset = chapterLengths.take(currentChapterIndex).sum()
            val chapterProgress = scrollState.value.toFloat() / scrollState.maxValue.toFloat().coerceAtLeast(1f)
            val currentOffset = chapterStartOffset + chapterProgress * chapterLengths[currentChapterIndex]
            (currentOffset / totalLength.toFloat()).coerceIn(0f, 1f)
        }
    }

    LaunchedEffect(globalProgress) {
        if (!isDraggingSlider && !isSearchActive) {
            sliderPosition = globalProgress
        }
    }

    LaunchedEffect(scrollState.maxValue) {
        pendingScrollFraction?.let { fraction ->
            if (scrollState.maxValue > 0) {
                scrollState.scrollTo((fraction * scrollState.maxValue).toInt())
                pendingScrollFraction = null
            }
        }
    }

    // Search Logic
    LaunchedEffect(bookSearchQuery, book) {
        if (bookSearchQuery.isNotBlank() && book != null && book!!.format != "pdf") {
            withContext(Dispatchers.Default) {
                val results = mutableListOf<Pair<Int, Int>>()
                book!!.chapters.forEachIndexed { chIndex, chapter ->
                    var index = chapter.content.indexOf(bookSearchQuery, ignoreCase = true)
                    while (index >= 0) {
                        results.add(Pair(chIndex, index))
                        index = chapter.content.indexOf(bookSearchQuery, startIndex = index + 1, ignoreCase = true)
                    }
                }
                searchResults = results
                currentSearchResultIndex = if (results.isNotEmpty()) 0 else -1
            }
        } else {
            searchResults = emptyList()
            currentSearchResultIndex = -1
        }
    }

    LaunchedEffect(currentSearchResultIndex, searchResults) {
        if (searchResults.isNotEmpty() && currentSearchResultIndex in searchResults.indices) {
            val target = searchResults[currentSearchResultIndex]
            isAutoScrolling = false
            if (currentChapterIndex != target.first) {
                currentChapterIndex = target.first
                textLayoutResult = null
            }
            
            var retries = 0
            while (textLayoutResult == null && retries < 20) {
                delay(50)
                retries++
            }
            
            textLayoutResult?.let { layout ->
                val safeOffset = target.second.coerceIn(0, (layout.layoutInput.text.length - 1).coerceAtLeast(0))
                val line = layout.getLineForOffset(safeOffset)
                val y = layout.getLineTop(line)
                scrollState.animateScrollTo(y.toInt())
            }
        }
    }
    
    LaunchedEffect(currentChapterIndex) {
        textLayoutResult = null
    }

    val themeContainerColor = when (readingTheme) {
        "light" -> Color(0xFFF5F5DC)
        "sepia" -> Color(0xFFF4ECD8)
        "dark" -> Color(0xFF121212)
        else -> MaterialTheme.colorScheme.background
    }
    val themeContentColor = when (readingTheme) {
        "light" -> Color(0xFF1A1A1A)
        "sepia" -> Color(0xFF43302E)
        "dark" -> Color(0xFFE0E0E0)
        else -> MaterialTheme.colorScheme.onBackground
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isSearchActive,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.chapters),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Divider()
                book?.chapters?.forEachIndexed { index, chapter ->
                    NavigationDrawerItem(
                        label = { Text(chapter.title ?: "Chapter ${index + 1}") },
                        selected = index == currentChapterIndex,
                        onClick = {
                            currentChapterIndex = index
                            coroutineScope.launch {
                                drawerState.close()
                                delay(100)
                                scrollState.scrollTo(0)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
              AnimatedVisibility(visible = !isFullscreen) {
                if (isSearchActive) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = bookSearchQuery,
                                onValueChange = { bookSearchQuery = it },
                                placeholder = { Text(stringResource(id = R.string.search_in_book), color = themeContentColor.copy(alpha = 0.6f)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = themeContentColor,
                                    unfocusedTextColor = themeContentColor,
                                    cursorColor = themeContentColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                isSearchActive = false
                                bookSearchQuery = ""
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Close")
                            }
                        },
                        actions = {
                            if (searchResults.isNotEmpty()) {
                                Text(
                                    "${currentSearchResultIndex + 1}/${searchResults.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = themeContentColor,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                IconButton(onClick = {
                                    if (currentSearchResultIndex > 0) currentSearchResultIndex--
                                }) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(id = R.string.previous_result))
                                }
                                IconButton(onClick = {
                                    if (currentSearchResultIndex < searchResults.size - 1) currentSearchResultIndex++
                                }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(id = R.string.next_result))
                                }
                            } else if (bookSearchQuery.isNotBlank()) {
                                Text(
                                    stringResource(id = R.string.no_results),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = themeContentColor,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = themeContainerColor,
                            titleContentColor = themeContentColor,
                            navigationIconContentColor = themeContentColor,
                            actionIconContentColor = themeContentColor
                        )
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = book?.title ?: stringResource(id = R.string.reader),
                                maxLines = 1,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(id = R.string.navigate_back)
                                )
                            }
                        },
                        actions = {
                            if (book != null && book!!.format != "pdf") {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Default.Search, contentDescription = stringResource(id = R.string.search_in_book))
                                }
                                Box {
                                    IconButton(onClick = { 
                                        book?.let {
                                            if (!it.hasBookmark) {
                                                viewModel.updateBookmark(it.id, true, currentChapterIndex, scrollState.value)
                                                Toast.makeText(context, R.string.bookmark_saved, Toast.LENGTH_SHORT).show()
                                            } else {
                                                showBookmarkMenu = true
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (book?.hasBookmark == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = stringResource(id = R.string.bookmark)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showBookmarkMenu,
                                        onDismissRequest = { showBookmarkMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.go_to_bookmark)) },
                                            onClick = {
                                                showBookmarkMenu = false
                                                book?.let {
                                                    currentChapterIndex = it.bookmarkChapterIndex
                                                    coroutineScope.launch {
                                                        delay(100)
                                                        scrollState.scrollTo(it.bookmarkScrollPosition)
                                                    }
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.update_bookmark)) },
                                            onClick = {
                                                showBookmarkMenu = false
                                                book?.let {
                                                    viewModel.updateBookmark(it.id, true, currentChapterIndex, scrollState.value)
                                                    Toast.makeText(context, R.string.bookmark_saved, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.remove_bookmark_action)) },
                                            onClick = {
                                                showBookmarkMenu = false
                                                book?.let {
                                                    viewModel.updateBookmark(it.id, false, -1, 0)
                                                    Toast.makeText(context, R.string.bookmark_removed, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            if (book != null && book!!.chapters.isNotEmpty()) {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        contentDescription = stringResource(id = R.string.chapters)
                                    )
                                }
                            }
                            if (book != null && book!!.format != "pdf") {
                                IconButton(onClick = { showSettings = true }) {
                                    Text("Aa", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                            IconButton(onClick = { isFullscreen = true }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = stringResource(id = R.string.fullscreen))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = themeContainerColor,
                            titleContentColor = themeContentColor,
                            navigationIconContentColor = themeContentColor,
                            actionIconContentColor = themeContentColor
                        )
                    )
                }
              }
            },
            bottomBar = {
              AnimatedVisibility(visible = !isFullscreen) {
                if (book != null && book!!.format != "pdf" && !isSearchActive) {
                    BottomAppBar(
                        containerColor = themeContainerColor,
                        contentColor = themeContentColor,
                        tonalElevation = 2.dp,
                        modifier = Modifier.height(64.dp)
                    ) {
                        IconButton(onClick = { isAutoScrolling = !isAutoScrolling }) {
                            Icon(
                                imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = stringResource(id = if (isAutoScrolling) R.string.pause_auto_scroll else R.string.auto_scroll)
                            )
                        }
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                                isDraggingSlider = true
                                isAutoScrolling = false
                            },
                            onValueChangeFinished = {
                                isDraggingSlider = false
                                val targetOffset = sliderPosition * totalLength
                                var accumulated = 0
                                var targetChapter = 0
                                for ((i, len) in chapterLengths.withIndex()) {
                                    if (accumulated + len >= targetOffset) {
                                        targetChapter = i
                                        break
                                    }
                                    accumulated += len
                                }
                                if (targetChapter >= chapterLengths.size) targetChapter = chapterLengths.size - 1

                                val fraction = if (chapterLengths[targetChapter] > 0) {
                                    (targetOffset - accumulated) / chapterLengths[targetChapter].toFloat()
                                } else 0f

                                if (currentChapterIndex != targetChapter) {
                                    currentChapterIndex = targetChapter
                                    pendingScrollFraction = fraction
                                } else {
                                    coroutineScope.launch {
                                        scrollState.scrollTo((fraction * scrollState.maxValue).toInt())
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "${(sliderPosition * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
              }
            },
            floatingActionButton = {
                AnimatedVisibility(visible = isFullscreen) {
                    FloatingActionButton(
                        onClick = { isFullscreen = false },
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.FullscreenExit, contentDescription = stringResource(id = R.string.exit_fullscreen))
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeContainerColor)
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.displayCutout)
            ) {
                if (book != null) {
                    if (book!!.format == "pdf") {
                        pdfRenderer?.let { renderer ->
                            if (renderer.pageCount > 0) {
                                var pdfScale by remember { mutableFloatStateOf(1f) }
                                var pdfOffset by remember { mutableStateOf(Offset.Zero) }
                                var renderScale by remember { mutableFloatStateOf(1f) }
                                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                                
                                LaunchedEffect(currentPdfPage) {
                                    pdfScale = 1f
                                    pdfOffset = Offset.Zero
                                }
                                
                                LaunchedEffect(pdfScale) {
                                    delay(300)
                                    renderScale = minOf(pdfScale, 3f)
                                }
                                
                                LaunchedEffect(currentPdfPage, renderScale) {
                                    withContext(Dispatchers.IO) {
                                        val page = renderer.openPage(currentPdfPage)
                                        val displayMetrics = context.resources.displayMetrics
                                        val baseWidth = displayMetrics.widthPixels
                                        val targetWidth = (baseWidth * renderScale).toInt().coerceAtMost(3000)
                                        val scale = targetWidth.toFloat() / page.width
                                        val targetHeight = (page.height * scale).toInt().coerceAtMost(4000)
                                        
                                        val newBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                        page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                        page.close()
                                        bitmap = newBitmap
                                    }
                                }
                                
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            val newScale = (pdfScale * zoom).coerceIn(1f, 4f)
                                            val maxOffsetX = (size.width * newScale - size.width) / 2
                                            val maxOffsetY = (size.height * newScale - size.height) / 2
                                            pdfScale = newScale
                                            pdfOffset = Offset(
                                                x = (pdfOffset.x + pan.x * newScale).coerceIn(-maxOffsetX, maxOffsetX),
                                                y = (pdfOffset.y + pan.y * newScale).coerceIn(-maxOffsetY, maxOffsetY)
                                            )
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onDoubleTap = {
                                                if (pdfScale > 1f) {
                                                    pdfScale = 1f
                                                    pdfOffset = Offset.Zero
                                                } else {
                                                    pdfScale = 2f
                                                }
                                            },
                                            onTap = {
                                                if (isFullscreen) isFullscreen = false
                                            }
                                        )
                                    }
                                ) {
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "PDF Page",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer(
                                                    scaleX = pdfScale,
                                                    scaleY = pdfScale,
                                                    translationX = pdfOffset.x,
                                                    translationY = pdfOffset.y
                                                ),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                                AnimatedVisibility(
                                    visible = !isFullscreen,
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { pdfScale = (pdfScale - 0.5f).coerceAtLeast(1f) }) {
                                                Icon(Icons.Default.ZoomOut, contentDescription = stringResource(id = R.string.zoom_out), tint = Color.White)
                                            }
                                            IconButton(onClick = { 
                                                pdfScale = 1f 
                                                pdfOffset = Offset.Zero
                                            }) {
                                                Text("Fit", color = Color.White)
                                            }
                                            IconButton(onClick = { pdfScale = (pdfScale + 0.5f).coerceAtMost(4f) }) {
                                                Icon(Icons.Default.ZoomIn, contentDescription = stringResource(id = R.string.zoom_in), tint = Color.White)
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { 
                                                    if (currentPdfPage > 0) {
                                                        currentPdfPage--
                                                        pdfScale = 1f
                                                        pdfOffset = Offset.Zero
                                                    }
                                                },
                                                enabled = currentPdfPage > 0
                                            ) { Text("Prev") }
                                            Text(
                                                text = "${currentPdfPage + 1} / ${renderer.pageCount}",
                                                color = Color.White
                                            )
                                            Button(
                                                onClick = { 
                                                    if (currentPdfPage < renderer.pageCount - 1) {
                                                        currentPdfPage++
                                                        pdfScale = 1f
                                                        pdfOffset = Offset.Zero
                                                    }
                                                },
                                                enabled = currentPdfPage < renderer.pageCount - 1
                                            ) { Text("Next") }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        val chapter = book!!.chapters.getOrNull(currentChapterIndex)
                        if (chapter != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { isFullscreen = !isFullscreen }
                                        )
                                    }
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 24.dp, vertical = 32.dp)
                            ) {
                                chapter.title?.let { title ->
                                    Text(
                                        text = title,
                                        fontSize = (fontSize + 6).sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeContentColor,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )
                                }
                                
                                val chapterText = chapter.content
                                val annotatedContent = remember(chapterText, bookSearchQuery, searchResults, readingTheme) {
                                    if (bookSearchQuery.isBlank()) {
                                        AnnotatedString(chapterText)
                                    } else {
                                        buildAnnotatedString {
                                            var currentIndex = 0
                                            val queryLower = bookSearchQuery.lowercase()
                                            val textLower = chapterText.lowercase()
                                            var matchIndex = textLower.indexOf(queryLower)
                                            while (matchIndex >= 0) {
                                                append(chapterText.substring(currentIndex, matchIndex))
                                                withStyle(SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black)) {
                                                    append(chapterText.substring(matchIndex, matchIndex + bookSearchQuery.length))
                                                }
                                                currentIndex = matchIndex + bookSearchQuery.length
                                                matchIndex = textLower.indexOf(queryLower, currentIndex)
                                            }
                                            append(chapterText.substring(currentIndex))
                                        }
                                    }
                                }

                                Text(
                                    text = annotatedContent,
                                    fontSize = fontSize.sp,
                                    lineHeight = (fontSize * lineSpacing).sp,
                                    color = themeContentColor,
                                    onTextLayout = { textLayoutResult = it }
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (currentChapterIndex > 0) {
                                        TextButton(onClick = {
                                            currentChapterIndex--
                                            coroutineScope.launch {
                                                delay(100)
                                                scrollState.scrollTo(0)
                                            }
                                        }) {
                                            Text("Previous Chapter")
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }
                                    if (currentChapterIndex < book!!.chapters.size - 1) {
                                        TextButton(onClick = {
                                            currentChapterIndex++
                                            coroutineScope.launch {
                                                delay(100)
                                                scrollState.scrollTo(0)
                                            }
                                        }) {
                                            Text("Next Chapter")
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(48.dp))
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = themeContentColor)
                    }
                }
            }
            
            if (showSettings && book?.format != "pdf") {
                ModalBottomSheet(
                    onDismissRequest = { showSettings = false },
                    sheetState = sheetState,
                    containerColor = themeContainerColor
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = stringResource(id = R.string.reading_settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = themeContentColor
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Font Size
                        Text(text = stringResource(id = R.string.font_size), style = MaterialTheme.typography.labelLarge, color = themeContentColor)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (fontSize > 14f) viewModel.setFontSize(fontSize - 2f) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text("A-", fontWeight = FontWeight.Bold)
                            }
                            Text("${fontSize.toInt()} sp", style = MaterialTheme.typography.bodyLarge, color = themeContentColor)
                            FilledTonalIconButton(
                                onClick = { if (fontSize < 30f) viewModel.setFontSize(fontSize + 2f) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Text("A+", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Line Spacing
                        Text(text = stringResource(id = R.string.line_spacing), style = MaterialTheme.typography.labelLarge, color = themeContentColor)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = lineSpacing == 1.2f,
                                onClick = { viewModel.setLineSpacing(1.2f) },
                                label = { Text(stringResource(id = R.string.spacing_compact)) }
                            )
                            FilterChip(
                                selected = lineSpacing == 1.5f,
                                onClick = { viewModel.setLineSpacing(1.5f) },
                                label = { Text(stringResource(id = R.string.spacing_normal)) }
                            )
                            FilterChip(
                                selected = lineSpacing == 1.8f,
                                onClick = { viewModel.setLineSpacing(1.8f) },
                                label = { Text(stringResource(id = R.string.spacing_relaxed)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Reading Speed
                        Text(text = stringResource(id = R.string.reading_speed), style = MaterialTheme.typography.labelLarge, color = themeContentColor)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = autoScrollSpeed == 0.5f,
                                onClick = { viewModel.setAutoScrollSpeed(0.5f) },
                                label = { Text(stringResource(id = R.string.speed_slow)) }
                            )
                            FilterChip(
                                selected = autoScrollSpeed == 1.0f,
                                onClick = { viewModel.setAutoScrollSpeed(1.0f) },
                                label = { Text(stringResource(id = R.string.speed_normal)) }
                            )
                            FilterChip(
                                selected = autoScrollSpeed == 1.5f,
                                onClick = { viewModel.setAutoScrollSpeed(1.5f) },
                                label = { Text(stringResource(id = R.string.speed_fast)) }
                            )
                            FilterChip(
                                selected = autoScrollSpeed == 2.0f,
                                onClick = { viewModel.setAutoScrollSpeed(2.0f) },
                                label = { Text(stringResource(id = R.string.speed_very_fast)) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Theme
                        Text(text = stringResource(id = R.string.theme), style = MaterialTheme.typography.labelLarge, color = themeContentColor)
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
                                val systemBg = if (isSystemDark) Color(0xFF121212) else Color(0xFFF5F5F5)
                                val systemText = if (isSystemDark) Color(0xFFE0E0E0) else Color(0xFF212121)
                                ReadingThemeCard(
                                    title = stringResource(id = R.string.theme_system),
                                    selected = readingTheme == "system",
                                    onClick = { viewModel.setReadingTheme("system") },
                                    bgColor = systemBg,
                                    textColor = systemText,
                                    modifier = Modifier.weight(1f)
                                )
                                ReadingThemeCard(
                                    title = stringResource(id = R.string.theme_light),
                                    selected = readingTheme == "light",
                                    onClick = { viewModel.setReadingTheme("light") },
                                    bgColor = Color(0xFFF5F5F5),
                                    textColor = Color(0xFF212121),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ReadingThemeCard(
                                    title = stringResource(id = R.string.theme_sepia),
                                    selected = readingTheme == "sepia",
                                    onClick = { viewModel.setReadingTheme("sepia") },
                                    bgColor = Color(0xFFF4ECD8),
                                    textColor = Color(0xFF5B4636),
                                    modifier = Modifier.weight(1f)
                                )
                                ReadingThemeCard(
                                    title = stringResource(id = R.string.theme_dark),
                                    selected = readingTheme == "dark",
                                    onClick = { viewModel.setReadingTheme("dark") },
                                    bgColor = Color(0xFF121212),
                                    textColor = Color(0xFFE0E0E0),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Text-to-Speech
                        Text(text = "Text-to-Speech", style = MaterialTheme.typography.labelLarge, color = themeContentColor)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            if (!isTtsPlaying) {
                                Button(
                                    onClick = { 
                                        val langSupported = ttsManager?.setLanguage(book?.language ?: "en") ?: false
                                        if (langSupported) {
                                            val textToRead = if (book?.chapters?.isNotEmpty() == true) {
                                                book?.chapters?.getOrNull(currentChapterIndex)?.content ?: ""
                                            } else {
                                                book?.content ?: ""
                                            }
                                            if (textToRead.isNotBlank()) {
                                                ttsManager?.speak(textToRead)
                                                isTtsPlaying = true
                                            }
                                        } else {
                                            Toast.makeText(context, R.string.tts_lang_not_supported, Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(id = R.string.tts_play), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { 
                                        ttsManager?.stop()
                                        isTtsPlaying = false
                                    }, 
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(id = R.string.tts_stop), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingThemeCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Aa",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
