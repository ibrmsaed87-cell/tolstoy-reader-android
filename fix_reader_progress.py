import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if 'val currentChapterState =' in line:
        new_lines.append("    val currentChapterState by rememberUpdatedState(currentChapterIndex)\n")
        continue
    if 'val currentScrollState =' in line:
        # We don't need currentScrollState, but let's keep it as rememberUpdatedState
        new_lines.append("    val currentScrollState by rememberUpdatedState(scrollState.value)\n")
        continue
    if 'val currentPdfPageState =' in line:
        new_lines.append("    val currentPdfPageState by rememberUpdatedState(currentPdfPage)\n")
        continue
    
    if 'viewModel.saveProgress(bookId, currentChapterState.value, currentScrollState.value)' in line:
        new_lines.append("            viewModel.saveProgress(bookId, currentChapterState, currentScrollState)\n")
        continue
    if 'viewModel.savePdfProgress(bookId, currentPdfPageState.value)' in line:
        new_lines.append("            viewModel.savePdfProgress(bookId, currentPdfPageState)\n")
        continue

    if 'val globalProgress by remember(currentChapterIndex, scrollState.value, scrollState.maxValue)' in line:
        new_lines.append("    val globalProgress by remember(currentChapterIndex) {\n")
        skip = True
        continue
    if skip and 'derivedStateOf {' in line:
        new_lines.append("        derivedStateOf {\n")
        skip = False
        continue

    new_lines.append(line)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.writelines(new_lines)

