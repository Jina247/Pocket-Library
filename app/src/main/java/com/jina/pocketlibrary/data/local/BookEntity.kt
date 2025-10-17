package com.jina.pocketlibrary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val coverUrl: String?,
    val localPhotoPath: String? = null
)