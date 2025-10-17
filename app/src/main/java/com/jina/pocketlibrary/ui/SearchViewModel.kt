package com.jina.pocketlibrary.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jina.pocketlibrary.data.model.Book
import com.jina.pocketlibrary.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: BookRepository): ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateQuery(query: String) {
        _searchQuery.value = query
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
}
