import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

if "import android.util.Log" not in content:
    content = content.replace("import android.content.Context", "import android.content.Context\nimport android.util.Log")

book_decl = "val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)"
new_book_decl = """val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(book) {
        if (book != null) {
            Log.d("BOOK_NAV", "READER: routeId = $bookId, resolvedId = ${book?.id}, resolvedTitle = ${book?.title}")
            if (bookId != book?.id) {
                Log.e("BOOK_NAV", "MISMATCH in ReaderScreen! routeId=$bookId, resolvedId=${book?.id}")
            }
        }
    }"""
content = content.replace(book_decl, new_book_decl)

# Check if there is anything else that uses selectedBook
with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
