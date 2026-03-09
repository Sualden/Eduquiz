package com.dens.eduquiz.viewmodel

import Activity
import ActivityItem
import CreateActivityRequest
import UpdateActivityRequest
import androidx.compose.runtime.*
import com.dens.eduquiz.repository.ActivityRepository
import kotlinx.coroutines.*
class ActivityViewModel(private val activityRepo: ActivityRepository) {

    var activityList by mutableStateOf<List<ActivityItem>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun loadActivities() {
        isLoading = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { activityRepo.getAllActivities() }
                activityList = result.map { it.toActivityItem() }
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    fun loadActivitiesByStatus(status: String) {
        isLoading = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) { activityRepo.getActivitiesByStatus(status) }
                activityList = result.map { it.toActivityItem() }
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    fun addActivity(request: CreateActivityRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) { activityRepo.insertActivity(request) }
                loadActivities()
                onSuccess()
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    fun updateActivity(id: Long, request: UpdateActivityRequest, onSuccess: () -> Unit = {}) {
        isLoading = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) { activityRepo.updateActivity(id, request) }
                loadActivities()
                onSuccess()
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    fun deleteActivity(id: Long, onSuccess: () -> Unit = {}) {
        isLoading = true
        scope.launch {
            try {
                withContext(Dispatchers.IO) { activityRepo.deleteActivity(id) }
                loadActivities()
                onSuccess()
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    // -------------------------------
    // QR FUNCTIONS WITH STUDENT ID
    // -------------------------------
    fun generateQr(id: Long, studentId: Long, onSuccess: (qrData: String) -> Unit = {}) {
        isLoading = true
        scope.launch {
            try {
                val token = java.util.UUID.randomUUID().toString()
                withContext(Dispatchers.IO) { activityRepo.generateQrForActivity(id, token) }
                val qrData = "eduquiz://join?activityId=$id&studentId=$studentId&token=$token"
                onSuccess(qrData)
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    fun getActivityByQr(id: Long, token: String, studentId: Long, onSuccess: (ActivityItem, Long) -> Unit) {
        isLoading = true
        scope.launch {
            try {
                val activity = withContext(Dispatchers.IO) { activityRepo.getActivityByQr(id, token) ?: throw Exception("Activity not found or QR expired") }
                withContext(Dispatchers.IO) { activityRepo.clearQrForActivity(id) }
                onSuccess(activity.toActivityItem(), studentId)
            } catch (e: Exception) { errorMessage = e.message } finally { isLoading = false }
        }
    }

    private fun Activity.toActivityItem() = ActivityItem(
        id = this.id ?: 0L,
        title = this.title ?: "",
        description = this.description ?: "",
        status = this.status ?: "pending",
        deadline = this.deadline ?: ""
    )

}
