with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_pdf_controls = """                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { if (currentPdfPage > 0) currentPdfPage-- },
                                        enabled = currentPdfPage > 0
                                    ) { Text("Prev") }
                                    Text(
                                        text = "${currentPdfPage + 1} / ${renderer.pageCount}",
                                        color = Color.White
                                    )
                                    Button(
                                        onClick = { if (currentPdfPage < renderer.pageCount - 1) currentPdfPage++ },
                                        enabled = currentPdfPage < renderer.pageCount - 1
                                    ) { Text("Next") }
                                }"""

new_pdf_controls = """                                AnimatedVisibility(
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
                                }"""

content = content.replace(old_pdf_controls, new_pdf_controls)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
