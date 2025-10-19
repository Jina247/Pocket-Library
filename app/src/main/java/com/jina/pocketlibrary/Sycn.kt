package com.jina.pocketlibrary

import android.content.Context
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import com.jina.pocketlibrary.data.local.BookDatabase
import com.jina.pocketlibrary.data.model.toDomain
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = BookDatabase.getDatabase(applicationContext)
            val firestore = FirebaseFirestore.getInstance()

            // Get all local books
            val localBooks = database.bookDao().getAllBooks()

            // This is a Flow, so we need to collect it once
            var booksList = emptyList<com.jina.pocketlibrary.data.model.Book>()
            localBooks.collect { entities ->
                booksList = entities.map { it.toDomain() }
            }

            // Upload each book to Firebase
            booksList.forEach { book ->
                firestore.collection("books")
                    .document(book.id)
                    .set(book)
                    .await()
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "book_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )
        }
    }
}