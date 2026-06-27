package org.ahugenb.movieswiper.server

import org.ahugenb.movieswiper.core.models.MovieInteraction
import org.ahugenb.movieswiper.core.models.InteractionType
import org.ahugenb.movieswiper.core.models.TasteProfile
import java.util.concurrent.ConcurrentHashMap

class TasteService {
    // In-memory storage for simplicity in this local testing phase
    private val profiles = ConcurrentHashMap<String, TasteProfile>()

    fun getProfile(userId: String): TasteProfile {
        return profiles.getOrPut(userId) {
            TasteProfile(userId, emptyMap(), System.currentTimeMillis())
        }
    }

    fun syncInteractions(userId: String, interactions: List<MovieInteraction>): TasteProfile {
        val currentProfile = getProfile(userId)
        val newWeights = currentProfile.genreWeights.toMutableMap()

        interactions.forEach { interaction ->
            val impact = when (interaction.type) {
                InteractionType.LIKE -> 1.0
                InteractionType.DISLIKE -> -1.0
                InteractionType.WATCHED -> 0.5
            }

            interaction.genreIds.forEach { genreId ->
                val currentWeight = newWeights[genreId] ?: 0.0
                newWeights[genreId] = currentWeight + impact
            }
        }

        val updatedProfile = TasteProfile(userId, newWeights, System.currentTimeMillis())
        profiles[userId] = updatedProfile
        return updatedProfile
    }
}