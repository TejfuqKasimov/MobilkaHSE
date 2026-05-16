package com.example.newsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newsapp.data.model.Article
import com.example.newsapp.databinding.ItemArticleBinding
import com.example.newsapp.databinding.ItemLoadingBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NewsAdapter(
    private val showLoading: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ARTICLE = 0
        private const val TYPE_LOADING = 1
    }

    private var articles: List<Article> = emptyList()

    fun submitList(newArticles: List<Article>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    fun isShowLoading(): Boolean = showLoading

    override fun getItemViewType(position: Int): Int {
        return if (showLoading && position == itemCount - 1) {
            TYPE_LOADING
        } else {
            TYPE_ARTICLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ARTICLE -> {
                val binding = ItemArticleBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ArticleViewHolder(binding)
            }
            TYPE_LOADING -> {
                val binding = ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LoadingViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ArticleViewHolder) {
            holder.bind(articles[position])
        }
        // LoadingViewHolder не требует привязки данных
    }

    override fun getItemCount(): Int {
        return if (showLoading) articles.size + 1 else articles.size
    }

    class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.titleTextView.text = article.title ?: "Без заголовка"
            binding.descriptionTextView.text = article.description ?: ""

            // Дата
            binding.dateTextView.text = formatDate(article.publishedAt)
            binding.sourceTextView.text = article.source?.name ?: "Источник неизвестен"

            // Картинка
            Glide.with(binding.root)
                .load(article.urlToImage)
                .centerCrop()
                .into(binding.imageView)
        }

        private fun formatDate(rawDate: String?): String {
            if (rawDate.isNullOrBlank()) return "Дата неизвестна"
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("ru"))
                val date = inputFormat.parse(rawDate)
                date?.let { outputFormat.format(it) } ?: rawDate
            } catch (e: Exception) {
                rawDate // fallback на случай совсем нестандартного формата
            }
        }
    }

    class LoadingViewHolder(binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)
}