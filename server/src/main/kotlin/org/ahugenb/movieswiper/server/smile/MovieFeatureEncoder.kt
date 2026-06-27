package org.ahugenb.movieswiper.server.smile

import org.ahugenb.movieswiper.core.models.GenreConstants
import org.ahugenb.movieswiper.core.models.InteractionType
import org.ahugenb.movieswiper.core.models.Movie
import org.ahugenb.movieswiper.core.models.MovieInteraction
import smile.data.DataFrame

/**
 * Turns movies and swipe history into Smile [DataFrame] columns.
 *
 * Training rows include a [RATING_COLUMN] response. Prediction rows use the
 * same feature columns without the response.
 */
object MovieFeatureEncoder {
    const val RATING_COLUMN = "rating"
    private const val VOTE_AVERAGE_COLUMN = "voteAverage"
    private const val POPULARITY_COLUMN = "popularity"
    private const val RELEASE_YEAR_COLUMN = "releaseYear"

    private val featureColumns = listOf(
        VOTE_AVERAGE_COLUMN,
        POPULARITY_COLUMN,
        RELEASE_YEAR_COLUMN,
    ) + GenreConstants.allGenreIds.map { genreColumnName(it) }

    val trainingColumns = listOf(RATING_COLUMN) + featureColumns

    fun genreColumnName(genreId: Int): String = "genre_$genreId"

    fun trainingFrame(interactions: List<MovieInteraction>): DataFrame {
        require(interactions.isNotEmpty())
        val rows = interactions.map { interaction ->
            featureRow(
                genreIds = interaction.genreIds,
                voteAverage = DEFAULT_VOTE_AVERAGE,
                popularity = DEFAULT_POPULARITY,
                releaseYear = DEFAULT_RELEASE_YEAR,
            ).let { features ->
                doubleArrayOf(ratingLabel(interaction.type), *features)
            }
        }
        return DataFrame.of(rows.toTypedArray(), *trainingColumns.toTypedArray())
    }

    fun predictionFrame(movies: List<Movie>): DataFrame {
        require(movies.isNotEmpty())
        val rows = movies.map { movie ->
            featureRow(
                genreIds = movie.genreIds,
                voteAverage = movie.voteAverage,
                popularity = normalizedPopularity(movie.popularity),
                releaseYear = releaseYear(movie) ?: DEFAULT_RELEASE_YEAR,
            )
        }
        return DataFrame.of(rows.toTypedArray(), *featureColumns.toTypedArray())
    }

    private fun featureRow(
        genreIds: List<Int>,
        voteAverage: Double,
        popularity: Double,
        releaseYear: Double,
    ): DoubleArray {
        val genreFlags = GenreConstants.allGenreIds.map { genreId ->
            if (genreId in genreIds) 1.0 else 0.0
        }
        return doubleArrayOf(voteAverage, popularity, releaseYear, *genreFlags.toDoubleArray())
    }

    private fun ratingLabel(type: InteractionType): Double = when (type) {
        InteractionType.LIKE -> 1.0
        InteractionType.DISLIKE -> -1.0
        InteractionType.WATCHED -> 0.25
    }

    private fun normalizedPopularity(popularity: Double): Double {
        return kotlin.math.ln1p(popularity.coerceAtLeast(0.0))
    }

    private fun releaseYear(movie: Movie): Double? {
        return movie.releaseDate?.take(4)?.toDoubleOrNull()
    }

    private const val DEFAULT_VOTE_AVERAGE = 6.5
    private const val DEFAULT_POPULARITY = 4.0
    private const val DEFAULT_RELEASE_YEAR = 2005.0
}
