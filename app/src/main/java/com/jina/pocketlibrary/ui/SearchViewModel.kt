package com.jina.pocketlibrary.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jina.pocketlibrary.data.model.Book
import com.jina.pocketlibrary.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: BookRepository,
    private val savedStateHandle: SavedStateHandle
    ): ViewModel() {
    private val _searchQuery = savedStateHandle.getStateFlow("search_query", "")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val savedBooks: StateFlow<List<Book>> = repository.getLocalBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    init {
        if (_searchQuery.value.isNotEmpty()) {
            searchBook()
        }
    }
    fun updateQuery(query: String) {
        savedStateHandle["search_query"] = query
    }

    fun searchBook() {
        if (_searchQuery.value.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.searchOnline(_searchQuery.value).fold(
                onSuccess = { books ->
                    _searchResults.value = books
                    if (books.isEmpty()) {
                        _errorMessage.value = "No book found"
                    }
                },
                onFailure = { error ->
                    _errorMessage.value = "Search failed. You can add books manually."
                }
            )
        }
        _isLoading.value = false
    }

    fun saveBook(book: Book) {
        viewModelScope.launch {
            repository.saveBook(book)
        }
    }

    fun addBook(book: Book) {
        viewModelScope.launch {

        }
    }
}
