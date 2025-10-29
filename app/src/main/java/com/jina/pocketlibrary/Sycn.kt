package com.jina.pocketlibrary

import android.content.Context
import androidx.work.*
import com.google.firebase.firestore.FirebaseFirestore
import com.jina.pocketlibrary.data.local.BookDatabase
import com.jina.pocketlibrary.data.model.toDomain
import kotlinx.coroutines.flow.first
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

            val localBooks = database.bookDao().getAllBooks().first()
            val booksList = localBooks.map { it.toDomain() }

            // Upload each book to Firebase
            booksList.forEach { book ->
                try {
                    firestore.collection("books")
                        .document(book.id)
                        .set(book)
                        .await()
                } catch (e: Exception) {
                }
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
                15, TimeUnit.MINUTES  // Minimum is 15 minutes for PeriodicWork
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