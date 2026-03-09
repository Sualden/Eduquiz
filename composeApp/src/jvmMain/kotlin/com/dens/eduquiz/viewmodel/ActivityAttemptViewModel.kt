package com.dens.eduquiz.viewmodel

import androidx.compose.runtime.*
import com.dens.eduquiz.model.ActivityAttempt
import com.dens.eduquiz.model.CreateActivityAttemptRequest
import com.dens.eduquiz.model.UpdateActivityAttemptRequest
import com.dens.eduquiz.repository.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityAttemptViewModel(private val repository: ActivityAttemptRepository) {

    var attempts by mutableStateOf<List<ActivityAttempt>>(emptyList())
        private set

    var selectedAttempt by mutableStateOf<ActivityAttempt?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    // Load all attempts for a user
    fun loadAttemptsByUser(userId: Long) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                attempts = repository.getAttemptsByUser(userId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Load all attempts for an activity
    fun loadAttemptsByActivity(activityId: Long) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                attempts = repository.getAttemptsByActivity(activityId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Load a single attempt by ID
    fun loadAttemptById(id: Long) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                selectedAttempt = repository.getActivityAttemptById(id)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Create a new activity attempt
    fun createAttempt(request: CreateActivityAttemptRequest, onSuccess: (Long) -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                val id = repository.insertActivityAttempt(request)
                onSuccess(id)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Update an attempt
    fun updateAttempt(id: Long, request: UpdateActivityAttemptRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                repository.updateActivityAttempt(id, request)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // Delete an attempt
    fun deleteAttempt(id: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                repository.deleteActivityAttempt(id)
                attempts = attempts.filterNot { it.id == id }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
}
