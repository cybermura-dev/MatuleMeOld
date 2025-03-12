package ru.takeshiko.matuleme.presentation.search

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.takeshiko.matuleme.data.remote.SupabaseClientManager
import ru.takeshiko.matuleme.data.repository.UserSearchQueryRepositoryImpl
import ru.takeshiko.matuleme.domain.models.database.UserSearchQuery
import ru.takeshiko.matuleme.domain.models.result.DataResult

class SearchViewModel(
    private val supabaseClientManager: SupabaseClientManager
) : ViewModel() {

    private val _searchQueriesResult = MutableLiveData<DataResult<List<UserSearchQuery>>>()
    val searchQueriesResult: LiveData<DataResult<List<UserSearchQuery>>> = _searchQueriesResult

    private val userSearchQueryRepository = UserSearchQueryRepositoryImpl(supabaseClientManager)

    fun loadSearchQueries() {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userSearchQueryRepository.getRecentQueriesByUser(userId, 10)) {
                    is DataResult.Success -> {
                        val uniqueQueries = result.data
                            .distinctBy { it.query.lowercase() }
                            .sortedByDescending { it.searchedAt }

                        _searchQueriesResult.value = DataResult.Success(uniqueQueries)
                    }
                    is DataResult.Error -> _searchQueriesResult.value = DataResult.Error(result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun logQuery(query: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                val queryConstructor = UserSearchQuery(
                    userId = userId,
                    query = query,
                    searchedAt = Clock.System.now()
                )
                userSearchQueryRepository.logQuery(queryConstructor)
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }

    fun deleteQuery(query: String) {
        viewModelScope.launch {
            supabaseClientManager.auth.currentUserOrNull()?.id?.let { userId ->
                when (val result = userSearchQueryRepository.deleteQuery(userId, query)) {
                    is DataResult.Success -> loadSearchQueries()
                    is DataResult.Error -> Log.d(javaClass.name, result.message)
                }
            } ?: run {
                Log.d(javaClass.name, "User not authenticated!")
            }
        }
    }
}