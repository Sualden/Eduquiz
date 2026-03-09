package com.dens.eduquiz.viewmodel

import Question
import androidx.compose.runtime.*
import com.dens.eduquiz.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityQuestionViewModel(
    private val activityQuestionRepo: ActivityQuestionRepository,
    private val questionRepo: QuestionRepository
) {

    var questionsForActivity by mutableStateOf<List<Question>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    // Load all questions for a specific activity
    fun loadQuestions(activityId: Long) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                // activityQuestionRepo returns List<Questions>, map to List<Question>
                val questionsList = activityQuestionRepo.getQuestionsForActivity(activityId)
                questionsForActivity = questionsList.map { it.toDTO() }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Add a question to an activity
    fun addQuestionToActivity(activityId: Long, questionId: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                // questionRepo.getQuestionById already returns Question, no .toDTO() needed
                val question = questionRepo.getQuestionById(questionId)
                if (question == null) {
                    errorMessage = "Question not found"
                } else {
                    activityQuestionRepo.insertActivityQuestion(activityId, questionId)
                    loadQuestions(activityId) // Refresh list
                    onSuccess()
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Remove a single question from an activity
    fun removeQuestionFromActivity(activityId: Long, questionId: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                activityQuestionRepo.deleteSingleQuestion(activityId, questionId)
                loadQuestions(activityId) // Refresh list
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Remove all questions from an activity
    fun removeAllQuestionsFromActivity(activityId: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                activityQuestionRepo.deleteQuestionsFromActivity(activityId)
                questionsForActivity = emptyList()
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
