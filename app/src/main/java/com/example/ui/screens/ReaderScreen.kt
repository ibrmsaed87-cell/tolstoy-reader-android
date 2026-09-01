package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.PdfViewer
import com.example.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    viewModel: BookViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }

    val book by viewModel.selectedBook.collectAsStateWithLifecycle()

    var currentChapterIndex by remember { mutableIntStateOf(0) }
    var initialized by remember { mutableStateOf(false) }
    
    var activePdfFile by remember { mutableStateOf<File?>(null) }
    var currentPdfPage by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(book) {
        if (book != null && book!!.format == "pdf") {
            if (book!!.isDownloaded && book!!.localPdfPath != null) {
                activePdfFile = File(book!!.localPdfPath!!)
            } else {
                activePdfFile = viewModel.getTemporaryPdf(book!!, context)
            }
        }
        if (book != null && !initialized) {
            currentChapterIndex = book!!.currentChapterIndex
            currentPdfPage = book!!.currentPdfPage
            initialized = true
            if (book!!.scrollPosition > 0) {
                // simple delay to let layout happen
                kotlinx.coroutines.delay(100)
                scrollState.scrollTo(book!!.scrollPosition)
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
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(
                    text = stringResource(id = R.string.chapters),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(24.dp)
                )
                if (book != null && book!!.chapters.isNotEmpty()) {
                    LazyColumn {
                        itemsIndexed(book!!.chapters) { index, chapter ->
                            val title = chapter.title ?: "${stringResource(id = R.string.chapters)} ${index + 1}"
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = title,
                                        fontWeight = if (index == currentChapterIndex) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                selected = index == currentChapterIndex,
                                onClick = {
                                    currentChapterIndex = index
                                    coroutineScope.launch {
                                        drawerState.close()
                                        scrollState.scrollTo(0)
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = book?.title ?: stringResource(id = R.string.reader),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    if (book != null && book!!.chapters.isNotEmpty()) {
                        IconButton(onClick = { 
                            coroutineScope.launch { drawerState.open() } 
                        }) {
                            Icon(Icons.Default.List, contentDescription = stringResource(id = R.string.chapters))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        if (book != null) {
            if (book!!.format == "pdf") {
                if (activePdfFile != null) {
                    PdfViewer(
                        file = activePdfFile!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        initialPage = currentPdfPage,
                        onPageChanged = { page -> currentPdfPage = page }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                if (book!!.chapters.isNotEmpty()) {
                    val chapter = book!!.chapters.getOrNull(currentChapterIndex)
                    if (chapter != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(scrollState)
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            if (!chapter.title.isNullOrEmpty()) {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            Text(
                                text = chapter.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 32.sp,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            Spacer(modifier = Modifier.height(48.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (currentChapterIndex > 0) {
                                    TextButton(onClick = { 
                                        currentChapterIndex-- 
                                        coroutineScope.launch { scrollState.scrollTo(0) }
                                    }) {
                                        Text("Previous")
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                
                                if (currentChapterIndex < book!!.chapters.size - 1) {
                                    TextButton(onClick = { 
                                        currentChapterIndex++ 
                                        coroutineScope.launch { scrollState.scrollTo(0) }
                                    }) {
                                        Text("Next")
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                } else if (book!!.content.isNotEmpty()) {
                    // Fallback for combined content if chapters aren't populated for some reason
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = book!!.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 32.sp,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.needs_internet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
}
