package com.example.data.local

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
    val format: String,
    val fileUrl: String,
    val isDownloaded: Boolean = false,
    val currentChapterIndex: Int = 0,
    val scrollPosition: Int = 0,
    val currentPdfPage: Int = 0,
    val localPdfPath: String? = null
)
