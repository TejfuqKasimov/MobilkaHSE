package com.example.newsapp.data.repository

import com.example.newsapp.data.model.Article
import com.example.newsapp.data.remote.RetrofitInstance

class NewsRepository {
    suspend fun getEverythingPage(
        query: String = "*",
        language: String = "ru",
        page: Int,
        pageSize: Int = 20
    ): List<Article> {
        val response = RetrofitInstance.api.getEverything(
            query = query,
            language = language,
            page = page,
            pageSize = pageSize,
            apiKey = RetrofitInstance.API_KEY
        )
        return response.articles
    }
}