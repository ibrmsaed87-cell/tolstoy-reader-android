package com.spinel.tolstoyreader.data.model

data class Chapter(
    val title: String?,
    val content: String
)

data class Book(
    val id: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val content: String = "", // Legacy for combined content
    val chapters: List<Chapter> = emptyList(),
    val coverUrl: String? = null,
    val language: String = "en",
    val order: Int = 0,
    val seriesId: String? = null,
    val seriesTitle: String? = null,
    val seriesOrder: Int? = null,
    val parts: List<Book> = emptyList(),
    val format: String = "json",
    val fileUrl: String = "",
    val isDownloaded: Boolean = false,
    val currentChapterIndex: Int = 0,
    val scrollPosition: Int = 0,
    val currentPdfPage: Int = 0,
    val localPdfPath: String? = null,
    val hasBookmark: Boolean = false,
    val bookmarkChapterIndex: Int = -1,
    val bookmarkScrollPosition: Int = 0,
    val isFavorite: Boolean = false,
    val lastReadAt: Long = 0L
)
