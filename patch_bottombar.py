with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_bottombar = """                    )
                }
            },
            bottomBar = {"""
new_bottombar = """                    )
                }
              }
            },
            bottomBar = {
              AnimatedVisibility(visible = !isFullscreen) {"""

content = content.replace(old_bottombar, new_bottombar)

old_content_start = """                }
            }
        ) { paddingValues ->
            Box("""
new_content_start = """                }
              }
            }
        ) { paddingValues ->
            Box("""

content = content.replace(old_content_start, new_content_start)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
