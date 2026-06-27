package org.ahugenb.movieswiper.feature.browser

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.*
import org.ahugenb.movieswiper.core.models.WatchlistItem
import org.ahugenb.movieswiper.core.models.Movie
import org.ahugenb.movieswiper.core.logic.TagBasedRecommendationEngine
import org.ahugenb.movieswiper.data.localdb.MovieRepository

interface BrowserComponent {
    val state: Value<State>
    
    fun refresh()
    fun onSortOrderChanged(sortOrder: SortOrder)
    fun onRemoveFromWatchlist(movieId: Int)
    fun onToggleWatched(movieId: Int, isWatched: Boolean)

    data class State(
        val items: List<WatchlistItem> = emptyList(),
        val isLoading: Boolean = false,
        val sortOrder: SortOrder = SortOrder.MATCH_ORDER
    )

    enum class SortOrder(val label: String) {
        ALPHABETICAL("A-Z"),
        RELEASE_DATE("Release Date"),
        MATCH_ORDER("Recently Matched"),
        AFFINITY("Affinity")
    }
}

class DefaultBrowserComponent(
    componentContext: ComponentContext,
    private val repository: MovieRepository
) : BrowserComponent, ComponentContext by componentContext {

    private val _state = MutableValue(BrowserComponent.State())
    override val state: Value<BrowserComponent.State> = _state

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        refresh()
    }

    override fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        scope.launch {
            val items = fetchAndSort(state.value.sortOrder)
            _state.value = _state.value.copy(items = items, isLoading = false)
        }
    }

    override fun onSortOrderChanged(sortOrder: BrowserComponent.SortOrder) {
        _state.value = _state.value.copy(sortOrder = sortOrder, isLoading = true)
        scope.launch {
            val items = fetchAndSort(sortOrder)
            _state.value = _state.value.copy(items = items, isLoading = false)
        }
    }

    override fun onRemoveFromWatchlist(movieId: Int) {
        scope.launch {
            repository.removeFromWatchlist(movieId)
            refresh()
        }
    }

    override fun onToggleWatched(movieId: Int, isWatched: Boolean) {
        scope.launch {
            repository.toggleWatchedStatus(movieId, isWatched)
            refresh()
        }
    }

    private suspend fun fetchAndSort(sortOrder: BrowserComponent.SortOrder): List<WatchlistItem> {
        val watchlist = repository.getWatchlistWithItems()
        return when (sortOrder) {
            BrowserComponent.SortOrder.ALPHABETICAL -> watchlist.sortedBy { item -> item.movie.title }
            BrowserComponent.SortOrder.RELEASE_DATE -> watchlist.sortedByDescending { item -> item.movie.releaseDate ?: "" }
            BrowserComponent.SortOrder.MATCH_ORDER -> watchlist.sortedByDescending { item -> item.addedAt }
            BrowserComponent.SortOrder.AFFINITY -> {
                val tags = repository.getAllTags()
                val engine = TagBasedRecommendationEngine(tags)
                val scoredMovies: List<Pair<Movie, Double>> = engine.scoreMovies(watchlist.map { item -> item.movie })
                val scoreMap: Map<Int, Double> = scoredMovies.associate { pair -> pair.first.id to pair.second }
                watchlist.sortedByDescending { item -> scoreMap[item.movie.id] ?: 0.0 }
            }
        }
    }
}