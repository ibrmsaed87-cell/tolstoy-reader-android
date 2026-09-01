with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_pdf = """                                var bitmap by remember { mutableStateOf<Bitmap?>(null) }
                                LaunchedEffect(currentPdfPage) {
                                    withContext(Dispatchers.IO) {
                                        val page = renderer.openPage(currentPdfPage)
                                        val displayMetrics = context.resources.displayMetrics
                                        val targetWidth = displayMetrics.widthPixels
                                        val scale = targetWidth.toFloat() / page.width
                                        val targetHeight = (page.height * scale).toInt()
                                        val newBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                                        page.render(newBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                        page.close()
                                        bitmap = newBitmap
                                    }
                                }
                                bitmap?.let {
                                    Image(
                                        bitmap = it.asImageBitmap(),
                                        contentDescription = "PDF Page",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }"""

new_pdf = """                                var pdfScale by remember { mutableFloatStateOf(1f) }
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
                                                    scaleX = pdfScale / renderScale,
                                                    scaleY = pdfScale / renderScale,
                                                    translationX = pdfOffset.x,
                                                    translationY = pdfOffset.y
                                                ),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }"""

content = content.replace(old_pdf, new_pdf)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
