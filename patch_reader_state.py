with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

state_vars = """    var isDraggingSlider by remember { mutableStateOf(false) }
    var pendingScrollFraction by remember { mutableStateOf<Float?>(null) }"""

new_state_vars = state_vars + """
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
"""

content = content.replace(state_vars, new_state_vars)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
