package com.jina.pocketlibrary.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jina.pocketlibrary.data.model.Book
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.jina.pocketlibrary.ui.components.BookGridLayout
import com.jina.pocketlibrary.ui.components.BookListLayout
import com.jina.pocketlibrary.ui.components.BookMasterDetailLayout
import com.jina.pocketlibrary.ui.components.DeviceType
import com.jina.pocketlibrary.ui.components.ErrorCard
import com.jina.pocketlibrary.ui.components.rememberDeviceType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBookClick: (Book) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val savedBooks by viewModel.savedBooks.collectAsState()
    val savedBookIds = remember(savedBooks) { savedBooks.map { it.id }.toSet() }

    var showManualEntryDialog by remember { mutableStateOf(false) }
    val deviceType = rememberDeviceType()

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    Scaffold(
        topBar = {
            when (deviceType) {
                DeviceType.PHONE_LANDSCAPE -> {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Search Books",
                                style = MaterialTheme.typography.titleMedium, // smaller title
                                maxLines = 1
                            )
                        },
                        windowInsets = WindowInsets.statusBars, // reduces extra padding
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }

                DeviceType.PHONE_PORTRAIT -> {
                    TopAppBar(
                        title = { Text("Search Books") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                DeviceType.TABLET_PORTRAIT -> {
                    TopAppBar(
                        title = { Text("Search Books") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                DeviceType.TABLET_LANDSCAPE -> {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Search Books",
                                style = MaterialTheme.typography.titleMedium, // smaller title
                                maxLines = 1
                            )
                        },
                        windowInsets = WindowInsets.statusBars, // reduces extra padding
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showManualEntryDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Book") }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateQuery(it) },
                onSearch = { viewModel.searchBook() },
                isLoading = isLoading
            )

            // Error Message
            errorMessage?.let { msg ->
                ErrorCard(msg)
            }

            // Results - ADAPTIVE LAYOUT
            when (deviceType) {
                DeviceType.PHONE_PORTRAIT -> {
                    // Portrait: LazyColumn
                    BookListLayout(
                        books = searchResults,
                        savedBookIds = savedBookIds,
                        onSaveClick = { viewModel.saveBook(it) },
                        onBookClick = onBookClick,
                        listState = listState
                    )
                }

                DeviceType.PHONE_LANDSCAPE -> {
                    // Landscape: 2-column grid
                    BookGridLayout(
                        books = searchResults,
                        savedBookIds = savedBookIds,
                        onSaveClick = { viewModel.saveBook(it) },
                        onBookClick = onBookClick,
                        gridState = gridState,
                        columns = 2
                    )
                }

                DeviceType.TABLET_PORTRAIT -> {
                    // Tablet Portrait: 2-column grid
                    BookGridLayout(
                        books = searchResults,
                        savedBookIds = savedBookIds,
                        onSaveClick = { viewModel.saveBook(it) },
                        onBookClick = onBookClick,
                        gridState = gridState,
                        columns = 2
                    )
                }

                DeviceType.TABLET_LANDSCAPE -> {
                    // Tablet Landscape: Master-Detail Split
                    BookMasterDetailLayout(
                        books = searchResults,
                        savedBookIds = savedBookIds,
                        onSaveClick = { viewModel.saveBook(it) },
                        listState = listState
                    )
                }
            }

            if (showManualEntryDialog) {
                ManualEntryDialog(
                    onDismiss = { showManualEntryDialog = false },
                    onSave = { book ->
                        viewModel.saveBook(book)
                        showManualEntryDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search books...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )

        Button(
            onClick = onSearch,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Search")
            }
        }
    }
}

@Composable
fun BookCard(
    book: Book,
    isSaved: Boolean = false,
    onSaveClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    // Animation state
    val scale by animateFloatAsState(
        targetValue = if (isSaved) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Book Cover
            Box(
                modifier = Modifier
                    .size(width = 70.dp, height = 100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (book.coverUrl != null || book.localPhotoPath != null) {
                    AsyncImage(
                        model = book.localPhotoPath ?: book.coverUrl,
                        contentDescription = book.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(android.R.drawable.ic_menu_gallery),
                        error = painterResource(android.R.drawable.ic_menu_gallery)
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Book Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                book.year?.let { year ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onClick != null) {
                    Text(
                        text = "Tap for details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Action Button
            Column(verticalArrangement = Arrangement.Center) {
                when {
                    onDeleteClick != null -> {
                        IconButton(onClick = onDeleteClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    onSaveClick != null -> {
                        IconButton(onClick = onSaveClick) {
                            Icon(
                                imageVector = if (isSaved)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isSaved)
                                    "Already saved"
                                else
                                    "Save to library",
                                tint = if (isSaved)
                                    Color.Red
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.scale(scale)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryDialog(
    onDismiss: () -> Unit,
    onSave: (Book) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        title = { Text("Add Book Manually") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    placeholder = { Text("e.g., Pride and Prejudice") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = { author = it },
                    label = { Text("Author *") },
                    placeholder = { Text("e.g., Jane Austen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = year,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            year = it
                        }
                    },
                    label = { Text("Year (optional)") },
                    placeholder = { Text("e.g., 1813") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "* Required fields",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank()) {
                        val book = Book(
                            id = "manual_${System.currentTimeMillis()}",
                            title = title.trim(),
                            author = author.trim(),
                            year = year.toIntOrNull(),
                            coverUrl = null
                        )
                        onSave(book)
                    }
                },
                enabled = title.isNotBlank() && author.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}