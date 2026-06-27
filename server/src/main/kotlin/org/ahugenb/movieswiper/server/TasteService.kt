package org.ahugenb.movieswiper.server

import org.ahugenb.movieswiper.core.models.InteractionType
import org.ahugenb.movieswiper.core.models.MovieInteraction
import org.ahugenb.movieswiper.core.models.TasteProfile
import java.util.concurrent.ConcurrentHashMap

class TasteService {
    private val profiles = ConcurrentHashMap<String, TasteProfile>()
    private val interactions = ConcurrentHashMap<String, MutableList<MovieInteraction>>()

    fun getProfile(userId: String): TasteProfile {
        return profiles.getOrPut(userId) {
            TasteProfile(userId, emptyMap(), System.currentTimeMillis())
        }
    }

    fun getInteractions(userId: String): List<MovieInteraction> {
        return interactions[userId]?.toList() ?: emptyList()
    }

    fun syncInteractions(userId: String, newInteractions: List<MovieInteraction>): TasteProfile {
        val history = interactions.getOrPut(userId) { mutableListOf() }
        history.addAll(newInteractions)

        val currentProfile = getProfile(userId)
        val newWeights = currentProfile.genreWeights.toMutableMap()

        newInteractions.forEach { interaction ->
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
