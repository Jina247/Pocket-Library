package com.jina.pocketlibrary.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jina.pocketlibrary.data.local.BookDao
import com.jina.pocketlibrary.data.remote.OpenLibraryApi
import com.jina.pocketlibrary.data.model.Book
import com.jina.pocketlibrary.data.model.toDomain
import com.jina.pocketlibrary.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BookRepository(
    private val bookDao: BookDao,
    private val api: OpenLibraryApi,
    private val firestore: FirebaseFirestore
) {
    // Get local books
    fun getLocalBooks(): Flow<List<Book>> {
        return bookDao.getAllBooks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Search local books
    fun searchLocalBooks(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // Search online
    suspend fun searchOnline(query: String): Result<List<Book>> {
        return try {
            val response = api.searchBooks(query)
            val books = response.docs.mapNotNull { dto ->
                if (dto.key != null) {
                    Book(
                        id = dto.key.replace("/works/", "").replace("/", "_"),
                        title = dto.title,
                        author = dto.authorName.firstOrNull() ?: "Unknown",
                        year = dto.firstPublishYear,
                        coverUrl = dto.coverId?.let {
                            "https://covers.openlibrary.org/b/id/$it-M.jpg"
                        }
                    )
                } else null
            }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save book locally and to Firebase
    suspend fun saveBook(book: Book) {
        bookDao.insertBook(book.toEntity())
        syncToFirebase(book)
    }

    // Delete book
    suspend fun deleteBook(book: Book) {
        bookDao.deleteBook(book.toEntity())
        deleteFromFirebase(book)
    }

    // Update book (for photos)
    suspend fun updateBook(book: Book) {
        bookDao.insertBook(book.toEntity())
        syncToFirebase(book)
    }

    suspend fun syncFromFirebase(): Result<Int> {
        return try {
            val syncedBook = fetchFromFirebase()
            var count: Int = 0

            syncedBook.forEach { book ->
                val existingBook = bookDao.getBookById(book.id)
                if (existingBook == null) {
                    bookDao.insertBook(book.toEntity())
                    count++
                }

            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firebase sync
    private suspend fun syncToFirebase(book: Book) {
        try {
            firestore.collection("books")
                .document(book.id)
                .set(book)
                .await()
        } catch (_: Exception) {

        }
    }

    private suspend fun deleteFromFirebase(book: Book) {
        try {
            firestore.collection("books")
                .document(book.id)
                .delete()
                .await()
        } catch (_: Exception) {

        }
    }

    // Fetch from Firebase
    suspend fun fetchFromFirebase(): List<Book> {
        return try {
            val snapshot = firestore.collection("books").get().await()
            snapshot.documents.mapNotNull { it.toObject(Book::class.java) }
        } catch (_: Exception) {
            emptyList()
        }
    }
}