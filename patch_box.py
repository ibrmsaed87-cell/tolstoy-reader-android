with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_box = """            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeContainerColor)
                    .padding(paddingValues)
            ) {"""

new_box = """            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(themeContainerColor)
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.displayCutout)
            ) {"""

content = content.replace(old_box, new_box)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
