import sys

# BookDetailsScreen
with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("LaunchedEffect(bookId) {\n        viewModel.loadBook(bookId)\n    }\n\n    val book by viewModel.selectedBook.collectAsStateWithLifecycle()",
                          "val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)")
content = content.replace("LaunchedEffect(bookId) {\n        viewModel.loadBook(bookId)\n    }\n    val book by viewModel.selectedBook.collectAsStateWithLifecycle()",
                          "val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)")

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'w') as f:
    f.write(content)

# ReaderScreen
with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("val book by viewModel.selectedBook.collectAsStateWithLifecycle()",
                          "val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)")

# There might be a LaunchedEffect in ReaderScreen as well
import re
content = re.sub(r'LaunchedEffect\(bookId\) \{\s*viewModel\.loadBook\(bookId\)\s*\}', '', content)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)

print("Fixed screens")
