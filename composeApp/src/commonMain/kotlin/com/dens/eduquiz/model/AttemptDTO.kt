import kotlinx.serialization.Serializable

@Serializable
data class ActivityQuestion(
    val activityId: Long,
    val questionId: Long,
    val createdAt: String? = null
)