package org.ahugenb.movieswiper.core.logic

import org.ahugenb.movieswiper.core.models.AnswerType
import org.ahugenb.movieswiper.core.models.FinderAnswer
import org.ahugenb.movieswiper.core.models.FinderQuestion
import org.ahugenb.movieswiper.core.models.FinderTargetType
import org.ahugenb.movieswiper.core.models.GenreConstants
import org.ahugenb.movieswiper.core.models.Movie
import kotlin.math.ln

object MoviePool {
    fun filterByAnswers(movies: List<Movie>, answers: List<FinderAnswer>): List<Movie> {
        if (answers.isEmpty()) return movies.distinctBy { it.id }

        return movies
            .distinctBy { it.id }
            .filter { movie -> answers.all { answer -> matchesAnswer(movie, answer) } }
    }

    private fun matchesAnswer(movie: Movie, answer: FinderAnswer): Boolean {
        val matchesTarget = when (answer.targetType) {
            FinderTargetType.GENRE -> answer.targetId in movie.genreIds
            FinderTargetType.ERA -> movieDecade(movie) == answer.targetId
            FinderTargetType.KEYWORD -> false
        }
        return when (answer.answer) {
            AnswerType.YES -> matchesTarget
            AnswerType.NO -> !matchesTarget
        }
    }

    fun movieDecade(movie: Movie): Int? {
        val year = movie.releaseDate?.take(4)?.toIntOrNull() ?: return null
        return (year / 10) * 10
    }
}

object FinderQuestionEngine {
    const val MAX_QUESTIONS = 20
    private const val MIN_POOL_TO_FINISH = 5

    private val QUESTIONABLE_GENRES = listOf(
        28, 12, 16, 35, 80, 18, 10751, 14, 27, 9648, 10749, 878, 53, 99, 36, 37,
    )
    private val QUESTIONABLE_DECADES = listOf(1970, 1980, 1990, 2000, 2010, 2020)

    data class Selection(
        val question: FinderQuestion?,
        val remainingPoolSize: Int,
        val isComplete: Boolean,
    )

    fun selectNext(pool: List<Movie>, answers: List<FinderAnswer>): Selection {
        val remaining = pool.size
        if (remaining <= MIN_POOL_TO_FINISH || answers.size >= MAX_QUESTIONS) {
            return Selection(question = null, remainingPoolSize = remaining, isComplete = true)
        }

        val askedGenreIds = answers
            .filter { it.targetType == FinderTargetType.GENRE }
            .map { it.targetId }
            .toSet()
        val askedDecades = answers
            .filter { it.targetType == FinderTargetType.ERA }
            .map { it.targetId }
            .toSet()

        val genreCandidates = QUESTIONABLE_GENRES
            .filter { it !in askedGenreIds }
            .mapNotNull { genreId ->
                val yesCount = pool.count { genreId in it.genreIds }
                val noCount = pool.size - yesCount
                if (yesCount == 0 || noCount == 0) null
                else QuestionCandidate(
                    targetType = FinderTargetType.GENRE,
                    targetId = genreId,
                    yesCount = yesCount,
                    noCount = noCount,
                )
            }

        val decadeCandidates = QUESTIONABLE_DECADES
            .filter { it !in askedDecades }
            .mapNotNull { decade ->
                val yesCount = pool.count { MoviePool.movieDecade(it) == decade }
                val noCount = pool.size - yesCount
                if (yesCount == 0 || noCount == 0) null
                else QuestionCandidate(
                    targetType = FinderTargetType.ERA,
                    targetId = decade,
                    yesCount = yesCount,
                    noCount = noCount,
                )
            }

        val best = (genreCandidates + decadeCandidates)
            .maxByOrNull { it.informationGain() }

        if (best == null) {
            return Selection(question = null, remainingPoolSize = remaining, isComplete = true)
        }

        return Selection(
            question = FinderQuestion(
                questionId = "q_${best.targetType.name.lowercase()}_${best.targetId}",
                displayText = best.displayText(),
                targetType = best.targetType,
                targetId = best.targetId,
            ),
            remainingPoolSize = remaining,
            isComplete = false,
        )
    }

    private data class QuestionCandidate(
        val targetType: FinderTargetType,
        val targetId: Int,
        val yesCount: Int,
        val noCount: Int,
    ) {
        fun informationGain(): Double {
            val total = yesCount + noCount
            val before = entropy(total)
            val after = (yesCount.toDouble() / total) * entropy(yesCount) +
                (noCount.toDouble() / total) * entropy(noCount)
            return before - after
        }

        private fun entropy(count: Int): Double {
            if (count <= 0) return 0.0
            val total = (yesCount + noCount).toDouble()
            val p = count / total
            return -p * ln(p) / ln(2.0)
        }

        fun displayText(): String = when (targetType) {
            FinderTargetType.GENRE -> {
                val genre = GenreConstants.getGenreName(targetId).lowercase()
                "Are you in the mood for a $genre film?"
            }
            FinderTargetType.ERA -> "Do you enjoy movies from the ${targetId}s?"
            FinderTargetType.KEYWORD -> "Do you want more movies like this?"
        }
    }
}
