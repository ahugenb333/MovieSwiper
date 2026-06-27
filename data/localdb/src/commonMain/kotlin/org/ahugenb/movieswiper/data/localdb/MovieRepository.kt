package org.ahugenb.movieswiper.data.localdb

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.ahugenb.movieswiper.core.models.*
import org.ahugenb.movieswiper.data.api.MovieBackendApi

class MovieRepository(
    private val api: MovieBackendApi,
    private val database: MovieDatabase
) {
    private val queries = database.movieDatabaseQueries

    suspend fun syncInteraction(userId: String, movie: Movie, type: InteractionType) = withContext(Dispatchers.IO) {
        api.syncProfile(
            InteractionRequest(
                userId = userId,
                interactions = listOf(
                    MovieInteraction(
                        movieId = movie.id,
                        genreIds = movie.genreIds,
                        type = type,
                        timestamp = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds,
                    )
                )
            )
        )
    }

    suspend fun discoverMovies(userId: String, page: Int = 1): List<Movie> = withContext(Dispatchers.IO) {
        val movies = api.discoverMovies(userId, page)
        movies.forEach { movie ->
            queries.insertMovie(
                id = movie.id.toLong(),
                title = movie.title,
                overview = movie.overview,
                posterPath = movie.posterPath,
                backdropPath = movie.backdropPath,
                releaseDate = movie.releaseDate,
                voteAverage = movie.voteAverage,
                genreIds = movie.genreIds.joinToString(","),
                popularity = movie.popularity
            )
        }
        movies
    }

    suspend fun addToWatchlist(movie: Movie) = withContext(Dispatchers.IO) {
        queries.addToWatchlist(
            movieId = movie.id.toLong(),
            addedAt = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds,
            isWatched = false
        )
    }

    suspend fun removeFromWatchlist(movieId: Int) = withContext(Dispatchers.IO) {
        queries.removeFromWatchlist(movieId.toLong())
    }

    suspend fun toggleWatchedStatus(movieId: Int, isWatched: Boolean) = withContext(Dispatchers.IO) {
        queries.updateWatchedStatus(isWatched, movieId.toLong())
    }

    suspend fun getWatchlistWithItems(): List<WatchlistItem> = withContext(Dispatchers.IO) {
        queries.getWatchlist().executeAsList().map { entity ->
            val movie = Movie(
                id = entity.id.toInt(),
                title = entity.title,
                overview = entity.overview,
                posterPath = entity.posterPath,
                backdropPath = entity.backdropPath,
                releaseDate = entity.releaseDate,
                voteAverage = entity.voteAverage,
                genreIds = entity.genreIds.split(",").filter { it.isNotEmpty() }.map { it.toInt() },
                popularity = entity.popularity
            )
            val watchlistMeta = queries.getWatchlistMetadata(entity.id).executeAsOne()
            
            WatchlistItem(
                movie = movie,
                addedAt = watchlistMeta.addedAt,
                isWatched = watchlistMeta.isWatched
            )
        }
    }
    
    suspend fun updateTagScore(tag: String, score: Int) = withContext(Dispatchers.IO) {
        queries.updateTagScore(tag, score.toLong())
    }

    suspend fun getAllTags(): Map<String, Int> = withContext(Dispatchers.IO) {
        queries.getAllTags().executeAsList().associate { it.tag to it.score.toInt() }
    }
}