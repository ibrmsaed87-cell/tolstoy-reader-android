with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_topbar = """        Scaffold(
            topBar = {"""
new_topbar = """        Scaffold(
            topBar = {
              AnimatedVisibility(visible = !isFullscreen) {"""

content = content.replace(old_topbar, new_topbar)

# Now close AnimatedVisibility. The topBar ends right before `bottomBar = {`
# Let's find bottomBar
old_bottombar = """            },
            bottomBar = {"""
new_bottombar = """            }
              },
            bottomBar = {
              AnimatedVisibility(visible = !isFullscreen) {"""
content = content.replace(old_bottombar, new_bottombar)

# Find the end of bottomBar. It ends before `content = {` or something similar?
# Let's verify where Scaffold content starts.
