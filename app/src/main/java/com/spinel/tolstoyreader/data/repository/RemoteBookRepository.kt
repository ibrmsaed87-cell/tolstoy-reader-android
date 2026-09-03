package com.spinel.tolstoyreader.data.repository

import android.content.Context
import com.spinel.tolstoyreader.data.local.BookDao
import com.spinel.tolstoyreader.data.local.BookEntity
import com.spinel.tolstoyreader.data.local.ChapterEntity
import com.spinel.tolstoyreader.data.model.Book
import com.spinel.tolstoyreader.data.model.Chapter
import com.spinel.tolstoyreader.data.network.RetrofitClient
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
            // Self-heal DB: populate missing series metadata
            val needsUpdate = localBooks.filter { it.seriesId == null && Regex("-(book|part|tom)-\\d+$").containsMatchIn(it.id) }
            if (needsUpdate.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    needsUpdate.forEach { entity ->
                        val match = Regex("-(book|part|tom)-(\\d+)$").find(entity.id)
                        if (match != null) {
                            val sId = entity.id.substring(0, match.range.first)
                            val sOrder = match.groupValues[2].toIntOrNull()
                            val sTitle = entity.title.split(" – ", " - ").firstOrNull() ?: entity.title
                            bookDao.updateBook(entity.copy(
                                seriesId = sId,
                                seriesOrder = sOrder,
                                seriesTitle = sTitle
                            ))
                        }
                    }
                }
            }
            
            val needsChapterCount = localBooks.filter { it.totalChapters == 0 && it.format == "json" }
            if (needsChapterCount.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    needsChapterCount.forEach { entity ->
                        val count = bookDao.getChapterCountSync(entity.id)
                        if (count > 0) {
                            bookDao.updateBook(entity.copy(totalChapters = count))
                        }
                    }
                }
            }
            
            val mappedLocalBooks = localBooks.map { it.toDomainModel(emptyList()) }
            if (mappedLocalBooks.isNotEmpty()) {
                // Group by seriesId
                val grouped = mutableListOf<Book>()
                val seriesMap = mappedLocalBooks.filter { it.seriesId != null }.groupBy { it.seriesId }
                val addedSeriesIds = mutableSetOf<String>()
                
                for (book in mappedLocalBooks) {
                    if (book.seriesId != null) {
                        if (!addedSeriesIds.contains(book.seriesId)) {
                            val parts = seriesMap[book.seriesId]?.sortedBy { it.seriesOrder } ?: emptyList()
                            val rep = parts.firstOrNull() ?: book
                            grouped.add(rep.copy(
                                id = book.seriesId,
                                title = book.seriesTitle ?: book.title,
                                parts = parts,
                                isFavorite = parts.any { it.isFavorite },
                                lastReadAt = parts.maxOfOrNull { it.lastReadAt } ?: 0L
                            ))
                            addedSeriesIds.add(book.seriesId)
                        }
                    } else {
                        grouped.add(book)
                    }
                }
                emit(grouped)
            }
            
            // Try to update from remote
            try {
                val response = api.getLibrary()
                val newBooks = response.books.map { dto ->
                    var seriesId: String? = null
                    var seriesTitle: String? = null
                    var seriesOrder: Int? = null
                    
                    val regex = Regex("-(book|part|tom)-(\\d+)$")
                    val match = regex.find(dto.id)
                    if (match != null) {
                        seriesId = dto.id.substring(0, match.range.first)
                        seriesOrder = match.groupValues[2].toIntOrNull()
                        seriesTitle = dto.title.split(" – ").firstOrNull() ?: dto.title
                    }
                    BookEntity(
                        id = dto.id,
                        seriesId = seriesId,
                        seriesTitle = seriesTitle,
                        seriesOrder = seriesOrder,
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
        val seriesParts = bookDao.getBooksBySeriesIdSync(id)
        if (seriesParts.isNotEmpty()) {
            val mappedParts = seriesParts.sortedBy { it.seriesOrder }.map { it.toDomainModel(emptyList()) }
            val rep = mappedParts.first()
            emit(rep.copy(
                id = id,
                title = rep.seriesTitle ?: rep.title,
                parts = mappedParts,
                isFavorite = mappedParts.any { it.isFavorite },
                lastReadAt = mappedParts.maxOfOrNull { it.lastReadAt } ?: 0L
            ))
            return@flow
        }
        
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
                        
                        val finalEntity = updatedEntity.copy(totalChapters = chapterEntities.size)
                        bookDao.updateBook(finalEntity)
                        domainBook = finalEntity.toDomainModel(chapterEntities)
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
    
    suspend fun toggleFavorite(bookId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        val seriesParts = bookDao.getBooksBySeriesIdSync(bookId)
        if (seriesParts.isNotEmpty()) {
            seriesParts.forEach { bookDao.updateFavorite(it.id, isFavorite) }
        } else {
            bookDao.updateFavorite(bookId, isFavorite)
        }
    }

    suspend fun saveProgress(bookId: String, chapterIndex: Int, scrollPos: Int) {
        bookDao.updateProgress(bookId, chapterIndex, scrollPos, System.currentTimeMillis())
    }

    suspend fun saveBookmark(bookId: String, hasBookmark: Boolean, chapterIndex: Int, scrollPos: Int) {
        bookDao.updateBookmark(bookId, hasBookmark, chapterIndex, scrollPos)
    }

    suspend fun savePdfProgress(bookId: String, pdfPage: Int) {
        bookDao.updatePdfProgress(bookId, pdfPage, System.currentTimeMillis())
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
            totalChapters = totalChapters,
            coverUrl = coverUrl,
            language = language,
            order = bookOrder,
            format = format,
            fileUrl = fileUrl,
            isDownloaded = isDownloaded,
            currentChapterIndex = currentChapterIndex,
            scrollPosition = scrollPosition,
            currentPdfPage = currentPdfPage,
            localPdfPath = localPdfPath,
            hasBookmark = hasBookmark,
            bookmarkChapterIndex = bookmarkChapterIndex,
            bookmarkScrollPosition = bookmarkScrollPosition,
            isFavorite = isFavorite,
            lastReadAt = lastReadAt,
            seriesId = seriesId,
            seriesTitle = seriesTitle,
            seriesOrder = seriesOrder
        )
    }
}
