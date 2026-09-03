import sys

with open('app/src/main/java/com/spinel/tolstoyreader/data/local/BookDao.kt', 'r') as f:
    content = f.read()

old_prop = """    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>>"""
new_prop = """    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>>
    
    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId")
    suspend fun getChapterCountSync(bookId: String): Int"""
content = content.replace(old_prop, new_prop)

with open('app/src/main/java/com/spinel/tolstoyreader/data/local/BookDao.kt', 'w') as f:
    f.write(content)
print("Updated BookDao.kt")
