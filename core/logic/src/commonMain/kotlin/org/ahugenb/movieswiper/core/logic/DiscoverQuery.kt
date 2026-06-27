package org.ahugenb.movieswiper.core.logic

import org.ahugenb.movieswiper.core.models.FinderAnswer
import org.ahugenb.movieswiper.core.models.FinderTargetType
import org.ahugenb.movieswiper.core.models.AnswerType
import org.ahugenb.movieswiper.core.models.TasteProfile

data class DiscoverQuery(
    val page: Int = 1,
    val withGenres: List<Int> = emptyList(),
    val withoutGenres: List<Int> = emptyList(),
    val primaryReleaseDateGte: String? = null,
    val primaryReleaseDateLte: String? = null,
    val sortBy: String = "vote_average.desc",
    val voteAverageGte: Double? = 6.5,
    val voteCountGte: Int? = 150,
)

object DiscoverQueryBuilder {
    private val sortRotation = listOf(
        "vote_average.desc",
        "popularity.desc",
        "release_date.desc",
        "revenue.desc",
    )

    fun forMatcher(profile: TasteProfile, page: Int): DiscoverQuery {
        val positiveGenres = profile.genreWeights
            .filter { it.value > 0 }
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val negativeGenres = profile.genreWeights
            .filter { it.value < -1 }
            .keys
            .toList()

        val hasTaste = positiveGenres.isNotEmpty() || negativeGenres.isNotEmpty()

        return DiscoverQuery(
            page = page,
            withGenres = positiveGenres,
            withoutGenres = negativeGenres,
            sortBy = if (hasTaste) "popularity.desc" else sortRotation[(page - 1) % sortRotation.size],
            voteAverageGte = if (hasTaste) 6.0 else 6.5,
            voteCountGte = if (hasTaste) 100 else 200,
        )
    }

    fun forFinder(answers: List<FinderAnswer>, page: Int): DiscoverQuery {
        val prefs = TastePreferences.fromAnswers(answers)

        return DiscoverQuery(
            page = page,
            // Genre preferences are applied locally — TMDB treats with_genres as AND.
            withoutGenres = prefs.excludedGenres.toList(),
            primaryReleaseDateGte = prefs.eraStart?.let { "${it}-01-01" },
            primaryReleaseDateLte = prefs.eraEnd?.let { "${it}-12-31" },
            sortBy = "vote_average.desc",
            voteAverageGte = 6.0,
            voteCountGte = 80,
        )
    }
}

data class TastePreferences(
    val genreWeights: Map<Int, Double> = emptyMap(),
    val requiredGenres: Set<Int> = emptySet(),
    val excludedGenres: Set<Int> = emptySet(),
    val eraStart: Int? = null,
    val eraEnd: Int? = null,
    val excludedDecades: Set<Int> = emptySet(),
) {
    fun merge(profile: TasteProfile): TastePreferences {
        val mergedWeights = profile.genreWeights.toMutableMap()
        genreWeights.forEach { (id, weight) ->
            mergedWeights[id] = (mergedWeights[id] ?: 0.0) + weight
        }
        return copy(genreWeights = mergedWeights)
    }

    companion object {
        fun fromAnswers(answers: List<FinderAnswer>): TastePreferences {
            var requiredGenres = emptySet<Int>()
            var excludedGenres = emptySet<Int>()
            var eraStart: Int? = null
            var eraEnd: Int? = null
            var excludedDecades = emptySet<Int>()
            val weightAdjustments = mutableMapOf<Int, Double>()

            answers.forEach { answer ->
                when (answer.targetType) {
                    FinderTargetType.GENRE -> when (answer.answer) {
                        AnswerType.YES -> {
                            requiredGenres = requiredGenres + answer.targetId
                            weightAdjustments[answer.targetId] =
                                (weightAdjustments[answer.targetId] ?: 0.0) + 3.0
                        }
                        AnswerType.NO -> {
                            excludedGenres = excludedGenres + answer.targetId
                            weightAdjustments[answer.targetId] =
                                (weightAdjustments[answer.targetId] ?: 0.0) - 3.0
                        }
                    }
                    FinderTargetType.ERA -> {
                        val decade = answer.targetId
                        when (answer.answer) {
                            AnswerType.YES -> {
                                eraStart = listOfNotNull(eraStart, decade).minOrNull()
                                eraEnd = listOfNotNull(eraEnd, decade + 9).maxOrNull()
                            }
                            AnswerType.NO -> excludedDecades = excludedDecades + decade
                        }
                    }
                    FinderTargetType.KEYWORD -> Unit
                }
            }

            return TastePreferences(
                genreWeights = weightAdjustments,
                requiredGenres = requiredGenres,
                excludedGenres = excludedGenres,
                eraStart = eraStart,
                eraEnd = eraEnd,
                excludedDecades = excludedDecades,
            )
        }
    }
}
