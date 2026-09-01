with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_column = """                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 24.dp, vertical = 32.dp)
                            ) {"""

new_column = """                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { isFullscreen = !isFullscreen }
                                        )
                                    }
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 24.dp, vertical = 32.dp)
                            ) {"""

content = content.replace(old_column, new_column)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
