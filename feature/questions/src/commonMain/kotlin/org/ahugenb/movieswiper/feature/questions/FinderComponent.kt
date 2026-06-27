package org.ahugenb.movieswiper.feature.questions

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.*
import org.ahugenb.movieswiper.core.models.*
import org.ahugenb.movieswiper.data.api.MovieBackendApi

interface FinderComponent {
    val state: Value<State>
    
    fun onAnswerYes()
    fun onAnswerNo()

    data class State(
        val currentQuestion: FinderQuestion? = null,
        val isFinished: Boolean = false,
        val recommendations: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val questionNumber: Int = 0,
        val remainingPoolSize: Int = 0,
    )
}

class DefaultFinderComponent(
    componentContext: ComponentContext,
    private val api: MovieBackendApi
) : FinderComponent, ComponentContext by componentContext {

    private val _state = MutableValue(FinderComponent.State(isLoading = true))
    override val state: Value<FinderComponent.State> = _state

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val answers = mutableListOf<FinderAnswer>()
    private val userId = "user_test_1"

    init {
        fetchNextQuestion()
    }

    private fun fetchNextQuestion() {
        _state.value = _state.value.copy(isLoading = true)
        scope.launch {
            try {
                val response = api.getNextQuestion(FinderQuestionRequest(userId, answers))
                _state.value = _state.value.copy(
                    currentQuestion = response.nextQuestion,
                    isFinished = response.status == FinderStatus.COMPLETE,
                    recommendations = response.recommendations,
                    isLoading = false,
                    questionNumber = answers.size + if (response.status == FinderStatus.CONTINUE) 1 else 0,
                    remainingPoolSize = response.remainingPoolSize,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    override fun onAnswerYes() {
        val question = _state.value.currentQuestion ?: return
        answers.add(FinderAnswer(question.questionId, question.targetType, question.targetId, AnswerType.YES))
        fetchNextQuestion()
    }

    override fun onAnswerNo() {
        val question = _state.value.currentQuestion ?: return
        answers.add(FinderAnswer(question.questionId, question.targetType, question.targetId, AnswerType.NO))
        fetchNextQuestion()
    }
}