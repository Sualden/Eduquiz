import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val deadline: String? = null,
    val status: String = "pending",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val qrToken: String? = null,
    val qrExpiresAt: String? = null
)

@Serializable
data class ActivityItem(
    val id: Long,
    val title: String,
    val description: String,
    val status: String,
    val deadline: String,
    val qrToken: String? = null,
    val qrExpiresAt: String? = null
)

data class ActivityMember(val activityId: Long, val studentId: Long)

@Serializable
data class CreateActivityRequest(
    val title: String,
    val description: String? = null,
    val deadline: String? = null,
    val status: String? = null
)

@Serializable
data class UpdateActivityRequest(
    val title: String,
    val description: String? = null,
    val deadline: String? = null,
    val status: String
)

// Extension function to convert SQLDelight entity to DTO
fun com.dens.eduquiz.database.Activity.toDTO() = Activity(
    id = id,
    title = title,
    description = description,
    deadline = deadline,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    qrToken = qr_token,
    qrExpiresAt = qr_expires_at
)
