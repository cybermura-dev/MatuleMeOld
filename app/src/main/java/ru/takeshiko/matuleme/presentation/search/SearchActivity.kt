package ru.takeshiko.matuleme.presentation.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ru.takeshiko.matuleme.data.adapters.SearchQueryShimmerAdapter
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.databinding.ActivitySearchBinding
import ru.takeshiko.matuleme.domain.models.result.DataResult
import ru.takeshiko.matuleme.presentation.searchresult.SearchResultActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(SupabaseClientManager.getInstance())
    }
    private lateinit var shimmerAdapter: SearchQueryShimmerAdapter
    private lateinit var adapter: SearchQueryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)

        with (binding) {
            setContentView(root)

            shimmerAdapter = SearchQueryShimmerAdapter(5)
            adapter = SearchQueryAdapter(
                onItemClick = { query ->
                    startActivity(Intent(this@SearchActivity, SearchResultActivity::class.java).apply {
                        putExtra("query", query.query)
                    })
                    finish()
                },
                onDeleteClick = { query ->
                    viewModel.deleteQuery(query.query)
                }
            )

            rvSearchQueries.apply {
                layoutManager = LinearLayoutManager(this@SearchActivity)
                adapter = shimmerAdapter
            }

            etSearch.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == android.view.KeyEvent.KEYCODE_ENTER)) {

                    val query = etSearch.text.toString().trim()
                    if (query.isNotEmpty()) {
                        viewModel.logQuery(query)
                        startActivity(Intent(this@SearchActivity, SearchResultActivity::class.java).apply {
                            putExtra("query", query)
                        })
                        finish()
                    }
                    return@setOnEditorActionListener true
                }
                false
            }

            viewModel.searchQueriesResult.observe(this@SearchActivity) { result ->
                when (result) {
                    is DataResult.Success -> {
                        adapter.submitList(result.data)
                        rvSearchQueries.adapter = adapter
                    }
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadSearchQueries()
    }
}