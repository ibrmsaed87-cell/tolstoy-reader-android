with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_actions = """                            if (book != null && book!!.format != "pdf") {
                                IconButton(onClick = { showSettings = true }) {
                                    Text("Aa", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        },"""
new_actions = """                            if (book != null && book!!.format != "pdf") {
                                IconButton(onClick = { showSettings = true }) {
                                    Text("Aa", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                            IconButton(onClick = { isFullscreen = true }) {
                                Icon(Icons.Default.Fullscreen, contentDescription = stringResource(id = R.string.fullscreen))
                            }
                        },"""

content = content.replace(old_actions, new_actions)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
