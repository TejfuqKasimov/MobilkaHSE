package com.example.newsapp.data.model

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("source") val source: Source?
)

data class Source(
    @SerializedName("name") val name: String?
)