package org.ahugenb.movieswiper.core.models

import kotlinx.serialization.Serializable

@Serializable
data class FinderQuestionRequest(
    val userId: String,
    val sessionAnswers: List<FinderAnswer>
)

@Serializable
data class FinderAnswer(
    val questionId: String,
    val targetType: FinderTargetType,
    val targetId: Int,
    val answer: AnswerType
)

@Serializable
enum class FinderTargetType {
    GENRE, KEYWORD, ERA
}

@Serializable
enum class AnswerType {
    YES, NO
}

@Serializable
data class FinderResponse(
    val status: FinderStatus,
    val remainingPoolSize: Int,
    val nextQuestion: FinderQuestion?,
    val recommendations: List<Movie>
)

@Serializable
enum class FinderStatus {
    CONTINUE, COMPLETE
}

@Serializable
data class FinderQuestion(
    val questionId: String,
    val displayText: String,
    val targetType: FinderTargetType,
    val targetId: Int
)