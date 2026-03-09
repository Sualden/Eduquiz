import kotlinx.serialization.Serializable

@Serializable
data class StudentAnswer(
    val id: Long? = null,
    val attemptId: Long,
    val questionId: Long,
    val selected: String,
    val isCorrect: Boolean = false,
    val status: String = "pending",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateStudentAnswerRequest(
    val attemptId: Long,
    val questionId: Long,
    val selected: String,
    val isCorrect: Boolean = false
)

@Serializable
data class UpdateStudentAnswerRequest(
    val selected: String,
    val isCorrect: Boolean,
    val status: String
)

// Extension function to convert SQLDelight entity to DTO
fun com.dens.eduquiz.database.StudentAnswer.toDTO() = StudentAnswer(
    id = id,
    attemptId = attemptId,
    questionId = questionId,
    selected = selected,
    isCorrect = isCorrect.toInt() != 0,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)
