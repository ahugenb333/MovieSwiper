package org.ahugenb.movieswiper.core.logic

import org.ahugenb.movieswiper.core.models.Movie
import org.ahugenb.movieswiper.core.models.TasteProfile

class TagBasedRecommendationEngine(
    private val userTags: Map<String, Int>,
) {
    fun scoreMovies(movies: List<Movie>): List<Pair<Movie, Double>> {
        return scoreMovies(movies, TastePreferences(), TasteProfile("", emptyMap(), 0))
    }

    fun scoreMovies(
        movies: List<Movie>,
        preferences: TastePreferences,
        profile: TasteProfile,
    ): List<Pair<Movie, Double>> {
        val mergedPrefs = preferences.merge(profile)
        val tagWeights = mergedPrefs.genreWeights.mapKeys { it.key.toString() }
            .mapValues { it.value.toInt() } + userTags

        return movies.map { movie ->
            movie to calculateScore(movie, tagWeights, mergedPrefs)
        }.sortedByDescending { it.second }
    }

    private fun calculateScore(
        movie: Movie,
        tags: Map<String, Int>,
        preferences: TastePreferences,
    ): Double {
        var score = movie.voteAverage * 0.4
        score += (movie.popularity / 150.0).coerceAtMost(2.0)

        movie.genreIds.forEach { genreId ->
            score += (tags[genreId.toString()] ?: 0) * 2.5
            if (genreId in preferences.requiredGenres) score += 4.0
            if (genreId in preferences.excludedGenres) score -= 8.0
        }

        val decade = MoviePool.movieDecade(movie)
        if (decade != null) {
            if (decade in preferences.excludedDecades) score -= 6.0
            preferences.eraStart?.let { start ->
                preferences.eraEnd?.let { end ->
                    val year = movie.releaseDate?.take(4)?.toIntOrNull()
                    if (year != null && year in start..end) score += 3.0
                }
            }
        }

        return score
    }
}
