with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_str = """              }
            }
        ) { paddingValues ->"""

new_str = """              }
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
        ) { paddingValues ->"""

content = content.replace(old_str, new_str)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
