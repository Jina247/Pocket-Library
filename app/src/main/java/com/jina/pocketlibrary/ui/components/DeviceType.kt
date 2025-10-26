package com.jina.pocketlibrary.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.jina.pocketlibrary.data.model.Book
import com.jina.pocketlibrary.ui.BookCard
import com.jina.pocketlibrary.ui.BookDetailScreen

enum class DeviceType {
    PHONE_PORTRAIT,        // Compact width, portrait
    PHONE_LANDSCAPE,       // Compact width, landscape
    TABLET_PORTRAIT,       // Medium/Expanded width, portrait
    TABLET_LANDSCAPE       // Medium/Expanded width, landscape
}

@Composable
fun rememberDeviceType(): DeviceType {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val isLandscape = screenWidth > screenHeight

    return when {
        screenWidth >= 600 && isLandscape -> DeviceType.TABLET_LANDSCAPE  // Split view
        screenWidth >= 600 && !isLandscape -> DeviceType.TABLET_PORTRAIT // 2-col grid
        isLandscape -> DeviceType.PHONE_LANDSCAPE                        // 2-col grid
        else -> DeviceType.PHONE_PORTRAIT                                // Single column
    }
}


@Composable
fun BookListLayout(
    books: List<Book>,
    savedBookIds: Set<String>,
    onSaveClick: (Book) -> Unit,
    onBookClick: (Book) -> Unit,
    listState: LazyListState
) {
    LazyColumn(state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)

    ) {
        items(books, key = { it.id }) { book ->
            BookCard(
                book = book,
                isSaved = savedBookIds.contains(book.id),
                onSaveClick = { onSaveClick(book) },
                onClick = { onBookClick(book) }
            )
        }
    }
}

// 2) LANDSCAPE - LazyVerticalGrid
@Composable
fun BookGridLayout(
    books: List<Book>,
    savedBookIds: Set<String>,
    onSaveClick: (Book) -> Unit,
    onBookClick: (Book) -> Unit,
    gridState: LazyGridState,
    columns: Int = 2
) {
    LazyVerticalGrid(state = gridState ,
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookCard(
                book = book,
                isSaved = savedBookIds.contains(book.id),
                onSaveClick = { onSaveClick(book) },
                onClick = { onBookClick(book) }
            )
        }
    }
}

// 3) TABLET - Master-Detail Split
@Composable
fun BookMasterDetailLayout(
    books: List<Book>,
    savedBookIds: Set<String>,
    onSaveClick: (Book) -> Unit,
    listState: LazyListState
) {
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    Row(Modifier.fillMaxSize()) {
        LazyColumn(state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books, key = { it.id }) { book ->
                BookCard(
                    book = book,
                    isSaved = savedBookIds.contains(book.id),
                    onSaveClick = { onSaveClick(book) },
                    onClick = { selectedBook = book }
                )
            }
        }

        // Divider
        VerticalDivider()

        // Detail - Book Details
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            if (selectedBook != null) {
                BookDetailContent(book = selectedBook!!)
            } else {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    androidx.compose.material.icons.Icons.Default.Menu
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Select a book to view details",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


// ============================================
// LAYOUT COMPONENTS - LIBRARY
// ============================================

// 1) PORTRAIT - LazyColumn
@Composable
fun LibraryListLayout(
    books: List<Book>,
    onDeleteClick: (Book) -> Unit,
    onBookClick: (Book) -> Unit,
    listState: LazyListState
) {
    LazyColumn(state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookCard(
                book = book,
                onDeleteClick = { onDeleteClick(book) },
                onClick = { onBookClick(book) }
            )
        }
    }
}

// 2) LANDSCAPE - LazyVerticalGrid
@Composable
fun LibraryGridLayout(
    books: List<Book>,
    onDeleteClick: (Book) -> Unit,
    onBookClick: (Book) -> Unit,
    gridState:  LazyGridState,
    columns: Int = 2
) {
    LazyVerticalGrid(state = gridState,
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(books, key = { it.id }) { book ->
            BookCard(
                book = book,
                onDeleteClick = { onDeleteClick(book) },
                onClick = { onBookClick(book) }
            )
        }
    }
}

// 3) TABLET - Master-Detail Split
@Composable
fun LibraryMasterDetailLayout(
    books: List<Book>,
    onDeleteClick: (Book) -> Unit,
    onPhotoTaken: (Book, android.net.Uri) -> Unit,
    listState: LazyListState
) {
    var selectedBook by remember { mutableStateOf<Book?>(null) }
    Row(Modifier.fillMaxSize()) {
        // Master - List
        LazyColumn(state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(books, key = { it.id }) { book ->
                BookCard(
                    book = book,
                    onDeleteClick = { onDeleteClick(book) },
                    onClick = { selectedBook = book }
                )
            }
        }

        // Divider
        VerticalDivider()

        // Detail - Full Detail Screen
        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight()
        ) {
            if (selectedBook != null) {
                BookDetailScreen(
                    book = selectedBook!!,
                    onPhotoTaken = { uri ->
                        onPhotoTaken(selectedBook!!, uri)
                    },
                    onShare = { /* Handle share */ },
                    onNavigateBack = { }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    androidx.compose.material.icons.Icons.Default.Favorite
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Select a book to view details",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================
// HELPER COMPONENTS
// ============================================

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material.icons.Icons.Default.Info
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
fun EmptyLibraryState(isEmptySearch: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            androidx.compose.material.icons.Icons.Default.Favorite
            Text(
                text = if (isEmptySearch) "No books saved yet" else "No books found",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun BookDetailContent(book: Book) {
    // Simple detail view for tablet master-detail
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "by ${book.author}",
            style = MaterialTheme.typography.titleMedium
        )
        book.year?.let {
            Text(text = "Published: $it")
        }
    }
}