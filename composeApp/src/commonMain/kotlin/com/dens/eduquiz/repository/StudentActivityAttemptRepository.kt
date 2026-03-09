package com.dens.eduquiz.repository

import com.dens.eduquiz.database.StudentActivityAttempt
import com.dens.eduquiz.database.StudentActivityAttemptQueries
import com.dens.eduquiz.model.ActivityAttempt
import com.dens.eduquiz.model.CreateActivityAttemptRequest
import com.dens.eduquiz.model.UpdateActivityAttemptRequest
import com.dens.eduquiz.model.toDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable



class ActivityAttemptRepository(
    private val queries: StudentActivityAttemptQueries
) {

    // Insert new activity attempt
    suspend fun insertActivityAttempt(request: CreateActivityAttemptRequest): Long =
        withContext(Dispatchers.IO) {
            queries.insertStudentActivityAttempt(
                userId = request.userId,
                activityId = request.activityId
            )
            queries.lastInsertStudentActivityAttemptId().executeAsOne()
        }

    // Update activity attempt after completion
    suspend fun updateActivityAttempt(id: Long, request: UpdateActivityAttemptRequest) =
        withContext(Dispatchers.IO) {
            queries.updateActivityAttempt(
                score = request.score.toLong(), // Convert Int -> Long for DB
                id = id
            )
        }

    // Delete activity attempt
    suspend fun deleteActivityAttempt(id: Long) =
        withContext(Dispatchers.IO) {
            queries.deleteActivityAttempt(id)
        }

    // Get all attempts for a user
    suspend fun getAttemptsByUser(userId: Long): List<ActivityAttempt> =
        withContext(Dispatchers.IO) {
            queries.selectAttemptsByUser(userId)
                .executeAsList()
                .map { it.toDTO() }
        }

    // Get all attempts for an activity
    suspend fun getAttemptsByActivity(activityId: Long): List<ActivityAttempt> =
        withContext(Dispatchers.IO) {
            queries.selectAttemptsByActivity(activityId)
                .executeAsList()
                .map { it.toDTO() }
        }

    // Get attempt by ID
    suspend fun getActivityAttemptById(id: Long): ActivityAttempt? =
        withContext(Dispatchers.IO) {
            queries.selectActivityAttemptById(id)
                .executeAsOneOrNull()
                ?.toDTO()
        }
}
