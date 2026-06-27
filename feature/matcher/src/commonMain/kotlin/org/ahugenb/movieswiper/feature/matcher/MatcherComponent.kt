package org.ahugenb.movieswiper.feature.matcher

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.*
import org.ahugenb.movieswiper.core.models.*
import org.ahugenb.movieswiper.data.localdb.MovieRepository

interface MatcherComponent {
    val state: Value<State>
    
    fun onSwipeLeft()
    fun onSwipeRight()

    data class State(
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val swipedCount: Int = 0
    )
}

class DefaultMatcherComponent(
    componentContext: ComponentContext,
    private val repository: MovieRepository
) : MatcherComponent, ComponentContext by componentContext {

    private val _state = MutableValue(MatcherComponent.State(isLoading = true))
    override val state: Value<MatcherComponent.State> = _state

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var currentPage = 1
    private val userId = "user_test_1"

    init {
        fetchMovies()
    }

    private fun fetchMovies() {
        scope.launch {
            try {
                // Now fetching from backend (via repository) which does the re-ranking
                val fetchedMovies = repository.discoverMovies(userId, currentPage)
                
                val watchlistItems = repository.getWatchlistWithItems()
                val watchlistIds = watchlistItems.map { it.movie.id }.toSet()
                val filteredMovies = fetchedMovies.filter { it.id !in watchlistIds }

                val currentList = _state.value.movies
                _state.value = _state.value.copy(
                    movies = currentList + filteredMovies,
                    isLoading = false
                )
                
                if (_state.value.movies.size < 5 && currentPage < 10) {
                    currentPage++
                    fetchMovies()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    override fun onSwipeLeft() {
        val movie = _state.value.movies.firstOrNull() ?: return
        popMovie()
        scope.launch {
            movie.genreIds.forEach { repository.updateTagScore(it.toString(), -1) }
            repository.syncInteraction(userId, movie, InteractionType.DISLIKE)
        }
    }

    override fun onSwipeRight() {
        val movie = _state.value.movies.firstOrNull() ?: return
        popMovie()
        scope.launch {
            repository.addToWatchlist(movie)
            movie.genreIds.forEach { repository.updateTagScore(it.toString(), 2) }
            repository.syncInteraction(userId, movie, InteractionType.LIKE)
        }
    }

    private fun popMovie() {
        val currentMovies = _state.value.movies
        if (currentMovies.isNotEmpty()) {
            _state.value = _state.value.copy(
                movies = currentMovies.drop(1),
                swipedCount = _state.value.swipedCount + 1
            )
        }
        
        if (_state.value.movies.size < 5) {
            currentPage++
            fetchMovies()
        }
    }
}