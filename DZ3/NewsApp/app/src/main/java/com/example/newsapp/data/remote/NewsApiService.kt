package com.example.newsapp.data.remote

import com.example.newsapp.data.model.NewsApiResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NewsApiService {
    @GET("everything")
    suspend fun getEverything(
        @Query("q") query: String = "*",            // * – все новости
        @Query("language") language: String = "ru", // русский язык
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Header("X-Api-Key") apiKey: String
    ): NewsApiResponse
}