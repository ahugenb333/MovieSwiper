package org.ahugenb.movieswiper.server

import org.ahugenb.movieswiper.core.logic.FinderQuestionEngine
import org.ahugenb.movieswiper.core.models.*

class FinderService(
    private val recommendationService: RecommendationService,
    private val tasteService: TasteService,
) {

    suspend fun getNextQuestion(request: FinderQuestionRequest): FinderResponse {
        val answers = request.sessionAnswers
        val pool = recommendationService.buildFinderPool(answers)
        val selection = FinderQuestionEngine.selectNext(pool, answers)

        if (selection.isComplete) {
            val recommendations = recommendationService.recommendFromPool(
                pool = pool,
                userId = request.userId,
                answers = answers,
            )
            syncFinderTaste(request.userId, answers)

            return FinderResponse(
                status = FinderStatus.COMPLETE,
                remainingPoolSize = selection.remainingPoolSize,
                nextQuestion = null,
                recommendations = recommendations,
            )
        }

        return FinderResponse(
            status = FinderStatus.CONTINUE,
            remainingPoolSize = selection.remainingPoolSize,
            nextQuestion = selection.question,
            recommendations = emptyList(),
        )
    }

    private fun syncFinderTaste(userId: String, answers: List<FinderAnswer>) {
        val interactions = answers.mapNotNull { answer ->
            when (answer.targetType) {
                FinderTargetType.GENRE -> MovieInteraction(
                    movieId = 0,
                    genreIds = listOf(answer.targetId),
                    type = when (answer.answer) {
                        AnswerType.YES -> InteractionType.LIKE
                        AnswerType.NO -> InteractionType.DISLIKE
                    },
                    timestamp = System.currentTimeMillis(),
                )
                FinderTargetType.ERA, FinderTargetType.KEYWORD -> null
            }
        }
        if (interactions.isNotEmpty()) {
            tasteService.syncInteractions(userId, interactions)
        }
    }
}
