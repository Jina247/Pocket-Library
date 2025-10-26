package com.jina.pocketlibrary.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jina.pocketlibrary.data.model.Book
import com.jina.pocketlibrary.ui.components.BookMasterDetailLayout
import com.jina.pocketlibrary.ui.components.DeviceType
import com.jina.pocketlibrary.ui.components.EmptyLibraryState
import com.jina.pocketlibrary.ui.components.LibraryGridLayout
import com.jina.pocketlibrary.ui.components.LibraryListLayout
import com.jina.pocketlibrary.ui.components.LibraryMasterDetailLayout
import com.jina.pocketlibrary.ui.components.rememberDeviceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookClick : (Book) -> Unit) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val savedBooks by viewModel.savedBooks.collectAsState()
    val deviceType = rememberDeviceType()

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Library (${savedBooks.size})") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search your library...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Saved Books
            if (savedBooks.isEmpty()) {
                EmptyLibraryState(searchQuery.isEmpty())
            } else {
                when (deviceType) {
                    DeviceType.PHONE_PORTRAIT -> {
                        LibraryListLayout(
                            books = savedBooks,
                            onDeleteClick = { viewModel.deleteBook(it) },
                            onBookClick = onBookClick,
                            listState = listState
                        )
                    }

                    DeviceType.PHONE_LANDSCAPE -> {
                        LibraryGridLayout(
                            books = savedBooks,
                            onDeleteClick = { viewModel.deleteBook(it) },
                            onBookClick = onBookClick,
                            columns = 2,
                            gridState = gridState
                        )
                    }

                    DeviceType.TABLET_PORTRAIT -> {
                        LibraryGridLayout(
                            books = savedBooks,
                            onDeleteClick = { viewModel.deleteBook(it) },
                            onBookClick = onBookClick,
                            columns = 2,
                            gridState = gridState
                        )
                    }

                    DeviceType.TABLET_LANDSCAPE -> {
                        LibraryMasterDetailLayout(
                            books = savedBooks,
                            onDeleteClick = { viewModel.deleteBook(it) },
                            onPhotoTaken = { book, uri -> viewModel.attachPhoto(book, uri) },
                            listState = listState
                        )
                    }
                }
            }
        }
    }
}