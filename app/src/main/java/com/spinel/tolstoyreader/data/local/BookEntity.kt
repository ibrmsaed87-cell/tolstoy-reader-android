package com.spinel.tolstoyreader.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val description: String,
    val coverUrl: String?,
    val language: String,
    val bookOrder: Int,
    val seriesId: String? = null,
    val seriesTitle: String? = null,
    val seriesOrder: Int? = null,
    val totalChapters: Int = 0,
    val format: String,
    val fileUrl: String,
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
