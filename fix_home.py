import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_prog = """                val progress = if (book.chapters.isNotEmpty()) {
                    ((book.currentChapterIndex + 1).toFloat() / book.chapters.size.toFloat()).coerceIn(0f, 1f)
                } else if (book.format == "pdf" && book.currentPdfPage > 0) {
                    0.5f // We don't know total pages for PDF
                } else {
                    0f
                }"""
new_prog = """                val progress = if (book.totalChapters > 0) {
                    ((book.currentChapterIndex + 1).toFloat() / book.totalChapters.toFloat()).coerceIn(0f, 1f)
                } else if (book.chapters.isNotEmpty()) {
                    ((book.currentChapterIndex + 1).toFloat() / book.chapters.size.toFloat()).coerceIn(0f, 1f)
                } else if (book.format == "pdf" && book.currentPdfPage > 0) {
                    0.5f // We don't know total pages for PDF
                } else {
                    0f
                }"""
if old_prog in content:
    content = content.replace(old_prog, new_prog)
    print("Fixed progress logic in HomeScreen")
else:
    print("Failed to find progress logic")

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
