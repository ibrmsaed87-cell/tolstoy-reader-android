package com.example.data.repository

import android.content.Context
import com.example.data.local.BookDao
import com.example.data.local.BookEntity
import com.example.data.local.ChapterEntity
import com.example.data.model.Book
import com.example.data.model.Chapter
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class RemoteBookRepository(private val bookDao: BookDao) : BookRepository {
    private val api = RetrofitClient.tolstoyApiService
    private val baseUrl = "https://tolstoy.spinel.info"

    override fun getBooks(): Flow<List<Book>> = flow {
        // Fetch from local first
        bookDao.getAllBooks().collect { localBooks ->
            val mappedLocalBooks = localBooks.map { it.toDomainModel(emptyList()) }
            if (mappedLocalBooks.isNotEmpty()) {
                emit(mappedLocalBooks)
            }
            
            // Try to update from remote
            try {
                val response = api.getLibrary()
                val newBooks = response.books.map { dto ->
                    BookEntity(
                        id = dto.id,
                        title = dto.title,
                        language = dto.language,
                        bookOrder = dto.order,
                        format = dto.format,
                        fileUrl = if (dto.file.startsWith("http")) dto.file else "$baseUrl${dto.file}",
                        coverUrl = dto.cover?.let { if (it.startsWith("http")) it else "$baseUrl$it" },
                        author = "", // populated later
                        description = ""
                    )
                }
                
                bookDao.insertBooks(newBooks)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getBookById(id: String): Flow<Book?> = flow {
        bookDao.getBookById(id).collect { bookEntity ->
            if (bookEntity != null) {
                val localChapters = bookDao.getChaptersForBookSync(id)
                var domainBook = bookEntity.toDomainModel(localChapters)

                // If content is empty and it's json, try to download and cache
                if (localChapters.isEmpty() && bookEntity.format == "json") {
                    try {
                        val contentResponse = api.getBookContent(bookEntity.fileUrl)
                        val updatedEntity = bookEntity.copy(
                            author = contentResponse.author ?: "",
                            description = contentResponse.description ?: ""
                        )
                        bookDao.updateBook(updatedEntity)
                        
                        val chapterEntities = contentResponse.chapters?.mapIndexed { index, chapterDto ->
                            ChapterEntity(
                                bookId = id,
                                chapterIndex = index,
                                title = chapterDto.title,
                                content = chapterDto.content
                            )
                        } ?: emptyList()
                        
                        bookDao.insertChapters(chapterEntities)
                        
                        domainBook = updatedEntity.toDomainModel(chapterEntities)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                emit(domainBook)
            } else {
                emit(null)
            }
        }
    }
    
    suspend fun saveProgress(bookId: String, chapterIndex: Int, scrollPos: Int) {
        bookDao.updateProgress(bookId, chapterIndex, scrollPos)
    }

    suspend fun savePdfProgress(bookId: String, pdfPage: Int) {
        bookDao.updatePdfProgress(bookId, pdfPage)
    }

    suspend fun downloadBookOffline(bookId: String, context: Context) = withContext(Dispatchers.IO) {
        val bookEntity = bookDao.getBookByIdSync(bookId) ?: return@withContext
        try {
            if (bookEntity.format == "pdf") {
                val dir = File(context.filesDir, "downloaded_books")
                if (!dir.exists()) dir.mkdirs()
                
                val file = File(dir, "${bookId}.pdf")
                if (!file.exists()) {
                    val response = api.downloadFile(bookEntity.fileUrl)
                    val inputStream = response.byteStream()
                    val outputStream = FileOutputStream(file)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                bookDao.updateBookOfflineStatus(bookId, true, file.absolutePath)
            } else if (bookEntity.format == "json") {
                val localChapters = bookDao.getChaptersForBookSync(bookId)
                if (localChapters.isEmpty()) {
                    val contentResponse = api.getBookContent(bookEntity.fileUrl)
                    val updatedEntity = bookEntity.copy(
                        author = contentResponse.author ?: "",
                        description = contentResponse.description ?: ""
                    )
                    bookDao.updateBook(updatedEntity)
                    val chapterEntities = contentResponse.chapters?.mapIndexed { index, chapterDto ->
                        ChapterEntity(
                            bookId = bookId,
                            chapterIndex = index,
                            title = chapterDto.title,
                            content = chapterDto.content
                        )
                    } ?: emptyList()
                    bookDao.insertChapters(chapterEntities)
                }
                bookDao.updateBookOfflineStatus(bookId, true, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun removeDownloadOffline(bookId: String) = withContext(Dispatchers.IO) {
        val bookEntity = bookDao.getBookByIdSync(bookId) ?: return@withContext
        if (bookEntity.format == "pdf" && bookEntity.localPdfPath != null) {
            val file = File(bookEntity.localPdfPath)
            if (file.exists()) {
                file.delete()
            }
        }
        bookDao.updateBookOfflineStatus(bookId, false, null)
    }

    suspend fun getTemporaryPdf(book: Book, context: Context): File? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "${book.id}.pdf")
            if (file.exists()) {
                return@withContext file
            }
            
            val response = api.downloadFile(book.fileUrl)
            val inputStream = response.byteStream()
            val outputStream = FileOutputStream(file)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return@withContext file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun BookEntity.toDomainModel(chapters: List<ChapterEntity>): Book {
        return Book(
            id = id,
            title = title,
            author = author,
            description = description,
            content = chapters.joinToString("\n\n") { (it.title?.let { t -> "$t\n" } ?: "") + it.content }, // legacy compatibility
            chapters = chapters.map { Chapter(it.title, it.content) },
            coverUrl = coverUrl,
            language = language,
            order = bookOrder,
            format = format,
            fileUrl = fileUrl,
            isDownloaded = isDownloaded,
            currentChapterIndex = currentChapterIndex,
            scrollPosition = scrollPosition,
            currentPdfPage = currentPdfPage,
            localPdfPath = localPdfPath
        )
    }
}
