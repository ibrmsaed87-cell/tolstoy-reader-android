package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Book
import com.example.data.repository.BookRepository
import com.example.data.repository.RemoteBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class BookViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository: BookRepository = RemoteBookRepository(database.bookDao())

    private val currentLanguage = Locale.getDefault().language

    val books: StateFlow<List<Book>> = repository.getBooks()
        .map { allBooks -> 
            val supportedLangs = listOf("ar", "en", "ru")
            val targetLang = if (currentLanguage in supportedLangs) currentLanguage else "en"
            allBooks.filter { it.language == targetLang }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedBook = MutableStateFlow<Book?>(null)
    val selectedBook: StateFlow<Book?> = _selectedBook.asStateFlow()

    fun loadBook(id: String) {
        viewModelScope.launch {
            repository.getBookById(id).collect { book ->
                _selectedBook.value = book
            }
        }
    }
    
    fun saveProgress(bookId: String, chapterIndex: Int, scrollPosition: Int) {
        if (repository is RemoteBookRepository) {
            viewModelScope.launch {
                repository.saveProgress(bookId, chapterIndex, scrollPosition)
            }
        }
    }

    fun savePdfProgress(bookId: String, pdfPage: Int) {
        if (repository is RemoteBookRepository) {
            viewModelScope.launch {
                repository.savePdfProgress(bookId, pdfPage)
            }
        }
    }

    fun downloadBookOffline(bookId: String, context: Context) {
        if (repository is RemoteBookRepository) {
            viewModelScope.launch {
                repository.downloadBookOffline(bookId, context)
            }
        }
    }

    fun removeDownloadOffline(bookId: String) {
        if (repository is RemoteBookRepository) {
            viewModelScope.launch {
                repository.removeDownloadOffline(bookId)
            }
        }
    }

    suspend fun getTemporaryPdf(book: Book, context: Context): File? {
        return if (repository is RemoteBookRepository) {
            repository.getTemporaryPdf(book, context)
        } else null
    }
}
