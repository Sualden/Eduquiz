package com.dens.eduquiz.repository

import com.dens.eduquiz.database.Activity
import com.dens.eduquiz.database.StudentActivityQueries
import com.dens.eduquiz.database.Students
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentActivityRepository(
    private val queries: StudentActivityQueries
) {

    // Assign student to activity
    suspend fun addStudentToActivity(activityId: Long, studentId: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                queries.addStudentToActivity(activityId, studentId)
                true
            } catch (e: Exception) {
                false
            }
        }

    // Remove student from activity
    suspend fun removeStudentFromActivity(activityId: Long, studentId: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                queries.removeStudentFromActivity(activityId, studentId)
                true
            } catch (e: Exception) {
                false
            }
        }

    // Get all students assigned to an activity
    suspend fun getStudentsByActivity(activityId: Long): List<Students> =
        withContext(Dispatchers.IO) {
            try {
                queries.getStudentsByActivity(activityId).executeAsList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Get all activities assigned to a student
    suspend fun getActivitiesByStudent(studentId: Long): List<Activity> =
        withContext(Dispatchers.IO) {
            try {
                queries.getActivitiesByStudent(studentId).executeAsList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    // Get last inserted record (if needed)
    suspend fun getLastInsertId(): Long =
        withContext(Dispatchers.IO) {
            try {
                queries.getLastInsertActivityStudent().executeAsOne()
            } catch (e: Exception) {
                -1L
            }
        }
}
