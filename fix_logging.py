import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

if "import android.util.Log" not in content:
    content = content.replace("package com.spinel.tolstoyreader.ui.navigation\n", "package com.spinel.tolstoyreader.ui.navigation\n\nimport android.util.Log\n")

# Intercept HomeScreen onBookClick
home_block = """                        onBookClick = { bookId ->
                            activity?.let {"""
new_home_block = """                        onBookClick = { bookId ->
                            Log.d("BOOK_NAV", "BOOK_CLICK: requestedId = $bookId")
                            activity?.let {"""
content = content.replace(home_block, new_home_block)

home_read_block = """                        onReadClick = { bookId ->
                            activity?.let {"""
new_home_read_block = """                        onReadClick = { bookId ->
                            Log.d("BOOK_NAV", "READ_CLICK: requestedId = $bookId")
                            activity?.let {"""
content = content.replace(home_read_block, new_home_read_block)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)

