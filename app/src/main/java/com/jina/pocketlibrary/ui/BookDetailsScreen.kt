// ============================================
// FILE: ui/BookDetailScreen.kt
// ADD CAMERA & SHARE FEATURES
// ============================================
package com.jina.pocketlibrary.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.jina.pocketlibrary.data.model.Book
import java.io.File
import androidx.compose.ui.res.painterResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    onPhotoTaken: (Uri) -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current

    // Camera Permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera
    val photoFile = remember {
        File(context.filesDir, "book_${book.id}.jpg")
    }

    val photoUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onPhotoTaken(photoUri)
            Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show()
        }
    }

    // Contact Picker
    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { contactUri ->
        contactUri?.let {
            shareBookViaContact(context, book, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Details") },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Book Cover
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = book.localPhotoPath ?: book.coverUrl,
                    contentDescription = book.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Book Info
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "by ${book.author}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            book.year?.let {
                Text(
                    text = "Published: $it",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera Button
                Button(
                    onClick = {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                                cameraLauncher.launch(photoUri)
                            }
                            else -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = com.jina.pocketlibrary.R.drawable.baseline_photo_camera_24),
                        contentDescription = "Take photo"
                    )
                }

                // Share Button
                Button(
                    onClick = { contactPickerLauncher.launch(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
                }
            }
        }
    }
}

// Helper function to share book
fun shareBookViaContact(context: android.content.Context, book: Book, contactUri: Uri) {
    val cursor = context.contentResolver.query(
        contactUri,
        arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
        null, null, null
    )

    var contactName = "friend"
    cursor?.use {
        if (it.moveToFirst()) {
            contactName = it.getString(0) ?: "friend"
        }
    }

    val shareText = """
        Check out this book!
        
        📚 ${book.title}
        ✍️ by ${book.author}
        📅 ${book.year ?: "Unknown year"}
        
        Shared via Pocket Library
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        putExtra(Intent.EXTRA_SUBJECT, "Book Recommendation: ${book.title}")
    }

    context.startActivity(Intent.createChooser(intent, "Share with $contactName"))
}