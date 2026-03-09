package com.dens.eduquiz.repository

import Activity
import CreateActivityRequest
import UpdateActivityRequest
import com.dens.eduquiz.database.ActivityQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import toDTO

class ActivityRepository(private val queries: ActivityQueries) {

    // Insert new activity
    suspend fun insertActivity(request: CreateActivityRequest): Long = withContext(Dispatchers.IO) {
        val statusValue = request.status ?: "pending"
        queries.insertActivity(
            request.title,
            request.description,
            request.deadline,
            statusValue
        )
        queries.lastInsertActivityId().executeAsOne()
    }

    // Update existing activity
    suspend fun updateActivity(id: Long, request: UpdateActivityRequest) = withContext(Dispatchers.IO) {
        queries.updateActivity(
            request.title,
            request.description,
            request.deadline,
            request.status,
            id
        )
    }

    // Delete activity
    suspend fun deleteActivity(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteActivity(id)
    }

    // Select all activities (as DTO)
    suspend fun getAllActivities(): List<Activity> = withContext(Dispatchers.IO) {
        queries.selectAllActivities().executeAsList().map { it.toDTO() }
    }

    // Select activity by ID (as DTO)
    suspend fun getActivityById(id: Long): Activity? = withContext(Dispatchers.IO) {
        queries.selectActivityById(id).executeAsOneOrNull()?.toDTO()
    }

    // Select activities by status (as DTO)
    suspend fun getActivitiesByStatus(status: String): List<Activity> = withContext(Dispatchers.IO) {
        queries.selectActivitiesByStatus(status).executeAsList().map { it.toDTO() }
    }

    // Generate a QR token and set expiration
    suspend fun generateQrForActivity(id: Long, token: String) = withContext(Dispatchers.IO) {
        queries.updateQrForActivity(token, id)
    }

    // Get activity by ID and QR token (only if active and not expired)
    suspend fun getActivityByQr(id: Long, token: String): Activity? = withContext(Dispatchers.IO) {
        queries.selectActivityByQr(id, token).executeAsOneOrNull()?.toDTO()
    }

    // Clear QR token after use or expiration
    suspend fun clearQrForActivity(id: Long) = withContext(Dispatchers.IO) {
        queries.clearQrForActivity(id)
    }
}
