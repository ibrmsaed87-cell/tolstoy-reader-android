package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
data class ChapterEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val chapterIndex: Int,
    val title: String?,
    val content: String
)
