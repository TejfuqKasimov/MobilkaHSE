package com.example.newsapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.model.Article
import com.example.newsapp.data.repository.NewsRepository
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val repository = NewsRepository()

    // Общий список всех загруженных статей
    private val _articles = MutableLiveData<List<Article>>()
    val articles: LiveData<List<Article>> = _articles

    // Флаг загрузки (показываем индикатор внизу списка)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Ошибка
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Текущая страница, которую нужно загрузить
    private var currentPage = 1
    // Флаг, что достигнут конец (больше нет данных)
    private var isLastPage = false

    init {
        // Загружаем первую страницу при старте
        fetchFirstPage()
    }

    private fun fetchFirstPage() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                currentPage = 1
                isLastPage = false
                val newArticles = repository.getEverythingPage(
                    query = "*",      // можно заменить на "новости" или "спорт"
                    language = "ru",
                    page = currentPage
                )
                _articles.value = newArticles
                if (newArticles.isEmpty()) isLastPage = true
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun refresh() {
        fetchFirstPage()
    }
    fun loadNextPage() {
        // Если уже идёт загрузка или это последняя страница — ничего не делаем
        if (_isLoading.value == true || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val nextPage = currentPage + 1
                val newArticles = repository.getEverythingPage(
                    query = "*",      // можно заменить на "новости" или "спорт"
                    language = "ru",
                    page = currentPage
                )
                if (newArticles.isEmpty()) {
                    isLastPage = true
                } else {
                    val currentList = _articles.value.orEmpty().toMutableList()
                    currentList.addAll(newArticles)
                    _articles.value = currentList
                    currentPage = nextPage
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}