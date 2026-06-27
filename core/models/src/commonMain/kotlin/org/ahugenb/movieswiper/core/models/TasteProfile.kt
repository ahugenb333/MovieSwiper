package org.ahugenb.movieswiper.core.models

import kotlinx.serialization.Serializable

@Serializable
data class TasteProfile(
    val userId: String,
    val genreWeights: Map<Int, Double>,
    val updatedAt: Long
)

@Serializable
data class InteractionRequest(
    val userId: String,
    val interactions: List<MovieInteraction>
)

@Serializable
data class MovieInteraction(
    val movieId: Int,
    val genreIds: List<Int>,
    val type: InteractionType,
    val timestamp: Long
)

@Serializable
enum class InteractionType {
    LIKE, DISLIKE, WATCHED
}