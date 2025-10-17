package com.jina.pocketlibrary.data.model

import com.jina.pocketlibrary.data.local.BookEntity


data class Book (
    val id: String,
    val title: String,
    val author: String,
    val year: Int?,
    val coverUrl: String?,
    val localPhotoPath: String? = null
)

fun BookEntity.toDomain() = Book(
    id = id,
    title = title,
    author = author,
    year = year,
    coverUrl = coverUrl,
    localPhotoPath = localPhotoPath
)

fun Book.toEntity() = BookEntity(
    id = id,
    title = title,
    author = author,
    year = year,
    coverUrl = coverUrl,
    localPhotoPath = localPhotoPath
)