package org.ahugenb.movieswiper.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    @SerialName("release_date")
    val releaseDate: String?,
    @SerialName("vote_average")
    val voteAverage: Double,
    @SerialName("genre_ids")
    val genreIds: List<Int>,
    val popularity: Double
) {
    val fullPosterUrl: String? get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val fullBackdropUrl: String? get() = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" }
}

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class WatchlistItem(
    val movie: Movie,
    val addedAt: Long,
    val isWatched: Boolean = false
)