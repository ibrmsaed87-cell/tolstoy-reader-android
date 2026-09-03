import sys

with open('app/src/main/java/com/spinel/tolstoyreader/data/model/Book.kt', 'r') as f:
    content = f.read()

old_prop = """    val chapters: List<Chapter> = emptyList(),"""
new_prop = """    val chapters: List<Chapter> = emptyList(),
    val totalChapters: Int = 0,"""
content = content.replace(old_prop, new_prop)

with open('app/src/main/java/com/spinel/tolstoyreader/data/model/Book.kt', 'w') as f:
    f.write(content)
print("Updated Book.kt")
