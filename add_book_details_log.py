import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'r') as f:
    content = f.read()

if "import android.util.Log" not in content:
    content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport android.util.Log")

book_decl = "val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)"
new_book_decl = """val book by viewModel.getBookByIdFlow(bookId).collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(book) {
        if (book != null) {
            Log.d("BOOK_NAV", "BOOK_DETAILS: routeId = $bookId, resolvedId = ${book?.id}, resolvedTitle = ${book?.title}")
            if (bookId != book?.id) {
                Log.e("BOOK_NAV", "MISMATCH in BookDetailsScreen! routeId=$bookId, resolvedId=${book?.id}")
            }
        }
    }"""
content = content.replace(book_decl, new_book_decl)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'w') as f:
    f.write(content)
