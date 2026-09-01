package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LibraryResponse(
    val version: Int,
    val languages: List<String>,
    val books: List<BookDto>
)

@JsonClass(generateAdapter = true)
data class BookDto(
    val id: String,
    val language: String,
    val order: Int,
    val title: String,
    val format: String,
    val file: String,
    val cover: String?
)

@JsonClass(generateAdapter = true)
data class BookContentDto(
    @Json(name = "book_id") val bookId: String,
    val title: String,
    val author: String?,
    val description: String?,
    val chapters: List<ChapterDto>?
)

@JsonClass(generateAdapter = true)
data class ChapterDto(
    val id: String,
    val title: String?,
    val content: String
)
