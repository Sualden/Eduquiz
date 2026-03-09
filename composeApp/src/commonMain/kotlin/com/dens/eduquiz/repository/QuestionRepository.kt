package com.dens.eduquiz.repository

import CreateQuestionRequest
import Question
import UpdateQuestionRequest
import com.dens.eduquiz.database.QuestionQueries
import com.dens.eduquiz.database.Questions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuestionRepository(private val queries: QuestionQueries) {

    // Insert new question
    suspend fun insertQuestion(request: CreateQuestionRequest): Long = withContext(Dispatchers.IO) {
        queries.insertQuestion(
            request.text,
            request.a,
            request.b,
            request.c,
            request.d,
            request.correct,
            request.activityId,
            request.timer.toLong(), // Added timer (converted to Long for SQLDelight)
            request.status
        )
        queries.lastInsertQuestionId().executeAsOne()
    }

    // Update existing question
    suspend fun updateQuestion(id: Long, request: UpdateQuestionRequest) = withContext(Dispatchers.IO) {
        queries.updateQuestion(
            request.text,
            request.a,
            request.b,
            request.c,
            request.d,
            request.correct,
            request.activityId,
            request.timer.toLong(), // Added timer
            request.status,
            id
        )
    }

    // Delete question
    suspend fun deleteQuestion(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteQuestion(id)
    }

    // Delete all questions
    suspend fun deleteAllQuestions() = withContext(Dispatchers.IO) {
        queries.deleteAllQuestions()
    }

    // Select all questions
    suspend fun getAllQuestions(): List<Question> = withContext(Dispatchers.IO) {
        queries.selectAllQuestions().executeAsList().map { it.toDTO() }
    }

    // Select question by ID
    suspend fun getQuestionById(id: Long): Question? = withContext(Dispatchers.IO) {
        queries.selectQuestionById(id).executeAsOneOrNull()?.toDTO()
    }

    // Select questions by activity
    suspend fun getQuestionsForActivity(activityId: Long): List<Question> = withContext(Dispatchers.IO) {
        queries.selectQuestionsForActivity(activityId).executeAsList().map { it.toDTO() }
    }

    // Select questions by status
    suspend fun getQuestionsByStatus(status: String): List<Question> = withContext(Dispatchers.IO) {
        queries.selectQuestionsByStatus(status).executeAsList().map { it.toDTO() }
    }
}

// Extension function to convert SQLDelight entity to DTO
fun Questions.toDTO() = Question(
    id = id,
    text = text,
    a = a,
    b = b,
    c = c,
    d = d,
    correct = correct,
    activityId = activityId,
    timer = timer.toInt(), // Added mapping (Long -> Int)
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)