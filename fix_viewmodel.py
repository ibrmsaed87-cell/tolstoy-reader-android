import sys
import re

with open('app/src/main/java/com/spinel/tolstoyreader/ui/viewmodel/BookViewModel.kt', 'r') as f:
    content = f.read()

# Remove _selectedBook, selectedBook, and loadBook
pattern_to_remove = r'private val _selectedBook = MutableStateFlow<Book\?>\(null\)\s*val selectedBook: StateFlow<Book\?> = _selectedBook\.asStateFlow\(\)\s*private var loadBookJob: kotlinx\.coroutines\.Job\? = null\s*fun loadBook\(id: String\) \{.*?\s*\}\s*\}'
content = re.sub(pattern_to_remove, '', content, flags=re.DOTALL)

# Let's insert getBookByIdFlow right after _searchQuery
new_func = """
    fun getBookByIdFlow(id: String): kotlinx.coroutines.flow.Flow<Book?> {
        return repository.getBookById(id)
    }
"""

content = content.replace('private val _searchQuery = MutableStateFlow("")', 
                          'private val _searchQuery = MutableStateFlow("")\n' + new_func)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/viewmodel/BookViewModel.kt', 'w') as f:
    f.write(content)
print("Fixed BookViewModel")
