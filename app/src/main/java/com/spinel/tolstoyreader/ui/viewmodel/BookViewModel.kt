package com.spinel.tolstoyreader.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spinel.tolstoyreader.data.local.AppDatabase
import com.spinel.tolstoyreader.data.model.Book
import com.spinel.tolstoyreader.data.model.Quote
import com.spinel.tolstoyreader.data.model.QuotesData
import com.spinel.tolstoyreader.data.repository.BookRepository
import com.spinel.tolstoyreader.data.repository.RemoteBookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import com.spinel.tolstoyreader.data.local.ReadingPreferencesManager

class BookViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository: BookRepository = RemoteBookRepository(database.bookDao())
    private val readingPrefs = ReadingPreferencesManager(application)

    val fontSize: StateFlow<Float> = readingPrefs.fontSizeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 18f)
    val lineSpacing: StateFlow<Float> = readingPrefs.lineSpacingFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.5f)
    val appLanguage: StateFlow<String> = readingPrefs.appLanguageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")
    val appTheme: StateFlow<String> = readingPrefs.appThemeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val readingTheme: StateFlow<String> = readingPrefs.themeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")
    val autoScrollSpeed: StateFlow<Float> = readingPrefs.autoScrollSpeedFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentQuote = MutableStateFlow<Quote?>(null)
    val currentQuote: StateFlow<Quote?> = _currentQuote.asStateFlow()

    fun refreshQuote() {
        _currentQuote.value = QuotesData.getRandomQuote(appLanguage.value)
    }


    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val books: StateFlow<List<Book>> = combine(repository.getBooks(), _searchQuery, appLanguage) { allBooks, query, targetLang ->
                
        val filteredByLang = allBooks.filter { it.language == targetLang }
        
        if (query.isBlank()) {
            filteredByLang
        } else {
            filteredByLang.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(
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

    fun setFontSize(size: Float) {
        viewModelScope.launch { readingPrefs.setFontSize(size) }
    }

    fun setLineSpacing(spacing: Float) {
        viewModelScope.launch { readingPrefs.setLineSpacing(spacing) }
    }


    fun setAppLanguage(lang: String) {
        viewModelScope.launch { readingPrefs.setAppLanguage(lang) }
    }

        fun setAppTheme(theme: String) {
        viewModelScope.launch { readingPrefs.setAppTheme(theme) }
    }
    fun setReadingTheme(theme: String) {
        viewModelScope.launch { readingPrefs.setTheme(theme) }
    }
    
    fun setAutoScrollSpeed(speed: Float) {
        viewModelScope.launch { readingPrefs.setAutoScrollSpeed(speed) }
    }

    fun toggleFavorite(bookId: String, isFavorite: Boolean) {
        if (repository is RemoteBookRepository) {
            viewModelScope.launch {
                repository.toggleFavorite(bookId, isFavorite)
            }
        }
    }

    fun updateBookmark(bookId: String, hasBookmark: Boolean, chapterIndex: Int, scrollPosition: Int) {
        if (repository is RemoteBookRepository) {
            viewModelScope.launch {
                repository.saveBookmark(bookId, hasBookmark, chapterIndex, scrollPosition)
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
