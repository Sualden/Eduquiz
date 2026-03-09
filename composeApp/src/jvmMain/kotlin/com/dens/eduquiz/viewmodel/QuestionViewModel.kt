package com.dens.eduquiz.viewmodel

import CreateQuestionRequest
import Question
import UpdateQuestionRequest
import androidx.compose.runtime.*
import com.dens.eduquiz.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuestionViewModel(private val questionRepo: QuestionRepository) {

    var questions by mutableStateOf<List<Question>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // NOTE: In Android, it is recommended to use 'viewModelScope' instead of a manual scope
    // to ensure coroutines are cancelled when the ViewModel is cleared.
    // If this is KMP (Multiplatform), this approach is fine, but ensure you cancel it if needed.
    private val scope = CoroutineScope(Dispatchers.IO)

    // Load all questions
    fun loadQuestions() {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                // The updated 'Question' data class (with timer) is handled automatically here
                questions = questionRepo.getAllQuestions()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Add question
    fun addQuestion(request: CreateQuestionRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                // The updated 'request' (with timer) is passed directly to the repo
                questionRepo.insertQuestion(request)
                loadQuestions() // refresh list to show new data
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Update question
    fun updateQuestion(id: Long, request: UpdateQuestionRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                // The updated 'request' (with timer) is passed directly to the repo
                questionRepo.updateQuestion(id, request)
                loadQuestions() // refresh list
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Delete question
    fun deleteQuestion(id: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                questionRepo.deleteQuestion(id)
                loadQuestions() // refresh list
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Delete all questions
    fun deleteAllQuestions(onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                questionRepo.deleteAllQuestions()
                questions = emptyList() // clear local state immediately
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}