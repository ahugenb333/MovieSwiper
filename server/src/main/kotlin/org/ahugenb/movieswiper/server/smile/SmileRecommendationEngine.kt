package org.ahugenb.movieswiper.server.smile

import io.github.aakira.napier.Napier
import org.ahugenb.movieswiper.core.logic.TagBasedRecommendationEngine
import org.ahugenb.movieswiper.core.logic.TastePreferences
import org.ahugenb.movieswiper.core.models.Movie
import org.ahugenb.movieswiper.core.models.MovieInteraction
import org.ahugenb.movieswiper.core.models.TasteProfile
import smile.data.formula.Formula
import smile.regression.LinearModel
import smile.regression.RidgeRegression
import java.util.concurrent.ConcurrentHashMap

/**
 * Learns a per-user ridge regression model from swipe history with Smile,
 * then predicts affinity scores for candidate movies.
 *
 * Falls back to [TagBasedRecommendationEngine] until enough interactions exist.
 */
class SmileRecommendationEngine {
    private val models = ConcurrentHashMap<String, CachedModel>()

    private data class CachedModel(
        val interactionCount: Int,
        val model: LinearModel,
    )

    fun rankMovies(
        userId: String,
        movies: List<Movie>,
        interactions: List<MovieInteraction>,
        preferences: TastePreferences,
        profile: TasteProfile,
        limit: Int = 20,
    ): List<Movie> {
        val distinctMovies = movies.distinctBy { it.id }
        if (distinctMovies.isEmpty()) return emptyList()

        val model = trainModel(userId, interactions)
        if (model != null) {
            try {
                val frame = MovieFeatureEncoder.predictionFrame(distinctMovies)
                val scores = model.predict(frame)
                return distinctMovies
                    .mapIndexed { index, movie -> movie to scores[index] }
                    .sortedByDescending { it.second }
                    .take(limit)
                    .map { it.first }
            } catch (e: Exception) {
                Napier.w(tag = "Smile", message = "Prediction failed for $userId: ${e.message}")
            }
        }

        return fallbackRank(distinctMovies, preferences, profile, limit)
    }

    private fun trainModel(userId: String, interactions: List<MovieInteraction>): LinearModel? {
        if (interactions.size < MIN_TRAINING_SAMPLES) {
            models.remove(userId)
            return null
        }

        val cached = models[userId]
        if (cached != null && cached.interactionCount == interactions.size) {
            return cached.model
        }

        return try {
            val frame = MovieFeatureEncoder.trainingFrame(interactions)
            val model = RidgeRegression.fit(
                Formula.lhs(MovieFeatureEncoder.RATING_COLUMN),
                frame,
                RIDGE_LAMBDA,
            )
            models[userId] = CachedModel(interactions.size, model)
            model
        } catch (e: Exception) {
            Napier.w(tag = "Smile", message = "Training failed for $userId: ${e.message}")
            models.remove(userId)
            null
        }
    }

    private fun fallbackRank(
        movies: List<Movie>,
        preferences: TastePreferences,
        profile: TasteProfile,
        limit: Int,
    ): List<Movie> {
        val tagWeights = profile.genreWeights.mapKeys { it.key.toString() }.mapValues { it.value.toInt() }
        return TagBasedRecommendationEngine(tagWeights)
            .scoreMovies(movies, preferences, profile)
            .take(limit)
            .map { it.first }
    }

    companion object {
        private const val MIN_TRAINING_SAMPLES = 4
        private const val RIDGE_LAMBDA = 0.5
    }
}
