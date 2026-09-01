package com.example.data.model

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
    val format: String = "json",
    val fileUrl: String = "",
    val isDownloaded: Boolean = false,
    val currentChapterIndex: Int = 0,
    val scrollPosition: Int = 0,
    val currentPdfPage: Int = 0,
    val localPdfPath: String? = null
)
