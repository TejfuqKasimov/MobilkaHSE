package com.example.newsapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsapp.databinding.ActivityMainBinding
import com.example.newsapp.ui.NewsAdapter
import com.example.newsapp.ui.NewsViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        binding.swipeRefresh.setOnRefreshListener {
            // При свайпе вниз перезагружаем первую страницу
            viewModel.refresh()
        }
    }

    private fun setupRecyclerView() {
        adapter = NewsAdapter(showLoading = false)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Слушатель скролла для подгрузки
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Если дошли до конца списка и не идёт загрузка
                if (!viewModel.isLoading.value!! &&
                    (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.articles.observe(this) { articles ->
            // Передаём в адаптер новый список (и флаг загрузки)
            adapter.submitList(articles)
            adapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Показываем или убираем ProgressBar в футере
            // Для этого пересоздаём адаптер с новым флагом (или меняем флаг и вызываем notify)
            (adapter as NewsAdapter).submitList(viewModel.articles.value.orEmpty())
            adapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = isLoading && viewModel.articles.value.isNullOrEmpty()
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, "Ошибка: $it", Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
            binding.swipeRefresh.isRefreshing = false
        }
    }
}