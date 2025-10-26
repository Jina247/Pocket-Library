package com.jina.pocketlibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.jina.pocketlibrary.data.local.BookDatabase
import com.jina.pocketlibrary.data.remote.RetrofitInstance
import com.jina.pocketlibrary.data.repository.BookRepository
import com.jina.pocketlibrary.ui.*
import com.jina.pocketlibrary.utils.decodeBookId
import com.jina.pocketlibrary.utils.navigateToDetail

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Schedule background sync
        SyncWorker.scheduleSync(this)

        val repository = BookRepository(
            bookDao = BookDatabase.getDatabase(this).bookDao(),
            api = RetrofitInstance.api,
            firestore = FirebaseFirestore.getInstance()
        )

        setContent {
            MaterialTheme {
                PocketLibraryApp(repository)
            }
        }
    }
}

@Composable
fun PocketLibraryApp(repository: BookRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only if we're not on a detail screen
    val showBottomBar = currentRoute?.startsWith("detail") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Search, "Search") },
                        label = { Text("Search") },
                        selected = currentRoute == "search",
                        onClick = {
                            navController.navigate("search") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, "Library") },
                        label = { Text("Library") },
                        selected = currentRoute == "library",
                        onClick = {
                            navController.navigate("library") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "search",
            modifier = Modifier.padding(padding)
        ) {
            composable("search") {
                val viewModel: SearchViewModel = viewModel(factory = ViewModelFactory(repository))
                SearchScreen(
                    viewModel = viewModel,
                    onBookClick = { book ->
                        viewModel.saveBook(book)
                        navController.navigateToDetail(book.id)
                    }
                )
            }

            composable("library") {
                val viewModel: LibraryViewModel = viewModel(factory = ViewModelFactory(repository))
                LibraryScreen(
                    viewModel = viewModel,
                    onBookClick = { book ->
                        navController.navigateToDetail(book.id)
                    }
                )
            }

            composable(
                route = "detail/{bookId}",
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { backStackEntry ->
                val bookId = decodeBookId(backStackEntry.arguments?.getString("bookId"))
                val viewModel: LibraryViewModel = viewModel(factory = ViewModelFactory(repository))

                val savedBooks by viewModel.savedBooks.collectAsState()
                val book = savedBooks.find { it.id == bookId }

                book?.let {
                    BookDetailScreen(
                        book = it,
                        onPhotoTaken = { uri -> viewModel.attachPhoto(it, uri) },
                        onShare = { /* share logic */ },
                        onNavigateBack = { navController.popBackStack() }
                    )
                } ?: run {
                    // fallback if book not found
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(64.dp))
                            Text("Book not found")
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
        }
    }
}

