package com.dens.eduquiz.repository

import CreateStudentAnswerRequest
import StudentAnswer
import UpdateStudentAnswerRequest
import com.dens.eduquiz.database.StudentAnswerQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import toDTO


class StudentAnswerRepository(private val queries: StudentAnswerQueries) {

    // Insert new student answer
    suspend fun insertStudentAnswer(request: CreateStudentAnswerRequest): Long = withContext(Dispatchers.IO) {
        queries.insertStudentAnswer(
            request.attemptId,
            request.questionId,
            request.selected,
            if (request.isCorrect) 1 else 0
        )
        queries.lastInsertStudentAnswerId().executeAsOne()
    }

    // Update existing student answer
    suspend fun updateStudentAnswer(id: Long, request: UpdateStudentAnswerRequest) = withContext(Dispatchers.IO) {
        queries.updateStudentAnswer(
            request.selected,
            if (request.isCorrect) 1 else 0,
            request.status,
            id
        )
    }

    // Delete student answer by ID
    suspend fun deleteStudentAnswer(id: Long) = withContext(Dispatchers.IO) {
        queries.deleteStudentAnswer(id)
    }

    // Delete all answers for an attempt
    suspend fun deleteAnswersByAttempt(attemptId: Long) = withContext(Dispatchers.IO) {
        queries.deleteAnswersByAttempt(attemptId)
    }

    // Get all answers for a specific attempt
    suspend fun getAnswersByAttempt(attemptId: Long): List<StudentAnswer> = withContext(Dispatchers.IO) {
        queries.getAnswersByAttempt(attemptId).executeAsList().map { it.toDTO() }
    }

    // Get all answers for a specific student
    suspend fun getAnswersByStudent(userId: Long): List<StudentAnswer> = withContext(Dispatchers.IO) {
        queries.getAnswersByStudent(userId).executeAsList().map { it.toDTO() }
    }
}


