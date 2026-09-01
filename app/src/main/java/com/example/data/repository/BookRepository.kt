package com.example.data.repository

import com.example.data.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface BookRepository {
    fun getBooks(): Flow<List<Book>>
    fun getBookById(id: String): Flow<Book?>
}

class FakeBookRepository : BookRepository {
    private val books = listOf(
        Book(
            id = "1",
            title = "War and Peace",
            author = "Leo Tolstoy",
            description = "A sweeping epic of Russian society during the Napoleonic era.",
            content = "Well, Prince, so Genoa and Lucca are now just family estates of the Buonapartes..."
        ),
        Book(
            id = "2",
            title = "Anna Karenina",
            author = "Leo Tolstoy",
            description = "A tragic story of a married aristocrat/socialite and her affair with the affluent Count Vronsky.",
            content = "Happy families are all alike; every unhappy family is unhappy in its own way..."
        ),
        Book(
            id = "3",
            title = "The Death of Ivan Ilyich",
            author = "Leo Tolstoy",
            description = "A masterpiece of psychological realism about a man confronting his mortality.",
            content = "Ivan Ilyich's life had been most simple and most ordinary and therefore most terrible..."
        )
    )

    override fun getBooks(): Flow<List<Book>> = flowOf(books)

    override fun getBookById(id: String): Flow<Book?> = flowOf(books.find { it.id == id })
}
