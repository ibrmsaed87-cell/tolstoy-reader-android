import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

if "import android.net.Uri" not in content:
    content = content.replace("import android.util.Log\n", "import android.util.Log\nimport android.net.Uri\n")

content = content.replace('fun createRoute(bookId: String) = "book_details/$bookId"', 'fun createRoute(bookId: String) = "book_details/${Uri.encode(bookId)}"')
content = content.replace('fun createRoute(bookId: String) = "reader/$bookId"', 'fun createRoute(bookId: String) = "reader/${Uri.encode(bookId)}"')

with open('app/src/main/java/com/spinel/tolstoyreader/ui/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
