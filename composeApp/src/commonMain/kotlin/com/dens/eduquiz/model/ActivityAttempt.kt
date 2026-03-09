package com.dens.eduquiz.model

import com.dens.eduquiz.database.StudentActivityAttempt
import kotlinx.serialization.Serializable

@Serializable
data class ActivityAttempt(
    val id: Long? = null,
    val userId: Long,
    val activityId: Long,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val score: Int? = null,
    val status: String = "pending",
    val updatedAt: String,
    val createdAt: String
)

@Serializable
data class CreateActivityAttemptRequest(
    val userId: Long,
    val activityId: Long
)

@Serializable
data class UpdateActivityAttemptRequest(
    val score: Int
)

// Convert SQLDelight entity → Kotlin DTO
fun StudentActivityAttempt.toDTO() = ActivityAttempt(
    id = id,
    userId = userId,
    activityId = activityId,
    score = score?.toInt(),      // SQLDelight gives Long
    createdAt = createdAt,
    updatedAt = updatedAt
)
