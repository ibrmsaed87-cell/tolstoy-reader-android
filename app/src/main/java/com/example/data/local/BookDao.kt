package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY bookOrder ASC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdSync(id: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET isDownloaded = :isDownloaded WHERE id = :bookId")
    suspend fun setBookDownloaded(bookId: String, isDownloaded: Boolean)

    @Query("UPDATE books SET isDownloaded = :isDownloaded, localPdfPath = :localPdfPath WHERE id = :bookId")
    suspend fun updateBookOfflineStatus(bookId: String, isDownloaded: Boolean, localPdfPath: String?)

    @Query("UPDATE books SET currentPdfPage = :pdfPage WHERE id = :bookId")
    suspend fun updatePdfProgress(bookId: String, pdfPage: Int)

    @Query("UPDATE books SET currentChapterIndex = :chapterIndex, scrollPosition = :scrollPos WHERE id = :bookId")
    suspend fun updateProgress(bookId: String, chapterIndex: Int, scrollPos: Int)

    // Chapters
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    fun getChaptersForBook(bookId: String): Flow<List<ChapterEntity>>
    
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY chapterIndex ASC")
    suspend fun getChaptersForBookSync(bookId: String): List<ChapterEntity>

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersForBook(bookId: String)
}
