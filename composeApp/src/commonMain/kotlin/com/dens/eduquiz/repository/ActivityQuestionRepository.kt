package com.dens.eduquiz.repository

import com.dens.eduquiz.database.ActivityQuestionQueries
import com.dens.eduquiz.database.Questions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


class ActivityQuestionRepository(private val queries: ActivityQuestionQueries) {

    // Insert relation activity ↔ question
    suspend fun insertActivityQuestion(activityId: Long, questionId: Long) =
        withContext(Dispatchers.IO) {
            queries.insertActivityQuestion(activityId, questionId)
        }

    // Get all questions associated with an activity (via join)
    suspend fun getQuestionsForActivity(activityId: Long): List<Questions> =
        withContext(Dispatchers.IO) {
            queries.selectQuestionsForActivityFromJoin(activityId).executeAsList()
        }

    // Remove all questions linked to an activity
    suspend fun deleteQuestionsFromActivity(activityId: Long) =
        withContext(Dispatchers.IO) {
            queries.deleteQuestionsFromActivity(activityId)
        }

    // Remove a specific question from an activity
    suspend fun deleteSingleQuestion(activityId: Long, questionId: Long) =
        withContext(Dispatchers.IO) {
            queries.deleteSingleQuestion(activityId, questionId)
        }
}
