package com.dens.eduquiz.viewmodel

import CreateStudentAnswerRequest
import StudentAnswer
import UpdateStudentAnswerRequest
import androidx.compose.runtime.*
import com.dens.eduquiz.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StudentAnswerViewModel(private val repository: StudentAnswerRepository) {

    var answers by mutableStateOf<List<StudentAnswer>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // --- STATE TRACKING (To know what to refresh) ---
    private var currentAttemptId: Long? = null
    private var currentUserId: Long? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    // Load answers by attempt
    fun loadAnswersByAttempt(attemptId: Long) {
        currentAttemptId = attemptId
        currentUserId = null // Reset the other context

        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                answers = repository.getAnswersByAttempt(attemptId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Load answers by student
    fun loadAnswersByStudent(userId: Long) {
        currentUserId = userId
        currentAttemptId = null // Reset the other context

        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                answers = repository.getAnswersByStudent(userId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Add new answer
    fun addAnswer(request: CreateStudentAnswerRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                repository.insertStudentAnswer(request)
                refreshData() // <--- AUTO REFRESH
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Update existing answer
    fun updateAnswer(id: Long, request: UpdateStudentAnswerRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                repository.updateStudentAnswer(id, request)
                refreshData() // <--- AUTO REFRESH
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Delete answer by ID
    fun deleteAnswer(id: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                repository.deleteStudentAnswer(id)
                refreshData() // <--- AUTO REFRESH
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Delete all answers for a specific attempt
    fun deleteAnswersByAttempt(attemptId: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                repository.deleteAnswersByAttempt(attemptId)
                answers = emptyList() // Clear list immediately
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // --- HELPER TO REFRESH CURRENT VIEW ---
    private suspend fun refreshData() {
        if (currentAttemptId != null) {
            answers = repository.getAnswersByAttempt(currentAttemptId!!)
        } else if (currentUserId != null) {
            answers = repository.getAnswersByStudent(currentUserId!!)
        }
    }
}