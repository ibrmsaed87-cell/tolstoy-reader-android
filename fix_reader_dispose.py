import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

old_dispose = """        onDispose {
            viewModel.saveProgress(bookId, currentChapterState, currentScrollState)
            viewModel.savePdfProgress(bookId, currentPdfPageState)
            pdfRenderer?.close()
            pdfFileDescriptor?.close()
            tempPdfFile?.delete()
        }"""

new_dispose = """        onDispose {
            viewModel.saveProgress(bookId, currentChapterState, currentScrollState)
            viewModel.savePdfProgress(bookId, currentPdfPageState)
            try {
                pdfRenderer?.close()
            } catch (e: Exception) {
                // Ignore if already closed
            }
            try {
                pdfFileDescriptor?.close()
            } catch (e: Exception) {
                // Ignore
            }
            tempPdfFile?.delete()
        }"""

if old_dispose in content:
    content = content.replace(old_dispose, new_dispose)
    with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
