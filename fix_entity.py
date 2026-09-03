import sys

with open('app/src/main/java/com/spinel/tolstoyreader/data/local/BookEntity.kt', 'r') as f:
    content = f.read()

old_prop = """    val seriesOrder: Int? = null,"""
new_prop = """    val seriesOrder: Int? = null,
    val totalChapters: Int = 0,"""
content = content.replace(old_prop, new_prop)

with open('app/src/main/java/com/spinel/tolstoyreader/data/local/BookEntity.kt', 'w') as f:
    f.write(content)
print("Updated BookEntity.kt")
