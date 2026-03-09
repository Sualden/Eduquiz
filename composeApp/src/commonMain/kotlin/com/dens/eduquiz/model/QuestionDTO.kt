import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Long? = null,
    val text: String,
    val a: String,
    val b: String,
    val c: String,
    val d: String,
    val correct: String,
    val activityId: Long,
    val timer: Int = 0, // Added to match UI
    val status: String = "pending",
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    // Helper property to let the UI treat columns as a list
    val options: List<String> get() = listOf(a, b, c, d)
}

@Serializable
data class CreateQuestionRequest(
    val text: String,
    val a: String,
    val b: String,
    val c: String,
    val d: String,
    val correct: String,
    val activityId: Long,
    val timer: Int, // Added
    val status: String = "pending"
)

@Serializable
data class UpdateQuestionRequest(
    val text: String,
    val a: String,
    val b: String,
    val c: String,
    val d: String,
    val correct: String,
    val activityId: Long,
    val timer: Int, // Added
    val status: String
)