import sys
import re

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'LaunchedEffect\(bookId\) \{\s*viewModel\.loadBook\(bookId\)\s*\}\s*val book by viewModel\.selectedBook\.collectAsStateWithLifecycle\(\)',
                 r'val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)',
                 content)
content = re.sub(r'val book by viewModel\.selectedBook\.collectAsStateWithLifecycle\(\)',
                 r'val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)',
                 content)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'w') as f:
    f.write(content)

